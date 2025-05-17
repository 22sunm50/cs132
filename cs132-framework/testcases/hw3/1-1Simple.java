class Main {
	public static void main(String[] a){
		A aa;
		aa = new A();
		System.out.println(!aa.boolRun());
	}
}

class A {
	public boolean boolRun(){
		return true;
	}
}