package com.chess.engine.board;

public class Coordinate {

    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Coordinate add(Coordinate vector) {
        return new Coordinate(this.row + vector.row, this.col + vector.col);
    }

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
