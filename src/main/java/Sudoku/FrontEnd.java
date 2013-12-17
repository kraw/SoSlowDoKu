package Sudoku;

import java.io.IOException;

public class FrontEnd {

    public static void printHelp() {
        System.out.println("Valid arugments:");
        System.out.println("  -p, --parallel      run with threads (this is the default)");
        System.out.println("  -s, --standard      run without threads");
        System.out.println("  -n, --nThreads [N]  run with the specified number of threads (defaults to 4)");
        System.out.println("  -h, --help          print this help");
        System.out.println("  -b, --benchmark     run the benchmark code");
        System.out.println();
        System.out.println("The program accepts input on stdin. Puzzles should be one per line.");
        System.out.println("Each puzzle should be 81 characters. Any character not between 1 and 9");
        System.out.println("is interpreted as a blank entry.");
    }

    public static void main(String args[]) throws IOException {
        boolean useParallel = true;
        boolean runBenchmark = false;

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-p") || args[i].equals("--parallel")) {
                useParallel = true;
            } else if (args[i].equals("-s") || args[i].equals("--standard")) {
                useParallel = false;
            } else if (args[i].equals("-h") || args[i].equals("--help")) {
                printHelp();
                return;
            } else if (args[i].equals("-n") || args[i].equals("--nThreads")) {
                i++;
            } else if (args[i].equals("-b") || args[i].equals("--benchmark")) {
                runBenchmark = true;
            } else {
                System.out.println("Unrecognized arg '" + args[i] + "'");
                printHelp();
                return;
            }
        }

        if (runBenchmark) {
            Perf.main(args);
        } else if (useParallel) {
            System.out.println("Running parallel...");
            ParallelSudokuSolver.main(args);
        } else {
            System.out.println("Running standard...");
            SudokuSolver.main(args);
        }
    }
}
