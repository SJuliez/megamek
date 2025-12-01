package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import com.formdev.flatlaf.ui.FlatLineBorder;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.Insets;

class EmptyUnitTableOverlay extends JPanel {

    EmptyUnitTableOverlay(SimpleASForceBuilderTab forceBuilder) {
        setLayout(new GridLayout(0, 1, 0, 10));
        setOpaque(false);
        setBorder(new FlatLineBorder(new Insets(40, 20, 40, 20), 12));

        add(new JLabel("Nothing here yet.", SwingConstants.CENTER));
        add(new JButton(forceBuilder.getBuilder().quickLoadListAction));
        add(new JButton(forceBuilder.getBuilder().addUnitAction));
        add(new JButton(forceBuilder.getBuilder().loadListAction));
    }
}
