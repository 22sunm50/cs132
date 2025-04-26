import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class ExpressionVisitor extends GJDepthFirst<MyType, SymbolTable> {
    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // 🧮 🧮 🧮 🧮 🧮 🧮 🧮 LITERALS 🧮 🧮 🧮 🧮 🧮 🧮 🧮 🧮
    @Override
    public MyType visit(IntegerLiteral n, SymbolTable table) {
        // n.f0 gives the actual integer value
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit(TrueLiteral n, SymbolTable table) {
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(FalseLiteral n, SymbolTable table) {
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    // 📣 📣 📣 📣 📣 📣 📣 📣 📣 EXPRESSIONS 📣 📣 📣 📣 📣 📣 📣 📣 📣
    @Override
    public MyType visit(PlusExpression n, SymbolTable table) {
        MyType lhs = n.f0.accept(this, table); // should equal INTEGER_LITERAL (AKA 43)
        MyType rhs = n.f2.accept(this, table); // should equal INTEGER_LITERAL (AKA 43)

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public MyType visit(MinusExpression n, SymbolTable table) {
        MyType lhs = n.f0.accept(this, table);
        MyType rhs = n.f2.accept(this, table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public MyType visit(AndExpression n, SymbolTable table) {
        MyType lhs = n.f0.accept(this, table);
        MyType rhs = n.f2.accept(this, table);

        MyType boolType = new MyType(MyType.BaseType.BOOLEAN);

        if (lhs.equals(boolType) && rhs.equals(boolType)) { return boolType; }
        printFailureAndExit();
        return null;
    }

    @Override
    public MyType visit(TimesExpression n, SymbolTable table) {
        MyType lhs = n.f0.accept(this, table);
        MyType rhs = n.f2.accept(this, table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs; // return INTEGER_LITERAL (AKA 43)
    }

    @Override
    public MyType visit(NotExpression n, SymbolTable table) {
        MyType expr = n.f1.accept(this, table);

        MyType boolType = new MyType(MyType.BaseType.BOOLEAN);

        if (!expr.equals(boolType)){ printFailureAndExit(); }
        return boolType;
    }

    @Override
    public MyType visit(CompareExpression n, SymbolTable table) {
        MyType lhs = n.f0.accept(this, table);
        MyType rhs = n.f2.accept(this, table);

        MyType intType = new MyType(MyType.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(BracketExpression n, SymbolTable table) {
        MyType expr = n.f1.accept(this, table); // some type
        return expr;
    }

    // Auxiliary methods 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
    // Need to override this otherwise there will be a null pointer access
    @Override
    public MyType visit(PrimaryExpression n, SymbolTable table) {
        // System.out.println("🌷(visit(PrimaryExpression)) n.f0 = " + n.f0);
        return n.f0.accept(this, table);
    }

    // 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ SYMBOL TABLE 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️ 🗺️
    // ALL POSSIBLE Type()
    @Override
    public MyType visit (ArrayType n, SymbolTable table) {
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit (BooleanType n, SymbolTable table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit (IntegerType n, SymbolTable table) {
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit (Identifier n, SymbolTable table) {
        String var_name = n.f0.toString();
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit (VarDeclaration n, SymbolTable table) { // 🍅 🍅 🍅 🍅 🍅: later, this will have to handle shadowing etc.
        String var_name = n.f1.f0.toString();
        MyType var_type = n.f0.f0.accept(this, table);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var name = " + var_name);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var type = " + var_type);
        table.addVar(var_name, var_type);
        return null;
    }

    // @Override
    // public MyType visit (AssignmentStatement n, SymbolTable table) {
    //     String var_name = n.f0.f0.toString();
    //     if (table.lookup(var_name) == null){ // triying to assign to a non-existent var
    //         printFailureAndExit();
    //     }
    //     return null;
    // }

}