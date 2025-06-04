import sparrowv.visitor.Visitor;
import sparrowv.*;

import java.util.HashMap;
import java.util.Map;

import IR.token.*;

public class FuncVisitor implements Visitor {
    Identifier ret_id = null;
    String curr_method = null;

    public Map<String, ActivationRecord> func_to_AR = new HashMap<>();
    ActivationRecord curr_AR = null;

    @Override
    public void visit(Program n) {
        for (FunctionDecl fd: n.funDecls){
            fd.accept(this);
        }
    }

    /*   Program parent;
    *   FunctionName functionName;
    *   List<Identifier> formalParameters;
    *   Block block; */
    @Override
    public void visit(FunctionDecl n) {
        curr_method = n.functionName.toString();
        curr_AR = new ActivationRecord();

        if (curr_method == "main"){
            curr_method = "Main";
        }

        // Add params
        for (Identifier param : n.formalParameters) {
            curr_AR.addParam(param.toString());
        }

        // Visit function body
        n.block.accept(this);

        // Save activation record for this function
        func_to_AR.put(curr_method, curr_AR);
    }

    /*   FunctionDecl parent;
    *   List<Instruction> instructions;
    *   Identifier return_id; */
    @Override
    public void visit(Block n) {
        for (Instruction s : n.instructions) {
            s.accept(this);
        }
        ret_id = n.return_id;
    }

    /*   Label label; */
    @Override
    public void visit(LabelInstr n) {

    }

    /*   Register lhs;
    *   int rhs; */
    @Override
    public void visit(Move_Reg_Integer n){

    }

    /*   Register lhs;
    *   FunctionName rhs; */
    @Override
    public void visit(Move_Reg_FuncName n) {

    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Add n){

    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Subtract n){

    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Multiply n){

    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(LessThan n){

    }

    /*   Register lhs;
    *   Register base;
    *   int offset; */
    @Override
    public void visit(Load n){

    }

    /*   Register base;
    *   int offset;
    *   Register rhs; */
    @Override
    public void visit(Store n){

    }

    /*   Register lhs;
    *   Register rhs; */
    @Override
    public void visit(Move_Reg_Reg n){

    }

    /*   Identifier lhs;
    *   Register rhs; */
    @Override
    public void visit(Move_Id_Reg n){
        curr_AR.addLocal(n.lhs.toString());
    }

    /*   Register lhs;
    *   Identifier rhs; */
    @Override
    public void visit(Move_Reg_Id n){
        curr_AR.addLocal(n.rhs.toString());
    }

    /*   Register lhs;
    *   Register size; */
    @Override
    public void visit(Alloc n){

    }

    /*   Register content; */
    @Override
    public void visit(Print n){

    }

    /*   String msg; */
    @Override
    public void visit(ErrorMessage n){
        
    }

    /*   Label label; */
    @Override
    public void visit(Goto n){

    }

    /*   Register condition;
    *   Label label; */
    @Override
    public void visit(IfGoto n){

    }

    /*   Register lhs;
    *   Register callee;
    *   List<Identifier> args; */
    @Override
    public void visit(Call n){

    }
}