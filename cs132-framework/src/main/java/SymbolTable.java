import java.util.HashMap;

public class SymbolTable {
    public HashMap < String, ClassInfo > class_table; // for class info
    public HashMap<String, HashMap<String, Boolean>> is_subtype; // move from ClassTableVisitor

    public SymbolTable() {
        class_table = new HashMap<>();
        is_subtype = new HashMap<>();
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
            System.out.println("Type error");
            System.exit(1);
        }
        return class_table.get(className);
    }

    public boolean hasClass(String className) {
        return class_table.containsKey(className);
    }

    public void printClassTable() {
        System.out.println("===== CLASS TABLE =====");
        for (String className : class_table.keySet()) {
            System.out.println("Class: " + className);
            ClassInfo info = class_table.get(className);
    
            // Print Fields
            System.out.println("  Fields:");
            for (String fieldName : info.fields_map.keySet()) {
                MyType fieldType = info.fields_map.get(fieldName);
                System.out.println("    " + fieldName + " : " + fieldType.toString());
            }
    
            // Print Methods
            System.out.println("  Methods:");
            for (String methodName : info.methods_map.keySet()) {
                MethodInfo methodInfo = info.methods_map.get(methodName);
                System.out.println("    Method: " + methodName + " : " + methodInfo.toString());
    
                // Print Arguments Map
                System.out.println("      Arguments:");
                for (String argName : methodInfo.args_map.keySet()) {
                    MyType argType = methodInfo.args_map.get(argName);
                    System.out.println("        " + argName + " : " + argType.toString());
                }
    
                // Print Variables Map
                System.out.println("      Variables:");
                for (String varName : methodInfo.vars_map.keySet()) {
                    MyType varType = methodInfo.vars_map.get(varName);
                    System.out.println("        " + varName + " : " + varType.toString());
                }
            }
    
            System.out.println();
        }
    }
}