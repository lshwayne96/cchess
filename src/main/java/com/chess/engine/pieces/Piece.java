package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Board.BoardStatus;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate destPosition : getDestPositions(board)) {
            Point destPoint = board.getPoint(destPosition);
            Optional<Piece> destPiece = destPoint.getPiece();
            destPiece.ifPresentOrElse(p -> {
                if (this.alliance != p.alliance) {
                    legalMoves.add(new Move(board, this, destPosition, p));
                }
            }, () -> legalMoves.add(new Move(board, this, destPosition)));
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    public abstract Collection<Coordinate> getDestPositions(Board board);

    public abstract Piece movePiece(Move move);

    public enum PieceType {

        SOLDIER("S", true, 100, 200, 300, 30, Board.POSITION_VALUES_SOLDIER),
        ADVISOR("A", false, 150, 200, 250, 2, Board.POSITION_VALUES_ADVISOR),
        ELEPHANT("E", false, 200, 250, 300, 2, Board.POSITION_VALUES_ELEPHANT),
        HORSE("H", true, 450, 500, 550, 24, Board.POSITION_VALUES_HORSE),
        CANNON("C", true, 500, 500, 500, 10, Board.POSITION_VALUES_CANNON),
        CHARIOT("R", true, 1000, 1000, 1000, 12, Board.POSITION_VALUES_CHARIOT),
        GENERAL("G", false, 5000, 5000, 5000, 0, Board.POSITION_VALUES_GENERAL);

        private final String abbrev;
        private final boolean isAttacking;
        private final int openingValue;
        private final int midValue;
        private final int endValue;
        private final int mobilityValue;
        private final int[][] positionValues;

        PieceType(String abbrev, boolean isAttacking,
                  int openingValue, int midValue, int endValue, int mobilityValue,
                  int[][] positionValues) {
            this.abbrev = abbrev;
            this.isAttacking = isAttacking;
            this.openingValue = openingValue;
            this.midValue = midValue;
            this.endValue = endValue;
            this.mobilityValue = mobilityValue;
            this.positionValues = positionValues;
        }

        @Override
        public String toString() {
            return abbrev;
        }

        public int getDefaultValue() {
            return openingValue;
        }

        public int getMobilityValue() {
            return mobilityValue;
        }

        public boolean isAttacking() {
            return isAttacking;
        }
    }
}
