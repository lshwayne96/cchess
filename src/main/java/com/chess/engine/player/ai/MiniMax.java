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

import static com.chess.engine.board.Board.*;

/**
 * Represents a MiniMax algorithm.
 */
abstract class MiniMax {
//TODO: quiescence, eval, zobrist
    static final int NEG_INF = Integer.MIN_VALUE + 1;
    static final int POS_INF = Integer.MAX_VALUE;
    static final int ASP_WINDOW = 50;
    private static final int R = 2; // depth reduction for null move pruning

    final Board board;
    final Collection<Move> legalMoves;
    private final Map<BoardState, TTEntry> transTable;

    MiniMax(Board board, Collection<Move> legalMoves) {
        this.board = board;
        this.legalMoves = legalMoves;
        transTable = new HashMap<>();
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    List<MoveEntry> alphaBetaRoot(List<MoveEntry> oldMoveEntries, int depth, int alpha, int beta) {
        List<MoveEntry> newMoveEntries = new ArrayList<>();

        for (MoveEntry moveEntry : oldMoveEntries) {
            Move move = moveEntry.move;
            board.makeMove(move);
            if (board.isStateAllowed()) {
                int val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                alpha = Math.max(alpha, val);
                newMoveEntries.add(new MoveEntry(move, val));
            }
            board.unmakeMove(move);
        }
        newMoveEntries.sort(MoveSorter.MOVE_ENTRY_COMPARATOR);

        return Collections.unmodifiableList(newMoveEntries);
    }

    int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull) {
        int alphaOrig = alpha;
        Move bestMove = null;

        // look up transposition table
        BoardState boardState = board.getState();
        TTEntry ttEntry = transTable.get(boardState);
        if (ttEntry != null) {
            bestMove = ttEntry.bestMove;
            if (ttEntry.depth >= depth) {
                switch (ttEntry.flag) {
                    case EXACT:
                        return ttEntry.val;
                    case LOWERBOUND:
                        alpha = Math.max(alpha, ttEntry.val);
                        break;
                    case UPPERBOUND:
                        beta = Math.min(beta, ttEntry.val);
                        break;
                }
                if (alpha >= beta) {
                    return ttEntry.val;
                }
            }
        }

        // evaluate board if ready
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        if (depth <= 0) {
            //return -quiescence(board, -beta, -alpha);
            return BoardEvaluator.evaluate(board, depth) * color;
        }
        if (board.isGameOver()) {
            return BoardEvaluator.getCheckmateValue(board, depth) * color;
        }

        // null move pruning if possible
        if (allowNull && board.allowNullMove()) {
            board.changeTurn();
            int val = -alphaBeta(board, depth - 1 - R, -beta, -beta + 1, false);
            board.changeTurn();
            if (val >= beta) {
                return val;
            }
        }

        // search all moves
        int bestVal = NEG_INF;
        if (bestMove != null) {
            board.makeMove(bestMove);
            int val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
            board.unmakeMove(bestMove);
            bestVal = val;
            alpha = Math.max(alpha, val);
            if (val >= beta) {
                return val;
            }
        }
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (move.equals(bestMove)) continue;
            board.makeMove(move);
            if (board.isStateAllowed()) {
                int val = -alphaBeta(board, depth - 1, -alpha - 1, -alpha, true);
                if (val > alpha && val < beta) {
                    val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                }
                board.unmakeMove(move);
                if (val > bestVal) {
                    bestVal = val;
                    if (val > alphaOrig) {
                        bestMove = move;
                    }
                    alpha = Math.max(alpha, val);
                }
                if (val >= beta) {
                    break;
                }
            } else {
                board.unmakeMove(move);
            }
        }

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
            TTEntry newEntry = new TTEntry(depth, bestVal, flag, bestMove);
            transTable.put(boardState, newEntry);
        }

        return bestVal;
    }

    private int quiescence(Board board, int alpha, int beta) {
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        int standPat = BoardEvaluator.evaluate(board, 0) * color;
        if (-standPat >= beta || board.isQuiet()) {
            return standPat;
        }
        alpha = Math.max(alpha, -standPat);

        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (!move.getCapturedPiece().isPresent()) break;

            board.makeMove(move);
            if (board.isStateAllowed()) {
                int val = -quiescence(board, -beta, -alpha);
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

        return bestVal;
    }

    /**
     * Represents a transposition table entry.
     */
    static class TTEntry {

        private final int depth;
        private final int val;
        private final Flag flag;
        private final Move bestMove;

        private TTEntry(int depth, int val, Flag flag, Move bestMove) {
            this.depth = depth;
            this.val = val;
            this.flag = flag;
            this.bestMove = bestMove;
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
        static final Comparator<MoveEntry> MOVE_ENTRY_COMPARATOR = (e1, e2) -> {
            if (e1.val != e2.val) {
                return e2.val - e1.val;
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
        final int val;

        MoveEntry(Move move, int val) {
            this.move = move;
            this.val = val;
        }
    }
}
