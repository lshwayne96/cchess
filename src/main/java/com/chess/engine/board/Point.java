package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Point {

    private static final Map<Coordinate, Point> EMPTY_POINTS = getEmptyPoints();

    private final Coordinate position;
    private final Piece piece;

    private Point(Coordinate position, Piece piece) {
        this.position = position;
        this.piece = piece;
    }

    static Point getInstance(Coordinate position, Piece piece) {
        if (piece == null) {
            return getInstance(position);
        }
        return new Point(position, piece);
    }

    private static Point getInstance(Coordinate position) {
        return EMPTY_POINTS.get(position);
    }

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
