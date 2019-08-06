package com.chess.gui;

import com.chess.CChess;
import com.chess.engine.LoadGameUtil;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveBook;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static com.chess.engine.pieces.Piece.*;
import static javafx.scene.control.Alert.*;

public class Table extends BorderPane {

    private static final int BOARD_WIDTH = 540;
    private static final int BOARD_HEIGHT = 600;
    private static final int POINT_WIDTH = 60;
    private static final String GRAPHICS_MISC_PATH = "/graphics/misc/";
    private static final String GRAPHICS_PIECES_PATH = "/graphics/pieces/";
    private static final Image BOARD_IMAGE = new Image(Table.class.getResourceAsStream("/graphics/board.png"));
    private static final Image HIGHLIGHT_LEGALS_IMAGE =
            new Image(Table.class.getResourceAsStream(GRAPHICS_MISC_PATH + "dot.png"));
    private static final Border HIGHLIGHT_LAST_MOVE_BORDER =
            new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(1.8)));
    private static final Border HIGHLIGHT_SELECTED_PIECE_BORDER =
            new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2.5)));
    private static final String WIKI_XIANGQI = "https://en.wikipedia.org/wiki/Xiangqi";
    static final Map<String, Image> PIECE_IMAGE_MAP = getPieceImageMap();
    private static final Table TABLE_INSTANCE = new Table();

    private final BoardPane boardPane;
    private final MoveHistoryPane moveHistoryPane;
    private final InfoPane infoPane;
    private final MoveLog fullMoveLog;
    private final List<Board> boardHistory;
    private final GameSetup gameSetup;
    private final HelpWindow helpWindow;
    private final AIObserver aiObserver;
    private final PropertyChangeSupport propertyChangeSupport;
    private Board currBoard;
    private Point sourcePoint;
    private Point destPoint;
    private Piece selectedPiece;
    private MoveLog partialMoveLog;
    private boolean inReplayMode;

    private Table() {
        currBoard = Board.initialiseBoard();
        inReplayMode = false;

        boardPane = new BoardPane();
        moveHistoryPane = new MoveHistoryPane();
        infoPane = new InfoPane();
        infoPane.updateStatusPane(currBoard);
        fullMoveLog = new MoveLog();
        boardHistory = new ArrayList<>();
        boardHistory.add(currBoard);
        gameSetup = new GameSetup();
        helpWindow = new HelpWindow();
        aiObserver = new AIObserver();
        propertyChangeSupport = new PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener(aiObserver);

        setTop(createMenuBar());
        setCenter(boardPane);
        setRight(moveHistoryPane);
        setLeft(infoPane);
    }

    /**
     * Returns an instance of this table.
     * @return An instance of this table.
     */
    public static Table getInstance() {
        return TABLE_INSTANCE;
    }

    /**
     * Creates and returns a menu bar for this table.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(createGameMenu(), createOptionsMenu(), createHelpMenu());
        return menuBar;
    }

    /**
     * Creates and returns a game menu for the menu bar.
     */
    private Menu createGameMenu() {
        Menu gameMenu = new Menu("Game");

        MenuItem newGame = new MenuItem("New game");
        newGame.setOnAction(e -> {
            if (fullMoveLog.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
                alert.setTitle("New game");
                alert.showAndWait();
                return;
            }
            Alert alert = new Alert(AlertType.CONFIRMATION, "Start a new game?");
            alert.setTitle("New game");
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.OK)) {
                    aiObserver.stopAI();
                    exitReplayMode();
                    undoAllMoves();
                    notifyAIObserver("newgame");
                }
            });
        });

        MenuItem saveGame = new MenuItem("Save game...");
        saveGame.setOnAction(e -> saveGame());

        MenuItem loadGame = new MenuItem("Load game...");
        loadGame.setOnAction(e -> loadGame());

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> System.exit(0));

        gameMenu.getItems().addAll(newGame, new SeparatorMenuItem(), saveGame, loadGame, new SeparatorMenuItem(), exit);

        return gameMenu;
    }

    /**
     * Creates and returns an options menu for the menu bar.
     */
    private Menu createOptionsMenu() {
        Menu optionsMenu = new Menu("Options");

        MenuItem undoTurn = new MenuItem("Undo last turn");
        undoTurn.setOnAction(e -> {
            aiObserver.stopAI();
            exitReplayMode();
            undoLastTurn();
            notifyAIObserver("undoturn");
        });

        MenuItem undoMove = new MenuItem("Undo last move");
        undoMove.setOnAction(e -> {
            aiObserver.stopAI();
            exitReplayMode();
            undoLastMove();
            notifyAIObserver("undomove");
        });

        MenuItem setup = new MenuItem("Setup...");
        setup.setOnAction(e -> {
            aiObserver.stopAI();
            gameSetup.showAndWait();
            notifyAIObserver("setup");
        });

        MenuItem flipBoard = new MenuItem("Flip board");
        flipBoard.setOnAction(e -> boardPane.flipBoard());

        optionsMenu.getItems().addAll(undoTurn, undoMove, new SeparatorMenuItem(),
                setup, new SeparatorMenuItem(), flipBoard);

        return optionsMenu;
    }

    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");

        MenuItem rules = new MenuItem("Rules (Wikipedia)");
        rules.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URL(WIKI_XIANGQI).toURI());
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        });

        MenuItem controls = new MenuItem("Controls...");
        controls.setOnAction(e -> helpWindow.showAndWait());

        helpMenu.getItems().addAll(rules, controls);

        return helpMenu;
    }

    /**
     * Saves the current game in-progress into a loadable text file.
     */
    private void saveGame() {
        if (fullMoveLog.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
            alert.setTitle("Save game");
            alert.showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save game");
        File file = fc.showSaveDialog(CChess.stage);

        if (file != null) {
            try {
                PrintWriter pw = new PrintWriter(file);
                for (Move move : fullMoveLog.getMoves()) {
                    pw.append(move.toString()).append("\n");
                }
                pw.flush();
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Alert alert = new Alert(AlertType.INFORMATION, "Save success");
            alert.setTitle("Save game");
            alert.showAndWait();
        }
    }

    /**
     * Loads a text file to restore a previously saved game.
     */
    private void loadGame() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load game");
        File file = fc.showOpenDialog(CChess.stage);

        if (file != null) {
            LoadGameUtil lgu = new LoadGameUtil(file);
            if (!lgu.isValidFile()) {
                Alert alert = new Alert(AlertType.ERROR, "Invalid file");
                alert.setTitle("Load game");
                alert.showAndWait();
            } else {
                aiObserver.stopAI();
                exitReplayMode();

                List<Board> boards = lgu.getBoardHistory();
                boardHistory.clear();
                boardHistory.addAll(boards);
                currBoard = boardHistory.get(boardHistory.size() - 1);

                fullMoveLog.clear();
                for (Move move : lgu.getMoves()) {
                    fullMoveLog.addMove(move);
                }
                moveHistoryPane.update(fullMoveLog);
                infoPane.updateCapturedPanes(fullMoveLog);
                infoPane.updateStatusPane(currBoard);
                boardPane.drawBoard(currBoard);

                notifyAIObserver("load");

                Alert alert = new Alert(AlertType.INFORMATION, "Load success");
                alert.setTitle("Load game");
                alert.showAndWait();
            }
        }
    }

    /**
     * Undoes the last move of either player.
     */
    private void undoLastMove() {
        if (!fullMoveLog.isEmpty()) {
            clearSelections();

            fullMoveLog.removeLastMove();
            boardHistory.remove(boardHistory.size() - 1);
            currBoard = boardHistory.get(boardHistory.size() - 1);

            moveHistoryPane.update(fullMoveLog);
            infoPane.updateCapturedPanes(fullMoveLog);
            infoPane.updateStatusPane(currBoard);
            boardPane.drawBoard(currBoard);
        } else {
            Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
            alert.setTitle("Undo last move");
            alert.showAndWait();
        }
    }

    /**
     * Undoes two consecutive moves.
     */
    private void undoLastTurn() {
        if (fullMoveLog.getSize() > 1) {
            undoLastMove();
            undoLastMove();
        } else {
            Alert alert = new Alert(AlertType.INFORMATION, "No turns made");
            alert.setTitle("Undo last turn");
            alert.showAndWait();
        }
    }

    /**
     * Undoes all moves made.
     */
    private void undoAllMoves() {
        while (!fullMoveLog.isEmpty()) {
            undoLastMove();
        }
    }

    /**
     * Clears all mouse selections made by the human player.
     */
    private void clearSelections() {
        sourcePoint = null;
        destPoint = null;
        selectedPiece = null;
    }

    /**
     * Notifies the AI observer with the given property name.
     */
    private void notifyAIObserver(String propertyName) {
        propertyChangeSupport.firePropertyChange(propertyName, null, null);
    }

    /**
     * Exits replay mode if currently in it.
     */
    private void exitReplayMode() {
        if (inReplayMode) {
            jumpToMove(-1);
        }
    }

    /**
     * Exits replay mode if moveIndex = -1; else enters replay mode at the given moveIndex.
     * @param moveIndex The index of the move in the full movelog.
     */
    void jumpToMove(int moveIndex) {
        if (moveIndex < -1 || moveIndex >= fullMoveLog.getSize()) return;
        if (moveIndex == -1) {
            inReplayMode = false;
            partialMoveLog = null;
            currBoard = boardHistory.get(boardHistory.size() - 1);
            boardPane.drawBoard(currBoard);
            infoPane.updateStatusPane(currBoard);
            infoPane.updateCapturedPanes(fullMoveLog);
            notifyAIObserver("exitreplay");
        } else {
            aiObserver.stopAI();
            inReplayMode = true;
            partialMoveLog = fullMoveLog.getPartialLog(moveIndex);
            clearSelections();
            currBoard = boardHistory.get(moveIndex + 1);
            boardPane.drawBoard(currBoard);
            infoPane.updateStatusPane(currBoard);
            infoPane.updateCapturedPanes(partialMoveLog);
        }
    }

    /**
     * Returns a mapping from a string representing a piece to its corresponding image.
     */
    private static Map<String, Image> getPieceImageMap() {
        Map<String, Image> pieceImageMap = new HashMap<>();

        for (PieceType pieceType : PieceType.values()) {
            String name = ("R" + pieceType.toString()).toLowerCase();
            Image image = new Image(Table.class.getResourceAsStream(GRAPHICS_PIECES_PATH + name + ".png"));
            pieceImageMap.put(name, image);

            name = ("B" + pieceType.toString()).toLowerCase();
            image = new Image(Table.class.getResourceAsStream(GRAPHICS_PIECES_PATH + name + ".png"));
            pieceImageMap.put(name, image);
        }

        return pieceImageMap;
    }

    /**
     * Helper class for storing all moves made.
     */
    static class MoveLog {

        private final List<Move> moves;

        private MoveLog() {
            this.moves = new ArrayList<>();
        }

        List<Move> getMoves() {
            return moves;
        }

        int getSize() {
            return moves.size();
        }

        boolean isEmpty() {
            return moves.isEmpty();
        }

        void addMove(Move move) {
            moves.add(move);
        }

        void removeLastMove() {
            if (!moves.isEmpty()) {
                moves.remove(moves.size() - 1);
            }
        }

        Move getLastMove() {
            if (!moves.isEmpty()) {
                return moves.get(moves.size() - 1);
            }

            return null;
        }

        MoveLog getPartialLog(int moveIndex) {
            MoveLog partialLog = new MoveLog();
            for (int i = 0; i < moveIndex + 1; i++) {
                partialLog.addMove(moves.get(i));
            }
            return partialLog;
        }

        void clear() {
            moves.clear();
        }
    }

    /**
     * A pane for displaying the game board.
     */
    private class BoardPane extends GridPane {

        private final List<PointPane> pointPanes;
        private BoardDirection boardDirection;

        private BoardPane() {
            pointPanes = new ArrayList<>();
            boardDirection = BoardDirection.NORMAL;

            for (int row = 0; row < Board.NUM_ROWS; row++) {
                for (int col = 0; col < Board.NUM_COLS; col++) {
                    PointPane pointPane = new PointPane(new Coordinate(row, col));
                    pointPanes.add(pointPane);
                    add(pointPane, col, row);
                }
            }

            BackgroundImage boardImage = new BackgroundImage(BOARD_IMAGE, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, null, null);
            setBackground(new Background(boardImage));
            setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
            setMinSize(BOARD_WIDTH, BOARD_HEIGHT);
            setMaxSize(BOARD_WIDTH, BOARD_HEIGHT);
            setGridLinesVisible(false);
        }

        /**
         * Redraws this board pane given the board.
         * @param board The current board.
         */
        private void drawBoard(Board board) {
            getChildren().clear();

            for (PointPane pointPane : pointPanes) {
                pointPane.drawPoint(board);
                if (boardDirection.isNormal()) {
                    add(pointPane, pointPane.position.getCol(), pointPane.position.getRow());
                } else {
                    add(pointPane, Board.NUM_COLS - pointPane.position.getCol(),
                            Board.NUM_ROWS - pointPane.position.getRow());
                }
            }
        }

        /**
         * Flips the current board.
         */
        private void flipBoard() {
            boardDirection = boardDirection.opposite();
            drawBoard(currBoard);
            infoPane.setDirection(boardDirection);
        }
    }

    /**
     * Represents the direction of the board.
     */
    public enum BoardDirection {
        NORMAL {
            @Override
            boolean isNormal() {
                return true;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            boolean isNormal() {
                return false;
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract boolean isNormal();

        abstract BoardDirection opposite();
    }

    /**
     * A pane for displaying a point on the board.
     */
    private class PointPane extends StackPane {

        private final Coordinate position;

        PointPane(Coordinate position) {
            this.position = position;

            setPrefSize(POINT_WIDTH, POINT_WIDTH);
            setMinSize(POINT_WIDTH, POINT_WIDTH);
            setMaxSize(POINT_WIDTH, POINT_WIDTH);
            setOnMouseClicked(getMouseEventHandler());

            assignPointPieceIcon(currBoard);
        }

        /**
         * Returns the mouse event handler for this point pane.
         */
        private EventHandler<MouseEvent> getMouseEventHandler() {
            return event -> {
                if (inReplayMode) return;
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    clearSelections();
                } else if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (sourcePoint == null) {
                        sourcePoint = currBoard.getPoint(position);
                        Optional<Piece> selectedPiece = sourcePoint.getPiece();
                        if (selectedPiece.isPresent()
                                && selectedPiece.get().getAlliance() == currBoard.getCurrPlayer().getAlliance()
                                && !gameSetup.isAIPlayer(currBoard.getCurrPlayer())) {
                            Table.this.selectedPiece = selectedPiece.get();
                        } else {
                            sourcePoint = null;
                        }
                    } else {
                        destPoint = currBoard.getPoint(position);
                        Optional<Move> move = Move.getMove(currBoard, sourcePoint.getPosition(),
                                destPoint.getPosition());
                        if (!move.isPresent()) return;

                        MoveTransition transition = currBoard.getCurrPlayer().makeMove(move.get());
                        if (transition.getMoveStatus().isDone()) {
                            currBoard = transition.getNextBoard();
                            boardHistory.add(currBoard);
                            fullMoveLog.addMove(transition.getMove());

                            clearSelections();
                            Platform.runLater(() -> {
                                moveHistoryPane.update(fullMoveLog);
                                infoPane.updateCapturedPanes(fullMoveLog);
                                infoPane.updateStatusPane(currBoard);
                                notifyAIObserver("movemade");
                            });
                        }
                    }
                }
                Platform.runLater(() -> boardPane.drawBoard(currBoard));
            };
        }

        /**
         * Redraws this point pane given the board.
         * @param board The current board.
         */
        private void drawPoint(Board board) {
            assignPointPieceIcon(board);
            highlightLastMoveAndSelectedPiece();
            highlightPossibleMoves(board);
        }

        /**
         * Assigns an image to this point pane given the current piece (if any) on it.
         * @param board The current board.
         */
        private void assignPointPieceIcon(Board board) {
            getChildren().clear();

            Point point = board.getPoint(position);
            Optional<Piece> destPiece = point.getPiece();
            destPiece.ifPresent(p -> {
                String name = (p.getAlliance().toString().substring(0, 1) + p.getPieceType().toString()).toLowerCase();
                Label label = new Label();
                label.setGraphic(new ImageView(PIECE_IMAGE_MAP.get(name)));
                getChildren().add(label);
            });
        }

        /**
         * Highlights (using a border) this point pane if it contains the selected piece
         * OR it was part of the previous move.
         */
        private void highlightLastMoveAndSelectedPiece() {
            Move lastMove;
            if (inReplayMode) {
                lastMove = partialMoveLog.getLastMove();
            } else {
                lastMove = fullMoveLog.getLastMove();
            }

            if (lastMove != null) {
                Piece lastMovedPiece = lastMove.getMovedPiece();
                if (lastMovedPiece.getPosition().equals(position)) {
                    setBorder(HIGHLIGHT_LAST_MOVE_BORDER);
                    return;
                }
                if (lastMove.getDestPosition().equals(position)) {
                    setBorder(HIGHLIGHT_LAST_MOVE_BORDER);
                    return;
                }
            }

            if (selectedPiece != null && selectedPiece.getPosition().equals(position)) {
                setBorder(HIGHLIGHT_SELECTED_PIECE_BORDER);
                return;
            }

            setBorder(null);
        }

        /**
         * Highlights (using a dot) this point pane if it is one of the legal destinations of the selected piece.
         * @param board The current board.
         */
        private void highlightPossibleMoves(Board board) {
            for (Move move : pieceLegalMoves(board)) {
                // check for suicidal move
                MoveTransition transition = board.getCurrPlayer().makeMove(move);
                if (!transition.getMoveStatus().isDone()) {
                    continue;
                }

                // legal AND non-suicidal move
                if (move.getDestPosition().equals(position)) {
                    Label label = new Label();
                    label.setGraphic(new ImageView(HIGHLIGHT_LEGALS_IMAGE));
                    getChildren().add(label);
                }
            }
        }

        /**
         * Returns a collection of legal moves of the selected piece.
         */
        private Collection<Move> pieceLegalMoves(Board board) {
            if (selectedPiece != null) {
                return selectedPiece.getLegalMoves(board);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Helper class for player communication involving AI.
     */
    private static class AIObserver implements PropertyChangeListener {

        private static final MoveBook movebook = MoveBook.getInstance();

        private FixedDepthAIPlayer fixedDepthAIPlayer;
        private FixedTimeAIPlayer fixedTimeAIPlayer;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!Table.getInstance().inReplayMode
                    && getInstance().gameSetup.isAIPlayer(getInstance().currBoard.getCurrPlayer())
                    && !getInstance().currBoard.getCurrPlayer().isInCheckmate()) {
                Board board = getInstance().currBoard;
                Optional<Move> move = movebook.getRandomMove(board);
                if (move.isPresent()) {
                    makeMove(move.get());
                    System.out.println(move.get() + " [movebook]");
                    return;
                }
                if (getInstance().gameSetup.isAITimeLimited()) {
                    fixedTimeAIPlayer = new FixedTimeAIPlayer();
                    Thread th = new Thread(fixedTimeAIPlayer);
                    th.setDaemon(true);
                    th.start();
                } else {
                    fixedDepthAIPlayer = new FixedDepthAIPlayer();
                    Thread th = new Thread(fixedDepthAIPlayer);
                    th.setDaemon(true);
                    th.start();
                }
            }
        }

        /**
         * Executes the given move on the board.
         * @param move
         */
        private static void makeMove(Move move) {
            getInstance().currBoard = getInstance().currBoard.getCurrPlayer().makeMove(move).getNextBoard();
            getInstance().boardHistory.add(getInstance().currBoard);
            getInstance().fullMoveLog.addMove(move);
            getInstance().boardPane.drawBoard(getInstance().currBoard);
            getInstance().moveHistoryPane.update(getInstance().fullMoveLog);
            getInstance().infoPane.updateCapturedPanes(getInstance().fullMoveLog);
            getInstance().infoPane.updateStatusPane(getInstance().currBoard);
            getInstance().notifyAIObserver("movemade");
        }

        /**
         * Terminates all running AI (if any).
         */
        private void stopAI() {
            if (fixedDepthAIPlayer != null) {
                fixedDepthAIPlayer.cancel(true);
            }
            if (fixedTimeAIPlayer != null) {
                fixedTimeAIPlayer.cancelTimer();
                fixedTimeAIPlayer.cancel(true);
            }
        }
    }

    /**
     * Represents an AI player.
     */
    private abstract static class AIPlayer extends Task<Move> {

        private static final int MAX_CONSEC_CHECKS = 3;

        Piece bannedPiece; // the piece not to use for checking the opponent

        private AIPlayer() {
            bannedPiece = getBannedPiece();
        }

        private Piece getBannedPiece() {
            List<Board> boardHistory = getInstance().boardHistory;
            List<Move> moveHistory = getInstance().fullMoveLog.getMoves();
            if (moveHistory.size() < MAX_CONSEC_CHECKS * 2) return null;

            for (int i = 0; i < MAX_CONSEC_CHECKS; i++) {
                Board board = boardHistory.get(boardHistory.size() - 2 - i*2);
                if (!board.getCurrPlayer().isInCheck()) return null;
            }

            Board board = boardHistory.get(boardHistory.size() - MAX_CONSEC_CHECKS*2);
            Move move = moveHistory.get(moveHistory.size() - MAX_CONSEC_CHECKS*2);
            if (move.getCapturedPiece().isPresent()) return null;
            Piece movedPiece = board.getPoint(move.getDestPosition()).getPiece().get();
            for (int i = 1; i < MAX_CONSEC_CHECKS; i++) {
                move = moveHistory.get(moveHistory.size() - MAX_CONSEC_CHECKS*2 + i*2);
                if (!move.getMovedPiece().equals(movedPiece) || move.getCapturedPiece().isPresent()) return null;
                board = boardHistory.get(boardHistory.size() - MAX_CONSEC_CHECKS*2 + i*2);
                movedPiece = board.getPoint(move.getDestPosition()).getPiece().get();
            }

            // check limit reached
            return movedPiece;
        }
    }

    /**
     * Represents a fixed-depth AI player.
     */
    private static class FixedDepthAIPlayer extends AIPlayer {

        private int searchDepth;
        private long startTime;

        @Override
        public void done() {
            if (isCancelled()) return;
            try {
                Move bestMove = get();
                Platform.runLater(() -> AIObserver.makeMove(bestMove));
                System.out.println(bestMove.toString() + " | "
                        + (System.currentTimeMillis() - startTime)/1000 + "s | " + "depth " + searchDepth);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Move call() {
            startTime = System.currentTimeMillis();
            searchDepth = getInstance().gameSetup.getSearchDepth();
            return MiniMax.getInstance().fixedDepth(getInstance().currBoard, searchDepth, bannedPiece);
        }
    }

    /**
     * Represents a fixed-time AI player.
     */
    public static class FixedTimeAIPlayer extends AIPlayer implements PropertyChangeListener {

        private final Timer timer;
        private Move currBestMove;
        private int currDepth;
        private TimerTask currTask;
        private int searchTime;

        private FixedTimeAIPlayer() {
            timer = new Timer("AI Timer");
        }

        @Override
        protected Move call() {
            currTask = getTimerTask();
            searchTime = getInstance().gameSetup.getSearchTime();
            timer.schedule(currTask, searchTime * 1000);
            return MiniMax.getInstance().iterativeDeepening(getInstance().currBoard, bannedPiece, this,
                    System.currentTimeMillis() + searchTime*1000);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            currBestMove = (Move) evt.getNewValue();
            currDepth = (int) evt.getOldValue();
        }

        /**
         * Returns a timer task for forcing a move when time is up.
         * @return
         */
        private TimerTask getTimerTask() {
            return new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> AIObserver.makeMove(currBestMove));
                    System.out.println(currBestMove.toString() + " | "
                            + searchTime + "s | " + "depth " + currDepth);
                    FixedTimeAIPlayer.this.cancel(true);
                }
            };
        }

        /**
         * Cancels the current timer task.
         */
        void cancelTimer() {
            if (currTask != null) {
                currTask.cancel();
            }
        }
    }

    /**
     * Represents a human or AI player type.
     */
    public enum PlayerType {
        HUMAN {
            @Override
            boolean isAI() {
                return false;
            }
        },
        AI {
            @Override
            boolean isAI() {
                return true;
            }
        };

        abstract boolean isAI();
    }

    /**
     * Represents a time- or depth-fixed AI player type.
     */
    public enum AIType {
        TIME {
            @Override
            boolean isTimeLimited() {
                return true;
            }
        },
        DEPTH {
            @Override
            boolean isTimeLimited() {
                return false;
            }
        };

        abstract boolean isTimeLimited();
    }
}
