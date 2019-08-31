package com.chess.engine.board;

import com.chess.engine.Alliance;

public class BoardUtil {

    /**
     * Checks if the given position is within bounds.
     */
    public static boolean isWithinBounds(Coordinate position) {
        int row = position.getRow();
        int col = position.getCol();

        return (row >= 0 && row < Board.NUM_ROWS) && (col >= 0 && col < Board.NUM_COLS);
    }

    /**
     * Returns the mirrored version of the given position.
     */
    public static Coordinate getMirrorPosition(Coordinate position) {
        return new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
    }

    /**
     * Returns the index of a position based on its row and column.
     */
    static int positionToIndex(int row, int col) {
        return row * Board.NUM_COLS + col;
    }

    /**
     * Returns the index of a given position.
     */
    static int positionToIndex(Coordinate position) {
        return positionToIndex(position.getRow(), position.getCol());
    }

    /**
     * Converts a column number to its corresponding file number based on the given alliance.
     */
    public static int colToFile(int col, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - col : col + 1;
    }

    /**
     * Converts a file number to its corresponding column number based on the given alliance.
     */
    public static int fileToCol(int file, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - file : file - 1;
    }

    /**
     * Converts a row number to its corresponding rank number based on the given alliance.
     */
    public static int rowToRank(int row, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - row : row + 1;
    }

    /**
     * Converts rank number to its corresponding row number based on the given alliance.
     */
    public static int rankToRow(int rank, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - rank : rank - 1;
    }

    /**
     * Checks if the given positions are in the same row or column.
     */
    public static boolean sameColOrRow(Coordinate first, Coordinate... others) {
        boolean sameRow = true;
        boolean sameCol = true;

        for (Coordinate position : others) {
            if (position.getRow() != first.getRow()) {
                sameRow = false;
            }
            if (position.getCol() != first.getCol()) {
                sameCol = false;
            }
            if (!(sameRow || sameCol)) {
                return false;
            }
        }

        return true;
    }
}
