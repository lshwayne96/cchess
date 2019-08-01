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

public abstract class Player {

    protected final Board board;
    private final General playerGeneral;
    private final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(Board board, Collection<Move> playerLegalMoves, Collection<Move> opponentLegalMoves) {
        this.board = board;
        playerGeneral = getPlayerGeneral();
        legalMoves = playerLegalMoves;
        isInCheck = !getIncomingAttacks(playerGeneral.getPosition(), opponentLegalMoves).isEmpty();
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract Alliance getAlliance();

    public abstract Player getOpponent();

    private General getPlayerGeneral() {
        for (Piece piece : getActivePieces()) {
            if (piece.getPieceType().equals(PieceType.GENERAL)) {
                return (General) piece;
            }
        }

        throw new RuntimeException(getAlliance().toString() + " GENERAL missing");
    }

    public static Collection<Move> getIncomingAttacks(Coordinate position, Collection<Move> opponentMoves) {
        List<Move> attacksOnPoint = new ArrayList<>();

        for (Move move : opponentMoves) {
            if (move.getDestPosition().equals(position)) {
                attacksOnPoint.add(move);
            }
        }

        return Collections.unmodifiableList(attacksOnPoint);
    }

    public Collection<Piece> getDefenses(Coordinate position) {
        List<Piece> defendingPieces = new ArrayList<>();

        for (Piece piece : getActivePieces()) {
            if (!piece.getPosition().equals(position) && piece.getDestPositions(board).contains(position)) {
                defendingPieces.add(piece);
            }
        }

        return Collections.unmodifiableList(defendingPieces);
    }

    public MoveTransition makeMove(Move move) {
        Board nextBoard = move.execute();
        Collection<Move> generalAttacks =
                getIncomingAttacks(nextBoard.getCurrPlayer().getOpponent().playerGeneral.getPosition(),
                        nextBoard.getCurrPlayer().getLegalMoves());
        if (!generalAttacks.isEmpty()) {
            return new MoveTransition(board, move, MoveStatus.SUICIDAL);
        }

        return new MoveTransition(nextBoard, move, MoveStatus.DONE);
    }

    public Collection<Move> getLegalMoves() {
        return legalMoves;
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    public boolean isInCheckmate() {
        return !hasEscapeMoves();
    }

    private boolean hasEscapeMoves() {
        for (Move move : legalMoves) {
            MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) return true;
        }

        return false;
    }
}
