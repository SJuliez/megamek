package megamek.client.ui.colors;

import megamek.client.ui.Messages;

/**
 * Semantic application color roles, such as WARNING or SUCCESS.
 * <p>
 * Note that each such role should clearly identify to both the player when configuring it and any dev when using it
 * what this color is supposed to be for and what this color is probably going to be (as long as the user hasn't set it
 * manually). Therefore, don't use names such as "Block color" or "Other color".
 * <p>
 * Note also that because light/dark modes affect the Swing GUI but don't affect MM's board or MHQ's star map, we need
 * to distinguish between colors for Swing GUI and colors for the other mentioned areas. Roles starting with "MAP"
 * should be applied to maps only; the other values should not be applied to maps.
 * <p>
 * Components should depend on semantic roles instead of concrete color values. This allows themes and user settings to
 * redefine visual appearance centrally.
 * </p>
 */
public enum ColorRole {

    // === Swing GUI Colors ===

    /** Swing GUI: Critical warnings, dangerous actions, destroyed elements, validation failures, negative (money) */
    WARNING,

    /** Swing GUI: Non-critical warnings, damaged elements, unused resources */
    CAUTION,

    /** Swing GUI: Success states, positive (money), healthy, undamaged */
    SUCCESS,

    /** Swing GUI: The player's forces */
    MY_FORCE,

    /** Swing GUI: Allies of the player's forces */
    ALLY,

    /** Swing GUI: Enemies of the player's forces */
    ENEMY,

    // === MAP COLORS ===

    /** Map: Neutral unit info on the map or the unit icon, such as "Hulldown" */
    MAP_UNIT_STATUS_NEUTRAL,

    /** Map: The player's forces */
    MAP_MY_FORCE,

    /** Map: Allies of the player's forces */
    MAP_ALLY,

    /** Map: Enemies of the player's forces */
    MAP_ENEMY;

    public String getDisplayName() {
        return Messages.getString("ColorRole." + name());
    }

    public String getDescription() {
        return Messages.getString("ColorRole." + name() + ".desc");
    }

    public String getExampleText() {
        return Messages.getString("ColorRole." + name() + ".example");
    }
}
