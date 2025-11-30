package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import megamek.client.ui.Messages;
import megamek.common.jacksonAdapters.MMUWriter;

class SaveListAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    SaveListAction(SimpleASForceBuilder forceBuilder) {
        super("Save Unit List");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Build the "load unit" dialog, if necessary.
        var dlgLoadList = new JFileChooser(".");
        dlgLoadList.setLocation(forceBuilder.frame.getLocation().x + 150, forceBuilder.frame.getLocation().y + 100);
        dlgLoadList.setDialogTitle(Messages.getString("ClientGUI.openUnitListFileDialog.title"));
        dlgLoadList.setFileFilter(new FileNameExtensionFilter("MMU files", "mmu"));

        int returnVal = dlgLoadList.showOpenDialog(forceBuilder.frame);
        if ((returnVal == JFileChooser.APPROVE_OPTION) && (dlgLoadList.getSelectedFile() != null)) {
            try {
                new MMUWriter().writeMMUFileFullStats(forceBuilder.getQuicksaveFile(), forceBuilder.model.getUnits());
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(forceBuilder.frame,
                      "The MMU file could not be written. " + ex.getMessage());
            }
        }
    }
}
