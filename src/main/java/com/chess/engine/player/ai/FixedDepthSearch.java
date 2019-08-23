package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;
import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm.
 */
public class FixedDepthSearch extends MiniMax {

    private final int searchDepth;

    public FixedDepthSearch(Board board, Collection<Move> legalMoves, int searchDepth) {
        super(board, legalMoves);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        MoveEntry bestMoveEntry = null;

        int alpha = NEG_INF;
        int beta = POS_INF;
        int currDepth = 1;
        List<MoveEntry> oldMoveEntries = getLegalMoveEntries();

        while (currDepth <= searchDepth) {
            List<MoveEntry> newMoveEntries = alphaBetaRoot(oldMoveEntries, currDepth, alpha, beta);
            bestMoveEntry = newMoveEntries.get(0);

            int bestVal = bestMoveEntry.val;
            if (bestVal <= alpha || bestVal >= beta) {
                alpha = NEG_INF;
                beta = POS_INF;
                continue;
            }
            alpha = bestVal - ASP;
            beta = bestVal + ASP;

            oldMoveEntries = newMoveEntries;
            currDepth++;
        }
System.out.println((double)hits/calls*100);
        assert bestMoveEntry != null;
        return bestMoveEntry.move;
    }
}
