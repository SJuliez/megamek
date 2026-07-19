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

package megamek.common.strategicBattleSystems;

import megamek.client.commands.ClientCommand;
import megamek.common.Hex;
import megamek.common.board.BoardLocation;
import megamek.common.units.Terrains;

import java.util.List;

/**
 * This is an SBF move step that happens on the surface of a hex, i.e. on the ground or, if the destination has water,
 * on the water surface. This means that the elevation at start and end is considered to be 0.
 */
public class SBFSurfaceMoveStep extends SBFMoveStep {

    protected SBFSurfaceMoveStep(int formationId) {
        super(formationId);
    }

    public static SBFMoveStep createSurfaceMoveStep(SBFGame game, int formationId,
          BoardLocation startingPoint, BoardLocation destination) {
        SBFMoveStep step = new SBFSurfaceMoveStep(formationId);
        step.startingPoint = startingPoint;
        step.destination = destination;
        step.computeStatus(game);
        return step;
    }

    @Override
    protected void computeStatus(SBFGame game) {
        super.computeStatus(game);

        SBFFormation formation = game.getFormation(formationId).orElseThrow();
        if (game.isHostileActiveFormationAt(startingPoint, formation)) {
            mpUsed++;
        }

        boolean isNaval = formation.getMovementMode().isDeepWater();
        boolean isHover = formation.getMovementMode().isHover();
        boolean isWige = formation.getMovementMode() == SBFMovementMode.WIGE;
        boolean wheeledOrHover = isHover || formation.getMovementMode().isWheeled();

        boolean isInfantry = formation.isAnyTypeOf(SBFElementType.CI, SBFElementType.BA);
        boolean isMek = formation.isType(SBFElementType.BM);
        boolean isVehicle = formation.isType(SBFElementType.V);

        Hex startingHex = game.getBoard(startingPoint.boardId()).getHex(startingPoint.coords());
        Hex destinationHex = game.getBoard(destination.boardId()).getHex(destination.coords());
        int levelDifference = Math.abs(destinationHex.getLevel() - startingHex.getLevel());

        if (destinationHex.containsAnyTerrainOf(Terrains.WOODS)) {
            int woodsLevel = destinationHex.terrainLevel(Terrains.WOODS);
            mpUsed += wheeledOrHover ? 2 : woodsLevel;
            mpUsed -= isInfantry ? 1 : 0;
            isIllegal |= (woodsLevel > 1) && isVehicle;
            isIllegal |= (woodsLevel == 3) && !isInfantry;
        }

        if (destinationHex.containsAnyTerrainOf(Terrains.JUNGLE)) {
            int jungleLevel = destinationHex.terrainLevel(Terrains.JUNGLE);
            mpUsed += wheeledOrHover ? 2 : jungleLevel + 1;
            isIllegal |= (jungleLevel > 1) && isVehicle;
            isIllegal |= (jungleLevel == 3) && !isInfantry;
        }

        if (destinationHex.containsAnyTerrainOf(Terrains.RUBBLE)) {
            mpUsed++;
        }

        if (destinationHex.containsAnyTerrainOf(Terrains.ROUGH)) {
            mpUsed += (wheeledOrHover) ? 2 : 1;
        }

        if ((destinationHex.terrainLevel(Terrains.WATER) <= 0) && isNaval) {
            isIllegal = true;
        }

        if (destinationHex.terrainLevel(Terrains.WATER) >= 1) {
            isIllegal = !isHover && !isNaval && !isWige;
        }

        // moving into a hex with a hostile formation is illegal if it breaks the friendly stacking rule, IO:BF p.164
        if (game.isHostileActiveFormationAt(destination, formation)
              && !startingPoint.equals(destination)) {
            List<SBFFormation> friendliesAtDestination = game.getFriendlyFormationsAt(destination,
                  formation.getOwnerId());

            if (friendliesAtDestination.size() >= 2) {
                isIllegal = true;
            } else if (friendliesAtDestination.size() == 1
                  && !formation.getType().isAnyOf(SBFElementType.CI, SBFElementType.BA)
                  && !friendliesAtDestination.getFirst().getType().isAnyOf(SBFElementType.CI, SBFElementType.BA)) {
                // a second friendly formation is only allowed if one of the two is Infantry
                // IO:BF speaks of "Infantry" formations; Using the formation type here; this is lenient
                // and allows formations with some non-infantry elements as long as their overall type is CI/BA; this
                // also allows checking the type even if the elements of the formation are unknown as in some source
                // book scenarios
                isIllegal = true;
            }
        }

        if (levelDifference > 0) {
            mpUsed += levelDifference;
            if (isVehicle || isInfantry) {
                mpUsed += 1;
            }
        }
        isIllegal |= (levelDifference > 2) || ((levelDifference == 2) && !isMek);
    }

    @Override
    public SBFMoveStep copy() {
        SBFMoveStep step = new SBFSurfaceMoveStep(formationId);
        step.startingPoint = startingPoint;
        step.destination = destination;
        step.mpUsed = mpUsed;
        step.isIllegal = isIllegal;
        return step;
    }

    @Override
    public String toString() {
        return ClientCommand.getDirection(startingPoint.coords().direction(destination.coords()));
    }

    @Override
    public int getMovementDirection() {
        return startingPoint.coords().direction(destination.coords());
    }
}
