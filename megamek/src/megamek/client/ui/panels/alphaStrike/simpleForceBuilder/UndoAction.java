package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

class UndoAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    UndoAction(SimpleASForceBuilder forceBuilder) {
        super("Undo");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        forceBuilder.model.undoLastChange();
    }
}
