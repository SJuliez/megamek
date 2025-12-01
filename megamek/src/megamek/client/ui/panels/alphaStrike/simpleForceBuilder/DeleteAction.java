package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

class DeleteAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    public DeleteAction(SimpleASForceBuilder forceBuilder) {
        super("Delete");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.currentModel().removeUnits(forceBuilder.currentUnitTable().getSelectedRows());
    }
}
