package megamek.client.ui;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class ThemeColorManager {

    public static final String PROPERTY_COLORS_CHANGED = "colors";

    public enum ThemeMode {
        LIGHT,
        DARK
    }

    public enum ColorRole {
        WARNING,
        CAUTION,
        SUCCESS,
        INFO,
        HIGHLIGHT
    }

    /**
     * Represents one configurable color entry.
     * <p>
     * If override == null: use default supplier
     * <p>
     * Otherwise: use explicit user-selected color
     */
    public static final class ColorSetting {

        private final Supplier<Color> defaultSupplier;

        private Color override;

        public ColorSetting(Supplier<Color> defaultSupplier) {
            this.defaultSupplier = Objects.requireNonNull(defaultSupplier);
        }

        public Color get() {
            return override != null
                  ? override
                  : defaultSupplier.get();
        }

        public boolean isUsingDefault() {
            return override == null;
        }

        public Color getOverride() {
            return override;
        }

        public void setOverride(Color override) {
            this.override = override;
        }

        public void clearOverride() {
            this.override = null;
        }
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final Map<ColorRole, ColorSetting> settings =
          new EnumMap<>(ColorRole.class);

    private ThemeMode themeMode = ThemeMode.LIGHT;

    public ThemeColorManager() {

        settings.put(ColorRole.WARNING,
              new ColorSetting(() -> isDark()
                    ? new Color(255, 120, 120)
                    : new Color(200, 40, 40)));

        settings.put(ColorRole.SUCCESS,
              new ColorSetting(() -> isDark()
                    ? new Color(120, 220, 140)
                    : new Color(40, 160, 60)));

        settings.put(ColorRole.INFO,
              new ColorSetting(() -> isDark()
                    ? new Color(120, 180, 255)
                    : new Color(40, 100, 220)));

        settings.put(ColorRole.HIGHLIGHT,
              new ColorSetting(() -> isDark()
                    ? new Color(80, 120, 200)
                    : new Color(180, 210, 255)));
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        if (this.themeMode == themeMode) {
            return;
        }

        ThemeMode old = this.themeMode;
        this.themeMode = themeMode;

        pcs.firePropertyChange("themeMode", old, themeMode);
        pcs.firePropertyChange(PROPERTY_COLORS_CHANGED, null, null);
    }

    public boolean isDark() {
        return themeMode == ThemeMode.DARK;
    }

    public Color get(ColorRole role) {
        return settings.get(role).get();
    }

    public boolean isUsingDefault(ColorRole role) {
        return settings.get(role).isUsingDefault();
    }

    public void setColorOverride(ColorRole role, Color color) {
        settings.get(role).setOverride(color);
        pcs.firePropertyChange(PROPERTY_COLORS_CHANGED, null, role);
    }

    public void clearColorOverride(ColorRole role) {
        settings.get(role).clearOverride();
        pcs.firePropertyChange(PROPERTY_COLORS_CHANGED, null, role);
    }

    public Color getColorOverride(ColorRole role) {
        return settings.get(role).getOverride();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Example usage.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            ThemeColorManager colors = new ThemeColorManager();

            JLabel label = new JLabel("Warning Text");
            label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));

            colors.addPropertyChangeListener(e -> label.setForeground(colors.get(ColorRole.WARNING)));

            JButton toggleTheme = new JButton("Toggle Theme");

            toggleTheme.addActionListener(e -> {
                colors.setThemeMode(
                      colors.isDark()
                            ? ThemeMode.LIGHT
                            : ThemeMode.DARK);
            });

            JButton customColor = new JButton("Custom Purple");

            customColor.addActionListener(e -> {
                colors.setColorOverride(
                      ColorRole.WARNING,
                      new Color(180, 80, 255));
            });

            JButton reset = new JButton("Reset To Default");

            reset.addActionListener(e -> {
                colors.clearColorOverride(ColorRole.WARNING);
            });

            JPanel controls = new JPanel();
            controls.add(toggleTheme);
            controls.add(customColor);
            controls.add(reset);

            JFrame frame = new JFrame("Theme Color Demo");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            frame.add(label, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);

            frame.setSize(600, 200);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
