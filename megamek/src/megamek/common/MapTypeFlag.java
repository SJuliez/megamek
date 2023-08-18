package megamek.common;

public enum MapTypeFlag {
    NONE,

    /** A space map with gravity is a high-altitude map */
    HIGH_ALTITUDE,

    /** A low atmosphere map with this flag uses no ground terrain (pure sky map) */
    SKY;

    public boolean isHighAltitude() {
        return this == HIGH_ALTITUDE;
    }

    public boolean isSky() {
        return this == SKY;
    }

    public boolean isNone() {
        return this == NONE;
    }
}
