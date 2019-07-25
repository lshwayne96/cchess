package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Point {

    private final Coordinate position;
    private final Piece piece;

    private static final Map<Coordinate, Point> EMPTY_POINTS = getAllEmptyPoints();

    private Point(Coordinate position, Piece piece) {
        this.position = position;
        this.piece = piece;
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

    public Coordinate getPosition() {
        return position;
    }

    public Optional<Piece> getPiece() {
        return Optional.ofNullable(piece);
    }

    public boolean isEmpty() {
        return piece == null;
    }

    public static Point getInstance(Coordinate position) {
        return EMPTY_POINTS.get(position);
    }

    public static Point getInstance(Coordinate position, Piece piece) {
        return new Point(position, piece);
    }

    private static Map<Coordinate, Point> getAllEmptyPoints() {
        final Map<Coordinate, Point> emptyPointMap = new HashMap<>();

        for (int row = 0; row < Board.NUM_ROWS; row++) {
            for (int col = 0; col < Board.NUM_COLS; col++) {
                Coordinate position = new Coordinate(row, col);
                emptyPointMap.put(position, new Point(position, null));
            }
        }

        return Collections.unmodifiableMap(emptyPointMap);
    }
}
