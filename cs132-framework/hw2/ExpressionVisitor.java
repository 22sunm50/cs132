import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class ExpressionVisitor extends GJDepthFirst<MyType, SymbolTable> {
    String curr_class;
    String curr_method;

    private Stack<List<MyType>> expressionListStack = new Stack<>();

    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // 🧮 🧮 🧮 🧮 🧮 🧮 🧮 LITERALS 🧮 🧮 🧮 🧮 🧮 🧮 🧮 🧮
    @Override
    public MyType visit(IntegerLiteral n, SymbolTable s_table) {
        // n.f0 gives the actual integer value
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit(TrueLiteral n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(FalseLiteral n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    // 📣 📣 📣 📣 📣 📣 📣 📣 📣 EXPRESSIONS 📣 📣 📣 📣 📣 📣 📣 📣 📣
    @Override
    public MyType visit(PlusExpression n, SymbolTable s_table) {
        MyType lhs = n.f0.accept(this, s_table);
        MyType rhs = n.f2.accept(this, s_table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            System.err.println("🚨: plus expr error --> lhs type = " + lhs + "rhs type = " + rhs);
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public MyType visit(MinusExpression n, SymbolTable s_table) {
        MyType lhs = n.f0.accept(this, s_table);
        MyType rhs = n.f2.accept(this, s_table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            System.err.println("🚨: minus expr error --> lhs type = " + lhs + "rhs type = " + rhs);
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public MyType visit(AndExpression n, SymbolTable s_table) {
        MyType lhs = n.f0.accept(this, s_table);
        MyType rhs = n.f2.accept(this, s_table);

        MyType boolType = new MyType(MyType.BaseType.BOOLEAN);

        if (lhs.equals(boolType) && rhs.equals(boolType)) { return boolType; }
        System.err.println("🚨: and expr error --> lhs type = " + lhs + "rhs type = " + rhs);
        printFailureAndExit();
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(TimesExpression n, SymbolTable s_table) {
        MyType lhs = n.f0.accept(this, s_table);
        MyType rhs = n.f2.accept(this, s_table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            System.err.println("🚨: times expr error --> lhs type = " + lhs + "rhs type = " + rhs);
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public MyType visit(NotExpression n, SymbolTable s_table) {
        MyType expr = n.f1.f0.accept(this, s_table);

        MyType boolType = new MyType(MyType.BaseType.BOOLEAN);

        System.err.println("! ! ! ! ! NOT EXPR: the expr = " + expr);

        if (!expr.equals(boolType)){ 
            System.err.println("🚨: not expr error --> expr type = " + expr);
            printFailureAndExit(); 
        }
        return boolType;
    }

    @Override
    public MyType visit(CompareExpression n, SymbolTable s_table) {
        MyType lhs = n.f0.accept(this, s_table);
        MyType rhs = n.f2.accept(this, s_table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            System.err.println("🚨: compare expr error --> lhs type = " + lhs + "rhs type = " + rhs);
            printFailureAndExit();
        }
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(BracketExpression n, SymbolTable s_table) {
        MyType expr = n.f1.f0.accept(this, s_table);
        return expr;
    }

    @Override
    public MyType visit(PrimaryExpression n, SymbolTable s_table) {
        MyType prim_expr_type = n.f0.accept(this, s_table);

        // if it is an identifier --> ret as var's type
        if (prim_expr_type.getBaseType() == MyType.BaseType.ID){ 
            String id_name = prim_expr_type.getClassName();
            if (curr_method == null){ // get field's type
                prim_expr_type = s_table.getClassInfo(curr_class).getFieldType(id_name);
            }
            else { // get local var's type
                prim_expr_type = s_table.getClassInfo(curr_class).getMethodInfo(curr_method).getVarOrArgType(id_name);
            }
        }

        // it is a CLASS (new allocation) -> ret as class's type
        if (prim_expr_type.getBaseType() == MyType.BaseType.CLASS){
            String class_name = prim_expr_type.getClassName();
            if (!s_table.hasClass(class_name)) { // non existent class
                System.err.println("🚨 Primary Expr: This class does not exist -> " + class_name);
                printFailureAndExit();
            }
            prim_expr_type = new MyType(MyType.BaseType.CLASS, class_name);
        }
        return prim_expr_type;
    }

    @Override
    public MyType visit(PrintStatement n, SymbolTable s_table) {
        MyType expr_type = n.f2.f0.accept(this, s_table);
        if (!expr_type.isOfType(MyType.BaseType.INT)){
            System.err.println("🚨: Print Expression not int, instead is: " + expr_type.getBaseType());
            printFailureAndExit();
        }
        System.err.println("🖨️ Print: SUCCESS ✅");
        return expr_type;
    }

    @Override
    public MyType visit(IfStatement n, SymbolTable s_table) {
        MyType cond_type = n.f2.f0.accept(this, s_table);
        if (!cond_type.isOfType(MyType.BaseType.BOOLEAN)){
            System.err.println("🚨: If condition type = " + cond_type);
            printFailureAndExit();
        }

        n.f4.accept(this, s_table); // statements after if
        n.f6.accept(this, s_table); // statements after else

        return cond_type;
    }

    @Override
    public MyType visit(WhileStatement n, SymbolTable s_table) {
        MyType cond_type = n.f2.f0.accept(this, s_table);
        if (!cond_type.isOfType(MyType.BaseType.BOOLEAN)){
            System.err.println("🚨: while condition type = " + cond_type);
            printFailureAndExit();
        }

        n.f4.accept(this, s_table); // statements after while

        return cond_type;
    }


    @Override
    public MyType visit(MessageSend n, SymbolTable s_table) {
        MyType raw_obj_type = n.f0.f0.accept(this, s_table);
        MyType actual_obj_type = raw_obj_type;

        // 📦 📦 📦 📦 📦 CHECK OBJ TYPE 📦 📦 📦 📦 📦
        // called on a variable
        if (raw_obj_type.isOfType(MyType.BaseType.ID)){
            String raw_obj_name = raw_obj_type.getClassName();
            if (curr_method == null) {  // get type of object from fields
                actual_obj_type = s_table.getClassInfo(curr_class).getFieldType(raw_obj_name);
            }
            else {  // get type of object from method vars
                actual_obj_type = s_table.getClassInfo(curr_class).getMethodInfo(curr_method).getVarOrArgType(raw_obj_name);
            }
        }

        // call method on literal
        if (!actual_obj_type.isOfType(MyType.BaseType.CLASS)){ 
            System.err.println("🚨 Method Call: Can't call method on non-class type: " + actual_obj_type);
            printFailureAndExit();
        }

        String obj_class_name = actual_obj_type.getClassName();
        if (!s_table.hasClass(obj_class_name)) { // call method on non-existent class
            System.err.println("🚨 Method Call: non-existent class type: " + actual_obj_type);
            printFailureAndExit();
        }
        
        // 📦 📦 📦 📦 📦 CHECK METHOD CALLED 📦 📦 📦 📦 📦
        String called_method_name = n.f2.f0.toString();
        System.err.println("🤙 Method Call: Method (" + called_method_name + ")");
        // check if method for the class exists
        if (!s_table.getClassInfo(obj_class_name).hasMethod(called_method_name)){   // method DNE
            System.err.println("🚨 Method Call: non-existent method: " + called_method_name);
            printFailureAndExit();
        }

        MethodInfo method_info = s_table.getClassInfo(obj_class_name).getMethodInfo(called_method_name);
        List<MyType> expectedArgTypes = method_info.getArgsTypeList();
    
        // Visit ExpressionList to push a new list onto the stack
        if (n.f4.present()) {
            n.f4.accept(this, s_table); // This fills the top of the stack
        } else {
            expressionListStack.push(new ArrayList<>()); // No params, push empty
        }

        List<MyType> actualArgTypes = expressionListStack.pop(); // Retrieve the correct list
        System.err.println("📋 Method Call: Method (" + called_method_name + "): the 📋 args List: " + actualArgTypes);

        // 📦 📦 📦 📦 📦 Check argument count 📦 📦 📦 📦 📦
        if (expectedArgTypes.size() != actualArgTypes.size()) {
            System.err.println("🚨 Method Call: argument count mismatch for method: " + called_method_name);
            printFailureAndExit();
        }

        MyType ret_type = s_table.getClassInfo(obj_class_name).getMethodInfo(called_method_name).getReturnType();
        System.err.println("⏪ Method Call: Method (" + called_method_name + ") Return type = " + ret_type);

        // 📦 📦 📦 📦 📦 Check each argument type 📦 📦 📦 📦 📦
        for (int i = 0; i < expectedArgTypes.size(); i++) {
            MyType expected_type = expectedArgTypes.get(i);
            MyType actual_type = actualArgTypes.get(i);
            // if (expectedArgTypes.get(i).equals(actualArgTypes.get(i)) || ) {
            // if equals or subtype -> good!
            if (expected_type.equals(actual_type) || s_table.isSubtype(actual_type.getClassName(), expected_type.getClassName())){
                return ret_type;
            }
            else {
                System.err.println("🚨 Method Call (" + called_method_name + "): argument type mismatch at position " + i + ". Expected: " + expectedArgTypes.get(i) + ", Got: " + actualArgTypes.get(i));
                printFailureAndExit();
            }
        }
        
        return ret_type;
    }

    @Override
    public MyType visit(ThisExpression n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.CLASS, curr_class);
    }
    
    @Override
    public MyType visit(ExpressionList n, SymbolTable s_table) {
        List<MyType> typeList = new ArrayList<>();
        expressionListStack.push(typeList); // Push a new list for this context
    
        // Visit the first expression and add its type
        MyType firstType = n.f0.f0.accept(this, s_table);
        typeList.add(firstType);

        // Manually iterate over each ExpressionRest in NodeListOptional
        if (n.f1.present()) {
            for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
                ExpressionRest exprRest = (ExpressionRest) e.nextElement();
                MyType restType = exprRest.accept(this, s_table); // Uses your ExpressionRest visitor
                typeList.add(restType);
            }
        }

        return null;
    }

    @Override
    public MyType visit(ExpressionRest n, SymbolTable s_table) {
        // Visit the first expression and add its type
        MyType nextType = n.f1.f0.accept(this, s_table);        
        return nextType;
    }

    @Override
    public MyType visit(ArrayAssignmentStatement n, SymbolTable s_table) {
        String id_string = n.f0.f0.toString();
        
        // check ID is an int_array
        if (curr_method == null){
            MyType id_type = s_table.getClassInfo(curr_class).getFieldType(id_string);
            // ID exists + ID type is int_array
            if (!id_type.isOfType(MyType.BaseType.INT_ARRAY)){
                System.err.println("🚨 Array Assignment: ID (" + id_string + ") is actually type = " + id_type);
                printFailureAndExit();
            }
        }
        else {
            MyType id_type = s_table.getClassInfo(curr_class).getMethodInfo(curr_method).getVarOrArgType(id_string);
            // if var exists + var type is int_array
            if (!id_type.isOfType(MyType.BaseType.INT_ARRAY)){
                System.err.println("🚨 Array Assignment: ID (" + id_string + ") is actually type = " + id_type);
                printFailureAndExit();
            }
        }

        // check index = int
        MyType index_type = n.f2.f0.accept(this, s_table);
        if (!index_type.isOfType(MyType.BaseType.INT)){
            System.err.println("🚨 Array Assignment: index type (not int) type = " + index_type);
            printFailureAndExit();
        }

        // check assigned = int
        MyType assigned_type = n.f5.f0.accept(this, s_table);
        if (!assigned_type.isOfType(MyType.BaseType.INT)){
            System.err.println("🚨 Array Assignment: assigned type (not int) type = " + assigned_type);
            printFailureAndExit();
        }

        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit(ArrayAllocationExpression n, SymbolTable s_table) {
        MyType expr_type = n.f3.f0.accept(this, s_table);
        if (!expr_type.isOfType(MyType.BaseType.INT)){
            System.err.println("🚨 Array Alloc: expr type (not int) type = " + expr_type);
            printFailureAndExit();
        }
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit(ArrayLength n, SymbolTable s_table) {
        MyType expr_type = n.f0.accept(this, s_table);
        if (!expr_type.isOfType(MyType.BaseType.INT_ARRAY)){
            System.err.println("🚨 Array Length: (" + expr_type.getClassName() + ") expr type (not int_arr) type = " + expr_type);
            printFailureAndExit();
        }
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit(ArrayLookup n, SymbolTable s_table) {
        MyType id_type = n.f0.accept(this, s_table);

        if (!id_type.isOfType(MyType.BaseType.INT_ARRAY)){
            System.err.println("🚨 Array Lookup: id type (not int_arr) type = " + id_type);
            printFailureAndExit();
        }

        MyType index_type = n.f2.accept(this, s_table);
        if (!index_type.isOfType(MyType.BaseType.INT)){
            System.err.println("🚨 Array Lookup: index type (not int_arr) type = " + index_type);
            printFailureAndExit();
        }

        return new MyType(MyType.BaseType.INT);
    }

    // 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ SYMBOL TABLE 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️
    // ALL POSSIBLE Type()
    @Override
    public MyType visit(ArrayType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit(BooleanType n, SymbolTable s_table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(IntegerType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit(Identifier n, SymbolTable s_table) {
        String var_name = n.f0.toString();
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit(ClassDeclaration n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString();
        System.err.println("🥕 🥕 🥕 🥕 🥕 Class Dec (" + class_name + ")");

        curr_class = class_name;
        curr_method = null;

        n.f3.accept(this, s_table); // var dec list
        n.f4.accept(this, s_table); // method dec list

        return null;
    }

    @Override
    public MyType visit(ClassExtendsDeclaration n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString();

        curr_class = class_name;
        curr_method = null;

        n.f5.accept(this, s_table); // var dec list
        n.f6.accept(this, s_table); // method dec list

        return null;
    }

    @Override
    public MyType visit(MainClass n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString(); // "Main"

        curr_class = class_name;
        curr_method = null;

        n.f11.accept(this, s_table); // main arg? "String[] a" --> not sure if this is ever needed
        n.f14.accept(this, s_table); // var dec list
        n.f15.accept(this, s_table); // statement list

        return null;
    }

    @Override
    public MyType visit(MethodDeclaration n, SymbolTable s_table) {
        String method_name = n.f2.f0.toString();
        MyType ret_type = s_table.getClassInfo(curr_class).getMethodInfo(method_name).getReturnType();

        curr_method = method_name; // set global method state
        n.f4.accept(this, s_table); // param list
        n.f7.accept(this, s_table); // var dec list
        n.f8.accept(this, s_table); // statements list
        // n.f10.accept(this, s_table); // return expression
        MyType ret_type_final = n.f10.f0.accept(this, s_table); // return expression
        // if ret type equals or subtype -> good!
        // else -> bad :(
        if (ret_type_final.equals(ret_type) || s_table.isSubtype(ret_type_final.getClassName(), ret_type.getClassName())) { // return type does not match!
            return null;
        }
        else {
            System.err.println("🚨 Method (" + method_name + "): ret type does not match. Expected: " + ret_type + " | Actual: " + ret_type_final);
            printFailureAndExit();
        }

        System.err.println("🙈 Method Dec: (" + method_name + ")");
        return null;
    }

    @Override
    public MyType visit(AssignmentStatement n, SymbolTable s_table) {

        String var_name = n.f0.f0.toString();
        MyType expr_type = n.f2.f0.accept(this, s_table);

        // add as a class field
        if (curr_method == null){
            if (!s_table.getClassInfo(curr_class).hasField(var_name)){ // field doesn't exist
                System.err.println("🚨 Assignment: Class " + curr_class + " does not have field " + var_name);
                printFailureAndExit();
            }
            MyType expected_type = s_table.getClassInfo(curr_class).getFieldType(var_name);
            System.err.println("🧮 Assignment of (" + var_name + "): expected type = " + expected_type.toString() + "|| expr type = " + expr_type.toString());
            
            // if equals OR subtype --> ret null
            // else -> fail
            if (expected_type.equals(expr_type) || s_table.isSubtype(expr_type.getClassName(), expected_type.getClassName())){  // expr type is not subtype
                return null;
            }
            else{
                System.err.println("🚨 Assignment of (" + var_name + "): expected type: " + expected_type + " || assigned type: " + expr_type);
                printFailureAndExit();
            }
        }

        // add as a method local var
        else {
            if (!s_table.getClassInfo(curr_class).getMethodInfo(curr_method).hasVarsOrArgs(var_name)){ // var doesn't exist in method
                System.err.println("🚨 Assignment: Class " + curr_class + " and Method " + curr_method + " does not have var " + var_name);
                printFailureAndExit();
            }
            MyType expected_type = s_table.getClassInfo(curr_class).getMethodInfo(curr_method).getVarOrArgType(var_name);
            System.err.println("🧮 🧮 🧮 🧮 🧮 Assignment of (" + var_name + "): expected type = " + expected_type.toString() + "|| expr type = " + expr_type.toString());
            // if not equals OR not subtype
            // if (!expected_type.equals(expr_type)){ // expected type doesn't match
            if (expected_type.equals(expr_type) || s_table.isSubtype(expr_type.getClassName(), expected_type.getClassName())){
                return null;
            }
            else{
                System.err.println("🚨 Assignment of (" + var_name + "): expected type: " + expected_type + " || assigned type: " + expr_type);
                printFailureAndExit();
            }
        }

        return null;
    }

    @Override
    public MyType visit(AllocationExpression n, SymbolTable s_table) {
        String class_obj_name = n.f1.f0.toString();
        System.err.println("🤖 Allocation: allocate new = " + class_obj_name);
        if (!s_table.hasClass(class_obj_name)) {
            System.err.println("🚨 Allocation: " + class_obj_name + " is not an existing custom class");
            printFailureAndExit();
        }
        
        return new MyType(MyType.BaseType.CLASS, class_obj_name);
    }

    @Override
    public MyType visit(VarDeclaration n, SymbolTable s_table) {
        MyType var_type = n.f0.f0.accept(this, s_table);

        // if custom class type
        if (var_type.isOfType(MyType.BaseType.ID)){
            String var_type_name = var_type.getClassName();
            // check if class exists yet:
            if (!s_table.hasClass(var_type_name)){
                System.err.println("🚨 Var Dec: declare of type class that DNE = " + var_type_name);
                printFailureAndExit();
            }
        }

        String var_name = n.f1.f0.toString();
        System.err.println("🐰 Var Dec: " + var_name + " : " + var_type);

        return null;
    }

}