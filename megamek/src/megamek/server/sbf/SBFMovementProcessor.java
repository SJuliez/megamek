/*
 * Copyright (C) 2024-2025 The MegaMek Team. All Rights Reserved.
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

package megamek.server.sbf;

import megamek.common.TargetRollModifier;
import megamek.common.annotations.Nullable;
import megamek.common.compute.Compute;
import megamek.common.rolls.Roll;
import megamek.common.rolls.TargetRoll;
import megamek.common.strategicBattleSystems.SBFFormation;
import megamek.common.strategicBattleSystems.SBFMovePath;
import megamek.common.strategicBattleSystems.SBFMoveStep;
import megamek.logging.MMLogger;

record SBFMovementProcessor(SBFGameManager gameManager) implements SBFGameManagerHelper {
    private static final MMLogger logger = MMLogger.create(SBFMovementProcessor.class);

    void processMovement(SBFMovePath movePath, SBFFormation formation) {
        if (!validatePermitted(movePath, formation)) {
            return;
        }
        // TODO: Check movement again; if illegal, skip the formation's movement

        // Moving out of a hostile occupied hex allows the enemy player to oppose the move if the two formations are
        // not already engaged
        // A formation may have decided to move through multiple hostile hexes in its path
        boolean isOpposed = false;
        for (SBFMoveStep step : movePath.getSteps()) {
            if (game().isHostileActiveFormationAt(step.getStartingPoint(), formation)) {
                // TODO
                try {
                    isOpposed |= gameManager.processOpposeMovementCFR(step.getStartingPoint(), formation);
                    if (isOpposed) {
                        // the moving player can be assumed not to want to engage
                        // maneuver roll!
                        //SBFFormation engangementWinner = doEngagementControlRoll(formation, )
                        // ends movement
                        formation.setPosition(step.getStartingPoint());
                        break;
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

        if (!isOpposed) {
            formation.setPosition(movePath.getLastPosition());
        }
        formation.setJumpUsedThisTurn(movePath.getJumpUsed());
        formation.setDone(true);
        gameManager.sendUnitUpdate(formation);
        gameManager.endCurrentTurn(formation);
    }

    @Nullable
    public static SBFFormation doEngagementControlRoll(SBFFormation formation1, SBFFormation formation2) {
        TargetRoll formation1Target = new TargetRoll(new TargetRollModifier(formation1.getTactics(), "Tactics"));
        if (formation1.isShaken()) {
            formation1Target.addModifier(new TargetRollModifier(1, "Shaken"));
        } else if (formation1.isBroken()) {
            formation1Target.addModifier(new TargetRollModifier(2, "Broken"));
        } else if (formation1.isRouted()) {
            formation1Target.addModifier(new TargetRollModifier(3, "Routed"));
        }

        TargetRoll formation2Target = new TargetRoll(new TargetRollModifier(formation2.getTactics(), "Tactics"));
        if (formation2.isShaken()) {
            formation1Target.addModifier(new TargetRollModifier(1, "Shaken"));
        } else if (formation2.isBroken()) {
            formation1Target.addModifier(new TargetRollModifier(2, "Broken"));
        } else if (formation2.isRouted()) {
            formation1Target.addModifier(new TargetRollModifier(3, "Routed"));
        }

        Roll formation1Roll = Compute.rollD6(2);
        Roll formation2Roll = Compute.rollD6(2);
        int mos1 = formation1Roll.getIntValue() - formation1Target.getValue();
        int mos2 = formation2Roll.getIntValue() - formation2Target.getValue();
        // rolls of 2 are automatically failures, IO:BF p.167
        if (formation1Roll.getIntValue() == 2) {
            mos1 = -1;
        }
        if (formation2Roll.getIntValue() == 2) {
            mos2 = -1;
        }

        if (mos1 < 0 && mos2 < 0) {
            return null;
        } else if (mos1 >= 0 && mos2 < 0) {
            return formation1;
        } else if (mos1 < 0) { // mos2 >= 0 certain here
            return formation2;
        } else if (mos1 == mos2 && formation1.getTactics() != formation2.getTactics()) {
            return (formation1.getTactics() > formation2.getTactics()) ? formation1 : formation2;
        } else {
            // both MoS and tactics tied -> repeat
            return doEngagementControlRoll(formation1, formation2);
        }
    }

    private boolean validatePermitted(SBFMovePath movePath, SBFFormation formation) {
        if (!game().getPhase().isMovement()) {
            logger.error("Server got movement packet in wrong phase!");
            return false;
        } else if (movePath.isIllegal()) {
            logger.error("Illegal move path!");
            return false;
        } else if (formation.isDone()) {
            logger.error("Formation already done!");
            return false;
        }
        return true;
    }

}
