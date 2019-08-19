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
    private final Collection<Piece> activePieces;
    private final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(Board board, Collection<Piece> activePieces, Collection<Move> legalMoves, Collection<Move> oppLegalMoves) {
        this.board = board;
        this.activePieces = activePieces;
        this.legalMoves = legalMoves;
        isInCheck = !getIncomingAttacks(getGeneral().getPosition(), oppLegalMoves).isEmpty();
    }

    /**
     * Returns a collection of this player's active pieces.
     * @return a collection of this player's active pieces.
     */
    public Collection<Piece> getActivePieces() {
        return Collections.unmodifiableCollection(activePieces);
    }

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
        for (Piece piece : activePieces) {
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

    public Collection<Move> getLegalMoves() {
        return Collections.unmodifiableCollection(legalMoves);
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    /**
     * Checks if this player has been checkmated.
     * @return true if this player has been checkmated, false otherwise.
     */
    public boolean isInCheckmate() {
        for (Move move : legalMoves) {
            board.makeMove(move);
            if (board.isLegalState()) {
                board.unmakeMove(move);
                return false;
            }
            board.unmakeMove(move);
        }
        return true;
    }
}
