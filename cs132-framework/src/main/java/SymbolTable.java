import java.util.Stack;
import java.util.HashMap;

public class SymbolTable {
    public Stack < HashMap < String, MyType >> env; // for local vars and args
    public HashMap < String, ClassInfo > class_table; // for class info

    public SymbolTable() {
        env = new Stack<>();
        class_table = new HashMap<>();
    }

    //  🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 SYMBOL TABLE FUNCS 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑 🪑
    public void pushScope() {
        env.push(new HashMap<>());
    }
    
    public void popScope() {
        if (!env.isEmpty()) {
            env.pop();
        } else {
            throw new RuntimeException("No scope to pop.");
        }
    }

    // add var (curr scope)
    public void addVar(String varName, MyType varType) {
        if (env.isEmpty()) {
            pushScope();
        }
        env.peek().put(varName, varType);
    }
    
    // look up var (from innermost to outtermost)
    public MyType lookupVar(String varName) {
        for (int i = env.size() - 1; i >= 0; i--) {
            if (env.get(i).containsKey(varName)) {
                return env.get(i).get(varName);
            }
        }
        return null; // variable not found
    }

    public void printEnv() {
        System.out.println("Environment Stack:");
        for (HashMap<String, MyType> scope : env) {
            System.out.println(scope);
        }
    }
    
    // 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 CLASS TABLE FUNCS 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫 👩‍🏫
    public void addClass(String className, ClassInfo classInfo) {
        class_table.put(className, classInfo);
    }

    public ClassInfo getClassInfo(String className) {
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
                System.out.println("    " + methodName + " : " + methodInfo.toString());
            }
    
            System.out.println();
        }
    }
}


// import java.util.Stack;
// import java.util.HashMap;
// import java.util.Map;

// public class SymbolTable {
//     private Stack<HashMap<String, MyType>> env;

//     public SymbolTable() {
//         env = new Stack<>();
//     }

//     public void pushScope() {
//         env.push(new HashMap<>());
//     }

//     public void popScope() {
//         if (!env.isEmpty()) {
//             env.pop();
//         } else {
//             System.out.println("Warning: Attempted to pop from an empty environment stack.");
//         }
//     }

//     public void addVar(String name, MyType type) { // 🍅 🍅 🍅 🍅 🍅: later, this will have to handle shadowing etc.
//         if (env.isEmpty()) {
//             pushScope();
//         }
//         env.peek().put(name, type); // add new var to the top scope
//     }

//     public MyType lookup(String name) {
//         for (int i = env.size() - 1; i >= 0; i--) {
//             if (env.get(i).containsKey(name)) {
//                 return env.get(i).get(name);
//             }
//         }
//         return null; // not found
//     }

//     public boolean containsInCurrentScope(String name) {
//         if (!env.isEmpty()) {
//             return env.peek().containsKey(name);
//         }
//         return false;
//     }

//     public void printSymbolTable() {
//         System.out.println("----- Symbol Table -----");
//         for (int i = env.size() - 1; i >= 0; i--) {
//             System.out.println("Scope Level " + i + ":");
//             for (Map.Entry<String, MyType> entry : env.get(i).entrySet()) {
//                 System.out.println("  " + entry.getKey() + " : " + entry.getValue());
//             }
//         }
//         System.out.println("------------------------");
//     }

//     @Override
//     public String toString() {
//         StringBuilder sb = new StringBuilder();
//         sb.append("----- Symbol Table -----\n");
//         for (int i = env.size() - 1; i >= 0; i--) {
//             sb.append("Scope Level ").append(i).append(":\n");
//             for (Map.Entry<String, MyType> entry : env.get(i).entrySet()) {
//                 sb.append("  ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
//             }
//         }
//         sb.append("------------------------\n");
//         return sb.toString();
//     }
// }