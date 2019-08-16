package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import static com.chess.gui.Table.*;

/**
 * Represents a fixed-time MiniMax algorithm.
 */
public class FixedTimeSearch extends MiniMax {

    private final PropertyChangeSupport support;
    private final long endTime;

    public FixedTimeSearch(Board currBoard, Move bannedMove, FixedTimeAIPlayer fixedTimeAIPlayer, long endTime) {
        super(currBoard, bannedMove);
        this.endTime = endTime;
        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(fixedTimeAIPlayer);
    }

    @Override
    public Move search() {
        Move bestMove = null;

        int currDepth = 1;
        List<Move> sortedMoves = MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves());

        while (System.currentTimeMillis() < endTime) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = Integer.MIN_VALUE + 1;
            for (Move move : sortedMoves) {
                if (move.equals(bannedMove)) continue;

                MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
                if (transition.getMoveStatus().isAllowed()) {
                    int val = -alphaBeta(transition.getNextBoard(), currDepth - 1,
                            Integer.MIN_VALUE + 1, -bestVal, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
            }

            support.firePropertyChange("currbestmove", currDepth, bestMove);
            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }
}
