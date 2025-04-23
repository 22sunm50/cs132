import java.io.InputStream;

import minijava.MiniJavaParser;
import minijava.MiniJavaParserConstants;
import minijava.visitor.GJDepthFirst;
import minijava.syntaxtree.*;
import minijava.visitor.DepthFirstVisitor;

public class Typecheck extends GJDepthFirst<String, String> implements MiniJavaParserConstants{
        public static void main(String[] args) throws Exception {
                InputStream in = System.in;
                new MiniJavaParser(in);
                Goal root = MiniJavaParser.Goal();

                SymbolTable table = new SymbolTable();
                SymbolTableVisitor sv = new SymbolTableVisitor();
                sv.visit(root, table);

                System.out.println("Program type checked successfully");
        }
}