package megamek.client.ui.colors;

import megamek.MegaMek;
import megamek.client.ui.buttons.ColourSelectorButton;
import megamek.client.ui.util.VerticalStackLayout;

import javax.swing.*;
import java.awt.*;

import static megamek.client.ui.util.UIUtil.spanCSS;

public class ColorSettingPanel extends JPanel {

    private static final ColorManager manager = MegaMek.getColorManager();

    private final ColorRole role;
    private final ColourSelectorButton previewButton;
    private final JLabel stateLabel;
    private final JButton resetButton;
    private final JLabel exampleLabel;

    private boolean usingDefault;
    private boolean isUpdating = false;

    //    private int width = UIUtil.scaleForGUI(400);

    public ColorSettingPanel(ColorRole role) {
        this.role = role;
        usingDefault = manager.isUsingDefault(role);
        stateLabel = new JLabel(usingDefault ? "Using adaptive default" : "Using custom color");
        previewButton = new ColourSelectorButton(manager.get(role), "");
        previewButton.addPropertyChangeListener("icon", evt -> colorSelected());
        resetButton = new JButton("Use Default");
        resetButton.addActionListener(e -> reset());
        resetButton.setEnabled(!manager.isUsingDefault(role));
        exampleLabel = new JLabel(role.getExampleText());
        exampleLabel.setForeground(manager.get(role));

        String styles = """
              <style>
              .name { font-weight:bold; }
              .description { }
              </style>;
              """;

        String descriptionText = "<HTML><HEAD>" + styles + "</HEAD><BODY><NOBR>"
              + spanCSS("name", role.getDisplayName())
              + " - "
              + spanCSS("description", role.getDescription())
              + "</BODY></HTML>";

        JLabel description = new JLabel(descriptionText);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(previewButton, BorderLayout.CENTER);
        buttonPanel.add(resetButton, BorderLayout.SOUTH);

        JPanel textPanel = new JPanel(new VerticalStackLayout());
        textPanel.add(description);
        textPanel.add(exampleLabel);
        textPanel.add(stateLabel);

        add(buttonPanel);
        add(textPanel);
    }

    public boolean isUsingDefault() {
        return usingDefault;
    }

    public Color selectedColor() {
        return previewButton.getColour();
    }

    public void updateState() {
        setPreviewButtonColor(manager.get(role));
        exampleLabel.setForeground(previewButton.getColour());
        usingDefault = manager.isUsingDefault(role);
        stateLabel.setText(usingDefault ? "Using adaptive default" : "Using custom color");
        resetButton.setEnabled(!usingDefault);
    }

    private void colorSelected() {
        if (!isUpdating) {
            usingDefault = false;
            exampleLabel.setForeground(previewButton.getColour());
            stateLabel.setText("Using custom color");
            resetButton.setEnabled(true);
        }
    }

    private void reset() {
        usingDefault = true;
        stateLabel.setText("Using adaptive default");
        resetButton.setEnabled(false);
        setPreviewButtonColor(manager.getDefault(role));
        exampleLabel.setForeground(previewButton.getColour());
    }

    private void setPreviewButtonColor(Color color) {
        try {
            isUpdating = true;
            previewButton.setColour(color);
        } finally {
            isUpdating = false;
        }
    }
}
