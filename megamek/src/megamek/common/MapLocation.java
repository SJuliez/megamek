package megamek.common;

import java.util.Objects;

/**
 * Represents a location (i.e. Coords) on the game map of a specific MapType (e.g. {@link MapType#LOW_ATMOSPHERE}.
 * With a game having multiple maps, this class needs to replace Coords in many methods to identify a specific
 * position of an Entity or event. A game can have only a single map of each type identified in
 * {@link MapType}.
 *
 * @implNote MapLocation is immutable.
 */
public final class MapLocation {

    private final Coords coords;
    private final MapType mapType;

    public MapLocation(Coords coords, MapType mapType) {
        this.coords = coords;
        this.mapType = mapType;
    }

    public Coords getCoords() {
        return coords;
    }

    public MapType getMapType() {
        return mapType;
    }

    @Override
    public String toString() {
        return coords + "; MapType: " + mapType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapLocation that = (MapLocation) o;
        return Objects.equals(coords, that.coords) && (mapType == that.mapType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coords, mapType);
    }

    public String getBoardNum() {
        return coords.getBoardNum() + " (" + mapType.getdisplayName() + ")";
    }

    public String toFriendlyString() {
        return coords.toFriendlyString() + " (" + mapType.getdisplayName() + ")";
    }
}
