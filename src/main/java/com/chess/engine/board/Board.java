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
import com.chess.engine.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Represents a Chinese Chess board.
 */
public class Board {

    public static final int NUM_ROWS = 10;
    public static final int NUM_COLS = 9;
    public static final int RIVER_ROW_RED = 5;
    public static final int RIVER_ROW_BLACK = 4;
    private static final int MAX_PIECES_MIDGAME = 29;
    private static final int MAX_ATTACKING_UNITS_ENDGAME = 8;
    private static final Zobrist ZOBRIST = new Zobrist();

    private final List<Point> points;
    private final List<PlayerInfo> playerInfoHistory;
    private PlayerInfo playerInfo;
    private Alliance currTurn;
    private long zobristKey;

    private Board(Builder builder) {
        points = createBoard(builder);
        playerInfoHistory = new ArrayList<>();
        playerInfo = generatePlayerInfo();
        currTurn = builder.currTurn;
        zobristKey = ZOBRIST.getKey(points, currTurn);
    }

    /**
     * Returns a list of points representing a board based on the given builder.
     */
    private static List<Point> createBoard(Builder builder) {
        List<Point> points = new ArrayList<>();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Coordinate position = new Coordinate(row, col);
                Point point = new Point(position);
                point.setPiece(builder.boardConfig.get(position));
                points.add(point);
            }
        }

        return Collections.unmodifiableList(points);
    }

    /**
     * Returns information related to both players on this board.
     */
    private PlayerInfo generatePlayerInfo() {
        Collection<Piece> redPieces = new ArrayList<>();
        Collection<Move> redLegalMoves = new ArrayList<>();
        Collection<Piece> blackPieces = new ArrayList<>();
        Collection<Move> blackLegalMoves = new ArrayList<>();

        for (Point point : points) {
            point.getPiece().ifPresent(p -> {
                if (p.getAlliance().isRed()) {
                    redPieces.add(p);
                    redLegalMoves.addAll(p.getLegalMoves(this));
                } else {
                    blackPieces.add(p);
                    blackLegalMoves.addAll(p.getLegalMoves(this));
                }
            });
        }

        return new PlayerInfo(new Player(Alliance.RED, redPieces, redLegalMoves, blackLegalMoves),
                new Player(Alliance.BLACK, blackPieces, blackLegalMoves, redLegalMoves));
    }

    /**
     * Returns the original state of a board.
     */
    public static Board initialiseBoard() {
        Builder builder = new Builder();

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

        builder.setCurrTurn(Alliance.RED);

        return builder.build();
    }

    /**
     * Makes the given move on this board. Player information and zobrist key are updated.
     * @param move The move to be made.
     */
    public void makeMove(Move move) {
        assert getCurrPlayer().getLegalMoves().contains(move);

        Piece movedPiece = move.getMovedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(positionToIndex(srcPosition));
        srcPoint.removePiece();
        Point destPoint = points.get(positionToIndex(destPosition));
        destPoint.setPiece(movedPiece.movePiece(move));

        playerInfoHistory.add(playerInfo);
        playerInfo = generatePlayerInfo();
        changeTurn();
        zobristKey = ZOBRIST.updateKey(zobristKey, move);
    }

    /**
     * Undoes the given move on this board. Player information and zobrist key are updated.
     * @param move The move to be undone.
     */
    public void unmakeMove(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Optional<Piece> capturedPiece = move.getCapturedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(positionToIndex(srcPosition));
        srcPoint.setPiece(movedPiece);
        Point destPoint = points.get(positionToIndex(destPosition));
        destPoint.removePiece();
        capturedPiece.ifPresent(destPoint::setPiece);

        playerInfo = playerInfoHistory.isEmpty() ? generatePlayerInfo()
                : playerInfoHistory.remove(playerInfoHistory.size() - 1);
        changeTurn();
        zobristKey = ZOBRIST.updateKey(zobristKey, move);
    }

    /**
     * Changes the current turn on the board. Zobrist key is updated.
     */
    public void changeTurn() {
        currTurn = currTurn.opposite();
        zobristKey ^= ZOBRIST.side;
    }

    /**
     * Checks if the current player's opponent is in check. Such a state is not allowed.
     * @return true if the current player's opponent is NOT in check, false otherwise.
     */
    public boolean isStateAllowed() {
        return !getOppPlayer().isInCheck();
    }

    /**
     * Returns a move, if any, corresponding to the given source and destination positions on this board.
     * @param srcPosition The source position.
     * @param destPosition The destination position.
     * @return A move, if any, corresponding to the given source and destination positions on this board.
     */
    public Optional<Move> getMove(Coordinate srcPosition, Coordinate destPosition) {
        for (Move move : getCurrPlayer().getLegalMoves()) {
            if (move.getMovedPiece().getPosition().equals(srcPosition)
                    && move.getDestPosition().equals(destPosition)) {
                return Optional.of(move);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the current status of this board.
     * @return The current status of this board.
     */
    public BoardStatus getStatus() {
        Collection<Piece> allPieces = getAllPieces();

        if (allPieces.size() > MAX_PIECES_MIDGAME) {
            return BoardStatus.OPENING;
        }

        int attackingUnits = 0;
        for (Piece piece : allPieces) {
            switch (piece.getPieceType()) {
                case CHARIOT:
                    attackingUnits += 2;
                    break;
                case CANNON:
                case HORSE:
                    attackingUnits++;
                    break;
            }
            if (attackingUnits > MAX_ATTACKING_UNITS_ENDGAME) {
                return BoardStatus.MIDDLE;
            }
        }
        return BoardStatus.END;
    }

    /**
     * Checks if the current player has been checkmated.
     * @return true if the current player has been checkmated, false otherwise.
     */
    public boolean isCurrPlayerCheckmated() {
        boolean isCheckmated = true;

        for (Move move : getCurrPlayer().getLegalMoves()) {
            makeMove(move);
            if (isStateAllowed()) {
                isCheckmated = false;
            }
            unmakeMove(move);
            if (!isCheckmated) break;
        }

        return isCheckmated;
    }

    /**
     * Checks if the game on this board is a draw.
     * @return true if the game is a draw, false otherwise.
     */
    public boolean isGameDraw() {
        for (Piece piece : getAllPieces()) {
            if (piece.getPieceType().isAttacking()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the current player has no capture moves.
     * @return true if the current player has no capture moves, false otherwise.
     */
    public boolean isQuiet() {
        for (Move move : getCurrPlayer().getLegalMoves()) {
            if (move.isCapture()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a copy of this board.
     * @return A copy of this board.
     */
    public Board getCopy() {
        Builder builder = new Builder();

        for (Point point : points) {
            Optional<Piece> piece = point.getPiece();
            piece.ifPresent(builder::putPiece);
        }
        builder.setCurrTurn(currTurn);

        return builder.build();
    }

    /**
     * Returns a mirrored copy (about the middle column) of this board.
     * @return A mirrored copy of this board.
     */
    public Board getMirrorBoard() {
        Builder builder = new Builder();

        for (Point point : points) {
            Optional<Piece> piece = point.getPiece();
            piece.ifPresent(p -> builder.putPiece(p.getMirrorPiece()));
        }
        builder.setCurrTurn(currTurn);

        return builder.build();
    }

    /**
     * Checks if the given position is within bounds.
     * @param position The position to check.
     * @return true if the given position is within bounds, false otherwise.
     */
    public static boolean isWithinBounds(Coordinate position) {
        int row = position.getRow();
        int col = position.getCol();

        return (row >= 0 && row < NUM_ROWS) && (col >= 0 && col < NUM_COLS);
    }

    /**
     * Returns the mirrored version of the given position.
     * @param position The position to mirror.
     * @return The mirrored version of the given position.
     */
    public static Coordinate getMirroredPosition(Coordinate position) {
        return new Coordinate(position.getRow(), NUM_COLS - 1 - position.getCol());
    }

    /**
     * Returns the point on this board with the given position.
     * @param position The position of the point.
     * @return The point on this board with the given position.
     */
    public Point getPoint(Coordinate position) {
        return points.get(positionToIndex(position));
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public Collection<Piece> getAllPieces() {
        Collection<Piece> allPieces = new ArrayList<>();

        allPieces.addAll(getRedPlayer().getActivePieces());
        allPieces.addAll(getBlackPlayer().getActivePieces());

        return allPieces;
    }

    public Collection<Move> getAllLegalMoves() {
        Collection<Move> allMoves = new ArrayList<>();

        allMoves.addAll(getRedPlayer().getLegalMoves());
        allMoves.addAll(getBlackPlayer().getLegalMoves());

        return allMoves;
    }

    public Player getRedPlayer() {
        return playerInfo.redPlayer;
    }

    public Player getBlackPlayer() {
        return playerInfo.blackPlayer;
    }

    public Player getCurrPlayer() {
        return currTurn.isRed() ? getRedPlayer() : getBlackPlayer();
    }

    public Player getOppPlayer() {
        return currTurn.isRed() ? getBlackPlayer() : getRedPlayer();
    }

    /**
     * Returns the index of a position based on its row and column.
     */
    private static int positionToIndex(int row, int col) {
        return row * NUM_COLS + col;
    }

    /**
     * Returns the index of a given position.
     */
    private static int positionToIndex(Coordinate position) {
        return positionToIndex(position.getRow(), position.getCol());
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

    /**
     * Represents both players on this board.
     */
    private static class PlayerInfo {
        private Player redPlayer;
        private Player blackPlayer;

        private PlayerInfo(Player redPlayer, Player blackPlayer) {
            this.redPlayer = redPlayer;
            this.blackPlayer = blackPlayer;
        }
    }

    /**
     * Helper class for calculating and updating Zobrist keys.
     */
    private static class Zobrist {

        private final long[][][] pieces;
        private final long side;

        private Zobrist() {
            Random rand = new Random();
            pieces = new long[7][2][90];
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 90; k++) {
                        pieces[i][j][k] = rand.nextLong();
                    }
                }
            }
            side = rand.nextLong();
        }

        /**
         * Returns the Zobrist hash of the given piece.
         */
        private long getPieceHash(Piece piece) {
            int pieceIndex = piece.getPieceType().ordinal();
            int sideIndex = piece.getAlliance().isRed() ? 0 : 1;
            int posIndex = positionToIndex(piece.getPosition());

            return pieces[pieceIndex][sideIndex][posIndex];
        }

        /**
         * Returns the Zobrist key given a list of points and current turn.
         */
        private long getKey(List<Point> points, Alliance currTurn) {
            long key = 0;

            for (Point point : points) {
                if (!point.isEmpty()) {
                    key ^= getPieceHash(point.getPiece().get());
                }
            }
            if (!currTurn.isRed()) {
                key ^= side;
            }

            return key;
        }

        /**
         * Returns the new Zobrist key given the old key and the move made.
         */
        private long updateKey(long key, Move move) {
            Piece movedPiece = move.getMovedPiece();
            Piece destPiece = movedPiece.movePiece(move);
            Optional<Piece> capturedPiece = move.getCapturedPiece();

            long movedPieceHash = getPieceHash(movedPiece);
            long destPieceHash = getPieceHash(destPiece);
            key ^= movedPieceHash ^ destPieceHash;
            if (capturedPiece.isPresent()) {
                key ^= getPieceHash(capturedPiece.get());
            }

            return key;
        }
    }

    /**
     * Represents the current status of a board.
     */
    public enum BoardStatus {
        OPENING,
        MIDDLE,
        END,
    }

    /**
     * A helper class for building a board.
     */
    static class Builder {

        private Map<Coordinate, Piece> boardConfig;
        private Alliance currTurn;

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
