package Sudoku;

public class SudokuBoard {

    public static int EMPTY_ENTRY = 0;

    public class SudokuException extends Exception {
        public SudokuException(String message) {
            super(message);
        }
    }

    /* Stores the 9x9 grid of numbers. -1 represents an empty entry.  */
    protected int[][] entries;

    /* Stores the number of entries filled */
    protected int nEntriesFilled;

    /* options[i][j] is the set of numbers that might fill entries[i][j] */
    protected IntSet[][] options;

    /* A submatrix is one of the nine 3x3 grids on the board that must contain the numbers 1-9.
     * Submatrices are indexed as follows:
     *   0 1 2
     *   3 4 5
     *   6 7 8
     * So `submatrixOptions[0]` gives the set of remaining numbers that need to be placed in the top-left submatrix.
     */
    protected IntSet[] submatrixOptions;

    /* rowOptions[i] gives the set of remaining numbers that can be placed in row i */
    protected IntSet[] rowOptions;

    /* colOptions[j] gives the set of remaining numbers that can be placed in column j */
    protected IntSet[] colOptions;

    protected void init() {
        /* Java arrays are guaranteed to have elements initialized to the default value (zero for ints).  */
        this.entries = new int[9][9];
        this.nEntriesFilled = 0;

        /* Java is a bit finicky about arrays of generic things.
         * We need casts, as seen here: http://stackoverflow.com/a/217093
         */
        this.options = new IntSet[9][9];
        this.submatrixOptions = new IntSet[9];
        this.colOptions = new IntSet[9];
        this.rowOptions = new IntSet[9];

        for (int i = 0; i < 9; ++i) {
            this.rowOptions[i] = new IntSet();
            this.colOptions[i] = new IntSet();
            this.submatrixOptions[i] = new IntSet();
            for (int j = 0; j < 9; ++j) {
                this.options[i][j] = new IntSet();
            }
        }
    }

    public SudokuBoard() {
        this.init();
        assert(this.isValid());
        this.updateOptions();
        this.updateComponentOptions();
    }

    public SudokuBoard(int[][] array) {
        this.init();

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                // anything not in the range [1, 9] is assumed to be a blank space
                int n = (i >= array.length || j >= array[i].length) ? -1 : array[i][j];
                if (1 <= array[i][j] && array[i][j] <= 9) {
                    this.entries[i][j] = array[i][j];
                    this.nEntriesFilled++;
                }
            }
        }

        this.updateOptions();
        this.updateComponentOptions();
    }

    public SudokuBoard(String s) throws SudokuException {
        this.init();

        if (s.length() < 81)
            throw new SudokuException("Not enough characters in string to construct a board.");

        for (int i = 0; i < Math.min(81, s.length()); ++i) {
            int c = s.charAt(i);
            if ('1' <= c && c <= '9') {
                this.entries[i / 9][i % 9] = c - '0';
                this.nEntriesFilled++;
            }
        }

        this.updateOptions();
        this.updateComponentOptions();
    }

    protected SudokuBoard(SudokuBoard other)  {
        this.copyFrom(other);
    }

    public void copyFrom(SudokuBoard other) {
        this.init();
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                // anything not in the range [1, 9] is assumed to be a blank space
                int n = other.getEntry(i, j);
                if (1 <= n && n <= 9) {
                    this.entries[i][j] = n;
                    this.nEntriesFilled++;
                } else {
                    this.entries[i][j] = -1;
                }
            }
        }
        this.updateOptions();
        this.updateComponentOptions();
    }

    public int getEntry(int i, int j) {
        return this.entries[i][j];
    }

    /** Set the entry at (i, j) to n only if the value has not been set **/
    public void setEntry(int i, int j, int n) {
        if (this.entries[i][j] > 0)
            return;

        this.entries[i][j] = n;
//        this.options[i][j].clear();
        this.nEntriesFilled++;

        this.rowOptions[i].remove(n);
        this.colOptions[j].remove(n);
        this.submatrixOptions[toSubmatrixIndex(i, j)].remove(n);

        this.updateOptions();
    }

    /** Update the `options` array of numbers that can go in each space **/
    protected void updateOptions() {
        for (int i = 0; i < 9 ; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.options[i][j].clear();
                // If the space is unfilled, check the row, col, and submatrix
                // to see which numbers can be placed there
                if (this.entries[i][j] < 1) {
                    for (int n = 1; n <= 9; ++n) {
                        if (this.countInCol(j, n) == 0
                                && this.countInRow(i, n) == 0
                                && this.countInSubmatrix(toSubmatrixIndex(i, j), n) == 0) {
                            this.options[i][j].add(n);
                        }
                    }
                }
            }
        }
    }

    /** Update the row, column, and submatrix options arrays **/
    protected void updateComponentOptions() {
        for (int n = 1; n <= 9; ++n) {
            for (int i = 0; i < 9; ++i) {
                if (this.countInRow(i, n) == 0)
                    this.rowOptions[i].add(n);
            }
            for (int j = 0; j < 9; ++j) {
                if (this.countInCol(j, n) == 0)
                    this.colOptions[j].add(n);
            }
            for (int k = 0; k < 9; ++k) {
                if (this.countInSubmatrix(k, n) == 0)
                    this.submatrixOptions[k].add(n);
            }
        }
    }

    /** Return true if all entries in the board have been filled **/
    public boolean isFilled() {
        return this.nEntriesFilled == 81;
    }

    /** Count the number of occurrences of n in the given submatrix **/
    protected int countInSubmatrix(int submatrix, int n) {
        int iStart = (submatrix / 3) * 3;   // row
        int jStart = (submatrix % 3) * 3;   // column
        int iEnd = iStart + 3;
        int jEnd = jStart + 3;
        int count = 0;
        for (int i = iStart; i < iEnd; ++i) {
            for (int j = jStart; j < jEnd; ++j) {
                if (this.entries[i][j] == n)
                    count++;
            }
        }
        return count;
    }

    /** Count the number of occurrences of n in the given row **/
    protected int countInRow(int row, int n) {
        int count = 0;
        for (int j = 0; j < 9; ++j) {
            if (this.entries[row][j] == n)
                count++;
        }
        return count;
    }

    /** Count the number of occurrences of n in the given column **/
    protected int countInCol(int col, int n) {
        int count = 0;
        for (int i = 0; i < 9; ++i) {
            if (this.entries[i][col] == n)
                count++;
        }
        return count;
    }

    public static int toSubmatrixIndex(int i, int j) {
        return (i / 3) * 3 + (j / 3);
    }

    public static int[] fromSubmatrixIndex(int k) {
        return new int[]{3 * (k / 3), 3 * (k % 3)};
    }

    /** Return false if a number is in any row, column, or submatrix more than once. **/
    public boolean isValid() {
        for (int n = 1; n <= 9; ++n) {
            for (int i = 0; i < 9; ++i)
                if (this.countInRow(i, n) > 1)
                    return false;
            for (int j = 0; j < 9; ++j)
                if (this.countInCol(j, n) > 1)
                    return false;
            for (int k = 0; k < 9; ++k)
                if (this.countInSubmatrix(k, n) > 1)
                    return false;
        }
        return true;
    }

    /** Return true if the current state of the board is a valid solution.
     * Specifically, this checks that there is exactly one occurrence of each
     * number in each row, in each column, and in each submatrix.
     */
    public boolean isSolution() {
        for (int i = 0; i < 9; ++i) {
            for (int n = 1; n <= 9; ++n) {
                if (this.countInCol(i, n) != 1)
                    return false;
                if (this.countInRow(i, n) != 1)
                    return false;
                if (this.countInSubmatrix(i, n) != 1)
                    return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.entries.length; ++i) {
            for (int j = 0; j < this.entries[i].length; ++j) {
                if (this.entries[i][j] < 1)
                    sb.append('.');
                else
                    sb.append(this.entries[i][j]);
            }
            if (i < this.entries.length - 1)
                sb.append('\n');
        }
        return sb.toString();
    }

    /** Same as toString(), but with no whitespace. Use for testing. **/
    public String rawString() {
        return this.toString().replace("\n", "");
    }

//    public void printData() {
//        System.out.println("-----------");
//        System.out.println("Filled squares: " + this.nEntriesFilled);
//
//        System.out.println("----Possibilities---");
//        for (int i = 0; i < this.options.length; i++) {
//            for (int j = 0; j < this.options[0].length; j++) {
//                if (this.options[i][j].size() == 0) {
//                    System.out.print("X");
//                } else {
//                    for (int n: this.options[i][j]) {
//                        System.out.print(n);
//                    }
//                }
//                System.out.print(" ");
//            }
//            System.out.println();
//        }
//
//        System.out.println("-----Submatrix Options-----");
//        for(int k = 0; k < this.submatrixOptions.length; k++){
//            System.out.print("Box " + k + ": ");
//            for (int n: this.submatrixOptions[k])
//                System.out.print(n);
//            System.out.println();
//        }
//
//        System.out.println("-----Row Options-----");
//        for(int i = 0; i < this.rowOptions.length; i++){
//            System.out.print("Row " + i + ": ");
//            for (int n: this.rowOptions[i])
//                System.out.print(n);
//            System.out.println();
//
//        }
//
//        System.out.println("-----Columns Options-----");
//        for(int j = 0; j < this.colOptions.length; j++){
//            System.out.print("Col " + j + ": ");
//            for (int n: this.colOptions[j])
//                System.out.print(n);
//            System.out.println();
//        }
//    }

}
