import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    MyType return_type;
    List<MyType> args_type_list;

    public MethodInfo(MyType return_type) {
        this.return_type = return_type;
        this.args_type_list = new ArrayList<>();
    } 

    public MyType getReturnType() {
        return return_type;
    }

    public List<MyType> getArgsTypeList() {
        return args_type_list;
    }

    public void addArg(MyType argType) {
        args_type_list.add(argType);
    }

    // get number of arguments
    public int getArgCount() {
        return args_type_list.size();
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