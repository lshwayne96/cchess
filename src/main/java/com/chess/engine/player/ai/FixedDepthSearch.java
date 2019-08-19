package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm.
 */
public class FixedDepthSearch extends MiniMax {

    private final int searchDepth;

    public FixedDepthSearch(Board board, List<Move> bannedMoves, int searchDepth) {
        super(board, bannedMoves);
        this.searchDepth = searchDepth;
    }

    public Move search2() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -negamax(board, searchDepth - 1);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
            board.unmakeMove(move);
        }

        return bestMove;
    }

    public Move search() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -alphaBeta(board, searchDepth - 1, NEG_INF, -bestVal, true);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
            board.unmakeMove(move);
        }

        return bestMove;
    }

    public Move search1() {
        Move bestMove = null;

        int currDepth = 1;
        int alpha = NEG_INF;
        int beta = POS_INF;
        List<Move> sortedMoves = MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves());

        while (currDepth <= searchDepth) {
            List<MoveEntry> moveEntries = new ArrayList<>();
            int bestVal = NEG_INF;

            for (Move move : sortedMoves) {
                if (bannedMoves.contains(move)) continue;
                board.makeMove(move);
                if (board.isLegalState()) {
                    int val = -alphaBeta(board, currDepth - 1, -beta, -alpha, true);
                    if (val > bestVal) {
                        bestVal = val;
                        bestMove = move;
                    }
                    moveEntries.add(new MoveEntry(move, val));
                }
                board.unmakeMove(move);
            }

            if (bestVal <= alpha || bestVal >= beta) {
                alpha = NEG_INF;
                beta = POS_INF;
                continue;
            }
            alpha = bestVal - ASP_WINDOW;
            beta = bestVal + ASP_WINDOW;

            sortedMoves = MoveSorter.valueSort(moveEntries);
            currDepth++;
        }

        return bestMove;
    }
}
