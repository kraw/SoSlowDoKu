package Sudoku;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SudokuSolver extends SudokuBoard {

    public static void printResult(String input, String result, boolean isSolved) {
        System.out.println("Solving:");
        System.out.println(input);
        if (isSolved) {
            System.out.println("Solved:");
            System.out.println(result);
        } else {
            System.out.println("No solution");
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (in.ready()) {
            String s = in.readLine();
            try {
                SudokuSolver board = new SudokuSolver(s);
                String input = board.toString();
                boolean solved = board.solve();
                String output = board.toString();
                printResult(input, output, solved);
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
        if (this.isSolution()) {
            return true;
        }
        this.applyLogic();
        this.bruteForce();
        return this.isValid() && this.isSolution();
    }

    public void applyLogic() {
        while(this.reduceOptions()
                || this.tryToFinishRows()
                || this.tryToFinishCols()
                || this.tryToFinishSubmatrices());
    }

    /* If any space has only one possible number remaining, place the number in its space.
     * Return true if the board changed.
     */
    protected boolean reduceOptions() {
        boolean stateChanged = false;
        for (int i = 0; i < this.options.length; ++i) {
            for (int j = 0; j < this.options[0].length; ++j) {
                if (this.options[i][j] != null && this.options[i][j].size() == 1) {
                    this.setEntry(i, j, this.options[i][j].getFirst());
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
        SudokuBoard backup = new SudokuBoard(this);
        int[] entry = this.findMostAttractiveEntry();
        this.bruteForce(entry[0], entry[1], backup);
    }

    protected boolean bruteForce(int i, int j, SudokuBoard backup) {
        for (int n: this.options[i][j].toArray()) {
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
        boolean stateChanged = false;
        final int[] ijCoords = SudokuBoard.fromSubmatrixIndex(submatrix);

        for (int n: this.submatrixOptions[submatrix].toArray()) {
            int ki = -1;
            int kj = -1;

            innerLoop: for (int i = ijCoords[0]; i < ijCoords[0] + 3; ++i) {
                for (int j = ijCoords[1]; j < ijCoords[1] + 3; ++j) {
                    if (this.entries[i][j] < 1 && this.options[i][j].contains(n)) {
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
                stateChanged = true;
            }
        }

        return stateChanged;
    }

    /*
     * For each number n in the row i, if only one space in the row has
     * n as an option then n must go in that space.
     */
    protected boolean tryToFillInRow(int i) {
        boolean stateChanged = false;
        for (int n: this.rowOptions[i].toArray()) {
            int k = -1;  // the index in the row where we see n
            for (int j = 0; j < 9; ++j) {
                if (this.entries[i][j] < 1 && this.options[i][j].contains(n)) {
                    if (k >= 0) {  // if we've already seen n once, then we can't
                        k = -1;
                        break;
                    } else {  // we've never seen n before, so store its location
                        k = j;
                    }
                }
            }

            if (k >= 0) {
                this.setEntry(i, k, n);
                stateChanged = true;
            }
        }
        return stateChanged;
    }

    protected boolean tryToFillInCol(int j) {
        boolean stateChanged = false;
        for (int n: this.colOptions[j].toArray()) {
            int k = -1;
            for (int i = 0; i < 9; ++i) {
                if (this.entries[i][j] < 1 && this.options[i][j].contains(n)) {
                    if (k >= 0) {   // n could go in two spaces, so we can't do anything
                        k = -1;
                        break;
                    } else {  // we've never seen n before, so store its location
                        k = i;
                    }
                }
            }

            if (k >= 0) {
                this.setEntry(k, j, n);
                stateChanged = true;
            }
        }

        return stateChanged;
    }

}
