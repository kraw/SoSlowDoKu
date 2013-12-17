package Sudoku;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Perf {
    public static double ONE_MIL = 1000000.0;
    public static double ONE_BIL = 1000000000.0;

    public static String[] getPuzzles(InputStream is) throws IOException {
        List<String> puzzles = new LinkedList<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        while (in.ready()) {
            puzzles.add(in.readLine());
        }
        return puzzles.toArray(new String[0]);
    }

    /**
     * @param boards Each inner array of SudokuBoards represents all the solutions to a single puzzle.
     *               Any inner array (e.g. SudokuBoard[i]) may be null, indicating we failed to solve
     *               the puzzle (invalid input puzzle, badly-formatted input, bugs, etc).
     * @return An int[]{nBadSolutions, nUnsolvable} where nUnsolvable is the number of puzzles
     *         for which no solutions were found, and nBadSolutions indicates a "solution" was given
     *         that is not actually a valid solution.
     */
    public static int[] verify(SudokuBoard[][] boards) {
        int nBadSolutions = 0;
        int nUnsolvable = 0;
        for (SudokuBoard[] solns: boards) {
            if (solns == null) {
                nUnsolvable += 1;
            } else {
                for (SudokuBoard b: solns) {
                    if (!b.isSolution())
                        nBadSolutions += 1;
                }
            }
        }
        return new int[]{nBadSolutions, nUnsolvable};
    }

    public static void main(String args[]) throws IOException {
        boolean useParallel = true;
        int numThreads = 4;
        boolean findAll = false;
        for (int i = 0; i < args.length; ++i) {
            if (FrontEnd.contains(FrontEnd.PARALLEL_OPTS, args[i])) {
                useParallel = true;
            } else if (FrontEnd.contains(FrontEnd.STANDARD_OPTS, args[i])) {
                useParallel = false;
            } else if (FrontEnd.contains(FrontEnd.NTHREADS_OPTS, args[i])) {
                numThreads = Integer.parseInt(args[++i]);
            } else if (FrontEnd.contains(FrontEnd.ALL_OPTS, args[i])) {
                findAll = true;
            }
        }

        long start = System.nanoTime();
        System.out.println("Loading puzzles...");
        String[] puzzles = getPuzzles(System.in);
        double duration = (System.nanoTime() - start) / ONE_MIL;
        System.out.println("  Done (" + duration + " ms)");

        System.out.println("Solving " + puzzles.length + " puzzles");

        SudokuBoard[][] solutions = null;
        if (useParallel) {
            System.out.println("Running in parallel with " + numThreads + " threads...");
            start = System.nanoTime();
            solutions = ParallelSudokuSolver.run(puzzles, numThreads, findAll);
            duration = (System.nanoTime() - start) / ONE_BIL;
        } else {
            System.out.println("Running standard...");
            start = System.nanoTime();
            solutions = SudokuSolver.run(puzzles, findAll);
            duration = (System.nanoTime() - start) / ONE_BIL;
        }
        double durationPerPuzzle = duration * 1000.0 / puzzles.length;

        System.out.println("Took " + duration + " seconds");
        System.out.println("  " + durationPerPuzzle + " ms per puzzle");

        System.out.println("Verifying results");
        int[] counts = verify(solutions);
        int nWrong = counts[0];
        int nUnsolvable = counts[1];
        if (nWrong > 0 || nUnsolvable > 0) {
            System.out.println("  Produced " + nWrong + " incorrect solutions");
            System.out.println("  " + nUnsolvable + " inputs were unsolvable");
        } else {
            System.out.println("  All good");
        }
    }
}
