package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MoveOrdering {

    private static final MoveOrdering INSTANCE = new MoveOrdering();
    private static final Comparator<Move> MOVE_COMPARATOR = (m1, m2) -> {
        boolean m1isAttack = m1.getCapturedPiece().isPresent();
        boolean m2isAttack = m2.getCapturedPiece().isPresent();
        if (m1isAttack != m2isAttack) {
            return Boolean.compare(m2isAttack, m1isAttack);
        }
        return m2.getMovedPiece().getPieceType().getDefaultValue() - m1.getMovedPiece().getPieceType().getDefaultValue();
    };
    private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR_RED = (e1, e2) -> {
        if (e1.getValue() != e2.getValue()) {
            return e2.getValue() - e1.getValue();
        }
        return MOVE_COMPARATOR.compare(e1.getMove(), e2.getMove());
    };
    private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR_BLACK = (e1, e2) -> {
        if (e1.getValue() != e2.getValue()) {
            return e1.getValue() - e2.getValue();
        }
        return MOVE_COMPARATOR.compare(e1.getMove(), e2.getMove());
    };

    private final BoardEvaluator evaluator;

    private MoveOrdering() {
        evaluator = BoardEvaluator.getInstance();
    }

    static MoveOrdering getInstance() {
        return INSTANCE;
    }

    List<Move> getSortedMoves(Board board, int searchDepth) {
        List<Move> sortedMoves = new ArrayList<>();

        if (searchDepth == 0) {
            sortedMoves.addAll(board.getCurrPlayer().getLegalMoves());
            sortedMoves.sort(MOVE_COMPARATOR);
            return Collections.unmodifiableList(sortedMoves);
        }

        List<MoveEntry> moveEntries = new ArrayList<>();
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                int value = board.getCurrPlayer().getAlliance().isRed() ?
                        min(transition.getNextBoard(), searchDepth - 1) :
                        max(transition.getNextBoard(), searchDepth - 1);
                moveEntries.add(new MoveEntry(move, value));
            }
        }
        if (board.getCurrPlayer().getAlliance().isRed()) {
            moveEntries.sort(MOVE_ENTRY_COMPARATOR_RED);
        } else {
            moveEntries.sort(MOVE_ENTRY_COMPARATOR_BLACK);
        }
        for (MoveEntry moveEntry : moveEntries) {
            sortedMoves.add(moveEntry.getMove());
        }

        return Collections.unmodifiableList(sortedMoves);
    }

    private int min(Board board, int depth) {
        if (depth == 0 || board.isGameOver() || board.isGameDraw()) {
            return evaluator.evaluate(board, depth);
        }

        int minValue = Integer.MAX_VALUE;
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1));
            }
        }

        return minValue;
    }

    private int max(Board board, int depth) {
        if (depth == 0 || board.isGameOver() || board.isGameDraw()) {
            return evaluator.evaluate(board, depth);
        }

        int maxValue = Integer.MIN_VALUE;
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1));
            }
        }

        return maxValue;
    }

    private static class MoveEntry {

        final Move move;
        final int value;

        MoveEntry(Move move, int value) {
            this.move = move;
            this.value = value;
        }

        Move getMove() {
            return move;
        }

        int getValue() {
            return value;
        }
    }
}
