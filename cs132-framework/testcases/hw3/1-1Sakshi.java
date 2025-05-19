class LinearConGen {
    public static void main(String[] args) {
      Generator gen;
      Matrix_5xn mtx;
      BinHistogram hist_gen;
      int[] hist;
      int dummy_int;
      int i;
      int r;
      int c;
      int rows;
      int cols;
      int bins;
  
      gen = new Generator();
      mtx = new Matrix_5xn();
      // set up linear congruential generator
      dummy_int = gen.initialize(10, 15625, 15586, 7981);
  
      cols = 20;
      dummy_int = mtx.initialize(cols);
      rows = mtx.row_size();
  
      r = 0;
      while (r < rows) {
        c = 0;
        while (c < cols) {
          System.out.println(mtx.set(r, c, (gen.advance())));
          c = c + 1;
        }
        r = r + 1;
      }
      
      bins = 6;
      hist_gen = new BinHistogram();
      System.out.println(100000000);
  
      dummy_int = hist_gen.initialize(bins);
      hist = hist_gen.get_bins(mtx);
  
      i = 0;
      //while (i < bins) {
        //System.out.println(hist[i]);
        //i = i + 1;
      //}
    }
  }
  
  class BinHistogram {
    int bin_count;
  
    public int initialize(int size) {
      bin_count = size;
      return size;
    }
  
    public int[] get_bins(Matrix_5xn matrix) {
      int i;
      int s;
      int c;
      int r;
      int cols;
      int rows;
      int value;
      int[] bins;
      int[] histogram;
      int[] range;
      int min;
      int max;
      int step;
      int b_max;
      b_max = bin_count;
      bins = new int[b_max];
      histogram = new int[b_max];
      cols = matrix.col_size();
      rows = matrix.row_size();
  
      range = this.find_range(matrix);
      min = range[0];
      max = range[1];
      step = this.div((max - min), b_max);
      System.out.println(min);
      System.out.println(max);
  
      i = 0;
      s = min;
      while (i < b_max) {
        bins[i] = s;
        i = i + 1;
        s = s + step;
      }
  
      r = 0;
      while (r < rows) {
        c = 0;
        while (c < cols) {
          i = 0;
          while (i < b_max) {
            value = matrix.get(r, c);
            if (value < (bins[i])) {
              histogram[i] = (histogram[i]) + 1;
            } else {
              // fall-thru
            }
            i = i + 1;
          }
          c = c + 1;
        }
        r = r + 1;
      }
  
      return histogram;
    }
  
    public int[] find_range(Matrix_5xn matrix) {
      int c;
      int r;
      int cols;
      int rows;
      int value;
      int min;
      int max;
      int[] min_max;
      min_max = new int[2];
      cols = matrix.col_size();
      rows = matrix.row_size();
      min = matrix.get(0, 0);
      max = min;
  
      r = 0;
      while (r < rows) {
        c = 0;
        while (c < cols) {
          value = matrix.get(r, c);
          if (value < min) {
            min = value;
          } else {
            // fall-thru
          }
          if (max < value) {
            max = value;
          } else {
            // fall-thru
          }
          c = c + 1;
        }
        r = r + 1;
      }
  
      min_max[0] = min;
      min_max[1] = max;
  
      return min_max;
    }
  
    // only works with positive numbers
    public int div(int a, int b) {
      int quotient;
      quotient = 0;
      while (!(a < b)) {
        a = a - b;
        quotient = quotient + 1;
      }
  
      return quotient;
    }
  }
  
  class Generator {
    int modulus;
    int multiplier;
    int increment;
    int state; // or seed
  
    public int initialize(int x, int m, int a, int c) {
      state = x;
      modulus = m;
      multiplier = a;
      increment = c;
      return state;
    }
  
    public int set_seed(int seed) {
      int prev;
      prev = state;
      state = seed;
      return prev;
    }
  
    public int advance() {
      state = this.mod((state * multiplier) + increment, modulus);
      return state;
    }
  
    public int mod(int a, int b) {
      while (!(a < b)) {
        a = a - b;
      }
      return a;
    }
  }
  
  class Matrix_5xn {
    int[] row0;
    int[] row1;
    int[] row2;
    int[] row3;
    int[] row4;
    int columns;
  
    public int initialize(int cols) {
      columns = cols;
      row0 = new int[cols];
      row1 = new int[cols];
      row2 = new int[cols];
      row3 = new int[cols];
      row4 = new int[cols];
      return cols;
    }
  
    public int set(int r, int c, int value) {
      int[] dummy;
      boolean searching_row;
      dummy = new int[1];
      searching_row = true;
  
      if ((c < columns) && !(c < 0)) {
        if (searching_row && (this.equalP(r, 0))) {
          searching_row = false;
          row0[c] = value;
        } else {
          // fall-thru
        }
        if (searching_row && (this.equalP(r, 1))) {
          searching_row = false;
          row1[c] = value;
        } else {
          // fall-thru
        }
        if (searching_row && (this.equalP(r, 2))) {
          searching_row = false;
          row2[c] = value;
        } else {
          // fall-thru
        }
        if (searching_row && (this.equalP(r, 3))) {
          searching_row = false;
          row3[c] = value;
        } else {
          // fall-thru
        }
        if (searching_row && (this.equalP(r, 4))) {
          searching_row = false;
          row4[c] = value;
        } else {
          // throw exception
          // fall-thru
        }
        if (searching_row) {
          dummy[1] = 0;
        } else {
          // fall-thru
        }
      } else {
        // I guess makeshift exception?
        dummy[1] = 0;
      }
  
      return value;
    }
  
    public int get(int r, int c) {
      int value;
      int[] dummy;
      boolean searching_row;
      dummy = new int[1];
      searching_row = true;
      value = (0 - 1);
  
      if ((c < columns) && !(c < 0)) {
        if (searching_row && (this.equal(r, 0))) {
          searching_row = false;
          value = row0[c];
        } else {
          // fall-thru
        }
        if (searching_row && (this.equal(r, 1))) {
          searching_row = false;
          value = row1[c];
        } else {
          // fall-thru
        }
        if (searching_row && (this.equal(r, 2))) {
          searching_row = false;
          value = row2[c];
        } else {
          // fall-thru
        }
        if (searching_row && (this.equal(r, 3))) {
          searching_row = false;
          value = row3[c];
        } else {
          // fall-thru
        }
        if (searching_row && (this.equal(r, 4))) {
          searching_row = false;
          value = row4[c];
        } else {
          // throw exception
          // fall-thru
        }
        if (searching_row) {
          dummy[1] = 0;
        } else {
          // fall-thru
        }
      } else {
        // I guess makeshift exception?
        dummy[1] = 0;
      }
  
      return value;
    }
  
    public int col_size() {
      return columns;
    }
  
    public int row_size() {
      return 5;
    }
  
    public boolean equalP(int a, int b) {
      boolean res;
      System.out.println(a - b);
  
      if (a < b) {
        res = false;
      } else {
        if (b < a) {
          res = false;
        } else {
          res = true;
        }
      }
      
      return res;
    }
  
    // same as above but silent
    public boolean equal(int a, int b) {
      boolean res;
  
      if (a < b) {
        res = false;
      } else {
        if (b < a) {
          res = false;
        } else {
          res = true;
        }
      }
  
      return res;
    }
  }