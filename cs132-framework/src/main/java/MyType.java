public class MyType {
    public enum BaseType {
        BOOLEAN,
        INT,
        INT_ARRAY,
        ID,
        CLASS
    }

    private final BaseType baseType;
    private final String className; // only used if baseType == CLASS

    public MyType(BaseType baseType) {
        this(baseType, null);
    }

    public MyType(BaseType baseType, String className) {
        this.baseType = baseType;
        this.className = className;
    }

    public BaseType getBaseType() {
        return baseType;
    }

    public String getClassName() {
        return className;
    }

    public boolean isOfType(BaseType type) {
        return this.baseType == type;
    }

    public boolean equals(MyType other) {
        if (this.baseType != other.baseType) return false;
        if (this.baseType == BaseType.ID) {
            return this.className != null && this.className.equals(other.className);
        }
        return true;
    }

    @Override
    public String toString() {
        if (baseType == BaseType.ID) {
            return "ID : " + className;
        }
        return baseType.toString().toLowerCase();
    }
}
