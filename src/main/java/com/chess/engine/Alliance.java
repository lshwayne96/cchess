package com.chess.engine;

/**
 * Represents a player's side in the game.
 */
public enum Alliance {
    RED {
        @Override
        public boolean isRed() {
            return true;
        }

        @Override
        public int getDirection() {
            return -1;
        }

        @Override
        public Alliance opposite() {
            return BLACK;
        }
    },
    BLACK {
        @Override
        public boolean isRed() {
            return false;
        }

        @Override
        public int getDirection() {
            return 1;
        }

        @Override
        public Alliance opposite() {
            return RED;
        }
    };

    public abstract boolean isRed();

    public abstract int getDirection();

    public abstract Alliance opposite();
}
