Overview
========

This is another quick Sudoku solver written in Java. It can find a solution in roughly 10-30 milliseconds per puzzle on my machine. It can also find all solutions for a puzzle (if it has more than one).

### Build with Maven ###

Run the following:
    
    mvn clean compile assmbly:single

This spits out a jar in the `target/` directory. If the jar is called `sudoku.jar`, then run it as follows:

    java -jar target/sudoku.jar < puzzles.txt > output.txt

### Build without Maven ###

This is more awkward, but you can do the following:

    cd src/main/java
    javac Sudoku/*.java
    java Sudoku.FrontEnd < ../../../puzzles.txt > output.txt

## Execution ##

By default the program runs in parallel and stops on the first solution found. There a some command line arguments to change this:

    $ java -jar sudoku.jar -h
    Valid arugments:
      -p, --parallel    run with threads (this is the default)
      -s, --standard    run without threads
      -n, --nThreads    run with a specified number of threads (defaults to 4)
      -h, --help        print this help
      -b, --benchmark   run the benchmark code
      -a, --all         find all possible solutions
    ...

Examples:
  * `java -jar sudoku.jar --nThreads 2 < puzzles.txt` -- run with two threads
  * `java -jar sudoku.jar -s -a < puzzles.txt` -- single-threaded and  find all solutions
  * `java -jar sudoku.jar -b -a -n 5 < puzzles.txt` -- time how long it takes using 5 threads while finding all solutions 

## Implementation ##

The program maintains lists of remaining numbers for each component of the puzzle (each space, row, column, or 3x3 submatrix). These lists of options are then wittled down in two ways: "logic" and guessing/backtracking. Once a component has only one number in its list of options, we can place that number on the board (and update these lists of options to reflect the change).

The "logic" used is fairly simple right now. For each number `n` not yet placed in a row, we check each space in the row to see if `n` is a valid option for that space (by checking the space's list of options). If we see that there's only one space in the row that can contain `n`, then `n` must go in that space. (And we can do an identical thing for each column and each submatrix).

The program uses "logic" until it fails to place a new number on the board. It then picks a space at which to guess numbers. It tries guessing each number which might go in the space and then recursively tries to solve the board, backtracking when it gets stuck (when no spaces have any valid options). I found it's generally faster to guess at spaces with the fewest options remaining.

## Bechmarks ##

I looked around for some benchmarks on the internet and found [this quality post](http://attractivechaos.wordpress.com/2011/06/19/an-incomplete-review-of-sudoku-solver-implementations/). For reference, I ran Peter Norvig's nifty Python solution on my machine using the [20 hard Sudoku puzzles](https://github.com/attractivechaos/plb/blob/master/sudoku/sudoku.txt) from the post (repeated 50 times, as in the post):

    $ time -p for i in {1..50}; do python norvig.py > tmp.out; done;
    real 99.49
    user 0.27
    sys 0.85

And here's JSolve (version 1.2, compiled under MinGW on Windows), which is very fast:

    $ time -p for i in {1..50}; do ./jsolve32.exe < HardPuzzlesSmall.txt > tmp.out; done;
    real 1.42
    user 0.22
    sys 0.52

And my solution:
    
    $ time -p for i in {1..50}; do java -jar sudoku.jar < HardPuzzlesSmall.txt > tmp.out; done;
    real 27.29
    user 0.50
    sys 1.02

## Code ##

  * `SudokuBoard` has the basic board implementation. This maintains the values of each space as well as lists of remaining numbers for each component (space, row, column, or 3x3 submatrix).

  * `SudokuSolver` subclasses `SudokuBoard` to add solving functionality. This class is (for now) entirely single-threaded. `SudokuSolver.main()` reads in puzzles from stdin and solves them one at a time.

  * `IntSet` is a set type designed for storing the numbers 1 through 9 efficiently. It has contant-time `add`, `remove`, `clear`, `contains`, and `size` methods.

  * `ParallelSudokuSolver.main()` reads in puzzles from stdin and uses a pool of worker threads to solve them in parallel. 

  * `Perf.main()` will benchmark a set of puzzles read in on stdin. It gives the number of puzzles, total time, average time per puzzle, and verfies all of the solutions.

  * `FrontEnd.main()` checks command line arguments and delegates to another main method appropriately.

## Todo ##

  - Random puzzle generation. I have a basic idea: Fill in [seventeen initial spaces](http://www.technologyreview.com/view/426554/mathematicians-solve-minimum-sudoku-problem/). Check if there is a unique solution (by finding all solutions). If not, place another number. Repeat until we have a puzzle with a unique solution. Varying the difficulty here is tricky though. 
  - A simple gui. This could easily provide hints, check your current progress, and generate random puzzles to solve.
