class Main {
    public static void main(String[] args) {
        A aa;
        aa = new A();
        System.out.println(aa.foo(1, 2));
        System.out.println(aa.bar(3, 4));
        System.out.println(aa.baz());
    }
}

class A {
    int a;
    int b;

    public int foo(int a1, int a2) {
        int res;
        res = a1 + a2;
        return res;
    }

    public int bar(int a1, int a2) {
        a = a1;
        b = a2;
        return 0;
    }

    public int baz() {
        return a + b;
    }
}
