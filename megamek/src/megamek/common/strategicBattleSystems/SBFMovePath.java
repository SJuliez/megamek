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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import megamek.common.actions.EntityAction;
import megamek.common.board.BoardLocation;
import megamek.logging.MMLogger;

public class SBFMovePath implements EntityAction, Serializable {

    private static final MMLogger logger = MMLogger.create(SBFMovePath.class);

    private final int formationId;
    private final List<SBFMoveStep> steps = new ArrayList<>();
    private final BoardLocation startLocation;

    private boolean isIllegal;

    /** Jumping is a value that is selected when moving, IO:BF 3rd p.166 */
    private int jumpUsed = 0;

    /** True when this path is a minimum movement path, IO:BF 3rd p.164 */
    private boolean isMinimumMovement;

    private transient SBFGame game;

    public SBFMovePath(int formationId, BoardLocation startLocation, SBFGame game) {
        this.formationId = formationId;
        this.startLocation = startLocation;
        this.game = game;
    }

    /**
     * Creates a new move path that is a copy of the given original. Note that the steps are not copied, i.e. the step
     * list is only a shallow copy!
     *
     * @param original The move path to copy
     *
     * @return A new move path that is equal to the original
     */
    public static SBFMovePath createMovePathShallow(SBFMovePath original) {
        SBFMovePath newPath = new SBFMovePath(original.formationId, original.startLocation, original.game);
        newPath.steps.addAll(original.steps);
        return newPath;
    }

    /**
     * Creates a new move path that is a copy of the given original. Note that the steps are copied, i.e. the step list
     * is a deep copy. The returned move path is completely independent of the original.
     *
     * @param original The move path to copy
     *
     * @return A new move path that is equal to the original
     */
    @Deprecated(since = "0.51.0", forRemoval = true)
    public static SBFMovePath createMovePathDeep(SBFMovePath original) {
        SBFMovePath newPath = new SBFMovePath(original.formationId, original.startLocation, original.game);
        newPath.steps.addAll(original.steps.stream().map(SBFMoveStep::copy).toList());
        return newPath;
    }

    @Override
    public int getEntityId() {
        return formationId;
    }

    public int getMpUsed() {
        return steps.stream().mapToInt(SBFMoveStep::getMpUsed).sum();
    }

    public void addStep(SBFMoveStep step) {
        steps.add(step);
        computeStatus();
    }

    public SBFMoveStep getLastStep() {
        return steps.getLast();
    }

    public BoardLocation getLastPosition() {
        return steps.isEmpty() ? startLocation : getLastStep().getLastPosition();
    }

    public boolean isIllegal() {
        return isIllegal;
    }

    boolean hassIllegalSteps() {
        return steps.stream().anyMatch(SBFMoveStep::isIllegal);
    }

    /**
     * Assembles and computes all data for this move path, especially if it is legal. Note that the status of the
     * individual move steps is not re-calculated here.
     */
    public void computeStatus() {
        SBFFormation formation = game.getFormation(formationId).orElseThrow();

        // any illegal move step makes this path illegal
        isIllegal = steps.stream().anyMatch(SBFMoveStep::isIllegal);

        // With sprinting IO:BF 3rd p.199 the formation has more MP
        int allowedMP = game.usesSprintingMove() ? formation.getSprintingMovement() : formation.getMovement();

        // Minimum movement IO:BF 3rd p.164: a unit may move to adjacent hexes regardless of MP if it can move at all
        isMinimumMovement = (steps.size() == 1)
              && (steps.getFirst() instanceof SBFSurfaceMoveStep)
              && allowedMP > 0
              && !steps.getFirst().isIllegal();

        // exceeding the allowed MP makes the path illegal -- unless it is minimum movement
        isIllegal |= (getMpUsed() > allowedMP) && !isMinimumMovement;

        // stacking friendly at end of movement
        List<SBFFormation> friendliesAtDestination = game.getFriendlyFormationsAt(getLastPosition(),
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

    /**
     * Restores the move path after serialization. This is unnecessary unless the {@link #computeStatus()} method is
     * used.
     *
     * @param game The SBFGame
     */
    public void restore(SBFGame game) {
        this.game = game;
    }

    /**
     * Returns the number of hexes moved
     */
    public int getHexesMoved() {
        return startLocation.coords().distance(getLastPosition().coords());
    }

    @Override
    public String toString() {
        return "[SBFMovePath]: ID: " + formationId + "; steps: " + steps;
    }

    /**
     * @return The steps of this move path as an unmodifiable list.
     */
    public List<SBFMoveStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public boolean isEndStep(SBFMoveStep step) {
        return (step != null) && steps.contains(step) && step.destination.equals(getLastPosition());
    }

    /**
     * Returns the number of mp used up to and including the given step. Returns -1 if the step is not part of this move
     * path.
     *
     * @param step The last step to include in the cost
     *
     * @return The total mp up to the given step
     */
    public int getMpUpTo(SBFMoveStep step) {
        if (steps.contains(step)) {
            int mpUsed = 0;
            for (SBFMoveStep step2 : steps) {
                mpUsed += step2.getMpUsed();
                if (step.equals(step2)) {
                    return mpUsed;
                }
            }
        }
        logger.error("Tried to find the mp used with a step that is not part of this move path!");
        return -1;
    }

    public void setJumpUsed(int jumpUsed) {
        this.jumpUsed = jumpUsed;
    }

    public int getJumpUsed() {
        return jumpUsed;
    }

    /**
     * @return True if this move path is a sprinting move. When the sprinting rule (IO:BF 3rd p.199) is not used, always
     *       returns false. When the sprinting rule is used, returns true for moves that use more than the formation's
     *       regular movement but not more than its sprinting MP. (In other words: movement paths using more than the
     *       sprinting MP - those should always be illegal - are NOT sprinting moves).
     */
    public boolean isSprintingMove() {
        SBFFormation formation = game.getFormation(formationId).orElseThrow();
        return game.usesSprintingMove()
              && getMpUsed() > formation.getMovement()
              && getMpUsed() <= (int) (formation.getMovement() * 1.5);
    }

    public boolean isMinimumMovement() {
        return isMinimumMovement;
    }
}
