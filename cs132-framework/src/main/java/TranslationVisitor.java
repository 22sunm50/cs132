import sparrowv.*;
import IR.token.*;

import sparrow.visitor.RetVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TranslationVisitor implements RetVisitor < List<sparrowv.Instruction> >{

    private final Register t0 = new Register("t0");
    private final Register t1 = new Register("t1");

    List<sparrowv.FunctionDecl> func_list;
    Identifier ret_id = null;

    Map<String, String> registerMap;
    Set<String> spilledVars;

    // constructor
    public TranslationVisitor(Map<String, String> registerMap, Set<String> spilledVars) {
        this.registerMap = registerMap;
        this.spilledVars = spilledVars;
    }

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

    private Object lookup(String name) {
        if (registerMap.containsKey(name)) {
            return new Register(registerMap.get(name));
        } else {
            return new Identifier(name); // spilled
        }
    }
    
    private Object lookup(Identifier id) {
        return lookup(id.toString());
    }

    private void moveToReg(Register dest, Object src, List<Instruction> list) {
        if (src instanceof Identifier) { //spill
            list.add(new Move_Reg_Id(dest, (Identifier)src));
        } else if (!dest.toString().equals(src.toString())) {
            list.add(new Move_Reg_Reg(dest, (Register)src));
        }
    }
    
    private void moveFromReg(Object dest, Register src, List<Instruction> list) {
        if (dest instanceof Identifier) {
            list.add(new Move_Id_Reg((Identifier)dest, src));
        } else {
            list.add(new Move_Reg_Reg((Register)dest, src));
        }
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
        List<Instruction> prologue = new ArrayList<>();
        List<Instruction> epilogue = new ArrayList<>();

        // Prologue: Save callee-saved (s1–s11) registers to stack (as identifiers)
        for (int i = 1; i <= 11; i++) {
            Register s = new Register("s" + i);
            Identifier save = new Identifier("save_s" + i);
            prologue.add(new Move_Id_Reg(save, s));
        }

        // Epilogue: Restore callee-saved registers from stack
        for (int i = 1; i <= 11; i++) {
            Register s = new Register("s" + i);
            Identifier save = new Identifier("save_s" + i);
            epilogue.add(new Move_Reg_Id(s, save));
        }

        // 🍅 : Handle function parameters: move from a2–a7 into allocated space
        for (int i = 0; i < n.formalParameters.size(); i++) {
            Identifier param = n.formalParameters.get(i);
            Object dest = lookup(param);
            Register argReg = new Register("a" + (i + 2)); // a2–a7

            if (dest instanceof Identifier) {
                prologue.add(new Move_Id_Reg((Identifier) dest, argReg));
                System.err.println("📥 Param " + param + " (spill) ← a" + (i + 2));
            } else {
                prologue.add(new Move_Reg_Reg((Register) dest, argReg));
                System.err.println("📥 Param " + param + " ← a" + (i + 2));
            }
        }

        // // Append return move and epilogue -> i already do this in visit(Block) I think
        // epilogue.add(new Move_Reg_Id(t0, ret_id));
        // System.err.println("🔚 Appending return: " + t0 + " ← " + ret_id);

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
        // set global
        ret_id = n.return_id;

        // check if return_id is associated with a register
        Object ret_id_reg = lookup(ret_id);
        if (ret_id_reg instanceof Register){
            instr_list.add(new Move_Id_Reg(n.return_id, (Register) ret_id_reg));
        }
        
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
        List<Instruction> instrs = new ArrayList<>();
        Object lhs = lookup(n.lhs);
    
        if (lhs instanceof Identifier) { // spill
            instrs.add(new Move_Reg_Integer(t0, n.rhs));
            instrs.add(new Move_Id_Reg((Identifier) lhs, t0));
        } else {
            instrs.add(new Move_Reg_Integer((Register) lhs, n.rhs));
            // instrs.add(new Move_Id_Reg(n.lhs, (Register) lhs)); // 🍅 🍅 🍅 : only so other code works for now i think
        }
    
        return instrs;
    }

    /*   Identifier lhs;
    *   FunctionName rhs; */
    @Override
    public List<Instruction> visit(sparrow.Move_Id_FuncName n){
        List<Instruction> instrs = new ArrayList<>();
        Object lhs = lookup(n.lhs);
    
        if (lhs instanceof Identifier) {
            instrs.add(new Move_Reg_FuncName(t0, n.rhs));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        } else {
            instrs.add(new Move_Reg_FuncName((Register) lhs, n.rhs));
            // instrs.add(new Move_Id_Reg(n.lhs, (Register) lhs)); // 🍅 🍅 🍅 : only so other code works for now i think
        }
    
        return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Add n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs = lookup(n.lhs);
        Object arg1 = lookup(n.arg1);
        Object arg2 = lookup(n.arg2);

        // arg1
        if (arg1 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t0, (Register) arg1));
        } else {
            instrs.add(new Move_Reg_Id(t0, n.arg1));
        }
        
        // arg2
        if (arg2 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t1, (Register) arg2));
        } else {
            instrs.add(new Move_Reg_Id(t1, n.arg2));
        }

        // lhs
        if (lhs instanceof Register) {
            instrs.add(new sparrowv.Add((Register) lhs, t0, t1));
        } else {
            instrs.add(new sparrowv.Add(t0, t0, t1));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        }

        return instrs;

        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.arg1));
        // instrs.add(new Move_Reg_Id(t1, n.arg2));
        // instrs.add(new sparrowv.Add(t0, t0, t1));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Subtract n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs = lookup(n.lhs);
        Object arg1 = lookup(n.arg1);
        Object arg2 = lookup(n.arg2);

        // arg1
        if (arg1 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t0, (Register) arg1));
        } else {
            instrs.add(new Move_Reg_Id(t0, n.arg1));
        }
        
        // arg2
        if (arg2 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t1, (Register) arg2));
        } else {
            instrs.add(new Move_Reg_Id(t1, n.arg2));
        }

        // lhs
        if (lhs instanceof Register) {
            instrs.add(new sparrowv.Subtract((Register) lhs, t0, t1));
        } else {
            instrs.add(new sparrowv.Subtract(t0, t0, t1));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        }
        
        return instrs;
        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.arg1));
        // instrs.add(new Move_Reg_Id(t1, n.arg2));
        // instrs.add(new sparrowv.Subtract(t0, t0, t1));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.Multiply n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs = lookup(n.lhs);
        Object arg1 = lookup(n.arg1);
        Object arg2 = lookup(n.arg2);

        // arg1
        if (arg1 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t0, (Register) arg1));
        } else {
            instrs.add(new Move_Reg_Id(t0, n.arg1));
        }
        
        // arg2
        if (arg2 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t1, (Register) arg2));
        } else {
            instrs.add(new Move_Reg_Id(t1, n.arg2));
        }

        // lhs
        if (lhs instanceof Register) {
            instrs.add(new sparrowv.Multiply((Register) lhs, t0, t1));
        } else {
            instrs.add(new sparrowv.Multiply(t0, t0, t1));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        }
        
        return instrs;

        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.arg1));
        // instrs.add(new Move_Reg_Id(t1, n.arg2));
        // instrs.add(new sparrowv.Multiply(t0, t0, t1));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public List<Instruction> visit(sparrow.LessThan n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs = lookup(n.lhs);
        Object arg1 = lookup(n.arg1);
        Object arg2 = lookup(n.arg2);

        // arg1
        if (arg1 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t0, (Register) arg1));
        } else {
            instrs.add(new Move_Reg_Id(t0, n.arg1));
        }
        
        // arg2
        if (arg2 instanceof Register) {
            instrs.add(new Move_Reg_Reg(t1, (Register) arg2));
        } else {
            instrs.add(new Move_Reg_Id(t1, n.arg2));
        }

        // lhs
        if (lhs instanceof Register) {
            instrs.add(new sparrowv.LessThan((Register) lhs, t0, t1));
        } else {
            instrs.add(new sparrowv.LessThan(t0, t0, t1));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        }
        
        return instrs;
        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.arg1));
        // instrs.add(new Move_Reg_Id(t1, n.arg2));
        // instrs.add(new sparrowv.LessThan(t0, t0, t1));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }

    /*   Identifier lhs;
    *   Identifier base;
    *   int offset; */
    @Override
    public List<Instruction> visit(sparrow.Load n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs_reg = lookup(n.lhs);
        Object base_reg = lookup(n.base);

        if (base_reg instanceof Identifier){
            instrs.add(new Move_Reg_Id(t0, n.base));
        } else {
            instrs.add(new Move_Reg_Reg(t0, (Register) base_reg));
        }

        if (lhs_reg instanceof Identifier){
            instrs.add(new Load(t0, t0, n.offset));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        } else {
            instrs.add(new Load((Register) lhs_reg, t0, n.offset));
            // instrs.add(new Load(t0, t0, n.offset));
            // instrs.add(new Move_Reg_Reg((Register) lhs_reg, t0));
        }

        return instrs; 
        // return List.of(
        //     new Move_Reg_Id(t0, n.base),
        //     new sparrowv.Load(t0, t0, n.offset),
        //     new Move_Id_Reg(n.lhs, t0)
        // );
    }

    /*   Identifier base;
    *   int offset;
    *   Identifier rhs; */
    @Override
    public List<Instruction> visit(sparrow.Store n){
        List<Instruction> instrs = new ArrayList<>();

        Object base_reg = lookup(n.base);
        Object rhs_reg = lookup(n.rhs);

        if (rhs_reg instanceof Identifier){
            instrs.add(new Move_Reg_Id(t0, n.rhs));
        } else {
            instrs.add(new Move_Reg_Reg(t0, (Register) rhs_reg));
        }

        if (base_reg instanceof Identifier){
            instrs.add(new Move_Reg_Id(t1, n.base));
            instrs.add(new Store(t1, n.offset, t0));
        } else {
            instrs.add(new Store((Register) base_reg, n.offset, t0));
        }

        return instrs;
        // return List.of(
        //     new Move_Reg_Id(t0, n.base),
        //     new Move_Reg_Id(t1, n.rhs),
        //     new sparrowv.Store(t0, n.offset, t1)
        // );
    }

    /*   Identifier lhs;
    *   Identifier rhs; */
    @Override
    public List<Instruction> visit(sparrow.Move_Id_Id n){
        List<Instruction> instrs = new ArrayList<>();

        Object lhs = lookup(n.lhs);
        Object rhs = lookup(n.rhs);

        Register r1 = t0;
        Register r2 = t1;

        // put rhs ID -> t0
        if (rhs instanceof Register) {
            r2 = (Register) rhs;
            instrs.add(new Move_Reg_Reg(t0, r2));
        } else {
            instrs.add(new Move_Reg_Id(t0, n.rhs));
        }

        // store rhs -> lhs id
        // instrs.add(new Move_Id_Reg(n.lhs, t0));

        // lhs
        if (lhs instanceof Register){
            r1 = (Register) lhs;
            instrs.add(new Move_Reg_Reg(r1, t0));
        } else {
            instrs.add(new Move_Id_Reg(n.lhs, t0));
        }
    
        return instrs;


        // return List.of(
        //     new Move_Reg_Id(t0, n.rhs),
        //     new Move_Id_Reg(n.lhs, t0)
        // );
    }

    /*   Identifier lhs;
    *   Identifier size; */
    @Override
    public List<Instruction> visit(sparrow.Alloc n){
        List<Instruction> instrs = new ArrayList<>();
        Object lhs = lookup(n.lhs);
        Object size = lookup(n.size);

        // put size -> t0
        if (size instanceof Identifier) {
            instrs.add(new Move_Reg_Id(t0, n.size));
        } else {
            instrs.add(new Move_Reg_Reg(t0, (Register) size));
        }

        // assign lhs
        if (lhs instanceof Identifier) {
            instrs.add(new Move_Reg_Id(t1, n.lhs));
            instrs.add(new sparrowv.Alloc(t1, t0));
            instrs.add(new Move_Id_Reg(n.lhs, t1));
        } else {
            instrs.add(new sparrowv.Alloc((Register) lhs, t0));
            // instrs.add(new Move_Id_Reg(n.lhs, (Register) lhs));
        }

        return instrs;
        // return List.of(
        //     new Move_Reg_Id(t0, n.size),
        //     new sparrowv.Alloc(t0, t0),
        //     new Move_Id_Reg(n.lhs, t0)
        // );
    }

    /*   Identifier content; */
    @Override
    public List<Instruction> visit(sparrow.Print n){
        List<Instruction> instrs = new ArrayList<>();
        Object content = lookup(n.content);

        if (content instanceof Identifier) {
            instrs.add(new Move_Reg_Id(t0, n.content));
            instrs.add(new Print(t0));
        } else {
            instrs.add(new Print((Register) content));
        }

        return instrs;

        // return List.of(
        //     new Move_Reg_Id(t0, n.content),
        //     new sparrowv.Print(t0)
        // );
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
        List<Instruction> instrs = new ArrayList<>();
        Object cond_reg = lookup(n.condition);

        if (cond_reg instanceof Identifier){
            instrs.add(new Move_Reg_Id(t0, n.condition));
            instrs.add(new sparrowv.IfGoto(t0, n.label));
        } else {
            instrs.add(new sparrowv.IfGoto((Register) cond_reg, n.label));
        }

        return instrs;
        // return List.of(
        //     new Move_Reg_Id(t0, n.condition),
        //     new sparrowv.IfGoto(t0, n.label)
        // );
    }

    /*   Identifier lhs;
    *   Identifier callee;
    *   List<Identifier> args; */
    @Override
    public List<Instruction> visit(sparrow.Call n){
        System.err.println("📞 Preparing call to: " + n.callee + " → result in " + n.lhs);

        List<Instruction> instrs = new ArrayList<>();
        Object callee_reg = lookup(n.callee);
        Object lhs_reg = lookup(n.lhs);

        // Save caller (t0–t5) to identifiers
        for (int i = 0; i <= 5; i++) {
            Register t = new Register("t" + i);
            Identifier save = new Identifier("save_t" + i);
            instrs.add(new Move_Id_Reg(save, t));
        }

        // Save a2–a7
        for (int i = 2; i <= 7; i++) {
            Register a = new Register("a" + i);
            Identifier save = new Identifier("save_a" + i);
            instrs.add(new Move_Id_Reg(save, a));
        }
    
        // Move callee
        if (callee_reg instanceof Identifier){
            instrs.add(new Move_Reg_Id(t0, n.callee));
        } else {
            instrs.add(new Move_Reg_Reg(t0, (Register) callee_reg));
        }

        System.err.println("🔁 Moving callee to t0: " + n.callee + " → t0");
        
        // // 🍅 FOR NOW: This assumes the call target uses a2–a7 directly.
        // // iterate through each arg: if spill -> use it directly (do nothing), if not -> assign the reg to ID
        // for (Identifier arg : n.args){
        //     Object arg_reg = lookup(arg);

        //     if (arg_reg instanceof Register){
        //         // instrs.add(new Move_Reg_Id((Register) arg_reg, arg));
        //         instrs.add(new Move_Id_Reg(arg, (Register) arg_reg));
        //     }
        // }
        
        // Move arguments into a2–a7
        for (int i = 0; i < n.args.size(); i++) {
            Object argVal = lookup(n.args.get(i));
            Register target = new Register("a" + (i + 2)); // a2, a3, ..., a7

            moveToReg(target, argVal, instrs);
            System.err.println("📤 Arg " + n.args.get(i) + " → " + target);
        }

        Register ret_reg;
        // Move return value to lhs
        if (lhs_reg instanceof Identifier){
            instrs.add(new sparrowv.Call(t0, t0, n.args));
            instrs.add(new Move_Id_Reg(n.lhs, t0));
            ret_reg = t0;
        } else {
            instrs.add(new sparrowv.Call((Register) lhs_reg, t0, n.args));
            ret_reg = (Register) lhs_reg;
        }
    
        // Restore t0–t5
        for (int i = 0; i <= 5; i++) {
            if (ret_reg.toString().equals("t" + i)){
                System.err.println("😭 😭 😭 😭 😭 IT SHOULD GO HERE!!!");
                continue;
            }
            Register t = new Register("t" + i);
            Identifier save = new Identifier("save_t" + i);
            instrs.add(new Move_Reg_Id(t, save));
        }

        // Restore a2–a7
        for (int i = 2; i <= 7; i++) {
            Register a = new Register("a" + i);
            Identifier save = new Identifier("save_a" + i);
            instrs.add(new Move_Reg_Id(a, save));
        }
    
        return instrs;

        // List<Instruction> instrs = new ArrayList<>();
        // instrs.add(new Move_Reg_Id(t0, n.callee));
        // instrs.add(new sparrowv.Call(t0, t0, n.args));
        // instrs.add(new Move_Id_Reg(n.lhs, t0));
        // return instrs;
    }
}