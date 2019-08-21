package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a MiniMax algorithm.
 */
abstract class MiniMax {

    static final int NEG_INF = Integer.MIN_VALUE + 1;
    static final int POS_INF = Integer.MAX_VALUE;
    private static final int R = 3; // depth reduction for null move pruning

    final Board currBoard;
    final List<Move> bannedMoves;

    MiniMax(Board currBoard, List<Move> bannedMoves) {
        this.currBoard = currBoard;
        this.bannedMoves = bannedMoves;
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull) {
        // evaluate board if ready
        if (depth <= 0 || board.isGameOver()) {
            int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
            return BoardEvaluator.evaluate(board, depth) * color;
        }

        // null move pruning if possible
        if (allowNull && !board.getCurrPlayer().isInCheck() && board.allowNullMove()) {
            Board nextBoard = board.makeNullMove();
            int val = -alphaBeta(nextBoard, depth - 1 - R, -beta, -beta + 1, false);
            if (val >= beta) {
                return val;
            }
        }

        // search all moves
        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -alphaBeta(transition.getNextBoard(), depth - 1, -beta, -alpha, true);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
        }

        return bestVal;
    }

    int min(Board board, int depth, int alpha, int beta) {
        if (depth <= 0 || board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }

        int minValue = POS_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1, alpha, beta));
                beta = Math.min(beta, minValue);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        return minValue;
    }

    int max(Board board, int depth, int alpha, int beta) {
        if (depth <= 0 || board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }

        int maxValue = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1, alpha, beta));
                alpha = Math.max(alpha, maxValue);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        return maxValue;
    }

    /**
     * A helper class for sorting moves to aid alpha-beta pruning.
     */
    static class MoveSorter {

        private static final Comparator<Move> MOVE_COMPARATOR = (m1, m2) -> {
            int cpValue1 = m1.getCapturedPiece().isPresent()
                    ? m1.getCapturedPiece().get().getPieceType().getDefaultValue() : 0;
            int cpValue2 = m2.getCapturedPiece().isPresent()
                    ? m2.getCapturedPiece().get().getPieceType().getDefaultValue() : 0;
            if (cpValue1 == 0 && cpValue2 == 0) {
                return m1.getMovedPiece().getPieceType().getMovePriority()
                        - m2.getMovedPiece().getPieceType().getMovePriority();
            }

            int pValue1 = m1.getMovedPiece().getPieceType().getDefaultValue();
            int pValue2 = m2.getMovedPiece().getPieceType().getDefaultValue();
            if (cpValue1 != 0 && cpValue2 != 0) {
                return (cpValue2 - pValue2) - (cpValue1 - pValue1);
            }

            return cpValue2 - cpValue1;
        };
        private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR = (e1, e2) -> {
            if (e1.value != e2.value) {
                return e2.value - e1.value;
            }
            return MOVE_COMPARATOR.compare(e1.move, e2.move);
        };

        /**
         * Sorts the given list of move entries in descending order of their calculated values.
         */
        static List<Move> valueSort(List<MoveEntry> moveEntries) {
            List<Move> sortedMoves = new ArrayList<>();

            moveEntries.sort(MOVE_ENTRY_COMPARATOR);
            for (MoveEntry moveEntry : moveEntries) {
                sortedMoves.add(moveEntry.move);
            }

            return Collections.unmodifiableList(sortedMoves);
        }

        /**
         * Sorts the given collection of moves according to their captured piece values, otherwise moved piece values.
         */
        static List<Move> simpleSort(Collection<Move> moves) {
            List<Move> sortedMoves = new ArrayList<>(moves);

            sortedMoves.sort(MOVE_COMPARATOR);

            return Collections.unmodifiableList(sortedMoves);
        }
    }

    /**
     * Represents an entry containing a move and its value.
     */
    static class MoveEntry {

        final Move move;
        final int value;

        MoveEntry(Move move, int value) {
            this.move = move;
            this.value = value;
        }
    }
}
