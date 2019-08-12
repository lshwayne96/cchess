package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Cannon;
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
    private static final int CHECKMATE_VALUE = PieceType.GENERAL.getDefaultValue();
    private static final int HORSE_PENALTY = 50;
    private static final int CHARIOT_PENALTY = 25;
    private static final int CANNON_GENERAL_BONUS = 250;
    private static final int CANNON_HORSE_BONUS = 50;
    private static final int CANNON_ELEPHANT_BONUS = 50;
    private static final int CHARIOT_ONE_ADVISOR_BONUS = 200;
    private static final int CHARIOT_ZERO_ADVISOR_BONUS = 350;

    /**
     * Returns the heuristic value of the given board at the current search depth.
     * The higher the value, the better for the red player.
     * @param board The current board.
     * @param depth The current search depth.
     * @return The heuristic value of the given board at the current search depth.
     */
    static int evaluate(Board board, int depth) {
        if (board.getCurrPlayer().isInCheckmate()) {
            return board.getCurrPlayer().getAlliance().isRed()
                    ? (-1 * CHECKMATE_VALUE) * (depth + 1) : CHECKMATE_VALUE * (depth + 1);
        }

        BoardStatus boardStatus = board.getStatus();
        return getPieceScoreDiff(board, boardStatus)
                + getRelationScoreDiff(board, boardStatus)
                + getTotalMobilityValue(board.getRedPlayer()) - getTotalMobilityValue(board.getBlackPlayer())
                + (Table.getInstance().isAIRandomised() ? rand.nextInt(10) : 0);
    }

    /**
     * Returns the piece score difference between the two players on the given board.
     */
    private static int getPieceScoreDiff(Board board, BoardStatus boardStatus) {
        int redScore = 0, blackScore = 0;

        int redChariotCount = 0, redCannonCount = 0, redHorseCount = 0,
                redElephantCount = 0, redAdvisorCount = 0;
        for (Piece piece : board.getRedPieces()) {
            redScore += piece.getMaterialValue(boardStatus) + piece.getPositionValue();
            switch (piece.getPieceType()) {
                case CHARIOT:
                    redChariotCount++;
                    if (((Chariot) piece).isInStartingPosition()) {
                        redScore -= CHARIOT_PENALTY;
                    }
                    break;
                case CANNON:
                    redCannonCount++;
                    if (!boardStatus.equals(BoardStatus.END) && ((Cannon) piece).isMiddleFacingGeneral(board)) {
                        redScore += CANNON_GENERAL_BONUS;
                    }
                    break;
                case HORSE:
                    if (((Horse) piece).isInStartingPosition()) {
                        redScore -= HORSE_PENALTY;
                    }
                    redHorseCount++;
                    break;
                case ELEPHANT:
                    redElephantCount++;
                    break;
                case ADVISOR:
                    redAdvisorCount++;
                    break;
                default:
                    break;
            }
        }
        int blackChariotCount = 0, blackCannonCount = 0, blackHorseCount = 0,
                blackElephantCount = 0, blackAdvisorCount = 0;
        for (Piece piece : board.getBlackPieces()) {
            blackScore += piece.getMaterialValue(boardStatus) + piece.getPositionValue();
            switch (piece.getPieceType()) {
                case CHARIOT:
                    blackChariotCount++;
                    if (((Chariot) piece).isInStartingPosition()) {
                        blackScore -= CHARIOT_PENALTY;
                    }
                    break;
                case CANNON:
                    blackCannonCount++;
                    if (!boardStatus.equals(BoardStatus.END) && ((Cannon) piece).isMiddleFacingGeneral(board)) {
                        blackScore += CANNON_GENERAL_BONUS;
                    }
                    break;
                case HORSE:
                    if (((Horse) piece).isInStartingPosition()) {
                        blackScore -= HORSE_PENALTY;
                    }
                    blackHorseCount++;
                    break;
                case ELEPHANT:
                    blackElephantCount++;
                    break;
                case ADVISOR:
                    blackAdvisorCount++;
                    break;
                default:
                    break;
            }
        }

        if (boardStatus.equals(BoardStatus.END)) {
            // cannon+horse might be better than cannon+cannon or horse+horse
            if (redCannonCount > 0 && redHorseCount > 0) {
                redScore += CANNON_HORSE_BONUS;
            }
            if (blackCannonCount > 0 && blackHorseCount > 0) {
                blackScore += CANNON_HORSE_BONUS;
            }
            // cannon might be strong against lack of elephants
            if (blackElephantCount == 0) {
                redScore += CANNON_ELEPHANT_BONUS * redCannonCount;
            }
            if (redElephantCount == 0) {
                blackScore += CANNON_ELEPHANT_BONUS * blackCannonCount;
            }
        }
        if (!boardStatus.equals(BoardStatus.OPENING)) {
            // double chariots might be strong against lack of advisors
            if (redChariotCount == 2) {
                if (blackAdvisorCount == 1) {
                    redScore += CHARIOT_ONE_ADVISOR_BONUS;
                } else if (blackAdvisorCount == 0) {
                    redScore += CHARIOT_ZERO_ADVISOR_BONUS;
                }
            }
            if (blackChariotCount == 2) {
                if (redAdvisorCount == 1) {
                    blackScore += CHARIOT_ONE_ADVISOR_BONUS;
                } else if (redAdvisorCount == 0) {
                    blackScore += CHARIOT_ZERO_ADVISOR_BONUS;
                }
            }
        }

        return redScore - blackScore;
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
     * Returns the relationship score difference between the two players on the given board.
     */
    private static int getRelationScoreDiff(Board board, BoardStatus boardStatus) {
        int[] scores = new int[2];
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
}
