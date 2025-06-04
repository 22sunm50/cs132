import java.io.InputStream;

import IR.SparrowParser;
import IR.visitor.SparrowVConstructor;
import IR.syntaxtree.Node;
import IR.registers.Registers;

import sparrowv.Program;

public class SV2V {
    public static void main(String[] args) throws Exception {
        Registers.SetRiscVregs();
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();
        SparrowVConstructor constructor = new SparrowVConstructor();
        root.accept(constructor);
        Program program = constructor.getProgram();

        FuncVisitor fv = new FuncVisitor();
        fv.visit(program);

        System.err.println("==== Activation Records ====");
        for (String func : fv.func_to_AR.keySet()) {
            ActivationRecord ar = fv.func_to_AR.get(func);
            System.err.println("Function: " + func);
            System.err.println("  Params:");
            for (String param : ar.params_map.keySet()) {
                System.err.println("    " + param + " -> offset " + ar.params_map.get(param));
            }
            System.err.println("  Locals:");
            for (String var : ar.vars_map.keySet()) {
                System.err.println("    " + var + " -> offset " + ar.vars_map.get(var));
            }
            System.err.println("  Total Frame Size: " + ar.totalFrameSize());
            System.err.println();
        }

        TranslationVisitor tv = new TranslationVisitor(fv.func_to_AR);
        tv.visit(program);

        System.err.println("🌷 🌷 🌷 🌷 🌷 🌷 🌷 RISC-V Translation Output 🌷 🌷 🌷 🌷 🌷 🌷 🌷");
        for (String instr : tv.instr_list) {
            System.out.println(instr);  // use System.out if you want actual output (not just debug)
        }
    }
}