package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

class DeleteAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    public DeleteAction(SimpleASForceBuilder forceBuilder) {
        super("Delete");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.model.removeUnits(forceBuilder.unitTable.getSelectedRows());
    }
}
