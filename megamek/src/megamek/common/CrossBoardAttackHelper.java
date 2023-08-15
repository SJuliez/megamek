package megamek.common;

public final class CrossBoardAttackHelper {

    /**
     * Returns true when the given attacker can possibly attack the given target where attacker and target
     * are not on the same board.
     *
     * <P>Note: Returns false when attacker and target are on the same board!</P>
     *
     * <P>Note: This method is strict in that when it returns false, no attack is possible. It
     * may however return true when an attack is possible in principle but may still be hindered
     * by some circumstance. In other words, this method should check for general features such as
     * unit types and a connection between the boards of attacker and target but it should not
     * check ammo availability, disallowed multiple targets, firing arcs etc.</P>
     *
     * @param attacker The attacking unit
     * @param target The target unit or object
     * @param game The game object
     * @return True when an attack is possible in principle (but might still be unavailable), false only when
     * an attack is definitely impossible
     */
    public static boolean isCrossBoardAttackPossible(Entity attacker, Targetable target, Game game) {
        // @@MultiBoardTODO:
        if ((attacker == null) || (target == null) || game.onTheSameBoard(attacker, target)) {
            return false;
        }

        if (attacker.isInfantry() || attacker.isProtoMek()) {
            // @@MultiBoardTODO: When NOT using aero on ground maps, a fighter on the atmo map is targteable
            // by ground units if it makes a flyover
            return false;
        }

        // A2A attacks are possible between ground map and atmospheric map
        if (attacker.isFighter() && target.isFighter() && game.onDirectlyConnectedBoards(attacker, target)
                && !attacker.getCurrentMapType().isSpace() && !game.getBoard(target).inSpace()) {
            return true;
        }

        // O2G fire is possible using capital and sub-capital weapons
        if (attacker.isCapitalScale() && attacker.isSpaceborne() && game.isOnGroundMap(target)
                && (target instanceof HexTarget)) {
            // @@MultiBoardTODO: might add some checks; not necessarily capital scale, but the weapons must be
            return true;
        }

        // A2G fire is possible using capital and sub-capital weapons
        if (attacker.isCapitalScale() && game.isInAtmosphericRowOnHighAtmoMap(attacker) && game.isOnGroundMap(target)
                && (target instanceof HexTarget)) {
            // @@MultiBoardTODO: might add some checks; not necessarily capital scale, but the weapons must be
            return true;
        }

        // S2O fire is possible using capital and sub-capital weapons
        if (attacker.isCapitalScale() && game.isOnGroundMap(attacker) && game.inSpace(target.getBoardLocation())
                && target.isLargeAerospace() // @@MultiBoardTODO: bring in line with isLargeCraft, SC are not large craft!
                && (target instanceof Entity)) {
            // @@MultiBoardTODO: might add some checks; not necessarily capital scale, but the weapons must be
            return true;
        }

        // A2O fire is possible using capital and sub-capital weapons
        if (attacker.isCapitalScale() && game.isOnGroundMap(attacker) && game.inSpace(target.getBoardLocation())
                && target.isLargeAerospace() // @@MultiBoardTODO: bring in line with isLargeCraft, SC are not large craft!
                && (target instanceof Entity)) {
            // @@MultiBoardTODO: might add some checks; not necessarily capital scale, but the weapons must be
            return true;
        }

        // S2S fire is possible using capital and sub-capital missiles between different ground maps under the same space map
        if (game.isOnGroundMap(attacker) && game.isOnGroundMap(target) && (target instanceof HexTarget)) {
            // @@MultiBoardTODO: might add some checks; not necessarily capital scale, but the weapons must be
            return true;
        }

        return false;
    }
}
