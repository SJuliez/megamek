package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.client.ui.Messages;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.alphaStrike.conversion.ASConverter;
import megamek.common.jacksonAdapters.MMUReader;
import megamek.common.loaders.MULParser;
import megamek.common.options.GameOptions;
import megamek.common.units.Entity;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This action loads a force and opens a new window for it. Present forces are not changed.
 */
class LoadForceAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;
    private final JFrame frame;

    LoadForceAction(SimpleASForceBuilder forceBuilder) {
        super("Load Force");
        this.forceBuilder = forceBuilder;
        frame = forceBuilder.frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Build the "load unit" dialog, if necessary.
        var dlgLoadList = new JFileChooser(".");
        dlgLoadList.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
        dlgLoadList.setDialogTitle(Messages.getString("ClientGUI.openUnitListFileDialog.title"));
        dlgLoadList.setFileFilter(new FileNameExtensionFilter("MUL files", "mul", "mmu"));

        int returnVal = dlgLoadList.showOpenDialog(frame);
        if ((returnVal == JFileChooser.APPROVE_OPTION) && (dlgLoadList.getSelectedFile() != null)) {
            loadListFromFile(dlgLoadList.getSelectedFile());
        }
    }

    void loadListFromFile(File unitFile) {
        try {
            List<AlphaStrikeElement> elements = new ArrayList<>();
            if (unitFile.toString().endsWith("mul")) {
                List<Entity> loadedUnits = new MULParser(unitFile, new GameOptions()).getEntities();
                for (Entity entity : loadedUnits) {
                    AlphaStrikeElement element = ASConverter.convert(entity);
                    if (element != null) {
                        elements.add(element);
                    }
                }
            } else if (unitFile.toString().endsWith("mmu")) {
                List<Object> units = new MMUReader().read(unitFile);
                for (Object unit : units) {
                    if (unit instanceof AlphaStrikeElement element) {
                        elements.add(element);
                    }
                }
            }
            // Apparently, the units were successfully loaded, so open a new force builder window for them.
            var simpleASForceBuilder = new SimpleASForceBuilder();
            simpleASForceBuilder.setVisible(true);
            simpleASForceBuilder.model.addUnits(elements);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(forceBuilder.frame,
                  "Error loading units from the selected file. Some units may have been added.");
        }
    }
}
