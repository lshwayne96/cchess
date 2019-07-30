package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

public class Minimax implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;

    public Minimax(int searchDepth) {
        evaluator = StandardBoardEvaluator.getInstance();
        this.searchDepth = searchDepth;
    }

    @Override
    public Move execute(Board board) {
        Move bestMove = null;
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        int currValue;

        long start = System.currentTimeMillis();
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
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

        return bestMove;
    }

    private int min(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }

        int minValue = Integer.MAX_VALUE;
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1, alpha, beta));
                beta = Math.min(beta, minValue);
                if (alpha >= beta) break;
            }
        }

        return minValue;
    }

    private int max(Board board, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }

        int maxValue = Integer.MIN_VALUE;
        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1, alpha, beta));
                alpha = Math.max(alpha, maxValue);
                if (alpha >= beta) break;
            }
        }

        return maxValue;
    }
}
