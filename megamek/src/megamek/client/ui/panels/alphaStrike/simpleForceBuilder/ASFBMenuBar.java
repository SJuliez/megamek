package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import static java.awt.event.KeyEvent.*;

class ASFBMenuBar extends JMenuBar {

    private final SimpleASForceBuilder forceBuilder;

    ASFBMenuBar(SimpleASForceBuilder forceBuilder) {
        this.forceBuilder = forceBuilder;

        JMenuItem item;

        // === File Menu
        JMenu menu = new JMenu("File");
        menu.setMnemonic(VK_F);
        add(menu);

        item = new JMenuItem(forceBuilder.quickLoadListAction);
        item.setAccelerator(KeyStroke.getKeyStroke("F9"));
        menu.add(item);

        item = new JMenuItem(forceBuilder.quickSaveListAction);
        item.setAccelerator(KeyStroke.getKeyStroke("F5"));
        menu.add(item);

        item = new JMenuItem(forceBuilder.loadForceAction);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        menu.add(item);

        item = new JMenuItem(forceBuilder.saveListAction);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        menu.add(item);

        // === Add Menu
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
            JMenuItem skillItem = new JMenuItem(forceBuilder.skillActions.get(skill));
            skillMenu.add(skillItem);
        }
        menu.add(skillMenu);

        menu.addSeparator();
        menu.add(new JMenuItem(forceBuilder.deleteAction));
    }
}
