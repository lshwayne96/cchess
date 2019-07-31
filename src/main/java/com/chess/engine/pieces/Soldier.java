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

public class Soldier extends Piece {

    private static final Coordinate MOVE_VECTOR_BEFORE_RIVER = new Coordinate(1, 0);

    private static final List<Coordinate> MOVE_VECTORS_AFTER_RIVER =
            List.of(new Coordinate(1, 0), new Coordinate(0, -1),
                    new Coordinate(0, 1));

    public Soldier(Coordinate position, Alliance alliance) {
        super(PieceType.SOLDIER, position, alliance);
    }

    private boolean crossedRiver() {
        if (alliance.isRed()) {
            return position.getRow() < Board.RIVER_ROW_RED;
        } else {
            return position.getRow() > Board.RIVER_ROW_BLACK;
        }
    }

    @Override
    public Collection<Coordinate> getDestPositions(Board board) {
        List<Coordinate> destPositions = new ArrayList<>();

        if (!crossedRiver()) {
            Coordinate destPosition = position.add(MOVE_VECTOR_BEFORE_RIVER.scale(alliance.getDirection()));
            destPositions.add(destPosition);
        } else {
            for (Coordinate vector : MOVE_VECTORS_AFTER_RIVER) {
                Coordinate destPosition = position.add(vector.scale(alliance.getDirection()));
                if (Board.isWithinBounds(destPosition)) {
                    destPositions.add(destPosition);
                }
            }
        }

        return destPositions;
    }

    @Override
    public Soldier movePiece(Move move) {
        return new Soldier(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }
}
