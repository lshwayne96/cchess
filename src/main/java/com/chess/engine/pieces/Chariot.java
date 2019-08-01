package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Chariot extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public Chariot(Coordinate position, Alliance alliance) {
        super(PieceType.CHARIOT, position, alliance);
    }

    @Override
    public Collection<Coordinate> getDestPositions(Board board) {
        List<Coordinate> destPositions = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);

            while (Board.isWithinBounds(destPosition)) {
                destPositions.add(destPosition);

                if (board.getPoint(destPosition).getPiece().isPresent()) break;

                destPosition = destPosition.add(vector);
            }
        }

        return Collections.unmodifiableList(destPositions);
    }

    @Override
    public Chariot movePiece(Move move) {
        return new Chariot(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }
}
