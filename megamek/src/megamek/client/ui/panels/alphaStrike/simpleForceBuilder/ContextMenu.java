package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

class ContextMenu extends JPopupMenu {

    ContextMenu(SimpleASForceBuilder forceBuilder) {
        add(forceBuilder.deleteAction);

        JMenu skillMenu = new JMenu("Skill");
        for (int skill = 0; skill <= 8; skill++) {
            skillMenu.add(forceBuilder.skillActions.get(skill));
        }
        add(skillMenu);
    }
}
