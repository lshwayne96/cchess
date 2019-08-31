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

public class Soldier extends Piece {

    private static final Coordinate MOVE_VECTOR_BEFORE_RIVER = new Coordinate(1, 0);

    private static final List<Coordinate> MOVE_VECTORS_AFTER_RIVER =
            List.of(new Coordinate(1, 0), new Coordinate(0, -1),
                    new Coordinate(0, 1));

    public Soldier(Coordinate position, Alliance alliance) {
        super(PieceType.SOLDIER, position, alliance);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        if (!crossedRiver()) {
            Coordinate destPosition = position.add(MOVE_VECTOR_BEFORE_RIVER.scale(alliance.getDirection()));
            Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
            destPiece.ifPresentOrElse(p -> {
                if (!p.alliance.equals(this.alliance)) {
                    legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                }
            }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
        } else {
            for (Coordinate vector : MOVE_VECTORS_AFTER_RIVER) {
                Coordinate destPosition = position.add(vector.scale(alliance.getDirection()));
                if (BoardUtil.isWithinBounds(destPosition)) {
                    Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                    destPiece.ifPresentOrElse(p -> {
                        if (!p.alliance.equals(this.alliance)) {
                            legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                        }
                    }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
                }
            }
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Soldier movePiece(Move move) {
        return new Soldier(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public Soldier getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new Soldier(mirrorPosition, alliance);
    }
}
