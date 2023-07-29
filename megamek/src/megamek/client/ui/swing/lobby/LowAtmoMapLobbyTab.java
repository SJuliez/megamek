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

import megamek.client.ui.Messages;
import megamek.client.ui.swing.minimap.Minimap;
import megamek.client.ui.swing.util.UIUtil;
import megamek.codeUtilities.MathUtility;
import megamek.common.Board;
import megamek.common.MapSettings;
import megamek.common.util.BoardUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static megamek.client.ui.swing.util.UIUtil.FixedYPanel;

public class LowAtmoMapLobbyTab {

    private final Box lowAtmoMapLobbyTabPanel = Box.createVerticalBox();
    private final ChatLounge lobby;

    private final JToggleButton lowAtmoMapButton = new JToggleButton("Use a Low Atmospheric Map");
    private final JToggleButton noTerrainButton = new JToggleButton("Use Terrain");
    private final SpaceMapPreviewPanel previewPanel = new SpaceMapPreviewPanel();
    private final JLabel lowAtmoBoardWidthLabel = new JLabel(Messages.getString("ChatLounge.labBoardWidth"));
    private final JTextField lowAtmoBoardWidthField = new JTextField(3);
    private final JLabel lowAtmoBoardHeightLabel = new JLabel(Messages.getString("ChatLounge.labBoardHeight"));
    private final JTextField lowAtmoBoardHeightField = new JTextField(3);
    private JSplitPane splitPanel;

    MapSettings lowAtmoMapSettings = MapSettings.newLowAtmoMap();

    LowAtmoMapLobbyTab(ChatLounge lobby) {
        this.lobby = lobby;
        setupPanel();
        refreshGUI();
        renewMapPreview();
    }

    void setLowAtmoMapSettings(MapSettings newMapSettings) {
        lowAtmoMapSettings = newMapSettings;
        refreshGUI();
        renewMapPreview();
    }

    JComponent getPanel() {
        return lowAtmoMapLobbyTabPanel;
    }

    private void setupPanel() {
        // Top: "Use Low Atmo Map" button
        FixedYPanel useLowAtmoMapPanel = new FixedYPanel();
        useLowAtmoMapPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        useLowAtmoMapPanel.add(lowAtmoMapButton);

        // Right side: Settings
        FixedYPanel panLowAtmoBoardWidth = new FixedYPanel();
        FixedYPanel panLowAtmoBoardHeight = new FixedYPanel();
        panLowAtmoBoardWidth.add(lowAtmoBoardWidthLabel);
        panLowAtmoBoardWidth.add(lowAtmoBoardWidthField);
        panLowAtmoBoardHeight.add(lowAtmoBoardHeightLabel);
        panLowAtmoBoardHeight.add(lowAtmoBoardHeightField);
        noTerrainButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        Box settingsPanel = Box.createVerticalBox();
        settingsPanel.add(noTerrainButton);
        settingsPanel.add(panLowAtmoBoardWidth);
        settingsPanel.add(panLowAtmoBoardHeight);

        previewPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Split Panel; left: map preview; right: settings
        splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, settingsPanel);
        splitPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPanel.setDividerLocation(getDividerLocation());
            }

            @Override
            public void componentShown(ComponentEvent e) {
                splitPanel.setDividerLocation(getDividerLocation());
            }
        });
        splitPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        splitPanel.setDividerLocation(getDividerLocation());

        lowAtmoMapLobbyTabPanel.add(useLowAtmoMapPanel);
        lowAtmoMapLobbyTabPanel.add(splitPanel);
        adaptToGuiScale();
    }

    /**
     * Refreshes the map assembly UI from the current map settings. Does NOT trigger further
     * changes or result in packets to the server.
     */
    private void refreshGUI() {
        removeListeners();
        lowAtmoMapButton.setSelected(lowAtmoMapSettings.isUsed());
        noTerrainButton.setEnabled(lowAtmoMapButton.isSelected());
        previewPanel.setEnabled(lowAtmoMapButton.isSelected());
        lowAtmoBoardWidthLabel.setEnabled(lowAtmoMapButton.isSelected());
        lowAtmoBoardHeightLabel.setEnabled(lowAtmoMapButton.isSelected());
        lowAtmoBoardWidthField.setEnabled(lowAtmoMapButton.isSelected());
        lowAtmoBoardHeightField.setEnabled(lowAtmoMapButton.isSelected());
        lowAtmoBoardWidthField.setText(Integer.toString(lowAtmoMapSettings.getBoardWidth()));
        lowAtmoBoardHeightField.setText(Integer.toString(lowAtmoMapSettings.getBoardHeight()));
        addListeners();
    }

    void removeListeners() {
        lowAtmoMapButton.removeActionListener(toggleLowAtmoMapListener);
        noTerrainButton.removeActionListener(toggleUseTerrainListener);
        lowAtmoBoardWidthField.removeFocusListener(focusListener);
        lowAtmoBoardHeightField.removeFocusListener(focusListener);
    }

    void addListeners() {
        lowAtmoMapButton.addActionListener(toggleLowAtmoMapListener);
        noTerrainButton.addActionListener(toggleUseTerrainListener);
        lowAtmoBoardWidthField.addFocusListener(focusListener);
        lowAtmoBoardHeightField.addFocusListener(focusListener);
    }

    final ActionListener toggleLowAtmoMapListener = e -> toggleLowAtmoMap();

    private void toggleLowAtmoMap() {
        lowAtmoMapSettings.setUsed(lowAtmoMapButton.isSelected());
        sendAndUpdate();
    }

    final ActionListener toggleUseTerrainListener = e -> toggleUseTerrain();

    private void toggleUseTerrain() {
        lowAtmoMapSettings.setUsesTerrain(noTerrainButton.isSelected());
        sendAndUpdate();
    }

    private void renewMapPreview() {
        Board spaceBoard = BoardUtilities.generateRandom(lowAtmoMapSettings);
        spaceBoard.setMapType(lowAtmoMapSettings.getMapType());
        Image image = Minimap.getMinimapImageMaxZoom(spaceBoard);
        previewPanel.setImage(image, "Space Map");
        splitPanel.setDividerLocation(getDividerLocation());
    }

    /**
     * Returns a suitable divider location for the splitpane that contains
     * the available boards list and the map preview. The divider location
     * gives between 30% and 50% of space to the map preview depending
     * on the width of the game map.
     */
    private double getDividerLocation() {
        if (lowAtmoMapSettings == null) {
            return 0.5;
        } else {
            double base = 0.5;
            int width = lowAtmoMapSettings.getBoardWidth();
            int height = lowAtmoMapSettings.getBoardHeight();
            double wAspect = Math.max(1, (double) width / (height + 1));
            return MathUtility.clamp(base + wAspect * 0.05, 0.5, 0.75);
        }
    }

    /** Applies changes to the board and map size when the textfields lose focus. */
    FocusListener focusListener = new FocusAdapter() {

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == lowAtmoBoardWidthField) {
                setBoardWidth();
            } else if (e.getSource() == lowAtmoBoardHeightField) {
                setBoardHeight();
            }
        }
    };

    private void setBoardWidth() {
        try {
            int newBoardWidth = Integer.parseInt(lowAtmoBoardWidthField.getText());
            if (newBoardWidth >= 12 && newBoardWidth <= 200) {
                lowAtmoMapSettings.setBoardSize(newBoardWidth, lowAtmoMapSettings.getBoardHeight());
                sendAndUpdate();
            }
        } catch (NumberFormatException e) {
            // no number, no new board width
        }
    }

    private void setBoardHeight() {
        try {
            int newBoardHeight = Integer.parseInt(lowAtmoBoardHeightField.getText());
            if (newBoardHeight >= 12 && newBoardHeight <= 200) {
                lowAtmoMapSettings.setBoardSize(lowAtmoMapSettings.getBoardWidth(), newBoardHeight);
                sendAndUpdate();
            }
        } catch (NumberFormatException e) {
            // no number, no new board height
        }
    }

    private void sendAndUpdate() {
        lobby.lowAtmoMapUpdate();
        renewMapPreview();
        refreshGUI();
    }

    void adaptToGuiScale() {
        UIUtil.scaleComp(lowAtmoMapButton, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(noTerrainButton, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lowAtmoBoardWidthLabel, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lowAtmoBoardHeightLabel, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lowAtmoBoardWidthField, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lowAtmoBoardHeightField, UIUtil.FONT_SCALE1);
        int border = UIUtil.scaleForGUI(20);
        previewPanel.setBorder(new EmptyBorder(border, border, border, border));
    }
}