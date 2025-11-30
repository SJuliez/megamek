package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;

class ToolBar extends JPanel {

    ToolBar(SimpleASForceBuilder forceBuilder) {
        super(new FlowLayout(FlowLayout.LEFT));
        add(new JButton(forceBuilder.addUnitAction));
        add(new JButton(forceBuilder.loadListAction));
        add(new JButton(forceBuilder.undoAction));
        add(new JButton(forceBuilder.redoAction));
        add(new JButton(forceBuilder.printAllAction));
//        JButton cardViewButton = new JButton("Show Card View");
//        printButton.addActionListener(e -> new ASCardPrinter(model.getUnits(), forceBuilder.frame).printCards());
//        add(printButton);
    }
}
