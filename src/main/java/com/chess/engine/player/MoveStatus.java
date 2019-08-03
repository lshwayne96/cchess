package com.chess.engine.player;

/**
 * Represents the status of a move.
 */
public enum MoveStatus {
    DONE { // legal AND non-suicidal
        @Override
        public boolean isDone() {
            return true;
        }
    },
    SUICIDAL { // causes the general to be captured immediately
        @Override
        public boolean isDone() {
            return false;
        }
    };

    public abstract boolean isDone();
}
