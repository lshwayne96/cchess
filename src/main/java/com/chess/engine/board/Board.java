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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.chess.engine.pieces.Piece.*;

/**
 * Represents a Chinese Chess board.
 */
public class Board {

    public static final int NUM_ROWS = 10;
    public static final int NUM_COLS = 9;
    public static final int RIVER_ROW_RED = 5;
    public static final int RIVER_ROW_BLACK = 4;
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
     * Generates information related to both players on this board.
     */
    private PlayerInfo generatePlayerInfo() {
        Collection<Piece> redPieces = new ArrayList<>();
        Collection<Move> redLegalMoves = new ArrayList<>();
        int redMobilityValue = 0;
        Collection<Attack> redAttacks = new ArrayList<>();
        Collection<Defense> redDefenses = new ArrayList<>();

        Collection<Piece> blackPieces = new ArrayList<>();
        Collection<Move> blackLegalMoves = new ArrayList<>();
        int blackMobilityValue = 0;
        Collection<Attack> blackAttacks = new ArrayList<>();
        Collection<Defense> blackDefenses = new ArrayList<>();

        for (Point point : points) {
            if (point.isEmpty()) continue;
            Piece piece = point.getPiece().get();

            if (piece.getAlliance().isRed()) {
                redPieces.add(piece);
                Collection<Move> moves = piece.getLegalMoves(this, redAttacks, redDefenses);
                redLegalMoves.addAll(moves);
                redMobilityValue += piece.getPieceType().getMobilityValue() * moves.size();
            } else {
                blackPieces.add(piece);
                Collection<Move> moves = piece.getLegalMoves(this, blackAttacks, blackDefenses);
                blackLegalMoves.addAll(moves);
                blackMobilityValue += piece.getPieceType().getMobilityValue() * moves.size();
            }
        }

        Player redPlayer = new Player(Alliance.RED, redPieces, redLegalMoves, blackLegalMoves,
                redMobilityValue, redAttacks, redDefenses);
        Player blackPlayer = new Player(Alliance.BLACK, blackPieces, blackLegalMoves, redLegalMoves,
                blackMobilityValue, blackAttacks, blackDefenses);
        return new PlayerInfo(redPlayer, blackPlayer);
    }

    /**
     * Updates information related to both players on this board from existing information.
     */
    private PlayerInfo updatePlayerInfo(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Piece destPiece = movedPiece.movePiece(move);
        Piece capturedPiece = move.isCapture() ? move.getCapturedPiece().get() : null;

        Collection<Piece> redPieces = new ArrayList<>();
        Collection<Move> redLegalMoves = new ArrayList<>();
        int redMobilityValue = 0;
        Collection<Attack> redAttacks = new ArrayList<>();
        Collection<Defense> redDefenses = new ArrayList<>();

        Collection<Piece> blackPieces = new ArrayList<>();
        Collection<Move> blackLegalMoves = new ArrayList<>();
        int blackMobilityValue = 0;
        Collection<Attack> blackAttacks = new ArrayList<>();
        Collection<Defense> blackDefenses = new ArrayList<>();

        Player redPlayer = getPlayer(Alliance.RED);
        Player blackPlayer = getPlayer(Alliance.BLACK);

        for (Piece piece : redPlayer.getActivePieces()) {
            if (piece.equals(movedPiece) || piece.equals(capturedPiece)) continue;
            redPieces.add(piece);
            Collection<Move> moves = piece.getLegalMoves(this, redAttacks, redDefenses);
            redLegalMoves.addAll(moves);
            redMobilityValue += piece.getPieceType().getMobilityValue() * moves.size();
        }
        for (Piece piece : blackPlayer.getActivePieces()) {
            if (piece.equals(movedPiece) || piece.equals(capturedPiece)) continue;
            blackPieces.add(piece);
            Collection<Move> moves = piece.getLegalMoves(this, blackAttacks, blackDefenses);
            blackLegalMoves.addAll(moves);
            blackMobilityValue += piece.getPieceType().getMobilityValue() * moves.size();
        }
        if (destPiece.getAlliance().isRed()) {
            redPieces.add(destPiece);
            Collection<Move> moves = destPiece.getLegalMoves(this, redAttacks, redDefenses);
            redLegalMoves.addAll(moves);
            redMobilityValue += destPiece.getPieceType().getMobilityValue() * moves.size();
        } else {
            blackPieces.add(destPiece);
            Collection<Move> moves = destPiece.getLegalMoves(this, blackAttacks, blackDefenses);
            blackLegalMoves.addAll(moves);
            blackMobilityValue += destPiece.getPieceType().getMobilityValue() * moves.size();
        }

        redPlayer = new Player(Alliance.RED, redPieces, redLegalMoves, blackLegalMoves,
                redMobilityValue, redAttacks, redDefenses);
        blackPlayer = new Player(Alliance.BLACK, blackPieces, blackLegalMoves, redLegalMoves,
                blackMobilityValue, blackAttacks, blackDefenses);
        return new PlayerInfo(redPlayer, blackPlayer);
    }

    /**
     * Makes the given move on this board. Player information and Zobrist key are updated.
     * @param move The move to be made.
     */
    public void makeMove(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(BoardUtil.positionToIndex(srcPosition));
        srcPoint.removePiece();
        Point destPoint = points.get(BoardUtil.positionToIndex(destPosition));
        destPoint.setPiece(movedPiece.movePiece(move));

        playerInfoHistory.add(playerInfo);
        playerInfo = updatePlayerInfo(move);
        changeTurn();
        zobristKey = ZOBRIST.updateKey(zobristKey, move);
    }

    /**
     * Undoes the given move on this board. Player information and Zobrist key are updated.
     * @param move The move to be undone.
     */
    public void unmakeMove(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Optional<Piece> capturedPiece = move.getCapturedPiece();
        Coordinate srcPosition = movedPiece.getPosition();
        Coordinate destPosition = move.getDestPosition();

        Point srcPoint = points.get(BoardUtil.positionToIndex(srcPosition));
        srcPoint.setPiece(movedPiece);
        Point destPoint = points.get(BoardUtil.positionToIndex(destPosition));
        destPoint.removePiece();
        capturedPiece.ifPresent(destPoint::setPiece);

        playerInfo = playerInfoHistory.isEmpty() ? generatePlayerInfo()
                : playerInfoHistory.remove(playerInfoHistory.size() - 1);
        changeTurn();
        zobristKey = ZOBRIST.updateKey(zobristKey, move);
    }

    /**
     * Switches the current turn on the board. Zobrist key is updated.
     */
    public void changeTurn() {
        currTurn = currTurn.opposite();
        zobristKey ^= ZOBRIST.side;
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
     * Checks if the current player's opponent is in check. Such a state is not allowed.
     * @return true if the current player's opponent is NOT in check, false otherwise.
     */
    public boolean isStateAllowed() {
        return !getOppPlayer().isInCheck();
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
        for (Piece piece : getPlayer(Alliance.RED).getActivePieces()) {
            if (piece.getPieceType().isAttacking()) {
                return false;
            }
        }
        for (Piece piece : getPlayer(Alliance.BLACK).getActivePieces()) {
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
     * Checks if the current player has given a check for three consecutive times.
     * @return true if the current player has given a check for three consecutive times, false otherwise.
     */
    public boolean lastThreeChecks() {
        if (playerInfoHistory.size() < 5) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            if (currTurn.isRed()) {
                if (!playerInfoHistory.get(playerInfoHistory.size() - 1 - i*2).blackPlayer.isInCheck()) {
                    return false;
                }
            } else {
                if (!playerInfoHistory.get(playerInfoHistory.size() - 1 - i*2).redPlayer.isInCheck()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the advisor structure of the player with the given alliance.
     * @param alliance The alliance of the player to check.
     * @return The advisor structure of the player with the given alliance.
     */
    public AdvisorStructure getAdvisorStructure(Alliance alliance) {
        int lowRow = BoardUtil.rankToRow(1, alliance);
        int midRow = BoardUtil.rankToRow(2, alliance);
        int leftCol = BoardUtil.fileToCol(6, alliance);
        int rightCol = BoardUtil.fileToCol(4, alliance);

        Optional<Piece> left = getPoint(new Coordinate(lowRow, leftCol)).getPiece();
        Optional<Piece> right = getPoint(new Coordinate(lowRow, rightCol)).getPiece();
        Optional<Piece> mid = getPoint(new Coordinate(midRow, 4)).getPiece();
        boolean hasLeft = left.map(p -> p.getPieceType().equals(PieceType.ADVISOR)).orElse(false);
        boolean hasRight = right.map(p -> p.getPieceType().equals(PieceType.ADVISOR)).orElse(false);
        boolean hasMid = mid.map(p -> p.getPieceType().equals(PieceType.ADVISOR)).orElse(false);

        if (hasLeft && hasRight) {
            return AdvisorStructure.START;
        } else if (hasLeft && hasMid) {
            return AdvisorStructure.LEFT;
        } else if (hasRight && hasMid) {
            return AdvisorStructure.RIGHT;
        } else {
            return AdvisorStructure.OTHER;
        }
    }

    public Point getPoint(Coordinate position) {
        return points.get(BoardUtil.positionToIndex(position));
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public Player getPlayer(Alliance alliance) {
        return alliance.isRed() ? playerInfo.redPlayer : playerInfo.blackPlayer;
    }

    public Player getCurrPlayer() {
        return currTurn.isRed() ? getPlayer(Alliance.RED) : getPlayer(Alliance.BLACK);
    }

    public Player getOppPlayer() {
        return currTurn.isRed() ? getPlayer(Alliance.BLACK) : getPlayer(Alliance.RED);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                String pointText = points.get(BoardUtil.positionToIndex(row, col)).toString();
                sb.append(String.format("%3s", pointText));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Represents the structure of a player's advisors.
     */
    public enum AdvisorStructure {
        START, // both advisors on starting positions
        LEFT, // one on left file, one on middle
        RIGHT, // one on right file, one on middle
        OTHER // any other structure
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
            int posIndex = BoardUtil.positionToIndex(piece.getPosition());

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
