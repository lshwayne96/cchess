package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a MiniMax algorithm.
 */
abstract class MiniMax {
//TODO: hashtable, eval, PV
    static final int NEG_INF = Integer.MIN_VALUE + 1;
    static final int POS_INF = Integer.MAX_VALUE;
    static final int ASP = 50; // aspiration window
    private static final int R = 3; // depth reduction for null move pruning
    private static final int TT_SIZE = 1000003;

    private final Board startBoard;
    private final List<Move> legalMoves;
    private final TTable tTable;
    int calls;
    int hits;

    MiniMax(Board startBoard, Collection<Move> legalMoves) {
        this.startBoard = startBoard;
        this.legalMoves = MoveSorter.simpleSort(legalMoves);
        tTable = new TTable();
        calls = 0;
        hits = 0;
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    List<MoveEntry> getLegalMoveEntries() {
        List<MoveEntry> legalMoveEntries = new ArrayList<>();
        for (Move move : legalMoves) {
            legalMoveEntries.add(new MoveEntry(move, 0));
        }
        return legalMoveEntries;
    }

    List<MoveEntry> alphaBetaRoot(List<MoveEntry> oldMoveEntries, int depth, int alpha, int beta) {
        List<MoveEntry> newMoveEntries = new ArrayList<>();
        MoveEntry bestMoveEntry = null;
        int bestVal = NEG_INF;
        int searchedMoves = 0;

        for (MoveEntry moveEntry : oldMoveEntries) {
            Move move = moveEntry.move;
            startBoard.makeMove(move);
            if (startBoard.isStateAllowed()) {
                int val;
                if (searchedMoves == 0) {
                    val = -alphaBeta(startBoard, depth - 1, -beta, -alpha, true);
                } else {
                    val = -alphaBeta(startBoard, depth - 1, -alpha - 1, -alpha, true);
                    if (val > alpha && val < beta) {
                        val = -alphaBeta(startBoard, depth - 1, -beta, -alpha, true);
                    }
                }
                if (val > bestVal) {
                    bestVal = val;
                    bestMoveEntry = moveEntry;
                    alpha = Math.max(alpha, val);
                }
                newMoveEntries.add(new MoveEntry(move, val));
            }
            startBoard.unmakeMove(move);
            searchedMoves++;
        }
        assert bestMoveEntry != null;

        // sort new move entries and swap best entry to the front
        newMoveEntries.sort(MoveSorter.MOVE_ENTRY_COMPARATOR);
        int bestIndex = 0;
        for (int i = 0; i < newMoveEntries.size(); i++) {
            if (newMoveEntries.get(i).move.equals(bestMoveEntry.move)) {
                bestIndex = i;
                break;
            }
        }
        Collections.swap(newMoveEntries, 0, bestIndex);

        return Collections.unmodifiableList(newMoveEntries);
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull) {
        int alphaOrig = alpha;
        Move bestMove = null;
calls++;
        // look up transposition table
        long zobristKey = board.getZobristKey();
        TTEntry ttEntry = tTable.getEntry(zobristKey);
        if (ttEntry != null) { hits++;
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
            return quiescence(board, -beta, -alpha);
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
                int val;
                if (bestMove != null) {
                    val = -alphaBeta(board, depth - 1, -alpha - 1, -alpha, true);
                    if (val > alpha && val < beta) {
                        val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                    }
                } else {
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
            tTable.storeEntry(new TTEntry(zobristKey, depth, bestVal, flag, bestMove));
        }

        return bestVal;
    }

    private int quiescence(Board board, int alpha, int beta) {
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        int bestVal = BoardEvaluator.evaluate(board) * color;
        alpha = Math.max(alpha, bestVal);
        if (alpha >= beta || board.isQuiet()) {
            return bestVal;
        }

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

    private static class TTable {

        private final TTEntry[] arr;

        private TTable() {
            arr = new TTEntry[TT_SIZE];
        }

        private TTEntry getEntry(long zobristKey) {
            int index = (int) Math.abs(zobristKey % TT_SIZE);
            TTEntry entry = arr[index];
            if (entry != null && entry.zobristKey == zobristKey) {
                return entry;
            } else {
                return null;
            }
        }

        private void storeEntry(TTEntry entry) {
            if (entry == null) return;
            int index = (int) Math.abs(entry.zobristKey % TT_SIZE);
            arr[index] = entry;
        }
    }

    /**
     * Represents a transposition table entry.
     */
    private static class TTEntry {

        private final long zobristKey;
        private final int depth;
        private final int val;
        private final Flag flag;
        private final Move bestMove;

        private TTEntry(long zobristKey, int depth, int val, Flag flag, Move bestMove) {
            this.zobristKey = zobristKey;
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
         * Sorts the given collection of moves in the default way.
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
