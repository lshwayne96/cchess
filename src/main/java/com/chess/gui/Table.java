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
    private final MoveLog fullMovelog;
    private final List<Board> boardHistory;
    private final GameSetup gameSetup;
    private final HelpWindow helpWindow;
    private final AIObserver aiObserver;
    private final PropertyChangeSupport propertyChangeSupport;
    private Board currBoard;
    private Point sourcePoint;
    private Point destPoint;
    private Piece selectedPiece;
    private MoveLog partialMovelog;

    private Table() {
        currBoard = Board.initialiseBoard();

        boardPane = new BoardPane();
        moveHistoryPane = new MoveHistoryPane();
        infoPane = new InfoPane();
        fullMovelog = new MoveLog();
        infoPane.update(currBoard, fullMovelog);
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

        MenuItem newGame = new MenuItem("New");
        newGame.setOnAction(e -> {
            if (fullMovelog.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
                alert.setTitle("New");
                alert.showAndWait();
                return;
            }
            Alert alert = new Alert(AlertType.CONFIRMATION, "Start a new game?");
            alert.setTitle("New");
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.OK)) {
                    aiObserver.stopAI();
                    exitReplayMode();
                    restart();
                    notifyAIObserver("newgame");
                }
            });
        });

        MenuItem saveGame = new MenuItem("Save...");
        saveGame.setOnAction(e -> saveGame());

        MenuItem loadGame = new MenuItem("Load...");
        loadGame.setOnAction(e -> loadGame());

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> System.exit(0));

        gameMenu.getItems().addAll(newGame, new SeparatorMenuItem(), saveGame, loadGame, new SeparatorMenuItem(), exit);

        return gameMenu;
    }

    private void restart() {
        clearSelections();
        currBoard = Board.initialiseBoard();
        boardHistory.clear();
        boardHistory.add(currBoard);
        fullMovelog.clear();

        boardPane.drawBoard(currBoard);
        moveHistoryPane.update(fullMovelog);
        infoPane.update(currBoard, fullMovelog);
    }

    /**
     * Creates and returns an options menu for the menu bar.
     */
    private Menu createOptionsMenu() {
        Menu optionsMenu = new Menu("Options");

        MenuItem undoTurn = new MenuItem("Undo turn");
        undoTurn.setOnAction(e -> {
            aiObserver.stopAI();
            exitReplayMode();
            undoLastTurn();
            notifyAIObserver("undoturn");
        });

        MenuItem undoMove = new MenuItem("Undo move");
        undoMove.setOnAction(e -> {
            aiObserver.stopAI();
            exitReplayMode();
            undoLastMove();
            notifyAIObserver("undomove");
        });

        MenuItem setup = new MenuItem("Setup...");
        setup.setOnAction(e -> {
            clearSelections();
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
     * Saves the current game in-progress into a loadable text file.
     */
    private void saveGame() {
        if (fullMovelog.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
            alert.setTitle("Save");
            alert.showAndWait();
            return;
        }

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

            Alert alert = new Alert(AlertType.INFORMATION, "Save success");
            alert.setTitle("Save");
            alert.showAndWait();
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
                Alert alert = new Alert(AlertType.ERROR, "Invalid file");
                alert.setTitle("Load");
                alert.showAndWait();
            } else {
                aiObserver.stopAI();
                exitReplayMode();

                List<Board> boards = lgu.getBoardHistory();
                boardHistory.clear();
                boardHistory.addAll(boards);
                currBoard = boardHistory.get(boardHistory.size() - 1);

                fullMovelog.clear();
                for (Move move : lgu.getMoves()) {
                    fullMovelog.addMove(move);
                }
                moveHistoryPane.update(fullMovelog);
                infoPane.update(currBoard, fullMovelog);
                boardPane.drawBoard(currBoard);

                notifyAIObserver("load");

                Alert alert = new Alert(AlertType.INFORMATION, "Load success");
                alert.setTitle("Load");
                alert.showAndWait();
            }
        }
    }

    /**
     * Undoes the last move of either player.
     */
    private void undoLastMove() {
        if (!fullMovelog.isEmpty()) {
            clearSelections();

            fullMovelog.removeLastMove();
            boardHistory.remove(boardHistory.size() - 1);
            currBoard = boardHistory.get(boardHistory.size() - 1);

            moveHistoryPane.update(fullMovelog);
            infoPane.update(currBoard, fullMovelog);
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
        if (fullMovelog.getSize() > 1) {
            undoLastMove();
            undoLastMove();
        } else {
            Alert alert = new Alert(AlertType.INFORMATION, "No turns made");
            alert.setTitle("Undo last turn");
            alert.showAndWait();
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
        if (moveHistoryPane.isInReplayMode()) {
            moveHistoryPane.disableReplay();
            jumpToMove(-1, false);
        }
    }

    /**
     * Exits replay mode if moveIndex = -1; else enters replay mode at the given moveIndex.
     * @param moveIndex The index of the move in the full movelog.
     * @param notifyAI Whether to notify the AI observer.
     */
    void jumpToMove(int moveIndex, boolean notifyAI) {
        if (moveIndex < -1 || moveIndex >= fullMovelog.getSize()) return;
        if (moveIndex == -1) {
            partialMovelog = null;
            currBoard = boardHistory.get(boardHistory.size() - 1);
            boardPane.drawBoard(currBoard);
            infoPane.update(currBoard, fullMovelog);
            if (notifyAI) {
                Table.getInstance().notifyAIObserver("exitreplay");
            }
        } else {
            aiObserver.stopAI();
            partialMovelog = fullMovelog.getPartialLog(moveIndex);
            clearSelections();
            currBoard = boardHistory.get(moveIndex + 1);
            boardPane.drawBoard(currBoard);
            infoPane.update(currBoard, partialMovelog);
        }
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
                if (moveHistoryPane.isInReplayMode()) return;
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
                            fullMovelog.addMove(transition.getMove());

                            clearSelections();
                            Platform.runLater(() -> {
                                moveHistoryPane.update(fullMovelog);
                                infoPane.update(currBoard, fullMovelog);
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
                    && getInstance().gameSetup.isAIPlayer(getInstance().currBoard.getCurrPlayer())
                    && !getInstance().currBoard.getCurrPlayer().isInCheckmate()) {
                Board board = getInstance().currBoard;
                Optional<Move> move = movebook.getRandomMove(board);
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
            getInstance().currBoard = getInstance().currBoard.getCurrPlayer().makeMove(move).getNextBoard();
            getInstance().boardHistory.add(getInstance().currBoard);
            getInstance().fullMovelog.addMove(move);
            getInstance().boardPane.drawBoard(getInstance().currBoard);
            getInstance().moveHistoryPane.update(getInstance().fullMovelog);
            getInstance().infoPane.update(getInstance().currBoard, getInstance().fullMovelog);
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

        private static final int MAX_CONSEC_CHECKS = 3;

        final Timer timer;
        TimerTask task;
        Piece bannedPiece;

        private AIPlayer() {
            timer = new Timer("AI Timer");
            bannedPiece = getBannedPiece();
        }

        /**
         * Returns the piece not to use for checking the opponent.
         */
        private Piece getBannedPiece() {
            List<Board> boardHistory = getInstance().boardHistory;
            List<Move> moveHistory = getInstance().fullMovelog.getMoves();
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

            return movedPiece;
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
            return MiniMax.getInstance().fixedDepth(getInstance().currBoard, searchDepth, bannedPiece);
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
            timer.schedule(task, searchTime * 1000);
            searchTime = getInstance().gameSetup.getSearchTime();
            return MiniMax.getInstance().fixedTime(getInstance().currBoard, bannedPiece, this,
                    System.currentTimeMillis() + searchTime*1000);
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
