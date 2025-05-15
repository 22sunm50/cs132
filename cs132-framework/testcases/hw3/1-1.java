class Main {
	public static void main(String[] a){
		a = new A();
		System.out.println(new A().runA(11, 22));
	}
}

class A {
	int a_A;
	public int runA(int sum, int bum) {
		System.out.println(42);
		return 111;
	}
}

class B extends A{
	int b_B;
	public int runB(){
		return 222;
	}
}