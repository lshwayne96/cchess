package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.chess.engine.pieces.Piece.*;

/**
 * Represents a player of Chinese Chess.
 */
public class Player {

    private final Alliance alliance;
    private final Collection<Piece> activePieces;
    private final Collection<Move> legalMoves;
    private final boolean isInCheck;
    private final int[] pieceCount;
    private final int totalMobilityValue;
    private final Collection<Attack> attacks;
    private final Collection<Defense> defenses;

    public Player(Alliance alliance, Collection<Piece> activePieces, Collection<Move> legalMoves,
                  Collection<Move> oppLegalMoves, int[] pieceCount, int totalMobilityValue,
                  Collection<Attack> attacks, Collection<Defense> defenses) {
        this.alliance = alliance;
        this.activePieces = activePieces;
        this.legalMoves = legalMoves;
        isInCheck = !getIncomingAttacks(findPlayerGeneral().getPosition(), oppLegalMoves).isEmpty();
        this.pieceCount = pieceCount;
        this.totalMobilityValue = totalMobilityValue;
        this.attacks = attacks;
        this.defenses = defenses;
    }

    private Piece findPlayerGeneral() {
        for (Piece piece : activePieces) {
            if (piece.getPieceType().equals(PieceType.GENERAL)) {
                return piece;
            }
        }

        throw new RuntimeException(getAlliance().toString() + " GENERAL missing");
    }

    /**
     * Returns a collection of opponent moves that attack the given position.
     */
    private static Collection<Move> getIncomingAttacks(Coordinate position, Collection<Move> opponentMoves) {
        List<Move> attacksOnPoint = new ArrayList<>();

        for (Move move : opponentMoves) {
            if (move.getDestPosition().equals(position)) {
                attacksOnPoint.add(move);
            }
        }

        return Collections.unmodifiableList(attacksOnPoint);
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public Collection<Piece> getActivePieces() {
        return Collections.unmodifiableCollection(activePieces);
    }

    public Collection<Move> getLegalMoves() {
        return Collections.unmodifiableCollection(legalMoves);
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    public int[] getPieceCountArray() {
        return pieceCount;
    }

    public int getPieceCount(PieceType pieceType) {
        return pieceCount[pieceType.ordinal()];
    }

    public int getTotalMobilityValue() {
        return totalMobilityValue;
    }

    public int getTotalValueUnits() {
        int totalValueUnits = 0;
        for (PieceType pieceType : PieceType.PIECE_TYPES) {
            totalValueUnits += pieceType.getValueUnits() * getPieceCount(pieceType);
        }
        return totalValueUnits;
    }

    public Collection<Attack> getAttacks() {
        return Collections.unmodifiableCollection(attacks);
    }

    public Collection<Defense> getDefenses() {
        return Collections.unmodifiableCollection(defenses);
    }
}
