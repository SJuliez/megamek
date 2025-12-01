package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

class SimpleASForceBuilderTab extends JPanel {

    // TODO: The field inits below are order-sensitive, bad bad
    final SimpleASUnitTableModel model = new SimpleASUnitTableModel();
    final ASUnitTable unitTable = new ASUnitTable(this);
    final JScrollPane tableScrollPane = new JScrollPane(unitTable);
    final TablePanel mainPanel;

    private boolean isInitialised = false;

    private final StatusBar statusBar = new StatusBar(this);
    private final SimpleASForceBuilder forceBuilder;

    SimpleASForceBuilderTab(SimpleASForceBuilder forceBuilder) {
        this.forceBuilder = forceBuilder;
        setLayout(new BorderLayout(5, 5));
        mainPanel = new TablePanel(this, tableScrollPane);
        add(mainPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void initialize() {
        if (!isInitialised) {
            unitTable.initialize();
            statusBar.initialize();
            mainPanel.initialize();
            isInitialised = true;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initialize();
        }
        super.setVisible(visible);
    }

    SimpleASForceBuilder getBuilder() {
        return forceBuilder;
    }

    boolean isEmpty() {
        return model.isEmpty();
    }
}
