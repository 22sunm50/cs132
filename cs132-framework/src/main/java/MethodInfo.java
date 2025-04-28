import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodInfo {
    MyType return_type;
    List<MyType> args_type_list;
    HashMap<String, MyType> vars_map;
    HashMap<String, MyType> args_map;

    public MethodInfo(MyType return_type) {
        this.return_type = return_type;
        this.args_type_list = new ArrayList<>();
        this.vars_map = new HashMap<>(); // contain vars & args
        this.args_map = new HashMap<>(); // just args
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

    // add arg w name to both arg map and var map & check uniqueness 
    public void addArg(String argName, MyType argType) {
        if (args_map.containsKey(argName)) {
            System.err.println("🚨 Argument already exists: " + argName);
            printFailureAndExit();
        }
        if (vars_map.containsKey(argName)) {
            System.err.println("🚨 Argument name conflicts with a variable name: " + argName);
            printFailureAndExit();
        }
        args_type_list.add(argType);
        args_map.put(argName, argType);
        vars_map.put(argName, argType);
    }

    // add var & check uniqueness
    public void addVar(String varName, MyType varType) {
        if (vars_map.containsKey(varName)) {
            System.err.println("🚨 Variable already exists: " + varName);
            printFailureAndExit();
        }
        if (args_map.containsKey(varName)) {
            System.err.println("🚨 Variable name conflicts with an argument name: " + varName);
            printFailureAndExit();
        }
        vars_map.put(varName, varType);
    }

    // get number of arguments
    public int getArgCount() {
        return args_type_list.size();
    }

    // check if a specific name exists as either an argument or a variable
    public boolean hasVarsOrArgs(String name) {
        return vars_map.containsKey(name);
    }

    // Returns the type of a variable (or argument) given its name, or null if not found
    public MyType getVarOrArgType(String varName) {
        if (vars_map.get(varName) == null){
            System.err.println("🚨: the method variable does not exist: " + varName);
            printFailureAndExit();
        }
        return vars_map.get(varName);
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

    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }
}