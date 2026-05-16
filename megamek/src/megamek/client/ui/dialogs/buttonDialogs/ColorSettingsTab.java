package megamek.client.ui.dialogs.buttonDialogs;

import megamek.MegaMek;
import megamek.client.ui.colors.ColorRole;
import megamek.client.ui.colors.ColorSettingPanel;
import megamek.client.ui.util.VerticalStackLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static megamek.client.ui.colors.ColorRole.*;

public class ColorSettingsTab {

    private static final List<ColorRole> swingGuiRoles = List.of(WARNING, CAUTION, MY_FORCE, ALLY, ENEMY);
    private final Map<ColorRole, ColorSettingPanel> rolePanels = new HashMap<>();
    private final JComponent panel = new JPanel(new VerticalStackLayout(12, true));
    private final JScrollPane scrollPane = new JScrollPane(panel);

    ColorSettingsTab() {
        setupUI();

        // Cell content needs to be redrawn actively when settings change
        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                setupUI();
            }
        });
    }

    private void setupUI() {
        panel.removeAll();
        rolePanels.clear();
        for (ColorRole role : swingGuiRoles) {
            rolePanels.put(role, new ColorSettingPanel(role));
            panel.add(rolePanels.get(role));
        }
        panel.revalidate();
        panel.repaint();
    }

    JComponent getComponent() {
        return scrollPane;
    }

    void applySettings() {
        for (ColorRole role : swingGuiRoles) {
            if (rolePanels.get(role).isUsingDefault()) {
                MegaMek.getColorManager().clearColorOverride(role);
            } else {
                MegaMek.getColorManager().setColorOverride(role, rolePanels.get(role).selectedColor());
            }
        }
    }

    void update() {
        for (ColorRole role : swingGuiRoles) {
            rolePanels.get(role).updateState();
        }
    }
}
