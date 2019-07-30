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
    protected final General playerGeneral;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(Board board, Collection<Move> playerMoves, Collection<Move> opponentMoves) {
        this.board = board;
        playerGeneral = getGeneral();
        legalMoves = playerMoves;
        isInCheck = !calculateAttacksOnPoint(playerGeneral.getPosition(), opponentMoves).isEmpty();
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

    public MoveTransition makeMove(Move move) {
        if (!isMoveLegal(move)) {
            return new MoveTransition(board, move, MoveStatus.ILLEGAL);
        }

        Board nextBoard = move.execute();
        Collection<Move> generalAttacks =
                calculateAttacksOnPoint(nextBoard.getCurrPlayer().getOpponent().playerGeneral.getPosition(),
                        nextBoard.getCurrPlayer().getLegalMoves());
        if (!generalAttacks.isEmpty()) {
            return new MoveTransition(board, move, MoveStatus.SUICIDAL);
        }

        return new MoveTransition(nextBoard, move, MoveStatus.DONE);
    }

    private boolean isMoveLegal(Move move) {
        return legalMoves.contains(move);
    }

    private boolean hasEscapeMoves() {
        for (Move move : legalMoves) {
            MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) return true;
        }

        return false;
    }

    private General getGeneral() {
        for (Piece piece : getActivePieces()) {
            if (piece.getType() == PieceType.GENERAL) {
                return (General) piece;
            }
        }

        throw new RuntimeException(getAlliance().toString() + "GENERAL missing");
    }

    private static Collection<Move> calculateAttacksOnPoint(Coordinate position,
                                                            Collection<Move> opponentMoves) {
        List<Move> attacksOnPoint = new ArrayList<>();

        for (Move move : opponentMoves) {
            if (move.getDestPosition().equals(position)) {
                attacksOnPoint.add(move);
            }
        }

        return Collections.unmodifiableCollection(attacksOnPoint);
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract Alliance getAlliance();

    public abstract Player getOpponent();
}
