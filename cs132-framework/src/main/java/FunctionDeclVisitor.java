import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.List;

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

    // @Override
    // public ArrayList<FunctionDecl> visit(MethodDeclaration n, SymbolTable s_table) {
    //     System.err.println("👩‍🔧 FuncDeclVisitor - visitMethodDecl: entered!");

    //     // list of params
    //     ArrayList<Identifier> params = new ArrayList<>();
    //     if (n.f4.present()) {
    //         // param list: Type Identifier ( , Type Identifier )*
    //         params.add(new Identifier(n.f4.f1.f1.toString()));
    //         for (Node argNode : n.f4.f3.nodes) {
    //             NodeSequence seq = (NodeSequence) argNode;
    //             Identifier paramName = new Identifier(((Identifier) seq.elementAt(1)).toString());
    //             params.add(paramName);
    //         }
    //     }

    //     InstrContainer bodyInstrs = new InstrContainer();
    //     if (n.f8.present()) {
    //         for (Node stmtNode : n.f8.nodes) {
    //             InstrContainer stmtResult = stmtNode.accept(iv, s_table);
    //             if (stmtResult != null) bodyInstrs.append(stmtResult);
    //         }
    //     }

    //     // Handle the return expression
    //     InstrContainer returnInstr = n.f10.accept(iv, s_table);
    //     bodyInstrs.append(returnInstr);

    //     Block block = new Block(bodyInstrs.instr_list, returnInstr.temp_name);
    //     FunctionName funcName = new FunctionName(n.f2.toString());

    //     return new ArrayList<>(List.of(new FunctionDecl(funcName, params, block)));
    // }

    // @Override
    // public ArrayList<FunctionDecl> visit(ClassDeclaration n, SymbolTable s_table) {
    //     ArrayList<FunctionDecl> methodFuncs = new ArrayList<>();

    //     for (Node methodNode : n.f4.nodes) { // f4 = list of MethodDeclaration
    //         FunctionDecl func = methodNode.accept(this, s_table).get(0); // each visit returns a singleton list
    //         methodFuncs.add(func);
    //     }

    //     return methodFuncs;
    // }
}
