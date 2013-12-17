package Sudoku;

import java.io.IOException;

public class FrontEnd {
    public static String[] PARALLEL_OPTS = {"-p", "--parallel"};
    public static String[] STANDARD_OPTS = {"-s", "--standard"};
    public static String[] NTHREADS_OPTS = {"-n", "--nThreads"};
    public static String[] HELP_OPTS = {"-h", "--help"};
    public static String[] BENCH_OPTS = {"-b", "--benchmark"};
    public static String[] ALL_OPTS = {"-a", "--all"};

    public static String join(String sep, String[] args) {
        String result = "";
        for (int i = 0; i < args.length; ++i) {
            result += args[i];
            if (i + 1 < args.length) {
                result += sep;
            }
        }
        return result;
    }

    public static String padString(String s, int width) {
        if (width-1 > 0)
            return String.format("%1$-" + (width-1) + "s", s);
        return "";
    }

    public static String helpString(String[] args, int width) {
        return padString(join(", ", args), width);
    }

    public static <T> boolean contains(T[] args, T value) {
        for (T t: args) {
            if (t.equals(value))
                return true;
        }
        return false;
    }

    public static void printHelp() {
        int width = 18;
        System.out.println("Valid arugments:");
        System.out.println("  " + helpString(PARALLEL_OPTS, width) + " run with threads (this is the default)");
        System.out.println("  " + helpString(STANDARD_OPTS, width) + " run without threads");
        System.out.println("  " + helpString(NTHREADS_OPTS, width) + " run with a specified number of threads (defaults to 4)");
        System.out.println("  " + helpString(HELP_OPTS, width)     + " print this help");
        System.out.println("  " + helpString(BENCH_OPTS, width)    + " run the benchmark code");
        System.out.println("  " + helpString(ALL_OPTS, width)      + " find all possible solutions");
        System.out.println();
        System.out.println("The program accepts input on stdin. Puzzles should be one per line.");
        System.out.println("Each puzzle should be 81 characters. Any character not between 1 and 9");
        System.out.println("is interpreted as a blank entry.");
    }

    public static void main(String args[]) throws IOException {
        boolean useParallel = true;
        boolean runBenchmark = false;
        boolean findAll = false;

        for (int i = 0; i < args.length; ++i) {
            if (contains(PARALLEL_OPTS, args[i])) {
                useParallel = true;
            } else if (contains(STANDARD_OPTS, args[i])) {
                useParallel = false;
            } else if (contains(HELP_OPTS, args[i])) {
                printHelp();
                return;
            } else if (contains(NTHREADS_OPTS, args[i])) {
                i++;
            } else if (contains(BENCH_OPTS, args[i])) {
                runBenchmark = true;
            } else if (contains(ALL_OPTS, args[i])) {
                findAll = true;
            } else {
                System.out.println("Unrecognized arg '" + args[i] + "'");
                printHelp();
                return;
            }
        }
        if (findAll) {
            System.out.println("Finding all valid solutions.");
            System.out.println("Quitting once " + SudokuSolver.MAX_SOLNS + " solutions are found.");
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
