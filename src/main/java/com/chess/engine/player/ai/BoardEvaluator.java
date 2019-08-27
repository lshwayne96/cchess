package com.chess.engine.player.ai;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Cannon;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.chess.gui.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.chess.engine.pieces.Piece.*;

/**
 * A helper class for evaluating a board.
 */
class BoardEvaluator {
//TODO: piece value/count + boardstatus update when making move, hollow cannon, central horse, soldier value, trap, mobility
    private static final Random rand = new Random();
    private static final int CHECKMATE_VALUE = 10000;
    private static final int CANNON_HOLLOW_BONUS = 50;
    private static final int CANNON_HORSE_BONUS = 5;
    private static final int CANNON_ELEPHANT_BONUS = 10;
    private static final int CHARIOT_ONE_ADVISOR_BONUS = 35;
    private static final int CHARIOT_ZERO_ADVISOR_BONUS = 70;

    /**
     * Returns the heuristic value of the given board.
     * The higher the value, the better for the red player.
     * @param board The current board.
     * @return The heuristic value of the given board.
     */
    static int evaluate(Board board) {
        return board.isCurrPlayerCheckmated() ? getCheckmateValue(board.getCurrPlayer().getAlliance(), 0)
                : getScoreDiff(board);
    }

    /**
     * Returns the checkmate value for the checkmated alliance and given depth.
     * @param alliance The alliance of the checkmated player.
     * @param depth The current depth.
     * @return The checkmate value for the checkmated alliance and given depth.
     */
    static int getCheckmateValue(Alliance alliance, int depth) {
        return alliance.isRed() ? (-1 * CHECKMATE_VALUE) * (depth + 1) : CHECKMATE_VALUE * (depth + 1);
    }

    /**
     * Returns the score difference between the two players.
     */
    private static int getScoreDiff(Board board) {
        return getPieceScoreDiff(board)
                + getRelationScoreDiff(board)
                + getTotalMobilityValue(board.getRedPlayer()) - getTotalMobilityValue(board.getBlackPlayer())
                + (Table.getInstance().isAIRandomised() ? rand.nextInt(2) : 0);
    }

    /**
     * Returns the piece score difference between the two players on the given board.
     */
    private static int getPieceScoreDiff(Board board) {
        int redScore = 0, blackScore = 0;
        Player redPlayer = board.getRedPlayer();
        Player blackPlayer = board.getBlackPlayer();
        boolean isEndgame = board.isEndgame();
        for (Piece piece : redPlayer.getActivePieces()) {
            redScore += piece.getValue(isEndgame);
            if (piece.getPieceType().equals(PieceType.CANNON)) {
                redScore += (((Cannon) piece).isMiddleFacingGeneral(board) ? CANNON_HOLLOW_BONUS : 0);
            }
        }
        for (Piece piece : blackPlayer.getActivePieces()) {
            blackScore += piece.getValue(isEndgame);
            if (piece.getPieceType().equals(PieceType.CANNON)) {
                blackScore += (((Cannon) piece).isMiddleFacingGeneral(board) ? CANNON_HOLLOW_BONUS : 0);
            }
        }

        int redChariotCount = redPlayer.getPieceCount(PieceType.CHARIOT);
        int redCannonCount = redPlayer.getPieceCount(PieceType.CANNON);
        int redHorseCount = redPlayer.getPieceCount(PieceType.HORSE);
        int redElephantCount = redPlayer.getPieceCount(PieceType.ELEPHANT);
        int redAdvisorCount = redPlayer.getPieceCount(PieceType.ADVISOR);
        int blackChariotCount = blackPlayer.getPieceCount(PieceType.CHARIOT);
        int blackCannonCount = blackPlayer.getPieceCount(PieceType.CANNON);
        int blackHorseCount = blackPlayer.getPieceCount(PieceType.HORSE);
        int blackElephantCount = blackPlayer.getPieceCount(PieceType.ELEPHANT);
        int blackAdvisorCount = blackPlayer.getPieceCount(PieceType.ADVISOR);

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
    private static int getRelationScoreDiff(Board board) {
        int[] scores = new int[2];
        Map<Piece, Collection<Move>> incomingAttacksMap = new HashMap<>();
        Map<Piece, Collection<Piece>> defendingPiecesMap = new HashMap<>();
        Collection<Move> allLegalMoves = board.getAllLegalMoves();
        Collection<Piece> allPieces = board.getAllPieces();
        boolean isEndgame = board.isEndgame();

        // calculate all attacks and defenses on the board
        for (Move move : allLegalMoves) {
            if (!move.isCapture()) continue;
            Piece capturedPiece = move.getCapturedPiece().get();
            if (!incomingAttacksMap.containsKey(capturedPiece)) {
                Collection<Move> attackMoves = new ArrayList<>();
                attackMoves.add(move);
                incomingAttacksMap.put(capturedPiece, attackMoves);
            } else {
                incomingAttacksMap.get(capturedPiece).add(move);
            }
        }
        for (Piece piece : allPieces) {
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

        for (Piece piece : allPieces) {
            if (piece.getPieceType().equals(PieceType.GENERAL)) continue;

            Player player = piece.getAlliance().isRed() ? board.getRedPlayer() : board.getBlackPlayer();
            int index = player.getAlliance().isRed() ? 0 : 1;
            int pieceValue = piece.getValue(isEndgame);

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
                    int attackValue = oppPiece.getValue(isEndgame);
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
                    int defendingValue = defendingPiece.getValue(isEndgame);
                    playerMinDefense = Math.min(playerMinDefense, defendingValue);
                    playerMaxDefense = Math.max(playerMaxDefense, defendingValue);
                    playerTotalDefense += defendingValue;
                }
            }

            // calculate scores
            boolean isCurrTurn = board.getCurrPlayer().getAlliance().equals(player.getAlliance());
            if (attackMoves == null) {
                scores[index] += defendingPieces == null ? 0 : 2 * defendingPieces.size();
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
