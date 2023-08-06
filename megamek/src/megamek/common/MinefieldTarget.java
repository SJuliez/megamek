/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */

package megamek.common;

import java.util.HashMap;
import java.util.Map;

public class MinefieldTarget implements Targetable {
    private static final long serialVersionUID = 420672189241204590L;

    private final MapLocation mapLocation;

    public MinefieldTarget(MapLocation mapLocation) {
        this.mapLocation = mapLocation;
    }

    public MinefieldTarget(Coords c, MapType mapType) {
        this(new MapLocation(c, mapType));
    }

    @Override
    public int getTargetType() {
        return Targetable.TYPE_MINEFIELD_CLEAR;
    }

    @Override
    public int getId() {
        return locationToId(mapLocation);
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
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Clear Minefield: " + mapLocation.getBoardNum();
    }

    public static int locationToId(MapLocation mapLocation) {
        return HexTarget.locationToId(mapLocation);
    }

    public static MapLocation idToLocation(int id) {
        return HexTarget.idToLocation(id);
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