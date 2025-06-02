import java.util.*;

public class LinearScanRegisterAllocator {
    private final Integer NUM_REGS;
    private final List<String> registerPool;
    private final List<LiveInterval> intervals;
    private final List<LiveInterval> active = new ArrayList<>();
    private final Map<String, String> registerMap = new HashMap<>();
    private final Set<String> spilled = new HashSet<>();

    ArrayList<Integer> f_call_lines = new ArrayList<Integer>();
    Map<String, Integer> func_arg_count_map = new HashMap<String, Integer>();
    HashMap<String, LiveInterval> func_interval_map = new HashMap<String, LiveInterval>(); // intervals of func decls
    HashMap<String, HashMap < String , ArrayList<LiveInterval>>> func_a_used_map = new HashMap<String, HashMap < String , ArrayList<LiveInterval>>> (); // func -> (a_reg used -> list of interval)

    public LinearScanRegisterAllocator(List<LiveInterval> intervals, Integer numRegisters, List<String> availableRegisters, ArrayList<Integer> f_call_lines, Map<String, Integer> func_arg_count_map, HashMap<String, LiveInterval> func_interval_map) {
        this.intervals = new ArrayList<>(intervals);
        this.NUM_REGS = numRegisters;
        this.registerPool = new ArrayList<>(availableRegisters);
        this.f_call_lines = f_call_lines;
        this.func_arg_count_map = func_arg_count_map;
        this.func_interval_map = func_interval_map;
    }

    public void allocate() {
        intervals.sort(Comparator.comparingInt(i -> i.start));

        for (LiveInterval i : intervals) {
            System.err.println("\n🔹Processing: " + i.name + " [" + i.start + ", " + i.end + "]");

            expireOldIntervals(i);
            if (active.size() == NUM_REGS) { // SPILL
                System.err.println("  💥 No free registers. Need to spill.");


                // String selectedReg = null;

                // // Find which function this interval belongs to
                // String currentFunc = null;
                // for (Map.Entry<String, LiveInterval> entry : func_interval_map.entrySet()) {
                //     LiveInterval funcInterval = entry.getValue();
                //     if (i.start >= funcInterval.start && i.end <= funcInterval.end) {
                //         currentFunc = entry.getKey();
                //         break;
                //     }
                // }

                // if (currentFunc != null) { // if found entirely inside a func
                //     int argCount = func_arg_count_map.get(currentFunc);
                //     Set<String> usedARegs = new HashSet<>();
                //     for (int j = 2; j < 2 + argCount; j++) {
                //         usedARegs.add("a" + j);
                //     }

                //     HashMap<String, ArrayList<LiveInterval>> used_a_list = func_a_used_map.get(currentFunc);
                //     for (ArrayList<LiveInterval> a_intervals : used_a_list){
                //         String a_reg = a_interval.name;
                //         if (usedARegs.contains(a_reg)){ // already used as param
                //             continue;
                //         }
                //         if ( (i.start >= a_interval.start && i.start <= a_interval.end ) || ( i.end >= a_interval.start && i.end <= a_interval.end ) ) {
                //             // they overlap and cannot be used
                //         } else {
                //             // assign selectedReg
                //             selectedReg = a_reg;
                //             // 
                //         }
                //     }

                //     // for (String reg : registerPool) {
                //     //     if (argCount < 6 && reg.startsWith("a") && !usedARegs.contains(reg)) {
                //     //         selectedReg = reg;
                //     //         break;
                //     //     }
                //     // }
                // }

                // // Fallback to any available register
                // if (selectedReg == null) {
                //     selectedReg = registerPool.get(0);
                // }

                // registerPool.remove(selectedReg);
                // registerMap.put(i.name, selectedReg);
                // active.add(i);
                // active.sort(Comparator.comparingInt(j -> j.end));

                // System.err.println("  ✅ Allocated " + selectedReg + " to " + i.name);
                // printActive();







                spillAtInterval(i);
            } else { // there are available regs
                String reg = registerPool.remove(0);
                registerMap.put(i.name, reg);
                active.add(i);
                active.sort(Comparator.comparingInt(j -> j.end));

                System.err.println("  ✅ Allocated " + reg + " to " + i.name);
                printActive();
            }
        }
    }

    private void expireOldIntervals(LiveInterval i) {
        Iterator<LiveInterval> iter = active.iterator();
        while (iter.hasNext()) {
            LiveInterval j = iter.next();
            if (j.end >= i.start) break;

            iter.remove();
            String freedReg = registerMap.get(j.name);
            registerPool.add(freedReg);

            System.err.println("  ⏳ Expired " + j.name + ", released " + freedReg);
        }
    }

    // private void expireOldIntervals(LiveInterval current) {
    //     // 1) Collect every interval whose end < current.start
    //     List<LiveInterval> toRemove = new ArrayList<>();
    //     for (LiveInterval iv : active) {
    //         if (iv.end < current.start) {
    //             toRemove.add(iv);
    //         }
    //     }
    
    //     // 2) Remove them from active, free their registers
    //     for (LiveInterval iv : toRemove) {
    //         active.remove(iv);
            
    //         // registerPool.add(freedReg);

    //         if (!spilled.contains(iv.name)){
    //         // if (freedReg != null) {
    //             String freedReg = registerMap.get(iv.name);
    //             registerPool.add(freedReg);
    //             System.err.println("  ⏳ Expired " + iv.name + ", released " + freedReg);
    //         }
    //     }
    
    //     // 3) Re-sort the remaining active set by end time
    //     active.sort(Comparator.comparingInt(j -> j.end));
    // }

    private void spillAtInterval(LiveInterval i) {
        LiveInterval spill = active.get(active.size() - 1); // last = largest end
        if (spill.end > i.end) {
            // i gets register, spill goes to memory
            String reg = registerMap.get(spill.name);
            System.err.println("  🔁 Spilling " + spill.name + " to assign " + i.name);

            registerMap.put(i.name, reg);
            spilled.add(spill.name);
            registerMap.remove(spill.name);
            active.remove(spill);
            active.add(i);
            active.sort(Comparator.comparingInt(j -> j.end));
        } else {
            // i is spilled
            spilled.add(i.name);

            System.err.println("  🚫 Spilling " + i.name + " (no register assigned)");
        }
    }

    public void printResult() {
        System.err.println("\n🏁 Final Register Allocation Result:");
        for (LiveInterval i : intervals) {
            if (spilled.contains(i.name)) {
                System.err.println(i.name + " → SPILL");
            } else {
                System.err.println(i.name + " → " + registerMap.get(i.name));
            }
        }
    }

    private void printActive() {
        System.err.print("  🔒 Active set: ");
        for (LiveInterval a : active) {
            System.err.print(a.name + "[" + a.end + "] ");
        }
        System.err.println();
    }

    public Map<String, String> getRegisterMap() {
        return registerMap;
    }

    public Set<String> getSpilledVars() {
        return spilled;
    }
}
