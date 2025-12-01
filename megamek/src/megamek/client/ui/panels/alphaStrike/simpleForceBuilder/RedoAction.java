package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

class RedoAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    RedoAction(SimpleASForceBuilder forceBuilder) {
        super("Redo");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Y"));
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.currentModel().redo();
    }
}
