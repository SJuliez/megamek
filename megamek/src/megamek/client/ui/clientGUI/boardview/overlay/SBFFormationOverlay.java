package megamek.client.ui.clientGUI.boardview.overlay;

import megamek.client.ui.IDisplayable;
import megamek.client.ui.clientGUI.tooltip.SBFInGameObjectTooltip;
import megamek.common.annotations.Nullable;
import megamek.common.strategicBattleSystems.SBFFormation;
import megamek.common.strategicBattleSystems.SBFGame;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class SBFFormationOverlay implements IDisplayable {

    private static final float SCALING = 0.7f;

    private final SBFGame game;
    private final JLabel label = new JLabel();

    private BufferedImage image;

    public SBFFormationOverlay(SBFGame game) {
        this.game = game;
    }

    public void setFormation(@Nullable SBFFormation shownFormation) {
        if (shownFormation == null) {
            image = null;
        } else {
            String tooltip = "<HTML>" + "<HEAD><STYLE>"
                  + SBFInGameObjectTooltip.styles(SCALING)
                  + "</STYLE></HEAD>"
                  + "<BODY style=padding:5;>"
                  + SBFInGameObjectTooltip.getTooltip(shownFormation, game)
                  + "</BODY></HTML>";
            label.setText(tooltip);
            Dimension size = label.getPreferredSize();
            label.setSize(size);
            image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, size.width, size.height);
                label.paint(g);
            } finally {
                g.dispose();
            }
        }
    }

    @Override
    public void draw(Graphics graph, Rectangle rect) {
        if (image != null) {
            graph.drawImage(image,
                  rect.x,
                  rect.y,
                  null);
        }
    }
}
