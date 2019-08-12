package com.chess.engine.player.ai;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a MiniMax algorithm with alpha-beta pruning and hashing of calculated board states.
 */
public abstract class MiniMax {

    final Board currBoard;
    final Move bannedMove;
    final Map<BoardState, Integer> stateToValueMap;

    MiniMax(Board currBoard, Move bannedMove) {
        this.currBoard = currBoard;
        this.bannedMove = bannedMove;
        stateToValueMap = new HashMap<>();
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    /**
     * The default algorithm for the minimising player.
     */
    int min(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }

        BoardState boardState = new BoardState(board, depth);
        Integer value = stateToValueMap.get(boardState);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int minValue = Integer.MAX_VALUE;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1, alpha, beta));
                beta = Math.min(beta, minValue);
                if (alpha >= beta) break;
            }
        }

        stateToValueMap.put(boardState, minValue);
        return minValue;
    }

    /**
     * The default algorithm for the maximising player.
     */
    int max(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }

        BoardState boardState = new BoardState(board, depth);
        Integer value = stateToValueMap.get(boardState);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int maxValue = Integer.MIN_VALUE;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1, alpha, beta));
                alpha = Math.max(alpha, maxValue);
                if (alpha >= beta) break;
            }
        }

        stateToValueMap.put(boardState, maxValue);
        return maxValue;
    }

    /**
     * Represents a state containing a board and the depth at which it was evaluated.
     */
    static class BoardState {

        final Board board;
        final int depth;

        BoardState(Board board, int depth) {
            this.board = board;
            this.depth = depth;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BoardState)) {
                return false;
            }

            BoardState other = (BoardState) obj;
            return this.board.equals(other.board) && this.depth == other.depth;
        }

        @Override
        public int hashCode() {
            return 31 * depth + board.hashCode();
        }
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

            if (cpValue1 != cpValue2) { // diff captured piece values -> larger value first
                return cpValue2 - cpValue1;
            }
            if (cpValue1 != 0) { // same nonzero captured piece values -> smaller moved piece value first
                return m1.getMovedPiece().getPieceType().getDefaultValue()
                        - m2.getMovedPiece().getPieceType().getDefaultValue();
            } else { // zero captured piece values -> larger moved piece value first
                return m2.getMovedPiece().getPieceType().getDefaultValue()
                        - m1.getMovedPiece().getPieceType().getDefaultValue();
            }
        };
        private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR_RED = (e1, e2) -> {
            if (e1.value != e2.value) {
                return e2.value - e1.value;
            }
            return MOVE_COMPARATOR.compare(e1.move, e2.move);
        };
        private static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR_BLACK = (e1, e2) -> {
            if (e1.value != e2.value) {
                return e1.value - e2.value;
            }
            return MOVE_COMPARATOR.compare(e1.move, e2.move);
        };

        /**
         * Sorts the given list of move entries according their calculated values and alliance.
         */
        static List<Move> valueSort(Alliance alliance, List<MoveEntry> moveEntries) {
            List<Move> sortedMoves = new ArrayList<>();

            if (alliance.isRed()) {
                moveEntries.sort(MOVE_ENTRY_COMPARATOR_RED);
            } else {
                moveEntries.sort(MOVE_ENTRY_COMPARATOR_BLACK);
            }
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
