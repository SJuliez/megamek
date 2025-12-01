package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import megamek.client.ui.Messages;
import megamek.common.jacksonAdapters.MMUWriter;

class SaveForceAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    SaveForceAction(SimpleASForceBuilder forceBuilder) {
        super("Save Force");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
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
        File listFile = dlgLoadList.getSelectedFile();
        if ((returnVal == JFileChooser.APPROVE_OPTION) && (listFile != null)) {
            if (!listFile.toString().contains(".")) {
                listFile = new File(listFile.getParentFile(), listFile.getName() + ".mmu");
            }
            try {
                new MMUWriter().writeMMUFileFullStats(listFile, forceBuilder.currentModel().getUnits());
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(forceBuilder.frame,
                      "The MMU file could not be written. Error message: " + ex.getMessage());
            }
        }
    }
}
