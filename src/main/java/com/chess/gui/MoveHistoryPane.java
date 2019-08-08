package com.chess.gui;

import com.chess.engine.board.Move;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

import static com.chess.gui.Table.*;
import static javafx.scene.control.Alert.*;

/**
 * A pane for displaying the move history of the game.
 */
class MoveHistoryPane extends BorderPane {

    private static final int HISTORY_PANE_WIDTH = 120;
    private static final int HISTORY_PANE_HEIGHT = 600;
    private static final Label EMPTY_TABLE_MESSAGE = new Label("No moves made");
    private static final Image PREV_MOVE =
            new Image(MoveHistoryPane.class.getResourceAsStream(GuiUtil.GRAPHICS_MISC_PATH + "prev.png"));
    private static final Image NEXT_MOVE =
            new Image(MoveHistoryPane.class.getResourceAsStream(GuiUtil.GRAPHICS_MISC_PATH + "next.png"));
    private static final Image START_MOVE =
            new Image(MoveHistoryPane.class.getResourceAsStream(GuiUtil.GRAPHICS_MISC_PATH + "start.png"));
    private static final Image END_MOVE =
            new Image(MoveHistoryPane.class.getResourceAsStream(GuiUtil.GRAPHICS_MISC_PATH + "end.png"));

    private final ReplayPane replayPane;
    private final TableView<Turn> turnTableView;
    private final ObservableList<Turn> turnList;

    MoveHistoryPane() {
        turnList = FXCollections.observableList(new ArrayList<>());
        turnTableView = new TableView<>(turnList);

        TableColumn<Turn, String> redMoveCol = new TableColumn<>("RED");
        redMoveCol.setCellValueFactory(new PropertyValueFactory<>("redMove"));
        redMoveCol.setSortable(false);
        redMoveCol.setReorderable(false);
        TableColumn<Turn, String> blackMoveCol = new TableColumn<>("BLACK");
        blackMoveCol.setCellValueFactory(new PropertyValueFactory<>("blackMove"));
        blackMoveCol.setSortable(false);
        blackMoveCol.setReorderable(false);

        turnTableView.getColumns().setAll(redMoveCol, blackMoveCol);
        turnTableView.getSelectionModel().setCellSelectionEnabled(true);
        turnTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        turnTableView.setPrefWidth(HISTORY_PANE_WIDTH);
        turnTableView.setPrefHeight(HISTORY_PANE_HEIGHT);
        turnTableView.setPlaceholder(EMPTY_TABLE_MESSAGE);

        replayPane = new ReplayPane();
        ObservableList selectedCells = turnTableView.getSelectionModel().getSelectedCells();
        selectedCells.addListener((ListChangeListener) c -> {
            if (selectedCells.isEmpty()) {
                Table.getInstance().jumpToMove(-1, true);
                return;
            }
            if (!replayPane.toggleReplay.isSelected()) {
                replayPane.toggleReplay.fire();
            }
            TablePosition tablePosition = (TablePosition) selectedCells.get(0);
            int moveIndex = tablePosition.getRow()*2 + tablePosition.getColumn();
            Table.getInstance().jumpToMove(moveIndex, true);
        });

        setTop(replayPane);
        setCenter(turnTableView);
        setVisible(true);
    }

    /**
     * Updates this move history pane based on the given movelog.
     * @param movelog The current movelog.
     */
    void update(MoveLog movelog) {
        turnList.clear();
        if (movelog.isEmpty()) {
            turnTableView.setPlaceholder(EMPTY_TABLE_MESSAGE);
            return;
        }

        Turn currTurn = new Turn();
        for (Move move : movelog.getMoves()) {
            if (move.getMovedPiece().getAlliance().isRed()) {
                currTurn = new Turn();
                currTurn.setRedMove(move.toString());
                turnList.add(currTurn);
            } else {
                currTurn.setBlackMove(move.toString());
            }
        }

        turnTableView.scrollTo(turnList.size() - 1);
    }

    /**
     * A pane for navigating replays.
     */
    private class ReplayPane extends GridPane {
        private final ToggleButton toggleReplay;
        private final Button prevMove;
        private final Button nextMove;
        private final Button startMove;
        private final Button endMove;

        private ReplayPane() {
            toggleReplay = new ToggleButton("REPLAY");
            toggleReplay.setOnAction(e -> {
                if (toggleReplay.isSelected()) {
                    if (!turnList.isEmpty()) {
                        disableReplayButtons(false);
                        if (turnTableView.getSelectionModel().getSelectedCells().isEmpty()) {
                            turnTableView.getSelectionModel().select(0, turnTableView.getColumns().get(0));
                            turnTableView.scrollTo(0);
                        }
                    } else {
                        Alert alert = new Alert(AlertType.INFORMATION, "No moves made");
                        alert.showAndWait();
                        toggleReplay.setSelected(false);
                    }
                } else {
                    disableReplayButtons(true);
                    turnTableView.getSelectionModel().clearSelection();
                }
            });
            toggleReplay.setSelected(false);
            toggleReplay.setPrefWidth(HISTORY_PANE_WIDTH);

            GridPane navigationPane = new GridPane();
            prevMove = new Button("", new ImageView(PREV_MOVE));
            nextMove = new Button("", new ImageView(NEXT_MOVE));
            startMove = new Button("", new ImageView(START_MOVE));
            endMove = new Button("", new ImageView(END_MOVE));

            TableColumn<Turn, ?> redMoveCol = turnTableView.getColumns().get(0);
            TableColumn<Turn, ?> blackMoveCol = turnTableView.getColumns().get(1);
            ObservableList selectedCells = turnTableView.getSelectionModel().getSelectedCells();
            prevMove.setOnAction(e -> {
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);
                if (tablePosition.getTableColumn().equals(blackMoveCol)) {
                    turnTableView.getSelectionModel().clearAndSelect(tablePosition.getRow(), redMoveCol);
                } else if (tablePosition.getTableColumn().equals(redMoveCol) && tablePosition.getRow() > 0) {
                    turnTableView.getSelectionModel().clearAndSelect(tablePosition.getRow() - 1, blackMoveCol);
                }
                turnTableView.scrollTo(turnTableView.getSelectionModel().getSelectedIndex());
            });
            nextMove.setOnAction(e -> {
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);
                Turn currTurn = turnList.get(tablePosition.getRow());
                if (tablePosition.getTableColumn().equals(redMoveCol) && currTurn.getBlackMove() != null) {
                    turnTableView.getSelectionModel().clearAndSelect(tablePosition.getRow(), blackMoveCol);
                } else if (tablePosition.getTableColumn().equals(blackMoveCol)
                        && tablePosition.getRow() < turnList.size() - 1) {
                    turnTableView.getSelectionModel().clearAndSelect(tablePosition.getRow() + 1, redMoveCol);
                }
                turnTableView.scrollTo(turnTableView.getSelectionModel().getSelectedIndex());
            });
            startMove.setOnAction(e -> {
                turnTableView.getSelectionModel().clearAndSelect(0, redMoveCol);
                turnTableView.scrollTo(turnTableView.getSelectionModel().getSelectedIndex());
            });
            endMove.setOnAction(e -> {
                Turn lastTurn = turnList.get(turnList.size() - 1);
                if (lastTurn.getBlackMove() != null) {
                    turnTableView.getSelectionModel().clearAndSelect(turnList.size() - 1, blackMoveCol);
                } else {
                    turnTableView.getSelectionModel().clearAndSelect(turnList.size() - 1, redMoveCol);
                }
                turnTableView.scrollTo(turnTableView.getSelectionModel().getSelectedIndex());
            });
            disableReplayButtons(true);
            prevMove.setPrefWidth(HISTORY_PANE_WIDTH / 2);
            nextMove.setPrefWidth(HISTORY_PANE_WIDTH / 2);
            startMove.setPrefWidth(HISTORY_PANE_WIDTH / 2);
            endMove.setPrefWidth(HISTORY_PANE_WIDTH / 2);
            navigationPane.add(prevMove, 0, 0);
            navigationPane.add(nextMove, 1, 0);
            navigationPane.add(startMove, 0, 1);
            navigationPane.add(endMove, 1, 1);

            add(toggleReplay, 0, 0);
            add(navigationPane, 0, 1);
        }

        /**
         * Disables/enables all replay buttons.
         */
        private void disableReplayButtons(boolean disabled) {
            nextMove.setDisable(disabled);
            prevMove.setDisable(disabled);
            startMove.setDisable(disabled);
            endMove.setDisable(disabled);
        }
    }

    /**
     * Disables replay mode.
     */
    void disableReplay() {
        if (replayPane.toggleReplay.isSelected()) {
            replayPane.toggleReplay.fire();
        }
    }

    /**
     * Checks if the game is in replay mode.
     * @return true if the game is in replay mode, false otherwise.
     */
    boolean isInReplayMode() {
        return replayPane.toggleReplay.isSelected();
    }

    /**
     * Represents a pair of consecutive RED and BLACK player moves.
     */
    public static class Turn {

        private StringProperty redMove;
        private StringProperty blackMove;

        public String getRedMove() {
            return redMoveProperty().get();
        }

        public String getBlackMove() {
            return blackMoveProperty().get();
        }

        private void setRedMove(String move) {
            redMoveProperty().set(move);
        }

        private void setBlackMove(String move) {
            blackMoveProperty().set(move);
        }

        private StringProperty redMoveProperty() {
            if (redMove == null) {
                redMove = new SimpleStringProperty(this, "redMove");
            }
            return redMove;
        }

        private StringProperty blackMoveProperty() {
            if (blackMove == null) {
                blackMove = new SimpleStringProperty(this, "blackMove");
            }
            return blackMove;
        }
    }
}
