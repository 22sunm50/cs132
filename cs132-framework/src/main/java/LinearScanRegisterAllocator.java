import java.util.*;

public class LinearScanRegisterAllocator {
    private final Integer NUM_REGS;
    private final List<String> registerPool;
    private final List<LiveInterval> intervals;
    private final List<LiveInterval> active = new ArrayList<>();
    private final Map<String, String> registerMap = new HashMap<>();
    private final Set<String> spilled = new HashSet<>();

    public LinearScanRegisterAllocator(List<LiveInterval> intervals, Integer numRegisters, List<String> availableRegisters) {
        this.intervals = new ArrayList<>(intervals);
        this.NUM_REGS = numRegisters;
        this.registerPool = new ArrayList<>(availableRegisters);
    }

    public void allocate() {
        intervals.sort(Comparator.comparingInt(i -> i.start));

        for (LiveInterval i : intervals) {
            System.err.println("\n🔹Processing: " + i.name + " [" + i.start + ", " + i.end + "]");

            expireOldIntervals(i);
            if (active.size() == NUM_REGS) {
                System.err.println("  💥 No free registers. Need to spill.");
                spillAtInterval(i);
            } else {
                // String reg = registerPool.remove(registerPool.size() - 1);
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
