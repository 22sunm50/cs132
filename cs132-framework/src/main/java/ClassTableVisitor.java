import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

public class ClassTableVisitor extends GJDepthFirst < MyType, SymbolTable > {
    HashMap< String, HashMap < String,Boolean > > is_subtype = new HashMap< String, HashMap < String,Boolean > > ();

    String curr_class;
    String curr_method;

    // 🔄 🔄 🔄 🔄 🔄 🔄 🔄 CYCLE DETECTION 🔄 🔄 🔄 🔄 🔄 🔄 🔄
    // set is_subtype to the s_table is_subtype
    public void setSymbolTableSubtype(SymbolTable s_table) {
        s_table.setIsSubtypeMap(this.is_subtype);
    }

    // get subtype outside of this class
    public HashMap< String, HashMap < String,Boolean > > get_is_subtype() {
        return is_subtype;
    }

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

    // 🌴 🌴 🌴 🌴 🌴 🌴 🌴 INHERITANCE 🌴 🌴 🌴 🌴 🌴 🌴 🌴 🌴
    public void inheritFields(SymbolTable s_table) { // basically goes thru all ancestors and puts into curr class (starting at closest parent)
        for (String className : s_table.class_table.keySet()) {     // loop thru all classes
            ClassInfo classInfo = s_table.getClassInfo(className);  // get its ClassInfo
            String parent = classInfo.getParentClassName();         // get its parent name (if any)
    
            // recursively inherit fields
            while (parent != null) {                                        // while class has parents
                ClassInfo parentInfo = s_table.getClassInfo(parent);        // get parent's ClassInfo
                for (String parentField : parentInfo.fields_map.keySet()) { // for each field in parent
                    if (!classInfo.fields_map.containsKey(parentField)) {   // inherit if not overshadowed
                        MyType parentField_type = parentInfo.fields_map.get(parentField);
                        classInfo.addField(parentField, parentField_type);
                    }
                }
                parent = parentInfo.getParentClassName(); // move up to grandparents and repeat
            } 
        }
    }

    // compare 2 methods for same ret type & param types
    private boolean areMethodsEqual(MethodInfo m1, MethodInfo m2) {
        // Check return type
        if (!m1.getReturnType().equals(m2.getReturnType())) {
            return false;
        }
    
        // Check number of parameters
        if (m1.getArgCount() != m2.getArgCount()) {
            return false;
        }
    
        // Check parameter types
        for (int i = 0; i < m1.getArgCount(); i++) {
            if (!m1.getArgsTypeList().get(i).equals(m2.getArgsTypeList().get(i))) {
                return false;
            }
        }
    
        return true; // They are equal
    }

    // method inheritance & overriding
    public void inheritMethods(SymbolTable s_table) {
        for (String className : s_table.class_table.keySet()) {
            ClassInfo classInfo = s_table.getClassInfo(className);
            String parent = classInfo.getParentClassName();
    
            // recursively inherit methods
            while (parent != null) {
                ClassInfo parentInfo = s_table.getClassInfo(parent);
                for (String parentMethod : parentInfo.methods_map.keySet()) {
                    MethodInfo parentMethodInfo = parentInfo.getMethodInfo(parentMethod);
    
                    if (!classInfo.hasMethod(parentMethod)) { // inherit the method if not overridden
                        // 😱 😱 😱 😱 😱: make a copy to not link methodInfo objs
                        MethodInfo copy_parentMethodInfo = new MethodInfo(parentMethodInfo);
                        classInfo.methods_map.put(parentMethod, copy_parentMethodInfo);
                    } else {    // method exists in subclass -> check for valid overriding
                        MethodInfo childMethodInfo = classInfo.getMethodInfo(parentMethod);    
                        // valid override: do nothing, child already overrides it properly
                    }
                }
                parent = parentInfo.getParentClassName(); // move up the chain
            }
        }
    }

    // add class fields to method vars if not shadowed (CALL AFTER INHERITANCE)
    public void addFieldsToMethodVars(SymbolTable s_table) {
        for (String className : s_table.class_table.keySet()) {                 // loop thru all classes in the symbol table
            ClassInfo classInfo = s_table.getClassInfo(className);              // get classInfo of this class

            for (String methodName : classInfo.methods_map.keySet()) {          // loop thru all methods in the class
                MethodInfo methodInfo = classInfo.getMethodInfo(methodName);    // get methodInfo of this method

                for (String fieldName : classInfo.fields_map.keySet()) {        // loop thru all fields in this class
                    if (!methodInfo.vars_map.containsKey(fieldName)) {          // if fieldname not a method var yet
                        // add class field to method's vars_map if not shadowed
                        methodInfo.addVar(fieldName, classInfo.getFieldType(fieldName));
                    }
                }
            }
        }
    }

    // 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ VISIT FUNCTIONS 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️ 🏖️
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
        this_classinfo.setParentClassName(parent_name);
        s_table.addClass(class_name, this_classinfo);

        n.f5.accept(this, s_table); // var dec list
        n.f6.accept(this, s_table); // method dec list

        return new MyType(MyType.BaseType.CLASS, class_name);
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

        return new MyType(MyType.BaseType.CLASS, class_name);
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

        return new MyType(MyType.BaseType.CLASS, class_name);
    }

    // ALL POSSIBLE Type()
    @Override
    public MyType visit(ArrayType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT_ARRAY);
    }

    @Override
    public MyType visit(BooleanType n, SymbolTable s_table) {
        // String var_type = n.f0.toString(); // gives "boolean"
        return new MyType(MyType.BaseType.BOOLEAN);
    }

    @Override
    public MyType visit(IntegerType n, SymbolTable s_table) {
        return new MyType(MyType.BaseType.INT);
    }

    @Override
    public MyType visit(Identifier n, SymbolTable s_table) {
        String var_name = n.f0.toString();
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit(VarDeclaration n, SymbolTable s_table) { // 🍅 🍅 🍅 🍅 🍅: later, this will have to handle shadowing etc.
        String var_name = n.f1.f0.toString();
        MyType var_type = n.f0.f0.accept(this, s_table);

        // if type is a custom class, change from ID to CLASS
        if (var_type.isOfType(MyType.BaseType.ID)){
            String var_type_name = var_type.getClassName();
            var_type = new MyType(MyType.BaseType.CLASS, var_type_name);
        }
        // add as a class field
        if (curr_method == null){
            s_table.getClassInfo(curr_class).addField(var_name, var_type);
        }

        // add as a method local var
        else {
            s_table.getClassInfo(curr_class).getMethodInfo(curr_method).addVar(var_name, var_type);;
        }

        // 🍅 🍅 🍅 🍅 🍅: check if type ID, then handle it as a var and check symbol table
        return new MyType(MyType.BaseType.ID, var_name);
    }

    @Override
    public MyType visit(MethodDeclaration n, SymbolTable s_table) {
        String method_name = n.f2.f0.toString();
        MyType ret_type = n.f1.f0.accept(this, s_table);

        // ret type ID -> CLASS
        if (ret_type.isOfType(MyType.BaseType.ID)){
            ret_type.changeBaseType(MyType.BaseType.CLASS);
        }

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
    public MyType visit(FormalParameter n, SymbolTable s_table) {
        MyType param_type = n.f0.f0.accept(this, s_table);
        String param_name = n.f1.f0.toString();

        if (param_type.getBaseType() == (MyType.BaseType.ID)){
            param_type.changeBaseType(MyType.BaseType.CLASS);
        }

        s_table.getClassInfo(curr_class).getMethodInfo(curr_method).addArg(param_name, param_type);

        return param_type;
    }
}
