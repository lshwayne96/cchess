package com.chess.engine.player.ai;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtil;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;
import com.chess.engine.pieces.Advisor;
import com.chess.engine.pieces.General;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.chess.gui.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.chess.engine.pieces.Piece.*;

/**
 * A helper class for evaluating a board.
 */
class BoardEvaluator {

    private static final Random rand = new Random();
    private static final int RANDOM_BOUND = 10;
    private static final int CHECKMATE_VALUE = 10000;
    private static final Coordinate FORWARD_VECTOR = new Coordinate(1, 0);

    private static final int CHARIOT_BONUS = 100;
    private static final int CANNON_HORSE_BONUS = 20;
    private static final int DEFENSE_BONUS = 10;

    private static final int CANNON_ELEPHANT_BONUS = 100;
    private static final int CHARIOT_ONE_ADVISOR_BONUS = 400;
    private static final int CHARIOT_ZERO_ADVISOR_BONUS = 600;
    private static final int MAX_ATTACK_VALUE = 8;

    private static final int CHARIOT_PIN_FACTOR = 7;
    private static final int CANNON_PIN_FACTOR = 5;

    private static final int[] CANNON_HOLLOW_BONUS = {400, 400, 400, 375, 350, 325, 300, 0, 0, 0};
    private static final int[] CANNON_CENTRAL_BONUS = {150, 150, 150, 175, 200, 225, 250, 0, 0, 0};

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
     * Returns the score difference between the two players on the given board.
     */
    private static int getScoreDiff(Board board) {
        Player redPlayer = board.getRedPlayer();
        Player blackPlayer = board.getBlackPlayer();
        boolean isEndgame = board.isEndgame();
        int redScore = 0, blackScore = 0;


        // calculate total basic piece value and player attack value
        int redAttackValue = 0, blackAttackValue = 0;
        for (Piece piece : redPlayer.getActivePieces()) {
            redScore += piece.getValue(isEndgame);
            if (piece.crossedRiver()) {
                redAttackValue += piece.getPieceType().getAttackUnits();
            }
            if (piece.getPieceType().equals(PieceType.CANNON)) {
                redScore += getCannonBonus(piece, board, isEndgame);
            }
        }
        for (Piece piece : blackPlayer.getActivePieces()) {
            blackScore += piece.getValue(isEndgame);
            if (piece.crossedRiver()) {
                blackAttackValue += piece.getPieceType().getAttackUnits();
            }
            if (piece.getPieceType().equals(PieceType.CANNON)) {
                blackScore += getCannonBonus(piece, board, isEndgame);
            }
        }

        // adjust player attack value according to difference in value units
        int redValueUnits = redPlayer.getTotalValueUnits();
        int blackValueUnits = blackPlayer.getTotalValueUnits();
        if (redValueUnits > blackValueUnits) {
            redAttackValue += (redValueUnits - blackValueUnits) * 2;
        } else {
            blackAttackValue += (blackValueUnits - redValueUnits) * 2;
        }
        redAttackValue = Math.min(redAttackValue, MAX_ATTACK_VALUE);
        blackAttackValue = Math.min(blackAttackValue, MAX_ATTACK_VALUE);

        // get piece counts
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

        // chariot(s) might be strong against no chariot
        if (redChariotCount > 0 && blackChariotCount == 0 && (blackCannonCount + blackHorseCount) <= 2) {
            redScore += CHARIOT_BONUS;
        }
        if (blackChariotCount > 0 && redChariotCount == 0 && (redCannonCount + redHorseCount) <= 2) {
            blackScore += CHARIOT_BONUS;
        }
        // cannon+horse might be better than cannon+cannon or horse+horse
        if (redCannonCount > 0 && redHorseCount > 0) {
            redScore += CANNON_HORSE_BONUS;
        }
        if (blackCannonCount > 0 && blackHorseCount > 0) {
            blackScore += CANNON_HORSE_BONUS;
        }
        // cannon might be strong against lack of elephants
        if (redCannonCount > blackElephantCount) {
            redScore += (redCannonCount - blackElephantCount) * CANNON_ELEPHANT_BONUS
                    * redAttackValue / MAX_ATTACK_VALUE;
        }
        if (blackCannonCount > redElephantCount) {
            blackScore += (blackCannonCount - redElephantCount) * CANNON_ELEPHANT_BONUS
                    * blackAttackValue / MAX_ATTACK_VALUE;
        }
        // double chariots might be strong against lack of advisors
        if (redChariotCount == 2) {
            if (blackAdvisorCount == 1) {
                redScore += CHARIOT_ONE_ADVISOR_BONUS * redAttackValue / MAX_ATTACK_VALUE;
            } else if (blackAdvisorCount == 0) {
                redScore += CHARIOT_ZERO_ADVISOR_BONUS * redAttackValue / MAX_ATTACK_VALUE;
            }
        }
        if (blackChariotCount == 2) {
            if (redAdvisorCount == 1) {
                blackScore += CHARIOT_ONE_ADVISOR_BONUS * blackAttackValue / MAX_ATTACK_VALUE;
            } else if (redAdvisorCount == 0) {
                blackScore += CHARIOT_ZERO_ADVISOR_BONUS * blackAttackValue / MAX_ATTACK_VALUE;
            }
        }


        return redScore - blackScore + (Table.getInstance().isAIRandomised() ? rand.nextInt(RANDOM_BOUND) : 0);
    }

    /**
     * Returns the relationship score difference between the two players on the given board.
     *//*
    private static int getRelationScoreDiff(Board board, boolean isEndgame) {
        int[] scores = new int[2];
        Map<Piece, List<Move>> incomingAttacksMap = new HashMap<>();
        Map<Piece, List<Piece>> defendingPiecesMap = new HashMap<>();
        Collection<Move> allLegalMoves = board.getAllLegalMoves();
        Collection<Piece> allPieces = board.getAllPieces();

        // calculate all attacks and defenses on the board
        for (Move move : allLegalMoves) {
            if (!move.isCapture()) continue;
            Piece capturedPiece = move.getCapturedPiece().get();
            if (!incomingAttacksMap.containsKey(capturedPiece)) {
                List<Move> attackMoves = new ArrayList<>();
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
                            List<Piece> defendingPieces = new ArrayList<>();
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

            List<Move> attackMoves = incomingAttacksMap.get(piece);
            List<Piece> defendingPieces = defendingPiecesMap.get(piece);
            if (attackMoves == null && defendingPieces == null) continue;

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
            if (defendingPieces != null) {
                for (Piece defendingPiece : defendingPieces) {
                    int defendingValue = defendingPiece.getValue(isEndgame);
                    playerMinDefense = Math.min(playerMinDefense, defendingValue);
                    playerMaxDefense = Math.max(playerMaxDefense, defendingValue);
                    playerTotalDefense += defendingValue;
                }
            }

            // check pins
            if (!piece.getPieceType().equals(PieceType.CHARIOT) && attackMoves != null && defendingPieces != null) {
                if (defendingPieces.size() == 1 && defendingPieces.get(0).getPieceType().equals(PieceType.CHARIOT)) {
                    for (Move move : attackMoves) {
                        Piece oppPiece = move.getMovedPiece();
                        if (BoardUtil.sameColOrRow(piece.getPosition(), oppPiece.getPosition(),
                                defendingPieces.get(0).getPosition())) {
                            if (oppPiece.getPieceType().equals(PieceType.CHARIOT)
                                    && defendingPiecesMap.get(defendingPieces.get(0)) == null) {
                                scores[index] -= pieceValue / CHARIOT_PIN_FACTOR;
                            } else if (oppPiece.getPieceType().equals(PieceType.CANNON)
                                    && !piece.getPieceType().equals(PieceType.CANNON)) {
                                scores[index] -= pieceValue / CANNON_PIN_FACTOR;
                            }
                        }
                    }
                }
            }

            // calculate scores
            boolean isCurrTurn = board.getCurrPlayer().getAlliance().equals(player.getAlliance());
            if (attackMoves == null) {
                scores[index] += defendingPieces == null ? 0 : DEFENSE_BONUS * defendingPieces.size();
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
    }*/

    /**
     * Returns the bonus value of the given cannon on the given board.
     */
    private static int getCannonBonus(Piece cannon, Board board, boolean isEndgame) {
        Coordinate cannonPosition = cannon.getPosition();
        Alliance cannonAlliance = cannon.getAlliance();

        // check position of cannon
        int cannonFile = BoardUtil.colToFile(cannonPosition.getCol(), cannonAlliance);
        int cannonRank = BoardUtil.rowToRank(cannonPosition.getRow(), cannonAlliance);
        if (cannonFile != 5 || cannonRank > 7) {
            return 0;
        }

        // check if opponent general and advisors are in starting positions
        Point oppGeneralStartPoint = board.getPoint(General.getStartingPosition(cannonAlliance.opposite()));
        if (oppGeneralStartPoint.isEmpty()
                || !oppGeneralStartPoint.getPiece().get().getPieceType().equals(PieceType.GENERAL)) {
            return 0;
        }
        for (Coordinate pos : Advisor.getStartingPositions(cannon.getAlliance().opposite())) {
            Point oppAdvisorStartPoint = board.getPoint(pos);
            if (oppAdvisorStartPoint.isEmpty()
                    || !oppAdvisorStartPoint.getPiece().get().getPieceType().equals(PieceType.ADVISOR)) {
                return 0;
            }
        }

        // check pieces between cannon and opponent general
        int pieceCount = 0;
        Coordinate position = cannonPosition.add(FORWARD_VECTOR.scale(cannonAlliance.getDirection()));
        while (BoardUtil.isWithinBounds(position)) {
            Point point = board.getPoint(position);
            if (!point.isEmpty()) {
                Piece piece = point.getPiece().get();
                if (piece.getPieceType().equals(PieceType.GENERAL)) break;
                pieceCount++;
                if (pieceCount > 2) {
                    return 0;
                }
            }
            position = position.add(FORWARD_VECTOR.scale(cannonAlliance.getDirection()));
        }

        if (pieceCount == 0) {
            return isEndgame ? CANNON_HOLLOW_BONUS[cannonRank - 1] / 2 : CANNON_HOLLOW_BONUS[cannonRank - 1];
        }

        // pieceCount == 2
        int centralHorseRow = BoardUtil.rankToRow(9, cannonAlliance);
        int centralHorseCol = BoardUtil.fileToCol(5, cannonAlliance);
        Point centralHorsePoint = board.getPoint(new Coordinate(centralHorseRow, centralHorseCol));
        if (!centralHorsePoint.isEmpty()
                && centralHorsePoint.getPiece().get().getPieceType().equals(PieceType.HORSE)) {
            return CANNON_CENTRAL_BONUS[cannonRank - 1];
        }

        return 0;
    }
}
