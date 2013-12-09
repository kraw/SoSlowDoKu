package Sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelSudokuSolver {

    static class Pair<A, B> {
        public A a;
        public B b;
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

    }

    static String[] hardPuzzles = {
            "...4......5..8.2.6.....7...2...4....3......1...5.3.8.25...6.3.8..6....95..8......",
            "1....6.8...71....66.....15..3.9.....7....184.....2........9.41.5....4..8...8..5..",
            ".2....7..4....9.3.6..2.3.4.....1......89.....9....4.6..94....5.5.....6.3.....5...",
            "1...5.7.9..7.......6.......2...........5.1..2....2.39.3.4.9...15...1...3...8...4.",
            "..3......4...8..36..83..1...4..6..73...9...1......2.....4.7..686........7.....5..",
            "...4......5...9...6...2..1.2...7.9.1..5....7......8.3...6....9.7...3.1......9.327",
            "...4......5..8.2.6.....71..2...4....3......1...5.3.8.25...6.3.8..6....9...8......",
            "1...5.7.9..71......6.......2...........5.1..2....2.39.3...9...15...1...3...8...4."
    };

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
        System.out.println("Reading from stdin");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        ArrayList<String> inputs = new ArrayList<String>();
        while (in.ready()) {
            String s = in.readLine();
            inputs.add(s);
        }

        SudokuBoard[] output = run(inputs.toArray(new String[0]), 4);
        assert(inputs.size() == output.length);
        for (int i = 0; i < output.length; ++i) {
            try {
                System.out.println("INPUT: `" + inputs.get(i) + "`");
                System.out.println(new SudokuBoard(inputs.get(i)));
                System.out.println("OUTPUT:");
                if (output[i] == null)
                    System.out.println("No solution found");
                else
                    System.out.println(output[i]);
            } catch (SudokuBoard.SudokuException e) {
                System.out.println(e.getMessage());
            }
        }



//        System.out.println("Solving:\n" + board);
//        if (board.solve()) {
//            System.out.println("Solved:\n" + board);
//        } else {
//            System.out.println("No solution");
//        }
    }
}
