package Sudoku;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SudokuSolver extends SudokuBoard {

    public static void main(String[] args) throws IOException {
        System.out.println("Reading from stdin");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (in.ready()) {
            String s = in.readLine();
            try {
                SudokuSolver board = new SudokuSolver(s);
                System.out.println("Solving:\n" + board);
                if (board.solve()) {
                    System.out.println("Solved:\n" + board);
                } else {
                    System.out.println("No solution");
                }
            } catch (SudokuException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public SudokuSolver(String s) throws SudokuException {
        super(s);
    }

    public SudokuSolver(SudokuBoard board) {
        super(board);
    }

    public boolean solve() {
        if (this.isSolution())
            return true;
        this.applyLogic();
        this.bruteForce();
        return this.isValid() && this.isSolution();
    }

    public void applyLogic() {
        boolean stateChanged;
        do {
            stateChanged = false;
//            System.out.println("\n" + this);
            while (this.reduceOptions()) {
//                System.out.println("\nCLEANUP\n" + this);
//                this.printData();
                stateChanged = true;
            }
            while (this.tryToFinishRows()) {
//                System.out.println("\nROWS\n" + this);
                stateChanged = true;
            }
            while (this.tryToFinishCols()) {
//                System.out.println("\nCOLS\n" + this);
                stateChanged = true;
            }
            while (this.tryToFinishSubmatrices()) {
//                System.out.println("\nSUBMATRIX\n" + this);
                stateChanged = true;
            }
        } while (stateChanged);
    }

    /* If any space has only one possible number remaining, place the number in its space.
     * Return true if the board changed.
     */
    protected boolean reduceOptions() {
        boolean stateChanged = false;
        for (int i = 0; i < this.options.length; ++i) {
            for (int j = 0; j < this.options[0].length; ++j) {
                if (this.options[i][j] != null && this.options[i][j].size() == 1) {
                    int n = this.options[i][j].iterator().next(); // grab the only thing in the set
                    this.setEntry(i, j, n);
                    stateChanged = true;
                }
            }
        }
        return stateChanged;
    }

    /* This method finds the next space where the solver will guess numbers.
     * Currently, this finds the entry with the fewest number of options left.
     */
    protected int[] findMostAttractiveEntry() {
        int min = 10;
        int imin = -1;
        int jmin = -1;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                int size = this.options[i][j].size();
                if (size > 0 && size < min) {
                    min = size;
                    imin = i;
                    jmin = j;
                }
            }
        }
        return new int[]{imin, jmin};
    }

    public void bruteForce() {
        if (this.isFilled())
            return;

        int[] space = this.findMostAttractiveEntry();
        int i = space[0];
        int j = space[1];

        SudokuBoard backup = new SudokuBoard(this);
        this.bruteForce(i, j, backup);
    }

    protected boolean bruteForce(int i, int j, SudokuBoard backup) {
        IntSet guesses = (IntSet) this.options[i][j].clone();
        for (int n: guesses) {
            this.setEntry(i, j, n);
            this.applyLogic();

            if (this.isFilled() && this.isSolution()) {
                return true;
            } else {
                int[] entry = this.findMostAttractiveEntry();
                assert(entry.length == 2);
                if (entry[0] >= 0 && this.bruteForce(entry[0], entry[1], new SudokuBoard(this))) {
                    return true;
                } else {
                    this.copyFrom(backup);
                }
            }
        }
        return false;
    }


    public boolean tryToFinishSubmatrices() {
        boolean stateChanged = false;
        for (int i = 0; i < 9; ++i)
            if (this.submatrixOptions[i].size() > 0 && this.tryToFillInSubmatrix(i))
                stateChanged = true;
        return stateChanged;
    }

    public boolean tryToFinishRows() {
        boolean stateChanged = false;
        for (int i = 0; i < 9; ++i)
            if (this.rowOptions[i].size() > 0 && this.tryToFillInRow(i))
                stateChanged = true;
        return stateChanged;
    }

    public boolean tryToFinishCols() {
        boolean stateChanged = false;
        for (int i = 0; i < 9; ++i)
            if (this.colOptions[i].size() > 0 && this.tryToFillInCol(i))
                stateChanged = true;
        return stateChanged;
    }

    protected boolean tryToFillInSubmatrix(int submatrix) {
        int iStart = (submatrix / 3) * 3;
        int jStart = (submatrix % 3) * 3;
        int iEnd = iStart + 3;
        int jEnd = jStart + 3;
        boolean stateChanged = false;

        for (int n: this.submatrixOptions[submatrix]) {
            int ki = -1;
            int kj = -1;

            innerLoop: for (int i = iStart; i < iEnd; ++i) {
                for (int j = jStart; j < jEnd; ++j) {
                    if (this.entries[i][j] < 0 && this.options[i][j].contains((Integer) n)) {
                        if (ki >= 0) {
                            ki = kj = -1;
                            break innerLoop;
                        } else {
                            ki = i;
                            kj = j;
                        }
                    }
                }
            }

            if (ki >= 0) {
                this.setEntry(ki, kj, n);
                return true;
            }
        }

        return stateChanged;
    }

    protected boolean tryToFillInRow(int i) {
        boolean stateChanged = false;
        for (int n: this.rowOptions[i].toArray(new Integer[0])) {
            // If only one space in the row has n as a possibility, then n must go in that space.
            // k represents the column where n must go.
            int k = -1;
            innerLoop : for (int j = 0; j < 9; ++j) {
                if (this.entries[i][j] < 0 && this.options[i][j].contains((Integer) n)) {
                    if (k >= 0) {   // n could go in two spaces, so we can't do anything
                        k = -1;
                        break innerLoop;
                    } else {  // we've never seen n before, so store its location
                        k = j;
                    }
                }
            }

            if (k >= 0) {
                this.setEntry(i, k, n);
                return true;
            }
        }
        return stateChanged;
    }

    protected boolean tryToFillInCol(int j) {
        boolean stateChanged = false;
        for (int n: this.colOptions[j]) {
            int k = -1;
            innerLoop : for (int i = 0; i < 9; ++i) {
                if (this.entries[i][j] < 0 && this.options[i][j].contains((Integer) n)) {
                    if (k >= 0) {   // n could go in two spaces, so we can't do anything
                        k = -1;
                        break innerLoop;
                    } else {  // we've never seen n before, so store its location
                        k = i;
                    }
                }
            }

            if (k >= 0) {
                this.setEntry(k, j, n);
                return true;
            }
        }

        return stateChanged;
    }

}
