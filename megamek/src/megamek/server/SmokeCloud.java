/*
 * MegaMek -
 * Copyright (C) 2000-2005 Ben Mazur (bmazur@sev.org)
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
package megamek.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import megamek.common.Coords;
import megamek.common.MapType;

public class SmokeCloud implements Serializable {

    private static final long serialVersionUID = -8937331680271675046L;

    public static final int SMOKE_NONE = 0;
    public static final int SMOKE_LIGHT = 1;
    public static final int SMOKE_HEAVY = 2;
    public static final int SMOKE_LI_LIGHT = 3;
    public static final int SMOKE_LI_HEAVY = 4;
    public static final int SMOKE_CHAFF_LIGHT = 5;
    public static final int SMOKE_GREEN = 6; // Anti-TSM smoke

    private int smokeDuration;
    private final MapType mapType;
    private final List<Coords> smokeHexList = new ArrayList<>();
    private int smokeLevel;
    private boolean didDrift = false;
    private final int roundOfGeneration;

    public SmokeCloud(Coords coords, int level, int duration, int roundOfGeneration) {
        this(List.of(coords), level, duration, roundOfGeneration, MapType.GROUND);
    }
    
    public SmokeCloud(Coords coords, int level, int duration, int roundOfGeneration, MapType mapType) {
        this(List.of(coords), level, duration, roundOfGeneration, mapType);
    }
    
    public SmokeCloud(List<Coords> coords, int level, int duration, int roundOfGeneration, MapType mapType) {
        this.mapType = mapType;
        smokeDuration = duration;
        smokeLevel = level;
        smokeHexList.addAll(coords);
        this.roundOfGeneration = roundOfGeneration;
    }
    
    public void setSmokeLevel(int level) {
        smokeLevel = Math.min(6, level);
    }
    
    /**
     * Reduces the level of smoke, heavy goes to light, LI heavy goes to LI 
     * light.
     */
    public void reduceSmokeLevel() {
        switch (smokeLevel) {
            case SMOKE_HEAVY:
                smokeLevel = SMOKE_LIGHT;
                break;
            case SMOKE_LI_HEAVY:
                smokeLevel = SMOKE_LI_LIGHT;
                break;
            default:
                smokeLevel = SMOKE_NONE;
                break;
        }
    }
    
    /**
     * Returns the level of smoke, odd levels will correspond to light smoke
     * while even levels will be heavy smoke.
     *
     * @return The smoke level
     */
    public int getSmokeLevel() {
        return smokeLevel;
    }

    /** @return True when this SmokeCloud is at a smoke level of SMOKE_NONE (= 0). */
    public boolean isCompletelyDissipated() {
        return smokeLevel == SMOKE_NONE;
    }

    public MapType getMapType() {
        return mapType;
    }
    
    public List<Coords> getCoordsList() {
        return smokeHexList;
    }

    /** Removes all the previously stored Coords of this SmokeCloud and stores the given Coords instead. */
    public void replaceCoords(Collection<Coords> newCoords) {
        smokeHexList.clear();
        smokeHexList.addAll(newCoords);
    }

    /** @return True when this SmokeCloud has no remaining smoke hex coordinates. */
    public boolean hasNoHexes() {
        return smokeHexList.isEmpty();
    }
    
    public void setDuration(int duration) {
        smokeDuration = duration;
    }
    
    public int getDuration() {
        return smokeDuration;
    }
    
    public void setDrift(boolean drift) {
        didDrift = drift;
    }
    
    public boolean didDrift() {
        return didDrift;
    }

    public int getRoundOfGeneration() {
        return roundOfGeneration;
    }

    public boolean isOnMap(MapType mapType) {
        return this.mapType == mapType;
    }
}