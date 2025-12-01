package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JToolBar;

class ToolBar extends JToolBar {

    ToolBar(SimpleASForceBuilder forceBuilder) {
        add(forceBuilder.addUnitAction);
        add(forceBuilder.loadListAction);
        add(forceBuilder.undoAction);
        add(forceBuilder.redoAction);
        add(forceBuilder.printAllAction);
    }
}
