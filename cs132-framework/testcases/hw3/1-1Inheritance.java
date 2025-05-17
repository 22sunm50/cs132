class Main {
    public static void main(String[] args) {
        A a;
        B b;
        int _;
        a = new B();
        b = new B();
        _ = b.initB(1, 2);
        _ = b.initA(3, 4);
        _ = b.printA();
        System.out.println(100);
        _ = b.printB();
        System.out.println(100);
        _ = a.override();
    }
}

class A {
    int a;
    int ab;

    public int initA(int x, int y) {
        a = x;
        ab = y;
        System.out.println(11111111);
        return 0;
    }

    public int printA() {
        System.out.println(a);
        System.out.println(ab);
        return 0;
    }

    public int override() {
        System.out.println(11);
        return 0;
    }
}

class B extends A {
    int b;
    int ab;

    public int initB(int x, int y) {
        b = x;
        ab = y;
        System.out.println(22222222);
        return 0;
    }

    public int printB() {
        System.out.println(b);
        System.out.println(ab);
        return 0;
    }

    public int override() {
        System.out.println(22);
        return 0;
    }
}
