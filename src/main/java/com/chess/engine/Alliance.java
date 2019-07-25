package main.java.com.chess.engine;

import main.java.com.chess.engine.player.BlackPlayer;
import main.java.com.chess.engine.player.Player;
import main.java.com.chess.engine.player.RedPlayer;

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
        public Player choosePlayer(RedPlayer redPlayer, BlackPlayer blackPlayer) {
            return redPlayer;
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
        public Player choosePlayer(RedPlayer redPlayer, BlackPlayer blackPlayer) {
            return blackPlayer;
        }
    };

    public abstract boolean isRed();

    public abstract int getDirection();

    public abstract Player choosePlayer(RedPlayer redPlayer, BlackPlayer blackPlayer);
}
