/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
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
package megamek.server;

import megamek.common.*;
import megamek.common.options.OptionsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a helper class for the GameManager for dealing with SO:AA p.105 Advanced Sensors in space combat.
 */
public final class GameManagerAdvancedSensorHelper {

    public static boolean isEligibleForSpacecraftDetection(Entity entity) {
        //Don't process for invalid units
        //in the case of squadrons and transports, we want the 'host'
        //unit, not the component entities
        return (entity != null) && (entity.getPosition() != null) && !entity.isDestroyed() && !entity.isDoomed()
                && !entity.isOffBoard() && !entity.isPartOfFighterSquadron() && entity.isSpaceborne()
                && (entity.getTransportId() == Entity.NONE);
    }

    /**
     * Called at the start and end of movement. Determines if an entity
     * has been detected and/or had a firing solution calculated
     */
    static void detectSpacecraft(Game game) {
        // Don't bother if we're not in space or if the game option isn't on
        if (!game.usesSpaceMap()
                || !game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_ADVANCED_SENSORS)) {
            return;
        }

        List<Entity> eligibleSpaceBorneUnits = game.getEntitiesVector().stream()
                .filter(GameManagerAdvancedSensorHelper::isEligibleForSpacecraftDetection).collect(Collectors.toList());

        //Now, run the detection rolls
        for (Entity detector : eligibleSpaceBorneUnits) {
            for (Entity target : game.getSpaceBorneEntities()) {
                //Once a target is detected, we don't need to detect it again
                if (detector.hasSensorContactFor(target.getId())) {
                    continue;
                }
                // Only process for enemy units
                if (!detector.isEnemyOf(target)) {
                    continue;
                }
                //If we successfully detect the enemy, add it to the appropriate detector's sensor contacts list
                if (calcSensorContact(game, detector, target)) {
                    game.getEntity(detector.getId()).addSensorContact(target.getId());
                    //If detector is part of a C3 network, share the contact
                    if (detector.hasNavalC3()) {
                        for (Entity c3NetMate : game.getC3NetworkMembers(detector)) {
                            game.getEntity(c3NetMate.getId()).addSensorContact(target.getId());
                        }
                    }
                }
            }
        }
        //Now, run the firing solution calculations
        for (Entity detector : eligibleSpaceBorneUnits) {
            for (int targetId : detector.getSensorContacts()) {
                Entity target = game.getEntity(targetId);
                //if we already have a firing solution, no need to process a new one
                if (detector.hasFiringSolutionFor(targetId)) {
                    continue;
                }
                //Don't process for invalid units
                //in the case of squadrons and transports, we want the 'host'
                //unit, not the component entities
                if (!isEligibleForSpacecraftDetection(target)) {
                    continue;
                }
                // Only process for enemy units
                if (!detector.isEnemyOf(target)) {
                    continue;
                }
                //If we successfully lock up the enemy, add it to the appropriate detector's firing solutions list
                if (calcFiringSolution(game, detector, target)) {
                    game.getEntity(detector.getId()).addFiringSolution(targetId);
                }
            }
        }
    }

    /**
     * Called at the end of movement. Determines if an entity
     * has moved beyond sensor range
     */
    static void updateSpacecraftDetection(Game game) {
        // Don't bother if we're not in space or if the game option isn't on
        if (!game.usesSpaceMap()
                || !game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_ADVANCED_SENSORS)) {
            return;
        }
        //Run through our list of units and remove any entities from the plotting board that have moved out of range
        for (Entity detector : game.getSpaceBorneEntities()) {
            updateFiringSolutions(game, detector);
            updateSensorContacts(game, detector);
        }
    }

    /**
     * Updates an entity's firingSolutions, removing any objects that no longer meet criteria for being
     * tracked as targets. Also, if the detecting entity no longer meets criteria for having firing solutions,
     * empty the list. We wouldn't want a dead ship to be providing NC3 data, now would we...
     */
    public static void updateFiringSolutions(Game game, Entity detector) {
        List<Integer> toRemove = new ArrayList<>();
        //Flush the detecting unit's firing solutions if any of these conditions applies
        if (!isEligibleForSpacecraftDetection(detector)) {
            detector.clearFiringSolutions();
            return;
        }
        for (int id : detector.getFiringSolutions()) {
            Entity target = game.getEntity(id);
            //The target should be removed if it's off the board for any of these reasons
            if (!isEligibleForSpacecraftDetection(target)) {
                toRemove.add(id);
                continue;
            }
            Coords targetPos = target.getPosition();
            int distance = detector.getPosition().distance(targetPos);
            //Per SO p119, optical firing solutions are lost if the target moves beyond 1/10 max range
            if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_THERMAL
                    && distance > Sensor.ASF_OPTICAL_FIRING_SOLUTION_RANGE) {
                toRemove.add(id);
            } else if (detector.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_THERMAL
                    && distance > Sensor.LC_OPTICAL_FIRING_SOLUTION_RANGE) {
                toRemove.add(id);
                //For ASF sensors, make sure we're using the space range of 555...
            } else if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_SENSOR
                    && distance > Sensor.ASF_RADAR_MAX_RANGE) {
                toRemove.add(id);
            } else {
                //Radar firing solutions are only lost if the target moves out of range
                if (distance > detector.getActiveSensor().getRangeByBracket()) {
                    toRemove.add(id);
                }
            }
        }
        detector.removeFiringSolution(toRemove);
    }

    /**
     * Updates an entity's sensorContacts, removing any objects that no longer meet criteria for being
     * tracked. Also, if the detecting entity no longer meets criteria for having sensor contacts,
     * empty the list. We wouldn't want a dead ship to be providing sensor data, now would we...
     */
    public static void updateSensorContacts(Game game, Entity detector) {
        List<Integer> toRemove = new ArrayList<>();
        //Flush the detecting unit's sensor contacts if any of these conditions applies
        if (!isEligibleForSpacecraftDetection(detector)) {
            detector.clearSensorContacts();
            return;
        }
        for (int id : detector.getSensorContacts()) {
            Entity target = game.getEntity(id);
            //The target should be removed if it's off the board for any of these reasons
            if (!isEligibleForSpacecraftDetection(target)) {
                toRemove.add(id);
                continue;
            }
            //And now calculate whether or not the target has moved out of range. Per SO p117-119,
            //sensor contacts remain tracked on the plotting board until this occurs.
            Coords targetPos = target.getPosition();
            int distance = detector.getPosition().distance(targetPos);
            if (distance > detector.getActiveSensor().getRangeByBracket()) {
                toRemove.add(id);
            }
        }
        detector.removeSensorContact(toRemove);
    }

    /**
     * If the game is in space, "visual range" represents a firing solution as defined in SO starting on p117
     * Also, in most cases each target must be detected with sensors before it can be seen, so we need to make
     * sensor rolls for detection. This should only be used if Tacops sensor rules are in use.
     * This requires line of sight effects to determine if there are
     * certain intervening obstructions, like sensor shadows, asteroids and that sort of thing, that can reduce visual
     * range. Since repeated LoSEffects computations can be expensive, it is
     * possible to pass in the LosEffects, since they are commonly already
     * computed when this method is called.
     *
     * @param game The current {@link Game}
     * @param detector the entity making a sensor scan
     * @param target the entity we're trying to spot
     * @return True when an firing solution is found for the given units
     */
    public static boolean calcFiringSolution(Game game, Entity detector, Targetable target) {
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            Entity te = (Entity) target;
            if (te.isOffBoard()) {
                return false;
            }
        }

        //NPE check. Fighter squadrons don't start with sensors, but pick them up from the component fighters each round
        if (detector.getActiveSensor() == null) {
            return false;
        }

        //ESM sensor can't produce a firing solution
        if (detector.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_ESM) {
            return false;
        }
        Coords targetPos = target.getPosition();
        int distance = detector.getPosition().distance(targetPos);
        int roll = Compute.d6(2);
        int tn = detector.getCrew().getPiloting();
        int autoVisualRange = 1;
        int outOfVisualRange = (detector.getActiveSensor().getRangeByBracket());
        int rangeIncrement = (int) Math.ceil(outOfVisualRange / 10.0);

        //A bit of a hack here. "Aero Sensors" return the ground range, because Sensor doesn't know about Game or Entity
        //to do otherwise. We need to use the space range instead.
        if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_SENSOR) {
            outOfVisualRange = Sensor.ASF_RADAR_MAX_RANGE;
            rangeIncrement = Sensor.ASF_RADAR_AUTOSPOT_RANGE;
        }

        if (distance > outOfVisualRange) {
            return false;
        }

        if (detector instanceof Aero) {
            Aero aero = (Aero) detector;
            //Account for sensor damage
            if (aero.isAeroSensorDestroyed()) {
                return false;
            } else {
                tn += aero.getSensorHits();
            }
        }

        //Targets at 1/10 max range are automatically detected
        if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_SENSOR) {
            autoVisualRange = Sensor.ASF_RADAR_AUTOSPOT_RANGE;
        } else if (detector.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_RADAR) {
            autoVisualRange = Sensor.LC_RADAR_AUTOSPOT_RANGE;
        } else if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_THERMAL) {
            autoVisualRange = Sensor.ASF_OPTICAL_FIRING_SOLUTION_RANGE;
        } else if (detector.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_THERMAL) {
            autoVisualRange = Sensor.LC_OPTICAL_FIRING_SOLUTION_RANGE;
        }

        if (distance <= autoVisualRange) {
            return true;
        }

        //Apply Sensor Geek SPA, if present
        if (detector.hasAbility(OptionsConstants.UNOFF_SENSOR_GEEK)) {
            tn -= 2;
        }

        //Otherwise, we add +1 to the tn for detection for each increment of the autovisualrange between detector and target
        tn += (distance / rangeIncrement);

        // Apply ECM/ECCM effects
        if (game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_ECM)) {
            tn += calcSpaceECM(detector, target);
        }

        // Apply large craft sensor shadows
        if (game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_SENSOR_SHADOW)) {
            tn += calcSensorShadow(game, detector, target);
        }

        //Apply modifiers for detector's equipment
        //-2 for a working Large NCSS
        if (detector.hasWorkingMisc(MiscType.F_LARGE_COMM_SCANNER_SUITE)) {
            tn -= 2;
        }
        //-1 for a working Small NCSS
        if (detector.hasWorkingMisc(MiscType.F_SMALL_COMM_SCANNER_SUITE)) {
            tn -= 1;
        }
        // -2 for any type of BAP or EW Equipment. ECM is already accounted for, so don't let the BAP check do that
        if (detector.hasWorkingMisc(MiscType.F_EW_EQUIPMENT)
                || detector.hasBAP(false)) {
            tn -= 2;
        }

        // Now, determine if we've detected the target this round
        return roll >= tn;
    }

    /**
     * Determines whether we have an "object" detection as defined in SO's Advanced Sensors rules starting on p117
     *
     * @param game The current {@link Game}
     * @param detector the entity making a sensor scan
     * @param target the entity we're trying to spot
     * @return True when an "Object" (sensor contact) is found for the given units
     */
    public static boolean calcSensorContact(Game game, Entity detector, Targetable target) {
        // NPE check. Fighter squadrons don't start with sensors, but pick them up from the component fighters each round
        if (detector.getActiveSensor() == null) {
            return false;
        }
        Coords targetPos = target.getPosition();
        int distance = detector.getPosition().distance(targetPos);
        int roll = Compute.d6(2);
        int tn = detector.getCrew().getPiloting();
        int maxSensorRange = detector.getActiveSensor().getRangeByBracket();
        int rangeIncrement = (int) Math.ceil(maxSensorRange / 10.0);

        // A bit of a hack here. "Aero Sensors" return the ground range, because Sensor doesn't know about Game or Entity
        // to do otherwise. We need to use the space range instead.
        if (detector.getActiveSensor().getType() == Sensor.TYPE_AERO_SENSOR) {
            maxSensorRange = Sensor.ASF_RADAR_MAX_RANGE;
            rangeIncrement = Sensor.ASF_RADAR_AUTOSPOT_RANGE;
        }

        if (detector instanceof Aero) {
            Aero aero = (Aero) detector;
            //Account for sensor damage
            if (aero.isAeroSensorDestroyed()) {
                return false;
            } else {
                tn += aero.getSensorHits();
            }
        }

        //Apply modifiers for detector's equipment
        //-2 for a working Large NCSS.  Triple the detection range.
        if (detector.hasWorkingMisc(MiscType.F_LARGE_COMM_SCANNER_SUITE)) {
            maxSensorRange *= 3;
            tn -= 2;
        }
        //-1 for a working Small NCSS. Double the detection range.
        if (detector.hasWorkingMisc(MiscType.F_SMALL_COMM_SCANNER_SUITE)) {
            maxSensorRange *= 2;
            tn -= 1;
        }
        //-2 for any type of BAP or EW Equipment. ECM is already accounted for, so don't let the BAP check do that
        if (detector.hasWorkingMisc(MiscType.F_EW_EQUIPMENT)
                || detector.hasBAP(false)) {
            tn -= 2;
        }

        //Military ESM automatically detects anyone using active sensors, which includes all telemissiles
        if (detector.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_ESM && target.getTargetType() == Targetable.TYPE_ENTITY) {
            Entity te = (Entity) target;
            return te.getActiveSensor().getType() == Sensor.TYPE_AERO_SENSOR
                    || te.getActiveSensor().getType() == Sensor.TYPE_SPACECRAFT_RADAR
                    || te instanceof TeleMissile;
        }

        //Can't detect anything beyond this distance
        if (distance > maxSensorRange) {
            return false;
        }

        //Apply Sensor Geek SPA, if present
        if (detector.hasAbility(OptionsConstants.UNOFF_SENSOR_GEEK)) {
            tn -= 2;
        }

        //Otherwise, we add +1 to the tn for each 1/10 of the max sensor range (rounded up) between detector and target
        tn += (distance / rangeIncrement);

        // Apply ECM/ECCM effects
        if (game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_ECM)) {
            tn += calcSpaceECM(detector, target);
        }

        // Apply large craft sensor shadows
        if (game.getOptions().booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_SENSOR_SHADOW)) {
            tn += calcSensorShadow(game, detector, target);
        }

        //Now, determine if we've detected the target this round
        return roll >= tn;
    }

    /**
     * Calculates the ECM effects in play between a detector and target pair
     *
     * @param detector - the entity making a sensor scan
     * @param target - the entity we're trying to spot
     * @return A target number modifier for ECM effects
     */
    private static int calcSpaceECM(Entity detector, Targetable target) {
        int mod = 0;
        int ecm = ComputeECM.getLargeCraftECM(detector, detector.getPosition(), target.getPosition());
        if (!detector.isLargeCraft()) {
            ecm += ComputeECM.getSmallCraftECM(detector, detector.getPosition(), target.getPosition());
        }
        ecm = Math.min(4, ecm);
        int eccm = 0;
        if (detector.isLargeCraft()) {
            eccm = ((Aero) detector).getECCMBonus();
        }
        if (ecm > 0) {
            mod += ecm;
            if (eccm > 0) {
                mod -= (Math.min(ecm, eccm));
            }
        }
        return mod;
    }

    /**
     * Calculates the Sensor Shadow effects in play between a detector and target pair
     *
     * @param game The current {@link Game}
     * @param detector the entity making a sensor scan
     * @param target the entity we're trying to spot
     * @return A target number modifier for sensor shadows
     */
    private static int calcSensorShadow(Game game, Entity detector, Targetable target) {
        int mod = 0;
        if (target.getTargetType() != Targetable.TYPE_ENTITY) {
            return 0;
        }
        Entity te = (Entity) target;
        for (Entity en : Compute.getAdjacentEntitiesAlongAttack(detector.getPosition(), target.getPosition(), game, detector.getCurrentBoardId())) {
            if (!en.isEnemyOf(te) && en.isLargeCraft() && !en.equals(te) && ((en.getWeight() - te.getWeight()) >= -100000.0)) {
                mod ++;
                break;
            }
        }
        for (Entity en : game.getEntitiesAt(target.getBoardLocation())) {
            if (!en.isEnemyOf(te) && en.isLargeCraft() && !en.equals(detector) && !en.equals(te)
                    && ((en.getWeight() - te.getWeight()) >= -100000.0)) {
                mod ++;
                break;
            }
        }
        return mod;
    }

    private GameManagerAdvancedSensorHelper() { }
}