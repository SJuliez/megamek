package megamek.common;

public class MapLocation {

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
}
