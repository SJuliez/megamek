package megamek.common;

import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Vector;

import static java.util.stream.Collectors.toList;

public final class ComputeArc {

    public static final int ARC_VGL_LF = 37;
    public static final int ARC_VGL_LR = 36;
    public static final int ARC_VGL_REAR = 35;
    public static final int ARC_VGL_RR = 34;
    public static final int ARC_VGL_RF = 33;
    public static final int ARC_VGL_FRONT = 32;
    public static final int ARC_360 = 0;
    public static final int ARC_FORWARD = 1;
    public static final int ARC_LEFTARM = 2;
    public static final int ARC_RIGHTARM = 3;
    public static final int ARC_REAR = 4;
    public static final int ARC_LEFTSIDE = 5;
    public static final int ARC_RIGHTSIDE = 6;
    public static final int ARC_MAINGUN = 7;
    public static final int ARC_NORTH = 8;
    public static final int ARC_EAST = 9;
    public static final int ARC_WEST = 10;
    public static final int ARC_NOSE = 11;
    public static final int ARC_LWING = 12;
    public static final int ARC_RWING = 13;
    public static final int ARC_LWINGA = 14;
    public static final int ARC_RWINGA = 15;
    public static final int ARC_LEFTSIDE_SPHERE = 16;
    public static final int ARC_RIGHTSIDE_SPHERE = 17;
    public static final int ARC_LEFTSIDEA_SPHERE = 18;
    public static final int ARC_RIGHTSIDEA_SPHERE = 19;
    public static final int ARC_LEFT_BROADSIDE = 20;
    public static final int ARC_RIGHT_BROADSIDE = 21;
    public static final int ARC_AFT = 22;
    public static final int ARC_LEFT_SPHERE_GROUND = 23;
    public static final int ARC_RIGHT_SPHERE_GROUND = 24;
    public static final int ARC_TURRET = 25;
    public static final int ARC_SPONSON_TURRET_LEFT = 26;
    public static final int ARC_SPONSON_TURRET_RIGHT = 27;
    public static final int ARC_PINTLE_TURRET_LEFT = 28;
    public static final int ARC_PINTLE_TURRET_RIGHT = 29;
    public static final int ARC_PINTLE_TURRET_FRONT = 30;
    public static final int ARC_PINTLE_TURRET_REAR = 31;
    // Expanded arcs for Waypoint Launched Capital Missiles
    public static final int ARC_NOSE_WPL = 38;
    public static final int ARC_LWING_WPL = 39;
    public static final int ARC_RWING_WPL = 40;
    public static final int ARC_LWINGA_WPL = 41;
    public static final int ARC_RWINGA_WPL = 42;
    public static final int ARC_LEFTSIDE_SPHERE_WPL = 43;
    public static final int ARC_RIGHTSIDE_SPHERE_WPL = 44;
    public static final int ARC_LEFTSIDEA_SPHERE_WPL = 45;
    public static final int ARC_RIGHTSIDEA_SPHERE_WPL = 46;
    public static final int ARC_AFT_WPL = 47;
    public static final int ARC_LEFT_BROADSIDE_WPL = 48;
    public static final int ARC_RIGHT_BROADSIDE_WPL = 49;

    /** Lookup table for vehicular grenade launcher firing arc from facing */
    private static final int[] VGL_FIRING_ARCS = { ARC_VGL_FRONT, ARC_VGL_RF, ARC_VGL_RR,
            ARC_VGL_REAR, ARC_VGL_LR, ARC_VGL_LF
    };

    /**
     * Checks to see if a target is in arc of the specified weapon, on the
     * specified attacker.
     */
    public static boolean isInArc(Game game, int attackerId, int weaponId, Targetable target) {
        Entity attacker = game.getEntity(attackerId);

        if ((attacker instanceof Mech) && (attacker.getGrappled() == target.getId())) {
            return true;
        }

        int facing = attacker.isSecondaryArcWeapon(weaponId) ? attacker.getSecondaryFacing() : attacker.getFacing();

        if ((attacker instanceof Tank)
            && (attacker.getEquipment(weaponId).getLocation() == ((Tank) attacker).getLocTurret2())) {
            facing = ((Tank) attacker).getDualTurretFacing();
        }

        if (attacker.getEquipment(weaponId).isMechTurretMounted()) {
            facing = attacker.getSecondaryFacing() + (attacker.getEquipment(weaponId).getFacing() % 6);
        }

        BoardLocation aPos = attacker.getBoardLocation();
        BoardLocation tPos = target.getBoardLocation();
        // aeros in the same hex in space may still be able to fire at one another. Translate
        // their positions to see who was further back
        if (attacker.isSpaceborne() && aPos.equals(tPos) && attacker.isAero() && target.isAero()) {
            if (Compute.shouldMoveBackHex(attacker, target) < 0) {
                aPos = attacker.getPriorPosition();
            } else {
                tPos = ((Entity) target).getPriorPosition();
            }
        }

        // if using advanced AA options, then ground-to-air fire determines arc by closest position
        if (Compute.isGroundToAir(attacker, target) && (target instanceof Entity)) {
            tPos = Compute.getClosestFlightPath(attacker.getId(), attacker.getBoardLocation(), (Entity) target);
        }

        // AMS defending against Ground to Air fire needs to calculate arc based on the closest flight path
        // Technically it's an AirToGround attack since the AMS is on the aircraft
        if (Compute.isAirToGround(attacker, target) && (target instanceof Entity)
                && (attacker.getEquipment(weaponId).getType().hasFlag(WeaponType.F_AMS)
                        || attacker.getEquipment(weaponId).getType().hasFlag(WeaponType.F_AMSBAY))) {
            Entity te = (Entity) target;
            aPos = Compute.getClosestFlightPath(te.getId(), te.getBoardLocation(), attacker);
        }

        List<BoardLocation> tPosV = new Vector<>();
        tPosV.add(tPos);
        // check for secondary positions
        if ((target instanceof Entity) && (null != target.getSecondaryPositions())) {
            tPosV.addAll(((Entity) target).getSecondaryPositionsAsBoardLocations());
        }

        if (CrossBoardAttackHelper.isCrossBoardArtyAttack(attacker, target, game)) {
            // When attacking between two ground boards, replace the attacker and target positions with the positions of
            // the boards themselves on the atmospheric map
            // When the ground boards are only connected through a high atmospheric map, the arrangement of
            // the maps is unkown and the arc cannot be tested; therefore return false in that case, although
            // a distance could be computed
            Board attackerAtmoBoard = game.getEnclosingBoard(attacker.getBoard());
            Board targetAtmoBoard = game.getEnclosingBoard(game.getBoard(target.getBoardId()));
            if (attackerAtmoBoard.getBoardId() == targetAtmoBoard.getBoardId()) {
                aPos = attackerAtmoBoard.embeddedBoardLocation(attacker.getBoardId());
                tPosV.clear();
                tPosV.add(attackerAtmoBoard.embeddedBoardLocation(target.getBoardId()));
            } else {
                return false;
            }
        }

        if (CrossBoardAttackHelper.isOrbitToSurface(attacker, target, game)) {
            // For this attack, the ground row hex enclosing the ground map target must be in arc; replace position
            Board targetAtmoBoard = game.getEnclosingBoard(game.getBoard(target.getBoardId()));
            tPosV.clear();
            tPosV.add(attacker.getBoard().embeddedBoardLocation(targetAtmoBoard.getBoardId()));
        }

        if (Compute.isAirToAir(game, attacker, target) && !game.onTheSameBoard(attacker, target)
                && (game.onDirectlyConnectedBoards(attacker, target) || CrossBoardAttackHelper.onGroundMapsWithinOneAtmoMap(game, attacker, target))) {
            // In A2A attacks between different maps (only ground/ground, ground/atmo or atmo/ground), replace the
            // position of the unit on the ground map with the position of the ground map itself in the atmo map
            if (game.isOnGroundMap(attacker) && game.isOnAtmosphericMap(target)) {
                aPos = game.getBoard(target).embeddedBoardLocation(attacker.getBoardId());
            } else if (game.isOnAtmosphericMap(attacker) && game.isOnGroundMap(target)) {
                tPosV.clear();
                tPosV.add(game.getBoard(attacker).embeddedBoardLocation(target.getBoardId()));
            } else if (game.isOnGroundMap(attacker) && game.isOnGroundMap(target)) {
                // Different ground maps, here replace both positions with their respective atmo map hexes
                aPos = game.getBoard(target).embeddedBoardLocation(attacker.getBoardId());
                tPosV.clear();
                tPosV.add(game.getBoard(attacker).embeddedBoardLocation(target.getBoardId()));
            }
        }

        final int attackerBoardId = aPos.getBoardId();
        if (tPosV.stream().anyMatch(bl -> !bl.isOnBoard(attackerBoardId))) {
            LogManager.getLogger().error("Target Coords must be on the same board as the attacker!");
        }

        List<Coords> targetCoords = tPosV.stream().map(BoardLocation::getCoords).collect(toList());
        return isInArc(aPos.getCoords(), facing, targetCoords, attacker.getWeaponArc(weaponId));
    }

    /**
     * Returns true if the line between source Coords and target goes through
     * the hex in front of the attacker
     */
    public static boolean isThroughFrontHex(Coords src, Entity target) {
        int fa = target.getPosition().degree(src) - target.getFacing() * 60;
        if (fa < 0) {
            fa += 360;
        }
        return (fa > 330) || (fa < 30);
    }

    /**
     * Converts the facing of a vehicular grenade launcher to the corresponding firing arc.
     *
     * @param facing The VGL facing returned by {@link Mounted#getFacing()}
     * @return       The firing arc
     */
    public static int firingArcFromVGLFacing(int facing) {
        return VGL_FIRING_ARCS[facing % 6];
    }

    public static boolean isInArc(Coords src, int facing, Targetable target, int arc) {
        return isInArc(src, facing, target.getAllPositions(), arc);
    }

    public static boolean isInArc(Coords src, int facing, Coords targetCoords, int arc) {
        return isInArc(src, facing, List.of(targetCoords), arc);
    }

    /**
     * Returns true if the target is in the specified arc. Note: This has to
     * take Lists of coordinates to account for potential secondary positions, as e.g. in
     * grounded DropShips. Also note that all Coords must be on the same board to get a
     * correct result.
     *
     * @param src    the attack coordinates
     * @param facing the appropriate attacker sfacing
     * @param targetCoords  A vector of target coordinates
     * @param arc    the arc
     */
    public static boolean isInArc(Coords src, int facing, List<Coords> targetCoords, int arc) {
        if ((src == null) || (targetCoords == null)) {
            return true;
        }

        // Jay: I have to adjust this to take in vectors of coordinates to account for secondary
        // positions of the target - I am fairly certain that secondary positions of the attacker
        // shouldn't matter because you don't get to move the angle based on the secondary positions

        // if any of the destination coords are in the right place, then return true
        for (Coords dest : targetCoords) {
            // calculate firing angle
            int fa = src.degree(dest) - (facing * 60);
            if (fa < 0) {
                fa += 360;
            }
            // is it in the specifed arc?
            switch (arc) {
                case ARC_FORWARD:
                    if ((fa >= 300) || (fa <= 60)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTARM:
                    if ((fa >= 300) || (fa <= 120)) {
                        return true;
                    }
                    break;
                case ARC_LEFTARM:
                    if ((fa >= 240) || (fa <= 60)) {
                        return true;
                    }
                    break;
                case ARC_REAR:
                case ARC_AFT:
                    if ((fa > 120) && (fa < 240)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTSIDE:
                    if ((fa > 60) && (fa <= 120)) {
                        return true;
                    }
                    break;
                case ARC_LEFTSIDE:
                    if ((fa < 300) && (fa >= 240)) {
                        return true;
                    }
                    break;
                case ARC_MAINGUN:
                    if ((fa >= 240) || (fa <= 120)) {
                        return true;
                    }
                    break;
                case ARC_360:
                    return true;
                case ARC_NORTH:
                    if ((fa >= 270) || (fa <= 30)) {
                        return true;
                    }
                    break;
                case ARC_EAST:
                    if ((fa >= 30) && (fa <= 150)) {
                        return true;
                    }
                    break;
                case ARC_WEST:
                    if ((fa >= 150) && (fa <= 270)) {
                        return true;
                    }
                    break;
                case ARC_NOSE:
                    if ((fa > 300) || (fa < 60)) {
                        return true;
                    }
                    break;
                case ARC_NOSE_WPL:
                    if ((fa > 240) || (fa < 120)) {
                        return true;
                    }
                    break;
                case ARC_LWING:
                    if ((fa > 300) || (fa <= 0)) {
                        return true;
                    }
                    break;
                case ARC_LWING_WPL:
                    if ((fa > 240) || (fa < 60)) {
                        return true;
                    }
                    break;
                case ARC_RWING:
                    if ((fa >= 0) && (fa < 60)) {
                        return true;
                    }
                    break;
                case ARC_RWING_WPL:
                    if ((fa > 300) || (fa < 120)) {
                        return true;
                    }
                    break;
                case ARC_LWINGA:
                    if ((fa >= 180) && (fa < 240)) {
                        return true;
                    }
                    break;
                case ARC_LWINGA_WPL:
                    if ((fa > 120) && (fa < 300)) {
                        return true;
                    }
                    break;
                case ARC_RWINGA:
                    if ((fa > 120) && (fa <= 180)) {
                        return true;
                    }
                    break;
                case ARC_RWINGA_WPL:
                    if ((fa > 60) && (fa < 240)) {
                        return true;
                    }
                    break;
                case ARC_AFT_WPL:
                    if ((fa > 60) && (fa < 300)) {
                        return true;
                    }
                    break;
                case ARC_LEFTSIDE_SPHERE:
                    if ((fa > 240) || (fa < 0)) {
                        return true;
                    }
                    break;
                case ARC_LEFTSIDE_SPHERE_WPL:
                    if ((fa > 180) || (fa < 60)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTSIDE_SPHERE:
                    if ((fa > 0) && (fa < 120)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTSIDE_SPHERE_WPL:
                    if ((fa > 300) || (fa < 180)) {
                        return true;
                    }
                    break;
                case ARC_LEFTSIDEA_SPHERE:
                    if ((fa > 180) && (fa < 300)) {
                        return true;
                    }
                    break;
                case ARC_LEFTSIDEA_SPHERE_WPL:
                    if ((fa > 120) && (fa < 360)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTSIDEA_SPHERE:
                    if ((fa > 60) && (fa < 180)) {
                        return true;
                    }
                    break;
                case ARC_RIGHTSIDEA_SPHERE_WPL:
                    if ((fa > 0) && (fa < 240)) {
                        return true;
                    }
                    break;
                case ARC_LEFT_BROADSIDE:
                    if ((fa >= 240) && (fa <= 300)) {
                        return true;
                    }
                    break;
                case ARC_LEFT_BROADSIDE_WPL:
                    if ((fa > 180) && (fa <= 360)) {
                        return true;
                    }
                    break;
                case ARC_RIGHT_BROADSIDE:
                    if ((fa >= 60) && (fa <= 120)) {
                        return true;
                    }
                    break;
                case ARC_RIGHT_BROADSIDE_WPL:
                    if ((fa > 0) && (fa < 180)) {
                        return true;
                    }
                    break;
                case ARC_LEFT_SPHERE_GROUND:
                    if ((fa >= 180) && (fa < 360)) {
                        return true;
                    }
                    break;
                case ARC_RIGHT_SPHERE_GROUND:
                    if ((fa >= 0) && (fa < 180)) {
                        return true;
                    }
                    break;
                case ARC_TURRET:
                    if ((fa >= 330) || (fa <= 30)) {
                        return true;
                    }
                    break;
                case ARC_SPONSON_TURRET_LEFT:
                case ARC_PINTLE_TURRET_LEFT:
                    if ((fa >= 180) || (fa == 0)) {
                        return true;
                    }
                    break;
                case ARC_SPONSON_TURRET_RIGHT:
                case ARC_PINTLE_TURRET_RIGHT:
                    if ((fa >= 0) && (fa <= 180)) {
                        return true;
                    }
                    break;
                case ARC_PINTLE_TURRET_FRONT:
                    if ((fa >= 270) || (fa <= 90)) {
                        return true;
                    }
                    break;
                case ARC_PINTLE_TURRET_REAR:
                    if ((fa >= 90) && (fa <= 270)) {
                        return true;
                    }
                    break;
                case ARC_VGL_FRONT:
                    return (fa >= 270) || (fa <= 90);
                case ARC_VGL_RF:
                    return (fa >= 330) || (fa <= 150);
                case ARC_VGL_RR:
                    return (fa >= 30) && (fa <= 210);
                case ARC_VGL_REAR:
                    return (fa >= 90) && (fa <= 270);
                case ARC_VGL_LR:
                    return (fa >= 150) && (fa <= 330);
                case ARC_VGL_LF:
                    return (fa >= 210) || (fa <= 30);
            }
        }
        // if we got here then no matches
        return false;
    }

    private ComputeArc() { }
}
