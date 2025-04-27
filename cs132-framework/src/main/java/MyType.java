public class MyType {
    public enum BaseType {
        BOOLEAN,
        INT,
        INT_ARRAY,
        ID,
        CLASS
    }

    private BaseType baseType;
    private String className; // only used if baseType == CLASS

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

    // change baseType
    public void changeBaseType(BaseType newBaseType) {
        this.baseType = newBaseType;
        // reset className to NULL if baseType is not CLASS or ID
        if (newBaseType != BaseType.CLASS && newBaseType != BaseType.ID) {
            this.className = null;
        }
    }

    // change baseType AND classname
    public void changeBaseType(BaseType newBaseType, String newClassName) {
        this.baseType = newBaseType;
        this.className = newClassName;
    }

    public boolean equals(MyType other) {
        if (this.baseType != other.baseType) return false;
        if (this.baseType == BaseType.ID) {
            return this.className != null && this.className.equals(other.className);
        }

        else if (this.baseType == BaseType.CLASS) {
            return this.className != null && this.className.equals(other.className);
        }
        return true;
    }

    @Override
    public String toString() {
        if (baseType == BaseType.ID) {
            return "ID : " + className;
        }

        // else if (baseType == BaseType.CLASS) {
        //     return "CLASS : " + className;
        // }
        return baseType.toString().toLowerCase();
    }
}
