public class LoopRange {
    public Integer start;
    public Integer end;

    public LoopRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(int line) {
        return line >= start && line <= end;
    }
}