class Main {
	public static void main(String[] a){
		System.out.println(33);
	}
}

class A {
	public int runA() {
		System.out.println(42);
		return 11;
	}
}

class B extends A{
	public int runB(){
		return 22;
	}
}