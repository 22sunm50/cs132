class Factorial{
    public static void main(String[] a){
        System.out.println(new Fac().ComputeFac(10, 20));
    }
}

class Fac {
    public int ComputeFac(int num, int bum){
        int num_aux ;
        if ((num + bum) < 35)
            num_aux = 101010;
        else
            num_aux = this.ComputeFac((num - 1), bum);
        return num_aux ;
    }
}
