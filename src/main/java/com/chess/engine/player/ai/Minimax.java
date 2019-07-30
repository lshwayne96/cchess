package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import java.util.HashMap;
import java.util.Map;

public class Minimax implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private final MoveOrdering moveOrdering;
    private final Map<BoardState, Integer> stateToValueMap;
    private int boards;

    public Minimax(int searchDepth) {
        evaluator = StandardBoardEvaluator.getInstance();
        this.searchDepth = searchDepth;
        moveOrdering = MoveOrdering.getInstance();
        stateToValueMap = new HashMap<>();
    }

    @Override
    public Move execute(Board board) {
        stateToValueMap.clear();
        boards = 0;
        Move bestMove = null;
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        int currValue;

        long start = System.currentTimeMillis();
        for (Move move : moveOrdering.getSortedMoves(board)) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                if (board.getCurrPlayer().getAlliance().isRed()) {
                    currValue = min(transition.getNextBoard(), searchDepth - 1, maxValue, minValue);
                    if (currValue > maxValue) {
                        maxValue = currValue;
                        bestMove = move;
                    }
                } else {
                    currValue = max(transition.getNextBoard(), searchDepth - 1, maxValue, minValue);
                    if (currValue < minValue) {
                        minValue = currValue;
                        bestMove = move;
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(bestMove.toString() + " " + (end-start) + "ms");
        System.out.println(boards + " boards");

        return bestMove;
    }

    private int min(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            boards++;
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int minValue = Integer.MAX_VALUE;
        for (Move move : moveOrdering.getSortedMoves(board)) {
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

    private int max(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            boards++;
            return evaluator.evaluate(board, depth);
        }
        BoardState state = new BoardState(board, depth);
        Integer value = stateToValueMap.get(state);
        if (value != null) {
            return value;
        }

        int maxValue = Integer.MIN_VALUE;
        for (Move move : moveOrdering.getSortedMoves(board)) {
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
