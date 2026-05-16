package megamek.client.ui.colors;

import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a single configurable color entry.
 * <p>
 * Each setting consists of:
 * </p>
 * <ul>
 *     <li>A dynamic default color supplier</li>
 *     <li>An optional user override</li>
 * </ul>
 * <p>
 * If no override is present, the effective color is resolved from
 * the default supplier each time {@link #get()} is called.
 */
final class ColorSetting {

    /**
     * Supplier providing the adaptive default color.
     */
    private final Supplier<Color> defaultSupplier;

    /**
     * Explicit user override.
     *
     * <p>
     * A {@code null} value means: "Use adaptive default".
     * </p>
     */
    private Color override;

    /**
     * Creates a new color setting. The default color supplier is used if the user override is not present.
     *
     * @param defaultSupplier supplier for the adaptive default color
     */
    ColorSetting(Supplier<Color> defaultSupplier) {
        this.defaultSupplier = Objects.requireNonNull(defaultSupplier);
    }

    /**
     * Returns the effective color.
     *
     * <p>
     * If a user override exists, it is returned. Otherwise, the current adaptive default color is returned.
     * </p>
     *
     * @return effective color
     */
    Color get() {
        return override != null ? override : defaultSupplier.get();
    }

    Color getDefault() {
        return defaultSupplier.get();
    }

    /**
     * Returns whether this setting currently uses its adaptive default.
     *
     * @return {@code true} if no override is set
     */
    boolean isUsingDefault() {
        return override == null;
    }

    /**
     * Sets a fixed user override color.
     *
     * @param override override color
     */
    void setOverride(Color override) {
        this.override = override;
    }

    /**
     * Removes the user override and restores adaptive default behavior.
     */
    void clearOverride() {
        this.override = null;
    }
}
