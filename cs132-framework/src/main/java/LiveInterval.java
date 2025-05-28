public class LiveInterval {
    public String name;
    public Integer start;
    public Integer end;

    public LiveInterval(String var_name, Integer start) {
        this.name = var_name;
        this.start = start;
        this.end = -1;
    }

    public LiveInterval(String var_name, Integer start, Integer end) {
        this.name = var_name;
        this.start = start;
        this.end = end;
    }

    public void updateEnd(Integer end) {
        this.end = Math.max(this.end, end);
    }

    @Override
    public String toString() {
        return name = " = [" + start + ", " + end + "]";
    }
}
