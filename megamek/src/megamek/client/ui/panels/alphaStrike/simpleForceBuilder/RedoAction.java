package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class RedoAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    RedoAction(SimpleASForceBuilder forceBuilder) {
        super("Redo");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.model.redo();
    }
}
