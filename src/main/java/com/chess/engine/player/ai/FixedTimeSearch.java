package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.gui.Table.*;

/**
 * Represents a fixed-time MiniMax algorithm.
 */
public class FixedTimeSearch extends MiniMax {

    private final PropertyChangeSupport support;
    private final long endTime;

    public FixedTimeSearch(Board board, Collection<Move> bannedMoves, FixedTimeAIPlayer fixedTimeAIPlayer, long endTime) {
        super(board, bannedMoves);
        this.endTime = endTime;
        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(fixedTimeAIPlayer);
    }

    @Override
    public Move search() {
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(legalMoves);

        while (System.currentTimeMillis() < endTime) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = NEG_INF;
            for (Move move : sortedMoves) {
                board.makeMove(move);
                if (board.isStateAllowed()) {
                    int val = -alphaBeta(board, currDepth - 1,
                            NEG_INF, -bestVal, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
                board.unmakeMove(move);
            }
            support.firePropertyChange("currbestmove", currDepth, bestMove);
            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }
}
