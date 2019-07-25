package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;

import java.util.Collection;

public class RedPlayer extends Player {

    public RedPlayer(Board board, Collection<Move> redStandardLegalMoves,
                     Collection<Move> blackStandardLegalMoves) {
        super(board, redStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return board.getRedPieces();
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
