package main.java.com.chess.engine.player;

import main.java.com.chess.engine.board.Board;
import main.java.com.chess.engine.board.Move;

public class MoveTransition {

    private final Board nextBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    public MoveTransition(Board nextBoard, Move move, MoveStatus moveStatus) {
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
