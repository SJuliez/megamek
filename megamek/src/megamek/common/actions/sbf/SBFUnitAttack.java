/*
 * Copyright (C) 2026 The MegaMek Team. All Rights Reserved.
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

package megamek.common.actions.sbf;

public abstract class SBFUnitAttack extends AbstractSBFAttackAction {

    private final int unitNumber;

    /**
     * Creates a standard attack of an SBF Unit on another formation. The unit number identifies the SBF Unit making the
     * attack, i.e. 1 for the first of the formation's units, 2 for the second etc.
     *
     * @param formationId The attacker's ID
     * @param unitNumber  The number of the attacking SBF Unit inside the formation
     * @param targetId    The target's ID
     */
    public SBFUnitAttack(int formationId, int targetId, int unitNumber) {
        super(formationId, targetId);
        this.unitNumber = unitNumber;
    }

    /**
     * Returns the number of the SBF Unit making the attack, i.e. 1 for the first of the formation's units, 2 for the
     * second.
     *
     * @return The unit number within the formation
     */
    public int getUnitNumber() {
        return unitNumber;
    }

    @Override
    public String toString() {
        return "["
              + getClass().getSimpleName()
              + "]: Formation ID "
              + getEntityId()
              + "; Unit #"
              + unitNumber
              + "; Target ID "
              + getTargetId();
    }
}
