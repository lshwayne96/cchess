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

public class General extends Piece {

    private static final List<Coordinate> MOVE_VECTORS =
            List.of(new Coordinate(-1, 0), new Coordinate(0, -1),
                    new Coordinate(1, 0), new Coordinate(0, 1));
    private static final Coordinate FORWARD_VECTOR = MOVE_VECTORS.get(2);
    private static final Coordinate STARTING_POSITION_RED = new Coordinate(9, 4);
    private static final Coordinate STARTING_POSITION_BLACK = new Coordinate(0, 4);

    public General(Coordinate position, Alliance alliance) {
        super(PieceType.GENERAL, position, alliance);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            if (isValidPosition(destPosition)) {
                Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                destPiece.ifPresentOrElse(p -> {
                    if (!p.alliance.equals(this.alliance)) {
                        legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                    }
                }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
            }
        }

        // flying general move (only used for enforcing check)
        Coordinate vector = FORWARD_VECTOR.scale(alliance.getDirection());
        Coordinate currPosition = position.add(vector);
        while (BoardUtil.isWithinBounds(currPosition)) {
            Optional<Piece> piece = board.getPoint(currPosition).getPiece();
            if (piece.isPresent()) {
                if (piece.get().getPieceType().equals(PieceType.GENERAL)) {
                    legalMoves.add(new Move(board.getZobristKey(), this, currPosition, piece.get()));
                }
                break;
            }
            currPosition = currPosition.add(vector);
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Collection<Move> getLegalMoves(Board board, Collection<Attack> attacks, Collection<Defense> defenses) {
        List<Move> legalMoves = new ArrayList<>();
        List<Piece> attackedPieces = new ArrayList<>();
        List<Piece> defendedPieces = new ArrayList<>();

        for (Coordinate vector : MOVE_VECTORS) {
            Coordinate destPosition = position.add(vector);
            if (isValidPosition(destPosition)) {
                Optional<Piece> destPiece = board.getPoint(destPosition).getPiece();
                destPiece.ifPresentOrElse(p -> {
                    if (!p.alliance.equals(this.alliance)) {
                        legalMoves.add(new Move(board.getZobristKey(), this, destPosition, p));
                        attackedPieces.add(p);
                    } else {
                        defendedPieces.add(p);
                    }
                }, () -> legalMoves.add(new Move(board.getZobristKey(), this, destPosition)));
            }
        }

        // flying general move (only used for enforcing check)
        Coordinate vector = FORWARD_VECTOR.scale(alliance.getDirection());
        Coordinate currPosition = position.add(vector);
        while (BoardUtil.isWithinBounds(currPosition)) {
            Optional<Piece> piece = board.getPoint(currPosition).getPiece();
            if (piece.isPresent()) {
                if (piece.get().getPieceType().equals(PieceType.GENERAL)) {
                    legalMoves.add(new Move(board.getZobristKey(), this, currPosition, piece.get()));
                    attackedPieces.add(piece.get());
                }
                break;
            }
            currPosition = currPosition.add(vector);
        }

        Attack attack = new Attack(this, attackedPieces);
        Defense defense = new Defense(this, defendedPieces);

        attacks.add(attack);
        defenses.add(defense);
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public General movePiece(Move move) {
        return new General(move.getDestPosition(), move.getMovedPiece().getAlliance());
    }

    @Override
    public General getMirrorPiece() {
        Coordinate mirrorPosition = new Coordinate(position.getRow(), Board.NUM_COLS - 1 - position.getCol());
        return new General(mirrorPosition, alliance);
    }

    /**
     * Checks if the given position is valid for this general.
     */
    private boolean isValidPosition(Coordinate positionToTest) {
        int row = positionToTest.getRow();
        int col = positionToTest.getCol();

        if (alliance.isRed()) {
            return (row >= 7 && row <= 9) && (col >= 3 && col <= 5);
        } else {
            return (row >= 0 && row <= 2) && (col >= 3 && col <= 5);
        }
    }

    public static Coordinate getStartingPosition(Alliance alliance) {
        return alliance.isRed() ? STARTING_POSITION_RED : STARTING_POSITION_BLACK;
    }
}
