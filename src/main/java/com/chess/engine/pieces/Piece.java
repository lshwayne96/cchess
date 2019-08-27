package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
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
    protected final Coordinate position;
    private final PieceType pieceType;
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
     * Returns a mirrored copy (about the middle column) of this piece.
     * @return A mirrored copy of this piece.
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
                    legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                }
            }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
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
     * Returns the combined material and positional value of this piece given the current board status.
     * @param isEndGame Whether the board is currently in endgame.
     * @return The combined material and positional value of this piece given the current board status.
     */
    public int getValue(boolean isEndGame) {
        if (isEndGame) {
            return alliance.isRed() ? pieceType.endGameValues[position.getRow()][position.getCol()]
                    : pieceType.endGameValues[Board.NUM_ROWS-position.getRow()-1][Board.NUM_COLS-position.getCol()-1];
        } else {
            return alliance.isRed() ? pieceType.midGameValues[position.getRow()][position.getCol()]
                    : pieceType.midGameValues[Board.NUM_ROWS-position.getRow()-1][Board.NUM_COLS-position.getCol()-1];
        }
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

        SOLDIER("S", true, MIDGAME_VALUES_SOLDIER, ENDGAME_VALUES_SOLDIER, 5, 3, 0),
        ADVISOR("A", false, VALUES_ADVISOR, VALUES_ADVISOR, 0, 4, 0),
        ELEPHANT("E", false, VALUES_ELEPHANT, VALUES_ELEPHANT, 0, 4, 0),
        HORSE("H", true, MIDGAME_VALUES_HORSE, ENDGAME_VALUES_HORSE, 4, 2, 1),
        CANNON("C", true, MIDGAME_VALUES_CANNON, ENDGAME_VALUES_CANNON, 1, 2, 1),
        CHARIOT("R", true, MIDGAME_VALUES_CHARIOT, ENDGAME_VALUES_CHARIOT, 2, 1, 2),
        GENERAL("G", false, MIDGAME_VALUES_GENERAL, ENDGAME_VALUES_GENERAL, 0, 5, 0);

        public static final PieceType[] pieceTypes = PieceType.values();

        private final String abbrev;
        private final boolean isAttacking; // to determine draw
        private final int[][] midGameValues;
        private final int[][] endGameValues;
        private final int mobilityValue;
        private final int movePriority;
        private final int attackingUnits; // to determine endgame

        PieceType(String abbrev, boolean isAttacking, int[][] midGameValues, int[][] endGameValues,
                  int mobilityValue, int movePriority, int attackingUnits) {
            this.abbrev = abbrev;
            this.isAttacking = isAttacking;
            this.midGameValues = midGameValues;
            this.endGameValues = endGameValues;
            this.mobilityValue = mobilityValue;
            this.movePriority = movePriority;
            this.attackingUnits = attackingUnits;
        }

        public boolean isAttacking() {
            return isAttacking;
        }

        public int getMobilityValue() {
            return mobilityValue;
        }

        public int getMovePriority() {
            return movePriority;
        }

        public int getAttackingUnits() {
            return attackingUnits;
        }

        @Override
        public String toString() {
            return abbrev;
        }
    }

    /* The following 2D arrays represent the combined material and positional values of each piece
       for midgame and endgame on all positions of the board (adapted from EleEye) */
    private static int[][] MIDGAME_VALUES_SOLDIER = {
            {  9,  9,  9, 11, 13, 11,  9,  9,  9},
            { 19, 24, 34, 42, 44, 42, 34, 24, 19},
            { 19, 24, 32, 37, 37, 37, 32, 24, 19},
            { 19, 23, 27, 29, 30, 29, 27, 23, 19},
            { 14, 18, 20, 27, 29, 27, 20, 18, 14},
            {  7,  0, 13,  0, 16,  0, 13,  0,  7},
            {  7,  0,  7,  0, 15,  0,  7,  0,  7},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0}
    };
    private static int[][] ENDGAME_VALUES_SOLDIER = {
            { 10, 10, 10, 15, 15, 15, 10, 10, 10},
            { 10, 15, 20, 45, 60, 45, 20, 15, 10},
            { 25, 30, 30, 35, 35, 35, 30, 30, 25},
            { 35, 40, 40, 45, 45, 45, 40, 40, 35},
            { 25, 30, 30, 35, 35, 35, 30, 30, 25},
            { 25,  0, 25,  0, 25,  0, 25,  0, 25},
            { 20,  0, 20,  0, 20,  0, 20,  0, 20},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0}
    };
    private static int[][] VALUES_ADVISOR = {
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0, 30,  0, 30,  0,  0,  0},
            {  0,  0,  0,  0, 33,  0,  0,  0,  0},
            {  0,  0,  0, 30,  0, 30,  0,  0,  0}
    };
    private static int[][] VALUES_ELEPHANT = {
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0, 30,  0,  0,  0, 30,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            { 28,  0,  0,  0, 33,  0,  0,  0, 28},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0, 30,  0,  0,  0, 30,  0,  0}
    };
    private static int[][] MIDGAME_VALUES_HORSE = {
            { 90, 90, 90, 96, 90, 96, 90, 90, 90},
            { 90, 96,103, 97, 94, 97,103, 96, 90},
            { 92, 98, 99,103, 99,103, 99, 98, 92},
            { 93,108,100,107,100,107,100,108, 93},
            { 90,100, 99,103,104,103, 99,100, 90},
            { 90, 98,101,102,103,102,101, 98, 90},
            { 92, 94, 98, 95, 98, 95, 98, 94, 92},
            { 93, 92, 94, 95, 92, 95, 94, 92, 93},
            { 85, 90, 92, 93, 78, 93, 92, 90, 85},
            { 88, 85, 90, 88, 90, 88, 90, 85, 88}
    };
    private static int[][] ENDGAME_VALUES_HORSE = {
            { 92, 94, 96, 96, 96, 96, 96, 94, 92},
            { 94, 96, 98, 98, 98, 98, 98, 96, 94},
            { 96, 98,100,100,100,100,100, 98, 96},
            { 96, 98,100,100,100,100,100, 98, 96},
            { 96, 98,100,100,100,100,100, 98, 96},
            { 94, 96, 98, 98, 98, 98, 98, 96, 94},
            { 94, 96, 98, 98, 98, 98, 98, 96, 94},
            { 92, 94, 96, 96, 96, 96, 96, 94, 92},
            { 90, 92, 94, 92, 92, 92, 94, 92, 90},
            { 88, 90, 92, 90, 90, 90, 92, 90, 88}
    };
    private static int[][] MIDGAME_VALUES_CANNON = {
            {100,100, 96, 91, 90, 91, 96,100,100},
            { 98, 98, 96, 92, 89, 92, 96, 98, 98},
            { 97, 97, 96, 91, 92, 91, 96, 97, 97},
            { 96, 99, 99, 98,100, 98, 99, 99, 96},
            { 96, 96, 96, 96,100, 96, 96, 96, 96},
            { 95, 96, 99, 96,100, 96, 99, 96, 95},
            { 96, 96, 96, 96, 96, 96, 96, 96, 96},
            { 97, 96,100, 99,101, 99,100, 96, 97},
            { 96, 97, 98, 98, 98, 98, 98, 97, 96},
            { 96, 96, 97, 99, 99, 99, 97, 96, 96}
    };
    private static int[][] ENDGAME_VALUES_CANNON = {
            {100,100,100,100,100,100,100,100,100},
            {100,100,100,100,100,100,100,100,100},
            {100,100,100,100,100,100,100,100,100},
            {100,100,100,102,104,102,100,100,100},
            {100,100,100,102,104,102,100,100,100},
            {100,100,100,102,104,102,100,100,100},
            {100,100,100,102,104,102,100,100,100},
            {100,100,100,102,104,102,100,100,100},
            {100,100,100,104,106,104,100,100,100},
            {100,100,100,104,106,104,100,100,100}
    };
    private static int[][] MIDGAME_VALUES_CHARIOT = {
            {206,208,207,213,214,213,207,208,206},
            {206,212,209,216,233,216,209,212,206},
            {206,208,207,214,216,214,207,208,206},
            {206,213,213,216,216,216,213,213,206},
            {208,211,211,214,215,214,211,211,208},
            {208,212,212,214,215,214,212,212,208},
            {204,209,204,212,214,212,204,209,204},
            {198,208,204,212,212,212,204,208,198},
            {200,208,206,212,200,212,206,208,200},
            {194,206,204,212,200,212,204,206,194}
    };
    private static int[][] ENDGAME_VALUES_CHARIOT = {
            {182,182,182,184,186,184,182,182,182},
            {184,184,184,186,190,186,184,184,184},
            {182,182,182,184,186,184,182,182,182},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180},
            {180,180,180,182,184,182,180,180,180}
    };
    private static int[][] MIDGAME_VALUES_GENERAL = {
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  1,  1,  1,  0,  0,  0},
            {  0,  0,  0,  2,  2,  2,  0,  0,  0},
            {  0,  0,  0, 11, 15, 11,  0,  0,  0}
    };
    private static int[][] ENDGAME_VALUES_GENERAL = {
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  5, 13,  5,  0,  0,  0},
            {  0,  0,  0,  3, 12,  3,  0,  0,  0},
            {  0,  0,  0,  1, 11,  1,  0,  0,  0}
    };
}
