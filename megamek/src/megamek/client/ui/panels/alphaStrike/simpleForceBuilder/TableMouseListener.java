package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.client.ui.panels.alphaStrike.ConfigurableASCardPanel;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

class TableMouseListener extends MouseInputAdapter {

    private final SimpleASForceBuilderTab forceTab;

    TableMouseListener(SimpleASForceBuilderTab forceTab) {
        this.forceTab = forceTab;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = forceTab.unitTable.rowAtPoint(e.getPoint());
            if (forceTab.model.unitAt(row).isPresent()) {
                var view = new ConfigurableASCardPanel(forceTab.model.unitAt(row).get(), null);
                JDialog window = new JDialog(forceTab.getBuilder().frame, "AS Card");
                window.add(view);
                window.pack();
                window.setVisible(true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkForPopup(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkForPopup(e);
    }

    private void checkForPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            checkSelectionAtMouse(e);
            showPopup(e);
        }
    }

    private void checkSelectionAtMouse(MouseEvent e) {
        // If the right mouse button is pressed over an unselected entity,
        // clear the selection and select that entity instead
        int row = forceTab.unitTable.rowAtPoint(e.getPoint());
        if (!forceTab.unitTable.isRowSelected(row)) {
            forceTab.unitTable.changeSelection(row, row, false, false);
        }
    }

    private void showPopup(MouseEvent e) {
        if (forceTab.unitTable.getSelectedRowCount() == 0) {
            return;
        }
        JPopupMenu popup = new ContextMenu(forceTab.getBuilder());
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
}
