package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.util.Collection;

public abstract class Piece {

    protected final PieceType type;
    protected final Coordinate position;
    protected final Alliance alliance;
    private final int hashCode;

    Piece(PieceType type, Coordinate position, Alliance alliance) {
        this.type = type;
        this.position = position;
        this.alliance = alliance;
        hashCode = computeHashCode();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;

        Piece piece = (Piece) o;
        return (this.position.equals(piece.position)) && (this.alliance == piece.alliance)
                && (this.type == piece.type);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public PieceType getType() {
        return type;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    private int computeHashCode() {
        int result = type.hashCode();
        result = 31*result + alliance.hashCode();
        result = 31*result + position.hashCode();
        return result;
    }

    public abstract double getValue();

    public abstract Collection<Move> calculateLegalMoves(Board board);

    public abstract Piece movePiece(Move move);


    public enum PieceType {
        SOLDIER("S"),
        ADVISOR("A"),
        ELEPHANT("E"),
        HORSE("H"),
        CANNON("C"),
        CHARIOT("R"),
        GENERAL("G");

        private String abbr;

        PieceType(String abbr) {
            this.abbr = abbr;
        }

        @Override
        public String toString() {
            return abbr;
        }
    }
}
