class Main {
    public static void main(String[] a) {
        A a;
        B b;
        C c;
        c = new A();
    }
}

class A {
    int int_from_A;
    boolean bool_from_A;
    public boolean i_miss_snoopy(boolean b, int bob){ 
        int func_in_A;
        int[] int_from_A;
        // bool_from_A
        return bool_from_A;
    }
}

class B extends A {
    int[] int_arr_from_B;
    // int int_from_A;
    // boolean bool_from_A;
}

class C extends B {
    boolean bool_from_C;
    int bool_from_A;
    // int int_from_A;
    // int[] int_arr_from_B;
    public boolean i_miss_snoopy(boolean snoopy, int num){
        // int int_from_A;
        // boolean bool_from_C;
        // int bool_from_A;
        boolean int_arr_from_B;
        return int_arr_from_B;
    }
}
