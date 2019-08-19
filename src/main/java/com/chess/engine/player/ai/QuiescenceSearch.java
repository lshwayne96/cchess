package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.List;

/**
 * Represents a fixed-depth MiniMax algorithm with Quiescence search.
 */
public class QuiescenceSearch extends MiniMax {

    private static final int Q_DEPTH = 2;

    private final int searchDepth;

    public QuiescenceSearch(Board currBoard, List<Move> bannedMoves, int searchDepth) {
        super(currBoard, bannedMoves);
        this.searchDepth = searchDepth;
    }

    public Move search() {
        Move bestMove = null;
        int bestVal = NEG_INF;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -normal(transition.getNextBoard(), searchDepth - 1,
                        NEG_INF, -bestVal, true);
                if (val > bestVal) {
                    bestVal = val;
                    bestMove = move;
                }
            }
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
            Board nextBoard = board.makeNullMove();
            int val = -normal(nextBoard, depth - 1 - R, -beta, -beta + 1, false);
            if (val >= beta) {
                return val;
            }
        }

        int bestVal = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -normal(transition.getNextBoard(), depth - 1, -beta, -alpha, true);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
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

            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = -quiescence(transition.getNextBoard(), depth - 1, -beta, -alpha);
                if (val >= beta) {
                    return val;
                }
                if (val > bestVal) {
                    bestVal = val;
                    alpha = Math.max(alpha, val);
                }
            }
        }

        return bestVal;
    }

    public Move search1() {
        Move bestMove = null;

        int maxValue = NEG_INF;
        int minValue = POS_INF;

        for (Move move : MoveSorter.simpleSort(currBoard.getCurrPlayer().getLegalMoves())) {
            if (bannedMoves.contains(move)) continue;

            MoveTransition transition = currBoard.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                Board nextBoard = transition.getNextBoard();
                int currValue;
                if (currBoard.getCurrPlayer().getAlliance().isRed()) {
                    currValue = normalMin(nextBoard, searchDepth - 1, maxValue, minValue, true);
                    if (currValue > maxValue) {
                        maxValue = currValue;
                        bestMove = move;
                    }
                } else {
                    currValue = normalMax(nextBoard, searchDepth - 1, maxValue, minValue, true);
                    if (currValue < minValue) {
                        minValue = currValue;
                        bestMove = move;
                    }
                }
            }
        }

        return bestMove;
    }


    int normalMin(Board board, int depth, int alpha, int beta, boolean allowNull) {
        if (board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }
        if (depth <= 0) {
            return board.isQuiet() ? BoardEvaluator.evaluate(board, depth)
                    : quiescenceMax(board, alpha, beta);
        }

        if (allowNull && !board.getCurrPlayer().isInCheck() && board.allowNullMove()) {
            Board nextBoard = board.makeNullMove();
            int val = normalMax(nextBoard, depth - 1 - R, alpha, alpha + 1, false);
            if (alpha >= val) {
                return val;
            }
        }

        int minValue = POS_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                minValue = Math.min(minValue, normalMax(transition.getNextBoard(), depth - 1, alpha, beta, true));
                beta = Math.min(beta, minValue);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        return minValue;
    }

    int normalMax(Board board, int depth, int alpha, int beta, boolean allowNull) {
        if (board.isGameOver()) {
            return BoardEvaluator.evaluate(board, depth);
        }
        if (depth <= 0) {
            return board.isQuiet() ? BoardEvaluator.evaluate(board, depth)
                    : quiescenceMin(board, alpha, beta);
        }

        if (allowNull && !board.getCurrPlayer().isInCheck() && board.allowNullMove()) {
            Board nextBoard = board.makeNullMove();
            int val = normalMin(nextBoard, depth - 1 - R, alpha, alpha + 1, false);
            if (val >= beta) {
                return val;
            }
        }

        int maxValue = NEG_INF;
        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                maxValue = Math.max(maxValue, normalMin(transition.getNextBoard(), depth - 1, alpha, beta, true));
                alpha = Math.max(alpha, maxValue);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        return maxValue;
    }

    private int quiescenceMin(Board board, int alpha, int beta) {
        int standPat = BoardEvaluator.evaluate(board, 0);
        if (standPat >= beta || board.isQuiet()) {
            return beta;
        }
        alpha = Math.max(alpha, standPat);

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (!move.getCapturedPiece().isPresent()) break;

            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = quiescenceMax(transition.getNextBoard(), alpha, beta);
                beta = Math.min(beta, val);
                if (alpha >= beta) {
                    return beta;
                }
            }
        }

        return beta;
    }

    private int quiescenceMax(Board board, int alpha, int beta) {
        int standPat = BoardEvaluator.evaluate(board, 0);
        if (alpha >= standPat || board.isQuiet()) {
            return alpha;
        }
        beta = Math.min(beta, standPat);

        for (Move move : MoveSorter.simpleSort(board.getCurrPlayer().getLegalMoves())) {
            if (!move.getCapturedPiece().isPresent()) break;

            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isAllowed()) {
                int val = quiescenceMin(transition.getNextBoard(), alpha, beta);
                alpha = Math.max(alpha, val);
                if (alpha >= beta) {
                    return alpha;
                }
            }
        }

        return alpha;
    }
}
