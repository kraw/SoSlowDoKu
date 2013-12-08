package Sudoku;

import java.util.HashSet;

public class SudokuBoard {

    public class SudokuException extends Exception {
        public SudokuException(String message) {
            super(message);
        }
    }

    /** Stores the 9x9 grid of numbers **/
    protected int[][] spaces;

    /** Stores the number of spaces filled **/
    protected int spacesFilled;

    /** possibilities[i][j] = {3,6,7} means one of 3, 6, or 7 goes in space (i, j) **/
    protected HashSet<Integer>[][] possibilities;

    /** count[3 - 1] = 4 means 4 of 9 threes have been placed on the board **/
    protected int[] count;

    /** A submatrix is one of the nine 3x3 grids on the board that must contain the numbers 1-9.
     * Submatrices are indexed as follows:
     *   0 1 2
     *   3 4 5
     *   6 7 8
     * submatrixPossibilities[0] = {2, 5} means that 2 and 5 are the only remaining numbers that
     * could possibliy be placed in the top-left submatrix.
     */
    protected HashSet<Integer>[] submatrixPossibilities;

    /** rowPossibilities[7] = {1, 9} means 1 and 9 are the only remaining numbers that could
     * be placed in the row at index 7.
     */
    protected HashSet<Integer>[] rowPossibilities;

    /** colPossibilities[3] = {6, 8} means 6 and 8 are the only remaining numbers that could
     * be placed in the column at index 3.
     */
    protected HashSet<Integer>[] colPossibilities;

    protected void init() {
        /* Java arrays are guaranteed to have elements initialized to the default value (zero for ints).  */
        this.spaces = new int[9][9];
        this.spacesFilled = 0;
        this.count = new int[9];

        /* Java is a bit finicky about arrays of generic things.
         * We need casts, as seen here: http://stackoverflow.com/a/217093
         */
        this.possibilities = (HashSet<Integer>[][]) new HashSet[9][9];
        this.submatrixPossibilities = (HashSet<Integer>[]) new HashSet[9];
        this.colPossibilities = (HashSet<Integer>[]) new HashSet[9];
        this.rowPossibilities = (HashSet<Integer>[]) new HashSet[9];

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j)
                this.possibilities[i][j] = new HashSet<Integer>();
            this.rowPossibilities[i] = new HashSet<Integer>();
            this.colPossibilities[i] = new HashSet<Integer>();
            this.submatrixPossibilities[i] = new HashSet<Integer>();
        }
    }

    public SudokuBoard() {
        this.init();

        for (int i = 0; i < this.spaces.length; ++i)
            for (int j = 0; j < this.spaces[0].length; ++j)
                this.spaces[i][j] = -1;

        assert this.boardIsValid();
        this.updatePossibilites();
        this.updateComponentPossibilites();
    }

    public SudokuBoard(int[][] array) throws SudokuException {
        this.init();

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                // anything not in the range [1, 9] is assumed to be a blank space
                int n = (i >= array.length || j >= array[i].length) ? -1 : array[i][j];
                if (1 <= n && n <= 9) {
                    this.spaces[i][j] = n;
                    this.spacesFilled++;
                    this.count[n - 1]++;
                } else {
                    this.spaces[i][j] = -1;
                }
            }
        }

        if (!this.boardIsValid())
            throw new SudokuException("Invalid board state");
        this.updatePossibilites();
        this.updateComponentPossibilites();
    }

    public SudokuBoard(String s) throws SudokuException {
        this.init();

        if (s.length() < 81)
            throw new SudokuException("Not enough characters in String to construct a board.");

        for (int i = 0; i < Math.min(81, s.length()); ++i) {
            int c = s.charAt(i);
            if ('1' <= c && c <= '9') {
                int n = Integer.parseInt("" + (char) c);
                this.spaces[i / 9][i % 9] = n;
                this.spacesFilled++;
                this.count[n - 1]++;
            } else {
                this.spaces[i / 9][i % 9] = -1;
            }
        }

        if (!this.boardIsValid())
            throw new SudokuException("Invalid board state");
        this.updatePossibilites();
        this.updateComponentPossibilites();
    }

    protected SudokuBoard(SudokuBoard other)  {
        this.copyFrom(other);
//        this.init();
//        for (int i = 0; i < 9; ++i) {
//            for (int j = 0; j < 9; ++j) {
//                // anything not in the range [1, 9] is assumed to be a blank space
//                int n = other.getSpace(i, j);
//                if (1 <= n && n <= 9) {
//                    this.spaces[i][j] = n;
//                    this.spacesFilled++;
//                    this.count[n - 1]++;
//                } else {
//                    this.spaces[i][j] = -1;
//                }
//            }
//        }
//        this.updatePossibilites();
//        this.updateComponentPossibilites();
    }

    public void copyFrom(SudokuBoard other) {
        this.init();
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                // anything not in the range [1, 9] is assumed to be a blank space
                int n = other.getSpace(i, j);
                if (1 <= n && n <= 9) {
                    this.spaces[i][j] = n;
                    this.spacesFilled++;
                    this.count[n - 1]++;
                } else {
                    this.spaces[i][j] = -1;
                }
            }
        }
        this.updatePossibilites();
        this.updateComponentPossibilites();
    }

    public int getSpace(int i, int j) {
        return this.spaces[i][j];
    }

    /** Set the space at (i, j) to n and update other data structures accordingly **/
    public void setSpace(int i, int j, int n) {
        if (this.spaces[i][j] == n)
            return;

//        System.out.println(String.format("Setting spaces[%d][%d] = %d", i, j, n));

        this.spaces[i][j] = n;
        this.possibilities[i][j].clear();
        this.count[n - 1]++;
        this.spacesFilled++;

        this.rowPossibilities[i].remove(n);
        this.colPossibilities[j].remove(n);
        this.submatrixPossibilities[this.getSubmatrixIndex(i, j)].remove(n);

        this.updatePossibilites();
    }

    /** Update the `possibilities` array of numbers that can go in each space **/
    protected void updatePossibilites() {
        for (int i = 0; i < 9 ; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.possibilities[i][j].clear();
                // If the space is unfilled, check the row, col, and submatrix
                // to see which numbers can be placed there
                if (this.spaces[i][j] < 0) {
                    int submatrixIndex = this.getSubmatrixIndex(i, j);
                    for (int n = 1; n <= 9; ++n) {
                        if (this.countInCol(j, n) == 0
                                && this.countInRow(i, n) == 0
                                && this.countInSubmatrix(submatrixIndex, n) == 0) {
                            this.possibilities[i][j].add(n);
                        }
                    }
                }
            }
        }
    }

    /** Update the row, column, and submatrix possibilities arrays **/
    protected void updateComponentPossibilites() {
        for (int n = 1; n <= 9; ++n) {
            for (int i = 0; i < 9; ++i)
                if (this.countInRow(i, n) == 0)
                    this.rowPossibilities[i].add(n);
            for (int j = 0; j < 9; ++j)
                if (this.countInCol(j, n) == 0)
                    this.colPossibilities[j].add(n);
            for (int k = 0; k < 9; ++k)
                if (this.countInSubmatrix(k, n) == 0)
                    this.submatrixPossibilities[k].add(n);
        }
    }

    /** Return true if n has been placed in the board nine times **/
    public boolean finishedNumber(int n) {
        return (0 <= n-1 && n-1 < this.count.length) && count[n-1] == 9;
    }

    /** Return true if all spaces in the board have been filled **/
    public boolean isFilled() {
        return this.spacesFilled == 81;
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
                if (this.spaces[i][j] == n)
                    count++;
            }
        }
        return count;
    }

    /** Count the number of occurrences of n in the given row **/
    protected int countInRow(int row, int n) {
        int count = 0;
        for (int j = 0; j < 9; ++j)
            if (this.spaces[row][j] == n) count++;
        return count;
    }

    /** Count the number of occurrences of n in the given column **/
    protected int countInCol(int col, int n) {
        int count = 0;
        for (int i = 0; i < 9; ++i)
            if (this.spaces[i][col] == n) count++;
        return count;
    }

    protected int getSubmatrixIndex(int i, int j) {
        return (i / 3) * 3 + (j / 3);
    }

    /** Return false if a number is in any row|col|submatrix more than once. **/
    public boolean boardIsValid() {
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.spaces.length; ++i) {
            for (int j = 0; j < this.spaces[i].length; ++j) {
                if (this.spaces[i][j] < 0)
                    sb.append('x');
                else
                    sb.append(this.spaces[i][j]);
            }
            if (i < this.spaces.length - 1)
                sb.append('\n');
        }
        return sb.toString();
    }

    /** Same as toString(), but with no whitespace. Use for testing. **/
    public String rawString() {
        return this.toString().replace("\n", "");
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

    public void printData() {
        System.out.println("-----------");
        System.out.println("Filled squares: " + this.spacesFilled);
        System.out.println("Num  #filled");
        for (int i = 0; i < this.count.length; i++) {
            System.out.println(" " + (i + 1) + "     " + this.count[i]);
        }

        System.out.println("----Possible Numbers---");
        for (int i = 0; i < this.possibilities.length; i++) {
            for (int j = 0; j < this.possibilities[0].length; j++) {
                if (this.possibilities[i][j].size() == 0) {
                    System.out.print("X");
                } else {
                    for (int n: this.possibilities[i][j]) {
                        System.out.print(n);
                    }
                }
                System.out.print(" ");
            }
            System.out.println();
        }

        System.out.println("-----BoxNumsLeft-----");
        for(int k = 0; k < this.submatrixPossibilities.length; k++){
            System.out.print("Box " + k + ": ");
            for (int n: this.submatrixPossibilities[k])
                System.out.print(n);
            System.out.println();
        }

        System.out.println("-----RowNumsLeft-----");
        for(int i = 0; i < this.rowPossibilities.length; i++){
            System.out.print("Row " + i + ": ");
            for (int n: this.rowPossibilities[i])
                System.out.print(n);
            System.out.println();

        }

        System.out.println("-----ColNumsLeft-----");
        for(int j = 0; j < this.colPossibilities.length; j++){
            System.out.print("Col " + j + ": ");
            for (int n: this.colPossibilities[j])
                System.out.print(n);
            System.out.println();
        }
    }

}
