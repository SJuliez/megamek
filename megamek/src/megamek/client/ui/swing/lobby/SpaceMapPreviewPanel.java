/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.ui.swing.lobby;

import javax.swing.*;
import java.awt.*;

public class SpaceMapPreviewPanel extends JPanel {

    private int lastPanelWidth = 0;
    private int lastPanelHeight = 0;
    private double lastScaleFactor = 0;
    private Image scaledImage;
    private Image baseImage;

    SpaceMapPreviewPanel() {
        setLayout(new GridLayout(1, 1));
    }

    public void setImage(Image minimapImage, String name) {
        // For now, rotate the incoming image, see alignment of the high atmo map in TW, Aero Movement
        baseImage = minimapImage;
        scaledImage = null;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (baseImage != null) {
            int panelWidth = getWidth() - getInsets().left - getInsets().right;
            int panelHeight = getHeight() - getInsets().top - getInsets().bottom;
            if ((panelWidth != lastPanelWidth) || (panelHeight != lastPanelHeight) || (scaledImage == null)) {
                // Scale to the maximum size keeping aspect ratio
                double factorX = (double) panelWidth / baseImage.getWidth(null);
                double factorY = (double) panelHeight / baseImage.getHeight(null);
                double factor = Math.min(factorX, factorY);
                if ((lastScaleFactor != factor) || (scaledImage == null)) {
                    lastPanelWidth = (int) (factor * baseImage.getWidth(null));
                    lastPanelHeight = (int) (factor * baseImage.getHeight(null));
                    scaledImage = baseImage.getScaledInstance(lastPanelWidth, lastPanelHeight, Image.SCALE_SMOOTH);
                    lastScaleFactor = factor;
                }
            }
            int x = getWidth() / 2 - scaledImage.getWidth(null) / 2;
            int y = getHeight() / 2 - scaledImage.getHeight(null) / 2;
            g.drawImage(scaledImage, x, y, null);
        }
    }
}