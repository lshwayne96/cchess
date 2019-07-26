package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Coordinate;
import com.chess.engine.board.Move;
import com.chess.engine.board.Point;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.chess.engine.pieces.Piece.*;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table {

    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private final GameHistoryPanel historyPanel;
    private final CapturedPiecesPanel capturedPiecesPanel;
    private final MoveLog movelog;
    private final List<Board> boardHistory;
    private Board board;
    private BoardDirection boardDirection;

    private Point sourcePoint;
    private Point destPoint;
    private Piece humanMovedPiece;

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(785, 685);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(585, 650);
    private static final Dimension POINT_PANEL_DIMENSION = new Dimension(65, 65);

    private static final String GRAPHICS_MISC_PATH = "/graphics/misc/";
    private static final String GRAPHICS_PIECES_PATH = "/graphics/pieces/";
    private static final ImageIcon HIGHLIGHT_ICON = getHighlightIcon();
    private static final Map<Coordinate, ImageIcon> POINT_ICON_MAP = getPointIconMap();
    public static final Map<String, ImageIcon> PIECE_ICON_MAP = getPieceIconMap();

    public Table() {
        gameFrame = new JFrame("CChess");
        board = Board.initialiseBoard();
        boardDirection = BoardDirection.NORMAL;
        boardPanel = new BoardPanel();
        historyPanel = new GameHistoryPanel();
        capturedPiecesPanel = new CapturedPiecesPanel();
        capturedPiecesPanel.updateStatus(board.getCurrPlayer());
        movelog = new MoveLog();
        boardHistory = new ArrayList<>();
        boardHistory.add(board);
        JMenuBar tableMenuBar = createMenuBar();

        gameFrame.setJMenuBar(tableMenuBar);
        gameFrame.setSize(OUTER_FRAME_DIMENSION);
        gameFrame.setLayout(new BorderLayout());
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameFrame.add(boardPanel, BorderLayout.CENTER);
        gameFrame.add(historyPanel, BorderLayout.EAST);
        gameFrame.add(capturedPiecesPanel, BorderLayout.WEST);

        gameFrame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar tableMenuBar = new JMenuBar();

        tableMenuBar.add(createGameMenu());
        tableMenuBar.add(createOptionsMenu());

        return tableMenuBar;
    }

    private JMenu createGameMenu() {
        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGame = new JMenuItem("New game");
        newGame.addActionListener(e -> undoAllMoves());
        gameMenu.add(newGame);
        gameMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(exit);

        return gameMenu;
    }

    private JMenu createOptionsMenu() {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem undoMove = new JMenuItem("Undo last move");
        undoMove.addActionListener(e -> undoLastMove());
        optionsMenu.add(undoMove);
        optionsMenu.addSeparator();

        JMenuItem flipMenuItem = new JMenuItem("Flip board");
        flipMenuItem.addActionListener(e -> flipBoard());
        optionsMenu.add(flipMenuItem);

        return optionsMenu;
    }

    private void undoLastMove() {
        if (boardHistory.size() > 1) {
            movelog.removeLastMove();
            boardHistory.remove(boardHistory.size() - 1);
            board = boardHistory.get(boardHistory.size() - 1);

            historyPanel.update(movelog);
            capturedPiecesPanel.update(movelog);
            capturedPiecesPanel.updateStatus(board.getCurrPlayer());
            boardPanel.drawBoard(board);
        }
    }

    private void undoAllMoves() {
        while (boardHistory.size() > 1) {
            undoLastMove();
        }
    }

    private void flipBoard() {
        boardDirection = boardDirection.opposite();
        boardPanel.drawBoard(board);
        capturedPiecesPanel.setDirection(boardDirection);
    }

    private static ImageIcon getHighlightIcon() {
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "green_dot.png"));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<Coordinate, ImageIcon> getPointIconMap() {
        ImageIcon dl = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_dl.png"));
            dl = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon dlr = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_dlr.png"));
            dlr = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon dr = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_dr.png"));
            dr = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon udl = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_udl.png"));
            udl = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon udlr = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_udlr.png"));
            udlr = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon udr = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_udr.png"));
            udr = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon ul = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_ul.png"));
            ul = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon ulr = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_ulr.png"));
            ulr = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageIcon ur = null;
        try {
            BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_MISC_PATH + "point_ur.png"));
            ur = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Coordinate, ImageIcon> pointIconMap = new HashMap<>();
        for (int row = 0; row < Board.NUM_ROWS; row++) {
            for (int col = 0; col < Board.NUM_COLS; col++) {
                Coordinate position = new Coordinate(row, col);
                ImageIcon icon;

                if ((row == 0 || row == Board.RIVER_ROW_RED) && col == 0) {
                    icon = dr;
                } else if ((row == 0 || row == Board.RIVER_ROW_RED) && (col > 0 && col < Board.NUM_COLS - 1)) {
                    icon = dlr;
                } else if ((row == 0 || row == Board.RIVER_ROW_RED) && col == Board.NUM_COLS - 1) {
                    icon = dl;
                } else if (((row > 0 && row < Board.RIVER_ROW_BLACK)
                        || (row > Board.RIVER_ROW_RED && row < Board.NUM_ROWS - 1)) && col == 0) {
                    icon = udr;
                } else if (((row > 0 && row < Board.RIVER_ROW_BLACK)
                        || (row > Board.RIVER_ROW_RED && row < Board.NUM_ROWS - 1)) && col == Board.NUM_COLS - 1) {
                    icon = udl;
                } else if ((row == Board.RIVER_ROW_BLACK || row == Board.NUM_ROWS - 1) && col == 0) {
                    icon = ur;
                } else if ((row == Board.RIVER_ROW_BLACK || row == Board.NUM_ROWS - 1)
                        && (col > 0 && col < Board.NUM_COLS - 1)) {
                    icon = ulr;
                } else if ((row == Board.RIVER_ROW_BLACK || row == Board.NUM_ROWS - 1) && col == Board.NUM_COLS - 1) {
                    icon = ul;
                } else {
                    icon = udlr;
                }
                pointIconMap.put(position, icon);
            }
        }

        return pointIconMap;
    }

    private static Map<String, ImageIcon> getPieceIconMap() {
        Map<String, ImageIcon> stringToIconMap = new HashMap<>();

        for (PieceType type : PieceType.values()) {
            try {
                String name = ("R" + type.toString()).toLowerCase();
                BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_PIECES_PATH + name + ".png"));
                stringToIconMap.put(name, new ImageIcon(image));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String name = ("B" + type.toString()).toLowerCase();
                BufferedImage image = ImageIO.read(Table.class.getResource(GRAPHICS_PIECES_PATH + name + ".png"));
                stringToIconMap.put(name, new ImageIcon(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return stringToIconMap;
    }

    private static Color getAllianceColor(Alliance alliance) {
        return alliance.isRed() ? Color.RED : Color.BLACK;
    }

    private static Coordinate getAbsolutePosition(Coordinate relative, BoardDirection direction) {
        if (direction.isNormal()) {
            return relative;
        }
        return new Coordinate(Board.NUM_ROWS - relative.getRow() - 1, Board.NUM_COLS - relative.getCol() - 1);
    }


    public static class MoveLog {

        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return moves;
        }

        public void addMove(Move move) {
            moves.add(move);
        }

        public void removeLastMove() {
            if (!moves.isEmpty()) {
                moves.remove(moves.size() - 1);
            }
        }

        public Move getLastMove() {
            if (!moves.isEmpty()) {
                return moves.get(moves.size() - 1);
            }

            return null;
        }

        public void clear() {
            moves.clear();
        }
    }

    public enum BoardDirection {
        NORMAL {
            @Override
            boolean isNormal() {
                return true;
            }

            @Override
            List<PointPanel> traverse(List<PointPanel> pointPanels) {
                return pointPanels;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            boolean isNormal() {
                return false;
            }

            @Override
            List<PointPanel> traverse(List<PointPanel> pointPanels) {
                List<PointPanel> copy = new ArrayList<>(pointPanels);
                Collections.reverse(copy);
                return copy;
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract boolean isNormal();

        abstract List<PointPanel> traverse(List<PointPanel> pointPanels);

        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel {

        final List<PointPanel> pointPanels;

        BoardPanel() {
            super(new GridLayout(Board.NUM_ROWS, Board.NUM_COLS));
            pointPanels = new ArrayList<>();

            for (int row = 0; row < Board.NUM_ROWS; row++) {
                for (int col = 0; col < Board.NUM_COLS; col++) {
                    PointPanel pointPanel
                            = new PointPanel(this, new Coordinate(row, col));
                    pointPanels.add(pointPanel);
                    this.add(pointPanel);
                }
            }

            setPreferredSize(BOARD_PANEL_DIMENSION);
            setMinimumSize(BOARD_PANEL_DIMENSION);
            setMaximumSize(BOARD_PANEL_DIMENSION);
            setBackground(Color.decode("0xa77d46"));
            validate();
        }

        private void drawBoard(Board board) {
            removeAll();

            for (PointPanel pointPanel : boardDirection.traverse(pointPanels)) {
                pointPanel.drawPoint(board);
                add(pointPanel);
            }

            validate();
            repaint();
        }
    }

    private class PointPanel extends JLayeredPane {

        private final Coordinate position;

        PointPanel(BoardPanel boardPanel, Coordinate position) {
            super();
            this.position = position;

            setOpaque(false);
            setPreferredSize(POINT_PANEL_DIMENSION);
            setMinimumSize(POINT_PANEL_DIMENSION);
            setMaximumSize(POINT_PANEL_DIMENSION);
            assignPointPieceIcon(board);
            drawTile();

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isRightMouseButton(e)) {
                        clearSelections();
                    } else if (isLeftMouseButton(e)) {
                        if (sourcePoint == null) {
                            sourcePoint = board.getPoint(position);
                            Optional<Piece> selectedPiece = sourcePoint.getPiece();
                            if (selectedPiece.isPresent()
                                    && selectedPiece.get().getAlliance() == board.getCurrPlayer().getAlliance()) {
                                humanMovedPiece = selectedPiece.get();
                            } else {
                                sourcePoint = null;
                            }
                        } else {
                            destPoint = board.getPoint(position);
                            Optional<Move> move = Move.getMove(board, sourcePoint.getPosition(),
                                    destPoint.getPosition());
                            if (!move.isPresent()) return;

                            MoveTransition transition = board.getCurrPlayer().makeMove(move.get());
                            if (transition.getMoveStatus().isDone()) {
                                board = transition.getNextBoard();
                                boardHistory.add(board);
                                movelog.addMove(transition.getMove());

                                clearSelections();
                                SwingUtilities.invokeLater(() -> {
                                    historyPanel.update(movelog);
                                    capturedPiecesPanel.update(movelog);
                                    capturedPiecesPanel.updateStatus(board.getCurrPlayer());
                                });
                            }
                        }
                    }

                    SwingUtilities.invokeLater(() -> boardPanel.drawBoard(board));
                }

                private void clearSelections() {
                    sourcePoint = null;
                    destPoint = null;
                    humanMovedPiece = null;
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            validate();
        }

        private void drawPoint(Board board) {
            assignPointPieceIcon(board);
            drawTile();
            highlightLastMoveAndSelectedPiece();
            highlightPossibleMoves(board);

            validate();
            repaint();
        }

        private void drawTile() {
            JLabel label = new JLabel(POINT_ICON_MAP.get(getAbsolutePosition(position, boardDirection)));
            add(label, DEFAULT_LAYER);
            label.setBounds(0, 0, label.getIcon().getIconWidth(), label.getIcon().getIconHeight());
        }

        private void assignPointPieceIcon(Board board) {
            removeAll();

            Point point = board.getPoint(position);
            Optional<Piece> destPiece = point.getPiece();
            destPiece.ifPresent(p -> {
                String name = (p.getAlliance().toString().substring(0, 1) + p.getType().toString()).toLowerCase();
                ImageIcon icon = PIECE_ICON_MAP.get(name);
                JLabel label = new JLabel(icon);
                add(label, PALETTE_LAYER);
                label.setBounds(1, 1, icon.getIconWidth(), icon.getIconHeight());
            });
        }

        private void highlightLastMoveAndSelectedPiece() {
            Move lastMove = movelog.getLastMove();

            if (lastMove != null) {
                Piece lastMovedPiece = lastMove.getMovedPiece();
                Color color = getAllianceColor(lastMovedPiece.getAlliance());
                if (lastMovedPiece.getPosition().equals(position)) {
                    setBorder(BorderFactory.createDashedBorder(color, 2, 2, 2, true));
                    return;
                }
                if (lastMove.getDestPosition().equals(position)) {
                    setBorder(BorderFactory.createDashedBorder(color, 2, 2, 2, true));
                    return;
                }
            }

            if (humanMovedPiece != null && humanMovedPiece.getPosition().equals(position)) {
                Color color = getAllianceColor(humanMovedPiece.getAlliance());
                setBorder(BorderFactory.createLineBorder(color, 2, true));
                return;
            }

            setBorder(null);
        }

        private void highlightPossibleMoves(Board board) {
            for (Move move : pieceLegalMoves(board)) {
                // check for suicidal move
                MoveTransition transition = board.getCurrPlayer().makeMove(move);
                if (!transition.getMoveStatus().isDone()) {
                    continue;
                }

                // legal AND non-suicidal move
                if (move.getDestPosition().equals(position)) {
                    JLabel label = new JLabel(HIGHLIGHT_ICON);
                    add(label, MODAL_LAYER);
                    label.setBounds(24, 24, HIGHLIGHT_ICON.getIconWidth(), HIGHLIGHT_ICON.getIconHeight());
                }
            }
        }

        private Collection<Move> pieceLegalMoves(Board board) {
            if (humanMovedPiece != null) {
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }
    }
}

//TODO: read/save game, AI