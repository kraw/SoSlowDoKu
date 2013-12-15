package Sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class SudokuFrame extends JFrame implements ActionListener {

    private static Random random = new Random();
    private static final Color DARK_GREEN = new Color(0, 162, 0);

    // the puzzle the user is trying to solve
    private SudokuBoard initialBoard;
    // the current state of the board as the user progresses
    private SudokuBoard currentBoard;
    // the solved board
    private SudokuSolver solution;

    private SudokuPanel sudokuPanel;

    private JButton hintButton;
    private JButton checkButton;
    private JButton solveButton;
    private JButton resetButton;

    private JLabel messageLabel;

    public SudokuFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.sudokuPanel = new SudokuPanel(new SudokuBoard());
        this.messageLabel = new JLabel("--");
        this.initButtons();
        this.add(createMainPanel());
        this.pack();
    }

    private void loadPuzzle(SudokuBoard sb) {
        this.initialBoard = new SudokuBoard(sb);
        this.currentBoard = new SudokuBoard(sb);
        if (sb.nEntriesFilled > 0) {
            this.solution = new SudokuSolver(sb);
            this.solution.solve();
        }
        this.sudokuPanel.loadPuzzle(sb);
    }

    private void initButtons() {
        this.hintButton = new JButton("Hint");
        this.checkButton = new JButton("Check");
        this.solveButton = new JButton("Solve");
        this.resetButton = new JButton("Reset");
        this.hintButton.addActionListener(this);
        this.checkButton.addActionListener(this);
        this.solveButton.addActionListener(this);
        this.resetButton.addActionListener(this);
    }

    private JComponent createButtonPanel() {
        Box box = Box.createVerticalBox();
        box.add(hintButton);
        box.add(Box.createVerticalStrut(10));
        box.add(checkButton);
        box.add(Box.createVerticalStrut(10));
        box.add(solveButton);
        box.add(Box.createVerticalStrut(30));
        box.add(resetButton);
        box.add(Box.createVerticalStrut(30));
        box.add(this.messageLabel);
        return box;
    }

    private JComponent createMainPanel() {
        Box box = Box.createHorizontalBox();
        box.add(this.sudokuPanel);
        box.add(Box.createHorizontalStrut(5));
        box.add(this.createButtonPanel());
        return box;
    }

    private int gradeEntries() {
        this.currentBoard.copyFrom(this.sudokuPanel.getCurrentBoard());
        int countWrong = 0;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (this.currentBoard.getEntry(i, j) != this.solution.getEntry(i, j) && this.currentBoard.getEntry(i, j) > 0) {
                    this.sudokuPanel.setFieldColor(i, j, Color.RED);
                    countWrong++;
                } else {
                    this.sudokuPanel.setFieldColor(i, j, DARK_GREEN);
                }
            }
        }
        return countWrong;
    }

    private void showHint() {
        this.currentBoard.copyFrom(this.sudokuPanel.getCurrentBoard());
        int nEntriesLeft = 81 - this.currentBoard.nEntriesFilled;
        if (nEntriesLeft < 2)
            return;
        int choice = random.nextInt(nEntriesLeft);
        int count = -1;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (this.currentBoard.getEntry(i, j) < 0) {
                    count += 1;
                    if (count == choice) {
                        this.sudokuPanel.setField(i, j, this.solution.getEntry(i, j));
                    }
                }
            }
        }
        assert(false);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.out.println("paramString: " + actionEvent.paramString());
        if (actionEvent.getSource() == this.hintButton) {
            System.out.println("Clicked hint");
            if (this.solution != null && this.solution.isSolution()) {
                showHint();
            } else {
                this.messageLabel.setText("Not solvable");
            }
        } else if (actionEvent.getSource() == this.checkButton) {
            System.out.println("Clicked check");
            if (this.solution != null && this.solution.isSolution()) {
                int nWrong = this.gradeEntries();
                this.messageLabel.setText(nWrong + " incorrect");
            } else {
                this.messageLabel.setText("Not solvable");
            }
        } else if (actionEvent.getSource() == this.solveButton) {
            System.out.println("Clicked solve");
            if (this.solution != null && this.solution.isSolution()) {
                this.sudokuPanel.loadPuzzle(this.solution);
            } else {
                this.messageLabel.setText("Not solvable");
            }
        } else if (actionEvent.getSource() == this.resetButton) {
            System.out.println("Clicked reset");
            this.sudokuPanel.loadPuzzle(this.initialBoard);
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SudokuFrame frame = new SudokuFrame();
                try {
                    frame.loadPuzzle(new SudokuBoard("........8..3...4...9..2..6.....79.......612...6.5.2.7...8...5...1.....2.4.5.....3"));
                } catch (SudokuBoard.SudokuException e) {
                    e.printStackTrace();
                }
                frame.setVisible(true);
            }
        });
    }
}
