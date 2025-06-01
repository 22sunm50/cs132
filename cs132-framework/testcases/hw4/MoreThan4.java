class MoreThan4{
    public static void main(String[] a){
        System.out.println(new MT4().Start(1,2,3,4,5,6));
    }
}

class MT4 {
    public int Start(int p1, int p2, int p3 , int p4, int p5, int p6){
	int aux ;
        System.out.println(p1); // 1
        System.out.println(p2); // 2
        System.out.println(p3); // 3
        System.out.println(p4); // 4
        System.out.println(p5); // 5
        System.out.println(p6); // 6
	aux = this.Change(p6,p5,p4,p3,p2,p1);
	return aux ;
    }

    public int Change(int p1, int p2, int p3 , int p4, int p5, int p6){
        System.out.println(p1); // 6
        System.out.println(p2); // 5
        System.out.println(p3); // 4
        System.out.println(p4); // 3
        System.out.println(p5); // 2
        System.out.println(p6); // 1
	return 0 ;
    }
}
