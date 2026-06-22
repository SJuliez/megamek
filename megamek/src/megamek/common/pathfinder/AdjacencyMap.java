/*
 * Copyright (C) 2014-2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MegaMek was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */
package megamek.common.pathfinder;

import java.util.Collection;

/**
 * Factory for retrieving neighbouring edges.
 *
 * @param <E> the type of directed edges used by the graph.
 */
public interface AdjacencyMap<E> {

    /**
     * Returns a collection of adjacent edges for the path finder. Note that in MegaMek, the type of edges (E) is
     * usually not a move step, but a complete move path! An adjacent edge is therefore a move path that extends the
     * given path e by a single new step. The destination hex of that step need not be adjacent to the previous
     * destination. E.g., in a VTOL movement step that goes up one level the destination is the same hex but the move
     * path is "adjacent" as it adds one UP step.
     *
     * @param e a directed edge (usually a move path)
     *
     * @return all the edges that lead from destination node of e
     */
    Collection<E> getAdjacent(E e);
}
