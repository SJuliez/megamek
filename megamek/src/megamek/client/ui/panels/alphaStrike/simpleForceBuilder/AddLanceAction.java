package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

class AddLanceAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    AddLanceAction(SimpleASForceBuilder forceBuilder) {
        super("New Lance");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl ADD"));
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SimpleASForceBuilderTab newTab = new SimpleASForceBuilderTab(forceBuilder);
        newTab.setName("New Lance");
        forceBuilder.mainPanel.add(newTab);
        // Switch to the new Tab:
        forceBuilder.mainPanel.setSelectedIndex(forceBuilder.mainPanel.getComponentCount() - 1);
    }
}
