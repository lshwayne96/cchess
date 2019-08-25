package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
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

/**
 * Represents an opening book.
 */
public class MoveBook {

    private static final String AI_MOVEBOOK_PATH = "/ai/movebook.txt";
    private static final Map<Long, List<Move>> MOVE_BOOK = readMoveBook();
    private static final Random rand = new Random();

    /**
     * Returns a random move in the book, if any, based on the given zobrist key.
     * @param zobristKey The zobrist key of the board.
     * @return A random move in the book, if any, based on the given zobrist key.
     */
    public static Optional<Move> getRandomMove(long zobristKey) {
        List<Move> moves = MOVE_BOOK.get(zobristKey);
        if (moves == null) {
            return Optional.empty();
        }
        return Optional.of(moves.get(rand.nextInt(moves.size())));
    }

    /**
     * Reads the text file containing moves into a map.
     */
    private static Map<Long, List<Move>> readMoveBook() {
        Map<Long, List<Move>> boardToMoves = new HashMap<>();
        Board board = Board.initialiseBoard();
        String str;

        BufferedReader br =
                new BufferedReader(new InputStreamReader(MoveBook.class.getResourceAsStream(AI_MOVEBOOK_PATH)));
        try {
            while ((str = br.readLine()) != null) {
                if (str.trim().isEmpty()) { // next opening
                    board = Board.initialiseBoard();
                    continue;
                }

                long zobristKey = board.getZobristKey();
                Optional<Move> move = Move.stringToMove(board, str);
                if (move.isPresent()) {
                    List<Move> currList = boardToMoves.get(zobristKey);
                    if (currList != null) {
                        if (!currList.contains(move.get())) {
                            currList.add(move.get());
                        }
                    } else {
                        List<Move> newList = new ArrayList<>();
                        newList.add(move.get());
                        boardToMoves.put(zobristKey, newList);
                    }

                    // store mirrored version of move
                    Board mirrorBoard = board.getMirrorBoard();
                    long mirrorZobristKey = mirrorBoard.getZobristKey();
                    Coordinate mirrorSrcPosition = Board.getMirrorPosition(move.get().getMovedPiece().getPosition());
                    Coordinate mirrorDestPosition = Board.getMirrorPosition(move.get().getDestPosition());
                    Move mirrorMove = mirrorBoard.getMove(mirrorSrcPosition, mirrorDestPosition).get();
                    List<Move> mirrorCurrList = boardToMoves.get(mirrorZobristKey);
                    if (mirrorCurrList != null) {
                        if (!mirrorCurrList.contains(mirrorMove)) {
                            mirrorCurrList.add(mirrorMove);
                        }
                    } else {
                        List<Move> mirrorNewList = new ArrayList<>();
                        mirrorNewList.add(mirrorMove);
                        boardToMoves.put(mirrorZobristKey, mirrorNewList);
                    }
                } else {
                    continue;
                }
                board.makeMove(move.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.unmodifiableMap(boardToMoves);
    }
}
