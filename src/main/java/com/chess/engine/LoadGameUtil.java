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

public class LoadGameUtil {

    private List<Board> boardHistory;
    private List<Move> moves;
    private boolean isValid;

    public LoadGameUtil(File file) {
        boardHistory = new ArrayList<>();
        moves = new ArrayList<>();
        isValid = true;

        Board board = Board.initialiseBoard();
        boardHistory.add(board);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            while (isValid && (str = br.readLine()) != null) {
                Optional<Move> move = Move.stringToMove(board, str);
                if (move.isPresent()) {
                    board = move.get().execute();
                    boardHistory.add(board);
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

    public List<Board> getBoardHistory() {
        return boardHistory;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public boolean isValidFile() {
        return isValid;
    }
}
