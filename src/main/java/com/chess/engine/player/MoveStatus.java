package com.chess.engine.player;

/**
 * Represents the status of a move.
 */
public enum MoveStatus {
    ALLOWED { // legal AND non-suicidal
        @Override
        public boolean isAllowed() {
            return true;
        }
    },
    SUICIDAL { // causes the general to be captured immediately
        @Override
        public boolean isAllowed() {
            return false;
        }
    };

    public abstract boolean isAllowed();
}
