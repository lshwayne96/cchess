package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

import java.util.Objects;
import java.util.Optional;

import static com.chess.engine.pieces.Piece.*;

/**
 * Represents a player move made on the board.
 */
public class Move {

    private final long zobristKey;
    private final Piece movedPiece;
    private final Coordinate destPosition;
    private final Piece capturedPiece;

    public Move(long zobristKey, Piece movedPiece, Coordinate destPosition, Piece capturedPiece) {
        this.zobristKey = zobristKey;
        this.movedPiece = movedPiece;
        this.destPosition = destPosition;
        this.capturedPiece = capturedPiece;
    }

    public Move(long zobristKey, Piece movedPiece, Coordinate destPosition) {
        this(zobristKey, movedPiece, destPosition, null);
    }

    public boolean isCapture() {
        return capturedPiece != null;
    }

    /**
     * Returns a move, if any, corresponding to the given string notation.
     * @param board The board to make a move on.
     * @param str The string notation.
     * @return A move, if any, corresponding to the given string notation.
     */
    public static Optional<Move> stringToMove(Board board, String str) {
        if (str.length() != 6) {
            return Optional.empty();
        }

        Alliance alliance = board.getCurrPlayer().getAlliance();
        int formerRow = BoardUtil.rankToRow(charToRank(str.charAt(1)), alliance);
        int formerCol = BoardUtil.fileToCol(Character.getNumericValue(str.charAt(2)), alliance);
        int newRow = BoardUtil.rankToRow(charToRank(str.charAt(4)), alliance);
        int newCol = BoardUtil.fileToCol(Character.getNumericValue(str.charAt(5)), alliance);

        Coordinate srcPosition = new Coordinate(formerRow, formerCol);
        Coordinate destPosition = new Coordinate(newRow, newCol);

        return board.getMove(srcPosition, destPosition);
    }

    /**
     * Returns a string representation of a piece with the given type and alliance.
     */
    private static String getPieceAbbrev(PieceType pieceType, Alliance alliance) {
        return alliance.isRed() ? pieceType.toString() : pieceType.toString().toLowerCase();
    }

    /**
     * Returns a string representation of the given rank.
     */
    private static String rankToString(int rank) {
        return rank < 10 ? Integer.toString(rank) : "X";
    }

    /**
     * Returns a rank number corresponding to the given character.
     */
    private static int charToRank(char rank) {
        return rank == 'X' ? 10 : Character.getNumericValue(rank);
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Coordinate getDestPosition() {
        return destPosition;
    }

    public Optional<Piece> getCapturedPiece() {
        return Optional.ofNullable(capturedPiece);
    }

    /**
     * Returns a string notation representing this move.
     * The notation follows the format: [piece abbr][former rank][former file]-[new rank][new file];
     * rank 10 is represented by "X"
     */
    @Override
    public String toString() {
        Coordinate srcPosition = movedPiece.getPosition();
        Alliance alliance = movedPiece.getAlliance();
        PieceType pieceType = movedPiece.getPieceType();

        String formerRank = rankToString(BoardUtil.rowToRank(srcPosition.getRow(), alliance));
        String formerFile = Integer.toString(BoardUtil.colToFile(srcPosition.getCol(), alliance));
        String newRank = rankToString(BoardUtil.rowToRank(destPosition.getRow(), alliance));
        String newFile = Integer.toString(BoardUtil.colToFile(destPosition.getCol(), alliance));

        return getPieceAbbrev(pieceType, alliance) +
                formerRank + formerFile +
                "-" +
                newRank + newFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Move)) {
            return false;
        }
        Move other = (Move) obj;
        return this.zobristKey == other.zobristKey
                && this.movedPiece.equals(other.movedPiece)
                && this.destPosition.equals(other.destPosition)
                && this.getCapturedPiece().equals(other.getCapturedPiece());
    }

    @Override
    public int hashCode() {
        return Objects.hash(zobristKey, movedPiece, destPosition, getCapturedPiece());
    }
}
