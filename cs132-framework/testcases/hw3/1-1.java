class Main {
	public static void main(String[] a){
		System.out.println(33);
	}
}

class A {
	int a_A;
	boolean boolinA;
	public int runA() {
		System.out.println(42);
		return 11;
	}
}

class B extends A{
	int sum_fieldB;
	public int runB(){
		return 22;
	}
}