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

    static final int NEG_INF = Integer.MIN_VALUE + 1; // represents negative infinity
    static final int POS_INF = Integer.MAX_VALUE; // represents positive infinity
    static final int ASP = 50; // aspiration window
    private static final int R_LOW = 2; // low depth reduction
    private static final int R_HIGH = 3; // high depth reduction
    private static final int TT_SIZE = 1000003; // transposition table size

    private final Board startBoard; // initial board
    private final List<Move> legalMoves; // initial legal moves (simple-sorted)
    private final TTable tTable; // transposition table
    private final int R; // variable depth reduction for null move pruning

    MiniMax(Board startBoard, Collection<Move> legalMoves, boolean high) {
        this.startBoard = startBoard;
        this.legalMoves = MoveSorter.simpleSort(legalMoves);
        tTable = new TTable();
        R = high ? R_HIGH : R_LOW;
    }

    /**
     * Returns the best move using the corresponding MiniMax algorithm.
     * @return The best move using the corresponding MiniMax algorithm.
     */
    public abstract Move search();

    /**
     * Returns a simple-sorted list of move entries of the initial legal moves.
     * @return A simple-sorted list of move entries of the initial legal moves.
     */
    List<MoveEntry> getLegalMoveEntries() {
        List<MoveEntry> legalMoveEntries = new ArrayList<>();
        for (Move move : legalMoves) {
            legalMoveEntries.add(new MoveEntry(move, 0));
        }
        return Collections.unmodifiableList(legalMoveEntries);
    }

    /**
     * The root method of alpha-beta search.
     * @param oldMoveEntries The list of move entries to search, with the best move at the front.
     * @param depth The search depth.
     * @param alpha The lower bound.
     * @param beta The upper bound.
     * @return A value-sorted list of move entries at the given search depth, with the best move at the front.
     */
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
                if (searchedMoves == 0) { // search best move with full window
                    val = -alphaBeta(startBoard, depth - 1, -beta, -alpha, true);
                } else { // search remaining moves with null window
                    val = -alphaBeta(startBoard, depth - 1, -alpha - 1, -alpha, true);
                    if (val > alpha && val < beta) { // research with full window
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

    /**
     * The inner method of alpha-beta search.
     * @param board The current board.
     * @param depth The current depth.
     * @param alpha The current lower bound.
     * @param beta The current upper bound.
     * @param allowNull Whether a null move is allowed here.
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull) {
        int alphaOrig = alpha;
        Move bestMove = null;

        // look up transposition table
        long zobristKey = board.getZobristKey();
        TTEntry ttEntry = tTable.getEntry(zobristKey);
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

        // evaluate board
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        if (depth <= 0) {
            int val = quiescence(board, -beta, -alpha);
            if (ttEntry == null) {
                tTable.storeEntry(new TTEntry(zobristKey, 0, val, Flag.EXACT, null));
            }
            return val;
        }
        if (board.isCurrPlayerCheckmated()) {
            return BoardEvaluator.getCheckmateValue(board.getCurrPlayer().getAlliance(), depth) * color;
        }

        // null move pruning
        if (allowNull && !board.getCurrPlayer().isInCheck()) {
            board.changeTurn();
            int val = -alphaBeta(board, depth - 1 - R, -beta, -beta + 1, false);
            board.changeTurn();
            if (val >= beta) {
                return val;
            }
        }

        // search all moves
        int bestVal = NEG_INF;
        boolean hasBestMove = bestMove != null;
        if (hasBestMove) { // search best move with full window
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
                if (hasBestMove) { // search remaining moves with null window
                    val = -alphaBeta(board, depth - 1, -alpha - 1, -alpha, true);
                    if (val > alpha && val < beta) { // research with full window
                        val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                    }
                } else {
                    val = -alphaBeta(board, depth - 1, -beta, -alpha, true);
                }
                if (val > bestVal) {
                    bestVal = val;
                    if (val > alphaOrig) {
                        bestMove = move;
                    }
                    alpha = Math.max(alpha, val);
                }
            }
            board.unmakeMove(move);

            if (bestVal >= beta) {
                break;
            }
        }

        // store into transposition table
        if (ttEntry == null || depth > ttEntry.depth) {
            Flag flag;
            if (bestVal <= alphaOrig) {
                flag = Flag.UPPERBOUND;
            } else if (bestVal >= beta) {
                flag = Flag.LOWERBOUND;
            } else {
                flag = Flag.EXACT;
            }
            tTable.storeEntry(new TTEntry(zobristKey, depth, bestVal, flag, bestMove));
        }

        return bestVal;
    }

    /**
     * The quiescence call when depth reaches 0.
     */
    private int quiescence(Board board, int alpha, int beta) {
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        int bestVal = BoardEvaluator.evaluate(board) * color; // "stand-pat"
        alpha = Math.max(alpha, bestVal);
        if (alpha >= beta || board.isQuiet()) {
            return bestVal;
        }

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (!move.isCapture()) break; // capture moves are at the front

            board.makeMove(move);
            if (board.isStateAllowed()) {
                int val = -quiescence(board, -beta, -alpha);
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
            board.unmakeMove(move);

            if (bestVal >= beta) {
                break;
            }
        }

        return bestVal;
    }

    /**
     * Represents a transposition table (TT).
     */
    private static class TTable {

        private final TTEntry[] arr;

        private TTable() {
            arr = new TTEntry[TT_SIZE];
        }

        /**
         * Returns a TT entry given the Zobrist key.
         */
        private TTEntry getEntry(long zobristKey) {
            int index = (int) Math.abs(zobristKey % TT_SIZE);
            TTEntry entry = arr[index];
            if (entry != null && entry.zobristKey == zobristKey) {
                return entry;
            } else {
                return null;
            }
        }

        /**
         * Stores the given entry into this TT.
         */
        private void storeEntry(TTEntry entry) {
            if (entry == null) return;
            int index = (int) Math.abs(entry.zobristKey % TT_SIZE);
            arr[index] = entry;
        }
    }

    /**
     * Represents an entry in the TT.
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
    }

    /**
     * Represents the relationship of value with alpha/beta.
     */
    private enum Flag {
        EXACT,
        LOWERBOUND,
        UPPERBOUND
    }

    /**
     * A helper class for sorting moves to aid alpha-beta pruning.
     */
    static class MoveSorter {

        private static final Comparator<Move> MOVE_COMPARATOR = (m1, m2) -> {
            int cpValue1 = m1.isCapture()
                    ? m1.getCapturedPiece().get().getValue(false) : 0;
            int cpValue2 = m2.isCapture()
                    ? m2.getCapturedPiece().get().getValue(false) : 0;
            if (cpValue1 == 0 && cpValue2 == 0) { // both non-captures, compare move priority
                return m1.getMovedPiece().getPieceType().getMovePriority()
                        - m2.getMovedPiece().getPieceType().getMovePriority();
            }

            int pValue1 = m1.getMovedPiece().getValue(false);
            int pValue2 = m2.getMovedPiece().getValue(false);
            if (cpValue1 != 0 && cpValue2 != 0) { // both captures, compare difference of capture profits
                return (cpValue2 - pValue2) - (cpValue1 - pValue1);
            }

            // captures first
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
