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

/**
 * A type of game turn that allows only one specific entity to trigger their Anti-Battle Armor
 * pods against attacking infantry/BA.
 */
public class TriggerBPodTurn extends SpecificEntityTurn {
    private static final long serialVersionUID = -9082006433957145275L;
    private String attackType;

    public TriggerBPodTurn(int playerId, int entityId, String attackType) {
        super(playerId, entityId);
        this.attackType = attackType;
    }

    public String getAttackType() {
        return attackType;
    }

    /**
     * @return true if the entity matches this game turn, even if the entity has declared an
     * action.
     */
    @Override
    public boolean isValidEntity(Entity entity, Game game, boolean useValidNonInfantryCheck) {
        final boolean oldDone = entity.done;
        entity.done = false;
        final boolean result = super.isValidEntity(entity, game, useValidNonInfantryCheck);
        entity.done = oldDone;
        return result;
    }
}
