package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm with Quiescence search.
 */
public class QuiescenceSearch extends MiniMax {

    private static final int Q_DEPTH = 2;

    private final int searchDepth;

    public QuiescenceSearch(Board board, List<Move> bannedMoves, int searchDepth) {
        super(board, bannedMoves);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -normal(board, searchDepth - 1,
                        NEG_INF, -bestVal, true);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
            board.unmakeMove(move);
        }

        return bestMove;
    }

    private int normal(Board board, int depth, int alpha, int beta, boolean allowNull) {
        if (board.isGameOver()) {
            int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
            return BoardEvaluator.evaluate(board, depth) * color;
        }
        if (depth <= 0) {
            int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
            return board.isQuiet() ? BoardEvaluator.evaluate(board, depth) * color
                    : quiescence(board, Q_DEPTH, -beta, -alpha);
        }

        if (allowNull && !board.getCurrPlayer().isInCheck() && board.allowNullMove()) {
            board.passMove();
            int val = -normal(board, depth - 1 - R, -beta, -beta + 1, false);
            if (val >= beta) {
                return val;
            }
        }

        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -normal(board, depth - 1, -beta, -alpha, true);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
            board.unmakeMove(move);
        }

        return bestVal;
    }

    private int quiescence(Board board, int depth, int alpha, int beta) {
        /*
        int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
        int standPat = -BoardEvaluator.evaluate(board, 0) * color;
        if (standPat >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, standPat);
         */
        if (depth <= 0 || board.isGameOver() || board.isQuiet()) {
            int color = board.getCurrPlayer().getAlliance().isRed() ? 1 : -1;
            return BoardEvaluator.evaluate(board, 0) * color;
        }

        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (!move.getCapturedPiece().isPresent()) break;

            board.makeMove(move);
            if (board.isLegalState()) {
                int val = -quiescence(board, depth - 1, -beta, -alpha);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
            board.unmakeMove(move);
        }

        return bestVal;
    }
}
