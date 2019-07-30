package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Advisor;
import com.chess.engine.pieces.Cannon;
import com.chess.engine.pieces.Chariot;
import com.chess.engine.pieces.Elephant;
import com.chess.engine.pieces.General;
import com.chess.engine.pieces.Horse;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Soldier;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.RedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Board {

    private final List<Point> points;
    private final Collection<Piece> redPieces;
    private final Collection<Piece> blackPieces;

    private final RedPlayer redPlayer;
    private final BlackPlayer blackPlayer;
    private final Player currPlayer;

    public static final int NUM_ROWS = 10;
    public static final int NUM_COLS = 9;
    public static final int RIVER_ROW_RED = 5;
    public static final int RIVER_ROW_BLACK = 4;

    private Board(Builder builder) {
        points = createBoard(builder);
        redPieces = getActivePieces(points, Alliance.RED);
        blackPieces = getActivePieces(points, Alliance.BLACK);

        Collection<Move> redStandardLegalMoves = calculateLegalMoves(redPieces);
        Collection<Move> blackStandardLegalMoves = calculateLegalMoves(blackPieces);

        redPlayer = new RedPlayer(this, redStandardLegalMoves, blackStandardLegalMoves);
        blackPlayer = new BlackPlayer(this, redStandardLegalMoves, blackStandardLegalMoves);
        currPlayer = builder.currTurn.choosePlayer(redPlayer, blackPlayer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                String pointText = points.get(positionToIndex(row, col)).toString();
                sb.append(String.format("%3s", pointText));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Point getPoint(Coordinate position) {
        return points.get(positionToIndex(position.getRow(), position.getCol()));
    }

    public Collection<Piece> getRedPieces() {
        return redPieces;
    }

    public Collection<Piece> getBlackPieces() {
        return blackPieces;
    }

    public Player getRedPlayer() {
        return redPlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Player getCurrPlayer() {
        return currPlayer;
    }

    public boolean hasEnded() {
        return currPlayer.isInCheckmate();
    }

    private Collection<Move> calculateLegalMoves(Collection<Piece> pieces) {
        List<Move> legalMoves = new ArrayList<>();

        for (Piece piece : pieces) {
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }

        return Collections.unmodifiableCollection(legalMoves);
    }

    public static Board initialiseBoard() {
        Builder builder = new Builder();

        // BLACK layout
        builder.putPiece(new Chariot(new Coordinate(0, 0), Alliance.BLACK))
                .putPiece(new Horse(new Coordinate(0, 1), Alliance.BLACK))
                .putPiece(new Elephant(new Coordinate(0, 2), Alliance.BLACK))
                .putPiece(new Advisor(new Coordinate(0, 3), Alliance.BLACK))
                .putPiece(new General(new Coordinate(0, 4), Alliance.BLACK))
                .putPiece(new Advisor(new Coordinate(0, 5), Alliance.BLACK))
                .putPiece(new Elephant(new Coordinate(0, 6), Alliance.BLACK))
                .putPiece(new Horse(new Coordinate(0, 7), Alliance.BLACK))
                .putPiece(new Chariot(new Coordinate(0, 8), Alliance.BLACK))
                .putPiece(new Cannon(new Coordinate(2, 1), Alliance.BLACK))
                .putPiece(new Cannon(new Coordinate(2, 7), Alliance.BLACK))
                .putPiece(new Soldier(new Coordinate(3, 0), Alliance.BLACK))
                .putPiece(new Soldier(new Coordinate(3, 2), Alliance.BLACK))
                .putPiece(new Soldier(new Coordinate(3, 4), Alliance.BLACK))
                .putPiece(new Soldier(new Coordinate(3, 6), Alliance.BLACK))
                .putPiece(new Soldier(new Coordinate(3, 8), Alliance.BLACK));

        // RED layout
        builder.putPiece(new Chariot(new Coordinate(9, 0), Alliance.RED))
                .putPiece(new Horse(new Coordinate(9, 1), Alliance.RED))
                .putPiece(new Elephant(new Coordinate(9, 2), Alliance.RED))
                .putPiece(new Advisor(new Coordinate(9, 3), Alliance.RED))
                .putPiece(new General(new Coordinate(9, 4), Alliance.RED))
                .putPiece(new Advisor(new Coordinate(9, 5), Alliance.RED))
                .putPiece(new Elephant(new Coordinate(9, 6), Alliance.RED))
                .putPiece(new Horse(new Coordinate(9, 7), Alliance.RED))
                .putPiece(new Chariot(new Coordinate(9, 8), Alliance.RED))
                .putPiece(new Cannon(new Coordinate(7, 1), Alliance.RED))
                .putPiece(new Cannon(new Coordinate(7, 7), Alliance.RED))
                .putPiece(new Soldier(new Coordinate(6, 0), Alliance.RED))
                .putPiece(new Soldier(new Coordinate(6, 2), Alliance.RED))
                .putPiece(new Soldier(new Coordinate(6, 4), Alliance.RED))
                .putPiece(new Soldier(new Coordinate(6, 6), Alliance.RED))
                .putPiece(new Soldier(new Coordinate(6, 8), Alliance.RED));

        // RED moves first
        builder.setCurrTurn(Alliance.RED);

        return builder.build();
    }

    public static boolean isWithinBounds(Coordinate position) {
        int row = position.getRow();
        int col = position.getCol();

        return (row >= 0 && row < NUM_ROWS) && (col >= 0 && col < NUM_COLS);
    }

    private static int positionToIndex(int row, int col) {
        return row * NUM_COLS + col;
    }

    private static List<Point> createBoard(Builder builder) {
        List<Point> points = new ArrayList<>();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Coordinate position = new Coordinate(row, col);
                points.add(Point.getInstance(position, builder.boardConfig.get(position)));
            }
        }

        return Collections.unmodifiableList(points);
    }

    private static Collection<Piece> getActivePieces(List<Point> board, Alliance alliance) {
        List<Piece> activePieces = new ArrayList<>();

        for (Point point : board) {
            Optional<Piece> piece = point.getPiece();
            piece.ifPresent(p -> {
                if (p.getAlliance() == alliance) {
                    activePieces.add(p);
                }
            });
        }

        return Collections.unmodifiableCollection(activePieces);
    }


    public static class Builder {

        Map<Coordinate, Piece> boardConfig;
        Alliance currTurn;

        Builder() {
            boardConfig = new HashMap<>();
        }

        Builder putPiece(Piece piece) {
            boardConfig.put(piece.getPosition(), piece);
            return this;
        }

        Builder setCurrTurn(Alliance currTurn) {
            this.currTurn = currTurn;
            return this;
        }

        Board build() {
            return new Board(this);
        }
    }
}
