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
package megamek.client.ui.swing.dialog;

import megamek.client.ratgenerator.AvailabilityRating;
import megamek.client.ratgenerator.ModelRecord;
import megamek.client.ratgenerator.RATGenerator;
import megamek.client.ui.swing.util.SpringUtilities;
import megamek.client.ui.swing.util.UIUtil;
import megamek.common.ERAS;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AvailabilityPanel {

    private final static RATGenerator RG = RATGenerator.getInstance();
    private static Integer[] RG_ERAS;

    private final JFrame parent;
    private final JPanel panel = new UIUtil.FixedXPanel(new SpringLayout());
    private final Box mainPanel = Box.createVerticalBox();
    private final JScrollPane scrollPane = new JScrollPane(mainPanel);
    private int columns;
    private ModelRecord record;

    public AvailabilityPanel(JFrame parent) {
        this.parent = parent;
        panel.setAlignmentX(0);
        mainPanel.setBorder(new EmptyBorder(20, 25, 0, 0));
        mainPanel.add(new JLabel("Clicking on the factions or eras opens a link to the MUL showing the respective entry."));
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        initializePanel();
    }

    public JComponent getPanel() {
        return scrollPane;
    }

    public void setUnit(String model, String chassis) {
        record = new ModelRecord(chassis, model);
        initializePanel();
    }

    public void reset() {
        record = null;
    }

    private void initializePanel() {
        if (!RG.isInitialized()) {
            return;
        } else if (RG_ERAS == null) {
            RG.getEraSet().forEach(RG::loadYear);
            RG.initRemainingUnits();
            RG_ERAS = RG.getEraSet().toArray(new Integer[0]);
        }
        panel.removeAll();
        columns = 0;
        addHeader("Faction");
        for (ERAS era : ERAS.values()) {
            String link = "<HTML><BODY><DIV ALIGN=CENTER><A HREF = http://www.masterunitlist.info/Era/Details/"
                    + era.getMulLinkId() + ">" + era + "</A></BODY></HTML>";
            addHeader(link);
        }

        if (record != null) {
            final Set<String> currentChassisFactions = new HashSet<>();

            for (Integer era : RG.getEraSet()) {
                Collection<AvailabilityRating> chassisRecs = RG.getChassisFactionRatings(era, record.getChassisKey());
                if (chassisRecs != null) {
                    chassisRecs.forEach(rec -> currentChassisFactions.add(rec.getFactionCode()));
                }
            }

            int row = 1;
            for (String faction : currentChassisFactions) {
                addGridElementLeftAlign(faction, row % 2 == 1);
                for (ERAS era : ERAS.values()) {
                    String text = "--";
                    for (Integer year : RG.getEraSet()) {
                        if (ERAS.getEra(year) != era) {
                            continue;
                        }
                        Collection<AvailabilityRating> chassisRecs = RG.getChassisFactionRatings(year, record.getChassisKey());
                        if (chassisRecs != null) {
                            if (chassisRecs.stream().anyMatch(rec -> faction.equals(rec.getFactionCode()))) {
                                text = "Yes";
                            }
                        }
                    }
                    addGridElement(text, row % 2 == 1);
                }
                row++;
            }

            SpringUtilities.makeCompactGrid(panel, row, columns, 5, 5, 1, 1);
            panel.revalidate();
        }
    }

    private void addHeader(String text, float alignment) {
        columns++;
        var headerPanel = new UIUtil.FixedYPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));
        var textLabel = new JTextPane();
        textLabel.setContentType("text/html");
        textLabel.setEditable(false);
        textLabel.setText(text);
        textLabel.setAlignmentX(alignment);
        textLabel.setFont(panel.getFont().deriveFont(Font.BOLD));
        textLabel.setForeground(UIUtil.uiLightBlue());
        textLabel.setFont(UIUtil.getScaledFont());
        textLabel.addHyperlinkListener(e -> {
            try {
                if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                }
            } catch (Exception ex) {
                LogManager.getLogger().error("", ex);
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        headerPanel.add(textLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(new JSeparator());
        panel.add(headerPanel);
    }

    private void addHeader(String text) {
        addHeader(text, JComponent.CENTER_ALIGNMENT);
    }

    private void addGridElement(String text, boolean coloredBG) {
        var elementPanel = new UIUtil.FixedYPanel();
        if (coloredBG) {
            elementPanel.setBackground(UIUtil.alternateTableBGColor());
        }
        var textLabel = new JLabel(text);
        textLabel.setFont(UIUtil.getScaledFont());
        elementPanel.add(textLabel);
        panel.add(elementPanel);
    }

    private void addGridElementLeftAlign(String text, boolean coloredBG) {
        var elementPanel = new UIUtil.FixedYPanel(new FlowLayout(FlowLayout.LEFT));
        if (coloredBG) {
            elementPanel.setBackground(UIUtil.alternateTableBGColor());
        }
        var textLabel = new JLabel(text);
        textLabel.setFont(UIUtil.getScaledFont());
        elementPanel.add(textLabel);
        panel.add(elementPanel);
    }
}
