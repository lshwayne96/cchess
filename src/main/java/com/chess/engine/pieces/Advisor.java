package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Advisor extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, -1), new Coordinate(1, -1),
                    new Coordinate(1, 1), new Coordinate(-1, 1));

    private static final Set<Coordinate> VALID_POSITIONS_RED =
            Set.of(new Coordinate(7, 3), new Coordinate(7, 5),
                    new Coordinate(8, 4),
                    new Coordinate(9, 3), new Coordinate(9, 5));

    private static final Set<Coordinate> VALID_POSITIONS_BLACK =
            Set.of(new Coordinate(0, 3), new Coordinate(0, 5),
                    new Coordinate(1,4),
                    new Coordinate(2, 3), new Coordinate(2, 5));

    public Advisor(Coordinate position, Alliance alliance) {
        super(PieceType.ADVISOR, position, alliance);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            if (isValidPosition(destPosition)) {
                Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                destPiece.ifPresentOrElse(p -> {
                    if (!p.alliance.equals(this.alliance)) {
                        legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                    }
                }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
            }
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board, Collection<Attack> attacks, Collection<Defense> defenses) {
        List<Move> legalMoves = new ArrayList<>();
        List<Piece> attackedPieces = new ArrayList<>();
        List<Piece> defendedPieces = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            if (isValidPosition(destPosition)) {
                Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                destPiece.ifPresentOrElse(p -> {
                    if (!p.alliance.equals(this.alliance)) {
                        legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                        attackedPieces.add(p);
                    } else {
                        defendedPieces.add(p);
                    }
                }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
            }
        }
        Attack attack = new Attack(this, attackedPieces);
        Defense defense = new Defense(this, defendedPieces);

        attacks.add(attack);
        defenses.add(defense);
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Advisor movePiece(Move move) {
        return new Advisor(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public Advisor getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new Advisor(mirrorPosition, alliance);
    }

    /**
     * Checks if the given position is valid for this advisor.
     */
    private boolean isValidPosition(Coordinate positionToTest) {
        if (alliance.isRed()) {
            return VALID_POSITIONS_RED.contains(positionToTest);
        } else {
            return VALID_POSITIONS_BLACK.contains(positionToTest);
        }
    }
}
