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
    private final BoardLocation boardLocation;

    /**
     * Creates a new HexTarget for the given {@link Coords} on the map of the given {@link MapType} and a
     * type defined in {@link Targetable}.
     */
    public HexTarget(Coords c, int boardId, int nType) {
        this(new BoardLocation(c, boardId), nType);
    }

    /**
     * Creates a new HexTarget for the given {@link BoardLocation} and a type defined in
     * {@link Targetable}.
     */
    public HexTarget(BoardLocation boardLocation, int nType) {
        this.boardLocation = Objects.requireNonNull(boardLocation);
        targetType = nType;
    }

    @Override
    public int getTargetType() {
        return targetType;
    }

    @Override
    public int getId() {
        return HexTarget.locationToId(boardLocation);
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
        return boardLocation.getCoords();
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
        return "Hex: " + boardLocation.getCoords().getBoardNum() + name;
    }

    /** Allows a 9999 x 999 map and board IDs up to 200 */
    public static int locationToId(BoardLocation boardLocation) {
        return boardLocation.getBoardId() * 10000000
                + boardLocation.getCoords().getY() * 10000
                + boardLocation.getCoords().getX();
    }

    public static BoardLocation idToLocation(int id) {
        int boardId = id / 10000000;
        id -= boardId * 10000000;
        int y = id / 10000;
        int x = id - y * 10000;
        return new BoardLocation(new Coords(x, y), boardId);
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
    public BoardLocation getBoardLocation() {
        return boardLocation;
    }
}
