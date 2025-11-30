package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.jacksonAdapters.MMUReader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

class QuickLoadListAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    QuickLoadListAction(SimpleASForceBuilder forceBuilder) {
        super("Quickload");
//        putValue(SHORT_DESCRIPTION, "Quickload");
//        putValue(MNEMONIC_KEY, mnemonic);
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.model.removeAllUnits();
        try {
            List<AlphaStrikeElement> elements = new ArrayList<>();
            List<Object> units = new MMUReader().read(forceBuilder.getQuicksaveFile());
            for (Object unit : units) {
                if (unit instanceof AlphaStrikeElement element) {
                    elements.add(element);
                }
            }
            forceBuilder.model.addUnits(elements);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(forceBuilder.frame,
                  "Error loading units from the selected file. Some units may have been added." + ex.getMessage());
        }
    }
}
