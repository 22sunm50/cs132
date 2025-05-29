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

    // combine list of instr
    private List<Instruction> concat(List<Instruction> a, List<Instruction> b) {
        List<Instruction> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
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
        System.err.println("🔧 Starting function: " + n.functionName);

        List<Instruction> bodyInstrs = n.block.accept(this);

        // Prologue: Save callee-saved (s1–s11) registers to stack (as identifiers)
        List<Instruction> prologue = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Register s = new Register("s" + i);
            Identifier save = new Identifier("save_s" + i);
            prologue.add(new Move_Id_Reg(save, s));
            System.err.println("🛡️  Saving callee register: " + s + " → " + save);
        }

        // Epilogue: Restore callee-saved registers from stack
        List<Instruction> epilogue = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Register s = new Register("s" + i);
            Identifier save = new Identifier("save_s" + i);
            epilogue.add(new Move_Reg_Id(s, save));
            System.err.println("🌎  Restoring callee register: " + save + " → " + s);
        }

        // Append return move and epilogue
        epilogue.add(new Move_Reg_Id(t0, ret_id));
        System.err.println("🔚 Appending return: " + t0 + " ← " + ret_id);

        sparrowv.Block block = new sparrowv.Block(concat(prologue, concat(bodyInstrs, epilogue)), ret_id);
        sparrowv.FunctionDecl func = new sparrowv.FunctionDecl(n.functionName, n.formalParameters, block);
        func_list.add(func);
        return null;

        // sparrowv.Block b = new sparrowv.Block(n.block.accept(this), ret_id);
        // sparrowv.FunctionDecl func = new sparrowv.FunctionDecl(n.functionName, n.formalParameters, b);
        // func_list.add(func);
        // return null;
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
        System.err.println("📞 Preparing call to: " + n.callee + " → result in " + n.lhs);

        List<Instruction> instrs = new ArrayList<>();

        // Save t0–t5 to identifiers
        for (int i = 0; i <= 5; i++) {
            Register t = new Register("t" + i);
            Identifier save = new Identifier("save_t" + i);
            instrs.add(new Move_Id_Reg(save, t));
            System.err.println("💾 Saving caller register: " + t + " → " + save);
        }

        // Save a2–a7
        for (int i = 2; i <= 7; i++) {
            Register a = new Register("a" + i);
            Identifier save = new Identifier("save_a" + i);
            instrs.add(new Move_Id_Reg(save, a));
            System.err.println("💾 Saving argument register: " + a + " → " + save);
        }
    
        // Move callee
        instrs.add(new Move_Reg_Id(t0, n.callee));
        System.err.println("🔁 Moving callee to t0: " + n.callee + " → t0");
        
        // NOTE: This assumes the call target uses a2–a7 directly. No argument register optimization yet.
        instrs.add(new sparrowv.Call(t0, t0, n.args));
        System.err.println("📲 Executed call with args: " + n.args);
    
        // Move return value to lhs
        instrs.add(new Move_Id_Reg(n.lhs, t0));
        System.err.println("📥 Stored return value: t0 → " + n.lhs);
    
        // Restore t0–t5
        for (int i = 0; i <= 5; i++) {
            Register t = new Register("t" + i);
            Identifier save = new Identifier("save_t" + i);
            instrs.add(new Move_Reg_Id(t, save));
            System.err.println("🔁 Restoring caller register: " + save + " → " + t);
        }

        // Restore a2–a7
        for (int i = 2; i <= 7; i++) {
            Register a = new Register("a" + i);
            Identifier save = new Identifier("save_a" + i);
            instrs.add(new Move_Reg_Id(a, save));
            System.err.println("🔁 Restoring argument register: " + save + " → " + a);
        }
    
        return instrs;

        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.callee));
        // instrs.add(new sparrowv.Call(t0, t0, n.args));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }
}