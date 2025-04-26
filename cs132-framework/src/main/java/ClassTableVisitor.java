import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class ClassTableVisitor extends GJDepthFirst < MyType, SymbolTable > {
    HashMap< String, HashMap < String,Boolean > > is_subtype = new HashMap< String, HashMap < String,Boolean > > ();

    String curr_class;
    String curr_method;

    public void printFailureAndExit() { 
        System.out.println("Type error");
        System.exit(1);
    }

    // 🔄 🔄 🔄 🔄 🔄 🔄 🔄 CYCLE DETECTION 🔄 🔄 🔄 🔄 🔄 🔄 🔄
    // list of class names
    public List<String> getClassNames() {
        return new ArrayList<>(is_subtype.keySet());
    }

    // connect all the subtypes of subtypes etc.
    public void computeTransitiveSubtypes() {
        List<String> classNames = getClassNames();
        for (String k : classNames) {
            for (String i : classNames) {
                for (String j : classNames) {
                    if (isSubtype(i, k) && isSubtype(k, j)) {
                        is_subtype.get(i).put(j, true);
                    }
                }
            }
        }
    }

    // helper func to get if is subtype
    private boolean isSubtype(String a, String b) {
        return is_subtype.containsKey(a) && is_subtype.get(a).getOrDefault(b, false);
    }

    public void checkCycle() {
        List<String> classNames = getClassNames();
        for (String i : classNames) {
            for (String j : classNames) {
                if (!i.equals(j) && isSubtype(i, j) && isSubtype(j, i)) {
                    System.err.println("🚨 🚨 🚨 Cyclic subtyping detected between " + i + " and " + j);
                    printFailureAndExit();
                }
                else if (i.equals(j) && isSubtype(i, j) && isSubtype(j, i)) {
                    System.err.println("🚨 🚨 🚨 Subtype of itself: " + i + " and " + j);
                    printFailureAndExit();
                }
            }
        }
    }

    @Override
    public MyType visit(ClassExtendsDeclaration n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString();
        String parent_name = n.f3.f0.toString();

        is_subtype.putIfAbsent(class_name, new HashMap<>());
        is_subtype.putIfAbsent(parent_name, new HashMap<>());

        is_subtype.get(class_name).put(parent_name, true);

        // class stuff
        curr_class = class_name;
        curr_method = null;
        ClassInfo this_classinfo = new ClassInfo();
        s_table.addClass(class_name, this_classinfo);
        n.f5.accept(this, s_table); // var dec list
        n.f6.accept(this, s_table); // method dec list

        return new MyType(MyType.BaseType.ID, class_name);
    }

    @Override
    public MyType visit(ClassDeclaration n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString();

        // class stuff
        curr_class = class_name;
        curr_method = null;
        ClassInfo this_classinfo = new ClassInfo();
        s_table.addClass(class_name, this_classinfo);
        n.f3.accept(this, s_table); // var dec list
        n.f4.accept(this, s_table); // method dec list

        return new MyType(MyType.BaseType.ID, class_name);
    }

    @Override
    public MyType visit(MainClass n, SymbolTable s_table) {
        String class_name = n.f1.f0.toString(); // name = "Main"

        // class stuff
        curr_class = class_name;
        curr_method = null;
        ClassInfo this_classinfo = new ClassInfo();
        s_table.addClass(class_name, this_classinfo);
        n.f11.accept(this, s_table); // main arg? "String[] a" --> not sure if this is ever needed
        n.f14.accept(this, s_table); // var dec list
        n.f15.accept(this, s_table); // statement list

        return new MyType(MyType.BaseType.ID, class_name);
    }

    // ALL POSSIBLE Type()
    @Override
    public MyType visit (ArrayType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit (BooleanType n, SymbolTable s_table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit (IntegerType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit (Identifier n, SymbolTable s_table) {
        String var_name = n.f0.toString();
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit (VarDeclaration n, SymbolTable s_table) { // 🍅 🍅 🍅 🍅 🍅: later, this will have to handle shadowing etc.
        String var_name = n.f1.f0.toString();
        MyType var_type = n.f0.f0.accept(this, s_table);

        // add as a class field
        if (curr_method == null){
            s_table.getClassInfo(curr_class).addField(var_name, var_type);
        }

        // add as a method local var
        else {
            s_table.getClassInfo(curr_class).getMethodInfo(curr_method);
        }

        // 🍅 🍅 🍅 🍅 🍅: check if type ID, then handle it as a var and check symbol table
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit (MethodDeclaration n, SymbolTable s_table) {
        String method_name = n.f2.f0.toString();
        MyType ret_type = n.f1.f0.accept(this, s_table);

        System.out.println("🍇 🍇 🍇 🍇 🍇 : method name = " + method_name);
        System.out.println("🍇 🍇 🍇 🍇 🍇 : ret type = " + ret_type);

        MethodInfo this_methodinfo = new MethodInfo(ret_type);

        s_table.getClassInfo(curr_class).addMethod(method_name, this_methodinfo);

        curr_method = method_name; // set global method state
        n.f4.accept(this, s_table); // param list
        n.f7.accept(this, s_table); // var dec list
        n.f8.accept(this, s_table); // statements list
        n.f10.accept(this, s_table); // return expression

        return new MyType(MyType.BaseType.ID, method_name);
    }

    @Override
    public MyType visit (FormalParameter n, SymbolTable s_table) {
        MyType param_type = n.f0.f0.accept(this, s_table);
        String param_name = n.f1.f0.toString();

        s_table.getClassInfo(curr_class).getMethodInfo(curr_method).addArg(param_type);

        return param_type;
    }
}
