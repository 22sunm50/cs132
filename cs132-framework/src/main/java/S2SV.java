import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import IR.syntaxtree.Node;

public class S2SV {
    public static void main(String [] args) throws Exception {
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();

        SparrowConstructor constructor = new SparrowConstructor();
        root.accept(constructor);
        sparrow.Program program = constructor.getProgram();
        System.err.println("🌼 🌼 🌼 🌼 🌼 INPUT SPARROW PROGRAM 🌼 🌼 🌼 🌼 🌼");
        System.err.println(program.toString());

        //// FAST LIVENESS ANALYSIS
        HashMap<String, LiveInterval> intervals_map = new HashMap<>();
        FastLivenessVisitor lv = new FastLivenessVisitor();
        lv.visit(program);
        intervals_map = lv.intervals_map;

        // print results
        for (Map.Entry<String, LiveInterval> entry : intervals_map.entrySet()) {
            System.err.println(entry.getKey() + " = " + entry.getValue());
        }

        System.err.println("📍 Label to Line Map:");
        for (Map.Entry<String, Integer> entry : lv.labelToLine.entrySet()) {
            System.err.println("  " + entry.getKey() + " : line " + entry.getValue());
        }

        System.err.println("🔁 Detected Loop Ranges:");
        for (LoopRange loop : lv.loopRanges) {
            System.err.println("  Loop from line " + loop.start + " to line " + loop.end);
        }

        //// LINEAR SCAN REGISTER ALLOCATION
        // convert to List<LivenessInterval>
        List<LiveInterval> intervalList = new ArrayList<>();
        for (var entry : lv.intervals_map.entrySet()) {
            intervalList.add(new LiveInterval(entry.getKey(), entry.getValue().start, entry.getValue().end));
        }

        // choose 3 registers: t0, t1, t2
        // List<String> regs = new ArrayList<>(List.of("t2", "t3", "t4", "t5"));
        List<String> regs = new ArrayList<>(List.of("t2", "t3", "t4", "t5", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"
                                                                ));
        LinearScanRegisterAllocator allocator = new LinearScanRegisterAllocator(intervalList, 15, regs, lv.f_call_lines, lv.func_arg_count_map, lv.func_interval_map);
        allocator.allocate();
        allocator.printResult();

        lv.printFuncIntervalMap();

        //// TRANSLATION
        // in a variables liveness variable, if u can track a func call, then alloc to a callee save register so you don't have to do reallocation
        // its just a flag, and just go to first pass and hit a call and iterate through all the ur variable intervals and check them off
        // if you can multiple calls to a funct, if you save to a callee save register, then you have less calls to the stack

        TranslationVisitor tv = new TranslationVisitor(allocator.getRegisterMap(), intervals_map, lv.func_interval_map);
        tv.visit(program);
    }
}