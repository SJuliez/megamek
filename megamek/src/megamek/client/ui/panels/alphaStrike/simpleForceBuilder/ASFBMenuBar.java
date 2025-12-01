package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import static java.awt.event.KeyEvent.*;

class ASFBMenuBar extends JMenuBar {

    ASFBMenuBar(SimpleASForceBuilder forceBuilder) {

        // === File Menu
        JMenu menu = new JMenu("Force");
        menu.setMnemonic(VK_F);
        add(menu);

        menu.add(forceBuilder.newForceAction);
        menu.add(forceBuilder.loadForceAction);
        menu.add(forceBuilder.saveListAction);
        menu.addSeparator();
        menu.add(forceBuilder.quickLoadListAction);
        menu.add(forceBuilder.quickSaveListAction);
        menu.addSeparator();
        menu.add(forceBuilder.addLanceAction);

        // === Units Menu
        menu = new JMenu("Units");
        menu.setMnemonic(VK_U);
        add(menu);

        menu.add(forceBuilder.addUnitAction);
        menu.add(forceBuilder.loadListAction);
        menu.addSeparator();
        menu.add(forceBuilder.clearAction);

        // === Edit Menu
        menu = new JMenu("Edit");
        menu.setMnemonic(VK_E);
        add(menu);

        menu.add(forceBuilder.undoAction);
        menu.add(forceBuilder.redoAction);
        menu.addSeparator();

        JMenu skillMenu = new JMenu("Skill");
        for (int skill = 0; skill <= 8; skill++) {
            skillMenu.add(forceBuilder.skillActions.get(skill));
        }
        menu.add(skillMenu);

        menu.addSeparator();
        menu.add(forceBuilder.deleteAction);
    }
}
