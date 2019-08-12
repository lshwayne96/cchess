package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.General;
import com.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.chess.engine.pieces.Piece.*;

/**
 * Represents a player of Chinese Chess.
 */
public abstract class Player {

    protected final Board board;
    private final General general;
    private final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(Board board, Collection<Move> playerLegalMoves, Collection<Move> opponentLegalMoves) {
        this.board = board;
        general = getGeneral();
        legalMoves = playerLegalMoves;
        isInCheck = !getIncomingAttacks(general.getPosition(), opponentLegalMoves).isEmpty();
    }

    /**
     * Returns a collection of this player's active pieces.
     * @return a collection of this player's active pieces.
     */
    public abstract Collection<Piece> getActivePieces();

    /**
     * Returns the alliance of this player.
     * @return The alliance of this player.
     */
    public abstract Alliance getAlliance();

    /**
     * Returns the opponent of this player.
     * @return The opponent of this player.
     */
    public abstract Player getOpponent();

    /**
     * Returns the general piece of this player.
     */
    private General getGeneral() {
        for (Piece piece : getActivePieces()) {
            if (piece.getPieceType().equals(PieceType.GENERAL)) {
                return (General) piece;
            }
        }

        throw new RuntimeException(getAlliance().toString() + " GENERAL missing");
    }

    /**
     * Returns a collection of opponent moves that attack the given position.
     */
    private static Collection<Move> getIncomingAttacks(Coordinate position, Collection<Move> opponentMoves) {
        List<Move> attacksOnPoint = new ArrayList<>();

        for (Move move : opponentMoves) {
            if (move.getDestPosition().equals(position)) {
                attacksOnPoint.add(move);
            }
        }

        return Collections.unmodifiableList(attacksOnPoint);
    }

    /**
     * Returns a move transition after making the given move.
     * @param move The move to make.
     * @return A move transition after making the given move.
     */
    public MoveTransition makeMove(Move move) {
        Board nextBoard = move.execute();
        Collection<Move> generalAttacks =
                getIncomingAttacks(nextBoard.getCurrPlayer().getOpponent().general.getPosition(),
                        nextBoard.getCurrPlayer().getLegalMoves());

        if (!generalAttacks.isEmpty()) {
            return new MoveTransition(board, move, MoveStatus.SUICIDAL);
        }
        return new MoveTransition(nextBoard, move, MoveStatus.ALLOWED);
    }

    public Collection<Move> getLegalMoves() {
        return legalMoves;
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    /**
     * Checks if this player has been checkmated.
     * @return true if this player has been checkmated, false otherwise.
     */
    public boolean isInCheckmate() {
        return !hasEscapeMoves();
    }

    /**
     * Checks if this player's general can escape check.
     */
    private boolean hasEscapeMoves() {
        for (Move move : legalMoves) {
            MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isAllowed()) return true;
        }
        return false;
    }
}
