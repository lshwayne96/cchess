package com.chess.gui;

import com.chess.engine.board.Move;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import static com.chess.gui.Table.*;

public class GameHistoryPanel extends JPanel {

    private final DataModel model;
    private final JScrollPane scrollPane;
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
    private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(120, 40);

    GameHistoryPanel() {
        setLayout(new BorderLayout());
        setBorder(PANEL_BORDER);

        model = new DataModel();
        JTable table = new JTable(model);
        table.setRowHeight(20);

        scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    void update(MoveLog movelog) {
        model.clear();
        int currentRow = 0;

        for (Move move : movelog.getMoves()) {
            String moveText = move.toString();
            if (move.getMovedPiece().getAlliance().isRed()) {
                model.setValueAt(moveText, currentRow, 0);
            }
            else {
                this.model.setValueAt(moveText, currentRow, 1);
                currentRow++;
            }
        }

        final JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        model.fireTableDataChanged();
    }

    private static class Row {

        private String redMove;
        private String blackMove;

        Row() {
        }

        public String getRedMove() {
            return redMove;
        }

        public String getBlackMove() {
            return blackMove;
        }

        public void setRedMove(String move) {
            redMove = move;
        }

        public void setBlackMove(String move) {
            blackMove = move;
        }

    }

    private static class DataModel extends DefaultTableModel {

        private final List<Row> values;
        private static final String[] NAMES = {"RED", "BLACK"};

        DataModel() {

            values = new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            if(values == null) {
                return 0;
            }

            return values.size();
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Row currentRow = values.get(row);

            if(col == 0) {
                return currentRow.getRedMove();
            } else if (col == 1) {
                return currentRow.getBlackMove();
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            Row currentRow;

            if(values.size() <= row) {
                currentRow = new Row();
                values.add(currentRow);
            } else {
                currentRow = values.get(row);
            }

            if(col == 0) {
                currentRow.setRedMove((String) aValue);
                fireTableRowsInserted(row, row);
            } else if(col == 1) {
                currentRow.setBlackMove((String) aValue);
                fireTableCellUpdated(row, col);
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return Move.class;
        }

        @Override
        public String getColumnName(int col) {
            return NAMES[col];
        }

        public void clear() {
            values.clear();
            setRowCount(0);
        }
    }
}
