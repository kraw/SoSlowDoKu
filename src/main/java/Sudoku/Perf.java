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

    public static int verify(SudokuBoard[] boards) {
        int nWrong = 0;
        for (SudokuBoard sb: boards)
            if (sb == null || !sb.isSolution())
                nWrong += 1;
        return nWrong;
    }

    public static void main(String args[]) throws IOException {
        boolean useParallel = true;
        int numThreads = 4;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-p") || args[i].equals("--parallel")) {
                useParallel = true;
            } else if (args[i].equals("-s") || args[i].equals("--standard")) {
                useParallel = false;
            } else if (args[i].equals("-n") || args[i].equals("--nThreads")) {
                numThreads = Integer.parseInt(args[++i]);
            }
        }


        long start = System.nanoTime();
        System.out.println("Loading puzzles...");
        String[] puzzles = getPuzzles(System.in);
        double duration = (System.nanoTime() - start) / ONE_MIL;
        System.out.println("  Done (" + duration + " ms)");

        System.out.println("Solving " + puzzles.length + " puzzles");

        start = System.nanoTime();
        SudokuBoard[] solutions = null;
        if (useParallel) {
            System.out.println("Running in parallel with " + numThreads + " threads...");
            solutions = ParallelSudokuSolver.run(puzzles, numThreads);
        } else {
            System.out.println("Running standard...");
            solutions = SudokuSolver.run(puzzles);
        }
        duration = (System.nanoTime() - start) / ONE_BIL;
        double durationPerPuzzle = duration * 1000.0 / puzzles.length;

        System.out.println("Took " + duration + " seconds");
        System.out.println("  " + durationPerPuzzle + " ms per puzzle");

        System.out.println("Verifying results");
        int nWrong = verify(solutions);
        if (nWrong > 0)
            System.out.println("  Failed to solve " + nWrong + " puzzles");
        else
            System.out.println("  All good");
    }
}
