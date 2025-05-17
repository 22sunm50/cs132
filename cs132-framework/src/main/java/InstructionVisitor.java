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
    Integer label_name_counter = 0;
    public String curr_class = null;
    public String curr_method = null;
    public String curr_sparrow_method = null;
    // ArrayList<String> reserved_names = ["a2", ""];

    public String generateTemp(){
        String name = "v" + id_name_counter;
        id_name_counter++;
        return name;
    }

    public String generateLabelName(){
        String name = "v" + label_name_counter;
        label_name_counter++;
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
    public InstrContainer visit(NotExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Evaluate the inner expression
        InstrContainer inner = n.f1.accept(this, s_table);
        result.append(inner);

        // Create constants and do: result = 1 - inner.temp
        Identifier one = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(one, 1));

        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Subtract(temp, one, inner.temp_name));

        result.setTemp(temp);
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

        Identifier this_id = new Identifier("this");
        result.setTemp(this_id);
        result.class_name = curr_class;

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

        if (curr_method != null) {
            ClassInfo classInfo = s_table.getClassInfo(curr_class);
            MethodInfo methodInfo = classInfo.getMethodInfo(curr_method);
    
            // If it's a local variable or argument, emit normal assignment
            if (methodInfo.vars_map.containsKey(varName) || methodInfo.args_map.containsKey(varName)) {
                result.addInstr(new Move_Id_Id(new Identifier(varName), rhs.temp_name));
                return result;
            }
    
            // Otherwise it's a field → emit [this + offset] = value
            int offset = classInfo.getFieldOffset(varName);

            result.addInstr(new Store(new Identifier("this"), offset, rhs.temp_name));
            return result;
        }
    
        // In main (outside method): all are locals
        result.addInstr(new Move_Id_Id(new Identifier(varName), rhs.temp_name));
        return result;
    }

    @Override
    public InstrContainer visit(minijava.syntaxtree.Identifier n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        String var_name = n.f0.toString();
        Identifier temp = new Identifier(generateTemp());
    
        // check if we're inside a method
        if (curr_method != null) {
            MethodInfo methodInfo = s_table.getClassInfo(curr_class).getMethodInfo(curr_method);
    
            // Is local var or argument?
            if (methodInfo.vars_map.containsKey(var_name) || methodInfo.args_map.containsKey(var_name)) {
                result.setTemp(new Identifier(var_name));  // use directly

                MyType type = methodInfo.vars_map.get(var_name);
                if (type == null) {
                    type = methodInfo.args_map.get(var_name);
                }
                if (type != null && type.isOfType(MyType.BaseType.CLASS)) {
                    result.setTemp(new Identifier(var_name), type.getClassName());
                } else {
                    result.setTemp(new Identifier(var_name)); // default fallback
                }

                return result;
            }
    
            // Not local → must be a field → load from [this + offset]
            ClassInfo classInfo = s_table.getClassInfo(curr_class);
            int offset = classInfo.getFieldOffset(var_name);

            Identifier thisId = new Identifier("this");
    
            result.addInstr(new Load(temp, thisId, offset));
            result.setTemp(temp);
            return result;
        }
    
        // Outside method context (e.g., in main) — assume all identifiers are locals
        result.setTemp(new Identifier(var_name));

        ClassInfo classInfo = s_table.getClassInfo(curr_class);
        MyType type = classInfo.getFieldType(var_name);  // get declared type of the variable
        result.setTemp(new Identifier(var_name), type.getClassName()); // ✅ attach class name
        
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
        Label elseLabel = new Label("L" + generateLabelName() + "_Else");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

        // Evaluate condition
        InstrContainer cond = n.f2.accept(this, s_table);
        result.append(cond);
        System.err.println("🤨 IfStatement: InstrContainer cond = " + cond);

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
        Label startLabel = new Label("L" + generateLabelName() + "_Start");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

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

    @Override
    public InstrContainer visit(AndExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Labels for short-circuit logic
        Label falseLabel = new Label("L" + generateLabelName() + "_False");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

        // 1. Evaluate the left side
        InstrContainer left = n.f0.accept(this, s_table);
        result.append(left);

        // 2. If left is false, jump to falseLabel
        result.addInstr(new IfGoto(left.temp_name, falseLabel));  // if0 left → false

        // 3. Evaluate the right side
        InstrContainer right = n.f2.accept(this, s_table);
        result.append(right);

        // 4. Assign final result = right
        Identifier finalTemp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Id(finalTemp, right.temp_name));
        result.addInstr(new Goto(endLabel));

        // 5. Label false branch
        result.addInstr(new LabelInstr(falseLabel));
        result.addInstr(new Move_Id_Integer(finalTemp, 0)); // false = 0

        // 6. End label
        result.addInstr(new LabelInstr(endLabel));
        result.setTemp(finalTemp);

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
        Integer fieldCount = classInfo.field_table_list.size();
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
        for (MethodOrigin mo : classInfo.method_origin_list) {
            String methodName = mo.methodName;
            String originClass = mo.className;
    
            // Correct label = originClass_methodName
            FunctionName sparrow_func_name = new FunctionName(originClass + "_" + methodName);
            System.err.println("👇 Allocation Expr: sparrow func name = " + sparrow_func_name);
            Integer offset = classInfo.getMethodOffset(methodName);
    
            Identifier func_ptr = new Identifier(generateTemp());
            result.addInstr(new Move_Id_FuncName(func_ptr, sparrow_func_name)); // fptr = @Class_Method
            result.addInstr(new Store(vmt_name, offset, func_ptr));             // [vmt + offset] = func_ptr
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
        Label nullLabel = new Label("L" + generateLabelName() + "_Error");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

        result.addInstr(new IfGoto(obj_temp, nullLabel));  // if0 obj → jump to null handler
        result.addInstr(new Goto(endLabel));               // skip error if not null
        result.addInstr(new LabelInstr(nullLabel));
        result.addInstr(new ErrorMessage("\"null pointer\""));        // throw runtime error
        result.addInstr(new LabelInstr(endLabel));

        // 3. Get method name
        String methodName = n.f2.f0.toString();

        System.err.println("📣 MessageSend: obj's class_name = " + className + " | calling method = " + methodName);
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
