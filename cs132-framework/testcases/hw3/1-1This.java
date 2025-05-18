class Main {
	public static void main(String[] a){
        B bb;
        bb = new B();
        System.out.println(bb.boom());
	}
}

class A {
    int intA;
    public int runThis(B boo){
        // System.out.println(boo.printB());
        return 101;
    }
}

class B {
    A aa;
    public int boom(){
        aa = new A();
        System.out.println(aa.runThis(this));
        return 222;
    }

    public int printB(){
        System.out.println(aa);
        return 777;
    }
}