package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class MoveBook {

    private static final Board INITIAL_BOARD = Board.initialiseBoard();
    private static final String AI_MOVEBOOK_PATH = "/ai/movebook.txt";
    private static final MoveBook INSTANCE = new MoveBook();

    private final Map<Board, List<Move>> movebook;
    private final Random rand;

    private MoveBook() {
        movebook = readMoveBook();
        rand = new Random();
    }

    public static MoveBook getInstance() {
        return INSTANCE;
    }

    public Optional<Move> getRandomMove(Board board) {
        List<Move> moves = movebook.get(board);
        if (moves == null) {
            return Optional.empty();
        }
        return Optional.of(moves.get(rand.nextInt(moves.size())));
    }

    private static Map<Board, List<Move>> readMoveBook() {
        Map<Board, List<Move>> boardToMoves = new HashMap<>();
        Board board = INITIAL_BOARD;
        String str;

        BufferedReader br = new BufferedReader(new InputStreamReader(MoveBook.class.getResourceAsStream(AI_MOVEBOOK_PATH)));
        try {
            while ((str = br.readLine()) != null) {
                if (str.trim().isEmpty()) { // next opening
                    board = INITIAL_BOARD;
                    continue;
                }

                Optional<Move> move = Move.stringToMove(board, str);
                if (move.isPresent()) {
                    List<Move> currList = boardToMoves.get(board);
                    if (currList != null) {
                        if (!currList.contains(move.get())) {
                            currList.add(move.get());
                        }
                    } else {
                        List<Move> newList = new ArrayList<>();
                        newList.add(move.get());
                        boardToMoves.put(board, newList);
                    }

                    Board mirroredBoard = board.getMirrorBoard();
                    Move mirroredMove = move.get().getMirroredMove();
                    List<Move> currListMirrored = boardToMoves.get(mirroredBoard);
                    if (currListMirrored != null) {
                        if (!currListMirrored.contains(mirroredMove)) {
                            currListMirrored.add(mirroredMove);
                        }
                    } else {
                        List<Move> newListMirrored = new ArrayList<>();
                        newListMirrored.add(mirroredMove);
                        boardToMoves.put(mirroredBoard, newListMirrored);
                    }
                } else {
                    continue;
                }
                board = move.get().execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.unmodifiableMap(boardToMoves);
    }
}
