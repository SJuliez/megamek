/*
 * Configuration dialog for the boardview hex texts
 *
 * Created on 8 April 2016
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import megamek.common.Terrains;
import megamek.client.ui.IBoardView;
import megamek.client.ui.Messages;
import megamek.client.ui.swing.boardview.BoardView1;

public class CommonSettingsHexTextsDialog extends ClientDialog implements ActionListener, KeyListener {
    
    private static final long serialVersionUID = -4227870501769580587L;
    
    ClientGUI clientgui;
    IBoardView boardView;
    GUIPreferences guip = GUIPreferences.getInstance();
    
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

    /** The font size for all texts. */
    private int fontSize = 12;
    
    // Other Text Features
    private JPanel panCenter;
    
    private final ArrayList<Integer> terrainList = new ArrayList<Integer>(Arrays.asList( HexTextSettings.HT_SEPARATOR, HexTextSettings.HT_LEVEL,  Terrains.WATER, 0,
            Terrains.WOODS, Terrains.JUNGLE, HexTextSettings.HT_LIGHT, HexTextSettings.HT_HEAVY, HexTextSettings.HT_ULTRA, 0,
            Terrains.SWAMP, Terrains.ROUGH, Terrains.RUBBLE, Terrains.MUD, Terrains.SAND, Terrains.TUNDRA, Terrains.MAGMA, Terrains.FIELDS, 0,
            Terrains.BLDG_ELEV, Terrains.BLDG_CF, Terrains.BUILDING, Terrains.BLDG_CLASS, Terrains.BLDG_ARMOR, Terrains.BLDG_BASEMENT_TYPE, Terrains.BLDG_BASE_COLLAPSED, 0,
            Terrains.BRIDGE_ELEV, Terrains.BRIDGE_CF, Terrains.BRIDGE, 0,
            Terrains.FUEL_TANK_ELEV, Terrains.FUEL_TANK_CF, Terrains.FUEL_TANK, Terrains.FUEL_TANK_MAGN, 0,
            Terrains.PAVEMENT, Terrains.ROAD, Terrains.INDUSTRIAL, 0,
            Terrains.IMPASSABLE, Terrains.ELEVATOR, Terrains.FORTIFIED, Terrains.SCREEN, Terrains.FLUFF, Terrains.ARMS, Terrains.LEGS, 0,
            Terrains.RAPIDS, Terrains.ICE, Terrains.SNOW, Terrains.FIRE, Terrains.SMOKE, Terrains.GEYSER, Terrains.METAL_CONTENT
             ));
    
    private List<JPanel> exampleBoxes = new ArrayList<>();
    private HashMap<Integer, JButton> colorButtons = new HashMap<>();
    private HashMap<Integer, JButton> shadowButtons = new HashMap<>();
    private HashMap<Integer, JTextField> textFields = new HashMap<>();
    private HashMap<Integer, JCheckBox> terrainChecks = new HashMap<>();
    private HashMap<Integer, JToggleButton> boldToggles = new HashMap<>();
    
    private HexTextSettings currentIngameSetting;
    
    // --------------------------------------------------------------------------
    
    /** A dialog for confiuration of the texts that are written on hexes
     * in the boardview, like "LEVEL" or "Swamp". Called from View->Client
     * Settings->Graphics Tab (CommonSettingsDialog). */
    public CommonSettingsHexTextsDialog(JFrame owner, ClientGUI cg, IBoardView bv) {

        super(owner, "Configure Hex Texts ... ", true); //$NON-NLS-1$
        clientgui = cg; 
        boardView = bv;
        if ((bv == null) && (cg != null) && (cg.bv != null)) {
            boardView = cg.bv;
        }
        
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
        panStandard.setLayout(new FlowLayout(FlowLayout.LEFT));
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
        for (int t: terrainList) {
            // for each terrain, add...
            
            // Spacer between groups of terrains
            if (t == 0) {
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
            String tLabel;
            if (t < 1000) {
                tLabel = Terrains.getEditorName(t);
            } else {
                tLabel = getSpecialLabel(t);
            }
            panCenter.add(new JLabel(tLabel), gridBagCon);
            
            // Text Color
            gridBagCon.gridx++;
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
            cSButton.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        setBackground(new Color(0,0,0,255));
                    }
                }
            });
            panCenter.add(cSButton, gridBagCon);
            shadowButtons.put(t, cSButton);
            
            // The actual painted text example
            JPanel exampleBox = new JPanel() {
                private static final long serialVersionUID = 3549083892447215237L;

                String myterrain = tLabel;
                JTextField myText = textF;
                JCheckBox myCheck = cbTerrain;
                JToggleButton myBold = texBold;
                JButton myColor = cButton;
                JButton mySColor = cSButton;
                
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g); 

                    // Paint the hex text if the checkbox is selected, otherwise just a gray box
                    if (myCheck.isSelected()) {
                        // arbitrary brownish background
                        g.setColor(new Color(180,150,40));
                        g.fillRect(0,2,80,25);

                        GUIPreferences.AntiAliasifSet(g);
                        // set the font
                        Font font = new Font(
                                (String)fontSelector.getSelectedItem(),
                                myBold.isSelected() ? Font.BOLD : Font.PLAIN,
                                        fontSize);
                        g.setFont(font);

                        // set the text and add a level to some
                        String txt = myText.getText();
                        if (myterrain.equals("Level") ||
                                myterrain.equals("Depth") ||
                                myterrain.equals("Building Height")) {
                            txt += "5";
                        }

                        BoardView1.DrawHexText(g, 40, 15, myColor.getBackground(), txt, mySColor.getBackground());
                    } else {
                        // Inactive hex text
                        g.setColor(new Color(150,150,150));
                        g.fillRect(0,2,80,25);
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
        currentIngameSetting = HexTextSettings.getInGameSettings();
        ApplySettingsToUI();
    }
    
    private void ApplySettingsToUI() {
        if (currentIngameSetting == null) return;
        
        fontSelector.setSelectedItem(currentIngameSetting.getFont());
        fontSize = currentIngameSetting.getFontSize();
        ArrayList<HexTextConfig> allHTC = currentIngameSetting.getAllConfigs();
        
        for (HexTextConfig htc: allHTC) {
            int terrain = htc.getTerrain();
            colorButtons.get(terrain).setBackground(htc.getColor());
            shadowButtons.get(terrain).setBackground(htc.getShadowColor());
            textFields.get(terrain).setText(htc.getText());
            terrainChecks.get(terrain).setSelected(htc.isDisplayed());
            boldToggles.get(terrain).setSelected(htc.isBold());
        }
    }
    
    private void SaveSettings() {
        HexTextSettings newHTS = createSettingsFromUI();
        HexTextSettings.saveInGameSettings(newHTS);
    }

    private HexTextSettings createSettingsFromUI() {
        HexTextSettings newHTS = HexTextSettings.getInstance();
        newHTS.setFont((String)fontSelector.getSelectedItem());
        newHTS.setFontSize(fontSize);
        for (int t: terrainList) {
            if (t == 0) continue;
            HexTextConfig htc = new HexTextConfig(
                    t, 
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
    
    /** Restores the old settings to the GUIP. Used for the Cancel button. 
     *  Necessary because the GUIP settings get updated when
     *  the Preview button is used. */
    private void restoreOldValues() {
        HexTextSettings.saveInGameSettings(currentIngameSetting);
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
            
        } else if (e.getSource().equals(butBigger)) {
            if (fontSize < 40) fontSize++;
            
        } else if (e.getSource().equals(butSmaller)) {
            if (fontSize > 1) fontSize--;
            
        } else if (e.getSource().getClass().equals(JButton.class)) {
            if (colorButtons.containsValue(e.getSource()))
                chooseColor((JButton)e.getSource(), "Choose Text Color...");
            
            if (shadowButtons.containsValue(e.getSource()))
                chooseColor((JButton)e.getSource(), "Choose Shadow Color...");
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
    }

    /** Enables/Disables the preview button depending on whether a boardview is present. */
    private void adjustButtons() {
        if (boardView != null) {
            butPreview.setEnabled(true);
        } else {
            butPreview.setEnabled(false);
        }
    }

    /** Makes the boardview redraw the hex graphics */
    private void updateBoard() {
        if (boardView != null) {
            boardView.redrawBoard();
        }
    }

}
