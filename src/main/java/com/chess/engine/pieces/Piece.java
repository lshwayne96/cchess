package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.util.Collection;
import java.util.Objects;

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
     * Returns a collection of legal moves that can be made by this piece on the given board.
     * @param board The current board.
     * @return a collection of legal moves that can be made by this piece on the given board.
     */
    public abstract Collection<Move> getLegalMoves(Board board);

    /**
     * Returns a collection of legal moves that can be made by this piece on the given board.
     * Adds this piece's attacks and defenses to the given respective collections.
     * @param board The current board.
     * @param attacks The collection of attacks to add to.
     * @param defenses The collection of defenses to add to.
     * @return a collection of legal moves that can be made by this piece on the given board.
     */
    public abstract Collection<Move> getLegalMoves(Board board, Collection<Attack> attacks, Collection<Defense> defenses);

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
     * Returns the combined material and positional value of this piece during midgame.
     * @return The combined material and positional value of this piece during midgame.
     */
    public int getMidgameValue() {
        return alliance.isRed() ? pieceType.midGameValues[position.getRow()][position.getCol()]
                : pieceType.midGameValues[Board.NUM_ROWS-position.getRow()-1][Board.NUM_COLS-position.getCol()-1];
    }

    /**
     * Returns the combined material and positional value of this piece during endgame.
     * @return The combined material and positional value of this piece during endgame.
     */
    public int getEndgameValue() {
        return alliance.isRed() ? pieceType.endGameValues[position.getRow()][position.getCol()]
                : pieceType.endGameValues[Board.NUM_ROWS-position.getRow()-1][Board.NUM_COLS-position.getCol()-1];
    }

    /**
     * Checks if this piece has crossed its side of the river.
     * @return true if this piece has crossed its side of the river, false otherwise.
     */
    public boolean crossedRiver() {
        return alliance.isRed() ? position.getRow() < Board.RIVER_ROW_RED : position.getRow() > Board.RIVER_ROW_BLACK;
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

        SOLDIER("S", MIDGAME_VALUES_SOLDIER, ENDGAME_VALUES_SOLDIER,
                1, 3, 0, 1, 1),
        ADVISOR("A", VALUES_ADVISOR, VALUES_ADVISOR,
                0, 3, 0, 0, 1),
        ELEPHANT("E", VALUES_ELEPHANT, VALUES_ELEPHANT,
                0, 3, 0, 0, 1),
        HORSE("H", MIDGAME_VALUES_HORSE, ENDGAME_VALUES_HORSE,
                6, 2, 1, 2, 3),
        CANNON("C", MIDGAME_VALUES_CANNON, ENDGAME_VALUES_CANNON,
                1, 2, 1, 1, 3),
        CHARIOT("R", MIDGAME_VALUES_CHARIOT, ENDGAME_VALUES_CHARIOT,
                3, 1, 2, 2, 6),
        GENERAL("G", MIDGAME_VALUES_GENERAL, ENDGAME_VALUES_GENERAL,
                0, 4, 0, 0, 0);

        private final String abbrev;
        private final int[][] midGameValues;
        private final int[][] endGameValues;
        private final int mobilityValue;
        private final int movePriority;
        private final int valueUnits;
        private final int attackUnits;
        private final int simpleUnits;

        PieceType(String abbrev, int[][] midGameValues, int[][] endGameValues,
                  int mobilityValue, int movePriority, int valueUnits, int attackUnits, int simpleUnits) {
            this.abbrev = abbrev;
            this.midGameValues = midGameValues;
            this.endGameValues = endGameValues;
            this.mobilityValue = mobilityValue;
            this.movePriority = movePriority;
            this.valueUnits = valueUnits;
            this.attackUnits = attackUnits;
            this.simpleUnits = simpleUnits;
        }

        public boolean isAttacking() {
            return attackUnits > 0;
        }

        public int getMobilityValue() {
            return mobilityValue;
        }

        public int getMovePriority() {
            return movePriority;
        }

        public int getValueUnits() {
            return valueUnits;
        }

        public int getAttackUnits() {
            return attackUnits;
        }

        public int getSimpleUnits() {
            return simpleUnits;
        }

        @Override
        public String toString() {
            return abbrev;
        }
    }

    /* The following 2D arrays represent the combined material and positional values of each piece
       for midgame and endgame on all positions of the board */
    private static int[][] MIDGAME_VALUES_SOLDIER = {
            {  45,  45,  45,  55,  65,  55,  45,  45,  45},
            { 145, 180, 255, 315, 330, 315, 255, 180, 145},
            { 145, 180, 240, 275, 275, 275, 240, 180, 145},
            { 145, 170, 200, 220, 225, 220, 200, 170, 145},
            { 105, 135, 150, 200, 220, 200, 150, 135, 105},
            {  35,   0,  65,   0,  80,   0,  65,   0,  35},
            {  35,   0,  35,   0,  75,   0,  35,   0,  35},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}
    };
    private static int[][] ENDGAME_VALUES_SOLDIER = {
            {  50,  50,  50,  75,  75,  75,  50,  50,  50},
            { 150, 175, 200, 325, 400, 325, 200, 175, 150},
            { 225, 250, 250, 275, 275, 275, 250, 250, 225},
            { 275, 300, 300, 310, 310, 310, 300, 300, 275},
            { 235, 250, 235, 260, 260, 260, 235, 250, 235},
            { 175,   0, 160,   0, 175,   0, 160,   0, 175},
            { 150,   0, 135,   0, 150,   0, 135,   0, 150},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}
    };
    private static int[][] VALUES_ADVISOR = {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0, 150,   0, 150,   0,   0,   0},
            {   0,   0,   0,   0, 165,   0,   0,   0,   0},
            {   0,   0,   0, 150,   0, 150,   0,   0,   0}
    };
    private static int[][] VALUES_ELEPHANT = {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0, 150,   0,   0,   0, 150,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            { 140,   0,   0,   0, 165,   0,   0,   0, 140},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0, 150,   0,   0,   0, 150,   0,   0}
    };
    private static int[][] MIDGAME_VALUES_HORSE = {
            { 450, 450, 450, 480, 450, 480, 450, 450, 450},
            { 450, 480, 515, 485, 470, 485, 515, 480, 450},
            { 460, 490, 495, 515, 495, 515, 495, 490, 460},
            { 465, 540, 500, 535, 500, 535, 500, 540, 465},
            { 450, 500, 495, 515, 520, 515, 495, 500, 450},
            { 450, 490, 505, 510, 515, 510, 505, 490, 450},
            { 460, 470, 490, 475, 490, 475, 490, 470, 460},
            { 465, 460, 470, 475, 460, 475, 470, 460, 465},
            { 425, 450, 460, 465, 390, 465, 460, 450, 425},
            { 440, 425, 450, 440, 450, 440, 450, 425, 440}
    };
    private static int[][] ENDGAME_VALUES_HORSE = {
            { 460, 470, 480, 480, 480, 480, 480, 470, 460},
            { 470, 480, 490, 490, 490, 490, 490, 480, 470},
            { 480, 490, 500, 500, 500, 500, 500, 490, 480},
            { 480, 490, 500, 500, 500, 500, 500, 490, 480},
            { 480, 490, 500, 500, 500, 500, 500, 490, 480},
            { 470, 480, 490, 490, 490, 490, 490, 480, 470},
            { 470, 480, 490, 490, 490, 490, 490, 480, 470},
            { 460, 470, 480, 480, 480, 480, 480, 470, 460},
            { 450, 460, 470, 460, 460, 460, 470, 460, 450},
            { 440, 450, 460, 450, 450, 450, 460, 450, 440}
    };
    private static int[][] MIDGAME_VALUES_CANNON = {
            { 500, 500, 480, 455, 450, 455, 480, 500, 500},
            { 490, 490, 480, 460, 445, 460, 480, 490, 490},
            { 485, 485, 480, 455, 460, 455, 480, 485, 485},
            { 480, 495, 495, 490, 500, 490, 495, 495, 480},
            { 480, 480, 480, 480, 500, 480, 480, 480, 480},
            { 475, 480, 495, 480, 500, 480, 495, 480, 475},
            { 480, 480, 480, 480, 480, 480, 480, 480, 480},
            { 485, 480, 500, 495, 505, 495, 500, 480, 485},
            { 480, 485, 490, 490, 490, 490, 490, 485, 480},
            { 480, 480, 485, 495, 495, 495, 485, 480, 480}
    };
    private static int[][] ENDGAME_VALUES_CANNON = {
            { 500, 500, 500, 500, 500, 500, 500, 500, 500},
            { 500, 500, 500, 500, 500, 500, 500, 500, 500},
            { 500, 500, 500, 500, 500, 500, 500, 500, 500},
            { 500, 500, 500, 510, 520, 510, 500, 500, 500},
            { 500, 500, 500, 510, 520, 510, 500, 500, 500},
            { 500, 500, 500, 510, 520, 510, 500, 500, 500},
            { 500, 500, 500, 510, 520, 510, 500, 500, 500},
            { 500, 500, 500, 510, 520, 510, 500, 500, 500},
            { 500, 500, 500, 520, 530, 520, 500, 500, 500},
            { 500, 500, 500, 520, 530, 520, 500, 500, 500},
    };
    private static int[][] MIDGAME_VALUES_CHARIOT = {
            {1030,1040,1035,1065,1070,1065,1035,1040,1030},
            {1030,1060,1045,1080,1165,1080,1045,1060,1030},
            {1030,1040,1035,1070,1080,1070,1035,1040,1030},
            {1030,1065,1065,1080,1080,1080,1065,1065,1030},
            {1040,1055,1055,1070,1075,1070,1055,1055,1040},
            {1040,1060,1060,1070,1075,1070,1060,1060,1040},
            {1020,1045,1020,1060,1070,1060,1020,1045,1020},
            { 990,1040,1020,1060,1060,1060,1020,1040, 990},
            {1000,1040,1030,1060,1000,1060,1030,1040,1000},
            { 970,1030,1020,1060,1000,1060,1020,1030, 970}
    };
    private static int[][] ENDGAME_VALUES_CHARIOT = {
            { 910, 910, 910, 920, 930, 920, 910, 910, 910},
            { 920, 920, 920, 930, 950, 930, 920, 920, 920},
            { 910, 910, 910, 920, 930, 920, 910, 910, 910},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900},
            { 900, 900, 900, 910, 920, 910, 900, 900, 900}
    };
    private static int[][] MIDGAME_VALUES_GENERAL = {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   5,   5,   5,   0,   0,   0},
            {   0,   0,   0,  10,  10,  10,   0,   0,   0},
            {   0,   0,   0,  55,  75,  55,   0,   0,   0}
    };
    private static int[][] ENDGAME_VALUES_GENERAL = {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,  25,  70,  25,   0,   0,   0},
            {   0,   0,   0,  15,  60,  15,   0,   0,   0},
            {   0,   0,   0,   5,  55,   5,   0,   0,   0}
    };

    public static abstract class Relation {

        final Piece piece;
        final Collection<Piece> relatedPieces;

        private Relation(Piece piece, Collection<Piece> relatedPieces) {
            this.piece = piece;
            this.relatedPieces = relatedPieces;
        }

        public Piece getPiece() {
            return piece;
        }

        public Collection<Piece> getRelatedPieces() {
            return relatedPieces;
        }
    }

    /**
     * Represents the attacks by a piece.
     */
    public static class Attack extends Relation {

        public Attack(Piece attackingPiece, Collection<Piece> attackedPieces) {
            super(attackingPiece, attackedPieces);
        }
    }

    /**
     * Represents the defenses by a piece.
     */
    public static class Defense extends Relation {

        public Defense(Piece defendingPiece, Collection<Piece> defendedPieces) {
            super(defendingPiece, defendedPieces);
        }
    }
}
