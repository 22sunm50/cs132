import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import IR.token.Identifier;

public class MethodInfo {
    MyType return_type;
    List<MyType> args_type_list;
    HashMap<String, MyType> vars_map;
    HashMap<String, MyType> args_map;
    ArrayList<Identifier> args_id_list;

    String[] reserved_names = {"a2", "a3", "a4", "a5", "a6", "a7", 
                                "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
                                "t0", "t1", "t3", "t4", "t5"};

    public String sanitizeName(String name) {
        for (String reserved : reserved_names) {
            if (reserved.equals(name)) {
                System.err.println("🧼 Sanitizing: " + name);
                return "Thaddy_" + name;
            }
        }
        return name;
    }

    public MethodInfo(MyType return_type) {
        this.return_type = return_type;
        this.args_type_list = new ArrayList<>();
        this.vars_map = new HashMap<>(); // contain vars & args
        this.args_map = new HashMap<>(); // just args
        this.args_id_list = new ArrayList<>();
    }

    // copy constructor (deep copy)
    public MethodInfo(MethodInfo other) {
        this.return_type = other.return_type; // assuming immutable or fine to share
        this.args_map = new HashMap<>(other.args_map); // shallow copy, or deep copy if needed
        this.vars_map = new HashMap<>(other.vars_map); // shallow copy, or deep copy if needed
        this.args_type_list = new ArrayList<>(other.args_type_list); // copy list
    }

    public MyType getReturnType() {
        return return_type;
    }

    public List<MyType> getArgsTypeList() {
        return args_type_list;
    }

    // Returns an ArrayList of IR Identifiers for the names in args_map
    public ArrayList<Identifier> getArgsIDList() {
        return args_id_list;
    }

    // add arg w name to both arg map and var map & check uniqueness 
    public void addArg(String argName, MyType argType) {
        argName = sanitizeName(argName);
        if (args_map.containsKey(argName)) {
            System.err.println("🚨 Argument already exists: " + argName);
        }
        if (vars_map.containsKey(argName)) {
            System.err.println("🚨 Argument name conflicts with a variable name: " + argName);
        }
        args_type_list.add(argType);
        args_map.put(argName, argType);
        args_id_list.add(new Identifier(argName));
    }

    // add var & check uniqueness
    public void addVar(String varName, MyType varType) {
        varName = sanitizeName(varName);
        if (vars_map.containsKey(varName)) {
            System.err.println("🚨 Variable already exists: " + varName);
        }
        if (args_map.containsKey(varName)) {
            System.err.println("🚨 Variable name conflicts with an argument name: " + varName);
        }
        vars_map.put(varName, varType);
    }

    // get number of arguments
    public int getArgCount() {
        return args_type_list.size();
    }

    // check if a specific name exists as either an argument or a variable
    public boolean hasVar(String name) {
        return vars_map.containsKey(name);
    }

    public boolean hasArg(String name) {
        return args_map.containsKey(name);
    }

    // Returns the type of a variable (or argument) given its name, or null if not found
    public MyType getVarType(String varName) {
        varName = sanitizeName(varName);
        if (vars_map.get(varName) == null){
            System.err.println("🚨: the method variable does not exist: " + varName);
        }
        return vars_map.get(varName);
    }

    public MyType getArgType(String varName) {
        varName = sanitizeName(varName);
        if (args_map.get(varName) == null){
            System.err.println("🚨: the method arg does not exist: " + varName);
        }
        return args_map.get(varName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Return Type: ").append(return_type.toString()).append(", Args: ");
        for (MyType arg : args_type_list) {
            sb.append(arg.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}