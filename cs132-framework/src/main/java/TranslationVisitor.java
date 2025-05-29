// must be sparrowv.Instruction !!
import sparrowv.*;
import IR.token.*;

import sparrow.visitor.RetVisitor;

import java.util.ArrayList;
import java.util.List;

public class TranslationVisitor implements RetVisitor < List<sparrowv.Instruction> >{

    private final Register t0 = new Register("t0");
    private final Register t1 = new Register("t1");

    List<sparrowv.FunctionDecl> func_list;
    Identifier ret_id = null;

    // wrap an instr w a list
    private List<Instruction> wrap(Instruction i) {
        List<Instruction> l = new ArrayList<>();
        l.add(i);
        return l;
    }

    // VISIT METHODS START HERE
    
    /*   List<FunctionDecl> funDecls; */
    @Override
    public List<Instruction> visit(sparrow.Program n){
        func_list = new ArrayList<>();

        for (sparrow.FunctionDecl fd: n.funDecls){
            fd.accept(this);
        }

        sparrowv.Program program = new sparrowv.Program(func_list);
        System.err.println("🌷 🌷 🌷 🌷 🌷 FINAL PROGRAM!! (.err.) 🌷 🌷 🌷 🌷 🌷");
        System.err.println(program);
        System.err.println("🌷 🌷 🌷 🌷 🌷 FINAL PROGRAM!! (.out.) 🌷 🌷 🌷 🌷 🌷");
        System.out.println(program);
        return null;
    }

    /*   Program parent;
    *   FunctionName functionName;
    *   List<Identifier> formalParameters;
    *   Block block; */
    @Override
    public List<Instruction> visit(sparrow.FunctionDecl n){
        sparrowv.Block b = new sparrowv.Block(n.block.accept(this), ret_id);
        sparrowv.FunctionDecl func = new sparrowv.FunctionDecl(n.functionName, n.formalParameters, b);
        func_list.add(func);
        return null;
    }

    /*   FunctionDecl parent;
    *   List<Instruction> instructions;
    *   Identifier return_id; */
    @Override
    public List<Instruction> visit(sparrow.Block n){
        List<Instruction> instr_list = new ArrayList<>();
        for (sparrow.Instruction s : n.instructions) {
            instr_list.addAll(s.accept(this));
        }
        ret_id = n.return_id;
        instr_list.add(new Move_Reg_Id(t0, n.return_id));
        return instr_list;
    }

    /*   Label label; */
    @Override
    public List<Instruction> visit(sparrow.LabelInstr n){
        return wrap(new sparrowv.LabelInstr(n.label));
    }

    /*   Identifier lhs;
    *   int rhs; */
    @Override
    public List<Instruction> visit(sparrow.Move_Id_Integer n){
        List<Instruction> instr_list = new ArrayList<>();
        instr_list.add(new Move_Reg_Integer(t0, n.rhs));
        instr_list.add(new Move_Id_Reg(n.lhs, t0));
        return instr_list;
    }

    /*   Identifier lhs;
    *   FunctionName rhs; */
    @Override
    public List<Instruction> visit(sparrow.Move_Id_FuncName n){
        return List.of(
            new Move_Reg_FuncName(t0, n.rhs),
            new Move_Id_Reg(n.lhs, t0)
        );
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Add n){
        List<Instruction> instrs = new ArrayList<>();
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Add(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Subtract n){
        List<Instruction> instrs = new ArrayList<>();
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Subtract(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Multiply n){
        List<Instruction> instrs = new ArrayList<>();
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Multiply(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.LessThan n){
        List<Instruction> instrs = new ArrayList<>();
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.LessThan(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        return instrs;
    }

    /*   Identifier lhs;
    *   Identifier base;
    *   int offset; */
    @Override
    public List<Instruction> visit(sparrow.Load n){
        return List.of(
            new Move_Reg_Id(t0, n.base),
            new sparrowv.Load(t0, t0, n.offset),
            new Move_Id_Reg(n.lhs, t0)
        );
    }

    /*   Identifier base;
    *   int offset;
    *   Identifier rhs; */
    @Override
    public List<Instruction> visit(sparrow.Store n){
        return List.of(
            new Move_Reg_Id(t0, n.base),
            new Move_Reg_Id(t1, n.rhs),
            new sparrowv.Store(t0, n.offset, t1)
        );
    }

    /*   Identifier lhs;
    *   Identifier rhs; */
    @Override
    public List<Instruction> visit(sparrow.Move_Id_Id n){
        return List.of(
            new Move_Reg_Id(t0, n.rhs),
            new Move_Id_Reg(n.lhs, t0)
        );
    }

    /*   Identifier lhs;
    *   Identifier size; */
    @Override
    public List<Instruction> visit(sparrow.Alloc n){
        return List.of(
            new Move_Reg_Id(t0, n.size),
            new sparrowv.Alloc(t0, t0),
            new Move_Id_Reg(n.lhs, t0)
        );
    }

    /*   Identifier content; */
    @Override
    public List<Instruction> visit(sparrow.Print n){
        return List.of(
            new Move_Reg_Id(t0, n.content),
            new sparrowv.Print(t0)
        );
    }

    /*   String msg; */
    @Override
    public List<Instruction> visit(sparrow.ErrorMessage n){
        return wrap(new sparrowv.ErrorMessage(n.msg));
    }

    /*   Label label; */
    @Override
    public List<Instruction> visit(sparrow.Goto n){
        return wrap(new sparrowv.Goto(n.label));
    }

    /*   Identifier condition;
    *   Label label; */
    @Override
    public List<Instruction> visit(sparrow.IfGoto n){
        return List.of(
            new Move_Reg_Id(t0, n.condition),
            new sparrowv.IfGoto(t0, n.label)
        );
    }

    /*   Identifier lhs;
    *   Identifier callee;
    *   List<Identifier> args; */
    @Override
    public List<Instruction> visit(sparrow.Call n){
        List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.lhs));
        instrs.add(new Move_Reg_Id(t0, n.callee));
        instrs.add(new sparrowv.Call(t0, t0, n.args));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        return instrs;
    }
}