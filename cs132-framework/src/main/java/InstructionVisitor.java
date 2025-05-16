import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;

import IR.token.FunctionName;
import IR.token.Identifier;
import IR.token.Label;
import sparrow.*;
// import sparrowv.Print;

public class InstructionVisitor extends GJDepthFirst < InstrContainer, SymbolTable > {

    // global counter for our identifier name generator
    Integer id_name_counter = 0;
    public String curr_class = null;
    public String curr_method = null;
    public String curr_sparrow_method = null;

    public String generateTemp(){
        String name = "v" + id_name_counter;
        id_name_counter++;
        return name;
    }

    @Override
    public InstrContainer visit(Goal n, SymbolTable s_table) {
        return n.f0.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(MainClass n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        for (Node node : n.f15.nodes){
            InstrContainer statement = node.accept(this, s_table);
            result.instr_list.addAll(statement.instr_list);
        }
        return result;
    }

    @Override
    public InstrContainer visit(Statement n, SymbolTable s_table) {
        InstrContainer result = n.f0.accept(this, s_table);
        return result;
    }

    @Override
    public InstrContainer visit(PrintStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        InstrContainer content = n.f2.accept(this, s_table);
        result.instr_list.addAll(content.instr_list);

        Print p = new Print(content.temp_name);
        result.instr_list.add(p);
        result.setTemp(content.temp_name);

        return result;
    }

    @Override
    public InstrContainer visit(Expression n, SymbolTable s_table) {
        return n.f0.choice.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(PrimaryExpression n, SymbolTable s_table) {
        return n.f0.choice.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(IntegerLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        Identifier temp_id = new Identifier(generateTemp());
        int int_val = Integer.parseInt(n.f0.toString());        
        result.instr_list.add(new Move_Id_Integer(temp_id, int_val)); // set instr list
        result.temp_name = (temp_id); // set temp name
        return result;
    }

    @Override
    public InstrContainer visit(TrueLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(temp, 1));  // true = 1
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(FalseLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(temp, 0));  // false = 0
        result.setTemp(temp, null);

        return result;
    }

    @Override
    public InstrContainer visit(PlusExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        InstrContainer left = n.f0.accept(this, s_table);
        InstrContainer right = n.f2.accept(this, s_table);
        
        result.instr_list=left.instr_list;
        result.instr_list.addAll(right.instr_list);
        result.temp_name = new Identifier(generateTemp());
        result.instr_list.add(new Add(result.temp_name, left.temp_name, right.temp_name));
        return result;
    }

    @Override
    public InstrContainer visit(MinusExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // evaluate the left operand
        InstrContainer left = n.f0.accept(this, s_table);
        result.append(left);
        // evaluate the right operand
        InstrContainer right = n.f2.accept(this, s_table);
        result.append(right);

        // create a new temp for the result
        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Subtract(temp, left.temp_name, right.temp_name));

        // set the result temp
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(TimesExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Evaluate left operand
        InstrContainer left = n.f0.accept(this, s_table);
        result.append(left);

        // Evaluate right operand
        InstrContainer right = n.f2.accept(this, s_table);
        result.append(right);

        // Generate a temp for the result
        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Multiply(temp, left.temp_name, right.temp_name));
        
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(ThisExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // "this" is passed as the first argument to every method
        Identifier this_id = new Identifier("this");
        result.setTemp(this_id);

        // Also store the current class name (needed for MessageSend)
        result.class_name = curr_class; // does this work?
        System.err.println("👇 This: curr_class = " + curr_class);

        return result;
    }


    @Override
    public InstrContainer visit(AssignmentStatement n, SymbolTable s_table) { // 🍅 🍅 🍅 : deal with polymorphism later?
        InstrContainer result = new InstrContainer();

        // the var name being assigned to
        String varName = n.f0.f0.toString();

        // rhs
        InstrContainer rhs = n.f2.accept(this, s_table);
        result.instr_list.addAll(rhs.instr_list);
    
        // wrap var as an Identifier
        Identifier id = new Identifier(varName);
    
        // add assignment: id = rhs.temp_name
        result.addInstr(new Move_Id_Id(id, rhs.temp_name));
    
        // we don’t need to set temp_name for statements
        return result;
    }

    @Override
    public InstrContainer visit(minijava.syntaxtree.Identifier n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        Identifier var = new Identifier(n.f0.toString());
        result.setTemp(var);
        return result;
    }

    @Override
    public InstrContainer visit(CompareExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Evaluate both sides
        InstrContainer left = n.f0.accept(this, s_table);
        InstrContainer right = n.f2.accept(this, s_table);

        // Combine instructions from both sides
        result.append(left);
        result.append(right);

        // Generate a new temp for the result
        Identifier temp = new Identifier(generateTemp());

        // Add the < instruction
        result.addInstr(new LessThan(temp, left.temp_name, right.temp_name));

        // Set the result temp
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(BracketExpression n, SymbolTable s_table) {
        return n.f1.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(IfStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Generate labels
        Label elseLabel = new Label("L" + generateTemp() + "_Else");
        Label endLabel = new Label("L" + generateTemp() + "_End");

        // Evaluate condition
        InstrContainer cond = n.f2.accept(this, s_table);
        result.append(cond);

        // Conditional jump
        result.addInstr(new IfGoto(cond.temp_name, elseLabel)); // if0 cond goto elseLabel

        // THEN block
        InstrContainer thenBlock = n.f4.accept(this, s_table);
        result.append(thenBlock);
        result.addInstr(new Goto(endLabel)); // jump to end after then block

        // ELSE block
        result.addInstr(new LabelInstr(elseLabel));
        InstrContainer elseBlock = n.f6.accept(this, s_table);
        result.append(elseBlock);

        // End label
        result.addInstr(new LabelInstr(endLabel));

        return result;
    }

    @Override
    public InstrContainer visit(WhileStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Labels for the loop
        Label startLabel = new Label("L" + generateTemp() + "_Start");
        Label endLabel = new Label("L" + generateTemp() + "_End");

        // Start label
        result.addInstr(new LabelInstr(startLabel));

        // Evaluate the loop condition
        InstrContainer cond = n.f2.accept(this, s_table);
        result.append(cond);

        // If condition is false, jump to end
        result.addInstr(new IfGoto(cond.temp_name, endLabel)); // if0 cond → end

        // Loop body
        InstrContainer body = n.f4.accept(this, s_table);
        result.append(body);

        // Jump back to the start
        result.addInstr(new Goto(startLabel));

        // End label
        result.addInstr(new LabelInstr(endLabel));

        return result;
    }

    @Override // doin this for multiple statements (like in IfStatement)
    public InstrContainer visit(minijava.syntaxtree.Block n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        for (Node stmtNode : n.f1.nodes) {
            InstrContainer inner = stmtNode.accept(this, s_table);
            if (inner != null) {
                result.append(inner);
            }
        }

        return result;
    }  

    @Override
    public InstrContainer visit(AllocationExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // get class name being allocated
        String className = n.f1.f0.toString();
        ClassInfo classInfo = s_table.getClassInfo(className);

        // compute total allocation size = 4 bytes * (number of fields + 1)
        Integer fieldCount = classInfo.fields_map.size();
        Integer field_size = (fieldCount + 1) * 4;

        // temp to hold the alloc size
        Identifier field_size_temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(field_size_temp, field_size));

        // generate a temp for the result (store the object instance - AKA the field table)
        Identifier instance_temp = new Identifier(generateTemp());
        result.addInstr(new Alloc(instance_temp, field_size_temp)); // add the alloc instruction
        result.setTemp(instance_temp, className); // set the resulting temp name

        //// create VMT
        Integer vmt_size = classInfo.methods_map.size() * 4;
        // temp to hold vmt size -> vmt_size_temp = vmt_size
        Identifier vmt_size_temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(vmt_size_temp, vmt_size));
        // temp to hold vmt -> vmt_name = alloc(vmt_size_temp)
        Identifier vmt_name = new Identifier("vmt_" + className);
        result.addInstr(new Alloc(vmt_name, vmt_size_temp));

        //// Loading Functions into VMT
        for (String methodName : classInfo.methods_map.keySet()) {
            FunctionName sparrow_func_name = new FunctionName(className + "_" + methodName);
            Integer offset = classInfo.getMethodOffset(methodName);

            // fptr = @Class_Method
            Identifier func_ptr = new Identifier(generateTemp());
            result.addInstr(new Move_Id_FuncName(func_ptr, sparrow_func_name));

            // [vmt + offset] = func_ptr
            result.addInstr(new Store(vmt_name, offset, func_ptr));
        }

        //// Loading vmt ptr into field's table -> [instance + 0] = vmt
        result.addInstr(new Store(instance_temp, 0, vmt_name));

        return result;
    }

    @Override
    public InstrContainer visit(MessageSend n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // 1. Evaluate the object expression (e.g., a.run().dog -> get 'a')
        InstrContainer obj = n.f0.accept(this, s_table);
        result.append(obj);

        // 2. Extract the object class from the symbol table
        Identifier obj_temp = obj.temp_name;
        String className = obj.class_name;
        ClassInfo classInfo = s_table.getClassInfo(className);

        //// 🍅 NULL CHECK!
        // Insert null pointer check before dereferencing the object
        Label nullLabel = new Label("L" + generateTemp() + "_Error");
        Label endLabel = new Label("L" + generateTemp() + "_End");

        result.addInstr(new IfGoto(obj_temp, nullLabel));  // if0 obj → jump to null handler
        result.addInstr(new Goto(endLabel));               // skip error if not null
        result.addInstr(new LabelInstr(nullLabel));
        result.addInstr(new ErrorMessage("\"null pointer\""));        // throw runtime error
        result.addInstr(new LabelInstr(endLabel));

        // 3. Get method name
        String methodName = n.f2.f0.toString();
        int methodOffset = classInfo.getMethodOffset(methodName);

        // 4. Load vmt ptr from vmt_ptr = [obj + 0]
        Identifier vmt_ptr = new Identifier(generateTemp());
        result.addInstr(new Load(vmt_ptr, obj_temp, 0));

        // 5. Load function ptr from func_ptr = vmt_ptr = [vmt + methodOffset]
        Identifier func_ptr = new Identifier(generateTemp());
        result.addInstr(new Load(func_ptr, vmt_ptr, methodOffset));

        // 6. Evaluate arguments
        ArrayList<Identifier> arg_temps = new ArrayList<Identifier>();
        if (n.f4.present()){
            ExpressionList args_exprList = (ExpressionList) n.f4.node;

            InstrContainer first_instr = args_exprList.f0.accept(this, s_table);
            result.append(first_instr);
            arg_temps.add(first_instr.temp_name);
            for (Node node : args_exprList.f1.nodes) {
                ExpressionRest rest = (ExpressionRest) node;
                InstrContainer arg_instr = rest.f1.accept(this, s_table);
                result.append(arg_instr);
                arg_temps.add(arg_instr.temp_name);
            }
        }

        System.err.println("📣 Message Send: arg_temps list = " + arg_temps);

        // 7. Prepare return temp and emit call
        Identifier ret_temp = new Identifier(generateTemp());

        // Argument list: receiver (obj) + all evaluated args
        ArrayList<Identifier> allArgs = new ArrayList<>();
        allArgs.add(obj_temp); // 'this' pointer
        allArgs.addAll(arg_temps);

        result.addInstr(new Call(ret_temp, func_ptr, allArgs));
        result.setTemp(ret_temp);

        return result;
    }
}
