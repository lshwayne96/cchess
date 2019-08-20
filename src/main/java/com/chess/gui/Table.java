package com.chess.gui;

import com.chess.CChess;
import com.chess.engine.LoadGameUtil;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.ai.FixedDepthSearch;
import com.chess.engine.player.ai.FixedTimeSearch;
import com.chess.engine.player.ai.MoveBook;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
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
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static com.chess.engine.pieces.Piece.*;
import static javafx.scene.control.Alert.*;

public class Table extends BorderPane {

    private static final int BOARD_WIDTH = 540;
    private static final int BOARD_HEIGHT = 600;
    private static final int POINT_WIDTH = 60;
    private static final Image BOARD_IMAGE = new Image(Table.class.getResourceAsStream("/graphics/board.png"));
    private static final Image HIGHLIGHT_LEGALS_IMAGE =
            new Image(Table.class.getResourceAsStream(GuiUtil.GRAPHICS_MISC_PATH + "dot.png"));
    private static final Border HIGHLIGHT_LAST_MOVE_BORDER =
            new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.DASHED,
                    new CornerRadii(POINT_WIDTH / 2), new BorderWidths(2)));
    private static final Border HIGHLIGHT_SELECTED_PIECE_BORDER =
            new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
                    new CornerRadii(POINT_WIDTH / 2), new BorderWidths(2)));
    private static final String WIKI_XIANGQI = "https://en.wikipedia.org/wiki/Xiangqi";
    static final Map<String, Image> PIECE_IMAGE_MAP = getPieceImageMap();
    private static final Table TABLE_INSTANCE = new Table();

    private final BoardPane boardPane;
    private final MoveHistoryPane moveHistoryPane;
    private final InfoPane infoPane;
    private final GameSetup gameSetup;
    private final HelpWindow helpWindow;
    private final AIObserver aiObserver;
    private final PropertyChangeSupport propertyChangeSupport;
    private Board board;
    private MoveLog fullMovelog;
    private MoveLog partialMovelog;
    private Point sourcePoint;
    private Point destPoint;
    private Piece selectedPiece;
    private List<Move> bannedMoves;
    private boolean highlightLegalMoves;

    private Table() {
        board = Board.initialiseBoard();
        boardPane = new BoardPane();
        moveHistoryPane = new MoveHistoryPane();
        infoPane = new InfoPane();
        fullMovelog = new MoveLog();
        infoPane.update(board, fullMovelog);
        gameSetup = new GameSetup();
        helpWindow = new HelpWindow();
        aiObserver = new AIObserver();
        propertyChangeSupport = new PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener(aiObserver);
        bannedMoves = new ArrayList<>();
        highlightLegalMoves = true;

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
        menuBar.getMenus().addAll(createGameMenu(), createOptionsMenu(), createPreferencesMenu(), createHelpMenu());
        return menuBar;
    }

    /**
     * Creates and returns a game menu for the menu bar.
     */
    private Menu createGameMenu() {
        Menu gameMenu = new Menu("Game");

        MenuItem newGame = new MenuItem("New");
        newGame.setOnAction(e -> {
            if (fullMovelog.isEmpty()) {
                showAlert(AlertType.INFORMATION, "New", "No moves made");
                return;
            }
            showAlert(AlertType.CONFIRMATION, "New", "Start a new game?")
                    .ifPresent(response -> {
                        if (response.equals(ButtonType.OK)) {
                            aiObserver.stopAI();
                            exitReplayMode();
                            restart();
                            notifyAIObserver("newgame");
                        }
                    });
        });

        MenuItem saveGame = new MenuItem("Save...");
        saveGame.setOnAction(e -> {
            if (fullMovelog.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Save", "No moves made");
                return;
            }
            saveGame();
        });

        MenuItem loadGame = new MenuItem("Load...");
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
            if (fullMovelog.getSize() < 2) {
                showAlert(AlertType.INFORMATION, "Undo last turn", "No turns made");
                return;
            }
            aiObserver.stopAI();
            exitReplayMode();
            undoLastTurn();
            notifyAIObserver("undoturn");
        });

        MenuItem undoMove = new MenuItem("Undo last move");
        undoMove.setOnAction(e -> {
            if (fullMovelog.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Undo last move", "No moves made");
                return;
            }
            aiObserver.stopAI();
            exitReplayMode();
            undoLastMove();
            notifyAIObserver("undomove");
        });

        MenuItem playFromMove = new MenuItem("Play from selected move");
        playFromMove.setOnAction(e -> {
            if (!moveHistoryPane.isInReplayMode()) {
                showAlert(AlertType.INFORMATION, "Play from selected move", "No move selected");
                return;
            }
            showAlert(AlertType.CONFIRMATION, "Play from selected move",
                    "Subsequent moves will be deleted. Continue?")
                    .ifPresent(response -> {
                        if (response.equals(ButtonType.OK)) {
                            playFromSelectedMove();
                            notifyAIObserver("playfrommove");
                        }
                    });
        });

        MenuItem banMove = new MenuItem("Ban selected move");
        banMove.setOnAction(e -> {
            if (!moveHistoryPane.isInReplayMode()) {
                showAlert(AlertType.INFORMATION, "Ban selected move", "No move selected");
                return;
            }
            Move bannedMove = partialMovelog.getLastMove();
            bannedMoves.add(bannedMove);
            showAlert(AlertType.INFORMATION, "Ban selected move", bannedMove.toString() + " has been banned");
        });

        MenuItem unbanAll = new MenuItem("Unban all");
        unbanAll.setOnAction(e -> {
            if (bannedMoves.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Unban all", "No moves banned");
                return;
            }
            aiObserver.stopAI();
            showAlert(AlertType.INFORMATION, "Unban all", "All moves unbanned");
            bannedMoves.clear();
            notifyAIObserver("unban");
        });

        MenuItem setup = new MenuItem("Setup...");
        setup.setOnAction(e -> {
            clearSelections();
            aiObserver.stopAI();
            gameSetup.showAndWait();
            notifyAIObserver("setup");
        });

        optionsMenu.getItems().addAll(undoTurn, undoMove, playFromMove, new SeparatorMenuItem(),
                banMove, unbanAll, new SeparatorMenuItem(), setup);

        return optionsMenu;
    }

    /**
     * Creates and returns a preferences menu for the menu bar.
     */
    private Menu createPreferencesMenu() {
        Menu prefMenu = new Menu("Preferences");

        CheckMenuItem highlight = new CheckMenuItem("Highlight legal moves");
        highlight.setSelected(highlightLegalMoves);
        highlight.setOnAction(e -> highlightLegalMoves = highlight.isSelected());

        MenuItem flipBoard = new MenuItem("Flip board");
        flipBoard.setOnAction(e -> boardPane.flipBoard());

        prefMenu.getItems().addAll(highlight, new SeparatorMenuItem(), flipBoard);

        return prefMenu;
    }

    /**
     * Creates and returns a help menu for the menu bar.
     */
    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");

        MenuItem rules = new MenuItem("Rules...");
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
     * Restarts the game.
     */
    private void restart() {
        clearSelections();
        board = Board.initialiseBoard();
        fullMovelog.clear();
        bannedMoves.clear();

        boardPane.drawBoard(board);
        moveHistoryPane.update(fullMovelog);
        infoPane.update(board, fullMovelog);
    }

    /**
     * Saves the current game in-progress into a loadable text file.
     */
    private void saveGame() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save");
        File file = fc.showSaveDialog(CChess.stage);

        if (file != null) {
            try {
                PrintWriter pw = new PrintWriter(file);
                for (Move move : fullMovelog.getMoves()) {
                    pw.append(move.toString()).append("\n");
                }
                pw.flush();
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            showAlert(AlertType.INFORMATION, "Save", "Save success");
        }
    }

    /**
     * Loads a text file to restore a previously saved game.
     */
    private void loadGame() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load");
        File file = fc.showOpenDialog(CChess.stage);

        if (file != null) {
            LoadGameUtil lgu = new LoadGameUtil(file);
            if (!lgu.isValidFile()) {
                showAlert(AlertType.ERROR, "Load", "Invalid file");
            } else {
                aiObserver.stopAI();
                exitReplayMode();

                board = lgu.getBoard();
                fullMovelog.clear();
                for (Move move : lgu.getMoves()) {
                    fullMovelog.addMove(move);
                }
                moveHistoryPane.update(fullMovelog);
                infoPane.update(board, fullMovelog);
                boardPane.drawBoard(board);

                notifyAIObserver("load");

                showAlert(AlertType.INFORMATION, "Load", "Load success");
            }
        }
    }

    /**
     * Undoes the last move of either player.
     */
    private void undoLastMove() {
        if (!fullMovelog.isEmpty()) {
            clearSelections();

            board.unmakeMove(fullMovelog.getLastMove());
            fullMovelog.removeLastMove();

            moveHistoryPane.update(fullMovelog);
            infoPane.update(board, fullMovelog);
            boardPane.drawBoard(board);
        }
    }

    /**
     * Undoes two consecutive moves.
     */
    private void undoLastTurn() {
        if (fullMovelog.getSize() > 1) {
            undoLastMove();
            undoLastMove();
        }
    }

    /**
     * Enters play mode from the selected move in replay mode.
     */
    private void playFromSelectedMove() {
        if (!moveHistoryPane.isInReplayMode()) return;

        fullMovelog = partialMovelog;
        exitReplayMode();
        moveHistoryPane.update(fullMovelog);
    }

    /**
     * Exits replay mode if currently in it.
     */
    private void exitReplayMode() {
        if (moveHistoryPane.isInReplayMode()) {
            moveHistoryPane.disableReplay();
            aiObserver.stopAI();
        }
    }

    /**
     * Exits replay mode if moveIndex = -1; else enters replay mode at the given moveIndex.
     * @param moveIndex The index of the move in the full movelog.
     */
    void jumpToMove(int moveIndex) {
        if (moveIndex < -1 || moveIndex >= fullMovelog.getSize()) return;
        if (moveIndex == -1) {
            int currIndex = partialMovelog.getSize() - 1;
            partialMovelog = null;
            for (int i = currIndex + 1; i < fullMovelog.getSize(); i++) {
                board.makeMove(fullMovelog.getMoves().get(i));
            }
            boardPane.drawBoard(board);
            infoPane.update(board, fullMovelog);
            Table.getInstance().notifyAIObserver("exitreplay");
        } else {
            aiObserver.stopAI();
            int currIndex = partialMovelog == null ? fullMovelog.getSize() - 1: partialMovelog.getSize() - 1;
            partialMovelog = fullMovelog.getPartialLog(moveIndex);
            clearSelections();
            for (int i = currIndex + 1; i <= moveIndex; i++) {
                board.makeMove(fullMovelog.getMoves().get(i));
            }
            for (int i = currIndex; i > moveIndex; i--) {
                board.unmakeMove(fullMovelog.getMoves().get(i));
            }
            boardPane.drawBoard(board);
            infoPane.update(board, partialMovelog);
        }
    }

    /**
     * Notifies the AI observer with the given property name.
     */
    private void notifyAIObserver(String propertyName) {
        propertyChangeSupport.firePropertyChange(propertyName, null, null);
    }

    /**
     * Checks if the current AI's moves are randomised.
     * @return true if the current AI's moves are randomised, false otherwise.
     */
    public boolean isAIRandomised() {
        return gameSetup.isAIRandomised();
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
     * Creates an alert given the alert type, title and content strings.
     */
    private Optional<ButtonType> showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType, content);
        alert.setTitle(title);
        return alert.showAndWait();
    }

    /**
     * Returns a mapping from a string representing a piece to its corresponding image.
     */
    private static Map<String, Image> getPieceImageMap() {
        Map<String, Image> pieceImageMap = new HashMap<>();

        for (PieceType pieceType : PieceType.values()) {
            String name = ("R" + pieceType.toString()).toLowerCase();
            Image image = new Image(Table.class.getResourceAsStream(GuiUtil.GRAPHICS_PIECES_PATH + name + ".png"));
            pieceImageMap.put(name, image);

            name = ("B" + pieceType.toString()).toLowerCase();
            image = new Image(Table.class.getResourceAsStream(GuiUtil.GRAPHICS_PIECES_PATH + name + ".png"));
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
            drawBoard(board);
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

            assignPointPieceIcon(board);
        }

        /**
         * Returns the mouse event handler for this point pane.
         */
        private EventHandler<MouseEvent> getMouseEventHandler() {
            return event -> {
                if (moveHistoryPane.isInReplayMode()) return;
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    clearSelections();
                } else if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (sourcePoint == null) {
                        sourcePoint = board.getPoint(position);
                        Optional<Piece> selectedPiece = sourcePoint.getPiece();
                        if (selectedPiece.isPresent()
                                && selectedPiece.get().getAlliance() == board.getCurrPlayer().getAlliance()
                                && !gameSetup.isAIPlayer(board.getCurrPlayer())) {
                            Table.this.selectedPiece = selectedPiece.get();
                        } else {
                            sourcePoint = null;
                        }
                    } else {
                        destPoint = board.getPoint(position);
                        Optional<Move> move = board.getMove(sourcePoint.getPosition(), destPoint.getPosition());
                        if (!move.isPresent()) return;

                        board.makeMove(move.get());
                        if (board.isLegalState()) {
                            fullMovelog.addMove(move.get());

                            clearSelections();
                            Platform.runLater(() -> {
                                moveHistoryPane.update(fullMovelog);
                                infoPane.update(board, fullMovelog);
                                notifyAIObserver("movemade");
                            });
                        } else {
                            board.unmakeMove(move.get());
                        }
                    }
                }
                Platform.runLater(() -> boardPane.drawBoard(board));
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
            if (moveHistoryPane.isInReplayMode()) {
                lastMove = partialMovelog.getLastMove();
            } else {
                lastMove = fullMovelog.getLastMove();
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
            if (!highlightLegalMoves) return;
            for (Move move : pieceLegalMoves(board)) {
                board.makeMove(move);
                // check for suicidal move
                if (!board.isLegalState()) {
                    board.unmakeMove(move);
                    continue;
                }
                board.unmakeMove(move);
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

        private static final int MIN_TIME = 1000;

        private final Timer timer;
        private final Stack<AIPlayer> aiPlayers;
        private TimerTask task;

        private AIObserver() {
            timer = new Timer("Movebook Timer");
            aiPlayers = new Stack<>();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!Table.getInstance().moveHistoryPane.isInReplayMode()
                    && getInstance().gameSetup.isAIPlayer(getInstance().board.getCurrPlayer())
                    && !getInstance().board.getCurrPlayer().isInCheckmate()) {
                Optional<Move> move = MoveBook.getRandomMove(getInstance().board.getState());
                if (move.isPresent()) {
                    task = getTimerTask(move.get());
                    timer.schedule(task, MIN_TIME);
                    return;
                }

                AIPlayer aiPlayer = getInstance().gameSetup.isAITimeLimited()
                                ? new FixedTimeAIPlayer() : new FixedDepthAIPlayer();
                aiPlayers.push(aiPlayer);
                Thread th = new Thread(aiPlayer);
                th.setDaemon(true);
                th.start();
            }
        }

        /**
         * Executes the given move on the board.
         */
        private static void makeMove(Move move) {
            getInstance().board.makeMove(move);
            getInstance().fullMovelog.addMove(move);
            getInstance().boardPane.drawBoard(getInstance().board);
            getInstance().moveHistoryPane.update(getInstance().fullMovelog);
            getInstance().infoPane.update(getInstance().board, getInstance().fullMovelog);
            getInstance().notifyAIObserver("movemade");
        }

        /**
         * Returns a timer task for a move in the movebook.
         */
        private TimerTask getTimerTask(Move move) {
            return new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> makeMove(move));
                    System.out.println(move + " [movebook]");
                }
            };
        }

        /**
         * Terminates all running AI (if any).
         */
        private void stopAI() {
            if (task != null) {
                task.cancel();
            }
            while (!aiPlayers.isEmpty()) {
                AIPlayer aiPlayer = aiPlayers.pop();
                aiPlayer.stop();
            }
        }
    }

    /**
     * Represents an AI player.
     */
    private static abstract class AIPlayer extends Task<Move> {

        final List<Move> bannedMoves;
        final Timer timer;
        TimerTask task;

        private AIPlayer() {
            timer = new Timer("AI Timer");
            bannedMoves = new ArrayList<>(getInstance().bannedMoves);
        }

        /**
         * Stops this AI player and its timer task.
         */
        private void stop() {
            if (task != null) {
                task.cancel();
            }
            cancel(true);
        }
    }

    /**
     * Represents a fixed-depth AI player.
     */
    private static class FixedDepthAIPlayer extends AIPlayer {

        private Move bestMove;
        private int searchDepth;
        private long startTime;

        @Override
        public void done() {
            if (isCancelled()) return;
            try {
                bestMove = get();
                if (System.currentTimeMillis() > startTime + AIObserver.MIN_TIME) {
                    move();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Move call() {
            task = getTimerTask();
            timer.schedule(task, AIObserver.MIN_TIME);
            startTime = System.currentTimeMillis();
            searchDepth = getInstance().gameSetup.getSearchDepth();
            return new FixedDepthSearch(getInstance().board.getCopy(), bannedMoves, searchDepth).search();
        }

        /**
         * Executes the move.
         */
        private void move() {
            Platform.runLater(() -> AIObserver.makeMove(bestMove));
            System.out.println(bestMove.toString() + " | "
                    + (System.currentTimeMillis() - startTime)/1000 + "s | " + "depth " + searchDepth);
        }

        /**
         * Returns a timer task for keeping a minimum time before AI moves.
         */
        private TimerTask getTimerTask() {
            return new TimerTask() {
                @Override
                public void run() {
                    if (bestMove != null) {
                        move();
                    }
                }
            };
        }
    }

    /**
     * Represents a fixed-time AI player.
     */
    public static class FixedTimeAIPlayer extends AIPlayer implements PropertyChangeListener {

        private Move currBestMove;
        private int currDepth;
        private int searchTime;

        @Override
        protected Move call() {
            task = getTimerTask();
            searchTime = getInstance().gameSetup.getSearchTime();
            timer.schedule(task, searchTime * 1000);
            return new FixedTimeSearch(getInstance().board.getCopy(), bannedMoves, this,
                    System.currentTimeMillis() + searchTime*1000).search();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            currBestMove = (Move) evt.getNewValue();
            currDepth = (int) evt.getOldValue();
        }

        /**
         * Returns a timer task for forcing a move when time is up.
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
