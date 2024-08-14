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
package megamek.server.scriptedevent;

import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.MechSummaryCache;
import megamek.logging.MMLogger;
import megamek.server.GameManager;
import megamek.server.IGameManager;
import megamek.server.trigger.KilledUnitsTrigger;
import megamek.server.trigger.OnceTrigger;
import megamek.server.trigger.Trigger;

public class UnitRenewalEvent implements TriggeredActiveEvent {

    private static final MMLogger logger = MMLogger.create(UnitRenewalEvent.class);

    private final int unitId;
    private final Trigger trigger;

    /**
     * Creates an event for the given unit that will continually renew this unit. If it or any of its
     * descendants is destroyed, a new copy of that unit will be available for deployment the following round.
     *
     * @param unitId The unit to keep renewed
     */
    public UnitRenewalEvent(int unitId) {
        this.unitId = unitId;
        trigger = new OnceTrigger(new KilledUnitsTrigger(unitId));
    }

    @Override
    public void process(IGameManager gameManager) {
        if (gameManager.getGame() instanceof Game twGame && gameManager instanceof GameManager twGameManager) {
            try {
                Entity unit = twGame.getEntityFromAllSources(unitId);
                Entity replacement = MechSummaryCache.getInstance().getMech(unit.getShortNameRaw()).loadEntity();
                if (replacement != null) {
                    replacement.setId(unitId);
                    twGame.addEntity(replacement, false);
                    replacement.setOwnerId(unit.getOwnerId());
                    replacement.setDeployRound(twGame.getCurrentRound() + 1);
                    replacement.setStartingPos(unit.getStartingPos());
                    gameManager.send(twGameManager.createAddEntityPacket(replacement.getId()));
                    twGame.addScriptedEvent(new UnitRenewalEvent(replacement.getId()));
                }
            } catch (Exception ex) {
                logger.error("Unit to renew was not found or could not be loaded");
            }
        } else {
            logger.warn("UnitRenewalEvent can currently only be used in TW games");
        }
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }
}
