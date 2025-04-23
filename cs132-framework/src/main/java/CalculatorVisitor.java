// src/main/java/CalculatorVisitor.java
import minijava.syntaxtree.IntegerLiteral;
import minijava.syntaxtree.PlusExpression;
import minijava.syntaxtree.PrimaryExpression;
import minijava.visitor.GJNoArguDepthFirst;

public class CalculatorVisitor extends GJNoArguDepthFirst<Integer> {
    @Override
    public Integer visit(PlusExpression n) {
        Integer lhs = n.f0.accept(this);
        Integer rhs = n.f2.accept(this);

        Integer res = lhs + rhs;
        return res;
    }

    @Override
    public Integer visit(IntegerLiteral n) {
        return Integer.parseInt(n.f0.toString());
    }

    // Auxiliary methods
    // Need to override this otherwise there will be a null pointer access
    @Override
    public Integer visit(PrimaryExpression n) {
        return n.f0.accept(this);
    }
}