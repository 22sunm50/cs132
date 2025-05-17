import java.util.HashMap;

public class SymbolTable {
    public HashMap < String, ClassInfo > class_table; // for class info
    public HashMap<String, HashMap<String, Boolean>> is_subtype; // move from ClassTableVisitor

    String[] reserved_names = {"a2", "a3", "a4", "a5", "a6", "a7", 
                                "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
                                "t0", "t1", "t3", "t4", "t5"};

    public SymbolTable() {
        class_table = new HashMap<>();
        is_subtype = new HashMap<>();
    }

    public String sanitizeName(String name) {
        for (String reserved : reserved_names) {
            if (reserved.equals(name)) {
                return "zzz" + name;
            }
        }
        return name;
    }

    // 🤰 🤰 🤰 🤰 🤰 🤰 SUBTYPING🤰 🤰 🤰 🤰 🤰 🤰
    public void setIsSubtypeMap(HashMap<String, HashMap<String, Boolean>> is_subtype) {
        this.is_subtype = is_subtype;
    }
    
    public boolean isSubtype(String a, String b) {
        return is_subtype.containsKey(a) && is_subtype.get(a).getOrDefault(b, false);
    }
    
    // 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 CLASS TABLE FUNCS 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫
    public void addClass(String className, ClassInfo classInfo) {
        class_table.put(className, classInfo);
    }

    public ClassInfo getClassInfo(String className) {
        if (!this.hasClass(className)){
            System.err.println("🚨 SymbolTable: tried getting non-existent class: " + className);
        }
        return class_table.get(className);
    }

    public boolean hasClass(String className) {
        return class_table.containsKey(className);
    }

    public void printClassTable() {
        System.err.println("===== CLASS TABLE =====");
        for (String className : class_table.keySet()) {
            System.err.println("Class: " + className);
            ClassInfo info = class_table.get(className);
    
            // Print Fields
            System.err.println("  Fields:");
            for (String fieldName : info.fields_map.keySet()) {
                MyType fieldType = info.fields_map.get(fieldName);
                int offset = info.getFieldOffset(fieldName);
                System.err.println("    " + fieldName + " : " + fieldType.toString() + " at offset " + offset);
            }
    
            // Print Field Table List (ordered + includes shadowed fields)
            System.err.println("  Full Field Order (field_table_list):");
            for (String fieldName : info.field_table_list) {
                int offset = info.getFieldOffset(fieldName);
                System.err.println("    " + fieldName + " @ offset " + offset);
            }
    
            // Print Methods
            System.err.println("  Methods:");
            for (String methodName : info.methods_map.keySet()) {
                MethodInfo methodInfo = info.methods_map.get(methodName);
                int offset = info.getMethodOffset(methodName);
                System.err.println("    Method: " + methodName + " : " + methodInfo.toString() + " @ offset " + offset);
    
                // Print Arguments Map
                System.err.println("      Arguments:");
                for (String argName : methodInfo.args_map.keySet()) {
                    MyType argType = methodInfo.args_map.get(argName);
                    System.err.println("        " + argName + " : " + argType.toString());
                }
    
                // Print Variables Map
                System.err.println("      Variables:");
                for (String varName : methodInfo.vars_map.keySet()) {
                    MyType varType = methodInfo.vars_map.get(varName);
                    System.err.println("        " + varName + " : " + varType.toString());
                }
            }
    
            // Print Method Origin List
            System.err.println("  Full Method Order (method_origin_list):");
            for (int i = 0; i < info.method_origin_list.size(); i++) {
                MethodOrigin mo = info.method_origin_list.get(i);
                int offset = i * 4;
                System.err.println("    " + mo.methodName + " from " + mo.className + " @ offset " + offset);
            }
    
            System.err.println();
        }
    }    
}