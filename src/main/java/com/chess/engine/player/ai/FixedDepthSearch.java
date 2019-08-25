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
        super(board, legalMoves, searchDepth > 6);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        MoveEntry bestMoveEntry = null;

        int alpha = NEG_INF;
        int beta = POS_INF;
        int currDepth = 1;
        List<MoveEntry> oldMoveEntries = getLegalMoveEntries(); // initialise move entries (simple-sorted)

        while (currDepth <= searchDepth) {
            // get value-sorted move entries for the current depth (best move at the front)
            List<MoveEntry> newMoveEntries = alphaBetaRoot(oldMoveEntries, currDepth, alpha, beta);
            bestMoveEntry = newMoveEntries.get(0);

            int bestVal = bestMoveEntry.val;
            if (bestVal <= alpha || bestVal >= beta) { // reset aspiration window
                alpha = NEG_INF;
                beta = POS_INF;
                continue;
            }
            // narrow aspiration window
            alpha = bestVal - ASP;
            beta = bestVal + ASP;

            oldMoveEntries = newMoveEntries;
            currDepth++;
        }

        assert bestMoveEntry != null;
        return bestMoveEntry.move;
    }
}
