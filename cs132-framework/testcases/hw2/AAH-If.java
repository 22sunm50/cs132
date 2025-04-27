class Main {
    public static void main(String[] a) {
        int num;
        boolean b;
        Dog snoopy;
        if (snoopy.hasOwner(snoopy.noArg())) {
            num = 1;
        }
        else {
            num = a;
        }

        while (snoopy.noArg()) {
            num = 1;
        }
    }
}

class Dog {
    int num;
    public boolean hasOwner(int num){
        return true;
    }

    public int noArg() {
        return 1;
    }
}