package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.General;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final int CHECK_VALUE = 30;
    private static final int CHECKMATE_VALUE = General.VALUE;

    public StandardBoardEvaluator() {
    }

    @Override
    public int evaluate(Board board, int depth) {
        return getPlayerScore(board, board.getRedPlayer(), depth)
                - getPlayerScore(board, board.getBlackPlayer(), depth);
    }

    private static int getPlayerScore(Board board, Player player, int depth) {
        int standardValue = getTotalPieceValue(player)
                //+ getMobility(player)
                + getCheckValue(player);

        // only need to get checkmate value for opp
        int checkmateValue = 0;
        if (board.getCurrPlayer().getOpponent().getAlliance().equals(player.getAlliance())) {
            checkmateValue = getCheckmateValue(player) * (depth + 1);
        }

        return standardValue + checkmateValue;
    }

    private static int getTotalPieceValue(Player player) {
        int totalPieceValue = 0;

        for (Piece piece : player.getActivePieces()) {
            totalPieceValue += piece.getValue();
        }

        return totalPieceValue;
    }

    private static int getMobility(Player player) {
        int ratio = (int) (player.getLegalMoves().size() * 100.0f / player.getOpponent().getLegalMoves().size());
        if (ratio > 1) {
            return Math.min(ratio, 300);
        } else {
            return Math.max(ratio, -300);
        }
    }

    private static int getCheckValue(Player player) {
        return player.getOpponent().isInCheck() ? CHECK_VALUE : 0;
    }

    private static int getCheckmateValue(Player player) {
        return player.getOpponent().isInCheckmate() ? CHECKMATE_VALUE : 0;
    }
}
