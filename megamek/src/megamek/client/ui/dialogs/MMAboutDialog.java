package megamek.client.ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import megamek.MMConstants;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MMAboutDialog extends AbstractAboutDialog {

    public MMAboutDialog(JFrame parentFrame) {
        super(parentFrame);
    }

    @Override
    protected JComponent version() {
        JLabel program = new JLabel("MegaMek");
        program.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        JLabel version = new JLabel("Version: " + MMConstants.VERSION);
        var panel = Box.createVerticalBox();
        panel.add(program);
        panel.add(Box.createVerticalStrut(8));
        panel.add(version);
        return panel;
    }
}
