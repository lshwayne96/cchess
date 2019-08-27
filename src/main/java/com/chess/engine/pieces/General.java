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

public class General extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));
    private static final Coordinate STARTING_POSITION_RED = new Coordinate(9, 4);
    private static final Coordinate STARTING_POSITION_BLACK = new Coordinate(0, 4);

    public General(Coordinate position, Alliance alliance) {
        super(PieceType.GENERAL, position, alliance);
    }

    @Override
    public Collection<Coordinate> getDestPositions(Board board) {
        List<Coordinate> destPositions = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            if (isValidPosition(destPosition)) {
                destPositions.add(destPosition);
            }
        }

        // flying general move (only used for enforcing check)
        Coordinate vector = new Coordinate(1, 0).scale(alliance.getDirection());
        Coordinate currPosition = position.add(vector);
        while (Board.isWithinBounds(currPosition)) {
            Point currPoint = board.getPoint(currPosition);
            Optional<Piece> piece = currPoint.getPiece();
            if (piece.isPresent()) {
                if (piece.get().getPieceType().equals(PieceType.GENERAL)) {
                    destPositions.add(currPosition);
                }
                break;
            }
            currPosition = currPosition.add(vector);
        }

        return Collections.unmodifiableList(destPositions);
    }

    @Override
    public General movePiece(Move move) {
        return new General(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public General getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new General(mirrorPosition, alliance);
    }

    /**
     * Checks if the given position is valid for this general.
     */
    private boolean isValidPosition(Coordinate positionToTest) {
        int row = positionToTest.getRow();
        int col = positionToTest.getCol();

        if (alliance.isRed()) {
            return (row >= 7 && row <= 9) && (col >= 3 && col <= 5);
        } else {
            return (row >= 0 && row <= 2) && (col >= 3 && col <= 5);
        }
    }

    /**
     * Checks if this general is in the starting position.
     * @return true if this general is in the starting position, false otherwise.
     */
    public boolean isInStartingPosition() {
        return alliance.isRed() ? STARTING_POSITION_RED.equals(position) : STARTING_POSITION_BLACK.equals(position);
    }
}
