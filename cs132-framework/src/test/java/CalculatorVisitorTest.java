import org.junit.Test;

import minijava.syntaxtree.*;

import static org.junit.Assert.*;

public class CalculatorVisitorTest {
    @Test public void testPlusExpr() {
        // IntegerLiteral a = new IntegerLiteral(new NodeToken("7"));
        // IntegerLiteral b = new IntegerLiteral(new NodeToken("9"));
        // NodeChoice a_expr = new NodeChoice(a);
        // NodeChoice b_expr = new NodeChoice(b);
        // PrimaryExpression a_pe = new PrimaryExpression(a_expr);
        // PrimaryExpression b_pe = new PrimaryExpression(b_expr);
        // PlusExpression root = new PlusExpression(a_pe, b_pe);
        
        // CalculatorVisitor cv = new CalculatorVisitor();
        // Integer res = cv.visit(root);

        // // assertEquals((Integer) 16, res);
        // System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 RES = " + res);
        // assertEquals((Integer) 43, res);

        // 🌷 🌷 🌷 🌷 🌷: TEST PRINT FAIL AND EXIT
        FalseLiteral c = new FalseLiteral(new NodeToken("false"));
        IntegerLiteral d = new IntegerLiteral(new NodeToken("9"));
        NodeChoice c_expr = new NodeChoice(c);
        NodeChoice d_expr = new NodeChoice(d);
        PrimaryExpression c_pe = new PrimaryExpression(c_expr);
        PrimaryExpression d_pe = new PrimaryExpression(d_expr);
        PlusExpression root2 = new PlusExpression(c_pe, d_pe);
        
        CalculatorVisitor cv = new CalculatorVisitor();
        Integer res2 = cv.visit(root2);

        System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 RES2 = " + res2);
        // assertEquals((Integer) 43, res);
    }
}