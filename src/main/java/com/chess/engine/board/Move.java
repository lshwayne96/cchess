package main.java.com.chess.engine.board;

import main.java.com.chess.engine.Alliance;
import main.java.com.chess.engine.pieces.Piece;

import java.util.Optional;

import static main.java.com.chess.engine.board.Board.*;
import static main.java.com.chess.engine.pieces.Piece.*;

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
     * [single-letter piece abbreviation][former file][operator indicating direction of movement]
     * [new file, or in the case of purely vertical movement, number of ranks traversed]
     */
    @Override
    public String toString() {
        Coordinate position = movedPiece.getPosition();
        Alliance alliance = movedPiece.getAlliance();
        PieceType type = movedPiece.getType();

        int rowsMoved = (destPosition.getRow() - position.getRow()) * alliance.getDirection();
        String direction = getStringDirection(rowsMoved);
        String move = getPieceString(type, alliance) + colToFile(position.getCol(), alliance) + direction;

        int destCol = colToFile(destPosition.getCol(), alliance);
        if (type == PieceType.ADVISOR || type == PieceType.ELEPHANT || type == PieceType.HORSE) {
            return move + destCol;
        } else {
            return move + (rowsMoved == 0 ? destCol : Math.abs(rowsMoved));
        }
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

    public static Optional<Move> getMove(Board board, Coordinate currPosition, Coordinate destPosition) {
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            if (move.getMovedPiece().getPosition().equals(currPosition)
                    && move.getDestPosition().equals(destPosition)) {
                return Optional.of(move);
            }
        }
        return Optional.empty();
    }

    private static String getPieceString(PieceType type, Alliance alliance) {
        return alliance.isRed() ? type.toString() : type.toString().toLowerCase();
    }

    private static int colToFile(int col, Alliance alliance) {
        return alliance.isRed() ? Board.NUM_COLS - col : col + 1;
    }

    private String getStringDirection(int direction) {
        if (direction > 0) {
            return "+";
        } else if (direction == 0) {
            return ".";
        } else {
            return "-";
        }
    }
}
