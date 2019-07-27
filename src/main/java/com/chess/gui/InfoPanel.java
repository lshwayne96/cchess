package com.chess.gui;

import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.chess.gui.Table.*;

public class InfoPanel extends JPanel {

    private final JPanel redCapturedPanel;
    private final JPanel blackCapturedPanel;
    private final JPanel statusPanel;

    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
    private static final Dimension PANEL_DIMENSION = new Dimension(100, 600);
    private static final Dimension CAPTURED_PANEL_DIMENSION = new Dimension(100, 250);
    private static final Color CAPTURED_PANEL_COLOR = Color.LIGHT_GRAY;
    private static final Dimension STATUS_PANEL_DIMENSION = new Dimension(100, 100);
    private static final JLabel CHECK_LABEL = new JLabel("Check", SwingConstants.CENTER);
    private static final JLabel CHECKMATE_LABEL = new JLabel("Checkmate", SwingConstants.CENTER);

    public InfoPanel() {
        super(new BorderLayout());
        setBorder(PANEL_BORDER);

        redCapturedPanel = new JPanel(new GridLayout(8,2));
        redCapturedPanel.setBackground(CAPTURED_PANEL_COLOR);
        redCapturedPanel.setPreferredSize(CAPTURED_PANEL_DIMENSION);
        blackCapturedPanel = new JPanel(new GridLayout(8,2));
        blackCapturedPanel.setBackground(CAPTURED_PANEL_COLOR);
        blackCapturedPanel.setPreferredSize(CAPTURED_PANEL_DIMENSION);
        statusPanel = new JPanel(new GridLayout(2,1));
        statusPanel.setPreferredSize(STATUS_PANEL_DIMENSION);

        add(redCapturedPanel, BorderLayout.SOUTH);
        add(blackCapturedPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);

        setPreferredSize(PANEL_DIMENSION);
        setMaximumSize(PANEL_DIMENSION);
        setMinimumSize(PANEL_DIMENSION);
    }

    public void updateStatusPanel(Player currPlayer) {
        statusPanel.removeAll();

        if (currPlayer.isInCheckmate()) {
            statusPanel.add(new JLabel(currPlayer.getOpponent().getAlliance().toString() + " wins",
                    SwingConstants.CENTER));
            statusPanel.add(CHECKMATE_LABEL);
            validate();
            return;
        }

        statusPanel.add(new JLabel(currPlayer.getAlliance().toString() + "'s turn", SwingConstants.CENTER));
        if (currPlayer.isInCheck()) {
            statusPanel.add(CHECK_LABEL);
        }

        validate();
    }

    public void updateCapturedPanel(MoveLog movelog) {
        redCapturedPanel.removeAll();
        blackCapturedPanel.removeAll();

        List<Piece> redCapturedPieces = new ArrayList<>();
        List<Piece> blackCapturedPieces = new ArrayList<>();

        for (Move move : movelog.getMoves()) {
            Optional<Piece> capturedPiece = move.getCapturedPiece();
            capturedPiece.ifPresent(p -> {
                if (p.getAlliance().isRed()) {
                    redCapturedPieces.add(p);
                } else {
                    blackCapturedPieces.add(p);
                }
        });
        }

        Comparator<Piece> comparator = Comparator.comparing(Piece::getType);
        Collections.sort(redCapturedPieces, comparator);
        Collections.sort(blackCapturedPieces, comparator);

        for (Piece piece : redCapturedPieces) {
            String name = (piece.getAlliance().toString().substring(0, 1)
                    + piece.getType().toString()).toLowerCase();
            ImageIcon icon = PIECE_ICON_MAP.get(name);
            Image image = icon.getImage().getScaledInstance(icon.getIconWidth()/2,
                    icon.getIconWidth()/2, Image.SCALE_SMOOTH);
            redCapturedPanel.add(new JLabel(new ImageIcon(image)));
        }
        for (Piece piece : blackCapturedPieces) {
            String name = (piece.getAlliance().toString().substring(0, 1)
                    + piece.getType().toString()).toLowerCase();
            ImageIcon icon = PIECE_ICON_MAP.get(name);
            Image image = icon.getImage().getScaledInstance(icon.getIconWidth()/2,
                    icon.getIconWidth()/2, Image.SCALE_SMOOTH);
            blackCapturedPanel.add(new JLabel(new ImageIcon(image)));
        }

        validate();
        repaint();
    }

    public void setDirection(BoardDirection direction) {
        removeAll();
        add(statusPanel, BorderLayout.CENTER);

        if (direction.isNormal()) {
            add(blackCapturedPanel, BorderLayout.NORTH);
            add(redCapturedPanel, BorderLayout.SOUTH);
        } else {
            add(redCapturedPanel, BorderLayout.NORTH);
            add(blackCapturedPanel, BorderLayout.SOUTH);
        }

        validate();
        repaint();
    }
}