package megamek.client.ui.colors;

import com.formdev.flatlaf.FlatLaf;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.util.UIUtil;
import megamek.logging.MMLogger;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static megamek.client.ui.colors.ColorRole.*;

/**
 * Central manager for semantic application colors that supports:
 * <ul>
 *     <li>Light and dark modes</li>
 *     <li>Mode-dependent default colors</li>
 *     <li>User-defined color overrides</li>
 *     <li>Change notifications for UI updates</li>
 * </ul>
 * <p>
 * Colors are addressed by semantic roles such as {@link ColorRole#WARNING} or {@link ColorRole#SUCCESS}.
 * UI components should request colors through this manager rather than storing raw {@link Color} instances permanently.
 * <p>
 * A color can either:
 * <ul>
 *     <li>Use its adaptive default value</li>
 *     <li>Use a fixed user override</li>
 * </ul>
 *
 * <p>
 * Adaptive defaults are implemented using {@link Supplier} instances, allowing defaults to dynamically depend on:
 * </p>
 * <ul>
 *     <li>Current theme</li>
 *     <li>Look and feel</li>
 *     <li>Accessibility settings</li>
 *     <li>Other runtime conditions</li>
 * </ul>
 */
@SuppressWarnings("unused")
public final class ColorManager {

    private static final MMLogger LOGGER = MMLogger.create(ColorManager.class);

    private static final String LEGACY_INPUT_DONE = "LegacyInputDone";

    private static final Color LIGHT_UI_YELLOW = new Color(140, 100, 0);
    private static final Color DARK_UI_YELLOW = new Color(200, 200, 60);

    public static final String PROPERTY_COLORS_CHANGED = "colors";
    private static final ColorsPreferences colorsPreferences = new ColorsPreferences();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * This map stores the overrides, i.e., the colors that are set by the user and don't use the defaults.
     */
    private final Map<ColorRole, ColorSetting> settings = new EnumMap<>(ColorRole.class);

    /**
     * Creates the manager and initializes built-in color roles.
     * <p>
     * Note that for Swing GUI colors, e.g., for the lobby or in MML and MekHQ, the defaults should always at least
     * differentiate between light and dark themes (unless the color works equally well for both).
     * <p>
     * Maybe at some point in the future MM wants to support some other style, such as high contrast or color blindness
     * special values; in that case, the default supplier can also consider such a setting when returning its color
     * value.
     * <p>
     * For colors that are used where light/dark Swing LAF is irrelevant (the MM game board or the MHQ star map), the
     * default supplier can supply just a single color.
     * <p>
     * When the user has set a color, only that color is returned, and the default suppliers and whatever logic they
     * contain are no longer considered. The user is responsible for choosing suitable colors.
     */
    public ColorManager() {

        settings.put(WARNING, new ColorSetting(() -> isDark() ? new Color(255, 120, 120) : new Color(200, 40, 40)));
        settings.put(SUCCESS, new ColorSetting(() -> isDark() ? new Color(120, 220, 140) : new Color(40, 160, 60)));
        settings.put(CAUTION, new ColorSetting(() -> isDark() ? DARK_UI_YELLOW : LIGHT_UI_YELLOW));
        settings.put(MY_FORCE, new ColorSetting(() -> isDark() ? new Color(150, 220, 150) : new Color(50, 150, 50)));
        settings.put(ALLY, new ColorSetting(() -> isDark() ? new Color(150, 220, 220) : new Color(50, 150, 150)));
        settings.put(ENEMY, new ColorSetting(() -> isDark() ? new Color(220, 140, 140) : new Color(170, 60, 60)));

        settings.put(MAP_UNIT_STATUS_NEUTRAL, new ColorSetting(() -> new Color(160, 180, 50)));
        settings.put(MAP_MY_FORCE, new ColorSetting(() -> new Color(120, 220, 140)));
        settings.put(MAP_ALLY, new ColorSetting(() -> new Color(150, 220, 220)));
        settings.put(MAP_ENEMY, new ColorSetting(() -> new Color(220, 140, 140)));

        // Legacy support: read in colors from clientsettings.xml (GUIPrefs) and set them; these colors are only stored
        // when the user had configured the color
        if (!colorsPreferences.hasProperty(LEGACY_INPUT_DONE) || !colorsPreferences.getBoolean(LEGACY_INPUT_DONE)) {
            readLegacyValues();

            // remember that legacy values have been read; this is only done once
            colorsPreferences.setValue(LEGACY_INPUT_DONE, true);
        }

        // Read in the current configuration (override legacy values)
        for (ColorRole role : ColorRole.values()) {
            if (colorsPreferences.hasProperty(role.name()) && !colorsPreferences.getString(role.name()).isBlank()) {
                try {
                    // set directly, no event and no re-save is needed here
                    settings.get(role).setOverride(colorsPreferences.getColor(role.name()));
                } catch (Exception e) {
                    LOGGER.error(e, "Error reading color override for role {}", role);
                }
            }
        }
    }

    private void readLegacyValues() {
        GUIPreferences GUIP = GUIPreferences.getInstance();
        if (GUIP.hasProperty(GUIPreferences.WARNING_COLOR)) {
            setColorOverride(WARNING, GUIP.getWarningColor());
        }
        if (GUIP.hasProperty(GUIPreferences.CAUTION_COLOR)) {
            setColorOverride(CAUTION, GUIP.getCautionColor());
        }
    }

    private boolean isDark() {
        return FlatLaf.isLafDark();
    }

    /**
     * Returns the effective color for the specified color role. The effective color is a user-set color for this role
     * if one has been set, or a default color supplied for this role, if not.
     *
     * @param role semantic color role
     *
     * @return The effective color for the role
     */
    public Color get(ColorRole role) {
        return settings.get(role).get();
    }

    /**
     * Returns the effective color for the specified color role. The effective color is a user-set color for this role
     * if one has been set, or a default color supplied for this role, if not.
     *
     * @param role semantic color role
     *
     * @return The effective color for the role
     */
    public Color getDefault(ColorRole role) {
        return settings.get(role).getDefault();
    }

    /**
     * Returns the effective color for the specified role as a hex String, e.g., ffff00 for yellow.
     *
     * @param role semantic color role
     *
     * @return effective color
     */
    public String asHex(ColorRole role) {
        return UIUtil.toColorHexString(get(role));
    }

    /**
     * Returns whether the specified role currently uses its adaptive default.
     *
     * @param role semantic color role
     *
     * @return {@code true} if no override exists
     */
    public boolean isUsingDefault(ColorRole role) {
        return settings.get(role).isUsingDefault();
    }

    /**
     * Sets a fixed user override color for a role. Once an override is set, the color no longer adapts automatically to
     * theme (or other) changes.
     *
     * @param role  semantic color role
     * @param color the new user-set color
     */
    public void setColorOverride(ColorRole role, Color color) {
        settings.get(role).setOverride(color);
        colorsPreferences.setColor(role.name(), color);
        pcs.firePropertyChange(PROPERTY_COLORS_CHANGED, null, role);
    }

    /**
     * Removes the override for a role and restores adaptive default behavior.
     *
     * @param role semantic color role
     */
    public void clearColorOverride(ColorRole role) {
        settings.get(role).clearOverride();
        colorsPreferences.setValue(role.name(), "");
        pcs.firePropertyChange(PROPERTY_COLORS_CHANGED, null, role);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
