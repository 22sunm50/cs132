class Main {
    public static void main(String[] a) {
        int x;
    }
}


class B extends A {
    boolean b;
}

class C extends B {
    boolean c;
    public boolean i_miss_snoopy(boolean snoopy){
        int[] to_be_overidden;
        return c;
    }
}

class A {
    int b;
    int c;
    int to_be_overidden;
    public boolean i_miss_snoopy(boolean b){ 
        boolean to_be_overidden; // shadowing
        b = 2;
        return b;
    }
}
