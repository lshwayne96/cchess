package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

/**
 * Represents a fixed-depth MiniMax algorithm.
 */
public class FixedDepthSearch extends MiniMax {

    private final int searchDepth;

    public FixedDepthSearch(Board currBoard, Move bannedMove, int searchDepth) {
        super(currBoard, bannedMove);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        Move bestMove = null;
        int bestVal = Integer.MIN_VALUE + 1;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (move.equals(bannedMove)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -alphaBeta(transition.getNextBoard(), searchDepth - 1,
                        Integer.MIN_VALUE + 1, -bestVal, true);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }
/*
    public Move search() {
        Move bestMove = null;

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (move.equals(bannedMove)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                Board nextBoard = transition.getNextBoard();
                int currValue;
                if (currBoard.getCurrPlayer().getAlliance().isRed()) {
                    currValue = min(nextBoard, searchDepth - 1, maxValue, minValue, true);
                    if (currValue > maxValue) {
                        maxValue = currValue;
                        bestMove = move;
                    }
                } else {
                    currValue = max(nextBoard, searchDepth - 1, maxValue, minValue, true);
                    if (currValue < minValue) {
                        minValue = currValue;
                        bestMove = move;
                    }
                }
            }
        }

        return bestMove;
    }*/
}
