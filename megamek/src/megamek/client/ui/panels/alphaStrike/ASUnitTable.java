package megamek.client.ui.panels.alphaStrike;

import megamek.client.ui.util.UIUtil;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;

public class ASUnitTable extends JTable {

    static final int ROW_HEIGHT_FULL = 65;

    public ASUnitTable(SimpleASUnitTableModel mekModel) {
        super(mekModel);
        mekModel.addTableModelListener(e -> setRowHeights());
        setRowHeights();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        setRowHeights();
        super.columnMarginChanged(e);
    }

    private void setRowHeights() {
        setRowHeight(UIUtil.scaleForGUI(ROW_HEIGHT_FULL));
    }
}
