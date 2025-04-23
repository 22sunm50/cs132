import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class SymbolTableVisitor extends GJDepthFirst<Type, SymbolTable> {
    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // DECLARATION
    @Override
    public Type visit (VarDeclaration n, SymbolTable table) {
        String var_name = n.f1.f0.toString();
        Type var_type = n.f0.f0.accept(this, table);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var name = " + var_name);
        System.out.println("🌷 🌷 🌷 🌷 🌷: var type = " + var_type);
        return null;
    }

    // ALL POSSIBLE Type()
    @Override
    public Type visit (ArrayType n, SymbolTable table) {
        return new Type(Type.BaseType.INT_ARRAY);
    }

    @Override
    public Type visit (BooleanType n, SymbolTable table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        // System.out.println("🌷 🌷 🌷 🌷 🌷: BOOL var type = " + var_type);
        return new Type(Type.BaseType.BOOLEAN);
    }

    @Override
    public Type visit (IntegerType n, SymbolTable table) {
        return new Type(Type.BaseType.INT);
    }

    @Override
    public Type visit (Identifier n, SymbolTable table) {
        String var_name = n.f0.toString();
        return new Type(Type.BaseType.CLASS, var_name);
    }


    // CLASS TABLE
}
