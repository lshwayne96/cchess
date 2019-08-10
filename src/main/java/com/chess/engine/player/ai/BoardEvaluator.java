package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Chariot;
import com.chess.engine.pieces.Horse;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.chess.gui.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

/**
 * A helper class for evaluating a board.
 */
class BoardEvaluator {

    private static final Random rand = new Random();

    /**
     * Returns the heuristic value of the given board at the current search depth.
     * The higher the value, the better for the red player.
     * @param board The current board.
     * @param depth The current search depth.
     * @return The heuristic value of the given board at the current search depth.
     */
    static int evaluate(Board board, int depth) {
        return getPlayerScore(board, board.getRedPlayer(), depth)
                - getPlayerScore(board, board.getBlackPlayer(), depth)
                + getRelationScore(board)
                + (Table.getInstance().isAIRandomised() ? rand.nextInt(5) : 0);
    }

    /**
     * Returns a heuristic value of a player based on the given board and search depth.
     */
    private static int getPlayerScore(Board board, Player player, int depth) {
        int standardValue = getTotalPieceValue(board, player) + getTotalMobilityValue(player);

        // only need to get checkmate value for opp
        int checkmateValue = 0;
        if (board.getCurrPlayer().getOpponent().getAlliance().equals(player.getAlliance())) {
            checkmateValue = getCheckmateValue(player) * (depth + 1);
        }

        return standardValue + checkmateValue;
    }

    /**
     * Returns the total value of all active pieces of the given player on the given board.
     */
    private static int getTotalPieceValue(Board board, Player player) {
        int totalMaterialValue = 0;
        int totalPositionValue = 0;
        int totalMiscValue = 0;
        BoardStatus boardStatus = board.getStatus();

        int chariotCount = 0, cannonCount = 0, horseCount = 0;
        for (Piece piece : player.getActivePieces()) {
            totalMaterialValue += piece.getMaterialValue(boardStatus);
            totalPositionValue += piece.getPositionValue();
            switch (piece.getPieceType()) {
                case CHARIOT:
                    chariotCount++;
                    if (((Chariot) piece).isInStartingPosition()) {
                        totalMiscValue -= 30;
                    }
                    break;
                case CANNON:
                    cannonCount++;
                    break;
                case HORSE:
                    if (((Horse) piece).isInStartingPosition()) {
                        totalMiscValue -= 30;
                    }
                    horseCount++;
                    break;
                default:
                    break;
            }
        }
        int oppElephantCount = 0, oppAdvisorCount = 0;
        for (Piece piece : player.getOpponent().getActivePieces()) {
            switch (piece.getPieceType()) {
                case ELEPHANT:
                    oppElephantCount++;
                    break;
                case ADVISOR:
                    oppAdvisorCount++;
                    break;
                default:
                    break;
            }
        }

        if (boardStatus.equals(BoardStatus.END)) {
            // cannon+horse might be better than cannon+cannon or horse+horse
            if (cannonCount > 0 && horseCount > 0) {
                totalMiscValue += 50;
            }
            // cannon might be strong against lack of elephants
            if (oppElephantCount == 0) {
                totalMiscValue += 75 * cannonCount;
            }
        }
        if (!boardStatus.equals(BoardStatus.OPENING)) {
            // double chariots might be strong against lack of advisors
            if (chariotCount == 2) {
                if (oppAdvisorCount == 1) {
                    totalMiscValue += 200;
                } else if (oppAdvisorCount == 0) {
                    totalMiscValue += 400;
                }
            }
        }

        return totalMaterialValue + totalPositionValue + totalMiscValue;
    }

    /**
     * Returns the total mobility value of the given player.
     */
    private static int getTotalMobilityValue(Player player) {
        int totalMobilityValue = 0;

        for (Move move : player.getLegalMoves()) {
            totalMobilityValue += move.getMovedPiece().getPieceType().getMobilityValue();
        }

        return totalMobilityValue;
    }

    /**
     * Returns a relationship analysis score on the given board.
     * The higher the score, the better for the red player.
     */
    private static int getRelationScore(Board board) {
        int[] scores = new int[2];
        BoardStatus boardStatus = board.getStatus();
        Map<Piece, Collection<Move>> incomingAttacksMap = new HashMap<>();
        Map<Piece, Collection<Piece>> defendingPiecesMap = new HashMap<>();

        for (Move move : board.getAllLegalMoves()) {
            if (!move.getCapturedPiece().isPresent()) continue;
            Piece capturedPiece = move.getCapturedPiece().get();
            if (!incomingAttacksMap.containsKey(capturedPiece)) {
                Collection<Move> attackMoves = new ArrayList<>();
                attackMoves.add(move);
                incomingAttacksMap.put(capturedPiece, attackMoves);
            } else {
                incomingAttacksMap.get(capturedPiece).add(move);
            }
        }
        for (Piece piece : board.getAllPieces()) {
            for (Coordinate destPosition : piece.getDestPositions(board)) {
                board.getPoint(destPosition).getPiece().ifPresent(p -> {
                    if (p.getAlliance().equals(piece.getAlliance())) {
                        if (!defendingPiecesMap.containsKey(p)) {
                            Collection<Piece> defendingPieces = new ArrayList<>();
                            defendingPieces.add(piece);
                            defendingPiecesMap.put(p, defendingPieces);
                        } else {
                            defendingPiecesMap.get(p).add(piece);
                        }
                    }
                });
            }
        }

        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceType().equals(PieceType.GENERAL)) continue;

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
            Collection<Move> attackMoves = incomingAttacksMap.get(piece);
            if (attackMoves != null) {
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
            }

            // tabulate defenses
            Collection<Piece> defendingPieces = defendingPiecesMap.get(piece);
            if (defendingPieces != null) {
                for (Piece defendingPiece : defendingPieces) {
                    int defendingValue = defendingPiece.getMaterialValue(boardStatus);
                    playerMinDefense = Math.min(playerMinDefense, defendingValue);
                    playerMaxDefense = Math.max(playerMaxDefense, defendingValue);
                    playerTotalDefense += defendingValue;
                }
            }

            // calculate scores
            boolean isCurrTurn = board.getCurrPlayer().getAlliance().equals(player.getAlliance());
            if (attackMoves == null) {
                scores[index] += defendingPieces == null ? 0 : 10 * defendingPieces.size();
            } else {
                if (defendingPieces == null) {
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

    /**
     * Returns the checkmate value for the given player.
     */
    private static int getCheckmateValue(Player player) {
        return player.getOpponent().isInCheckmate() ? PieceType.GENERAL.getDefaultValue() : 0;
    }
}
