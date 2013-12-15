package Sudoku;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Perf {
    public static double ONE_MIL = 1000000.0;
    public static double ONE_BIL = 1000000000.0;

    public static String[] getPuzzles(String filename) throws IOException {
        List<String> puzzles = new LinkedList<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
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
        if (args.length < 1) {
            System.out.println("Need a file of Sudoku puzzles");
            return;
        }

        long start = System.nanoTime();
        System.out.println("Loading puzzles...");
        String[] puzzles = getPuzzles(args[0]);
        double duration = (System.nanoTime() - start) / ONE_MIL;
        System.out.println("  Done (" + duration + " ms)");

        System.out.println("Solving " + puzzles.length + " puzzles");

        start = System.nanoTime();
        SudokuBoard[] solutions = ParallelSudokuSolver.run(puzzles, 4);
        duration = (System.nanoTime() - start) / ONE_MIL;
        double durationPerPuzzle = duration / puzzles.length;

        System.out.println("Took " + duration + " ms");
        System.out.println("  " + durationPerPuzzle + " ms per puzzle");

        System.out.println("Verifying results");
        int nWrong = verify(solutions);
        if (nWrong > 0)
            System.out.println("  Failed to solve " + nWrong + " puzzles");
        else
            System.out.println("  All good");
    }
}
