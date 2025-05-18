class Main {
	public static void main(String[] a){
        A aa;
        aa = new A();
        System.out.println(aa.setA());
		System.out.println(!(aa.runA()));
	}
}

class A {
    boolean bool_A;
    public boolean runA(){
        return bool_A;
    }
    public int setA(){
        bool_A = true;
        return 888;
    }
}