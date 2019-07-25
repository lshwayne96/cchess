package main.java.com.chess.engine.pieces;

import main.java.com.chess.engine.Alliance;
import main.java.com.chess.engine.board.Board;
import main.java.com.chess.engine.board.Coordinate;
import main.java.com.chess.engine.board.Move;
import main.java.com.chess.engine.board.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Cannon extends Piece {

    private static final double VALUE = 4.5;

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));

    public Cannon(Coordinate position, Alliance alliance) {
        super(PieceType.CANNON, position, alliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate coordinate : MOVE_VECTORS) {
            Coordinate destPosition = position.add(coordinate);
            boolean jumped = false;

            while (Board.isWithinBounds(destPosition)) {
                Point destPoint = board.getPoint(destPosition);
                Optional<Piece> destPiece = destPoint.getPiece();

                if (!jumped && !destPiece.isPresent()) { // before first piece
                    legalMoves.add(new Move(board, this, destPosition));
                } else if (!jumped) { // reached first piece
                    jumped = true;
                } else if (destPiece.isPresent()) { // after first piece
                    if (this.alliance != destPiece.get().alliance) {
                        legalMoves.add(new Move(board, this, destPosition, destPiece.get()));
                    }
                    break;
                }

                destPosition = destPosition.add(coordinate);
            }
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    @Override
    public Cannon movePiece(Move move) {
        return new Cannon(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public double getValue() {
        return VALUE;
    }
}
