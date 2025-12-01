package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.alphaStrike.cardDrawer.ASCardPrinter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class PrintAllAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;

    PrintAllAction(SimpleASForceBuilder forceBuilder) {
        super("Print");
        this.forceBuilder = forceBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ASCardPrinter(forceBuilder.currentModel().getUnits(), forceBuilder.frame).printCards();
    }
}
