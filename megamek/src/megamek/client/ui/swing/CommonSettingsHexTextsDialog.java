/*  
* MegaMek - Copyright (C) 2020 - The MegaMek Team  
*  
* This program is free software; you can redistribute it and/or modify it under  
* the terms of the GNU General Public License as published by the Free Software  
* Foundation; either version 2 of the License, or (at your option) any later  
* version.  
*  
* This program is distributed in the hope that it will be useful, but WITHOUT  
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS  
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more  
* details.  
*/

package megamek.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;

import megamek.common.Terrains;
import megamek.client.ui.IBoardView;
import megamek.client.ui.Messages;
import megamek.client.ui.swing.boardview.BoardView1;

/**
 * CommonSettingsHexTextsDialog.java Created on April 8, 2016. Configuration
 * dialog for settings concerning the display of terrain info texts such as 
 * "Light Woods" in the board hexes. 
 *
 * @author Simon
 */
public class CommonSettingsHexTextsDialog extends ClientDialog implements ActionListener, KeyListener {
    
    private static final long serialVersionUID = -4227870501769580587L;
    
    private class TerrainLevelPair {
        public TerrainLevelPair(int i, int j) {
            terrain = i;
            level = j;
        }
        private int terrain;
        private int level;
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TerrainLevelPair other = (TerrainLevelPair) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (level != other.level)
                return false;
            if (terrain != other.terrain)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 117;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + level;
            result = prime * result + terrain;
            return result;
        }

        private CommonSettingsHexTextsDialog getEnclosingInstance() {
            return CommonSettingsHexTextsDialog.this;
        }
    }
    
    ClientGUI clientgui;
    IBoardView boardView;
    boolean inMapEditor;
    GUIPreferences guip = GUIPreferences.getInstance();
    private HexTextSettings currentSetting;
    
    private static final String[] FONTS = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
    
    // OK / Cancel / Preview
    private JPanel panButtons = new JPanel();
    private JButton butOkay = new JButton(Messages.getString("Okay")); //$NON-NLS-1$
    private JButton butCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
    private JButton butPreset = new JButton("Load Preset"); //$NON-NLS-1$
    private JButton butPreview = new JButton("Preview"); //$NON-NLS-1$

    // Default Text Features
    private JPanel panStandard = new JPanel();
    private JLabel defaultLabel = new JLabel("General Features: "); //$NON-NLS-1$
    private JComboBox<String> fontSelector = new JComboBox<>();
    private JButton butBigger = new JButton("+"); //$NON-NLS-1$
    private JButton butSmaller = new JButton("-"); //$NON-NLS-1$
    private JLabel settingsLabel = new JLabel("--- In-game Settings ---"); //$NON-NLS-1$

    /** The font size for all texts. */
    private int fontSize = 12;
    
    // Other Text Features
    private JPanel panCenter;
    
    private final ArrayList<TerrainLevelPair> settingsList = new ArrayList<TerrainLevelPair>(Arrays.asList( 
            new TerrainLevelPair(HexTextSettings.HT_SEPARATOR, -1), 
            new TerrainLevelPair(HexTextSettings.HT_LEVEL, -1),
            new TerrainLevelPair(Terrains.WATER, -1), 
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.WOODS, 1), 
            new TerrainLevelPair(Terrains.WOODS, 2), 
            new TerrainLevelPair(Terrains.WOODS, 3), 
            new TerrainLevelPair(Terrains.JUNGLE, 1), 
            new TerrainLevelPair(Terrains.JUNGLE, 2), 
            new TerrainLevelPair(Terrains.JUNGLE, 3), 
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.SWAMP, 1),
            new TerrainLevelPair(Terrains.SWAMP, 3),
            new TerrainLevelPair(Terrains.ROUGH, 1),
            new TerrainLevelPair(Terrains.ROUGH, 2), 
            new TerrainLevelPair(Terrains.RUBBLE, -1),
            new TerrainLevelPair(Terrains.RUBBLE, 6), 
            new TerrainLevelPair(Terrains.MUD, -1), 
            new TerrainLevelPair(Terrains.SAND, -1), 
            new TerrainLevelPair(Terrains.TUNDRA, -1), 
            new TerrainLevelPair(Terrains.MAGMA, 1), 
            new TerrainLevelPair(Terrains.MAGMA, 2),
            new TerrainLevelPair(Terrains.FIELDS, -1), 
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.BLDG_ELEV, -1), 
            new TerrainLevelPair(Terrains.BLDG_CF, -1), 
            new TerrainLevelPair(Terrains.BUILDING, 1),
            new TerrainLevelPair(Terrains.BUILDING, 2),
            new TerrainLevelPair(Terrains.BUILDING, 3),
            new TerrainLevelPair(Terrains.BUILDING, 4),
            new TerrainLevelPair(Terrains.BUILDING, 5),
            new TerrainLevelPair(Terrains.BLDG_CLASS, 1),
            new TerrainLevelPair(Terrains.BLDG_CLASS, 2),
            new TerrainLevelPair(Terrains.BLDG_CLASS, 3),
            new TerrainLevelPair(Terrains.BLDG_ARMOR, -1),
            new TerrainLevelPair(Terrains.BLDG_BASEMENT_TYPE, -1),
            new TerrainLevelPair(Terrains.BLDG_BASE_COLLAPSED, 1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.BRIDGE_ELEV, -1),
            new TerrainLevelPair(Terrains.BRIDGE_CF, -1),
            new TerrainLevelPair(Terrains.BRIDGE, -1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.FUEL_TANK_ELEV, -1),
            new TerrainLevelPair(Terrains.FUEL_TANK_CF, -1),
            new TerrainLevelPair(Terrains.FUEL_TANK, -1),
            new TerrainLevelPair(Terrains.FUEL_TANK_MAGN, -1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.PAVEMENT, -1),
            new TerrainLevelPair(Terrains.ROAD, -1),
            new TerrainLevelPair(Terrains.INDUSTRIAL, -1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.IMPASSABLE, -1),
            new TerrainLevelPair(Terrains.ELEVATOR, -1),
            new TerrainLevelPair(Terrains.FORTIFIED, -1),
            new TerrainLevelPair(Terrains.SCREEN, -1),
            new TerrainLevelPair(Terrains.FLUFF, -1),
            new TerrainLevelPair(Terrains.ARMS, -1),
            new TerrainLevelPair(Terrains.LEGS, -1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(Terrains.RAPIDS, 1),
            new TerrainLevelPair(Terrains.RAPIDS, 2),
            new TerrainLevelPair(Terrains.ICE, -1),
            new TerrainLevelPair(Terrains.SNOW, 1),
            new TerrainLevelPair(Terrains.SNOW, 2),
            new TerrainLevelPair(Terrains.FIRE, -1),
            new TerrainLevelPair(Terrains.SMOKE, 1),
            new TerrainLevelPair(Terrains.SMOKE, 2), 
            new TerrainLevelPair(Terrains.GEYSER, 1),
            new TerrainLevelPair(Terrains.GEYSER, 2),
            new TerrainLevelPair(Terrains.GEYSER, 3),
            new TerrainLevelPair(Terrains.METAL_CONTENT, -1),
            new TerrainLevelPair(0, -1),
            new TerrainLevelPair(HexTextSettings.HT_OTHERS, -1)
             ));
    
    private final ArrayList<Integer> terrainWithLevel = new ArrayList<Integer>(Arrays.asList( 
            HexTextSettings.HT_LEVEL,  Terrains.WATER, Terrains.BLDG_ELEV, Terrains.BLDG_CF, 
            Terrains.BLDG_ARMOR, Terrains.BLDG_BASEMENT_TYPE, Terrains.BLDG_BASE_COLLAPSED,
            Terrains.BRIDGE_ELEV, Terrains.BRIDGE_CF, Terrains.FUEL_TANK_ELEV, 
            Terrains.FUEL_TANK_CF, Terrains.FUEL_TANK_MAGN, Terrains.ELEVATOR 
            ));

    private List<JPanel> exampleBoxes = new ArrayList<>();
    private HashMap<TerrainLevelPair, JButton> colorButtons = new HashMap<>();
    private HashMap<TerrainLevelPair, JButton> shadowButtons = new HashMap<>();
    private HashMap<TerrainLevelPair, JTextField> textFields = new HashMap<>();
    private HashMap<TerrainLevelPair, JCheckBox> terrainChecks = new HashMap<>();
    private HashMap<TerrainLevelPair, JToggleButton> boldToggles = new HashMap<>();
    
    
    // --------------------------------------------------------------------------
    
    /** A dialog for confiuration of the texts that are written on hexes
     * in the boardview, like "LEVEL" or "Swamp". Called from View->Client
     * Settings->Graphics Tab (CommonSettingsDialog). */
    public CommonSettingsHexTextsDialog(JFrame owner, ClientGUI cg, IBoardView bv) {

        super(owner, "Configure Hex Texts ... ", true); //$NON-NLS-1$
        updateClient(cg, bv);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        // Lower panel: OK / Cancel / Preview buttons
        butOkay.addActionListener(this);
        butCancel.addActionListener(this);
        panButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        panButtons.add(butOkay);
        panButtons.add(butCancel);
        panButtons.add(butPreview);
        panButtons.add(butPreset);
        content.add(panButtons, BorderLayout.SOUTH);

        // Upper panel: Overall settings
        for (String f: FONTS) fontSelector.addItem(f);
        fontSelector.addActionListener(this);
        butBigger.addActionListener(this);
        butSmaller.addActionListener(this);
        butPreview.addActionListener(this);
        butPreset.addActionListener(this);
        panStandard.setLayout(new FlowLayout(FlowLayout.LEFT));
        panStandard.add(settingsLabel);
        panStandard.add(defaultLabel);
        panStandard.add(fontSelector);
        panStandard.add(butBigger);
        panStandard.add(butSmaller);
        content.add(panStandard, BorderLayout.NORTH);
        
        // Center panel: individual terrains
        panCenter = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagCon = new GridBagConstraints();
        panCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridBagCon.gridy = 0;
        gridBagCon.fill = GridBagConstraints.VERTICAL;
        for (TerrainLevelPair t: settingsList) {
            // for each terrain, add...
            
            // Spacer between groups of terrains
            if (t.terrain == 0) {
                gridBagCon.gridwidth = GridBagConstraints.REMAINDER;
                gridBagCon.gridx = 0;
                gridBagCon.ipady = 3;
                JSeparator js = new JSeparator();
                js.setBackground(Color.DARK_GRAY);
                js.setPreferredSize(new Dimension(1, 5));
                panCenter.add(js, gridBagCon);
                gridBagCon.gridy++;
                continue;
            }
            
            // Checkbox: Text for this terrain?
            gridBagCon.gridwidth = 1;
            gridBagCon.gridx = 0;
            gridBagCon.ipady = 0;
            JCheckBox cbTerrain = new JCheckBox();
            cbTerrain.addActionListener(this);
            panCenter.add(cbTerrain, gridBagCon);
            terrainChecks.put(t, cbTerrain);
            
            // A label with the terrain type 
            gridBagCon.gridx++;
            gridBagCon.anchor = GridBagConstraints.LINE_START;
            String tLabel;
            switch (t.terrain) {
            case HexTextSettings.HT_SEPARATOR:
            case HexTextSettings.HT_LEVEL:
            case HexTextSettings.HT_OTHERS:
            case HexTextSettings.HT_LIGHT:
            case HexTextSettings.HT_HEAVY:
            case HexTextSettings.HT_ULTRA:
                tLabel = getSpecialLabel(t.terrain);
                break;
            default:
                if (t.level == -1) {
                    tLabel = Terrains.getEditorName(t.terrain);
                } else {
                    tLabel = Terrains.getDisplayName(t.terrain, t.level);
                }
            }
            panCenter.add(new JLabel(tLabel), gridBagCon);

            // Text Color
            gridBagCon.gridx++;
            gridBagCon.anchor = GridBagConstraints.CENTER;
            JButton cButton = new JButton();
            cButton.addActionListener(this);
            panCenter.add(cButton, gridBagCon);
            colorButtons.put(t, cButton);
            
            // Hex Terrain Text
            gridBagCon.gridx++;
            JTextField textF = new JTextField();
            textF.addKeyListener(this);
            textF.setPreferredSize(new Dimension(80,25));
            panCenter.add(textF, gridBagCon);
            textFields.put(t, textF);
            
            // Bold
            gridBagCon.gridx++;
            JToggleButton texBold = new JToggleButton("B");
            texBold.addActionListener(this);
            panCenter.add(texBold, gridBagCon);
            boldToggles.put(t, texBold);
            
            // Shadow Color
            gridBagCon.gridx++;
            JButton cSButton = new JButton();
            cSButton.addActionListener(this);
            panCenter.add(cSButton, gridBagCon);
            shadowButtons.put(t, cSButton);
            
            // The actual painted text example
            JPanel exampleBox = new JPanel() {
                private static final long serialVersionUID = 3549083892447215237L;

                final JTextField myText = textF;
                final JCheckBox myCheck = cbTerrain;
                final JToggleButton myBold = texBold;
                final JButton myColor = cButton;
                final JButton mySColor = cSButton;
                final int myterrainID = t.terrain;
                final String terrainN = terrainWithLevel.contains(myterrainID) ? "5" : "";
                
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // Paint the hex text if the checkbox is selected, otherwise just a gray box
                    if (myCheck.isSelected()) {
                        // set the text and add a level to some
                        String txt = myText.getText() + terrainN;
                        
                        // arbitrary brownish background
                        g.setColor(new Color(180, 150, 40));
                        g.fillRect(0, 2, 80, 25);

                        // and draw the text
                        GUIPreferences.AntiAliasifSet(g);
                        Font font = new Font((String) fontSelector.getSelectedItem(),
                                myBold.isSelected() ? Font.BOLD : Font.PLAIN, fontSize);
                        g.setFont(font);
                        BoardView1.DrawHexText(g, 40, 15, myColor.getBackground(), txt, mySColor.getBackground());
                    } else {
                        // Inactive hex text
                        g.setColor(new Color(150, 150, 150));
                        g.fillRect(0, 2, 80, 25);
                    }
                }
                
            };
            exampleBox.setPreferredSize(new Dimension(80,23));
            gridBagCon.gridx++;
            panCenter.add(exampleBox, gridBagCon);
            exampleBoxes.add(exampleBox);
            
            gridBagCon.gridy++;
        }
        
        JScrollPane scp = new JScrollPane(panCenter);
        scp.getVerticalScrollBar().setUnitIncrement(20);
        setPreferredSize(new Dimension(500, 800));
        content.add(scp, BorderLayout.CENTER);
        
        // Close this dialog when the window manager says to.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelActions();
            }
        });
        
        setResizable(true);
        setLocationRelativeTo(owner);
        validate();
        pack();

    }

    private void LoadSettings() {
        currentSetting = inMapEditor ? HexTextSettings.getMapEdSettings() : HexTextSettings.getInGameSettings();
        ApplySettingsToUI();
    }
    
    private void ApplySettingsToUI() {
        if (currentSetting == null) return;
        
        fontSelector.setSelectedItem(currentSetting.getFont());
        fontSize = currentSetting.getFontSize();
        ArrayList<HexTextConfig> allHTC = currentSetting.getAllConfigs();
        
        for (HexTextConfig htc: allHTC) {
            TerrainLevelPair tlp = new TerrainLevelPair(htc.getTerrain(), htc.getLevel());
            if (!colorButtons.containsKey(tlp)) {
                tlp = new TerrainLevelPair(htc.getTerrain(), -1);
            }
            if (colorButtons.containsKey(tlp)) {
                colorButtons.get(tlp).setBackground(htc.getColor());
                shadowButtons.get(tlp).setBackground(htc.getShadowColor());
                textFields.get(tlp).setText(htc.getText());
                terrainChecks.get(tlp).setSelected(htc.isDisplay());
                boldToggles.get(tlp).setSelected(htc.isBold());
            }
        }
    }
    
    private void SaveSettings() {
        HexTextSettings newHTS = createSettingsFromUI();
        if (inMapEditor) {
            HexTextSettings.saveMapEdSettings(newHTS);            
        } else {
            HexTextSettings.saveInGameSettings(newHTS);
        }
    }

    private HexTextSettings createSettingsFromUI() {
        HexTextSettings newHTS = HexTextSettings.getInstance();
        newHTS.setFont((String)fontSelector.getSelectedItem());
        newHTS.setFontSize(fontSize);
        for (TerrainLevelPair t: settingsList) {
            if (t.terrain == 0) continue;
            HexTextConfig htc = new HexTextConfig(
                    t.terrain, 
                    t.level,
                    terrainChecks.get(t).isSelected(),
                    textFields.get(t).getText(),
                    colorButtons.get(t).getBackground(),
                    shadowButtons.get(t).getBackground(),
                    boldToggles.get(t).isSelected());
            newHTS.addConfig(htc);
        }
        return newHTS;
    }
        
    private String getSpecialLabel(int t) {
        switch (t) {
        case HexTextSettings.HT_SEPARATOR: return "Separator";
        case HexTextSettings.HT_LEVEL: return "Level";
        case HexTextSettings.HT_LIGHT: return "Light";
        case HexTextSettings.HT_HEAVY: return "Heavy";
        case HexTextSettings.HT_ULTRA: return "Ultra";
        case HexTextSettings.HT_OTHERS: return "All Others";
        default: return "--";
        }
    }

    /** Opens a color choice dialog for the indicated text.
     * @param index The index in hexTexts for which the color is chosen 
     */ 
    private void chooseColor(JButton button, String frameTitle) {
        Color newC = JColorChooser.showDialog(this, frameTitle, button.getBackground());
        // update the button icon if a color was chosen
        if (newC != null) button.setBackground(newC);
    }
    
    /** Repaints the text samples with the current settings */
    private void refreshExamples() {
        for (JPanel e: exampleBoxes) {
            e.repaint();
        }
    }
    
    /** Restores the old settings. Used for the Cancel button. 
     *  Necessary because the settings need to be saved to file for  
     *  the preview. */
    private void restoreOldValues() {
        if (inMapEditor) {
            HexTextSettings.saveMapEdSettings(currentSetting);            
        } else {
            HexTextSettings.saveInGameSettings(currentSetting);
        }
    }
    
    private void cancelActions() {
        restoreOldValues();
        updateBoard();
    }

    // All the button presses are handled here. The refresh for text changes
    // in the text fields is handled by the keyPressed etc. below.
    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource().equals(butOkay)) {
            SaveSettings();
            updateBoard();
            setVisible(false);
            
        } else if (e.getSource().equals(butPreview)) {
            SaveSettings();
            updateBoard();
            
        } else if (e.getSource().equals(butCancel)) {
            cancelActions();
            setVisible(false);
            
        } else if (e.getSource().equals(butPreset)) {
            loadPreset();
            
        }else if (e.getSource().equals(butBigger)) {
            if (fontSize < 40) fontSize++;
            
        } else if (e.getSource().equals(butSmaller)) {
            if (fontSize > 1) fontSize--;
            
        } else if (e.getSource().getClass().equals(JButton.class)) {
            if (colorButtons.containsValue(e.getSource()))
                chooseColor((JButton)e.getSource(), "Choose Text Color...");

            if (shadowButtons.containsValue(e.getSource())) {
                if ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) {
                    ((JButton)e.getSource()).setBackground(new Color (1,1,1,0));
                } else {
                    chooseColor((JButton)e.getSource(), "Choose Shadow Color...");
                }
            }
        }
        
        refreshExamples();
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        refreshExamples();
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        refreshExamples();    
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        refreshExamples();
    }
    
    /** Fetches the GUIPrefs in addition to super.setVisible(). */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LoadSettings(); 
            adjustButtons();
            refreshExamples();
        }
        super.setVisible(visible);
    }
    
    /** Give this a new ClientGUI (may be null) */
    public void updateClient(ClientGUI cg, IBoardView bv) {
        clientgui = cg; 
        boardView = bv;
        if ((bv == null) && (cg != null) && (cg.bv != null)) {
            boardView = cg.bv;
        }
        clientgui = cg;
        inMapEditor = ((cg == null) && (bv != null));
        if (inMapEditor) {
            settingsLabel.setText("--- Map-Editor Settings ---"); //$NON-NLS-1$
            settingsLabel.setForeground(Color.BLUE);
        } else {
            settingsLabel.setText("--- In-game Settings ---"); //$NON-NLS-1$
            settingsLabel.setForeground(Color.GREEN);
        }
    }

    /** Enables/Disables the preview button depending on whether a boardview is present. */
    private void adjustButtons() {
        butPreview.setEnabled(boardView != null);
    }

    /** Makes the boardview redraw the hex graphics */
    private void updateBoard() {
        if (boardView != null) {
            boardView.redrawBoard();
        }
    }
    
    /** Loads a preset from the mmconf/hextextpresets directory. */
    private void loadPreset() {
        JFileChooser fc = new JFileChooser("mmconf/hextextpresets");
        fc.setLocation(getLocation().x + 150, getLocation().y + 100);
        fc.setDialogTitle("Load HexText Preset");
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File dir) {
                return (dir.getName().endsWith(".xml") || dir.isDirectory()); //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "*.xml";
            }
        });
        int returnVal = fc.showOpenDialog(this);

        // canceled some way
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (fc.getSelectedFile() == null)) {
            return;
        }

        // load and apply the settings
        currentSetting = HexTextSettings.loadSettings(fc.getSelectedFile());
        ApplySettingsToUI();
    }

}
