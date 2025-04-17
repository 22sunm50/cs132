import java.io.InputStream;

import minijava.MiniJavaParser;
import minijava.MiniJavaParserConstants;
import minijava.visitor.GJDepthFirst;
import minijava.syntaxtree.*;
import minijava.visitor.DepthFirstVisitor;

public class Typecheck extends GJDepthFirst<String, String> implements MiniJavaParserConstants{
        /**
         * Grammar production:
         * f0 -> "true"
         */
        @Override
        public String visit(TrueLiteral n, String argu) {
                return n.f0.toString();
        }
        
        /**
         * Grammar production:
         * f0 -> PrimaryExpression()
         * f1 -> "&&"
         * f2 -> PrimaryExpression()
         */
        @Override
        public String visit(AndExpression n, String argu) {
                // n.f0.accept(new BooleanVisitor());
        
                String b = n.f2.accept(this, argu);

                return b;
        }
        

        public static void main(String[] args) throws Exception {
             InputStream in = System.in;
             new MiniJavaParser(in);
             Goal root = MiniJavaParser.Goal();

             System.out.println(root.toString());
        }
}

// public class BooleanVisitor extends DepthFirstVisitor{
//     @Override
//     public void visit(TrueLiteral n) {
//         System.out.println("a true!");
//     }

//     @Override
//     public void visit(FalseLiteral n) {
//         System.out.println("a false!");
//     }
// }
