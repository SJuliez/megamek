/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HexTarget implements Targetable {
    private static final long serialVersionUID = -5742445409423125942L;

    private final int targetType;
    private final MapLocation mapLocation;

    /**
     * Creates a new HexTarget for the given {@link Coords} on the map of the given {@link MapType} and a
     * type defined in {@link Targetable}.
     */
    public HexTarget(Coords c, MapType mapType, int nType) {
        this(new MapLocation(c, mapType), nType);
    }

    /**
     * Creates a new HexTarget for the given {@link MapLocation} and a type defined in
     * {@link Targetable}.
     */
    public HexTarget(MapLocation mapLocation, int nType) {
        this.mapLocation = Objects.requireNonNull(mapLocation);
        Objects.requireNonNull(mapLocation.getCoords());
        Objects.requireNonNull(mapLocation.getMapType());
        targetType = nType;
    }

    @Override
    public int getTargetType() {
        return targetType;
    }

    @Override
    public int getId() {
        return HexTarget.locationToId(mapLocation);
    }

    @Override
    public int getOwnerId() {
        return Player.PLAYER_NONE;
    }

    @Override
    public int getStrength() {
        return 0;
    }

    @Override
    public Coords getPosition() {
        return mapLocation.getCoords();
    }

    @Override
    public Map<Integer, Coords> getSecondaryPositions() {
        return new HashMap<>();
    }

    @Override
    public int relHeight() {
        return getHeight() + getElevation();
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getElevation() {
        return 0;
    }

    @Override
    public boolean isImmobile() {
        return ((targetType != Targetable.TYPE_HEX_BOMB) && (targetType != Targetable.TYPE_HEX_AERO_BOMB));
    }

    @Override
    public String getDisplayName() {
        final String name;
        switch (targetType) {
            case Targetable.TYPE_FLARE_DELIVER:
                name = Messages.getString("HexTarget.DeliverFlare");
                break;
            case Targetable.TYPE_MINEFIELD_DELIVER:
                name = Messages.getString("HexTarget.DeliverMinefield");
                break;
            case Targetable.TYPE_HEX_BOMB:
            case Targetable.TYPE_HEX_AERO_BOMB:
                name = Messages.getString("HexTarget.Bomb");
                break;
            case Targetable.TYPE_HEX_CLEAR:
                name = Messages.getString("HexTarget.Clear");
                break;
            case Targetable.TYPE_HEX_IGNITE:
                name = Messages.getString("HexTarget.Ignite");
                break;
            case Targetable.TYPE_HEX_ARTILLERY:
                name = Messages.getString("HexTarget.Artillery");
                break;
            case Targetable.TYPE_HEX_EXTINGUISH:
                name = Messages.getString("HexTarget.Extinguish");
                break;
            case Targetable.TYPE_HEX_SCREEN:
                name = Messages.getString("HexTarget.Screen");
                break;
            case Targetable.TYPE_HEX_TAG:
                name = Messages.getString("HexTarget.Tag");
                break;
            default:
                name = "";
                break;
        }
        return "Hex: " + mapLocation.getCoords().getBoardNum() + name;
    }

    /**
     * Could more efficiently encode this by
     * partitioning the binary representation, but this is more human readable
     * and still allows for a 9999x20000 hex map.
     */
    public static int locationToId(MapLocation mapLocation) {
        return mapLocation.getCoords().getY() * 100000
                + mapLocation.getCoords().getX() * 10
                + mapLocation.getMapType().ordinal();
    }

    public static MapLocation idToLocation(int id) {
        int y = id / 100000;
        int x = (id - y * 100000) / 10;
        int ordinal = id - y * 100000 - x * 10;
        return new MapLocation(new Coords(x, y), MapType.values()[ordinal]);
    }

    @Override
    public int sideTable(Coords src) {
        return ToHitData.SIDE_FRONT;
    }

    @Override
    public int sideTable(Coords src, boolean usePrior) {
        return sideTable(src);
    }

    @Override
    public boolean isOffBoard() {
        return false;
    }

    @Override
    public boolean isAirborne() {
        return false;
    }

    @Override
    public boolean isAirborneVTOLorWIGE() {
        return false;
    }

    @Override
    public int getAltitude() {
        return 0;
    }
    
    @Override
    public boolean isEnemyOf(Entity other) {
        return true;
    }

    @Override
    public MapLocation getMapLocation() {
        return mapLocation;
    }
}
