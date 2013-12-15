package Sudoku;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

public class SudokuPanel extends JPanel implements PropertyChangeListener {

    // the top level layout for 81 text fields
    private GridLayout layout;

    // each text field displays a single entry of the SudokuBoard
    // each of these add this class as a listener so we can handle updates here
    private static final String DEFAULT_TEXT = "";
    private JFormattedTextField[][] fields;

    public SudokuPanel(SudokuBoard board) {
        this.setPreferredSize(new Dimension(250, 300));
        this.initLayout();
        this.resetToBoard(board);
    }

    public void loadPuzzle(SudokuBoard board) {
        this.resetToBoard(board);
    }

    public void setField(int i, int j, int n) {
        fields[i][j].setValue(n);
    }

    public void setFieldColor(int i, int j, Color color) {
        if (!fields[i][j].getValue().toString().equals(DEFAULT_TEXT) && fields[i][j].isEditable())
            fields[i][j].setForeground(color);
    }

    /**
     * @return A SudokuBoard created from the current state of the text fields
     */
    public SudokuBoard getCurrentBoard(){
        int[][] grid = new int[9][9];
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                grid[i][j] = getValueAt(i,j);
            }
        }
        return new SudokuBoard(grid);
    }

    private int getValueAt(int i, int j) {
        try {
            int n = Integer.parseInt(fields[i][j].getValue().toString());
            if (1 <= n && n <= 9) {
                return n;
            }
        } catch (NumberFormatException e) {
            ;
        }
        return -1;
    }

    private void initLayout() {
        // A 3x3 grid of submatrices
        this.layout = new GridLayout(3, 3);
        layout.setHgap(3);
        layout.setVgap(3);
        this.setLayout(layout);

        // store our text fields for later updates
        fields = new JFormattedTextField[9][9];

        for (int k = 0; k < 9; ++k) {
            // create a 3x3 submatrix
            GridLayout subMatrixGrid = new GridLayout(3, 3);
            subMatrixGrid.setHgap(1);
            subMatrixGrid.setVgap(1);
            JPanel subMatrixPanel = new JPanel(subMatrixGrid);

            // Set the entries in each submatrix
            int[] ijCoords = SudokuBoard.fromSubmatrixIndex(k);
            for (int i = ijCoords[0]; i < ijCoords[0] + 3; ++i) {
                for (int j = ijCoords[1]; j < ijCoords[1] + 3; ++j) {
                    assert(SudokuBoard.toSubmatrixIndex(i, j) == k);
                    try {
                        // this MaskFormatter allows for only a single digit
                        this.fields[i][j] = new JFormattedTextField(new MaskFormatter("#"));
                        this.fields[i][j].setValue(DEFAULT_TEXT);
                        this.fields[i][j].setHorizontalAlignment(JTextField.CENTER);
                        this.fields[i][j].addPropertyChangeListener("value", this);
                        subMatrixPanel.add(this.fields[i][j]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.add(subMatrixPanel);
        }
    }

    private void resetToBoard(SudokuBoard board) {
        // set text fields
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                int n = board.getEntry(i, j);
                if (n > 0) {
                    this.fixTextFieldValue(i, j, n);
                } else {
                    this.clearTextFieldValue(i, j);
                    fields[i][j].setEditable(true);
                }
            }
        }
    }

    /* Fix a text field to the given value. The field cannot be changed until a new board is loaded */
    private void fixTextFieldValue(int i, int j, int value) {
        fields[i][j].setValue(value);
        fields[i][j].setEditable(false);
        // make text bold
        fields[i][j].setFont(fields[i][j].getFont().deriveFont(Font.BOLD));
        fields[i][j].setForeground(Color.BLACK);
        fields[i][j].setBackground(Color.LIGHT_GRAY);
    }

    private void clearTextFieldValue(int i, int j) {
        fields[i][j].setEditable(true);
        fields[i][j].setValue(DEFAULT_TEXT);
        fields[i][j].setFont(fields[i][j].getFont().deriveFont(Font.PLAIN));
        fields[i][j].setForeground(Color.BLACK);
        fields[i][j].setBackground(Color.WHITE);
    }

    private void handleTextFieldChange(int i, int j) {
        fields[i][j].setForeground(Color.BLACK);
        int n = getValueAt(i, j);
        if (n < 1) {
            this.clearTextFieldValue(i, j);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        // Check for an updated text field
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (fields[i][j] == propertyChangeEvent.getSource()) {
                    this.handleTextFieldChange(i, j);
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SudokuPanel sPanel = new SudokuPanel(new SudokuBoard("........8..3...4...9..2..6.....79.......612...6.5.2.7...8...5...1.....2.4.5.....3"));
                    JFrame frame = new JFrame("Sudoku");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.add(sPanel);
                    frame.pack();
                    frame.setVisible(true);
                } catch (SudokuBoard.SudokuException e) {
                    e.printStackTrace();
                }


            }
        });
    }
}
