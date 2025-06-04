import java.util.HashMap;

public class ActivationRecord {
    HashMap<String, Integer> vars_map;
    HashMap<String, Integer> params_map;
    int next_local_offset = -12; // start below return address (-4) and old FP (-8)
    int param_offset = 0;

    public ActivationRecord() {
        vars_map = new HashMap<>();
        params_map = new HashMap<>();
    }

    public void addParam(String name) {
        if (!params_map.containsKey(name)) {
            params_map.put(name, param_offset);
            param_offset += 4;
        }
    }

    public void addLocal(String name) {
        if (!vars_map.containsKey(name) && !params_map.containsKey(name)) {
            vars_map.put(name, next_local_offset);
            next_local_offset -= 4;
        }
    }

    public Integer getOffset(String name) {
        if (vars_map.containsKey(name)) return vars_map.get(name);
        if (params_map.containsKey(name)) return params_map.get(name);
        throw new RuntimeException("🚨 Identifier " + name + " not found in activation record");
    }

    public Integer totalFrameSize() {
        return (vars_map.size() + 2) * 4;
    }

    public Integer getParamSize() {
        return params_map.size();
    }
}

