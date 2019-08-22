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
        MoveEntry bestMoveEntry = null;

        int alpha = NEG_INF;
        int beta = POS_INF;
        int currDepth = 1;
        List<MoveEntry> oldMoveEntries = new ArrayList<>();
        for (Move move : MoveSorter.simpleSort(legalMoves)) {
            oldMoveEntries.add(new MoveEntry(move, 0));
        }

        while (currDepth <= searchDepth) {
            List<MoveEntry> newMoveEntries = new ArrayList<>();
            bestMoveEntry = alphaBetaRoot(oldMoveEntries, newMoveEntries, currDepth, alpha, beta);

            int bestVal = bestMoveEntry.val;
            if (bestVal <= alpha || bestVal >= beta) {
                alpha = NEG_INF;
                beta = POS_INF;
                continue;
            }
            alpha = bestVal - ASP_WINDOW;
            beta = bestVal + ASP_WINDOW;

            oldMoveEntries = newMoveEntries;
            currDepth++;
        }

        assert bestMoveEntry != null;
        return bestMoveEntry.move;
    }

    public Move search1() {
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(legalMoves);

        while (currDepth <= searchDepth) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = NEG_INF;
            for (Move move : sortedMoves) {
                startBoard.makeMove(move);
                if (startBoard.isStateAllowed()) {
                    int val = -alphaBeta(startBoard, currDepth - 1, NEG_INF, -bestVal, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
                startBoard.unmakeMove(move);
            }

            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }
}
