package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

class TableMouseListener extends MouseInputAdapter {

    private final SimpleASForceBuilder forceBuilder;
    private final ASUnitTable unitTable;

    TableMouseListener(SimpleASForceBuilder forceBuilder) {
        this.forceBuilder = forceBuilder;
        unitTable = forceBuilder.unitTable;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // TODO: Edit unit
//            int row = unitTable.rowAtPoint(e.getPoint());
//            InGameObject entity = mekModel.getEntityAt(row);
//            if ((entity instanceof Entity) && isEditable((Entity) entity)) {
//                lobbyActions.customizeMek((Entity) entity);
//            }
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
        int row = unitTable.rowAtPoint(e.getPoint());
        if (!unitTable.isRowSelected(row)) {
            unitTable.changeSelection(row, row, false, false);
        }
    }

    private void showPopup(MouseEvent e) {
        if (unitTable.getSelectedRowCount() == 0) {
            return;
        }
        JPopupMenu popup = new ContextMenu(forceBuilder);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
}
