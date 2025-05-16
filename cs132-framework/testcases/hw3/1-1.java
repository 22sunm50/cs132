class Main {
	public static void main(String[] a){
		C ccc;
		ccc = new C();
		System.out.println(ccc.runA());
	}
}

class A {
	int a_A;
	public int runA() {
		return 111;
	}
}

class B extends A{
	int b_B;
	public int runB(){
		return 222;
	}
}

class C extends B{
	boolean a_A;
	int c_C;
	public int runA(){
		return 333;
	}
	public int runC(){
		return 555;
	}
}