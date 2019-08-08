package com.chess.gui;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog for displaying game controls.
 */
class HelpWindow extends Dialog {

    HelpWindow() {
        DialogPane dialogPane = new DialogPane();
        GridPane gridPane = new GridPane();
        List<Node> nodes = new ArrayList<>();

        Label playHeader = GuiUtil.getHeader("Play mode");
        Label lmbLabel = new Label("\u2022 LMB \u2014 Select piece to move | Choose destination (green dot) for selected piece");
        Label rmbLabel = new Label("\u2022 RMB \u2014 Cancel piece selection");
        Label replayHeader = GuiUtil.getHeader("Replay mode");
        Label prevLabel = new Label("\u2022 Left single arrow \u2014 Go to previous move");
        Label nextLabel = new Label("\u2022 Right single arrow \u2014 Go to next move");
        Label startLabel = new Label("\u2022 Left double arrow \u2014 Go to first move");
        Label endLabel = new Label("\u2022 Right double arrow \u2014 Go to last move");
        Label noteHeader = GuiUtil.getHeader("Notes");
        Label note1 = new Label("\u2022 Click the REPLAY button to toggle between Play and Replay mode");
        Label note2 = new Label("\u2022 Replay mode can also be activated by clicking on a move in the move history");
        Label note3 = new Label("\u2022 While in Replay mode, all controls from Play mode are disabled (and vice versa)");
        Label note4 = new Label("\u2022 Starting/loading a new game or undoing a turn/move will automatically exit Replay mode");
        Label note5 = new Label("\u2022 Entering Setup or Replay mode will stop any thinking AI");

        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        nodes.add(playHeader);
        nodes.add(lmbLabel);
        nodes.add(rmbLabel);
        nodes.add(GuiUtil.getSeparator());
        nodes.add(replayHeader);
        nodes.add(prevLabel);
        nodes.add(nextLabel);
        nodes.add(startLabel);
        nodes.add(endLabel);
        nodes.add(GuiUtil.getSeparator());
        nodes.add(noteHeader);
        nodes.add(note1);
        nodes.add(note2);
        nodes.add(note3);
        nodes.add(note4);
        nodes.add(note5);

        for (int i = 0; i < nodes.size(); i++) {
            gridPane.add(nodes.get(i), 0, i);
        }
        dialogPane.setContent(gridPane);
        dialogPane.getButtonTypes().add(close);

        setTitle("Controls");
        setDialogPane(dialogPane);
        setResizable(false);
        hide();
    }
}
