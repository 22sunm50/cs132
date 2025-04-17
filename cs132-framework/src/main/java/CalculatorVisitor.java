import minijava.MiniJavaParserConstants;

import minijava.syntaxtree.*;
import minijava.visitor.GJNoArguDepthFirst;

public class CalculatorVisitor extends GJNoArguDepthFirst<Integer> implements MiniJavaParserConstants {
    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    @Override
    public Integer visit(PlusExpression n) {
        Integer lhs = n.f0.accept(this); // should equal INTEGER_LITERAL (AKA 43)
        Integer rhs = n.f2.accept(this); // should equal INTEGER_LITERAL (AKA 43)

        if (lhs != INTEGER_LITERAL || rhs != INTEGER_LITERAL) {
            printFailureAndExit();
        }
        return lhs;
    }

    @Override
    public Integer visit(IntegerLiteral n) {
        // n.f0 gives the actual integer value
        return INTEGER_LITERAL;
    }

    @Override
    public Integer visit(TrueLiteral n) {
        return TRUE;
    }

    @Override
    public Integer visit(FalseLiteral n) {
        return FALSE;
    }

    // Auxiliary methods
    // Need to override this otherwise there will be a null pointer access
    @Override
    public Integer visit(PrimaryExpression n) {
        // System.out.println("🌷(visit(PrimaryExpression)) n.f0 = " + n.f0);
        return n.f0.accept(this);
    }
}