class Main {
	public static void main(String[] a){
		int a;
		int b;
        A aa;
		aa = new A();
        a = 1;
        b = 2;
		System.out.println( (aa.runA(a, b)));
	}
}

class A {
	boolean end;
	public int runA(int a, int b) {
        int c;
		c = a + b;
		return (c);
	}
}