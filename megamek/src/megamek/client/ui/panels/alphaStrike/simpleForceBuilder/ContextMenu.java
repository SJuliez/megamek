package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class ContextMenu extends JPopupMenu {

    private final SimpleASForceBuilder forceBuilder;

    ContextMenu(SimpleASForceBuilder forceBuilder) {
        this.forceBuilder = forceBuilder;
        JMenuItem item = new JMenuItem(forceBuilder.deleteAction);
        add(item);
        JMenu skillMenu = new JMenu("Skill");
        for (int skill = 0; skill <= 8; skill++) {
            JMenuItem skillItem = new JMenuItem(forceBuilder.skillActions.get(skill));
            skillMenu.add(skillItem);
        }
        add(skillMenu);
    }
}
