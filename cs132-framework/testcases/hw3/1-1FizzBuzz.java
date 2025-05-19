class Main {
    public static void main(String[] args) {
        System.out.println((new FizzBuzz()).run(10));
    }
}

class FizzBuzz {
    public int run(int n) {
        int i;
        int res;
        i = 0;
        res = 0;

        while (i < (n + 1)) {
            if (this.isDivisibleByThree(i)) {
                res = res + 1;
            } else {
                
            }

            if (this.isDvisibleByFive(i)) {
                res = res + 1;
            } else {
                
            }

            i = i + 1;
        }

        return res;
    }

    public boolean isLessThan(int a, int b) {
        System.out.println(a);
        return a < b;
    }

    
    public boolean isEqual(int a, int b) {
        return (!(this.isLessThan(a, b)) && !(this.isLessThan(b, a)));
    }

    public boolean isDivisibleByThree(int num) {
        boolean res;

        if (this.isEqual(num, 3)) {
            res = true;
        } else {
            if (this.isEqual(num, 6)) {
                res = true;
            } else {
                if (this.isEqual(num, 9)) {
                    res = true;
                }
                else {
                    res = false;
                }
            }
        }
    
        return res;
    }

    public boolean isDvisibleByFive(int num) {
        boolean res;

        if (!this.isEqual(num, 5) && !this.isEqual(num, 10)) {
            res = false;
        } else {
            res = true;
        }

        return res;
    }
}
