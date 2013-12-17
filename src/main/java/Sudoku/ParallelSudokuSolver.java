package Sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelSudokuSolver {

    public static class SudokuTask implements Callable<SudokuBoard> {
        String boardString;
        public SudokuTask(String boardString) {
            this.boardString = boardString;
        }
        public SudokuBoard call() {
            try {
                SudokuSolver board = new SudokuSolver(this.boardString);
                if (board.solve())
                    return board;
            } catch (SudokuBoard.SudokuException e) {
                ;
            }
            return null;
        }
    }

    public static SudokuBoard[] run(String[] inputStrings, int nThreads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
        CompletionService<SudokuBoard> pool = new ExecutorCompletionService<SudokuBoard>(threadPool);
        List<Future<SudokuBoard>> output = new LinkedList<Future<SudokuBoard>>();

        for (String s: inputStrings) {
            output.add(pool.submit(new SudokuTask(s)));
        }

        SudokuBoard[] result = new SudokuBoard[output.size()];
        int i = 0;
        for (Future<SudokuBoard> future: output) {
            SudokuBoard sb = null;
            try {
                sb = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            result[i++] = sb;
        }

        threadPool.shutdown();
        return result;
    }

    public static void main(String args[]) throws IOException {
        int numThreads = 4;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-n") || args[i].equals("--nThreads")) {
                numThreads = Integer.parseInt(args[++i]);
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> inputs = new ArrayList<String>();
        while (in.ready()) {
            String s = in.readLine();
            inputs.add(s);
        }

        System.out.println("Using " + numThreads + " threads");
        SudokuBoard[] output = run(inputs.toArray(new String[0]), numThreads);
        assert(inputs.size() == output.length);
        for (int i = 0; i < output.length; ++i) {
            try {
                String input = new SudokuBoard(inputs.get(i)).toString();
                boolean isSolved = output[i] != null;
                if (isSolved) {
                    SudokuSolver.printResult(input, output[i].toString(), true);
                } else {
                    SudokuSolver.printResult(input, "", false);
                }
            } catch (SudokuBoard.SudokuException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
