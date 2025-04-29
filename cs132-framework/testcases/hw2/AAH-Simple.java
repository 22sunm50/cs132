class Main {
    public static void main(String[] a) {

    }
}

class Visitor {
    Tree l ;
    //Tree r ;

    public int visit(Tree n){
        int nti ;

        if (n.GetHas_Right()){
            r = n.GetRight() ; //TE
            nti = r.accept(this) ; }
        else nti = 0 ;

        return 0;
    }

}

class Tree {
    public boolean GetHas_Right(){
        return true;
    }
    public Tree GetRight(){
        return new Tree();
    }
    public int accept(Visitor v){
        return 1;
    }
}