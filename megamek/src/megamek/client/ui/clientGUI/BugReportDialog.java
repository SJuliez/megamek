package megamek.client.ui.clientGUI;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import megamek.MMConstants;
import megamek.client.ui.BugReportMessages;
import megamek.client.ui.CopySystemDataAction;
import megamek.client.ui.util.UIUtil;
import megamek.common.annotations.Nullable;

public class BugReportDialog {

    private static final int UNSCALED_WIDTH = 600;
    private static final BugReportMessages I18N = new BugReportMessages();

    private static final String REPORT_LINK_MM = "https://github.com/MegaMek/megamek/issues/new/choose";
    private static final String REPORT_LINK_MML = "https://github.com/MegaMek/megameklab/issues/new/choose";
    private static final String REPORT_LINK_MHQ = "https://github.com/MegaMek/mekhq/issues/new/choose";
    private static final String REPORT_LINK_MM_DATA = "https://github.com/MegaMek/mm-data/issues/new";

    private final Window parent;
    private final JComponent content;

    private Action copySystemDataAction;

    public BugReportDialog(@Nullable Window parent, @Nullable Action copySystemDataAction) {
        this.parent = parent;
        this.copySystemDataAction = copySystemDataAction;
        content = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        int width = UIUtil.scaleForGUI(UNSCALED_WIDTH);
        String ebm = "<html><body width=%d>%s</body></html>".formatted(width, I18N.get("mainText"));
        content.add(new JLabel(ebm), gbc);
        String ebs = "<html><body width=%d>%s</body></html>".formatted(width, I18N.get("secondaryText"));
        content.add(new JLabel(ebs), gbc);
        content.add(buttonPanel(), gbc);
    }

    public void show() {
        JOptionPane.showMessageDialog(parent, content, "Report a bug", JOptionPane.PLAIN_MESSAGE, null);
    }

    private JComponent buttonPanel() {
        JPanel row1 = new JPanel();
        row1.add(new UrlButton(I18N.get("discord.text"), MMConstants.DISCORD_LINK));

        JPanel row2 = new JPanel();
        row2.add(new UrlButton(I18N.get("mm.text"), REPORT_LINK_MM));
        row2.add(new UrlButton(I18N.get("mml.text"), REPORT_LINK_MML));
        row2.add(new UrlButton(I18N.get("mhq.text"), REPORT_LINK_MHQ));
        row2.add(new UrlButton(I18N.get("mmData.text"), REPORT_LINK_MM_DATA));

        JPanel row3 = new JPanel();
        if (copySystemDataAction != null) {
            row3.add(new JButton(copySystemDataAction));
        }

        JComponent rootPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        rootPanel.add(row1);
        rootPanel.add(row2);
        rootPanel.add(row3);
        return rootPanel;
    }

    private static class UrlButton extends JButton {
        UrlButton(String text, String address) {
            super(text);
            setToolTipText(address);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> UIUtil.browse(address));
        }
    }
}
