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

public class Chariot extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public Chariot(Coordinate position, Alliance alliance) {
        super(PieceType.CHARIOT, position, alliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate coordinate : MOVE_VECTORS) {
            Coordinate destPosition = position.add(coordinate);

            while (Board.isWithinBounds(destPosition)) {
                Point destPoint = board.getPoint(destPosition);
                Optional<Piece> destPiece = destPoint.getPiece();

                if (!destPiece.isPresent()) {
                    legalMoves.add(new Move(board, this, destPosition));
                } else {
                    if (this.alliance != destPiece.get().alliance) {
                        legalMoves.add(new Move(board, this, destPosition, destPiece.get()));
                    }
                    break; // cannot advance further
                }

                destPosition = destPosition.add(coordinate);
            }
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    @Override
    public Chariot movePiece(Move move) {
        return new Chariot(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }
}
