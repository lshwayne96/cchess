package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Point {

    /* A map containing all possible empty points on the board */
    private static final Map<Coordinate, Point> EMPTY_POINTS = getEmptyPoints();

    private final Coordinate position; // the position of this point on the board
    private final Piece piece; // the piece (if any) on this point

    private Point(Coordinate position, Piece piece) {
        this.position = position;
        this.piece = piece;
    }

    /**
     * Returns an instance of a point having the given position and piece.
     * @param position The position of the point.
     * @param piece The piece on the point.
     * @return An instance of a point having the given position and piece.
     */
    static Point getInstance(Coordinate position, Piece piece) {
        if (piece == null) {
            return getInstance(position);
        }
        return new Point(position, piece);
    }

    /**
     * Returns an instance of an empty point having the given position.
     */
    private static Point getInstance(Coordinate position) {
        return EMPTY_POINTS.get(position);
    }

    /**
     * Returns a map containing all possible empty points on the board.
     */
    private static Map<Coordinate, Point> getEmptyPoints() {
        final Map<Coordinate, Point> emptyPointMap = new HashMap<>();

        for (int row = 0; row < Board.NUM_ROWS; row++) {
            for (int col = 0; col < Board.NUM_COLS; col++) {
                Coordinate position = new Coordinate(row, col);
                emptyPointMap.put(position, new Point(position, null));
            }
        }

        return Collections.unmodifiableMap(emptyPointMap);
    }

    /**
     * Checks if this point has no piece.
     * @return true if this point has no piece, false otherwise.
     */
    public boolean isEmpty() {
        return piece == null;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Optional<Piece> getPiece() {
        return Optional.ofNullable(piece);
    }

    @Override
    public String toString() {
        if (piece == null) {
            return "-";
        }
        if (piece.getAlliance().isRed()) {
            return piece.toString();
        } else {
            return piece.toString().toLowerCase();
        }
    }
}
