package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtil;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int INDENT_SPACES = 7;

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
        List<Move> moveHistory = new ArrayList<>();
        String str;
        int currIndentLevel = 0;

        BufferedReader br =
                new BufferedReader(new InputStreamReader(MoveBook.class.getResourceAsStream(AI_MOVEBOOK_PATH)));
        try {
            while ((str = br.readLine()) != null) {
                String trimmedStr = str.trim();
                int spaces = str.indexOf(trimmedStr);
                if (spaces % INDENT_SPACES != 0) {
                    throw new IOException("Invalid indent");
                }
                int indentLevel = str.indexOf(trimmedStr) / INDENT_SPACES;
                for (int i = 0; i < currIndentLevel - indentLevel; i++) {
                    Move move = moveHistory.remove(moveHistory.size() - 1);
                    board.unmakeMove(move);
                }
                String[] moveSequence = trimmedStr.split(" ");
                currIndentLevel = indentLevel + moveSequence.length;

                for (String moveStr : moveSequence) {
                    long zobristKey = board.getZobristKey();
                    Optional<Move> move = Move.stringToMove(board, moveStr);
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
                        Coordinate mirrorSrcPosition = BoardUtil.getMirrorPosition(move.get().getMovedPiece().getPosition());
                        Coordinate mirrorDestPosition = BoardUtil.getMirrorPosition(move.get().getDestPosition());
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
                        throw new IOException("Invalid move");
                    }
                    board.makeMove(move.get());
                    moveHistory.add(move.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.unmodifiableMap(boardToMoves);
    }
}
