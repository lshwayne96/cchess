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
import java.util.Set;

public class Elephant extends Piece {

    private static final double VALUE = 2;

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, -1), new Coordinate(1, -1),
                    new Coordinate(1, 1), new Coordinate(-1, 1));

    private static final Set<Coordinate> VALID_POSITIONS_RED =
            Set.of(new Coordinate(5, 2), new Coordinate(5, 6),
                    new Coordinate(7, 0), new Coordinate(7, 4), new Coordinate(7, 8),
                    new Coordinate(9, 2), new Coordinate(9, 6));

    private static final Set<Coordinate> VALID_POSITIONS_BLACK =
            Set.of(new Coordinate(0, 2), new Coordinate(0, 6),
                    new Coordinate(2, 0), new Coordinate(2, 4), new Coordinate(2, 8),
                    new Coordinate(4, 2), new Coordinate(4, 6));

    public Elephant(Coordinate position, Alliance alliance) {
        super(PieceType.ELEPHANT, position, alliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate firstPosition = position.add(vector);
            if (!(Board.isWithinBounds(firstPosition)
                    && board.getPoint(firstPosition).isEmpty())) continue;

            Coordinate destPosition = firstPosition.add(vector);
            if (!isValidPosition(destPosition)) continue;

            Point destPoint = board.getPoint(destPosition);
            Optional<Piece> destPiece = destPoint.getPiece();
            destPiece.ifPresentOrElse(p -> {
                if (this.alliance != p.alliance) {
                    legalMoves.add(new Move(board, this, destPosition, p));
                }
            }, () -> legalMoves.add(new Move(board, this, destPosition)));
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    @Override
    public Elephant movePiece(Move move) {
        return new Elephant(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public double getValue() {
        return VALUE;
    }

    private boolean isValidPosition(Coordinate positionToTest) {
        if (alliance.isRed()) {
            return VALID_POSITIONS_RED.contains(positionToTest);
        } else {
            return VALID_POSITIONS_BLACK.contains(positionToTest);
        }
    }
}

