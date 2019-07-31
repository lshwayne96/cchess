package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

import java.util.Collection;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

public final class BoardEvaluator {

    private static final BoardEvaluator INSTANCE = new BoardEvaluator();
    private static final int CHECKMATE_VALUE = PieceType.GENERAL.getDefaultValue();

    private BoardEvaluator() {
    }

    public int evaluate(Board board, int depth) {
        return getPlayerScore(board, board.getRedPlayer(), depth)
                - getPlayerScore(board, board.getBlackPlayer(), depth)
                + getTotalRelationScore(board);
    }

    public static BoardEvaluator getInstance() {
        return INSTANCE;
    }

    private static int getPlayerScore(Board board, Player player, int depth) {
        int standardValue = getTotalPieceValue(board, player) + getTotalMobilityValue(player);

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

    private static int getTotalMobilityValue(Player player) {
        int totalMobilityValue = 0;

        for (Move move : player.getLegalMoves()) {
            totalMobilityValue += move.getMovedPiece().getType().getMobilityValue();
        }

        return totalMobilityValue;
    }

    private static int getTotalRelationScore(Board board) {
        int[] scores = new int[2];
        BoardStatus boardStatus = board.getStatus();

        for (Piece piece : board.getAllPieces()) {
            Player player = piece.getAlliance().isRed() ? board.getRedPlayer() : board.getBlackPlayer();
            int index = player.getAlliance().isRed() ? 0 : 1;
            int pieceValue = piece.getMaterialValue(boardStatus) + piece.getPositionValue();

            int oppTotalAttack = 0;
            int oppMinAttack = Integer.MAX_VALUE;
            int oppMaxAttack = 0;
            int playerTotalDefense = 0;
            int playerMinDefense = Integer.MAX_VALUE;
            int playerMaxDefense = 0;
            int flagValue = Integer.MAX_VALUE;
            int unitValue = pieceValue >> 3;

            // tabulate incoming attacks
            Collection<Move> attackMoves =
                    Player.calculateAttacksOnPoint(piece.getPosition(), player.getOpponent().getLegalMoves());
            for (Move move : attackMoves) {
                Piece oppPiece = move.getMovedPiece();
                int attackValue = oppPiece.getMaterialValue(boardStatus) + oppPiece.getPositionValue();
                if (attackValue < pieceValue && attackValue < flagValue) {
                    flagValue = attackValue;
                }
                oppMinAttack = Math.min(oppMinAttack, attackValue);
                oppMaxAttack = Math.max(oppMaxAttack, attackValue);
                oppTotalAttack += attackValue;
            }

            // tabulate defenses
            Collection<Piece> defendingPieces = player.calculateDefensesOnPoint(piece.getPosition());
            for (Piece defendingPiece : defendingPieces) {
                int defendingValue = defendingPiece.getMaterialValue(boardStatus);
                playerMinDefense = Math.min(playerMinDefense, defendingValue);
                playerMaxDefense = Math.max(playerMaxDefense, defendingValue);
                playerTotalDefense += defendingValue;
            }

            // calculate scores
            boolean isCurrTurn = board.getCurrPlayer().getAlliance().equals(player.getAlliance());
            if (oppTotalAttack == 0) {
                scores[index] += 10 * defendingPieces.size();
            } else {
                if (defendingPieces.isEmpty()) {
                    scores[index] -= isCurrTurn ? unitValue : 5 * unitValue;
                } else {
                    int playerScore = 0;
                    int oppScore = 0;

                    if (flagValue != Integer.MAX_VALUE) {
                        playerScore = unitValue;
                        oppScore = flagValue >> 3;
                    } else if (defendingPieces.size() == 1 && attackMoves.size() > 1
                            && oppMinAttack < (pieceValue + playerTotalDefense)) {
                        playerScore = unitValue + (playerTotalDefense >> 3);
                        oppScore = oppMinAttack >> 3;
                    } else if (defendingPieces.size() == 2 && attackMoves.size() == 3
                            && (oppTotalAttack - oppMaxAttack) < (pieceValue + playerTotalDefense)) {
                        playerScore = unitValue + (playerTotalDefense >> 3);
                        oppScore = (oppTotalAttack - oppMaxAttack) >> 3;
                    } else if (defendingPieces.size() == attackMoves.size()
                            && oppTotalAttack < (pieceValue + playerTotalDefense - playerMaxDefense)) {
                        playerScore = unitValue + ((playerTotalDefense - playerMaxDefense) >> 3);
                        oppScore = oppTotalAttack >> 3;
                    }

                    scores[index] -= isCurrTurn ? playerScore : 5 * playerScore;
                    scores[1 - index] -= isCurrTurn ? oppScore  : 5 * oppScore;
                }
            }
        }

        return scores[0] - scores[1];
    }

    private static int getCheckmateValue(Player player) {
        return player.getOpponent().isInCheckmate() ? CHECKMATE_VALUE : 0;
    }
}
