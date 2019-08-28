package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtil;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Horse extends Piece {

    private static final List<Coordinate> FIRST_MOVE_VECTORS =
            List.of(new Coordinate(-1, 0),
                    new Coordinate(0, -1),
                    new Coordinate(1, 0),
                    new Coordinate(0, 1));
    private static final List<List<Coordinate>> SECOND_MOVE_VECTORS_LIST =
            List.of(List.of(new Coordinate(-1, -1), new Coordinate(-1 ,1)),
                    List.of(new Coordinate(-1, -1), new Coordinate(1, -1)),
                    List.of(new Coordinate(1, -1), new Coordinate(1, 1)),
                    List.of(new Coordinate(-1, 1), new Coordinate(1, 1)));

    public Horse(Coordinate position, Alliance alliance) {
        super(PieceType.HORSE, position, alliance);
    }

    @Override
    public Collection<Coordinate> getDestPositions(Board board) {
        List<Coordinate> destPositions = new ArrayList<>();

        for (int i = 0; i < FIRST_MOVE_VECTORS.size(); i++) {
            Coordinate firstPosition = position.add(FIRST_MOVE_VECTORS.get(i));
            if (!(BoardUtil.isWithinBounds(firstPosition)
                    && board.getPoint(firstPosition).isEmpty())) continue;

            for (Coordinate second : SECOND_MOVE_VECTORS_LIST.get(i)) {
                Coordinate destPosition = firstPosition.add(second);
                if (BoardUtil.isWithinBounds(destPosition)) {
                    destPositions.add(destPosition);
                }
            }
        }

        return Collections.unmodifiableList(destPositions);
    }

    @Override
    public Horse movePiece(Move move) {
        return new Horse(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public Horse getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new Horse(mirrorPosition, alliance);
    }
}
