package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MoveOrdering {

    private final BoardEvaluator evaluator;

    private static final MoveOrdering INSTANCE = new MoveOrdering();

    private static final Comparator<Move> MOVE_COMPARATOR = (m1, m2) -> {
        boolean m1isAttack = m1.getCapturedPiece().isPresent();
        boolean m2isAttack = m2.getCapturedPiece().isPresent();
        if (m1isAttack != m2isAttack) {
            return Boolean.compare(m2isAttack, m1isAttack);
        }
        return m2.getMovedPiece().getType().getDefaultValue() - m1.getMovedPiece().getType().getDefaultValue();
    };
    private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR = (e1, e2) -> {
        if (e1.getScore() != e2.getScore()) {
            return e2.getScore() - e1.getScore();
        }
        return MOVE_COMPARATOR.compare(e1.getMove(), e2.getMove());
    };

    private MoveOrdering() {
        evaluator = BoardEvaluator.getInstance();
    }

    public static MoveOrdering getInstance() {
        return INSTANCE;
    }

    public List<Move> getSortedMoves(Board board, int searchDepth) {
        List<Move> sortedMoves = new ArrayList<>();

        if (searchDepth == 0) {
            sortedMoves.addAll(board.getCurrPlayer().getLegalMoves());
            sortedMoves.sort(MOVE_COMPARATOR);
            return sortedMoves;
        }

        List<MoveEntry> moveEntryList = new ArrayList<>();
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                int value = board.getCurrPlayer().getAlliance().isRed() ?
                        min(transition.getNextBoard(), searchDepth - 1) :
                        max(transition.getNextBoard(), searchDepth - 1);
                moveEntryList.add(new MoveEntry(move, value));
            }
        }
        moveEntryList.sort(MOVE_ENTRY_COMPARATOR);
        if (!board.getCurrPlayer().getAlliance().isRed()) {
            Collections.reverse(moveEntryList);
        }
        for (MoveEntry moveEntry : moveEntryList) {
            sortedMoves.add(moveEntry.getMove());
        }

        return Collections.unmodifiableList(sortedMoves);
    }

    private int min(Board board, int depth) {
        if (depth == 0 || board.isGameOver()) {
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
        if (depth == 0 || board.isGameOver()) {
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

        MoveEntry(Move move, int score) {
            this.move = move;
            this.value = score;
        }

        Move getMove() {
            return move;
        }

        int getScore() {
            return value;
        }
    }
}
