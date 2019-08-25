package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.List;

import static com.chess.gui.Table.*;

/**
 * Represents a fixed-time MiniMax algorithm.
 */
public class FixedTimeSearch extends MiniMax {

    private final PropertyChangeSupport support;
    private final long endTime;

    public FixedTimeSearch(Board board, Collection<Move> legalMoves, FixedTimeAIPlayer fixedTimeAIPlayer, long endTime) {
        super(board, legalMoves, true);
        this.endTime = endTime;
        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(fixedTimeAIPlayer);
    }

    public Move search() {
        MoveEntry bestMoveEntry = null;

        int alpha = NEG_INF;
        int beta = POS_INF;
        int currDepth = 1;
        List<MoveEntry> oldMoveEntries = getLegalMoveEntries(); // initialise move entries (simple-sorted)

        while (System.currentTimeMillis() < endTime) {
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

            // notify AI with current best move
            support.firePropertyChange("currbestmove", currDepth, bestMoveEntry.move);
            oldMoveEntries = newMoveEntries;
            currDepth++;
        }

        assert bestMoveEntry != null;
        return bestMoveEntry.move;
    }
}
