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

    public int foo(int arg1, int arg2) {
        int res;
        res = arg1 + arg2;
        return res;
    }

    public int bar(int arg1, int arg2) {
        a = arg1;
        b = arg2;
        return 0;
    }

    public int baz() {
        return a + b;
    }
}
