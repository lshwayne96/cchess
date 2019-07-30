package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MoveOrdering {

    private final BoardEvaluator evaluator;

    private static final MoveOrdering INSTANCE = new MoveOrdering();
    private static final int SEARCH_DEPTH = 2;

    private MoveOrdering() {
        evaluator = StandardBoardEvaluator.getInstance();
    }

    public List<Move> getSortedMoves(Board board) {
        List<MoveEntry> moveEntryList = new ArrayList<>();
        List<Move> sortedMoves = new ArrayList<>();

        for (Move move : board.getCurrPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                int attackBonus = getAttackBonus(board.getCurrPlayer(), move);
                int value = board.getCurrPlayer().getAlliance().isRed() ?
                        min(transition.getNextBoard(), SEARCH_DEPTH - 1) :
                        max(transition.getNextBoard(), SEARCH_DEPTH - 1);
                moveEntryList.add(new MoveEntry(move, value + attackBonus));
            }
        }
        moveEntryList.sort((e1, e2) -> e2.getScore() - e1.getScore());
        for (MoveEntry moveEntry : moveEntryList) {
            sortedMoves.add(moveEntry.getMove());
        }

        return Collections.unmodifiableList(sortedMoves);
    }

    private int getAttackBonus(Player player, Move move) {
        int bonus = move.getCapturedPiece().isPresent() ? 1000 : 0;
        return bonus * (player.getAlliance().isRed() ? 1 : -1);
    }

    private int min(Board board, int depth) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }

        int minValue = Integer.MAX_VALUE;
        for (Move move : simpleSortMoves(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                minValue = Math.min(minValue, max(transition.getNextBoard(), depth - 1));
            }
        }

        return minValue;
    }

    private int max(Board board, int depth) {
        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board, depth);
        }

        int maxValue = Integer.MIN_VALUE;
        for (Move move : simpleSortMoves(board.getCurrPlayer().getLegalMoves())) {
            MoveTransition transition = board.getCurrPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                maxValue = Math.max(maxValue, min(transition.getNextBoard(), depth - 1));
            }
        }

        return maxValue;
    }

    public static MoveOrdering getInstance() {
        return INSTANCE;
    }

    private static List<Move> simpleSortMoves(Collection<Move> moves) {
        List<Move> simpleSortedMoves = new ArrayList<>(moves);
        simpleSortedMoves.sort((m1, m2) ->
                Boolean.compare(m2.getCapturedPiece().isPresent(), m1.getCapturedPiece().isPresent()));

        return simpleSortedMoves;
    }

    private static class MoveEntry {

        final Move move;
        final int score;

        MoveEntry(Move move, int score) {
            this.move = move;
            this.score = score;
        }

        Move getMove() {
            return move;
        }

        int getScore() {
            return score;
        }
    }
}
