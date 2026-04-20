package megamek.client.ui;

import megamek.MMConstants;
import megamek.MegaMek;

import javax.swing.AbstractAction;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import static megamek.client.ui.Messages.getString;

/**
 * This Action copies OS, Java and project data to the clipboard.
 */
public class CopySystemDataAction extends AbstractAction {

    private final String currentProject;

    public CopySystemDataAction(String currentProject) {
        super(getString("CommonMenuBar.helpCopySystemData"));
        this.currentProject = currentProject;
        putValue(AbstractAction.SHORT_DESCRIPTION, getString("CommonMenuBar.helpCopySystemData.tip"));
    }

    public CopySystemDataAction() {
        this(MMConstants.PROJECT_NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(
              MegaMek.getUnderlyingInformation(MegaMek.getOriginProject(), currentProject)), null);
    }
}
