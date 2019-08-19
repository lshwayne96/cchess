package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

import java.util.Optional;

public class Point {

    private final Coordinate position;
    private Piece piece;

    Point(Coordinate position) {
        this.position = position;
    }

    void setPiece(Piece piece) {
        this.piece = piece;
    }

    void removePiece() {
        piece = null;
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
