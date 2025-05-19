class Main {
    public static void main(String[] args) {
        A a;
        int result;
        boolean rt;
        Element e1;
        Element e2;
        Element e3;
        Element e4;
        Element e5;
        
        e1 = new Element();
        rt = e1.Init(10, 100, true);
        e2 = new Element();
        rt = e2.Init(20, 200, false);
        e3 = new Element();
        rt = e3.Init(30, 300, true);
        e4 = new Element();
        rt = e4.Init(40, 400, false);
        e5 = new Element();
        rt=e5.Init(50, 500, true);
        
        a = new A();
        
        result = a.calculate(e1, e2, e3, e4, e5);
        System.out.println(result);
    }
}

class Element {
    int value;
    int id;
    boolean flag;

    public boolean Init(int v, int i, boolean f) {
        value = v;
        id = i;
        flag = f;
        return true;
    }

    public int GetValue() {
        return value;
    }

    public int GetID() {
        return id;
    }

    public boolean GetFlag() {
        return flag;
    }
}

class A {
    int x;
    int y;
    int z;
    int w;
    int v;

    public int calculate(Element e1, Element e2, Element e3, Element e4, Element e5) {
        x = ((((e1.GetValue()) + (e2.GetValue())) + ((e3.GetValue()) + (e4.GetValue()))) + (e5.GetValue())); // 150
        System.out.println((e1.GetID()));
        System.out.println((e2.GetID()));
        System.out.println((e3.GetID()));
        System.out.println((e4.GetID()));
        System.out.println((e5.GetID()));
        y = ((((e1.GetID()) + (e2.GetID())) + ((e3.GetID()) + (e4.GetID()))) + ((e5.GetID()))); // 1500
        z = 0;
        if ((e1.GetFlag())) {z = z + 1;} // true: z = 1
        else{

        }
        if ((e2.GetFlag())) z = z + 1; // false: z = 1
        else{

        }
        if ((e3.GetFlag())) z = z + 1; // true: z = 2
        else{

        }
        if ((e4.GetFlag())) z = z + 1; // false: z = 2
        else{

        }
        if ((e5.GetFlag())) z = z + 1; // true: z = 3
        else{

        }

        System.out.println(x);  // should be 150
        System.out.println(y);  // should be 1500
        System.out.println(z);  // should be 3
        
        w = x * y;      // 150 * 1500 = 225000
        v = w + z;      // 225000 + 3 = 225003
        return ((x + y) + (z + w)) + v; // 1650 + 225003 + 225003 = 451656
    }
}