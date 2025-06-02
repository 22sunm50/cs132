import java.util.*;

import IR.token.Register;

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
                if (i.end == -1){
                    registerMap.put(i.name, "t0");
                } else {
                    spillAtInterval(i);
                }
            } 
            else { // there are available regs
                boolean containsCall = false;
                for (int line : f_call_lines) {
                    if (i.start < line && line < i.end) { // 🍅 🍅 : GOT ME TO 61 BUT FAILING 2 TEST CASES
                        containsCall = true;
                        break;
                    }
                }

                // 🍒 Try to get a register based on call containment
                String selectedReg = null;

                if (containsCall) {
                    // Prefer s-registers
                    for (String r : registerPool) {
                        if (r.startsWith("s")) {
                            selectedReg = r;
                            break;
                        }
                    }
                    if (selectedReg == null) {
                        selectedReg = registerPool.get(0);
                    }
                } else {
                    // Prefer t-registers
                    for (String r : registerPool) {
                        if (r.startsWith("t")) {
                            selectedReg = r;
                            break;
                        }
                    }
                    if (selectedReg == null) {
                        selectedReg = registerPool.get(0);
                    }
                }

                if (selectedReg != null) {
                    registerPool.remove(selectedReg);
                    registerMap.put(i.name, selectedReg);
                    active.add(i);
                    active.sort(Comparator.comparingInt(j -> j.end));
            
                    System.err.println("  ✅ Allocated " + selectedReg + " to " + i.name);
                    printActive();
                } else {
                    System.err.println("  🧨 Logic error: no register found but active.size() < NUM_REGS?");
                }
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
