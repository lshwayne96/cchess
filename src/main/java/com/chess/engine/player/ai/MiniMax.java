package com.chess.engine.player.ai;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chess.gui.Table.*;

/**
 * Represents a MiniMax algorithm with alpha-beta pruning and hashing of calculated board states.
 */
public class MiniMax {

    private static final MiniMax INSTANCE = new MiniMax();

    private final BoardEvaluator evaluator;
    private final Map<BoardState, Integer> stateToValueMap;

    private MiniMax() {
        evaluator = BoardEvaluator.getInstance();
        stateToValueMap = new HashMap<>();
    }

    /**
     * Returns an instance of this MiniMax.
     * @return An instance of this MiniMax.
     */
    public static MiniMax getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the best move using fixed-depth search based on the given board and search depth.
     * @param board The current board.
     * @param searchDepth The depth of the search.
     * @param bannedPiece The piece not to use for checking the opponent.
     * @return The best move using fixed-depth search based on the given board and search depth.
     */
    public Move fixedDepth(Board board, int searchDepth, Piece bannedPiece) {
        stateToValueMap.clear();
        Move bestMove = null;

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        int currValue;

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                Board nextBoard = transition.getNextBoard();
                if (move.getMovedPiece().equals(bannedPiece) && !move.getCapturedPiece().isPresent()
                        && nextBoard.getCurrPlayer().isInCheck()) continue;

                if (board.getCurrPlayer().getAlliance().isRed()) {
                    currValue = min(nextBoard, searchDepth - 1, maxValue, minValue);
                    if (currValue > maxValue) {
                        maxValue = currValue;
                        bestMove = move;
                    }
                } else {
                    currValue = max(nextBoard, searchDepth - 1, maxValue, minValue);
                    if (currValue < minValue) {
                        minValue = currValue;
                        bestMove = move;
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * Returns the best move using time-limited search based on the given board and end time.
     * @param board The current board.
     * @param bannedPiece The piece not to use for checking the opponent.
     * @param fixedTimeAIPlayer The AI player to notify after the best move at each depth has been computed.
     * @param endTime The time to stop searching.
     * @return The best move using time-limited search based on the given board and end time.
     */
    public Move iterativeDeepening(Board board, Piece bannedPiece,
                                   FixedTimeAIPlayer fixedTimeAIPlayer, long endTime) {
        PropertyChangeSupport support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(fixedTimeAIPlayer);
        stateToValueMap.clear();
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves());
        while (System.currentTimeMillis() < endTime) {
            int maxValue = Integer.MIN_VALUE;
            int minValue = Integer.MAX_VALUE;
            List<MoveEntry> moveEntries = new ArrayList<>();

            for (Move move : sortedMoves) {
                MoveTransition transition = board.getCurrPlayer().makeMove(move);
                if (transition.getMoveStatus().isDone()) {
                    Board nextBoard = transition.getNextBoard();
                    if (move.getMovedPiece().equals(bannedPiece)
                            && !move.getCapturedPiece().isPresent()
                            && nextBoard.getCurrPlayer().isInCheck()) continue;

                    int currValue;
                    if (board.getCurrPlayer().getAlliance().isRed()) {
                        currValue = min(nextBoard, currDepth - 1, maxValue, minValue);
                        if (currValue > maxValue) {
                            maxValue = currValue;
                            bestMove = move;
                        }
                    } else {
                        currValue = max(nextBoard, currDepth - 1, maxValue, minValue);
                        if (currValue < minValue) {
                            minValue = currValue;
                            bestMove = move;
                        }
                    }

                    moveEntries.add(new MoveEntry(move, currValue));
                }
            }

            support.firePropertyChange("currbestmove", currDepth, bestMove);
            sortedMoves = MoveSorter.valueSort(board.getCurrPlayer().getAlliance(), moveEntries);
            currDepth++;
        }

        return bestMove;
    }

    /**
     * The algorithm for the minimising player.
     */
    private int min(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver() || board.isGameDraw()) {
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int minValue = Integer.MAX_VALUE;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1, alpha, beta));
                beta = Math.min(beta, minValue);
                if (alpha >= beta) break;
            }
        }

        stateToValueMap.put(state, minValue);
        return minValue;
    }

    /**
     * The algorithm for the maximising player.
     */
    private int max(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver() || board.isGameDraw()) {
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int maxValue = Integer.MIN_VALUE;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1, alpha, beta));
                alpha = Math.max(alpha, maxValue);
                if (alpha >= beta) break;
            }
        }

        stateToValueMap.put(state, maxValue);
        return maxValue;
    }

    /**
     * Represents a state containing a board and the depth at which it was evaluated.
     */
    private static class BoardState {

        private final Board board;
        private final int depth;

        private BoardState(Board board, int depth) {
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
    private static class MoveSorter {

        private static final Comparator<Move> MOVE_COMPARATOR = (m1, m2) -> {
            int cpValue1 = m1.getCapturedPiece().isPresent()
                    ? m1.getCapturedPiece().get().getPieceType().getDefaultValue() : 0;
            int cpValue2 = m2.getCapturedPiece().isPresent()
                    ? m2.getCapturedPiece().get().getPieceType().getDefaultValue() : 0;

            if (cpValue1 != cpValue2) {
                return cpValue2 - cpValue1;
            }
            return m2.getMovedPiece().getPieceType().getDefaultValue()
                    - m1.getMovedPiece().getPieceType().getDefaultValue();
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
        private static List<Move> valueSort(Alliance alliance, List<MoveEntry> moveEntries) {
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
        private static List<Move> simpleSort(Collection<Move> moves) {
            List<Move> sortedMoves = new ArrayList<>(moves);

            sortedMoves.sort(MOVE_COMPARATOR);

            return Collections.unmodifiableList(sortedMoves);
        }
    }

    /**
     * Represents an entry containing a move and its value.
     */
    private static class MoveEntry {

        private final Move move;
        private final int value;

        private MoveEntry(Move move, int value) {
            this.move = move;
            this.value = value;
        }
    }
}
