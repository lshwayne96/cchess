package main.java.com.chess.engine.board;

public class Coordinate {

    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Coordinate)) {
            return false;
        }
        Coordinate c = (Coordinate) o;
        return (c.row == this.row) && (c.col == this.col);
    }

    @Override
    public int hashCode() {
        return 31*row + col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Coordinate add(Coordinate vector) {
        return new Coordinate(this.row + vector.row, this.col + vector.col);
    }

    public Coordinate scale(int factor) {
        return new Coordinate(this.row * factor, this.col * factor);
    }
}
