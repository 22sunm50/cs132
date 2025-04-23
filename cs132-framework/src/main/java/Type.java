// public class Type {
//     private final String type;
//     public static final Type INT = new Type ("int");
//     public static final Type BOOLEAN = new Type ("boolean");
//     public static final Type INT_ARRAY = new Type ("int[]");
//     public static final Type CLASS = new Type ("class");

//     public Type (String type) {
//         this.type = type;
//     }
// }

public class Type {
    public enum BaseType {
        BOOLEAN,
        INT,
        INT_ARRAY,
        CLASS
    }

    private final BaseType baseType;
    private final String className; // only used if baseType == CLASS

    public Type(BaseType baseType) {
        this(baseType, null);
    }

    public Type(BaseType baseType, String className) {
        this.baseType = baseType;
        this.className = className;
    }

    public BaseType getBaseType() {
        return baseType;
    }

    public String getClassName() {
        return className;
    }

    public boolean equals(Type other) {
        if (this.baseType != other.baseType) return false;
        if (this.baseType == BaseType.CLASS) {
            return this.className != null && this.className.equals(other.className);
        }
        return true;
    }

    @Override
    public String toString() {
        if (baseType == BaseType.CLASS) {
            return "class " + className;
        }
        return baseType.toString().toLowerCase();
    }
}
