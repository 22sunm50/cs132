class Main {
	public static void main(String[] a){
		int a1;
		int b;
		int c;
		int d;
		int e;
		int f;
		int g;
		int h; 
		int i;
		A aa;
		aa = new A();
		a1 = 1;
		b = 2;
		c = 3;
		d = 4;
		e = 5;
		f = 6;
		g = 7;
		h = 8;
		i = 9;
		System.out.println( (aa.runA(a1, b, c, d, e, f, g, h, i)));
	}
}

class A {
	boolean end;
	public int runA(int a, int b, int c, int d, int e, int f, int g, int h, int i) {
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		System.out.println(e);
		System.out.println(f);
		System.out.println(g);
		System.out.println(h);
		System.out.println(i);
		return 111;
	}
}