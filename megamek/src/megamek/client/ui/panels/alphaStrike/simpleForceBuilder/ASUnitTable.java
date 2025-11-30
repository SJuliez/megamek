package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.client.ui.util.UIUtil;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

class ASUnitTable extends JTable {

    static final int ROW_HEIGHT_FULL = 65;

    private final SimpleASForceBuilder forceBuilder;

    ASUnitTable(SimpleASForceBuilder forceBuilder) {
        super(forceBuilder.model);
        this.forceBuilder = forceBuilder;
    }

    void initialize() {
        setIntercellSpacing(new Dimension(2, 1));
        prepareColumns();

        setRowHeights();
        forceBuilder.model.addTableModelListener(e -> setRowHeights());

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "delete");
        getActionMap().put("delete", forceBuilder.deleteAction);

        addMouseListener(new TableMouseListener(forceBuilder));
    }

    private void prepareColumns() {
        var columnModel = getColumnModel();
        TableColumn pvColumn = columnModel.getColumn(SimpleASUnitTableModel.COL_PV);
        pvColumn.setCellRenderer(UIUtil.rightAlignedTableCellRenderer());
        TableColumn skillColumn = columnModel.getColumn(SimpleASUnitTableModel.COL_SKILL);
        skillColumn.setCellRenderer(UIUtil.centerAlignedTableCellRenderer());
        packColumn(this, SimpleASUnitTableModel.COL_SKILL, "  Skill  ");
        packColumn(this, SimpleASUnitTableModel.COL_PV, "  99999  ");
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        setRowHeights();
        super.columnMarginChanged(e);
    }

    private void setRowHeights() {
        double scale = Double.parseDouble(System.getProperty("flatlaf.uiScale"));
        setRowHeight((int) (scale * ROW_HEIGHT_FULL));
    }

//    public static void packColumn(JTable table, int columnIndex, int margin) {
//        TableColumn column = table.getColumnModel().getColumn(columnIndex);
//        int width;
//
//        // --- Header width ---
//        TableCellRenderer headerRenderer = column.getHeaderRenderer();
//        if (headerRenderer == null) {
//            headerRenderer = table.getTableHeader().getDefaultRenderer();
//        }
//        Component headerComp = headerRenderer.getTableCellRendererComponent(
//              table, column.getHeaderValue(), false, false, 0, columnIndex);
//        width = headerComp.getPreferredSize().width;
//
//        // --- Cell widths ---
//        for (int row = 0; row < table.getRowCount(); row++) {
//            TableCellRenderer renderer = table.getCellRenderer(row, columnIndex);
//            Component comp = renderer.getTableCellRendererComponent(
//                  table, table.getValueAt(row, columnIndex), false, false, row, columnIndex);
//            width = Math.max(width, comp.getPreferredSize().width);
//        }
//
//        // Add margin for spacing
//        width += 2 * margin;
//
//        column.setPreferredWidth(width);
//        column.setMaxWidth(5 * width);
//    }

    public static void packColumn(JTable table, int columnIndex, String protoTypeText) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        Component comp = new JLabel(protoTypeText);
        int width = comp.getPreferredSize().width;
        column.setPreferredWidth(width);
        column.setMaxWidth(2 * width);
    }
}
