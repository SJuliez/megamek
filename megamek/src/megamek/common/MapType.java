/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common;

import java.io.Serializable;

/**
 * Represents the type of a game map. Note that low atmosphere (aka atmospheric) maps may come with or without
 * terrain and space maps may be high atmospheric maps when they are near a planet.
 * Radar and Capital Radar maps may be used in the Abstract Aerospace Combat rules of SO:AA.
 *
 */
public enum MapType implements Serializable {
    GROUND("G"),
    LOW_ATMOSPHERE("A"),
    SPACE("S"),
    RADAR("R"),
    CAPITAL_RADAR("C");

    private final String code;

    MapType(String code) {
        this.code = code;
    }

    public boolean isSpace() {
        return this == SPACE;
    }

    public boolean isLowAtmo() {
        return this == LOW_ATMOSPHERE;
    }

    public boolean isGround() {
        return this == GROUND;
    }

    public boolean isRadarMap() {
        return this == RADAR;
    }

    public boolean isCapitalRadarMap() {
        return this == CAPITAL_RADAR;
    }

    public String getDisplayName() {
        return Messages.getString("MapType." + name());
    }

    /**
     * Returns a single character code identifying the this MapType (e.g. "G" for GROUND). Can be used
     * e.g. in context menus. Use {@link #mapTypeForCode(String)} to reconstruct the MapType.
     *
     * @return the code character for this MapType
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the MapType represented by the given code. (e.g. GROUND for "G"). Can be used
     * e.g. in context menus.
     *
     * @throws IllegalArgumentException When the given code has no corresponding MapType
     * @return the MapType for the given code
     */
    public static MapType mapTypeForCode(String code) {
        for (MapType mapType: values()) {
            if (mapType.code.equals(code)) {
                return mapType;
            }
        }
        throw new IllegalArgumentException("No MapType exists for the code " + code);
    }

    @Override
    public String toString() {
        switch (this) {
            case CAPITAL_RADAR:
                return "Map Type: Capital Radar";
            case LOW_ATMOSPHERE:
                return "Map Type: Low Atmosphere";
            case SPACE:
                return "Map Type: Space";
            case RADAR:
                return "Map Type: Radar";
            case GROUND:
                return "Map Type: Ground";
            default:
                return "Unknown";
        }
    }
}
