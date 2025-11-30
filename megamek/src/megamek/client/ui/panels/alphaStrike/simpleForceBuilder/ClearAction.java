package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class ClearAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    ClearAction(SimpleASForceBuilder forceBuilder) {
        super("Remove All");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.model.removeAllUnits();
    }
}
