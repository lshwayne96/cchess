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
import java.util.Optional;

public class Chariot extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public Chariot(Coordinate position, Alliance alliance) {
        super(PieceType.CHARIOT, position, alliance);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);

            while (BoardUtil.isWithinBounds(destPosition)) {
                Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                if (destPiece.isPresent()) {
                    if (!destPiece.get().alliance.equals(this.alliance)) {
                        legalMoves.add(new Move(board.getZobristKey(), this, destPosition, destPiece.get()));
                    }
                    break;
                } else {
                    legalMoves.add(new Move(board.getZobristKey(), this, destPosition));
                }
                destPosition = destPosition.add(vector);
            }
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Chariot movePiece(Move move) {
        return new Chariot(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public Chariot getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new Chariot(mirrorPosition, alliance);
    }
}
