package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

class BadgeLabel extends JLabel {

    private static final String VALUE_FORMAT = "arc: 12; border: 2,8,2,8,#444444; foreground: #6c6; background: #445";

    public BadgeLabel(String text) {
        super(text);
        putClientProperty(FlatClientProperties.STYLE, VALUE_FORMAT);
    }
}
