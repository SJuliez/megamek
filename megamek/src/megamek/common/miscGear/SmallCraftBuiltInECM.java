/*
 * Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
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
 */
package megamek.common.miscGear;

import megamek.common.*;

/**
 * In a game, this ECM is added to military SmallCraft to indicate their built-in ECM capability, SO:AA p.99.
 */
public class SmallCraftBuiltInECM extends MiscType {

    public SmallCraftBuiltInECM() {
        name = "SmallCraft Built-In ECM";
        setInternalName(EquipmentTypeLookup.SC_BUILTIN_ECM);
        tonnage = 0;
        criticals = 0;
        cost = 0;
        bv = 0;
        hittable = false;
        flags = flags.or(F_ECM).or(F_SC_EQUIPMENT).or(MiscTypeFlag.F_GAMEPLAY_ONLY);
        setModes("ECM");
        setInstantModeSwitch(false);
        rulesRefs = "99, SO:AA";
        // Tech advancement is the same as SmallCraft unit type TA
        techAdvancement.setTechBase(TECH_BASE_ALL)
            .setAdvancement(DATE_NONE, 2350, 2400).setISApproximate(false, true, false)
            .setProductionFactions(F_TH).setTechRating(RATING_D)
            .setAvailability(RATING_D, RATING_E, RATING_D, RATING_D)
            .setStaticTechLevel(SimpleTechLevel.STANDARD);
    }
}
