package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import javax.swing.JLayeredPane;
import java.awt.Component;
import java.awt.Dimension;

public class TablePanel extends JLayeredPane {

    private final EmptyUnitTableOverlay overlay;
    private final Component tableComponent;
    private final SimpleASForceBuilderTab forceBuilder;

    TablePanel(SimpleASForceBuilderTab forceBuilder, Component tableComponent) {
        this.forceBuilder = forceBuilder;
        this.tableComponent = tableComponent;
        overlay = new EmptyUnitTableOverlay(forceBuilder);
        setLayout(null); // absolute positioning for overlay
        add(tableComponent, JLayeredPane.DEFAULT_LAYER);
        add(overlay, JLayeredPane.PALETTE_LAYER);
        setPreferredSize(new Dimension(1000, 800));
    }

    void initialize() {
        forceBuilder.model.addTableModelListener(e -> overlay.setVisible(forceBuilder.model.isEmpty()));
    }

    @Override
    public void doLayout() {
        // fill the whole available area with the table
        tableComponent.setBounds(0, 0, getWidth(), getHeight());
        // center the overlay
        Dimension overlaySize = overlay.getPreferredSize();
        overlay.setBounds((getWidth() - overlaySize.width) / 2, (getHeight() - overlaySize.height) / 2,
              overlaySize.width, overlaySize.height);
    }
}
