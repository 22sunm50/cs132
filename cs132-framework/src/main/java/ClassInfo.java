import java.util.ArrayList;
import java.util.HashMap;

public class ClassInfo {
    HashMap<String, MyType> fields_map;
    HashMap<String, Integer> field_offsets;
    ArrayList<String> field_table_list;
    ArrayList<MethodOrigin> method_origin_list;

    HashMap<String, MethodInfo> methods_map;
    String parent_name;

    public ClassInfo() {
        fields_map = new HashMap<>();
        field_offsets = new HashMap<>();
        methods_map = new HashMap<>();
        field_table_list = new ArrayList<>();
        method_origin_list = new ArrayList<>();
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
        }
        // Fail if field name conflicts with a method name
        if (methods_map.containsKey(fieldName)) {
            System.err.println("🚨 Field name conflicts with a method name: " + fieldName);
        }
        Integer offset = (fields_map.size() + 1) * 4;
        fields_map.put(fieldName, typeName);
        field_offsets.put(fieldName, offset);
    }

    // get type of a field
    public MyType getFieldType(String fieldName) {
        if (!this.hasField(fieldName)){
            System.err.println("🚨: Fetching a field (" + fieldName + ") that DNE");
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
        }
        // Fail if method name conflicts with a field name
        if (fields_map.containsKey(methodName)) {
            System.err.println("🚨 Method name conflicts with a field name: " + methodName);
        }
        methods_map.put(methodName, methodInfo);
    }

    // get method info
    public MethodInfo getMethodInfo(String methodName) {
        if (!this.hasMethod(methodName)){
            System.err.println("🚨 MethodInfo: tried getting non-existent method: " + methodName);
        }
        return methods_map.get(methodName);
    }

    // check if method exists
    public boolean hasMethod(String methodName) {
        return methods_map.containsKey(methodName);
    }

    public int getMethodOffset(String methodName) {
        if (!methods_map.containsKey(methodName)) {
            System.err.println("🚨 Method offset lookup failed: " + methodName + " does not exist");
        }
        MethodInfo m = methods_map.get(methodName);
        if (m == null || m.method_offset == null) {
            System.err.println("🚨 Method offset is undefined for: " + methodName);
        }
        return m.method_offset;
    }

    // get method offset:
    public int getFieldOffset(String fieldName) {
        if (!field_offsets.containsKey(fieldName)) {
            System.err.println("🚨 Field offset lookup failed: " + fieldName + " does not exist");
        }
        return field_offsets.get(fieldName);
    }
}