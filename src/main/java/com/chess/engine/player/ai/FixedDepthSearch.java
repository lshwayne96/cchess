package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm.
 */
public class FixedDepthSearch extends MiniMax {

    private final int searchDepth;

    public FixedDepthSearch(Board board, Collection<Move> bannedMoves, int searchDepth) {
        super(board, bannedMoves);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(legalMoves);

        while (currDepth <= searchDepth) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = NEG_INF;
            for (Move move : sortedMoves) {
                board.makeMove(move);
                if (board.isStateAllowed()) {
                    int val = -alphaBeta(board, currDepth - 1, NEG_INF, -bestVal, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
                board.unmakeMove(move);
            }
            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }
}
