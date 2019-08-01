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

    private final PieceType pieceType;
    protected final Coordinate position;
    protected final Alliance alliance;
    private final int hashCode;

    Piece(PieceType pieceType, Coordinate position, Alliance alliance) {
        this.pieceType = pieceType;
        this.position = position;
        this.alliance = alliance;
        hashCode = getHashCode();
    }

    public abstract Collection<Coordinate> getDestPositions(Board board);

    public abstract Piece movePiece(Move move);

    public abstract Piece getMirrorPiece();

    public Collection<Move> getLegalMoves(Board board) {
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

        return Collections.unmodifiableList(legalMoves);
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public int getMaterialValue(BoardStatus boardStatus) {
        if (boardStatus.equals(BoardStatus.OPENING)) {
            return pieceType.openingValue;
        }
        if (boardStatus.equals(BoardStatus.MIDDLE)) {
            return pieceType.midValue;
        }
        return pieceType.endValue;
    }

    public int getPositionValue() {
        return alliance.isRed() ? pieceType.positionValues[position.getRow()][position.getCol()]
                : pieceType.positionValues[Board.NUM_ROWS - position.getRow() - 1][Board.NUM_COLS - position.getCol() - 1];
    }

    @Override
    public String toString() {
        return pieceType.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Piece)) {
            return false;
        }

        Piece other = (Piece) obj;
        return this.position.equals(other.position)
                && this.alliance.equals(other.alliance)
                && this.pieceType.equals(other.pieceType);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int getHashCode() {
        int result = pieceType.hashCode();
        result = 31*result + alliance.hashCode();
        result = 31*result + position.hashCode();

        return result;
    }

    public enum PieceType {

        SOLDIER("S", true,
                100, 200, 300, 30, Board.POSITION_VALUES_SOLDIER),
        ADVISOR("A", false,
                150, 200, 250, 2, Board.POSITION_VALUES_ADVISOR),
        ELEPHANT("E", false,
                200, 250, 300, 2, Board.POSITION_VALUES_ELEPHANT),
        HORSE("H", true,
                450, 500, 550, 24, Board.POSITION_VALUES_HORSE),
        CANNON("C", true,
                500, 500, 500, 10, Board.POSITION_VALUES_CANNON),
        CHARIOT("R", true,
                1000, 1000, 1000, 12, Board.POSITION_VALUES_CHARIOT),
        GENERAL("G", false,
                5000, 5000, 5000, 0, Board.POSITION_VALUES_GENERAL);

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

        public boolean isAttacking() {
            return isAttacking;
        }

        public int getDefaultValue() {
            return openingValue;
        }

        public int getMobilityValue() {
            return mobilityValue;
        }

        @Override
        public String toString() {
            return abbrev;
        }
    }
}
