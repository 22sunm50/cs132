public class LivenessInterval {
    public Integer start;
    public Integer end;

    public LivenessInterval(int start) {
        this.start = start;
        this.end = -1;
    }

    public void updateEnd(int end) {
        this.end = Math.max(this.end, end);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}
