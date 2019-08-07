package com.chess.gui;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class GuiUtil {

    static final String GRAPHICS_MISC_PATH = "/graphics/misc/";
    static final String GRAPHICS_PIECES_PATH = "/graphics/pieces/";

    private static final Font HEADER_FONT = Font.font("System", FontWeight.BOLD, Font.getDefault().getSize() + 2);

    static Label getHeader(String header) {
        Label label = new Label(header);
        label.setFont(HEADER_FONT);
        return label;
    }

    static Separator getSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        return separator;
    }
}
