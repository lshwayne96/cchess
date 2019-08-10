package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Cannon extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public Cannon(Coordinate position, Alliance alliance) {
        super(PieceType.CANNON, position, alliance);
    }

    @Override
    public Collection<Coordinate> getDestPositions(Board board) {
        List<Coordinate> destPositions = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            boolean jumped = false;

            while (Board.isWithinBounds(destPosition)) {
                Point destPoint = board.getPoint(destPosition);
                Optional<Piece> destPiece = destPoint.getPiece();

                if (!jumped && !destPiece.isPresent()) { // before first piece
                    destPositions.add(destPosition);
                } else if (!jumped) { // reached first piece
                    jumped = true;
                } else if (destPiece.isPresent()) { // after first piece
                    destPositions.add(destPosition);
                    break;
                }

                destPosition = destPosition.add(vector);
            }
        }

        return Collections.unmodifiableList(destPositions);
    }

    @Override
    public Cannon movePiece(Move move) {
        return new Cannon(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public Cannon getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new Cannon(mirrorPosition, alliance);
    }

    /**
     * Checks if this cannon is directly facing the opponent's general on the middle column.
     * @param board The current board.
     * @return true if this cannon is directly facing the opponent's general on the middle column, false otherwise.
     */
    public boolean isMiddleFacingGeneral(Board board) {
        if (position.getCol() != 4
                || (alliance.isRed() && position.getRow() < 3)
                || (!alliance.isRed() && position.getRow() > 6)) return false;

        Coordinate forwardVector = MOVE_VECTORS.get(2);
        Coordinate destPosition = position.add(forwardVector.scale(alliance.getDirection()));
        while (Board.isWithinBounds(destPosition)) {
            Point destPoint = board.getPoint(destPosition);
            if (destPoint.getPiece().isPresent()) {
                Piece destPiece = destPoint.getPiece().get();
                return destPiece.getPieceType().equals(PieceType.GENERAL);
            }
            destPosition = destPosition.add(forwardVector.scale(alliance.getDirection()));
        }
        return false;
    }
}
