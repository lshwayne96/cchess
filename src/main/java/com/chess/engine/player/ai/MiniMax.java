package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a MiniMax algorithm.
 */
abstract class MiniMax {
// TODO: PVS, aspiration window, transposition table, quiescence
    static final int R = 3; // depth reduction for null move pruning
    static final int NEG_INF = Integer.MIN_VALUE + 1;
    static final int POS_INF = Integer.MAX_VALUE;
    static final int ASP_WINDOW = 50;

    final Board board;
    final List<Move> bannedMoves;
    final Map<Board, TTEntry> transTable;

    MiniMax(Board board, List<Move> bannedMoves) {
        this.board = board;
        this.bannedMoves = bannedMoves;
        transTable = new HashMap<>();
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    int negamax(Board board, int depth) {
        if (depth == 0) {
            return BoardEvaluator.evaluate(board, depth);
        }
        int bestVal = NEG_INF;
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -negamax(board, depth - 1);
                bestVal = Math.max(bestVal, val);
            }
            board.unmakeMove(move);
        }
        return bestVal;
    }

    int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull) {
        int alphaOrig = alpha;
/*
        // look up transposition table
        TTEntry ttEntry = transTable.get(board);
        if (ttEntry != null && ttEntry.depth >= depth) {
            switch (ttEntry.flag) {
                case EXACT:
                    return ttEntry.value;
                case LOWERBOUND:
                    alpha = Math.max(alpha, ttEntry.value);
                    break;
                case UPPERBOUND:
                    beta = Math.min(beta, ttEntry.value);
                    break;
            }
            if (alpha >= beta) {
                return ttEntry.value;
            }
        }*/

        // evaluate board if ready
        if (depth <= 0 || board.isGameOver()) {
            int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
            return BoardEvaluator.evaluate(board, depth) * color;
        }

        // null move pruning if possible
        if (allowNull && board.allowNullMove()) {
            board.passMove();
            int val = -alphaBeta(board, depth - 1 - R, -beta, -beta + 1, false);
            board.passMove();
            if (val >= beta) {
                return val;
            }
        }

        // search all moves
        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                board.unmakeMove(move);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            } else {
                board.unmakeMove(move);
            }
        }
/*
        // store into transposition table if necessary
        if (ttEntry == null || depth > ttEntry.depth) {
            TTEntry.Flag flag;
            if (bestVal <= alphaOrig) {
                flag = TTEntry.Flag.UPPERBOUND;
            } else if (bestVal >= beta) {
                flag = TTEntry.Flag.LOWERBOUND;
            } else {
                flag = TTEntry.Flag.EXACT;
            }
            TTEntry newEntry = new TTEntry(depth, bestVal, flag);
            transTable.put(board, newEntry);
        }*/

        return bestVal;
    }

    /**
     * Represents a transposition table entry.
     */
    static class TTEntry {

        private final int depth;
        private final int value;
        private final Flag flag;

        private TTEntry(int depth, int value, Flag flag) {
            this.depth = depth;
            this.value = value;
            this.flag = flag;
        }

        /**
         * Represents the relationship of value with alpha/beta.
         */
        enum Flag {
            EXACT,
            LOWERBOUND,
            UPPERBOUND
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
