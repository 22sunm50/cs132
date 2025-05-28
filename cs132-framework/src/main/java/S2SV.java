import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        System.err.println(program.toString());

        //// fast liveness analysis
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

        //// Linear Scan Register Allocation
        // convert to List<LivenessInterval>
        List<LiveInterval> intervalList = new ArrayList<>();
        for (var entry : lv.intervals_map.entrySet()) {
            intervalList.add(new LiveInterval(entry.getKey(), entry.getValue().start, entry.getValue().end));
        }

        // choose 3 registers: t0, t1, t2
        List<String> regs = new ArrayList<>(List.of("t0", "t1", "t2"));
        LinearScanRegisterAllocator allocator = new LinearScanRegisterAllocator(intervalList, 3, regs);
        allocator.allocate();
        allocator.printResult();

    }
}