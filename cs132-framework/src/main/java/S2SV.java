import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

        // fast liveness analysis
        HashMap<String, LivenessInterval> intervals_map = new HashMap<>();
        FastLivenessVisitor lv = new FastLivenessVisitor();
        lv.visit(program);
        intervals_map = lv.intervals_map;

        // print results
        for (Map.Entry<String, LivenessInterval> entry : intervals_map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("📍 Label to Line Map:");
        for (Map.Entry<String, Integer> entry : lv.labelToLine.entrySet()) {
            System.out.println("  " + entry.getKey() + " : line " + entry.getValue());
        }

        System.out.println("🔁 Detected Loop Ranges:");
        for (LoopRange loop : lv.loopRanges) {
            System.out.println("  Loop from line " + loop.start + " to line " + loop.end);
        }
    }
}