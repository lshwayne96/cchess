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
        Label lmbLabel = new Label("LMB - Select piece to move / Choose destination for selected piece");
        Label rmbLabel = new Label("RMB - Cancel piece selection");
        Label replayHeader = GuiUtil.getHeader("Replay mode");
        Label zLabel = new Label("Z - Toggle replay mode");
        Label aLabel = new Label("A - Go to previous move");
        Label dLabel = new Label("D - Go to next move");
        Label wLabel = new Label("W - Go to previous turn");
        Label sLabel = new Label("S - Go to next turn");
        Label qLabel = new Label("Q - Go to first move");
        Label eLabel = new Label("E - Go to last move");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        nodes.add(playHeader);
        nodes.add(lmbLabel);
        nodes.add(rmbLabel);
        nodes.add(GuiUtil.getSeparator());
        nodes.add(replayHeader);
        nodes.add(zLabel);
        nodes.add(aLabel);
        nodes.add(dLabel);
        nodes.add(wLabel);
        nodes.add(sLabel);
        nodes.add(qLabel);
        nodes.add(eLabel);

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
