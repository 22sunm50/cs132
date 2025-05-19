class Main {
	public static void main(String[] a){
		A aa;
		aa = new A();
		System.out.println( (aa.retA()).foo() );
	}
}

class A {
    int field;
    public A retA(){
        A local;
        local = new A();
        field = 8;
        return this;
    }

    public int foo(){
        return field;
    }
}