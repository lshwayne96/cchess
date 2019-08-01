package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

import java.util.Optional;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

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

    public Board execute() {
        Builder builder = new Builder();

        for (Piece piece : board.getCurrPlayer().getActivePieces()) {
            if (!movedPiece.equals(piece)) {
                builder.putPiece(piece);
            }
        }
        for (Piece piece : board.getCurrPlayer().getOpponent().getActivePieces()) {
            builder.putPiece(piece);
        }
        builder.putPiece(movedPiece.movePiece(this))
                .setCurrTurn(board.getCurrPlayer().getOpponent().getAlliance());

        return builder.build();
    }

    public Move getMirroredMove() {
        int srcRow = movedPiece.getPosition().getRow();
        int srcCol = Board.NUM_COLS - 1 - movedPiece.getPosition().getCol();
        Coordinate mirroredSrcPosition = new Coordinate(srcRow, srcCol);

        int destRow = destPosition.getRow();
        int destCol = Board.NUM_COLS - 1 - destPosition.getCol();
        Coordinate mirroredDestPosition = new Coordinate(destRow, destCol);

        return getMove(board.getMirrorBoard(), mirroredSrcPosition, mirroredDestPosition).get();
    }

    public static Optional<Move> getMove(Board board, Coordinate srcPosition, Coordinate destPosition) {
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            if (move.getMovedPiece().getPosition().equals(srcPosition)
                    && move.getDestPosition().equals(destPosition)) {
                return Optional.of(move);
            }
        }

        return Optional.empty();
    }

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

        return getMove(board, srcPosition, destPosition);
    }

    private static String getPieceAbbrev(PieceType type, Alliance alliance) {
        return alliance.isRed() ? type.toString() : type.toString().toLowerCase();
    }

    private static int colToFile(int col, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - col : col + 1;
    }

    private static int fileToCol(int file, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - file : file - 1;
    }

    private static int rowToRank(int row, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - row : row + 1;
    }

    private static int rankToRow(int rank, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_ROWS - rank : rank - 1;
    }

    private static String rankToString(int rank) {
        return rank < 10 ? Integer.toString(rank) : "X";
    }

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
     * [piece abbr][former rank][former file]-[new rank][new file]
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

        return new StringBuilder().append(getPieceAbbrev(pieceType, alliance))
                .append(formerRank).append(formerFile)
                .append("-")
                .append(newRank).append(newFile)
                .toString();
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
                && this.getCapturedPiece().equals(other.getCapturedPiece());
    }

    @Override
    public int hashCode() {
        int result = destPosition.hashCode();
        result = 31*result + movedPiece.hashCode();
        result = 31*result + getCapturedPiece().hashCode();

        return result;
    }
}
