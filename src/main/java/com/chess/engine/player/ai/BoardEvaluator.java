package com.chess.engine.player.ai;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtil;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Point;
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

import static com.chess.engine.board.Board.*;
import static com.chess.engine.pieces.Piece.*;

/**
 * A helper class for evaluating a board.
 */
class BoardEvaluator {

    private static final Random rand = new Random();
    private static final int RANDOM_BOUND = 10;
    private static final Coordinate FORWARD_VECTOR = new Coordinate(1, 0);
    private static final Coordinate PALACE_CENTRE_RED = new Coordinate(8, 4);
    private static final Coordinate PALACE_CENTRE_BLACK = new Coordinate(1, 4);

    private static final int CHECKMATE_VALUE = 10000;
    private static final int MAX_SIMPLE_UNITS = 66;
    private static final int MAX_ATTACK_VALUE = 8;

    private static final int GENERAL_PENALTY = 100;
    private static final int CHARIOT_BONUS = 100;
    private static final int CANNON_HORSE_BONUS = 20;
    private static final int DEFENSE_BONUS_FACTOR = 50;

    private static final int CANNON_ELEPHANT_BONUS = 100;
    private static final int CHARIOT_ADVISOR_BONUS = 400;

    private static final int CHARIOT_PIN_FACTOR = 7;
    private static final int CANNON_PIN_FACTOR = 5;

    private static final int[] CANNON_HOLLOW_BONUS = {400, 400, 400, 375, 350, 325, 300, 0, 0, 0};
    private static final int[] CANNON_CENTRAL_BONUS = {150, 150, 150, 175, 200, 225, 250, 0, 0, 0};
    private static final int CANNON_CENTRAL_REDUCTION = 4;
    private static final int CANNON_CHARIOT_BONUS = 100;
    private static final int[] CANNON_BOTTOM_BONUS = {200, 150,  0,  0,  0,  0,  0, 150, 200};

    /**
     * Returns the heuristic value of the given board.
     * The higher the value, the better for the red player.
     * @param board The current board.
     * @return The heuristic value of the given board.
     */
    static int evaluate(Board board) {
        return board.isCurrPlayerCheckmated() ? getCheckmateValue(board.getCurrPlayer().getAlliance(), 0)
                : (getScoreDiff(board) + (Table.getInstance().isAIRandomised() ? rand.nextInt(RANDOM_BOUND) : 0));
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
        Player redPlayer = board.getPlayer(Alliance.RED);
        Player blackPlayer = board.getPlayer(Alliance.BLACK);
        int redScore = 0, blackScore = 0;
        int[] pieceValues = new int[90];

        // add mobility values
        redScore += redPlayer.getTotalMobilityValue();
        blackScore += blackPlayer.getTotalMobilityValue();

        // calculate total simple units, player value units and attack value
        // tabulate piece counts; get cannons and chariots
        int totalSimpleUnits = 0;
        int redValueUnits = 0, blackValueUnits = 0;
        int redAttackValue = 0, blackAttackValue = 0;
        int[] redPieceCount = new int[7];
        int[] blackPieceCount = new int[7];
        Collection<Piece> redCannons = new ArrayList<>();
        Collection<Piece> redChariots = new ArrayList<>();
        Collection<Piece> blackCannons = new ArrayList<>();
        Collection<Piece> blackChariots = new ArrayList<>();
        for (Piece piece : redPlayer.getActivePieces()) {
            totalSimpleUnits += piece.getPieceType().getSimpleUnits();
            redValueUnits += piece.getPieceType().getValueUnits();
            if (piece.crossedRiver()) {
                redAttackValue += piece.getPieceType().getAttackUnits();
            }
            PieceType pieceType = piece.getPieceType();
            redPieceCount[pieceType.ordinal()]++;
            if (pieceType.equals(PieceType.CANNON)) {
                redCannons.add(piece);
            } else if (pieceType.equals(PieceType.CHARIOT)) {
                redChariots.add(piece);
            }
        }
        for (Piece piece : blackPlayer.getActivePieces()) {
            totalSimpleUnits += piece.getPieceType().getSimpleUnits();
            blackValueUnits += piece.getPieceType().getValueUnits();
            if (piece.crossedRiver()) {
                blackAttackValue += piece.getPieceType().getAttackUnits();
            }
            PieceType pieceType = piece.getPieceType();
            blackPieceCount[pieceType.ordinal()]++;
            if (pieceType.equals(PieceType.CANNON)) {
                blackCannons.add(piece);
            } else if (pieceType.equals(PieceType.CHARIOT)) {
                blackChariots.add(piece);
            }
        }
        // adjust total simple units and player attack values
        totalSimpleUnits = (2 * MAX_SIMPLE_UNITS - totalSimpleUnits) * totalSimpleUnits / MAX_SIMPLE_UNITS;
        if (redValueUnits > blackValueUnits) {
            redAttackValue += (redValueUnits - blackValueUnits) * 2;
        } else if (blackValueUnits > redValueUnits) {
            blackAttackValue += (blackValueUnits - redValueUnits) * 2;
        }
        redAttackValue = Math.min(redAttackValue, MAX_ATTACK_VALUE);
        blackAttackValue = Math.min(blackAttackValue, MAX_ATTACK_VALUE);

        // calculate basic piece values
        for (Piece piece : redPlayer.getActivePieces()) {
            int value = getPieceValue(piece, totalSimpleUnits);
            pieceValues[BoardUtil.positionToIndex(piece.getPosition())] = value;
            redScore += value;
        }
        for (Piece piece : blackPlayer.getActivePieces()) {
            int value = getPieceValue(piece, totalSimpleUnits);
            pieceValues[BoardUtil.positionToIndex(piece.getPosition())] = value;
            blackScore += value;
        }

        int redChariotCount = redPieceCount[PieceType.CHARIOT.ordinal()];
        int redCannonCount = redPieceCount[PieceType.CANNON.ordinal()];
        int redHorseCount = redPieceCount[PieceType.HORSE.ordinal()];
        int redElephantCount = redPieceCount[PieceType.ELEPHANT.ordinal()];
        int redAdvisorCount = redPieceCount[PieceType.ADVISOR.ordinal()];
        int blackChariotCount = blackPieceCount[PieceType.CHARIOT.ordinal()];
        int blackCannonCount = blackPieceCount[PieceType.CANNON.ordinal()];
        int blackHorseCount = blackPieceCount[PieceType.HORSE.ordinal()];
        int blackElephantCount = blackPieceCount[PieceType.ELEPHANT.ordinal()];
        int blackAdvisorCount = blackPieceCount[PieceType.ADVISOR.ordinal()];

        // general on palace centre might be bad when having 2 advisors
        if (redAdvisorCount == 2) {
           if (board.getPoint(PALACE_CENTRE_RED).getPiece()
                   .map(p -> p.getPieceType().equals(PieceType.GENERAL)).orElse(false)) {
               redScore -= GENERAL_PENALTY;
           }
        }
        if (blackAdvisorCount == 2) {
            if (board.getPoint(PALACE_CENTRE_BLACK).getPiece()
                    .map(p -> p.getPieceType().equals(PieceType.GENERAL)).orElse(false)) {
                blackScore -= GENERAL_PENALTY;
            }
        }
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
        if (redChariotCount == 2 && blackAdvisorCount < 2) {
            redScore += CHARIOT_ADVISOR_BONUS * redAttackValue / MAX_ATTACK_VALUE;
        }
        if (blackChariotCount == 2 && redAdvisorCount < 2) {
            blackScore += CHARIOT_ADVISOR_BONUS * blackAttackValue / MAX_ATTACK_VALUE;
        }
        // get cannon special bonuses
        for (Piece cannon : redCannons) {
            redScore += getCannonBonus(board, totalSimpleUnits, redAttackValue, cannon, redChariots);
        }
        for (Piece cannon : blackCannons) {
            blackScore += getCannonBonus(board, totalSimpleUnits, blackAttackValue, cannon, blackChariots);
        }
/*
        // store all attacks and defenses into maps
        Map<Piece, List<Piece>> incomingAttacksMap = new HashMap<>();
        Map<Piece, List<Piece>> defendingPiecesMap = new HashMap<>();
        storeRelationIntoMap(redPlayer.getAttacks(), incomingAttacksMap);
        storeRelationIntoMap(blackPlayer.getAttacks(), incomingAttacksMap);
        storeRelationIntoMap(redPlayer.getDefenses(), defendingPiecesMap);
        storeRelationIntoMap(blackPlayer.getDefenses(), defendingPiecesMap);

        // calculate relation scores
        int relationScoreDiff =
                calculateRelationScore(pieceValues, redPlayer.getActivePieces(),
                        incomingAttacksMap, defendingPiecesMap)
                - calculateRelationScore(pieceValues, blackPlayer.getActivePieces(),
                        incomingAttacksMap, defendingPiecesMap);
*/

        return redScore - blackScore;// + relationScoreDiff;
    }

    /**
     * Returns the bonus value of the given cannon on the given board.
     */
    private static int getCannonBonus(Board board, int totalSimpleUnits, int attackValue,
                                      Piece cannon, Collection<Piece> chariots) {
        Coordinate cannonPosition = cannon.getPosition();
        Alliance cannonAlliance = cannon.getAlliance();
        Alliance oppAlliance = cannonAlliance.opposite();
        int cannonFile = BoardUtil.colToFile(cannonPosition.getCol(), cannonAlliance);
        int cannonRank = BoardUtil.rowToRank(cannonPosition.getRow(), cannonAlliance);

        // check if opponent general is in starting position
        Point oppGeneralStartPoint = board.getPoint(General.getStartingPosition(oppAlliance));
        if (oppGeneralStartPoint.isEmpty()
                || !oppGeneralStartPoint.getPiece().get().getPieceType().equals(PieceType.GENERAL)) {
            return 0;
        }

        if (cannonFile == 5 && cannonRank <= 7) { // central cannon
            AdvisorStructure oppAdvStruct = board.getAdvisorStructure(oppAlliance);
            if (oppAdvStruct.equals(AdvisorStructure.OTHER)) {
                return 0;
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

            if (pieceCount == 0) { // advisors at start
                return CANNON_HOLLOW_BONUS[cannonRank - 1]
                        * (totalSimpleUnits + MAX_SIMPLE_UNITS) / (2 * MAX_SIMPLE_UNITS);
            }

            // pieceCount == 2, left/right advisors
            // check opp central horse
            Coordinate centralHorsePosition = oppAlliance.isRed() ? PALACE_CENTRE_RED : PALACE_CENTRE_BLACK;
            Point centralHorsePoint = board.getPoint(centralHorsePosition);
            if (!centralHorsePoint.isEmpty()
                    && centralHorsePoint.getPiece().get().getPieceType().equals(PieceType.HORSE)
                    && centralHorsePoint.getPiece().get().getAlliance().equals(oppAlliance)) {
                return CANNON_CENTRAL_BONUS[cannonRank - 1];
            }

            int bonus;
            if (chariots.isEmpty()) {
                return 0;
            } else {
                bonus = CANNON_CENTRAL_BONUS[cannonRank - 1] / CANNON_CENTRAL_REDUCTION;
            }
            // check if chariot at opp general free file
            int freeCol;
            if (oppAdvStruct.equals(AdvisorStructure.LEFT)) { // right free
                freeCol = BoardUtil.fileToCol(4, oppAlliance);
            } else { // left free
                freeCol = BoardUtil.fileToCol(6, oppAlliance);
            }
            for (Piece chariot : chariots) {
                if (chariot.getPosition().getCol() == freeCol) {
                    bonus += CANNON_CHARIOT_BONUS;
                }
            }

            return bonus;
        } else if (cannonRank == 10) { // bottom cannon
            AdvisorStructure oppAdvStruct = board.getAdvisorStructure(oppAlliance);
            if ((oppAdvStruct.equals(AdvisorStructure.LEFT) && cannonFile > 5)
                    || (oppAdvStruct.equals(AdvisorStructure.RIGHT) && cannonFile < 5)) {
                return CANNON_BOTTOM_BONUS[cannonFile - 1] * attackValue / MAX_ATTACK_VALUE;
            }
        }

        return 0;
    }

    /**
     * Stores the given collection of relations into a map.
     */
    private static void storeRelationIntoMap(Collection<? extends Relation> relations, Map<Piece, List<Piece>> map) {
        for (Relation relation : relations) {
            Piece piece = relation.getPiece();
            for (Piece relatedPiece : relation.getRelatedPieces()) {
                if (map.containsKey(relatedPiece)) {
                    map.get(relatedPiece).add(piece);
                } else {
                    List<Piece> pieces = new ArrayList<>();
                    pieces.add(piece);
                    map.put(relatedPiece, pieces);
                }
            }
        }
    }

    /**
     * Returns the total relation score of the given pieces (same alliance).
     */
    private static int calculateRelationScore(int[] pieceValues, Collection<Piece> pieces,
                                              Map<Piece, List<Piece>> incomingAttacksMap,
                                              Map<Piece, List<Piece>> defendingPiecesMap) {
        int score = 0;

        for (Piece piece : pieces) {
            PieceType pieceType = piece.getPieceType();
            if (pieceType.equals(PieceType.GENERAL)) continue;

            int pieceValue = pieceValues[BoardUtil.positionToIndex(piece.getPosition())];
            List<Piece> attackingPieces = incomingAttacksMap.get(piece);
            List<Piece> defendingPieces = defendingPiecesMap.get(piece);

            // add defense scores
            if (defendingPieces != null && !pieceType.equals(PieceType.CHARIOT)) {
                score += pieceValue / DEFENSE_BONUS_FACTOR;
            }

            if (attackingPieces == null || defendingPieces == null || defendingPieces.size() != 1
                    || !defendingPieces.get(0).getPieceType().equals(PieceType.CHARIOT)) continue;

            // add pin penalty
            for (Piece attackingPiece : attackingPieces) {
                if (attackingPiece.getPieceType().equals(PieceType.CHARIOT)
                        && !piece.getPieceType().equals(PieceType.CHARIOT)
                        && defendingPiecesMap.get(defendingPieces.get(0)) == null) {
                    if (BoardUtil.sameColOrRow(attackingPiece.getPosition(), defendingPieces.get(0).getPosition())) {
                        score -= pieceValue / CHARIOT_PIN_FACTOR;
                    }
                } else if (attackingPiece.getPieceType().equals(PieceType.CANNON)
                        && !piece.getPieceType().equals(PieceType.CANNON)) {
                    if (BoardUtil.sameColOrRow(attackingPiece.getPosition(), defendingPieces.get(0).getPosition())) {
                        score -= pieceValue / CANNON_PIN_FACTOR;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Returns the weighted value of the given piece.
     */
    private static int getPieceValue(Piece piece, int totalSimpleUnits) {
        return (piece.getMidgameValue() * totalSimpleUnits
                + piece.getEndgameValue() * (MAX_SIMPLE_UNITS - totalSimpleUnits)) / MAX_SIMPLE_UNITS;
    }
}
