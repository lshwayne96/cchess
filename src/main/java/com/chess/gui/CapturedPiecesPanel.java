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

public class CapturedPiecesPanel extends JPanel {

    private final JPanel redPanel;
    private final JPanel blackPanel;
    private final JPanel statusPanel;

    private static final Dimension CAPTURED_PIECES_DIMENSION = new Dimension(100, 650);
    private static final Dimension STATUS_PANEL_DIMENSION = new Dimension(100, 150);
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
    private static final JLabel CHECK = new JLabel("Check", SwingConstants.CENTER);
    private static final JLabel CHECKMATE = new JLabel("Checkmate", SwingConstants.CENTER);

    public CapturedPiecesPanel() {
        super(new BorderLayout());
        setBorder(PANEL_BORDER);

        redPanel = new JPanel(new GridLayout(8,2));
        redPanel.setBackground(Color.LIGHT_GRAY);
        redPanel.setPreferredSize(new Dimension(100, 250));
        blackPanel = new JPanel(new GridLayout(8,2));
        blackPanel.setBackground(Color.LIGHT_GRAY);
        blackPanel.setPreferredSize(new Dimension(100, 250));
        statusPanel = new JPanel(new GridLayout(2,1));
        statusPanel.setPreferredSize(STATUS_PANEL_DIMENSION);

        add(redPanel, BorderLayout.SOUTH);
        add(blackPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);
        setPreferredSize(CAPTURED_PIECES_DIMENSION);
    }

    public void updateStatus(Player currPlayer) {
        statusPanel.removeAll();

        if (currPlayer.isInCheckmate()) {
            statusPanel.add(new JLabel(currPlayer.getOpponent().getAlliance().toString() + " wins",
                    SwingConstants.CENTER));
            statusPanel.add(CHECKMATE);
            validate();
            return;
        }

        statusPanel.add(new JLabel(currPlayer.getAlliance().toString() + "'s turn", SwingConstants.CENTER));
        if (currPlayer.isInCheck()) {
            statusPanel.add(CHECK);
        }

        validate();
    }

    public void update(MoveLog movelog) {
        redPanel.removeAll();
        blackPanel.removeAll();

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

        Comparator<Piece> comparator = new Comparator<>() {
            @Override
            public int compare(Piece p1, Piece p2) {
                return p1.getType().compareTo(p2.getType());
            }
        };
        Collections.sort(redCapturedPieces, comparator);
        Collections.sort(blackCapturedPieces, comparator);

        for (Piece piece : redCapturedPieces) {
            String name = (piece.getAlliance().toString().substring(0, 1)
                    + piece.getType().toString()).toLowerCase();
            ImageIcon icon = PIECE_ICON_MAP.get(name);
            Image image = icon.getImage().getScaledInstance(icon.getIconWidth() - 30,
                    icon.getIconWidth() - 30, Image.SCALE_SMOOTH);
            redPanel.add(new JLabel(new ImageIcon(image)));
        }
        for (Piece piece : blackCapturedPieces) {
            String name = (piece.getAlliance().toString().substring(0, 1)
                    + piece.getType().toString()).toLowerCase();
            ImageIcon icon = PIECE_ICON_MAP.get(name);
            Image image = icon.getImage().getScaledInstance(icon.getIconWidth() - 30,
                    icon.getIconWidth() - 30, Image.SCALE_SMOOTH);
            blackPanel.add(new JLabel(new ImageIcon(image)));
        }

        validate();
        repaint();
    }

    public void setDirection(BoardDirection direction) {
        removeAll();
        add(statusPanel, BorderLayout.CENTER);

        if (direction == BoardDirection.NORMAL) {
            add(blackPanel, BorderLayout.NORTH);
            add(redPanel, BorderLayout.SOUTH);
        } else {
            add(redPanel, BorderLayout.NORTH);
            add(blackPanel, BorderLayout.SOUTH);
        }

        validate();
        repaint();
    }
}
