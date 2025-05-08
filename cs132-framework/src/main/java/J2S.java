import java.io.InputStream;

import minijava.MiniJavaParser;
import minijava.MiniJavaParserConstants;
import minijava.visitor.GJDepthFirst;
import minijava.syntaxtree.*;

public class J2S extends GJDepthFirst<String, String> implements MiniJavaParserConstants{
        public static void main(String[] args) throws Exception {
                InputStream in = System.in;
                new MiniJavaParser(in);
                Goal root = MiniJavaParser.Goal();

                SymbolTable s_table = new SymbolTable();
                
                // first pass
                ClassTableVisitor cv = new ClassTableVisitor();
                cv.visit(root, s_table);
                cv.computeTransitiveSubtypes();
                cv.checkCycle();
                cv.setSymbolTableSubtype(s_table);
                cv.inheritFields(s_table);
                cv.inheritMethods(s_table);
                cv.addFieldsToMethodVars(s_table); // do after inheritance

                s_table.printClassTable();

                System.out.println("Program type checked successfully");
        }
}








// import java.io.InputStream;
// import java.util.ArrayList;

// import IR.SparrowParser;
// import IR.token.FunctionName;
// import IR.token.Identifier;
// import minijava.MiniJavaParser;
// import minijava.syntaxtree.Goal;
// import sparrow.*;

// public class J2S {
//     public static void main(String[] args) {
//         // Program prog;
//         InputStream in = System.in;
//         new SparrowParser(in);
//         Program prog = SparrowParser.Program();
//         // ArrayList<FunctionDecl> funcs;
//         // FunctionDecl f;
//         // Block block;
//         // ArrayList<Instruction> instrs = new ArrayList<Instruction>();

//         // // Build a list of instructions
//         // Identifier a = new Identifier("v0");
//         // instrs.add(new Move_Id_Integer(a, 3));

//         // Identifier b = new Identifier("v1");
//         // instrs.add(new Move_Id_Integer(b, 5));

//         // instrs.add(new Add(b, b, a));

//         // // Build a function
//         // block = new Block(instrs, b);
//         // f = new FunctionDecl(new FunctionName("main"), new ArrayList<Identifier>(), block);

//         // // Build a program
//         // funcs = new ArrayList<FunctionDecl>();
//         // funcs.add(f);
//         // prog = new Program(funcs);

//         // Print the program
//         System.out.println(prog.toString());
//     }
// }