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

import static com.chess.gui.Table.*;

public class GameSetup extends JDialog {

    private PlayerType redPlayerType;
    private PlayerType blackPlayerType;
    private AIType aiType;
    private int searchDepth;
    private int searchTime;
    private JSpinner searchDepthSpinner;
    private JSpinner searchTimeSpinner;

    private static final String HUMAN_TEXT = "Human";
    private static final String AI_TEXT = "AI";
    private static final String FIXED_DEPTH_TEXT = "Fixed depth";
    private static final String FIXED_TIME_TEXT = "Fixed time";

    GameSetup(JFrame frame, boolean modal) {
        super(frame, modal);

        // setup default settings
        redPlayerType = PlayerType.HUMAN;
        blackPlayerType = PlayerType.AI;
        aiType = AIType.TIME;
        searchTime = 10;
        searchDepth = 5;

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JRadioButton redHumanButton = new JRadioButton(HUMAN_TEXT);
        JRadioButton redAIButton = new JRadioButton(AI_TEXT);
        JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
        JRadioButton blackAIButton = new JRadioButton(AI_TEXT);
        JRadioButton fixedDepthAIButton = new JRadioButton(FIXED_DEPTH_TEXT);
        JRadioButton fixedTimeAIButton = new JRadioButton(FIXED_TIME_TEXT);

        ButtonGroup redGroup = new ButtonGroup();
        redGroup.add(redHumanButton);
        redGroup.add(redAIButton);
        redHumanButton.setSelected(true);

        ButtonGroup blackGroup = new ButtonGroup();
        blackGroup.add(blackHumanButton);
        blackGroup.add(blackAIButton);
        blackAIButton.setSelected(true);

        ButtonGroup aiGroup = new ButtonGroup();
        aiGroup.add(fixedDepthAIButton);
        aiGroup.add(fixedTimeAIButton);
        fixedDepthAIButton.setSelected(true);

        getContentPane().add(panel);
        panel.add(new JLabel("Red"));
        panel.add(redHumanButton);
        panel.add(redAIButton);
        panel.add(new JLabel("Black"));
        panel.add(blackHumanButton);
        panel.add(blackAIButton);
        panel.add(new JLabel("AI type"));
        panel.add(fixedDepthAIButton);
        panel.add(fixedTimeAIButton);

        panel.add(new JLabel("AI Settings"));
        searchDepthSpinner = addLabeledSpinner(panel, "Depth limit (levels)",
                new SpinnerNumberModel(searchDepth, 2, 8, 1));
        searchTimeSpinner = addLabeledSpinner(panel, "Time limit (seconds)",
                new SpinnerNumberModel(searchTime, 1, 180, 1));

        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            redPlayerType = redAIButton.isSelected() ? PlayerType.AI : PlayerType.HUMAN;
            blackPlayerType = blackAIButton.isSelected() ? PlayerType.AI : PlayerType.HUMAN;
            aiType = fixedTimeAIButton.isSelected() ? AIType.TIME : AIType.DEPTH;
            searchDepth = (int) searchDepthSpinner.getValue();
            searchTime = (int) searchTimeSpinner.getValue();

            setVisible(false);
        });
        cancelButton.addActionListener(e -> setVisible(false));

        panel.add(cancelButton);
        panel.add(okButton);

        setLocationRelativeTo(frame);
        setLocation(frame.getLocation());
        pack();
        setVisible(false);
    }

    void promptUser() {
        setVisible(true);
        repaint();
    }

    boolean isAIPlayer(Player player) {
        return player.getAlliance().isRed() ? redPlayerType.isAI() : blackPlayerType.isAI();
    }

    boolean isAITimeLimited() {
        return aiType.isTimeLimited();
    }

    int getSearchDepth() {
        return searchDepth;
    }

    int getSearchTime() {
        return searchTime;
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
