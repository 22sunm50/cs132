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
        String varName = n.f0.toString();
        // Identifier temp = new Identifier(generateTemp());

        // Default class context
        ClassInfo classInfo = s_table.getClassInfo(curr_class);
        MethodInfo methodInfo = curr_method != null ? classInfo.getMethodInfo(curr_method) : null;

        // 1. Local variable
        if (methodInfo != null && methodInfo.hasVar(varName)) {
            result.setTemp(new Identifier(varName));
            MyType type = methodInfo.getVarType(varName);
            if (type != null && type.isOfType(MyType.BaseType.CLASS)) {
                result.setTemp(new Identifier(varName), type.getClassName());
            }
            return result;
        }

        // 2. Method parameter
        if (methodInfo != null && methodInfo.hasArg(varName)) {
            result.setTemp(new Identifier(varName));
            MyType type = methodInfo.getArgType(varName);
            if (type != null && type.isOfType(MyType.BaseType.CLASS)) {
                result.setTemp(new Identifier(varName), type.getClassName());
            }
            return result;
        }

        // 3. Field access: load from [this + offset]
        if (methodInfo != null && curr_class != null && classInfo.hasField(varName)) {
            int offset = classInfo.getFieldOffset(varName);
            Identifier fieldTemp = new Identifier(generateTemp());
            result.addInstr(new Load(fieldTemp, new Identifier("this"), offset));
            System.err.println("🕵️‍♀️ Identifier: curr_class = " + curr_class + " | curr_method = " + curr_method + " | var name = " + varName);
            MyType type = classInfo.getFieldType(varName);
            if (type != null && type.isOfType(MyType.BaseType.CLASS)) {
                result.setTemp(fieldTemp, type.getClassName());
            } else {
                result.setTemp(fieldTemp);
            }
            return result;
        }

        // 4. Global/local in main
        if (s_table.getClassInfo(curr_class).getFieldType(varName).isOfType(MyType.BaseType.CLASS)) { // if local is of type CLASS
            String var_type = s_table.getClassInfo(curr_class).getFieldType(varName).getClassName();var_type = s_table.getClassInfo(curr_class).getFieldType(varName).getClassName();
            result.setTemp(new Identifier(varName), var_type);  // Default to just the name
            return result;
        }
        result.setTemp(new Identifier(varName));  // Default to just the name if primitive type
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
    
        // Labels
        Label falseLabel = new Label("L" + generateLabelName() + "_False");
        Label endLabel = new Label("L" + generateLabelName() + "_End");
    
        // Step 1: Evaluate left
        InstrContainer left = n.f0.accept(this, s_table);
        result.append(left);
    
        // Step 2: If left == 0, short-circuit to false
        result.addInstr(new IfGoto(left.temp_name, falseLabel)); // if0 left -> false
    
        // Step 3: Evaluate right
        InstrContainer right = n.f2.accept(this, s_table);
        result.append(right);
    
        // Step 4: result = right
        Identifier resultTemp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Id(resultTemp, right.temp_name));
        result.addInstr(new Goto(endLabel));
    
        // Step 5: false branch
        result.addInstr(new LabelInstr(falseLabel));
        result.addInstr(new Move_Id_Integer(resultTemp, 0));
    
        // Step 6: end
        result.addInstr(new LabelInstr(endLabel));
        result.setTemp(resultTemp);
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

        // if (obj.class_name == null && obj.temp_name.toString().equals("this")) {
        if (obj.temp_name.toString().equals("this")) {
            obj.class_name = curr_class;
        }

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

                // if (arg_instr.class_name == null && arg_instr.temp_name.toString().equals("this")) {
                if (arg_instr.temp_name.toString().equals("this")) {
                    arg_instr.class_name = curr_class;
                }
            }
        }

        // 7. Prepare return temp and emit call
        Identifier ret_temp = new Identifier(generateTemp());

        String ret_class = null;
        MyType ret_type = classInfo.getMethodInfo(methodName).return_type;
        if (ret_type.isOfType(MyType.BaseType.CLASS)){
            ret_class = ret_type.getClassName();
        }

        // Argument list: receiver (obj) + all evaluated args
        ArrayList<Identifier> allArgs = new ArrayList<>();
        allArgs.add(obj_temp); // 'this' pointer

        allArgs.addAll(arg_temps);

        result.addInstr(new Call(ret_temp, func_ptr, allArgs));
        result.setTemp(ret_temp, ret_class);

        return result;
    }

    @Override
    public InstrContainer visit(ArrayAllocationExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Step 1: Evaluate the array length expression inside brackets
        InstrContainer lengthExpr = n.f3.accept(this, s_table);  // `new int[expr]`
        result.append(lengthExpr);

        // Step 2: Add 1 to account for storing array length
        Identifier one = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(one, 1));

        Identifier paddedLength = new Identifier(generateTemp());
        result.addInstr(new Add(paddedLength, lengthExpr.temp_name, one)); // paddedLength = length + 1

        // Step 3: Multiply by 4 (size of int)
        Identifier intSize = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(intSize, 4));

        Identifier allocSize = new Identifier(generateTemp());
        result.addInstr(new Multiply(allocSize, paddedLength, intSize)); // allocSize = (length + 1) * 4

        // Step 4: Allocate memory
        Identifier arrayPtr = new Identifier(generateTemp());
        result.addInstr(new Alloc(arrayPtr, allocSize)); // arrayPtr = alloc(allocSize)

        // Step 5: Null pointer check
        Label errorLabel = new Label("L" + generateLabelName() + "_Error");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

        result.addInstr(new IfGoto(arrayPtr, errorLabel));  // if0 arrayPtr → error
        result.addInstr(new Goto(endLabel));                // else → continue

        result.addInstr(new LabelInstr(errorLabel));
        result.addInstr(new ErrorMessage("\"null pointer\""));

        result.addInstr(new LabelInstr(endLabel));

        // Step 6: Store original length at [arrayPtr + 0]
        result.addInstr(new Store(arrayPtr, 0, lengthExpr.temp_name));

        // Done: set result.temp_name
        result.setTemp(arrayPtr);

        return result;
    }

    @Override
    public InstrContainer visit(ArrayAssignmentStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Step 1: Evaluate array reference
        InstrContainer arrayRef = n.f0.accept(this, s_table); // arr
        result.append(arrayRef);
        Identifier arr = arrayRef.temp_name;

        // Step 2: Evaluate index
        InstrContainer indexExpr = n.f2.accept(this, s_table); // i
        result.append(indexExpr);
        Identifier index = indexExpr.temp_name;

        // Step 3: Evaluate value to store
        InstrContainer valueExpr = n.f5.accept(this, s_table); // val
        result.append(valueExpr);
        Identifier value = valueExpr.temp_name;

        // Constants
        Identifier four = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(four, 4));

        Identifier zero = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(zero, 0));

        // Step 4: Bounds checking
        // Compute -1 using: -1 = 4 - 5
        Identifier five = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(five, 5));
        Identifier minusOne = new Identifier(generateTemp());
        result.addInstr(new Subtract(minusOne, four, five));  // -1

        Identifier isNonNegative = new Identifier(generateTemp());
        result.addInstr(new LessThan(isNonNegative, minusOne, index)); // -1 < index

        Identifier arrLen = new Identifier(generateTemp());
        result.addInstr(new Load(arrLen, arr, 0));  // arrLen = [arr + 0]

        Identifier isInBound = new Identifier(generateTemp());
        result.addInstr(new LessThan(isInBound, index, arrLen));  // index < arrLen

        Identifier inBounds = new Identifier(generateTemp());
        result.addInstr(new Multiply(inBounds, isNonNegative, isInBound));  // AND condition

        // Labels for bound check
        Label errorLabel = new Label("L" + generateLabelName() + "_BoundsError");
        Label endLabel = new Label("L" + generateLabelName() + "_End");

        result.addInstr(new IfGoto(inBounds, errorLabel));
        result.addInstr(new Goto(endLabel));
        result.addInstr(new LabelInstr(errorLabel));
        result.addInstr(new ErrorMessage("\"array index out of bounds\""));
        result.addInstr(new LabelInstr(endLabel));

        // Step 5: Compute address = arr + 4 * index + 4
        Identifier offset = new Identifier(generateTemp());
        result.addInstr(new Multiply(offset, four, index)); // offset = 4 * index

        Identifier dataOffset = new Identifier(generateTemp());
        result.addInstr(new Add(dataOffset, offset, four)); // offset = offset + 4

        Identifier targetAddr = new Identifier(generateTemp());
        result.addInstr(new Add(targetAddr, arr, dataOffset)); // final address

        // Step 6: Store value
        result.addInstr(new Store(targetAddr, 0, value)); // [targetAddr + 0] = value

        return result;
    }

    @Override
    public InstrContainer visit(ArrayLookup n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Step 1: Evaluate array and index
        InstrContainer arrExpr = n.f0.accept(this, s_table);
        InstrContainer indexExpr = n.f2.accept(this, s_table);
        result.append(arrExpr);
        result.append(indexExpr);

        Identifier arr = arrExpr.temp_name;
        Identifier index = indexExpr.temp_name;

        // Step 2: Load constants
        Identifier four = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(four, 4));

        Identifier five = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(five, 5));

        Identifier minusOne = new Identifier(generateTemp());
        result.addInstr(new Subtract(minusOne, four, five));  // -1

        // Step 3: -1 < index
        Identifier lowBound = new Identifier(generateTemp());
        result.addInstr(new LessThan(lowBound, minusOne, index));

        // Step 4: index < length
        Identifier arrLen = new Identifier(generateTemp());
        result.addInstr(new Load(arrLen, arr, 0)); // length = [arr + 0]

        Identifier highBound = new Identifier(generateTemp());
        result.addInstr(new LessThan(highBound, index, arrLen));

        // Step 5: Combine bounds
        Identifier inBounds = new Identifier(generateTemp());
        result.addInstr(new Multiply(inBounds, lowBound, highBound));

        // Labels
        Label boundsError = new Label("L" + generateLabelName() + "_BoundsError");
        Label boundsOK = new Label("L" + generateLabelName() + "_BoundsOK");

        result.addInstr(new IfGoto(inBounds, boundsError));
        result.addInstr(new Goto(boundsOK));
        result.addInstr(new LabelInstr(boundsError));
        result.addInstr(new ErrorMessage("\"array index out of bounds\""));
        result.addInstr(new LabelInstr(boundsOK));

        // Step 6: Compute offset = index * 4 + 4
        Identifier scaled = new Identifier(generateTemp());
        result.addInstr(new Multiply(scaled, four, index));

        Identifier offset = new Identifier(generateTemp());
        result.addInstr(new Add(offset, scaled, four)); // offset = scaled + 4

        Identifier addr = new Identifier(generateTemp());
        result.addInstr(new Add(addr, arr, offset));

        // Step 7: Load [addr + 0]
        Identifier value = new Identifier(generateTemp());
        result.addInstr(new Load(value, addr, 0));

        result.setTemp(value);
        return result;
    }

    @Override
    public InstrContainer visit(ArrayLength n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Evaluate the array expression
        InstrContainer arrayExpr = n.f0.accept(this, s_table);
        result.append(arrayExpr);

        Identifier arr = arrayExpr.temp_name;

        // Load [arr + 0]
        Identifier length = new Identifier(generateTemp());
        result.addInstr(new Load(length, arr, 0));

        result.setTemp(length);
        return result;
    }

}
