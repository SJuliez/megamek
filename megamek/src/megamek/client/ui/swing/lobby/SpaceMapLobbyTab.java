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
import megamek.common.MapType;
import megamek.common.util.BoardUtilities;
import megamek.common.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static megamek.client.ui.swing.util.UIUtil.FixedYPanel;

public class SpaceMapLobbyTab {

    private final Box spaceMapLobbyTabPanel = Box.createVerticalBox();
    private final ChatLounge lobby;

    private final JToggleButton butSpaceMap = new JToggleButton(Messages.getString("ChatLounge.name.spaceMap"));
    private final JToggleButton butHighAtmo = new JToggleButton(Messages.getString("ChatLounge.useHighAtmo"));
    private final SpaceMapPreviewPanel previewPanel = new SpaceMapPreviewPanel();
    private final JLabel lblSpaceBoardWidth = new JLabel(Messages.getString("ChatLounge.labBoardWidth"));
    private final JTextField fldSpaceBoardWidth = new JTextField(3);
    private final JLabel lblSpaceBoardHeight = new JLabel(Messages.getString("ChatLounge.labBoardHeight"));
    private final JTextField fldSpaceBoardHeight = new JTextField(3);
    private JSplitPane splitPanel;

    MapSettings spaceMapSettings = MapSettings.newSpaceMap();

    SpaceMapLobbyTab(ChatLounge lobby) {
        this.lobby = lobby;
        spaceMapSettings.setMedium(MapSettings.MEDIUM_SPACE);
        spaceMapSettings.setMapType(MapType.SPACE);
        setupPanel();
        refreshGUI();
        renewMapPreview();
    }

    void setSpaceMapSettings(MapSettings newMapSettings) {
        spaceMapSettings = newMapSettings;
        refreshGUI();
        renewMapPreview();
    }

    JComponent getPanel() {
        return spaceMapLobbyTabPanel;
    }

    private void setupPanel() {
        // Top: "Use Space Map" button
        FixedYPanel useSpaceMapPanel = new FixedYPanel();
        useSpaceMapPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        useSpaceMapPanel.add(butSpaceMap);

        // Right side: Settings
        UIUtil.FixedYPanel panSpaceBoardWidth = new UIUtil.FixedYPanel();
        UIUtil.FixedYPanel panSpaceBoardHeight = new UIUtil.FixedYPanel();
        panSpaceBoardWidth.add(lblSpaceBoardWidth);
        panSpaceBoardWidth.add(fldSpaceBoardWidth);
        panSpaceBoardHeight.add(lblSpaceBoardHeight);
        panSpaceBoardHeight.add(fldSpaceBoardHeight);
        butHighAtmo.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        Box settingsPanel = Box.createVerticalBox();
        settingsPanel.add(butHighAtmo);
        settingsPanel.add(panSpaceBoardWidth);
        settingsPanel.add(panSpaceBoardHeight);

        previewPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Split Panel; left: space map preview; right: settings
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

        spaceMapLobbyTabPanel.add(useSpaceMapPanel);
        spaceMapLobbyTabPanel.add(splitPanel);
        adaptToGuiScale();
    }

    /**
     * Refreshes the map assembly UI from the current map settings. Does NOT trigger further
     * changes or result in packets to the server.
     */
    private void refreshGUI() {
        removeListeners();
        butSpaceMap.setSelected(spaceMapSettings.isUsed());
        splitPanel.setVisible(spaceMapSettings.isUsed());
        butHighAtmo.setEnabled(butSpaceMap.isSelected());
        previewPanel.setEnabled(butSpaceMap.isSelected());
        lblSpaceBoardWidth.setEnabled(butSpaceMap.isSelected());
        lblSpaceBoardHeight.setEnabled(butSpaceMap.isSelected());
        fldSpaceBoardWidth.setEnabled(butSpaceMap.isSelected());
        fldSpaceBoardHeight.setEnabled(butSpaceMap.isSelected());
        fldSpaceBoardWidth.setText(Integer.toString(spaceMapSettings.getBoardWidth()));
        fldSpaceBoardHeight.setText(Integer.toString(spaceMapSettings.getBoardHeight()));
        addListeners();
    }

    void removeListeners() {
        butSpaceMap.removeActionListener(toggleSpaceMapListener);
        butHighAtmo.removeActionListener(toggleHighAtmoMapListener);
        fldSpaceBoardWidth.removeFocusListener(focusListener);
        fldSpaceBoardHeight.removeFocusListener(focusListener);
    }

    void addListeners() {
        butSpaceMap.addActionListener(toggleSpaceMapListener);
        butHighAtmo.addActionListener(toggleHighAtmoMapListener);
        fldSpaceBoardWidth.addFocusListener(focusListener);
        fldSpaceBoardHeight.addFocusListener(focusListener);
    }

    final ActionListener toggleSpaceMapListener = e -> toggleSpaceMap();

    private void toggleSpaceMap() {
        spaceMapSettings.setUsed(butSpaceMap.isSelected());
        sendAndUpdate();
    }

    final ActionListener toggleHighAtmoMapListener = e -> toggleHighAtmoMap();

    private void toggleHighAtmoMap() {
        spaceMapSettings.setMapType(MapType.SPACE);
        spaceMapSettings.setSpaceUsesGravity(butHighAtmo.isSelected());
        sendAndUpdate();
    }


    private void renewMapPreview() {
        Board spaceBoard = BoardUtilities.generateRandom(spaceMapSettings);
        spaceBoard.setMapType(spaceMapSettings.getMapType());
        Image image = Minimap.getMinimapImageMaxZoom(spaceBoard);
        previewPanel.setImage(ImageUtil.rotate270(image), "Space Map");
        splitPanel.setDividerLocation(getDividerLocation());
    }

    /**
     * Returns a suitable divider location for the splitpane that contains
     * the available boards list and the map preview. The divider location
     * gives between 30% and 50% of space to the map preview depending
     * on the width of the game map.
     */
    private double getDividerLocation() {
        if (spaceMapSettings == null) {
            return 0.5;
        } else {
            double base = 0.5;
            int width = spaceMapSettings.getBoardWidth();
            int height = spaceMapSettings.getBoardHeight();
            double wAspect = Math.max(1, (double) width / (height + 1));
            return MathUtility.clamp(base + wAspect * 0.05, 0.5, 0.75);
        }
    }

    /** Applies changes to the board and map size when the textfields lose focus. */
    FocusListener focusListener = new FocusAdapter() {

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == fldSpaceBoardWidth) {
                setBoardWidth();
            } else if (e.getSource() == fldSpaceBoardHeight) {
                setBoardHeight();
            }
        }
    };

    private void setBoardWidth() {
        try {
            int newBoardWidth = Integer.parseInt(fldSpaceBoardWidth.getText());
            if (newBoardWidth >= 12 && newBoardWidth <= 200) {
                spaceMapSettings.setBoardSize(newBoardWidth, spaceMapSettings.getBoardHeight());
                sendAndUpdate();
            }
        } catch (NumberFormatException e) {
            // no number, no new board width
        }
    }

    private void setBoardHeight() {
        try {
            int newBoardHeight = Integer.parseInt(fldSpaceBoardHeight.getText());
            if (newBoardHeight >= 12 && newBoardHeight <= 200) {
                spaceMapSettings.setBoardSize(spaceMapSettings.getBoardWidth(), newBoardHeight);
                sendAndUpdate();
            }
        } catch (NumberFormatException e) {
            // no number, no new board height
        }
    }

    private void sendAndUpdate() {
        lobby.spaceMapUpdate();
        renewMapPreview();
        refreshGUI();
    }

    void adaptToGuiScale() {
        UIUtil.scaleComp(butSpaceMap, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(butHighAtmo, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lblSpaceBoardWidth, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(lblSpaceBoardHeight, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(fldSpaceBoardWidth, UIUtil.FONT_SCALE1);
        UIUtil.scaleComp(fldSpaceBoardHeight, UIUtil.FONT_SCALE1);
        int border = UIUtil.scaleForGUI(20);
        previewPanel.setBorder(new EmptyBorder(border, border, border, border));
    }
}