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
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a Chinese Chess board.
 */
public class Board {

    public static final int NUM_ROWS = 10;
    public static final int NUM_COLS = 9;
    public static final int RIVER_ROW_RED = 5;
    public static final int RIVER_ROW_BLACK = 4;
    private static final int MAX_PIECES_MIDGAME = 30;
    private static final int MAX_ATTACKING_UNITS_ENDGAME = 8;
    private static final int MIN_PIECES_NULLMOVE = 5;

    private final List<Point> points;
    private RedPlayer redPlayer;
    private BlackPlayer blackPlayer;
    private Player currPlayer;

    private Board(Builder builder) {
        points = createBoard(builder);
        updatePlayers();
        currPlayer = builder.currTurn.choosePlayer(redPlayer, blackPlayer);
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

    private void updatePlayers() {
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

        redPlayer = new RedPlayer(this, redPieces, redLegalMoves, blackLegalMoves);
        blackPlayer = new BlackPlayer(this, blackPieces, blackLegalMoves, redLegalMoves);
    }

    private void updatePlayers(PlayerInfo playerInfo) {
        this.redPlayer = playerInfo.redPlayer;
        this.blackPlayer = playerInfo.blackPlayer;
    }

    /**
     * Returns the initial state of the board.
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

    public void makeMove(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(positionToIndex(srcPosition.getRow(), srcPosition.getCol()));
        srcPoint.removePiece();
        Point destPoint = points.get(positionToIndex(destPosition.getRow(), destPosition.getCol()));
        destPoint.setPiece(movedPiece.movePiece(move));

        updatePlayers();
        currPlayer = currPlayer.getOpponent();
    }

    public void unmakeMove(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Optional<Piece> capturedPiece = move.getCapturedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(positionToIndex(srcPosition.getRow(), srcPosition.getCol()));
        srcPoint.setPiece(movedPiece);
        Point destPoint = points.get(positionToIndex(destPosition.getRow(), destPosition.getCol()));
        destPoint.removePiece();
        capturedPiece.ifPresent(destPoint::setPiece);

        updatePlayers();
        currPlayer = currPlayer.getOpponent();
    }

    public void unmakeMove(Move move, PlayerInfo playerInfo) {
        Piece movedPiece = move.getMovedPiece();
        Optional<Piece> capturedPiece = move.getCapturedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(positionToIndex(srcPosition.getRow(), srcPosition.getCol()));
        srcPoint.setPiece(movedPiece);
        Point destPoint = points.get(positionToIndex(destPosition.getRow(), destPosition.getCol()));
        destPoint.removePiece();
        capturedPiece.ifPresent(destPoint::setPiece);

        updatePlayers(playerInfo);
        currPlayer = currPlayer.getOpponent();
    }

    public PlayerInfo getPlayerInfo() {
        return new PlayerInfo(redPlayer, blackPlayer);
    }

    public boolean isLegalState() {
        return !currPlayer.getOpponent().isInCheck();
    }

    /**
     * Returns a move, if any, corresponding to the given source and destination positions on this board.
     * @param srcPosition The source position.
     * @param destPosition The destination position.
     * @return A move, if any, corresponding to the given source and destination positions on this board.
     */
    public Optional<Move> getMove(Coordinate srcPosition, Coordinate destPosition) {
        for (Move move : currPlayer.getLegalMoves()) {
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
                default:
                    break;
            }
        }
        if (attackingUnits > MAX_ATTACKING_UNITS_ENDGAME) {
            return BoardStatus.MIDDLE;
        } else {
            return BoardStatus.END;
        }
    }

    /**
     * Checks if the game on this board is already over.
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return currPlayer.isInCheckmate();
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
     * Checks if this board is quiet.
     * @return true if this board is quiet (current player has no capture moves), false otherwise.
     */
    public boolean isQuiet() {
        for (Move move : currPlayer.getLegalMoves()) {
            if (move.getCapturedPiece().isPresent()) {
                return false;
            }
        }
        return true;
    }

    public boolean allowNullMove() {
        return !currPlayer.isInCheck() && currPlayer.getActivePieces().size() >= MIN_PIECES_NULLMOVE;
    }

    public void passMove() {
        currPlayer = currPlayer.getOpponent();
    }

    /**
     * Returns the mirrored version (about the middle column) of this board.
     * @return The mirrored version of this board.
     */
    public Board getMirrorBoard() {
        Builder builder = new Builder();

        for (Point point : points) {
            Optional<Piece> piece = point.getPiece();
            piece.ifPresent(p -> builder.putPiece(p.getMirrorPiece()));
        }
        builder.setCurrTurn(currPlayer.getAlliance());

        return builder.build();
    }

    public Board getCopy() {
        Builder builder = new Builder();

        for (Point point : points) {
            Optional<Piece> piece = point.getPiece();
            piece.ifPresent(builder::putPiece);
        }
        builder.setCurrTurn(currPlayer.getAlliance());

        return builder.build();
    }

    /**
     * Returns the point on this board with the given position.
     * @param position The position of the point.
     * @return The point on this board with the given position.
     */
    public Point getPoint(Coordinate position) {
        return points.get(positionToIndex(position.getRow(), position.getCol()));
    }

    /**
     * Checks if the given position is within bounds of the board.
     * @param position The position to check.
     * @return true if the given position is within bounds, false otherwise.
     */
    public static boolean isWithinBounds(Coordinate position) {
        int row = position.getRow();
        int col = position.getCol();

        return (row >= 0 && row < NUM_ROWS) && (col >= 0 && col < NUM_COLS);
    }

    /**
     * Returns the index of a position based on its row and column.
     */
    private static int positionToIndex(int row, int col) {
        return row * NUM_COLS + col;
    }

    public Collection<Piece> getAllPieces() {
        Collection<Piece> allPieces = new ArrayList<>();

        allPieces.addAll(redPlayer.getActivePieces());
        allPieces.addAll(blackPlayer.getActivePieces());

        return allPieces;
    }

    public Collection<Move> getAllLegalMoves() {
        Collection<Move> allMoves = new ArrayList<>();

        allMoves.addAll(redPlayer.getLegalMoves());
        allMoves.addAll(blackPlayer.getLegalMoves());

        return allMoves;
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

    public BoardState getState() {
        return new BoardState(toString(), currPlayer.getAlliance());
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

    public static class PlayerInfo {
        RedPlayer redPlayer;
        BlackPlayer blackPlayer;

        private PlayerInfo(RedPlayer redPlayer, BlackPlayer blackPlayer) {
            this.redPlayer = redPlayer;
            this.blackPlayer = blackPlayer;
        }
    }

    public static class BoardState {
        private String string;
        private Alliance currTurn;

        private BoardState(String string, Alliance currTurn) {
            this.string = string;
            this.currTurn = currTurn;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BoardState)) {
                return false;
            }

            BoardState other = (BoardState) obj;
            return this.string.equals(other.string) && this.currTurn.equals(other.currTurn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(string, currTurn);
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
