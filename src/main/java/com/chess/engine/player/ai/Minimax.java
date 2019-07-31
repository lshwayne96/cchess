package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import java.util.HashMap;
import java.util.Map;

/**
 * MiniMax algorithm with Alpha-Beta pruning and Hashing of calculated board states
 */
public class Minimax {

    private static Minimax INSTANCE = new Minimax();
    private static final int SORT_DEPTH = 0;

    private final BoardEvaluator evaluator;
    private final MoveOrdering moveOrdering;
    private final Map<BoardState, Integer> stateToValueMap;

    // check for repeated checking
    private int checkCounter;
    private Piece prevMovedPiece;

    private Minimax() {
        evaluator = BoardEvaluator.getInstance();
        moveOrdering = MoveOrdering.getInstance();
        stateToValueMap = new HashMap<>();
        checkCounter = 0;
    }

    public static Minimax getInstance() {
        return INSTANCE;
    }

    public Move execute(Board board, int searchDepth) {
        stateToValueMap.clear();
        Move bestMove = null;
        Board bestNextBoard = null;

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        int currValue;

        long start = System.currentTimeMillis();
        for (Move move : moveOrdering.getSortedMoves(board, SORT_DEPTH)) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                Board nextBoard = transition.getNextBoard();
                if (move.getMovedPiece().equals(prevMovedPiece) && nextBoard.getCurrPlayer().isInCheck()
                        && checkCounter + 1 > 3) continue;

                if (board.getCurrPlayer().getAlliance().isRed()) {
                    currValue = min(nextBoard, searchDepth - 1, maxValue, minValue);
                    if (currValue > maxValue) {
                        maxValue = currValue;
                        bestMove = move;
                        bestNextBoard = nextBoard;
                    }
                } else {
                    currValue = max(nextBoard, searchDepth - 1, maxValue, minValue);
                    if (currValue < minValue) {
                        minValue = currValue;
                        bestMove = move;
                        bestNextBoard = nextBoard;
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(bestMove.toString() + " " + (end-start) + "ms");

        if (bestMove.getMovedPiece().equals(prevMovedPiece) && bestNextBoard.getCurrPlayer().isInCheck()) {
            checkCounter++;
        } else {
            checkCounter = 0;
        }
        prevMovedPiece = bestNextBoard.getPoint(bestMove.getDestPosition()).getPiece().get();

        return bestMove;
    }

    private int min(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int minValue = Integer.MAX_VALUE;
        for (Move move : moveOrdering.getSortedMoves(board, SORT_DEPTH)) {
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

    private int max(Board board, int depth, int a, int b) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int alpha = a, beta = b;
        int maxValue = Integer.MIN_VALUE;
        for (Move move : moveOrdering.getSortedMoves(board, SORT_DEPTH)) {
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

    private static class BoardState {
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
}
