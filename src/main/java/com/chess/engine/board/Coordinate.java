package com.chess.engine.board;

/**
 * Represents a position on the board OR a movement vector.
 */
public class Coordinate {

    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Adds the given movement vector to this position and returns the new position.
     * @param vector The vector to add.
     * @return The new position (not guaranteed to be within bounds of the board).
     */
    public Coordinate add(Coordinate vector) {
        return new Coordinate(this.row + vector.row, this.col + vector.col);
    }

    /**
     * Scales this movement vector by the given constant factor and returns the scaled vector.
     * @param factor The factor to scale by.
     * @return The scaled vector.
     */
    public Coordinate scale(int factor) {
        return new Coordinate(this.row * factor, this.col * factor);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Coordinate)) {
            return false;
        }

        Coordinate other = (Coordinate) obj;
        return (this.row == other.row) && (this.col == other.col);
    }

    @Override
    public int hashCode() {
        return 31*row + col;
    }
}
