import java.util.HashMap;

public class ClassInfo {
    HashMap<String, MyType> fields_map;
    HashMap<String, Integer> field_offsets;

    HashMap<String, MethodInfo> methods_map;
    String parent_name;

    public ClassInfo() {
        fields_map = new HashMap<>();
        field_offsets = new HashMap<>();
        methods_map = new HashMap<>();
        parent_name = null;
    }

    public void setParentClassName(String parent) {
        this.parent_name = parent;
    }

    public String getParentClassName() {
        return parent_name;
    }

    public void addField(String fieldName, MyType typeName) {
        // Fail if field already exists
        if (fields_map.containsKey(fieldName)) {
            System.err.println("🚨 Field already exists: " + fieldName);
            printFailureAndExit();
        }
        // Fail if field name conflicts with a method name
        if (methods_map.containsKey(fieldName)) {
            System.err.println("🚨 Field name conflicts with a method name: " + fieldName);
            printFailureAndExit();
        }
        Integer offset = (fields_map.size() + 1) * 4;
        fields_map.put(fieldName, typeName);
        field_offsets.put(fieldName, offset);
    }

    // get type of a field
    public MyType getFieldType(String fieldName) {
        if (!this.hasField(fieldName)){
            System.err.println("🚨: Fetching a field (" + fieldName + ") that DNE");
            printFailureAndExit();
        }
        return fields_map.get(fieldName);
    }

    // check if field exists
    public boolean hasField(String fieldName) {
        return fields_map.containsKey(fieldName);
    }

    public void addMethod(String methodName, MethodInfo methodInfo) {
        // Fail if method already exists
        if (methods_map.containsKey(methodName)) {
            System.err.println("🚨 Method already exists: " + methodName);
            printFailureAndExit();
        }
        // Fail if method name conflicts with a field name
        if (fields_map.containsKey(methodName)) {
            System.err.println("🚨 Method name conflicts with a field name: " + methodName);
            printFailureAndExit();
        }
        methods_map.put(methodName, methodInfo);
    }

    // get method info
    public MethodInfo getMethodInfo(String methodName) {
        if (!this.hasMethod(methodName)){
            System.err.println("🚨 MethodInfo: tried getting non-existent method: " + methodName);
            printFailureAndExit();
        }
        return methods_map.get(methodName);
    }

    // check if method exists
    public boolean hasMethod(String methodName) {
        return methods_map.containsKey(methodName);
    }

    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }
}