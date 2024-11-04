/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to combine multiple TroopSpaces, for example pod-mounted and fixed, into a troop space with the combined capacity of
 * the individual troop spaces so that the unit can load as much as the that combined capacity even if a loaded unit weighs more than any
 * individual troop space's capacity. Only this combined transporter is added to the unit's list of transporters while the individual troop
 * spaces are stored in this class.
 */
public class CombinedTroopSpace extends TroopSpace {

    private final List<TroopSpace> includedTransports = new ArrayList<>();

    /**
     * Creates a combined troop space of capacity 0 and no actual troop spaces.
     *
     * @see #addTransporter(TroopSpace)
     */
    public CombinedTroopSpace() {
        super(0);
    }

    /**
     * Creates a combined troop space containing the given troop space and having that troop space's capacity.
     *
     * @see #addTransporter(TroopSpace)
     */
    public CombinedTroopSpace(TroopSpace troopSpace) {
        super(troopSpace.totalSpace);
        addTransporter(troopSpace);
    }

    /**
     * Adds a troop space to the combined troop space transport, adding its capacity to this transporter's total capacity. Note that troop
     * spaces cannot be added once this transporter actually holds other units. This method should only be used during construction (loading
     * from file) of a unit.
     *
     * @throws IllegalStateException when trying to add a troop space and a unit is already loaded into this transporter
     * @see #addTransporter(TroopSpace)
     */
    public final void addTransporter(TroopSpace includedTransport) {
        if (!getLoadedUnits().isEmpty()) {
            throw new IllegalStateException("Cannot add troop space to a combined troop space that is already loaded");
        }
        includedTransports.add(includedTransport);
        totalSpace = includedTransports.stream().mapToDouble(Transporter::getUnused).sum();
        currentSpace = totalSpace;
    }

    @Override
    public void resetTransporter() {
        super.resetTransporter();
        includedTransports.clear();
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        includedTransports.forEach(transporter -> transporter.setGame(game));
    }

    /**
     * Returns an unmodifiable view of this combined transporters list of contained troop spaces. Do not modify the returned transports.
     */
    public List<TroopSpace> getIncludedTransports() {
        return Collections.unmodifiableList(includedTransports);
    }
}
