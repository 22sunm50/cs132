import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;

// import IR.SparrowParser;
import IR.token.Identifier;
import IR.token.FunctionName;
import sparrow.Block;
import sparrow.FunctionDecl;
import sparrow.Move_Id_Integer;

public class FunctionDeclVisitor extends GJDepthFirst<ArrayList<FunctionDecl>, SymbolTable> {

    String curr_class;
    String curr_method;

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
        // InstrContainer mainInstrs = n.f15.accept(iv, s_table); // call IV on statements

        InstrContainer mainInstrs = new InstrContainer();

        // call IV on each statement in main
        if (n.f15.present()) {
            for (Node statement_node : n.f15.nodes) {
                InstrContainer statement_result = statement_node.accept(iv, s_table);
                if (statement_result != null) {
                    mainInstrs.append(statement_result);
                }
            }
        }

        // main returns null always so return dummy 0
        if (mainInstrs.temp_name == null) {
            Identifier dummyReturn = new Identifier("v" + iv.id_name_counter++);
            mainInstrs.instr_list.add(new Move_Id_Integer(dummyReturn, 0));
            mainInstrs.temp_name = dummyReturn;
        }
        Block block = new Block(mainInstrs.instr_list, mainInstrs.temp_name);
        return new FunctionDecl(new FunctionName("main"), new ArrayList<>(), block);
    }

    @Override
    public ArrayList<FunctionDecl> visit(TypeDeclaration n, SymbolTable s_table) {
        return n.f0.accept(this, s_table);
    }


    @Override
    public ArrayList<FunctionDecl> visit(MethodDeclaration n, SymbolTable s_table) {
        String methodName = n.f2.f0.toString();
        String full_method_name = curr_class + "_" + methodName; // 🍅 🍅 🍅 make sure to update curr_class in visit(classDec) when you implement it

        // Construct param list
        MethodInfo this_methodInfo = s_table.getClassInfo(curr_class).getMethodInfo(methodName);
        ArrayList<Identifier> args = new ArrayList<>();
        args.add(new Identifier("this"));
        args.addAll(this_methodInfo.getArgsIDList());

        // Get body instructions
        InstrContainer bodyInstrs = new InstrContainer();
        if (n.f8.present()) {
            for (Node statement : n.f8.nodes) {
                InstrContainer statementInstr = statement.accept(iv, s_table);
                if (statementInstr != null)
                    bodyInstrs.append(statementInstr);
            }
        }

        // Translate return expression
        InstrContainer returnExpr = n.f10.accept(iv, s_table);
        bodyInstrs.instr_list.addAll(returnExpr.instr_list);
        bodyInstrs.temp_name = returnExpr.temp_name != null ? new Identifier(returnExpr.temp_name.toString()) : new Identifier("0"); // 🍅 🍅 🍅 🍅 🍅: if void ret, jsut return 0 right?
        bodyInstrs.class_name = returnExpr.class_name;

        Block body = new Block(bodyInstrs.instr_list, bodyInstrs.temp_name);
        FunctionDecl func_dec = new FunctionDecl(new FunctionName(full_method_name), args, body);

        ArrayList<FunctionDecl> func_list = new ArrayList<>();
        func_list.add(func_dec);
        return func_list;
    }

    @Override
    public ArrayList<FunctionDecl> visit(ClassDeclaration n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString();
        curr_class = class_name;
        // ClassInfo classInfo = s_table.getClassInfo(class_name);

        ArrayList<FunctionDecl> methodFuncs = new ArrayList<>();

        for (Node methodNode : n.f4.nodes) {
            ArrayList<FunctionDecl> methodIRs = methodNode.accept(this, s_table);
            methodFuncs.addAll(methodIRs);
        }

        return methodFuncs;
    }

    @Override
    public ArrayList<FunctionDecl> visit(ClassExtendsDeclaration n, SymbolTable s_table) { // 🍅 🍅 🍅 : Do I need to do anything more for classExtends? (I copied from ClassDeclaration)
        String class_name = n.f1.f0.toString();
        curr_class = class_name;
        // ClassInfo classInfo = s_table.getClassInfo(class_name);

        ArrayList<FunctionDecl> methodFuncs = new ArrayList<>();

        for (Node methodNode : n.f6.nodes) {
            ArrayList<FunctionDecl> methodIRs = methodNode.accept(this, s_table);
            methodFuncs.addAll(methodIRs);
        }

        return methodFuncs;
    }
}
