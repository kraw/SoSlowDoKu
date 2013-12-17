package Sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class uses a thread pool to work on a stream of puzzles.
 */
public class ParallelSudokuSolver {

    public static class SudokuTask implements Callable<SudokuBoard[]> {
        String boardString;
        boolean findAll;
        public SudokuTask(String boardString, boolean findAll) {
            this.boardString = boardString;
            this.findAll = findAll;
        }
        public SudokuBoard[] call() {
            List<SudokuBoard> result = null;
            try {
                SudokuSolver board = new SudokuSolver(this.boardString);
                if (findAll) {
                    result = board.solveAll();
                } else if (board.solve()) {
                    result = new LinkedList<SudokuBoard>();
                    result.add(board);
                }
            } catch (SudokuBoard.SudokuException e) {
                ;
            }
            if (result != null) {
                return result.toArray(new SudokuBoard[0]);
            }
            return null;
        }
    }

    public static void printResult(SudokuBoard input, SudokuBoard[] output) {
        System.out.println("Solving:");
        System.out.println(input);
        if (output == null || output.length == 0) {
            System.out.println("No solutions");
        } else {
            System.out.println("Found " + output.length + " solution(s):");
            for (int i = 0; i < output.length; ++i) {
                System.out.println((i + 1) + ".");
                System.out.println(output[i]);
            }
        }
    }

    public static SudokuBoard[][] run(String[] inputStrings, int nThreads, boolean findAll) {
        ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
        CompletionService<SudokuBoard[]> pool =
                new ExecutorCompletionService<SudokuBoard[]>(threadPool);
        List<Future<SudokuBoard[]>> output =
                new LinkedList<Future<SudokuBoard[]>>();

        for (String s: inputStrings) {
            output.add(pool.submit(new SudokuTask(s, findAll)));
        }

        // each input puzzle has an array of 1 or more solutions (or null if no solution)
        SudokuBoard[][] result = new SudokuBoard[output.size()][];
        int i = 0;
        for (Future<SudokuBoard[]> future: output) {
            SudokuBoard[] solns = null;
            try {
                solns = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            result[i++] = solns;
        }

        threadPool.shutdown();
        return result;
    }

    public static void main(String args[]) throws IOException {
        int numThreads = 4;
        boolean findAll = false;
        for (int i = 0; i < args.length; ++i) {
            if (FrontEnd.contains(FrontEnd.NTHREADS_OPTS, args[i])) {
                numThreads = Integer.parseInt(args[++i]);
            } else if (FrontEnd.contains(FrontEnd.ALL_OPTS, args[i])) {
                findAll = true;
            }
        }

        String[] inputs = Perf.getPuzzles(System.in);
        System.out.println("Using " + numThreads + " threads");
        SudokuBoard[][] output = run(inputs, numThreads, findAll);
        assert(inputs.length == output.length);
        for (int i = 0; i < output.length; ++i) {
            try {
                SudokuBoard input = new SudokuBoard(inputs[i]);
                printResult(input, output[i]);
            } catch (SudokuBoard.SudokuException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
