package main.java.com.chess.engine.player;

import main.java.com.chess.engine.Alliance;
import main.java.com.chess.engine.board.Board;
import main.java.com.chess.engine.board.Move;
import main.java.com.chess.engine.pieces.Piece;

import java.util.Collection;

public class BlackPlayer extends Player {

    public BlackPlayer(Board board, Collection<Move> redStandardLegalMoves,
                     Collection<Move> blackStandardLegalMoves) {
        super(board, blackStandardLegalMoves, redStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return board.getRedPlayer();
    }
}
