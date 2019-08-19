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
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a Chinese Chess piece.
 */
public abstract class Piece {

    protected final Alliance alliance;
    private final PieceType pieceType;
    protected Coordinate position;
    private final int hashCode;

    Piece(PieceType pieceType, Coordinate position, Alliance alliance) {
        this.pieceType = pieceType;
        this.position = position;
        this.alliance = alliance;
        hashCode = getHashCode();
    }

    /**
     * Returns a collection of positions reachable by this piece.
     * @param board The board this piece is on.
     * @return A collection of positions reachable by this piece.
     */
    public abstract Collection<Coordinate> getDestPositions(Board board);

    /**
     * Moves this piece based on the given move and returns the new piece.
     * @param move The move made on this piece.
     * @return The new piece after the move is made.
     */
    public abstract Piece movePiece(Move move);

    /**
     * Returns the mirrored version (about the middle column) of this piece.
     * @return the mirrored version of this piece.
     */
    public abstract Piece getMirrorPiece();

    /**
     * Returns a collection of legal moves that can be made by this piece on the given board.
     * @param board The current board.
     * @return a collection of legal moves that can be made by this piece on the given board.
     */
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

    /**
     * Returns the material value of this piece, given the current board status.
     * @param boardStatus The current board status.
     * @return The material value of this piece, given the current board status.
     */
    public int getMaterialValue(BoardStatus boardStatus) {
        if (boardStatus.equals(BoardStatus.OPENING)) {
            return pieceType.openingValue;
        }
        if (boardStatus.equals(BoardStatus.MIDDLE)) {
            return pieceType.midValue;
        }
        return pieceType.endValue;
    }

    /**
     * Returns the positional value of this piece.
     * @return The positional value of this piece.
     */
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
        return Objects.hash(pieceType, alliance, position);
    }

    /**
     * Represents the type of a piece.
     */
    public enum PieceType {

        SOLDIER("S", true,
                100, 150, 200, 30, 3, POSITION_VALUES_SOLDIER),
        ADVISOR("A", false,
                200, 225, 250, 2, 4, POSITION_VALUES_ADVISOR),
        ELEPHANT("E", false,
                200, 225, 250, 2, 4, POSITION_VALUES_ELEPHANT),
        HORSE("H", true,
                450, 500, 550, 24, 2, POSITION_VALUES_HORSE),
        CANNON("C", true,
                500, 525, 550, 10, 2, POSITION_VALUES_CANNON),
        CHARIOT("R", true,
                1000, 1000, 1000, 12, 1, POSITION_VALUES_CHARIOT),
        GENERAL("G", false,
                5000, 5000, 5000, 0, 5, POSITION_VALUES_GENERAL);

        private final String abbrev;
        private final boolean isAttacking;
        private final int openingValue;
        private final int midValue;
        private final int endValue;
        private final int mobilityValue;
        private final int movePriority;
        private final int[][] positionValues;

        PieceType(String abbrev, boolean isAttacking,
                  int openingValue, int midValue, int endValue, int mobilityValue, int movePriority,
                  int[][] positionValues) {
            this.abbrev = abbrev;
            this.isAttacking = isAttacking;
            this.openingValue = openingValue;
            this.midValue = midValue;
            this.endValue = endValue;
            this.mobilityValue = mobilityValue;
            this.movePriority = movePriority;
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

        public int getMovePriority() {
            return movePriority;
        }

        @Override
        public String toString() {
            return abbrev;
        }
    }

    /* The following 2D arrays represent the positional values of each piece on all positions of the board */
    private static int[][] POSITION_VALUES_SOLDIER = {
            {  0,    0,    0,    2,    4,    2,    0,    0,    0},
            { 40,   60,  100,  130,  140,  130,  100,   60,   40},
            { 40,   60,   90,  110,  110,  110,   90,   60,   40},
            { 40,   54,   60,   80,   84,   80,   60,   54,   40},
            { 20,   36,   44,   70,   80,   70,   44,   36,   20},
            {  6,    0,    8,    0,   14,    0,    8,    0,    6},
            { -4,    0,   -4,    0,   12,    0,   -4,    0,   -4},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };
    private static int[][] POSITION_VALUES_ADVISOR = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    6,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };
    private static int[][] POSITION_VALUES_ELEPHANT = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,   -2,    0,    0,    0,   -2,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            { -4,    0,    0,    0,    6,    0,    0,    0,   -4},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };
    private static int[][] POSITION_VALUES_HORSE = {
            {  4,    4,    4,   16,    4,   16,    4,    4,    4},
            {  4,   16,   30,   18,   12,   18,   30,   16,    4},
            {  8,   20,   22,   30,   22,   30,   22,   20,    8},
            { 10,   40,   24,   38,   24,   38,   24,   40,    10},
            {  4,   24,   22,   30,   32,   30,   22,   24,    4},
            {  4,   20,   26,   28,   30,   28,   26,   20,    4},
            {  8,   12,   20,   14,   20,   14,   20,   12,    8},
            { 10,    8,   12,   14,    8,   14,   12,    8,   10},
            { -6,    4,    8,   10,  -20,   10,    8,    4,   -6},
            {  0,  -6,    4,    0,    4,    0,    4,    -6,    0}
    };
    private static int[][] POSITION_VALUES_CANNON = {
            {  8,    8,    0,  -10,  -12,  -10,    0,    8,    8},
            {  4,    4,    0,   -8,  -14,   -8,    0,    4,    4},
            {  2,    2,    0,  -10,   -8,  -10,    0,    2,    2},
            {  0,    6,    6,    4,    8,    4,    6,    0,    0},
            {  0,    0,    0,    0,    8,    0,    0,    0,    0},
            { -2,    0,    6,    0,    8,    0,    6,    0,   -2},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  2,    0,    8,    6,   10,    6,    8,    0,    2},
            {  0,    2,    4,    4,    4,    4,    4,    2,    0},
            {  0,    0,    2,    6,    6,    6,    2,    0,    0}
    };
    private static int[][] POSITION_VALUES_CHARIOT = {
            { 12,   16,   14,   26,   28,   26,   14,   16,   12},
            { 12,   24,   18,   32,   66,   32,   18,   24,   12},
            { 12,   16,   14,   28,   32,   28,   14,   16,   12},
            { 12,   26,   26,   32,   32,   32,   26,   26,   12},
            { 16,   22,   22,   28,   30,   28,   22,   22,   16},
            { 16,   24,   24,   28,   30,   28,   24,   24,   16},
            {  8,   18,    8,   24,   28,   24,    8,   18,    8},
            { -4,   16,    8,   24,   24,   24,    8,   16,   -4},
            { 10,   16,   12,   24,    0,   24,   12,   16,   10},
            {-12,   12,    8,   24,    0,   24,    8,   12,  -12}
    };
    private static int[][] POSITION_VALUES_GENERAL = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,  -18,  -18,  -18,    0,    0,    0},
            {  0,    0,    0,  -16,  -16,  -16,    0,    0,    0},
            {  0,    0,    0,    2,   10,    2,    0,    0,    0}
    };
}
