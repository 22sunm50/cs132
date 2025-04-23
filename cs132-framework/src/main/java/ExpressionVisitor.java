import minijava.MiniJavaParserConstants;

import minijava.syntaxtree.*;
import minijava.visitor.GJNoArguDepthFirst;

public class ExpressionVisitor extends GJNoArguDepthFirst<Type> implements MiniJavaParserConstants {
    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // 🌷 🌷 🌷 🌷 🌷 🌷: LITERALS
    @Override
    public Type visit(IntegerLiteral n) {
        // n.f0 gives the actual integer value
        return new Type(Type.BaseType.INT);
    }

    @Override
    public Type visit(TrueLiteral n) {
        return new Type(Type.BaseType.BOOLEAN);
    }

    @Override
    public Type visit(FalseLiteral n) {
        return new Type(Type.BaseType.BOOLEAN);
    }

    // 🌷 🌷 🌷 🌷 🌷 🌷: EXPRESSIONS
    @Override
    public Type visit(PlusExpression n) {
        Type lhs = n.f0.accept(this); // should equal INTEGER_LITERAL (AKA 43)
        Type rhs = n.f2.accept(this); // should equal INTEGER_LITERAL (AKA 43)

        Type intType = new Type(Type.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public Type visit(MinusExpression n) {
        Type lhs = n.f0.accept(this);
        Type rhs = n.f2.accept(this);

        Type intType = new Type(Type.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public Type visit(AndExpression n) {
        Type lhs = n.f0.accept(this);
        Type rhs = n.f2.accept(this);

        Type boolType = new Type(Type.BaseType.BOOLEAN);

        if (lhs.equals(boolType) && rhs.equals(boolType)) { return boolType; }
        printFailureAndExit();
        return null;
    }

    @Override
    public Type visit(TimesExpression n) {
        Type lhs = n.f0.accept(this);
        Type rhs = n.f2.accept(this);

        Type intType = new Type(Type.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return lhs; // return INTEGER_LITERAL (AKA 43)
    }

    @Override
    public Type visit(NotExpression n) {
        Type expr = n.f1.accept(this);

        Type boolType = new Type(Type.BaseType.BOOLEAN);

        if (!expr.equals(boolType)){ printFailureAndExit(); }
        return boolType;
    }

    @Override
    public Type visit(CompareExpression n) {
        Type lhs = n.f0.accept(this);
        Type rhs = n.f2.accept(this);

        Type intType = new Type(Type.BaseType.INT);

        if (!lhs.equals(intType) || !rhs.equals(intType)) {
            printFailureAndExit();
        }
        return new Type(Type.BaseType.BOOLEAN);
    }

    @Override
    public Type visit(BracketExpression n) {
        Type expr = n.f1.accept(this); // some type
        return expr;
    }

    // Auxiliary methods
    // Need to override this otherwise there will be a null pointer access
    @Override
    public Type visit(PrimaryExpression n) {
        // System.out.println("🌷(visit(PrimaryExpression)) n.f0 = " + n.f0);
        return n.f0.accept(this);
    }
}