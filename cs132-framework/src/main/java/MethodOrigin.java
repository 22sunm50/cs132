public class MethodOrigin {
    public String methodName;
    public String className; // Class where this method is currently defined/overridden

    public MethodOrigin(String methodName, String className) {
        this.methodName = methodName;
        this.className = className;
    }

    @Override
    public String toString() {
        return methodName + " from " + className;
    }
}