/*
 * Configuration dialog for the boardview hex texts
 *
 * Created on 8 April 2016
 */

package megamek.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import megamek.client.ui.Messages;
import megamek.client.ui.swing.boardview.BoardView1;

public class CommonSettingsBoardTextsDialog extends ClientDialog implements ActionListener, KeyListener {
    
    private static final long serialVersionUID = -4227870501769580587L;
    
    ClientGUI clientgui;
    GUIPreferences guip = GUIPreferences.getInstance();
    
    private static final String[] FONTS = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();

    // OK / Cancel / Preview
    private JPanel panButtons = new JPanel();
    private JButton butOkay = new JButton(Messages.getString("Okay")); //$NON-NLS-1$
    private JButton butCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
    private JButton butPreview = new JButton("Preview"); //$NON-NLS-1$

    // Default Text Features
    private JPanel panStandard = new JPanel();
    private JLabel defaultLabel = new JLabel("General Features: "); //$NON-NLS-1$
    private JComboBox<String> defaultFont = new JComboBox<>();
    private JButton butBold = new JButton("B"); //$NON-NLS-1$
    private JButton butBigger = new JButton("+"); //$NON-NLS-1$
    private JButton butSmaller = new JButton("-"); //$NON-NLS-1$
    private JButton butShadowColor = new JButton("Shadow Color"); //$NON-NLS-1$
    /** The font size for all texts. */
    private int fontSize = 12;
    private boolean fontBold = true;
    private Color shadowColor;
    
    
    // Other Text Features
    private JPanel panCenter = new JPanel();
    private final String[] hexTexts = { "Separator", "Level", "Depth", "Building Height", "Swamp", 
            "Rough", "Rubble", "Woods", "Jungle", "Mud" };
    private final String[] guipCodes = { "Sep", "Lvl", "Dth", "Bdg", "Swp", 
             "Rgh", "Rbl", "Wds", "Jng", "Mud" };
    private final String guipA = "HexText";  //$NON-NLS-1$
    
    private List<JButton> textButtons = new ArrayList<>();
    private List<JTextField> textFields = new ArrayList<>();
    private List<JPanel> exampleBoxes = new ArrayList<>();
    
    /** the currently chosen colors for each entry of hexTexts */
    private Map<Integer,Color> colors = new HashMap<>();
    
    // The old values before the dialog was called, used when Cancel is pressed 
    private String oldFont;
    private Map<Integer,Color> oldColors = new HashMap<>();
    private int oldFontSize;
    private List<String> oldTexts = new ArrayList<>();
    private Color oldShadowColor;
    private boolean oldFontBold;
   
    // --------------------------------------------------------------------------
    
    /** A dialog for confiuration of the texts that are written on hexes
     * in the boardview, like "LEVEL" or "Swamp". Called from View->Client
     * Settings (CommonSettingsDialog). */
    public CommonSettingsBoardTextsDialog(JFrame owner, ClientGUI cg) {

        super(owner, "Configure Hex Texts ... ", true); //$NON-NLS-1$
        clientgui = cg; 

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        // OK / Cancel / Preview buttons
        butOkay.addActionListener(this);
        butCancel.addActionListener(this);
        butPreview.addActionListener(this);
        panButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        panButtons.add(butOkay);
        panButtons.add(butCancel);
        panButtons.add(butPreview);
        adjustButtons();
        content.add(panButtons, BorderLayout.SOUTH);

        // Default Text Config
        for (String f: FONTS) defaultFont.addItem(f);
        defaultFont.addActionListener(this);
        butBigger.addActionListener(this);
        butSmaller.addActionListener(this);
        butBold.addActionListener(this);
        butShadowColor.addActionListener(this);
        panStandard.setLayout(new FlowLayout(FlowLayout.LEFT));
        panStandard.add(defaultLabel);
        panStandard.add(defaultFont);
        panStandard.add(butBold);
        panStandard.add(butBigger);
        panStandard.add(butSmaller);
        panStandard.add(butShadowColor);
        content.add(panStandard, BorderLayout.NORTH);
        
        // Specific terrains
        panCenter.setLayout(new GridLayout(0, 4, 4, 4));
        panCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String t: Arrays.asList(hexTexts)) {
            // for each row, add...
            
            // A label with the terrain type 
            panCenter.add(new JLabel(t));
            
            // A color chooser button
            JButton cButton = new JButton(t);
            cButton.addActionListener(this);
            panCenter.add(cButton);
            textButtons.add(cButton);
            
            // A text field for the terrain text
            JTextField textF = new JTextField();
            textF.addKeyListener(this);
            panCenter.add(textF);
            textFields.add(textF);
            
            // The actual painted text example
            final int i = Arrays.asList(hexTexts).indexOf(t);
            JPanel exampleBox = new JPanel() {
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g); 
                    
                    GUIPreferences.AntiAliasifSet(g);
                    // arbitrary brownish background
                    g.setColor(new Color(180,150,40));
                    g.fillRect(0,0,80,20);
                    
                    // set the font
                    Font font = new Font(
                            (String)defaultFont.getSelectedItem(),
                            fontBold ? Font.BOLD : Font.PLAIN,
                            fontSize);
                    g.setFont(font);
                    
                    // set the text and add a level to some
                    String txt = textFields.get(i).getText();
                    if (hexTexts[i].equals("Level") ||
                            hexTexts[i].equals("Depth") ||
                            hexTexts[i].equals("Building Height")) {
                        txt += "5";
                    }
                    
                    BoardView1.DrawHexText(g, 40, 10, colors.get(i), txt, shadowColor);
                }
            };
            panCenter.add(exampleBox);
            exampleBoxes.add(exampleBox);
        }
        content.add(panCenter, BorderLayout.CENTER);
        
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
    
    /** Opens a color choice dialog for the indicated text.
     * @param index The index in hexTexts for which the color is chosen 
     */ 
    private void chooseColor(int index) {
        Color newC = JColorChooser.showDialog(this,"Choose Standard Text Color", colors.get(index));
        // update the button icon if a color was chosen
        if (newC != null) {
            colors.put(index, newC);
            refreshExamples();
        }
    }
    
    /** Opens a color choice dialog for the shadow color. */ 
    private void chooseShadowColor() {
        Color newC = JColorChooser.showDialog(this,"Choose Shadow Color", shadowColor);
        // update the button icon if a color was chosen
        if (newC != null) {
            shadowColor = newC;
            refreshExamples();
        }
    }
    
    /** Repaints the text samples with the current settings */
    private void refreshExamples() {
        for (JPanel e: exampleBoxes) {
            e.repaint();
        }
    }
    
    /** Saves the current config to the GUIP. */
    private void saveValuesToGUIP() {
        String f = (String)defaultFont.getSelectedItem();
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_FONT, f);
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_SIZE, fontSize);
        guip.setValue(GUIPreferences.HEX_TEXT_SHD_COLOR, guip.getColorString(shadowColor));
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_BOLD, fontBold);
        
        for (JButton b: textButtons) {
            int i = textButtons.indexOf(b);
            guip.setValue(guipA+guipCodes[i]+"Color", guip.getColorString(colors.get(i)));
            guip.setValue(guipA+guipCodes[i]+"Text", textFields.get(i).getText());
        }
    }
    
    /** Restores the old settings to the GUIP. Used for the Cancel button. 
     *  Necessary because the GUIP settings get updated when
     *  the Preview button is used. */
    private void restoreOldValuesToGUIP() {
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_FONT, oldFont);
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_SIZE, oldFontSize);
        guip.setValue(GUIPreferences.HEX_TEXT_SHD_COLOR, guip.getColorString(oldShadowColor));
        guip.setValue(GUIPreferences.HEX_TEXT_DEF_BOLD, oldFontBold);
        
        for (JButton b: textButtons) {
            int i = textButtons.indexOf(b);
            guip.setValue(guipA+guipCodes[i]+"Color", guip.getColorString(oldColors.get(i)));
            guip.setValue(guipA+guipCodes[i]+"Text", oldTexts.get(i));
        }
    }
    
    /** Loads the settings from the GUIP. Also stores them separately
     *  in the old... variables for cancelling. */  
    private void loadValuesfromGUIP() {
        oldFont = guip.getString(GUIPreferences.HEX_TEXT_DEF_FONT);
        defaultFont.setSelectedItem(oldFont);
        oldFontSize = guip.getInt(GUIPreferences.HEX_TEXT_DEF_SIZE);
        fontSize = oldFontSize;
        shadowColor = guip.getColor(GUIPreferences.HEX_TEXT_SHD_COLOR);
        oldShadowColor = guip.getColor(GUIPreferences.HEX_TEXT_SHD_COLOR);
        oldFontBold = guip.getBoolean(GUIPreferences.HEX_TEXT_DEF_BOLD);
        fontBold = oldFontBold;
        
        for (JButton b: textButtons) {
            int i = textButtons.indexOf(b);
            String t = guip.getString(guipA+guipCodes[i]+"Text");
            oldTexts.add(i, t);
            textFields.get(i).setText(t);

            colors.put(i, guip.getColor(guipA+guipCodes[i]+"Color"));
            oldColors.put(i, guip.getColor(guipA+guipCodes[i]+"Color"));
        }
    }
    
    private void cancelActions() {
        restoreOldValuesToGUIP();
        updateBoard();
    }

    // All the button presses are handled here. The refresh for text changes
    // in the text fields is handled by the keyPressed etc. below.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(butOkay)) {
            saveValuesToGUIP();
            updateBoard();
            setVisible(false);
            
        } else if (e.getSource().equals(butCancel)) {
            cancelActions();
            setVisible(false);
            
        } else if (e.getSource().equals(butBigger)) {
            fontSize++;
            refreshExamples();
            
        } else if (e.getSource().equals(butBold)) {
            fontBold = !fontBold;
            adjustButtons();
            refreshExamples();
            
        } else if (e.getSource().equals(butShadowColor)) {
            chooseShadowColor();
            refreshExamples();
            
        } else if (e.getSource().equals(butPreview)) {
            saveValuesToGUIP();
            updateBoard();
            
        } else if (e.getSource().equals(butSmaller)) {
            if (fontSize > 1) { 
                fontSize--;
                refreshExamples();
            }

        } else if (e.getSource().equals(defaultFont)) {
            refreshExamples();
            
        } else {
            // identify the color button pressed
            int i = textButtons.indexOf(e.getSource());
            // it shouldn't happen, but don't do anything if 
            // the button couldn't be identified
            if (i != -1) {
                chooseColor(i);
            }
        }

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
            loadValuesfromGUIP(); 
            adjustButtons();
            refreshExamples();
        }
        super.setVisible(visible);
    }
    
    /** Give this a new ClientGUI (may be null) */
    public void updateClient(ClientGUI cg) {
        clientgui = cg;
    }

    /** Enables/Disables the preview button depending on whether a boardview is present. */
    private void adjustButtons() {
        if ((clientgui != null) && (clientgui.bv != null)) {
            butPreview.setEnabled(true);
        } else {
            butPreview.setEnabled(false);
        }
        butBold.setText("<HTML>"+(fontBold?"<B>":"")+"B</HTML>");        
    }

    /** Makes the boardview redraw the hex graphics */
    private void updateBoard() {
        if ((clientgui != null) && (clientgui.bv != null)) {
            clientgui.bv.clearHexImageCache();
            clientgui.bv.repaint(); // TODO: necessary?
        }
    }

}


