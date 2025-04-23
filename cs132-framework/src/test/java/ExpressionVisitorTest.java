import org.junit.Test;

import minijava.syntaxtree.*;

import static org.junit.Assert.*;

public class ExpressionVisitorTest {
    @Test public void testPlusExpr() {
        IntegerLiteral a = new IntegerLiteral(new NodeToken("7"));
        IntegerLiteral b = new IntegerLiteral(new NodeToken("9"));
        NodeChoice a_expr = new NodeChoice(a);
        NodeChoice b_expr = new NodeChoice(b);
        PrimaryExpression a_pe = new PrimaryExpression(a_expr);
        PrimaryExpression b_pe = new PrimaryExpression(b_expr);
        PlusExpression root = new PlusExpression(a_pe, b_pe);
        
        ExpressionVisitor cv = new ExpressionVisitor();
        Integer res = cv.visit(root);

        // assertEquals((Integer) 16, res);
        System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 RES = " + res);
        assertEquals((Integer) 43, res);

        // // 🌷 🌷 🌷 🌷 🌷: TEST PRINT FAIL AND EXIT
        // FalseLiteral c = new FalseLiteral(new NodeToken("false"));
        // IntegerLiteral d = new IntegerLiteral(new NodeToken("9"));
        // NodeChoice c_expr = new NodeChoice(c);
        // NodeChoice d_expr = new NodeChoice(d);
        // PrimaryExpression c_pe = new PrimaryExpression(c_expr);
        // PrimaryExpression d_pe = new PrimaryExpression(d_expr);
        // PlusExpression root2 = new PlusExpression(c_pe, d_pe);
        
        // // ExpressionVisitor cv = new ExpressionVisitor();
        // Integer res2 = cv.visit(root2);

        // System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 RES2 = " + res2);
    }

    @Test public void testAndExpr() {
        TrueLiteral a = new TrueLiteral(new NodeToken("true"));
        FalseLiteral b = new FalseLiteral(new NodeToken("false"));
        NodeChoice a_expr = new NodeChoice(a);
        NodeChoice b_expr = new NodeChoice(b);
        PrimaryExpression a_pe = new PrimaryExpression(a_expr);
        PrimaryExpression b_pe = new PrimaryExpression(b_expr);
        AndExpression root = new AndExpression(a_pe, b_pe);
        
        ExpressionVisitor cv = new ExpressionVisitor();
        Integer res = cv.visit(root);

        assertEquals((Integer) 28, res);
        System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 res AND = " + res);

        // // 🌷 🌷 🌷 🌷 🌷: TEST PRINT FAIL AND EXIT
        // IntegerLiteral c = new IntegerLiteral(new NodeToken("7"));
        // IntegerLiteral d = new IntegerLiteral(new NodeToken("9"));
        // NodeChoice c_expr = new NodeChoice(c);
        // NodeChoice d_expr = new NodeChoice(d);
        // PrimaryExpression c_pe = new PrimaryExpression(c_expr);
        // PrimaryExpression d_pe = new PrimaryExpression(d_expr);
        // AndExpression root2 = new AndExpression(c_pe, d_pe);
        
        // // ExpressionVisitor cv = new ExpressionVisitor();
        // Integer res2 = cv.visit(root2);

        // System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀🚀 RES AND FAIL = " + res2);
    }
}