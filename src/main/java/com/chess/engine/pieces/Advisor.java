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


public class Advisor extends Piece {

    private static final double VALUE = 2;

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
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
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
    public Advisor movePiece(Move move) {
        return new Advisor(move.getDestPosition(), move.getMovedPiece().getAlliance());
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
