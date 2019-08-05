package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.chess.gui.Table.*;

/**
 * A pane for displaying captured pieces on both alliances as well as the current status of the game.
 */
class InfoPane extends BorderPane {

    private static final int INFO_PANE_WIDTH = 120;
    private static final int INFO_PANE_HEIGHT = 600;

    private final CapturedPane redCapturedPane;
    private final CapturedPane blackCapturedPane;
    private final StatusPane statusPane;

    InfoPane() {
        redCapturedPane = new CapturedPane(Alliance.RED);
        blackCapturedPane = new CapturedPane(Alliance.BLACK);
        statusPane = new StatusPane();

        setTop(blackCapturedPane);
        setBottom(redCapturedPane);
        setCenter(statusPane);

        setPrefSize(INFO_PANE_WIDTH, INFO_PANE_HEIGHT);
        setMinSize(INFO_PANE_WIDTH, INFO_PANE_HEIGHT);
        setMaxSize(INFO_PANE_WIDTH, INFO_PANE_HEIGHT);
    }

    /**
     * Updates both captured panes based on the given movelog.
     * @param movelog The current movelog.
     */
    void updateCapturedPanes(MoveLog movelog) {
        redCapturedPane.update(movelog);
        blackCapturedPane.update(movelog);
    }

    /**
     * Updates the status pane based on the given board.
     * @param board The current board.
     */
    void updateStatusPane(Board board) {
        statusPane.update(board);
    }

    /**
     * A pane for displaying captured pieces for an alliance.
     */
    private static class CapturedPane extends GridPane {

        private static final int CAPTURED_PANE_HEIGHT = 250;
        private static final Color CAPTURED_PANE_COLOR = Color.LIGHTGRAY;
        private static final Background CAPTURED_PANE_BACKGROUND =
                new Background(new BackgroundFill(CAPTURED_PANE_COLOR, CornerRadii.EMPTY, Insets.EMPTY));

        private final Alliance alliance;

        private CapturedPane(Alliance alliance) {
            this.alliance = alliance;
            setBackground(CAPTURED_PANE_BACKGROUND);
            setPrefSize(INFO_PANE_WIDTH, CAPTURED_PANE_HEIGHT);
        }

        /**
         * Updates this captured pane based on the given movelog.
         */
        private void update(MoveLog movelog) {
            getChildren().clear();
            List<Piece> capturedPieces = new ArrayList<>();

            for (Move move : movelog.getMoves()) {
                Optional<Piece> capturedPiece = move.getCapturedPiece();
                capturedPiece.ifPresent(p -> {
                    if (p.getAlliance().equals(alliance)) {
                        capturedPieces.add(p);
                    }
                });
            }

            Comparator<Piece> comparator = Comparator.comparing(Piece::getPieceType);
            capturedPieces.sort(comparator);

            for (int i = 0; i < capturedPieces.size(); i++) {
                Piece piece = capturedPieces.get(i);
                String name = (piece.getAlliance().toString().substring(0, 1)
                        + piece.getPieceType().toString()).toLowerCase();
                Image image = PIECE_IMAGE_MAP.get(name);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(image.getWidth() / 2);
                imageView.setFitHeight(image.getHeight() / 2);

                Label label = new Label();
                label.setGraphic(imageView);
                label.setPrefSize(INFO_PANE_WIDTH / 2, CAPTURED_PANE_HEIGHT / 8);
                label.setAlignment(Pos.CENTER);
                add(label, i % 2, i / 2);
            }
        }
    }

    /**
     * A pane for displaying the current status of the game.
     */
    private static class StatusPane extends GridPane {

        private static final int STATUS_PANE_HEIGHT = 100;
        private static final Font TOP_FONT = Font.font("System", FontWeight.MEDIUM, Font.getDefault().getSize() + 3);
        private static final Font BOTTOM_FONT = Font.font("System", FontWeight.BOLD, Font.getDefault().getSize() + 3);
        private static final Label CHECK_LABEL = getCheckLabel();
        private static final Label CHECKMATE_LABEL = getCheckmateLabel();
        private static final Label DRAW_LABEL = getDrawLabel();

        private StatusPane() {
            setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT);
        }

        /**
         * Returns a label for CHECK status.
         */
        private static Label getCheckLabel() {
            Label label = new Label("Check");
            label.setFont(BOTTOM_FONT);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT / 2);

            return label;
        }

        /**
         * Returns a label for CHECKMATE status.
         */
        private static Label getCheckmateLabel() {
            Label label = new Label("Checkmate");
            label.setFont(BOTTOM_FONT);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT / 2);

            return label;
        }

        /**
         * Returns a label for DRAW status.
         */
        private static Label getDrawLabel() {
            Label label = new Label("Draw");
            label.setFont(BOTTOM_FONT);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT / 2);

            return label;
        }

        /**
         * Updates this status pane based on the given board.
         */
        private void update(Board board) {
            getChildren().clear();

            if (board.isGameOver()) {
                Label gameOverLabel = new Label(board.getCurrPlayer().getOpponent().getAlliance().toString() + " wins");
                gameOverLabel.setFont(TOP_FONT);
                gameOverLabel.setAlignment(Pos.CENTER);
                gameOverLabel.setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT / 2);
                add(gameOverLabel, 0, 0);
                add(CHECKMATE_LABEL, 0, 1);
                return;
            }

            Label moveLabel = new Label(board.getCurrPlayer().getAlliance().toString() + "'s move");
            moveLabel.setFont(TOP_FONT);
            moveLabel.setAlignment(Pos.CENTER);
            moveLabel.setPrefSize(INFO_PANE_WIDTH, STATUS_PANE_HEIGHT / 2);
            add(moveLabel, 0, 0);
            if (board.isGameDraw()) {
                add(DRAW_LABEL, 0, 1);
            } else if (board.getCurrPlayer().isInCheck()) {
                add(CHECK_LABEL, 0, 1);
            }
        }
    }

    /**
     * Sets the direction of the captured panes based on the given board direction.
     * @param direction The current board direction.
     */
    void setDirection(BoardDirection direction) {
        getChildren().remove(blackCapturedPane);
        getChildren().remove(redCapturedPane);

        if (direction.isNormal()) {
            setTop(blackCapturedPane);
            setBottom(redCapturedPane);
        } else {
            setTop(redCapturedPane);
            setBottom(blackCapturedPane);
        }
    }
}
