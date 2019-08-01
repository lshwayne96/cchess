package com.chess.engine.player;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public class MoveTransition {

    private final Board nextBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    MoveTransition(Board nextBoard, Move move, MoveStatus moveStatus) {
        this.nextBoard = nextBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public Board getNextBoard() {
        return nextBoard;
    }

    public Move getMove() {
        return move;
    }

    public MoveStatus getMoveStatus() {
        return moveStatus;
    }
}
