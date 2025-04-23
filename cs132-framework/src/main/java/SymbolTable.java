import java.util.Stack;
import java.util.HashMap;

public class SymbolTable {
    Stack < HashMap < String, Type > > env;

    public void pushScope() // 🍅 CHANGE LATER
    {
        env.push(new HashMap<String, Type>());
    }

    public void addVar(String name, Type type)
    {
        if (env.isEmpty())
        {
            pushScope();
        }
        env.peek().put(name, type);
    }
}