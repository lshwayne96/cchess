package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Board.BoardStatus;
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

    public int getMaterialValue(BoardStatus boardStatus) {
        if (boardStatus.equals(BoardStatus.OPENING)) {
            return type.openingValue;
        }
        if (boardStatus.equals(BoardStatus.MIDDLE)) {
            return type.midValue;
        }
        return type.endValue;
    }

    public int getPositionValue() {
        return alliance.isRed() ? type.positionValues[position.getRow()][position.getCol()]
                : type.positionValues[Board.NUM_ROWS - position.getRow() - 1][Board.NUM_COLS - position.getCol() - 1];
    }

    public abstract Collection<Move> calculateLegalMoves(Board board);

    public abstract Piece movePiece(Move move);

    public enum PieceType {

        SOLDIER("S", 100, 150, 300, Board.POSITION_VALUES_SOLDIER),
        ADVISOR("A", 150, 200, 250, Board.POSITION_VALUES_ADVISOR),
        ELEPHANT("E", 200, 250, 300, Board.POSITION_VALUES_ELEPHANT),
        HORSE("H", 400, 500, 600, Board.POSITION_VALUES_HORSE),
        CANNON("C", 500, 500, 500, Board.POSITION_VALUES_CANNON),
        CHARIOT("R", 1000, 1000, 1000, Board.POSITION_VALUES_CHARIOT),
        GENERAL("G", 5000, 5000, 5000, Board.POSITION_VALUES_GENERAL);

        private final String abbrev;
        private final int openingValue;
        private final int midValue;
        private final int endValue;
        private final int[][] positionValues;

        PieceType(String abbrev, int openingValue, int midValue, int endValue, int[][] positionValues) {
            this.abbrev = abbrev;
            this.openingValue = openingValue;
            this.midValue = midValue;
            this.endValue = endValue;
            this.positionValues = positionValues;
        }

        @Override
        public String toString() {
            return abbrev;
        }

        public int getDefaultValue() {
            return openingValue;
        }
    }
}
