package com.chess.gui;

import com.chess.engine.board.Move;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;

import static com.chess.gui.Table.*;

/**
 * A pane for displaying the move history of the game.
 */
class MoveHistoryPane extends BorderPane {

    private static final int HISTORY_PANE_WIDTH = 120;
    private static final int HISTORY_PANE_HEIGHT = 600;
    private static final Label EMPTY_TABLE_MESSAGE = new Label("No moves made");

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
        turnTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        turnTableView.setPrefWidth(HISTORY_PANE_WIDTH);
        turnTableView.setPrefHeight(HISTORY_PANE_HEIGHT);
        turnTableView.setPlaceholder(EMPTY_TABLE_MESSAGE);

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
