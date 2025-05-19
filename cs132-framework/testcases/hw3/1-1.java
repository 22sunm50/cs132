class Main {
	public static void main(String[] a){
		A aa;
		aa = new A();
		System.out.println( (aa.runA()));
	}
}

class A {
	boolean end;
	public int runA() {
		boolean var_end;
		end = true;
		var_end = end;
		if(!var_end){
			var_end = false;
		}else{
			var_end = true;
		}
		return 111;
	}
}