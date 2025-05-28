import java.util.ArrayList;
import java.util.HashMap;

import sparrow.visitor.*;
import sparrow.*;
import IR.token.Identifier;

public class FastLivenessVisitor implements Visitor {
    HashMap<String, LivenessInterval> intervals_map = new HashMap<String, LivenessInterval>();
    HashMap<String, Integer> labelToLine = new HashMap<String, Integer>();
    ArrayList<LoopRange> loopRanges = new ArrayList<LoopRange>();

    Integer currentLine = 1;

    private void def(String id) {
        if (intervals_map.putIfAbsent(id, new LivenessInterval(currentLine)) != null){
            System.err.println("🚨 def() in lv: try to def (" + id + ") already in the intervals_map");
        }
        // want to keep the earliest def
        intervals_map.get(id).start = Math.min(intervals_map.get(id).start, currentLine);
    }

    private void use(String id) {
        if (!id.matches("\\d+")) { // id is a number, not a variable, skip constants
            intervals_map.putIfAbsent(id, new LivenessInterval(currentLine));
            intervals_map.get(id).updateEnd(currentLine);
        }
    }

    /*   List<FunctionDecl> funDecls; */
    @Override
    public void visit(Program n){
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this);
        }
    }

    /*   Program parent;
    *   FunctionName functionName;
    *   List<Identifier> formalParameters;
    *   Block block; */
    @Override
    public void visit(FunctionDecl n){
        if (n.formalParameters != null) {
            for (Identifier id : n.formalParameters) {
                def(id.toString());  // parameters are defined at the start
            }
        }
        n.block.accept(this);
    }

    /*   FunctionDecl parent;
    *   List<Instruction> instructions;
    *   Identifier return_id; */
    @Override
    public void visit(Block n){
        // for (Instruction instr : n.instructions) {
        //     instr.accept(this);
        // }
        // // treat return as a use of the return_id
        // use(n.return_id.toString());
        // currentLine++;

        // // post-processing to extend live ranges for loop variables
        // for (var entry : varToLoops.entrySet()) {
        //     String var = entry.getKey();
        //     for (LoopRange loop : entry.getValue()) {
        //         intervals_map.get(var).updateEnd(loop.end);
        //     }
        // }

        for (Instruction instr : n.instructions) {
            instr.accept(this);
        }
    
        // Treat return as use
        use(n.return_id.toString());
        currentLine++;
    
        // === Loop-aware live range extension ===
        for (LoopRange loop : loopRanges) {
            for (var entry : intervals_map.entrySet()) {
                // String var = entry.getKey();
                LivenessInterval interval = entry.getValue();
                int def = interval.start;
                int lastUse = interval.end;
    
                // Variable was defined before loop
                // And used somewhere inside loop (after label/start but before or inside end)
                if (def < loop.start && lastUse >= loop.start && lastUse <= loop.end) {
                    interval.updateEnd(loop.end);
                }
            }
        }
    }

    /*   Label label; */
    @Override
    public void visit(LabelInstr n){
        labelToLine.put(n.label.toString(), currentLine);
        currentLine++; // label only, no variables
    }

    /*   Identifier lhs;
    *   int rhs; */
    @Override
    public void visit(Move_Id_Integer n){
        def(n.lhs.toString()); // definition only
        currentLine++;
    }

    /*   Identifier lhs;
    *   FunctionName rhs; */
    @Override
    public void visit(Move_Id_FuncName n){
        def(n.lhs.toString()); // function name is just stored in lhs
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public void visit(Add n){
        def(n.lhs.toString());
        use(n.arg1.toString());
        use(n.arg2.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public void visit(Subtract n){
        def(n.lhs.toString());
        use(n.arg1.toString());
        use(n.arg2.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public void visit(Multiply n){
        def(n.lhs.toString());
        use(n.arg1.toString());
        use(n.arg2.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier arg1;
    *   Identifier arg2; */
    @Override
    public void visit(LessThan n){
        def(n.lhs.toString());
        use(n.arg1.toString());
        use(n.arg2.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier base;
    *   int offset; */
    @Override
    public void visit(Load n){
        def(n.lhs.toString());
        use(n.base.toString()); // base register being indexed
        currentLine++;
    }

    /*   Identifier base;
    *   int offset;
    *   Identifier rhs; */
    @Override
    public void visit(Store n){
        use(n.base.toString());
        use(n.rhs.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier rhs; */
    @Override
    public void visit(Move_Id_Id n){
        def(n.lhs.toString());
        use(n.rhs.toString());
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier size; */
    @Override
    public void visit(Alloc n){
        def(n.lhs.toString());
        use(n.size.toString());
        currentLine++;
    }

    /*   Identifier content; */
    @Override
    public void visit(Print n){
        use(n.content.toString());
        currentLine++;
    }

    /*   String msg; */
    @Override
    public void visit(ErrorMessage n){
        currentLine++; // no variable interaction
    }

    /*   Label label; */
    @Override
    public void visit(Goto n){
        Integer targetLine = labelToLine.get(n.label.toString());
        if (targetLine != null && targetLine < currentLine) {
            loopRanges.add(new LoopRange(targetLine, currentLine));
        }
        currentLine++; // control flow only
    }

    /*   Identifier condition;
    *   Label label; */
    @Override
    public void visit(IfGoto n){
        use(n.condition.toString());

        Integer targetLine = labelToLine.get(n.label.toString());
        if (targetLine != null && targetLine < currentLine) {
            loopRanges.add(new LoopRange(targetLine, currentLine));
        }
        currentLine++;
    }

    /*   Identifier lhs;
    *   Identifier callee;
    *   List<Identifier> args; */
    @Override
    public void visit(Call n){
        def(n.lhs.toString());
        use(n.callee.toString());
        for (Identifier arg : n.args) {
            use(arg.toString());
        }
        currentLine++;
    }
}
