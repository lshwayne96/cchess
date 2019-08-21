package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm.
 */
public class FixedDepthSearch extends MiniMax {

    private final int searchDepth;

    public FixedDepthSearch(Board currBoard, List<Move> bannedMoves, int searchDepth) {
        super(currBoard, bannedMoves);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -alphaBeta(transition.getNextBoard(), searchDepth - 1,
                        NEG_INF, -bestVal, true);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

    public Move search1() {
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves());

        while (currDepth <= searchDepth) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = NEG_INF;

            for (Move move : sortedMoves) {
                if (bannedMoves.contains(move)) continue;
                MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
                if (transition.getMoveStatus().isAllowed()) {
                    int val = -alphaBeta(transition.getNextBoard(), currDepth - 1,
                            NEG_INF, -bestVal, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
            }
            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }

    public Move search2() {
        Move bestMove = null;

        int maxValue = NEG_INF;
        int minValue = POS_INF;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                Board nextBoard = transition.getNextBoard();
                int currValue;
                if (currBoard.getCurrPlayer().getAlliance().isRed()) {
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
}
