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

    public Move search1() {
        Move bestMove = null;

        int alpha = NEG_INF;
        int beta = POS_INF;
        int currDepth = 1;
        List<MoveEntry> sortedMoveEntries = new ArrayList<>();
        for (Move move : MoveSorter.simpleSort(legalMoves)) {
            sortedMoveEntries.add(new MoveEntry(move, 0));
        }

        while (currDepth <= searchDepth) {
            sortedMoveEntries = alphaBetaRoot(sortedMoveEntries, currDepth, alpha, beta);
            MoveEntry bestMoveEntry = sortedMoveEntries.get(0);
            bestMove = bestMoveEntry.move;
            int bestVal = bestMoveEntry.val;

            if (bestVal <= alpha || bestVal >= beta) {
                alpha = NEG_INF;
                beta = POS_INF;
                continue;
            }
            alpha = bestVal - ASP_WINDOW;
            beta = bestVal + ASP_WINDOW;

            currDepth++;
        }

        return bestMove;
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

    public Move search2() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            board.makeMove(move);
            if (board.isStateAllowed()) {
                int val = -alphaBeta1(board, searchDepth - 1, NEG_INF, -bestVal);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
            board.unmakeMove(move);
        }

        return bestMove;
    }
}
