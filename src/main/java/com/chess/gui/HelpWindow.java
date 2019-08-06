package com.chess.gui;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * A dialog for displaying game controls.
 */
class HelpWindow extends Dialog {

    HelpWindow() {
        DialogPane dialogPane = new DialogPane();
        GridPane gridPane = new GridPane();

        Label lmbLabel = new Label("LMB - Select piece to move / Choose destination for selected piece");
        Label rmbLabel = new Label("RMB - Cancel piece selection");
        Label zLabel = new Label("Z - Toggle replay mode");
        Label aLabel = new Label("A - Go to previous move");
        Label dLabel = new Label("D - Go to next move");
        Label wLabel = new Label("W - Go to previous turn");
        Label sLabel = new Label("S - Go to next turn");
        Label qLabel = new Label("Q - Go to first move");
        Label eLabel = new Label("E - Go to last move");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        gridPane.add(lmbLabel, 0, 0);
        gridPane.add(rmbLabel, 0, 1);
        gridPane.add(zLabel, 0, 2);
        gridPane.add(aLabel, 0, 3);
        gridPane.add(dLabel, 0, 4);
        gridPane.add(wLabel, 0, 5);
        gridPane.add(sLabel, 0, 6);
        gridPane.add(qLabel, 0, 7);
        gridPane.add(eLabel, 0, 8);
        dialogPane.setContent(gridPane);
        dialogPane.getButtonTypes().add(close);

        setTitle("Controls");
        setDialogPane(dialogPane);
        setResizable(false);
        hide();
    }
}
