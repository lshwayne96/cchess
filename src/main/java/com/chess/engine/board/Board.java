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
    public static final int MAX_PIECES_IN_MIDGAME = 14;
    public static final int MAX_PIECES_IN_ENDGAME = 8;

    public static int[][] POSITION_VALUES_SOLDIER = {
            {  0,    0,    0,    2,    4,    2,    0,    0,    0},
            { 40,   60,  100,  130,  140,  130,  100,   60,   40},
            { 40,   60,   90,  110,  110,  110,   90,   60,   40},
            { 40,   54,   60,   80,   84,   80,   60,   54,   40},
            { 20,   36,   44,   70,   80,   70,   44,   36,   20},
            {  6,    0,    8,    0,   14,    0,    8,    0,    6},
            { -4,    0,   -4,    0,   12,    0,   -4,    0,   -4},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };

    public static int[][] POSITION_VALUES_ADVISOR = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    6,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };

    public static int[][] POSITION_VALUES_ELEPHANT = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            { -4,    0,    0,    0,    6,    0,    0,    0,   -4},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0}
    };

    public static int[][] POSITION_VALUES_HORSE = {
            {  4,    4,    4,   16,    4,   16,    4,    4,    4},
            {  4,   16,   30,   18,   12,   18,   30,   16,    4},
            {  8,   20,   22,   30,   22,   30,   22,   20,    8},
            { 10,   40,   24,   38,   24,   38,   24,   40,    10},
            {  4,   24,   22,   30,   32,   30,   22,   24,    4},
            {  4,   20,   26,   28,   30,   28,   26,   20,    4},
            {  8,   12,   20,   14,   20,   14,   20,   12,    8},
            { 10,    8,   12,   14,    8,   14,   12,    8,   10},
            { -6,    4,    8,   10,  -20,   10,    8,    4,   -6},
            {  0,   -6,    4,    0,    4,    0,    4,   -6,    0}
    };

    public static int[][] POSITION_VALUES_CANNON = {
            {  8,    8,    0,  -10,  -12,  -10,    0,    8,    8},
            {  4,    4,    0,   -8,  -14,   -8,    0,    4,    4},
            {  2,    2,    0,  -10,   -8,  -10,    0,    2,    2},
            {  0,    6,    6,    4,    8,    4,    6,    0,    0},
            {  0,    0,    0,    0,    8,    0,    0,    0,    0},
            { -2,    0,    6,    0,    8,    0,    6,    0,   -2},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  2,    0,    8,    6,   10,    6,    8,    0,    2},
            {  0,    2,    4,    4,    4,    4,    4,    2,    0},
            {  0,    0,    2,    6,    6,    6,    2,    0,    0}
    };

    public static int[][] POSITION_VALUES_CHARIOT = {
            { 12,   16,   14,   26,   28,   26,   14,   16,   12},
            { 12,   24,   18,   32,   66,   32,   18,   24,   12},
            { 12,   16,   14,   28,   32,   28,   14,   16,   12},
            { 12,   26,   26,   32,   32,   32,   26,   26,   12},
            { 16,   22,   22,   28,   30,   28,   22,   22,   16},
            { 16,   24,   24,   28,   30,   28,   24,   24,   16},
            {  8,   18,    8,   24,   28,   24,    8,   18,    8},
            { -4,   16,    8,   24,   24,   24,    8,   16,   -4},
            { 10,   16,   12,   24,    0,   24,   12,   16,   10},
            {-12,   12,    8,   24,    0,   24,    8,   12,  -12}
    };

    public static int[][] POSITION_VALUES_GENERAL = {
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,    0,    0,    0,    0,    0,    0},
            {  0,    0,    0,  -18,  -18,  -18,    0,    0,    0},
            {  0,    0,    0,  -16,  -16,  -16,    0,    0,    0},
            {  0,    0,    0,    2,   10,    2,    0,    0,    0}
    };

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Board)) {
            return false;
        }
        Board other = (Board) obj;
        return this.toString().equals(other.toString())
                && this.currPlayer.getAlliance().equals(other.currPlayer.getAlliance());
    }

    @Override
    public int hashCode() {
        return 31 * toString().hashCode() + currPlayer.getAlliance().hashCode();
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

    public Collection<Piece> getAllPieces() {
        Collection<Piece> allPieces = new ArrayList<>();
        allPieces.addAll(redPieces);
        allPieces.addAll(blackPieces);

        return allPieces;
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

    public BoardStatus getStatus() {
        if (redPlayer.getActivePieces().size() <= MAX_PIECES_IN_ENDGAME
                && blackPlayer.getActivePieces().size() <= MAX_PIECES_IN_ENDGAME) {
            return BoardStatus.END;
        }
        if (redPlayer.getActivePieces().size() <= MAX_PIECES_IN_MIDGAME
                || blackPlayer.getActivePieces().size() <= MAX_PIECES_IN_MIDGAME) {
            return BoardStatus.MIDDLE;
        }
        return BoardStatus.OPENING;
    }

    public boolean isGameOver() {
        return currPlayer.isInCheckmate();
    }

    public boolean isGameDraw() {
        if (getRedPieces().size() > 5 || getBlackPieces().size() > 5) {
            return false;
        }
        for (Piece piece : getAllPieces()) {
            if (piece.getType().isAttacking()) {
                return false;
            }
        }
        return true;
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

    public enum BoardStatus {
        OPENING,
        MIDDLE,
        END,
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
