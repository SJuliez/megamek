package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 * This action opens a new empty force window. Present forces are not changed.
 */
class NewForceAction extends AbstractAction {

    NewForceAction() {
        super("New Force");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SimpleASForceBuilder().setVisible(true);
    }
}
