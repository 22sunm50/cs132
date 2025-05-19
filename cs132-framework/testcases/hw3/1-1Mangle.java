class Main {
	public static void main(String[] a){
		A a2;
		a2 = new A();
		System.out.println( (a2.runA()));
	}
}

class A {
    int aa;

    public int runA(){
        aa = 8;
        return aa;
    }
}