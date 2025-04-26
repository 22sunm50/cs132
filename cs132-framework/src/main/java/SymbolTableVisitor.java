import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class SymbolTableVisitor extends GJDepthFirst<MyType, SymbolTable> {
    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // DECLARATION
    @Override
    public MyType visit (VarDeclaration n, SymbolTable table) {
        String var_name = n.f1.f0.toString();
        MyType var_type = n.f0.f0.accept(this, table);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var name = " + var_name);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var type = " + var_type);
        return null;
    }

    // ALL POSSIBLE Type()
    @Override
    public MyType visit (ArrayType n, SymbolTable table) {
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit (BooleanType n, SymbolTable table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        // System.out.println("🌷 🌷 🌷 🌷 🌷: BOOL var type = " + var_type);
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


    // CLASS TABLE
}
