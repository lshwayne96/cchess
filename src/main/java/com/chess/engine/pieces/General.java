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

    public static final int VALUE = 5000;

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public General(Coordinate position, Alliance alliance) {
        super(PieceType.GENERAL, position, alliance);
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

        // flying general move
        Coordinate vector = new Coordinate(1, 0).scale(alliance.getDirection());
        Coordinate currPosition = position.add(vector);
        while (Board.isWithinBounds(currPosition)) {
            Point currPoint = board.getPoint(currPosition);
            Optional<Piece> piece = currPoint.getPiece();
            if (piece.isPresent()) {
                if (piece.get().getType() == PieceType.GENERAL) {
                    legalMoves.add(new Move(board, this, currPosition, piece.get()));
                }
                break;
            }
            currPosition = currPosition.add(vector);
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    @Override
    public General movePiece(Move move) {
        return new General(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public int getValue() {
        return VALUE;
    }

    private boolean isValidPosition(Coordinate positionToTest) {
        int row = positionToTest.getRow();
        int col = positionToTest.getCol();

        if (alliance.isRed()) {
            return (row >= 7 && row <= 9) && (col >= 3 && col <= 5);
        } else {
            return (row >= 0 && row <= 2) && (col >= 3 && col <= 5);
        }
    }
}
