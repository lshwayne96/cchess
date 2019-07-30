package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final StandardBoardEvaluator INSTANCE = new StandardBoardEvaluator();
    private static final int CHECKMATE_VALUE = PieceType.GENERAL.getDefaultValue();

    private StandardBoardEvaluator() {
    }

    @Override
    public int evaluate(Board board, int depth) {
        return getPlayerScore(board, board.getRedPlayer(), depth)
                - getPlayerScore(board, board.getBlackPlayer(), depth);
    }

    public static StandardBoardEvaluator getInstance() {
        return INSTANCE;
    }

    private static int getPlayerScore(Board board, Player player, int depth) {
        int standardValue = getTotalPieceValue(board, player);
                //+ getMobility(player)

        // only need to get checkmate value for opp
        int checkmateValue = 0;
        if (board.getCurrPlayer().getOpponent().getAlliance().equals(player.getAlliance())) {
            checkmateValue = getCheckmateValue(player) * (depth + 1);
        }

        return standardValue + checkmateValue;
    }

    private static int getTotalPieceValue(Board board, Player player) {
        int totalPieceValue = 0;
        BoardStatus boardStatus = board.getStatus();

        for (Piece piece : player.getActivePieces()) {
            totalPieceValue += piece.getMaterialValue(boardStatus) + piece.getPositionValue();
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

    private static int getCheckmateValue(Player player) {
        return player.getOpponent().isInCheckmate() ? CHECKMATE_VALUE : 0;
    }
}
