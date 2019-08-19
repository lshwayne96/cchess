package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

import java.util.Objects;
import java.util.Optional;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

/**
 * Represents a player move made on the board.
 */
public class Move {

    private final Board board;
    private final Piece movedPiece;
    private final Coordinate destPosition;
    private final Piece capturedPiece;

    public Move(Board board, Piece movedPiece, Coordinate destPosition, Piece capturedPiece) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destPosition = destPosition;
        this.capturedPiece = capturedPiece;
    }

    public Move(Board board, Piece movedPiece, Coordinate destPosition) {
        this(board, movedPiece, destPosition, null);
    }

    /**
     * Returns the mirrored equivalent (about the middle column) of this move
     * @return The mirrored equivalent of this move.
     */
    public Move getMirroredMove() {
        int srcRow = movedPiece.getPosition().getRow();
        int srcCol = Board.NUM_COLS - 1 - movedPiece.getPosition().getCol();
        Coordinate mirroredSrcPosition = new Coordinate(srcRow, srcCol);

        int destRow = destPosition.getRow();
        int destCol = Board.NUM_COLS - 1 - destPosition.getCol();
        Coordinate mirroredDestPosition = new Coordinate(destRow, destCol);

        return board.getMirrorBoard().getMove(mirroredSrcPosition, mirroredDestPosition).get();
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
        int formerRow = rankToRow(charToRank(str.charAt(1)), alliance);
        int formerCol = fileToCol(Character.getNumericValue(str.charAt(2)), alliance);
        int newRow = rankToRow(charToRank(str.charAt(4)), alliance);
        int newCol = fileToCol(Character.getNumericValue(str.charAt(5)), alliance);

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
     * Converts a column number to its corresponding file number based on the given alliance.
     */
    private static int colToFile(int col, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - col : col + 1;
    }

    /**
     * Converts a file number to its corresponding column number based on the given alliance.
     */
    private static int fileToCol(int file, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - file : file - 1;
    }

    /**
     * Converts a row number to its corresponding rank number based on the given alliance.
     */
    private static int rowToRank(int row, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - row : row + 1;
    }

    /**
     * Converts rank number to its corresponding row number based on the given alliance.
     */
    private static int rankToRow(int rank, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - rank : rank - 1;
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

        String formerRank = rankToString(rowToRank(srcPosition.getRow(), alliance));
        String formerFile = Integer.toString(colToFile(srcPosition.getCol(), alliance));
        String newRank = rankToString(rowToRank(destPosition.getRow(), alliance));
        String newFile = Integer.toString(colToFile(destPosition.getCol(), alliance));

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
        return this.movedPiece.equals(other.movedPiece)
                && this.destPosition.equals(other.destPosition)
                && this.getCapturedPiece().equals(other.getCapturedPiece())
                && this.board.toString().equals(other.board.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(movedPiece, destPosition, getCapturedPiece(), board.toString());
    }
}
