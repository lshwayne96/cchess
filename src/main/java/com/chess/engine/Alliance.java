package com.chess.engine;

import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.RedPlayer;

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
