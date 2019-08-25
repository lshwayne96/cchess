package com.chess.engine;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A helper class for loading a saved game.
 */
public class LoadGameUtil {

    private Board board;
    private List<Move> moves;
    private boolean isValid;

    public LoadGameUtil(File file) {
        moves = new ArrayList<>();
        isValid = true;

        board = Board.initialiseBoard();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;

            while (isValid && (str = br.readLine()) != null) {
                Optional<Move> move = Move.stringToMove(board, str);
                if (move.isPresent()) {
                    board.makeMove(move.get());
                    moves.add(move.get());
                } else {
                    isValid = false;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Board getBoard() {
        return board;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public boolean isValidFile() {
        return isValid;
    }
}
