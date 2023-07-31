package megamek.common;

public enum MapTypeFlag {
    NONE,

    /** A space map with gravity is a high atmosphere map */
    HIGH_ATMOSPHERE,

    /** A low atmosphere map with this flag uses no ground terrain (pure sky map) */
    SKY;

    public boolean isHighAtmosphere() {
        return this == HIGH_ATMOSPHERE;
    }

    public boolean isSky() {
        return this == SKY;
    }

    public boolean isNone() {
        return this == NONE;
    }
}
