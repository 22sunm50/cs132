import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class ExpressionVisitor extends GJDepthFirst<MyType, SymbolTable> {
    String curr_class;
    String curr_method;

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
        return null;
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
        return lhs; // return INTEGER_LITERAL (AKA 43)
    }

    @Override
    public MyType visit(NotExpression n, SymbolTable s_table) {
        MyType expr = n.f1.accept(this, s_table);

        MyType boolType = new MyType(MyType.BaseType.BOOLEAN);

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
    public MyType visit(BracketExpression n, SymbolTable s_table) { // 🍅 🍅 🍅 🍅 🍅
        MyType expr = n.f1.accept(this, s_table); // some type
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
        return expr_type;
    }

    @Override
    public MyType visit(MessageSend n, SymbolTable s_table) {
        MyType obj_type = n.f0.f0.accept(this, s_table);
        // return ret_type;
        return null;
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
        if (!ret_type_final.equals(ret_type)) { // return type does not match!
            System.err.println("🚨 Method (" + method_name + "): ret type does not match. Expected: " + ret_type + " | Actual: " + ret_type_final);
            printFailureAndExit();
        }

        System.err.println("📝 📝 📝 📝 📝 Method Dec: expected ret type = " + ret_type.toString() + " | final ret type = " + ret_type_final.toString());

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
            System.err.println("🧮 🧮 🧮 🧮 🧮 Assignment of (" + var_name + "): expected type = " + expected_type.toString() + "|| expr type = " + expr_type.toString());
            if (!expected_type.equals(expr_type)){                           // expected type doesn't match
                System.err.println("🚨 Assignment of " + var_name + ": expected type: " + expected_type + " || assigned type: " + expr_type);
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
            if (!expected_type.equals(expr_type)){ // expected type doesn't match
                System.err.println("🚨 Assignment of (" + var_name + "): expected type: " + expected_type + " || assigned type: " + expr_type);
                printFailureAndExit();
            }
        }

        return null;
    }

    @Override
    public MyType visit(AllocationExpression n, SymbolTable s_table) {
        String class_obj_name = n.f1.f0.toString();
        System.err.println("🤖 🤖 🤖 🤖 🤖 : allocation class object name = " + class_obj_name);
        if (!s_table.hasClass(class_obj_name)) {
            System.err.println("🚨 Allocation: " + class_obj_name + "is not an existing custom class");
            printFailureAndExit();
        }
        
        return new MyType(MyType.BaseType.CLASS, class_obj_name);
    }

}