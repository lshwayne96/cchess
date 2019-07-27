package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

import java.util.Optional;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

public class Move {

    protected final Board board;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;

        Move move = (Move) o;
        return (this.movedPiece.equals(move.movedPiece))
                && (this.destPosition.equals(move.destPosition))
                && (this.getCapturedPiece().equals(move.getCapturedPiece()));
    }

    @Override
    public int hashCode() {
        int result = destPosition.hashCode();
        result = 31 * result + movedPiece.hashCode();
        result = 31 * result + getCapturedPiece().hashCode();
        return result;
    }

    /**
     * [piece abbr][former rank][former file]-[new rank][new file]
     * rank 10 is represented by "X"
     */
    @Override
    public String toString() {
        Coordinate sourcePosition = movedPiece.getPosition();
        Alliance alliance = movedPiece.getAlliance();
        PieceType type = movedPiece.getType();

        String formerRank = rankToString(rowToRank(sourcePosition.getRow(), alliance));
        String formerFile = Integer.toString(colToFile(sourcePosition.getCol(), alliance));
        String newRank = rankToString(rowToRank(destPosition.getRow(), alliance));
        String newFile = Integer.toString(colToFile(destPosition.getCol(), alliance));

        return new StringBuilder().append(getPieceString(type, alliance))
                .append(formerRank).append(formerFile)
                .append("-")
                .append(newRank).append(newFile)
                .toString();
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

    public static Optional<Move> getMove(Board board, Coordinate sourcePosition, Coordinate destPosition) {
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            if (move.getMovedPiece().getPosition().equals(sourcePosition)
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

        Coordinate sourcePosition = new Coordinate(formerRow, formerCol);
        Coordinate destPosition = new Coordinate(newRow, newCol);

        return getMove(board, sourcePosition, destPosition);
    }

    private static String getPieceString(PieceType type, Alliance alliance) {
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
}
