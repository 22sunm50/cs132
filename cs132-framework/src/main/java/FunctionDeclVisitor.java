import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;

import IR.SparrowParser;
import IR.token.Identifier;
import IR.token.FunctionName;
import sparrow.Block;
import sparrow.FunctionDecl;

public class FunctionDeclVisitor extends GJDepthFirst<ArrayList<FunctionDecl>, SymbolTable> {

    InstructionVisitor iv = new InstructionVisitor();

    @Override
    public ArrayList<FunctionDecl> visit(Goal n, SymbolTable s_table) {
        ArrayList<FunctionDecl> funcs = new ArrayList<>();

        // Visit main class
        FunctionDecl mainFunc = visitMainAsFunction(n.f0, s_table);
        funcs.add(mainFunc);

        // Visit all class declarations and collect methods
        for (Node node : n.f1.nodes) {
            ArrayList<FunctionDecl> classFuncs = node.accept(this, s_table);
            funcs.addAll(classFuncs);
        }

        return funcs;
    }

    public FunctionDecl visitMainAsFunction(MainClass n, SymbolTable s_table) {
        System.err.println("👩‍🔧 FuncDeclVisitor - visitMainAsFunction : entered!");

        // InstrContainer mainInstrs = n.f15.accept(iv, s_table); // call IV on statements

        InstrContainer mainInstrs = new InstrContainer();

        if (n.f15.present()) {
            for (Node stmtNode : n.f15.nodes) {
                InstrContainer stmtResult = stmtNode.accept(iv, s_table);
                if (stmtResult != null) {
                    mainInstrs.append(stmtResult);
                }
            }
        }

        System.err.println("👩‍🔧 FuncDeclVisitor - visitMainAsFunction : What's in mainInstr: " + mainInstrs);
        Block block = new Block(mainInstrs.instr_list, mainInstrs.temp_name);
        return new FunctionDecl(new FunctionName("main"), new ArrayList<>(), block);
    }

    // You’d also implement visit(ClassDeclaration n, ...) here to get methods inside each class.
}
