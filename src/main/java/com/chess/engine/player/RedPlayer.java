package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Point;
import com.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RedPlayer extends Player {

    public RedPlayer(Board board) {
        super(board);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        List<Piece> pieces = new ArrayList<>();

        for (Point point : board.getPoints()) {
            point.getPiece().ifPresent(p -> {
                if (p.getAlliance().isRed()) {
                    pieces.add(p);
                }
            });
        }

        return pieces;
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.RED;
    }

    @Override
    public Player getOpponent() {
        return board.getBlackPlayer();
    }
}
