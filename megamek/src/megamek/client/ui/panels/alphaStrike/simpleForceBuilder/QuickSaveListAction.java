package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.jacksonAdapters.MMUWriter;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.IOException;

class QuickSaveListAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    QuickSaveListAction(SimpleASForceBuilder forceBuilder) {
        super("Quicksave");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            new MMUWriter().writeMMUFileFullStats(forceBuilder.getQuicksaveFile(), forceBuilder.model.getUnits());
            forceBuilder.updateActionStates(); // allow quickload if not allowed before
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(forceBuilder.frame,
                  "The MMU file could not be written. " + ex.getMessage());
        }
    }
}
