class Main {
	public static void main(String[] a){
		A aa;
		aa = new A();
		System.out.println((aa.retFalse()) && (aa.retTrue()));
	}
}

class A {
	public boolean retTrue(){
		System.out.println(11111);
		return true;
	}

	public boolean retFalse(){
		System.out.println(22222);
		return false;
	}
}