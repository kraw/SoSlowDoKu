
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import Sudoku.SudokuSolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import Sudoku.SudokuBoard;

@RunWith(JUnit4.class)
public class SudokuTest {
    static String validBoardString = "x9x7xx86x" +
                                     "x31xx5x2x" +
                                     "8x6xxxxxx" +
                                     "xx7x5xxx6" +
                                     "xxx3x7xxx" +
                                     "5xxx1x7xx" +
                                     "xxxxxx1x9" +
                                     "x2x6xx35x" +
                                     "x54xx8x7x";

    static String invalidRowString = "x9x7xx86x" +
                                     "x311x5x2x" +  // two ones in this row
                                     "8x6xxxxxx" +
                                     "xx7x5xxx6" +
                                     "xxx3x7xxx" +
                                     "5xxx1x7xx" +
                                     "xxxxxx1x9" +
                                     "x2x6xx35x" +
                                     "x54xx8x7x";

    // has two nines in last column
    static String invalidColString = "x9x7xx86x" +
                                     "x31xx5x29" +
                                     "8x6xxxxxx" +
                                     "xx7x5xxx6" +
                                     "xxx3x7xxx" +
                                     "5xxx1x7xx" +
                                     "xxxxxx1x9" +
                                     "x2x6xx35x" +
                                     "x54xx8x7x";

    // has two sevens in the bottom right submatrix
    static String invalidSubmatrixString = "x9x7xx86x" +
                                           "x31xx5x2x" +
                                           "8x6xxxxxx" +
                                           "xx7x5xxx6" +
                                           "xxx3x7xxx" +
                                           "5xxx1x7xx" +
                                           "xxxxxx1x9" +
                                           "x2x6xx357" +
                                           "x54xx8x7x";

    static String solvedBoardString = "295743861" +
                                      "431865927" +
                                      "876192543" +
                                      "387459216" +
                                      "612387495" +
                                      "549216738" +
                                      "763524189" +
                                      "928671354" +
                                      "154938672";

    /* Borrowed from http://sudopedia.enjoysudoku.com/Valid_Test_Cases.html */
    static String inputA  = "2564891733746159829817234565932748617128.6549468591327635147298127958634849362715";
    static String outputA = "256489173374615982981723456593274861712836549468591327635147298127958634849362715";

    static String inputB  = "3.542.81.4879.15.6.29.5637485.793.416132.8957.74.6528.2413.9.655.867.192.965124.8";
    static String outputB = "365427819487931526129856374852793641613248957974165283241389765538674192796512438";

    static String inputC  = "..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..";
    static String outputC = "672435198549178362831629547368951274917243856254867931193784625486592713725316489";

    /* Borrowed from http://www.spoj.com/problems/SUD/ */
    static String inputD  = "..41..3.8.1....62...82..4.....3.28.9....7....7.16.8...562..17.3.3.....4.1....5...";
    static String outputD = "294167358315489627678253491456312879983574216721698534562941783839726145147835962";

    static String inputE  = "1.......4....1.38.27.9.4...91.7...........5..86.4.5.9..3......8..9....2.4.......7";
    static String outputE = "198563274654217389273984615915726843347198562862435791731642958589371426426859137";

    /* Found here: http://forum.enjoysudoku.com/the-hardest-sudokus-new-thread-t6539.html
     * with some similar-looking puzzles removed */
    static String[] hardPuzzles = {
            "...4......5..8.2.6.....7...2...4....3......1...5.3.8.25...6.3.8..6....95..8......",
            "1....6.8...71....66.....15..3.9.....7....184.....2........9.41.5....4..8...8..5..",
            ".2....7..4....9.3.6..2.3.4.....1......89.....9....4.6..94....5.5.....6.3.....5...",
            "1...5.7.9..7.......6.......2...........5.1..2....2.39.3.4.9...15...1...3...8...4.",
            "..3......4...8..36..83..1...4..6..73...9...1......2.....4.7..686........7.....5..",
            "...4......5...9...6...2..1.2...7.9.1..5....7......8.3...6....9.7...3.1......9.327",
            "...4......5..8.2.6.....71..2...4....3......1...5.3.8.25...6.3.8..6....9...8......",
            "1...5.7.9..71......6.......2...........5.1..2....2.39.3...9...15...1...3...8...4.",
            "1....6.8...7.....66.....15..3.9.........2....7....854.....9.81.8...41..5.1....4..",
            "...4......5..8.2.6.....7...2........3.....51...5.3.8.25...6.3.8..6....9...8.7....",
            "12..5...9.57...2...9..2..1....8..9.47...6.1.......4............57..9.6....6..3...",
            ".2.4...8...7.....3.8.237.1.2.1....9..9....8.4...9......1.8...4.5.8..........6....",
            "..3..67.........2.79.2......3....6..5....4..76.7..3.453.5..74...............1...8",
            "..3.5.....5.1..2.66...2..4....8...9..8..1.6.5...6.....7.......4..........6...18.2",
            "..3.56....5.1..2.6....2..4...68...9..8..1.6.5...6.....7.......4..........6...18.2",
            "..3.....945...92....9..3.54....6....6..9..8....5..8.2..1.7................4..5.92",
            ".....6..9..67...3.79...3....1...7.5...752......5.....2..167..2......14..8........",
            "...4......5..8.2.6.....71..2........3.....51...5.3.8.25...6.3.8..6..8.9...8......",
            ".2.4...8......9..2..9.3............5..8..7....4.5..82...46..21.6.21..4...1......8",
            "........9.5.7...2.7.9..2....1.67..5.......4..8....5....7.31....6....7.3..3..6...1",
            "....5.7..4.7..9.3..8....1..2.8..7.....4..8....7..9...3......6...4...2.97..2.....1",
            ".2.4.6..9..7......6...2..5......15...1.64...28......1..3.26.....6..1...39....3...",
            "1....67.9.5.........9.....4....9..3.....1....9..6..8.1..27.....7..8...4.8...6.1.7",
            ".2..5...9...7.....7.....5..2..........4..8....6..2..91.3.2..9..6...9..13..1.6..2.",
            ".......8...67......8...36.5.4..3..5......4..66....83.4..1.9..2..3...25..9........",
            "..3.9....4...8..36..8...1...4..6..73...9..........2.....4.7..686........7.....5.4",
            "..39.....4...8..36..8...1...4..6..73...3...1......2.....4.7..686........7.....5..",
            "..3......4...8..36..8...1...4..6..73...9...1......2.....4.7..686....4...7.....5..",
            "12.4..3..3...1..5...6...1..7...9.....4.6.3.....3..2...5...8.7....7.....5.......98",
            "1...5......7..9.3...9..754...4..3.7..6........9.8........79..2......24.3..2......",
            ".23.....94.....1...9..3..4.2..81...4.....78..9...4...23...9...1.6..........5.....",
            "1..4.6..........2..8..3.5.6.6...48.5............5..2.......3.9...7..8....4.6..3.8",
            "..3.5.....567....27..2...4......18..3...2...66...7...453..4...7.......9.......4..",
            "...4.67....7.....6....7..512......9...5.6.1...91.426..3...........8.......4.1.5..",
            "1..4....9.56..9.......1..6..6....8..5....4.9.9....5.1..7....2..6....1.5....3.....",
            "1.....78...6.8.1....9.....6.....4.5.6..59...19...1...8..2.........3.....8...6...7",
            "12.3.....4.5...6...7.....2.6..1..3....453.........8..9...45.1.........8......2..7",
            "5..6......2.....4...1.2.3..9..8.......7.4.1.......9..6..4.7.2...3.....1......5..8"
    };

    @Test
    public void DefaultConstruction() {
        SudokuBoard board = new SudokuBoard();
    }

    @Test
    public void ConstructFromString_validBoard() {
        try {
            SudokuBoard board = new SudokuBoard(validBoardString);
            assertEquals(board.rawString(), validBoardString);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void ConstructFromString_invalidRow() {
        try {
            SudokuBoard board = new SudokuBoard(invalidRowString);
            fail("No exception thrown");
        } catch (SudokuBoard.SudokuException e) {
            assertEquals(e.getMessage(), "Invalid board state");
        }
    }

    @Test
    public void ConstructFromString_invalidCol() {
        try {
            SudokuBoard board = new SudokuBoard(invalidColString);
            fail("No exception thrown");
        } catch (SudokuBoard.SudokuException e) {
            assertEquals(e.getMessage(), "Invalid board state");
        }
    }

    @Test
    public void ConstructFromString_invalidSubmatrix() {
        try {
            SudokuBoard board = new SudokuBoard(invalidSubmatrixString);
            fail("No exception thrown");
        } catch (SudokuBoard.SudokuException e) {
            assertEquals(e.getMessage(), "Invalid board state");
        }
    }

    @Test
    public void isSolution() {
        try {
            SudokuBoard board = new SudokuBoard(solvedBoardString);
            assertEquals(board.rawString(), solvedBoardString);
            assertTrue(board.isSolution());
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_BasicSolving() {
        try {
            SudokuSolver solver = new SudokuSolver(validBoardString);
            solver.solve();
            assertEquals(solver.rawString(), solvedBoardString);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_Idempotence() {
        try {
            SudokuSolver solver = new SudokuSolver(solvedBoardString);
            solver.solve();
            assertEquals(solver.rawString(), solvedBoardString);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_inputA() {
        try {
            SudokuSolver solver = new SudokuSolver(inputA);
            solver.solve();
            assertEquals(solver.rawString(), outputA);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_inputB() {
        try {
            SudokuSolver solver = new SudokuSolver(inputB);
            solver.solve();
            assertEquals(solver.rawString(), outputB);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_inputC() {
        try {
            SudokuSolver solver = new SudokuSolver(inputC);
            solver.solve();
            assertEquals(solver.rawString(), outputC);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_inputD() {
        try {
            SudokuSolver solver = new SudokuSolver(inputD);
            solver.solve();
            assertEquals(solver.rawString(), outputD);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_inputE() {
        try {
            SudokuSolver solver = new SudokuSolver(inputE);
            solver.solve();
            assertEquals(solver.rawString(), outputE);
        } catch (SudokuBoard.SudokuException e) {
            fail(e.toString());
        }
    }

    @Test
    public void Solver_hardPuzzles_1() {
        for (int i = 0; i < hardPuzzles.length / 4; ++i) {
            try {
                SudokuSolver solver = new SudokuSolver(hardPuzzles[i]);
                assertTrue(solver.solve());
            } catch (SudokuBoard.SudokuException e) {
                fail("Failed " + hardPuzzles[i] + " : " + e.toString());
            }
        }
    }

    @Test
    public void Solver_hardPuzzles_2() {
        for (int i = hardPuzzles.length / 4; i < hardPuzzles.length / 2; ++i) {
            try {
                SudokuSolver solver = new SudokuSolver(hardPuzzles[i]);
                assertTrue(solver.solve());
            } catch (SudokuBoard.SudokuException e) {
                fail("Failed " + hardPuzzles[i] + " : " + e.toString());
            }
        }
    }

    @Test
    public void Solver_hardPuzzles_3() {
        for (int i = hardPuzzles.length / 2; i < 3 * hardPuzzles.length / 4; ++i) {
            try {
                SudokuSolver solver = new SudokuSolver(hardPuzzles[i]);
                assertTrue(solver.solve());
            } catch (SudokuBoard.SudokuException e) {
                fail("Failed " + hardPuzzles[i] + " : " + e.toString());
            }
        }
    }

    @Test
    public void Solver_hardPuzzles_4() {
        for (int i = 3 * hardPuzzles.length / 4; i < hardPuzzles.length; ++i) {
            try {
                SudokuSolver solver = new SudokuSolver(hardPuzzles[i]);
                assertTrue(solver.solve());
            } catch (SudokuBoard.SudokuException e) {
                fail("Failed " + hardPuzzles[i] + " : " + e.toString());
            }
        }
    }


}
