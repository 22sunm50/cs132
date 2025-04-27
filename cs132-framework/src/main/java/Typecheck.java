import java.io.InputStream;

import minijava.MiniJavaParser;
import minijava.MiniJavaParserConstants;
import minijava.visitor.GJDepthFirst;
import minijava.syntaxtree.*;

public class Typecheck extends GJDepthFirst<String, String> implements MiniJavaParserConstants{
        public static void main(String[] args) throws Exception {
                InputStream in = System.in;
                new MiniJavaParser(in);
                Goal root = MiniJavaParser.Goal();

                SymbolTable s_table = new SymbolTable();
                // HashMap<String, ClassInfo> c_table = new HashMap<>(); // table of class info

                // first pass
                ClassTableVisitor cv = new ClassTableVisitor();
                cv.visit(root, s_table);
                cv.checkCycle();
                cv.inheritFields(s_table);
                cv.inheritMethods(s_table);
                cv.addFieldsToMethodVars(s_table); // do after inheritance
                // s_table.printClassTable();

                // third pass
                ExpressionVisitor ev = new ExpressionVisitor();
                ev.visit(root, s_table);

                // s_table.printClassTable();

                System.out.println("Program type checked successfully");
        }
}