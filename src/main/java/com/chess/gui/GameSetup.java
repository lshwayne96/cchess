package com.chess.gui;

import com.chess.engine.player.Player;
import com.chess.gui.Table.PlayerType;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.awt.Container;
import java.awt.GridLayout;

public class GameSetup extends JDialog {

    private PlayerType redPlayerType;
    private PlayerType blackPlayerType;
    private int searchDepth;
    private JSpinner searchDepthSpinner;

    private static final String HUMAN_TEXT = "Human";
    private static final String COMPUTER_TEXT = "Computer";

    GameSetup(JFrame frame, boolean modal) {
        super(frame, modal);

        //setup default settings
        redPlayerType = PlayerType.HUMAN;
        blackPlayerType = PlayerType.COMPUTER;
        searchDepth = 4;

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JRadioButton redHumanButton = new JRadioButton(HUMAN_TEXT);
        JRadioButton redComputerButton = new JRadioButton(COMPUTER_TEXT);
        JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
        JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
        redHumanButton.setActionCommand(HUMAN_TEXT);

        ButtonGroup redGroup = new ButtonGroup();
        redGroup.add(redHumanButton);
        redGroup.add(redComputerButton);
        redHumanButton.setSelected(true);

        ButtonGroup blackGroup = new ButtonGroup();
        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackComputerButton.setSelected(true);

        getContentPane().add(panel);
        panel.add(new JLabel("Red"));
        panel.add(redHumanButton);
        panel.add(redComputerButton);
        panel.add(new JLabel("Black"));
        panel.add(blackHumanButton);
        panel.add(blackComputerButton);

        panel.add(new JLabel("Search"));
        searchDepthSpinner = addLabeledSpinner(panel, "Search Depth",
                new SpinnerNumberModel(searchDepth, 1, 6, 1));

        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            redPlayerType = redComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            blackPlayerType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            searchDepth = (int) searchDepthSpinner.getValue();


            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            setVisible(false);
        });

        panel.add(cancelButton);
        panel.add(okButton);

        setLocationRelativeTo(frame);
        pack();
        setVisible(false);
    }

    void promptUser() {
        setVisible(true);
        repaint();
    }

    boolean isAIPlayer(Player player) {
        if (player.getAlliance().isRed()) {
            return getRedPlayerType().isComputer();
        } else {
            return getBlackPlayerType().isComputer();
        }
    }

    PlayerType getRedPlayerType() {
        return redPlayerType;
    }

    PlayerType getBlackPlayerType() {
        return blackPlayerType;
    }

    int getSearchDepth() {
        return searchDepth;
    }

    private static JSpinner addLabeledSpinner(Container container, String string, SpinnerModel model) {
        JLabel label = new JLabel(string);
        container.add(label);
        JSpinner spinner = new JSpinner(model);
        label.setLabelFor(spinner);
        container.add(spinner);

        return spinner;
    }
}
