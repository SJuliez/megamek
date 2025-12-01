package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

class UndoAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    UndoAction(SimpleASForceBuilder forceBuilder) {
        super("Undo");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Z"));
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.currentModel().undoLastChange();
    }
}
