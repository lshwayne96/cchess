package com.chess;

import com.chess.gui.Table;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class CChess extends Application  {

    private static final int TABLE_WIDTH = 782;
    private static final int TABLE_HEIGHT = 625;

    public static Stage stage;
    private static final Table table = Table.getInstance();
    private static final Scene tableScene = new Scene(table, TABLE_WIDTH, TABLE_HEIGHT);

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        stage.setTitle("CChess");
        stage.getIcons().add(new Image(CChess.class.getResourceAsStream("/graphics/icon.png")));
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setScene(tableScene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(CChess.class, args);
    }
}
