import sparrowv.visitor.Visitor;
import sparrowv.*;
import IR.token.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslationVisitor implements Visitor {
    public List<String> instr_list = new ArrayList<String>();
    Identifier ret_id = null;
    String curr_method = null;
    Map<String, ActivationRecord> func_to_AR;
    Integer mangle_count = 0;

    public TranslationVisitor (Map<String, ActivationRecord> func_to_AR){
        this.func_to_AR = func_to_AR;
    }

    public String two_args (String instr, String arg1, String arg2){
        return ("  " + instr + " " + arg1 + ", " + arg2);
    }

    public String three_args (String instr, String arg1, String arg2, String arg3){
        return ("  " + instr + " " + arg1 + ", " + arg2 + ", " + arg3);
    }

    @Override
    public void visit(Program n) {
        instr_list.add(".equiv @sbrk, 9");
        instr_list.add(".equiv @print_string, 4");
        instr_list.add(".equiv @print_char, 11");
        instr_list.add(".equiv @print_int, 1");
        instr_list.add(".equiv @exit, 10");
        instr_list.add(".equiv @exit2, 17");
        instr_list.add("");
        instr_list.add(".text");
        instr_list.add("");
        instr_list.add(".globl main");
        instr_list.add("  jal Main");
        instr_list.add("  li a0, @exit");
        instr_list.add("  ecall");
        instr_list.add("");

        for (FunctionDecl fd: n.funDecls){
            fd.accept(this);
        }

        instr_list.add(".globl print");
        instr_list.add("print:");
        instr_list.add("  mv a1, a0");
        instr_list.add("  li a0, @print_int");
        instr_list.add("  ecall");
        instr_list.add("  li a1, 10");
        instr_list.add("  li a0, @print_char");
        instr_list.add("  ecall");
        instr_list.add("  jr ra");
        instr_list.add("");
        instr_list.add(".globl error");
        instr_list.add("error:");
        instr_list.add("  mv a1, a0");
        instr_list.add("  li a0, @print_string");
        instr_list.add("  ecall");
        instr_list.add("  li a1, 10");
        instr_list.add("  li a0, @print_char");
        instr_list.add("  ecall");
        instr_list.add("  li a0, @exit");
        instr_list.add("  ecall");
        instr_list.add("abort_17:");
        instr_list.add("  j abort_17");
        instr_list.add("");
        instr_list.add(".globl alloc");
        instr_list.add("alloc:");
        instr_list.add("  mv a1, a0");
        instr_list.add("  li a0, @sbrk");
        instr_list.add("  ecall");
        instr_list.add("  jr ra");
        instr_list.add("");
        instr_list.add(".data");
        instr_list.add("");
        instr_list.add(".globl msg_nullptr");
        instr_list.add("msg_nullptr:");
        instr_list.add("  .asciiz \"null pointer\"");
        instr_list.add("  .align 2");
        instr_list.add("");
        instr_list.add(".globl msg_array_oob");
        instr_list.add("msg_array_oob:");
        instr_list.add("  .asciiz \"array index out of bounds\"");
        instr_list.add("  .align 2");
    }

    /*   Program parent;
    *   FunctionName functionName;
    *   List<Identifier> formalParameters;
    *   Block block; */
    @Override
    public void visit(FunctionDecl n) {
        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        // DON'T HAVE A "main" NAMED FUNCTION <- or else it will print out twice
        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        curr_method = n.functionName.toString();
        String func_name_mangled = curr_method;

        if (curr_method == "main"){
            curr_method = "Main";
            func_name_mangled = "Main";
        }

        ActivationRecord this_AR = func_to_AR.get(curr_method);
        Integer frame_size = this_AR.totalFrameSize();
        List<String> prologue = new ArrayList<String>();
        List<String> epilogue = new ArrayList<String>();

        
        prologue.add(".globl " + func_name_mangled);
        prologue.add(func_name_mangled + ":");
        prologue.add(two_args("sw", "fp", "-8(sp)")); // store old fp in slot 2
        prologue.add(two_args("mv", "fp", "sp")); // update fp to sp
        prologue.add(two_args("li", "t6", frame_size.toString())); // # frame size = 2 slots for ret addr & old fp + num of vars slots
        prologue.add(three_args("sub", "sp", "sp", "t6")); // alloc. the space for the AR
        prologue.add(two_args("sw", "ra", "-4(fp)")); // put ret. addr. into slot 1
        instr_list.addAll(prologue);

        n.block.accept(this);

        Integer ret_id_offset = this_AR.getOffset(ret_id.toString());
        String ret_id_calc = ret_id_offset.toString() + "(fp)";
        epilogue.add(two_args("lw", "a0", ret_id_calc)); // load ret_id in a0 (return value)
        epilogue.add(two_args("lw", "ra", "-4(fp)")); // load ret. addr. in ra
        epilogue.add(two_args("lw", "fp", "-8(fp)")); // load old fp into fp
        epilogue.add(three_args("addi", "sp", "sp", frame_size.toString())); // remove frame size from AR
        epilogue.add("  jr ra");
        instr_list.addAll(epilogue);
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

        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        // HANDLE RETURN STATEMENTS HERE LATER !!!
        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        // instr_list.add(new Move_Reg_Id(t0, n.return_id));
    }

    /*   Label label; */
    @Override
    public void visit(LabelInstr n) {
        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        // MANGLE LABEL LATER !!!
        // 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅 🍅
        String instr = curr_method + n.label.toString() + ":";
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   int rhs; */
    @Override
    public void visit(Move_Reg_Integer n){
        String instr = two_args("li", n.lhs.toString(), Integer.toString(n.rhs));
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   FunctionName rhs; */
    @Override
    public void visit(Move_Reg_FuncName n) {
        String instr = two_args("la", n.lhs.toString(), n.rhs.toString());
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Add n){
        String instr = three_args("add", n.lhs.toString(), n.arg1.toString(), n.arg2.toString());
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Subtract n){
        String instr = three_args("sub", n.lhs.toString(), n.arg1.toString(), n.arg2.toString());
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(Multiply n){
        String instr = three_args("mul", n.lhs.toString(), n.arg1.toString(), n.arg2.toString());
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    @Override
    public void visit(LessThan n){
        String instr = three_args("slt", n.lhs.toString(), n.arg1.toString(), n.arg2.toString());
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register base;
    *   int offset; */
    @Override
    public void visit(Load n){
        String rhs = n.offset + "(" + n.base + ")";
        String instr = two_args("lw", n.lhs.toString(), rhs);
        instr_list.add(instr);
    }

    /*   Register base;
    *   int offset;
    *   Register rhs; */
    @Override
    public void visit(Store n){
        String rhs = n.offset + "(" + n.base + ")";
        String instr = two_args("sw", n.rhs.toString(), rhs);
        instr_list.add(instr);
    }

    /*   Register lhs;
    *   Register rhs; */
    @Override
    public void visit(Move_Reg_Reg n){
        String instr = two_args("mv", n.lhs.toString(), n.rhs.toString());
        instr_list.add(instr);
    }

    /*   Identifier lhs;
    *   Register rhs; */
    @Override
    public void visit(Move_Id_Reg n){
        ActivationRecord this_AR = func_to_AR.get(curr_method);
        Integer offset = this_AR.getOffset(n.lhs.toString());
        String offset_instr = offset + "(fp)";
        instr_list.add(two_args("sw", n.rhs.toString(), offset_instr));
    }

    /*   Register lhs;
    *   Identifier rhs; */
    @Override
    public void visit(Move_Reg_Id n){
        ActivationRecord this_AR = func_to_AR.get(curr_method);
        Integer offset = this_AR.getOffset(n.rhs.toString());
        String offset_instr = offset + "(fp)";
        instr_list.add(two_args("lw", n.lhs.toString(), offset_instr));
    }

    /*   Register lhs;
    *   Register size; */
    @Override
    public void visit(Alloc n){
        String instr1 = two_args("mv", "a0", n.size.toString());
        instr_list.add(instr1);
        String instr2 = "  jal alloc";
        instr_list.add(instr2);
        String instr3 = two_args("mv", n.lhs.toString(), "a0");
        instr_list.add(instr3);
    }

    /*   Register content; */
    @Override
    public void visit(Print n){
        String instr1 = two_args("mv", "a0", n.content.toString());
        instr_list.add(instr1);
        String instr2 = "  jal print";
        instr_list.add(instr2);
    }

    /*   String msg; */
    @Override
    public void visit(ErrorMessage n){
        if (n.msg == "\"null pointer\""){
            instr_list.add(two_args("la", "a0", "msg_nullptr"));
            instr_list.add("  jal error");
        }
        else {
            instr_list.add(two_args("la", "a0", "msg_array_oob"));
            instr_list.add("  jal error");
        }
    }

    /*   Label label; */
    @Override
    public void visit(Goto n){

        instr_list.add("  jal " + curr_method + n.label.toString());
    }

    /*   Register condition;
    *   Label label; */
    @Override
    public void visit(IfGoto n){
        String mangled_label = curr_method + n.label + "_no_long_jump" + mangle_count;
        mangle_count ++;
        instr_list.add(two_args("bnez", n.condition.toString(), mangled_label));
        instr_list.add("  jal " + curr_method + n.label);
        instr_list.add(mangled_label + ":");
    }

    /*   Register lhs;
    *   Register callee;
    *   List<Identifier> args; */
    @Override
    public void visit(Call n){
        Integer args_size = n.args.size() * 4;
        ActivationRecord this_AR = func_to_AR.get(curr_method);

        instr_list.add(two_args("li", "t6", args_size.toString())); // make args_size slots
        instr_list.add(three_args("sub", "sp", "sp", "t6")); // add args_size slots to AR
        
        Integer slot_count = 0;
        for (Identifier arg : n.args){
            Integer offset = this_AR.getOffset(arg.toString());
            String load_offset = offset + "(fp)";
            String store_offset = slot_count + "(sp)";

            instr_list.add(two_args("lw", "t6", load_offset)); // load param into t6
            instr_list.add(two_args("sw", "t6", store_offset)); // store into slot
            slot_count += 4;
        }

        instr_list.add("  jalr " + n.callee); // call t1 and set ra to here
        instr_list.add(three_args("addi", "sp", "sp", args_size.toString())); // remove args_size from AR
        instr_list.add(two_args("mv", n.lhs.toString(), "a0")); // move return value (a0) to t0
    }
}