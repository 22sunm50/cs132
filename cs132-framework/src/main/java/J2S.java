import java.io.InputStream;
import java.util.ArrayList;

import minijava.MiniJavaParser;
import minijava.visitor.GJDepthFirst;
import minijava.syntaxtree.Goal;
import IR.token.*;
import sparrow.*;

public class J2S extends GJDepthFirst<String, String>{
        public static void main(String[] args) throws Exception {
                InputStream in = System.in;
                new MiniJavaParser(in);
                Goal root = MiniJavaParser.Goal();

                SymbolTable s_table = new SymbolTable();
                
                // first pass
                ClassTableVisitor cv = new ClassTableVisitor();
                cv.visit(root, s_table);
                cv.computeTransitiveSubtypes();
                cv.setSymbolTableSubtype(s_table);
                cv.inheritFields(s_table);
                cv.inheritMethods(s_table);
                cv.addFieldsToMethodVars(s_table); // do after inheritance

                s_table.printClassTable();

                // InstructionVisitor iv = new InstructionVisitor();

                // InstrContainer sp = iv.visit(root, s_table);

                // Block block = new Block(sp.instr_list, sp.temp_name);
                // FunctionDecl f = new FunctionDecl(new FunctionName("main"), new ArrayList<Identifier>(), block);

                // // Build a function
                // ArrayList<FunctionDecl> funcs = new ArrayList<FunctionDecl>();
                // funcs.add(f);
                // Program prog = new Program(funcs);

                // 3 VISITORS: FROM CHAT 🍅
                FunctionDeclVisitor fdv = new FunctionDeclVisitor();
                ArrayList<FunctionDecl> funcs = root.accept(fdv, s_table);
                Program prog = new Program(funcs);

                // Debug: Print the program
                System.err.println("🌷 🌷 🌷 🌷 🌷 FINAL PROGRAM!! 🌷 🌷 🌷 🌷 🌷 ");
                System.err.println(prog.toString());

                // actually print
                System.out.println(prog.toString());
        }
}