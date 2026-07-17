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
package megamek.client.ui.panels.phaseDisplay;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import megamek.client.event.BoardViewEvent;
import megamek.client.ui.Messages;
import megamek.client.ui.clientGUI.boardview.overlay.ToastLevel;
import megamek.client.ui.clientGUI.sbf.SBFClientGUI;
import megamek.client.ui.dialogs.phaseDisplay.SBFTargetDialog;
import megamek.client.ui.util.KeyCommandBind;
import megamek.client.ui.widget.MegaMekButton;
import megamek.common.actions.EntityAction;
import megamek.common.actions.sbf.SBFStandardUnitAttack;
import megamek.common.alphaStrike.ASRange;
import megamek.common.annotations.Nullable;
import megamek.common.board.BoardLocation;
import megamek.common.event.GameTurnChangeEvent;
import megamek.common.game.InGameObject;
import megamek.common.rolls.TargetRoll;
import megamek.common.strategicBattleSystems.SBFFormation;
import megamek.common.strategicBattleSystems.SBFFormationTurn;
import megamek.common.strategicBattleSystems.SBFToHitData;
import megamek.common.units.BTObject;

public class SBFFiringDisplay extends SBFActionPhaseDisplay implements ListSelectionListener {

    private final List<EntityAction> plannedActions = new ArrayList<>();
    private InGameObject selectedTarget;
    private int firingUnit = BTObject.NONE;
    private final SBFTargetDialog targetDialog;

    private final Map<SBFFiringCommand, MegaMekButton> buttons = new HashMap<>();

    public SBFFiringDisplay(SBFClientGUI cg) {
        super(cg);
        setupStatusBar(Messages.getString("FiringDisplay.waitingForFiringPhase"));
        setButtons();
        setButtonsTooltips();
        setupButtonPanel();
        registerKeyCommands();
        game().addGameListener(this);
        clientGUI.boardViews().forEach(b -> b.addBoardViewListener(this));
        targetDialog = new SBFTargetDialog(getClientGUI().getFrame(), game(), this);
    }

    @Override
    protected void updateDonePanel() {
        if (plannedActions.isEmpty()) {
            updateDonePanelButtons("Done", "Skip Firing", false, null);
        } else {
            updateDonePanelButtons("Fire", "Skip Firing", true, null);
        }
    }

    private void selectFormation(@Nullable SBFFormation formation) {
        if (formation == null) {
            currentFormation = SBFFormation.NONE;
            firingUnit = BTObject.NONE;
        } else {
            if (currentFormation == formation.getId()) {
                // Selection hasn't changed, do nothing
                return;
            }
            firingUnit = BTObject.NONE;
            currentFormation = formation.getId();
        }
        resetPlannedActions();
        clientGUI.selectForAction(formation);
        updateTargetingData();
        updateDonePanel();
    }

    protected boolean shouldPerformClearKeyCommand() {
        return !clientGUI.isChatBoxActive() && !isIgnoringEvents() && isVisible();
    }

    private void registerKeyCommands() {
        controller.registerCommandAction(KeyCommandBind.NEXT_UNIT, this, this::selectNextFormation);
        controller.registerCommandAction(KeyCommandBind.PREV_UNIT, this, this::selectPreviousFormation);
        controller.registerCommandAction(KeyCommandBind.CANCEL, this::shouldPerformClearKeyCommand, this::clear);
    }

    @Override
    protected List<MegaMekButton> getButtonList() {
        return new ArrayList<>(buttons.values());
    }

    @Override
    protected void setButtons() {
        for (SBFFiringCommand cmd : SBFFiringCommand.values()) {
            buttons.put(cmd, createButton(cmd.getCmd(), "SBFFiringDisplay."));
        }
    }

    @Override
    protected void setButtonsTooltips() {

    }

    @Override
    public void clear() {
        resetPlannedActions();
        updateButtonStatus();
        updateDonePanel();
    }

    private void resetPlannedActions() {
        plannedActions.clear();
    }

    private void selectNextFormation() {
        Optional<SBFFormation> nextFormation = game().getNextEligibleFormation(currentFormation);
        if (nextFormation.isEmpty()) {
            clientGUI.addToast(ToastLevel.INFO, "No Formation available");
        } else if (nextFormation.get().getId() == currentFormation) {
            clientGUI.addToast(ToastLevel.INFO, "No other Formation available");
        }
        nextFormation.ifPresent(this::selectFormation);
    }

    private void selectPreviousFormation() {
        Optional<SBFFormation> previousFormation = game().getPreviousEligibleFormation(currentFormation);
        if (previousFormation.isEmpty()) {
            clientGUI.addToast(ToastLevel.INFO, "No Formation available");
        } else if (previousFormation.get().getId() == currentFormation) {
            clientGUI.addToast(ToastLevel.INFO, "No other Formation available");
        }
        previousFormation.ifPresent(this::selectFormation);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isIgnoringEvents() || !isMyTurn()) {
            return;
        }

        final String actionCmd = e.getActionCommand();
        if (actionCmd.equals(SBFFiringCommand.FIRE_NEXT.getCmd())) {
            selectNextFormation();
        } else if (actionCmd.equals(SBFFiringCommand.FIRE_PREVIOUS.getCmd())) {
            selectPreviousFormation();
        } else if (actionCmd.equals(SBFFiringCommand.FIRE_UNIT.getCmd())) {
            fire();
        }
    }

    private void fire() {
        if (!isMyTurn()) {
            clientGUI.addToast(ToastLevel.INFO, "It is not your turn to declare fire");
            return;
        } else if (actingFormation().isEmpty()) {
            clientGUI.addToast(ToastLevel.INFO, "No firing Formation selected");
            return;
        } else if (firingUnit < 0) {
            clientGUI.addToast(ToastLevel.INFO, "No firing Unit selected");
            return;
        } else if (selectedTarget == null) {
            clientGUI.addToast(ToastLevel.INFO, "No target selected");
            return;
        } else if (getCurrentToHitData().isImpossible()) {
            clientGUI.addToast(ToastLevel.INFO, "Attack is impossible (%s)".formatted(getCurrentToHitData().getDesc()));
            return;
        }
        var attack = new SBFStandardUnitAttack(actingFormation().get().getId(),
              firingUnit,
              selectedTarget.getId(),
              ASRange.LONG);

        plannedActions.add(attack);
        updateButtonStatus();
        updateDonePanel();
        targetDialog.updateAttacks(plannedActions);
        clientGUI.addToast(ToastLevel.INFO, "Attacking " + selectedTarget.getId() + " " + firingUnit);
    }

    private SBFToHitData getCurrentToHitData() {
        SBFToHitData toHitData = new SBFToHitData();
        if (selectedTarget == null) {
            toHitData.addModifier(TargetRoll.IMPOSSIBLE, "No target selected");

        } else if (firingUnit == BTObject.NONE) {
            toHitData.addModifier(TargetRoll.IMPOSSIBLE, "No Unit selected for firing");

        } else if (actingFormation().isEmpty()) {
            toHitData.addModifier(TargetRoll.IMPOSSIBLE, "No Formation selected for firing");

        } else if (!game().usesFriendlyFire()
              && selectedTarget instanceof SBFFormation targetFormation
              && !game().areHostile(targetFormation, clientGUI.getClient().getLocalPlayer())) {
            toHitData.addModifier(TargetRoll.IMPOSSIBLE, "Cannot attack friendly target");

        } else {
            SBFFormation attacker = actingFormation().get();
            if (firingUnit >= attacker.getUnits().size() || firingUnit < 0) {
                toHitData.addModifier(TargetRoll.IMPOSSIBLE, "Invalid Unit");
            } else {
                toHitData = SBFToHitData.compileToHit(game(),
                      new SBFStandardUnitAttack(attacker.getId(), firingUnit, selectedTarget.getId(), ASRange.LONG));
            }
        }
        return toHitData;
    }

    @Override
    public void ready() {
        if (actingFormation().isEmpty()) {
            clientGUI.addToast(ToastLevel.INFO, "No Formation selected");
            return;
        }
        clientGUI.getClient().sendAttackData(plannedActions, currentFormation);
        if (plannedActions.isEmpty()) {
            clientGUI.addToast(ToastLevel.INFO, "No attacks for formation " + currentFormation);
        }
        endMyTurn();
    }

    /**
     * Clears out old movement data and disables relevant buttons.
     */
    private void endMyTurn() {
        stopTimer();
        updateButtonStatus();
        selectFormation(null);
        hideTargetDialog();
    }

    @Override
    public void removeAllListeners() {
        game().removeGameListener(this);
        clientGUI.boardViews().forEach(b -> b.removeBoardViewListener(this));
    }

    private void beginMyTurn() {
        initDonePanelForNewTurn();
        updateButtonStatus();
        if (GUIP.getAutoSelectNextUnit()) {
            clientGUI.getClient().getGame().getNextEligibleFormation().ifPresent(this::selectFormation);
        }
        //            clientGUI.bingMyTurn();
        startTimer();
    }

    private void updateButtonStatus() {
        boolean myTurn = isMyTurn();
        boolean turnIsFormationTurn = game().getTurn() instanceof SBFFormationTurn;
        boolean hasAvailableFormations = turnIsFormationTurn
              && game().hasEligibleFormation((SBFFormationTurn) game().getTurn());
        boolean hasTarget = selectedTarget != null;

        buttons.get(SBFFiringCommand.FIRE_NEXT).setEnabled(myTurn && hasAvailableFormations);
        buttons.get(SBFFiringCommand.FIRE_PREVIOUS).setEnabled(myTurn && hasAvailableFormations);
        //        buttons.get(FiringCommand.FIRE_MORE).setVisible(myTurn && (numButtonGroups > 1));
        buttons.get(SBFFiringCommand.FIRE_UNIT).setEnabled(true);
    }

    private boolean isFirePossible() {
        return actingFormation().isPresent()
              && (firingUnit >= 0)
              && (actingFormation().get().getUnits().size() > firingUnit)
              && actingFormation().get().isEligibleForPhase(game().getPhase())
              && !unitHasPlannedFire();
    }

    private boolean unitHasPlannedFire() {
        return plannedActions.stream()
              .filter(a -> a instanceof SBFStandardUnitAttack)
              .anyMatch(a -> ((SBFStandardUnitAttack) a).getUnitNumber() == firingUnit);
    }

    @Override
    public void gameTurnChange(GameTurnChangeEvent e) {
        if (isIgnoringEvents()) {
            return;
        }

        if (isMyTurn()) {
            setStatusBarText(Messages.getString("FiringDisplay.its_your_turn"));
            beginMyTurn();
        } else {
            setStatusBarText(Messages.getString("FiringDisplay.its_others_turn", playerNameOrUnknown(e.getPlayer())));
            endMyTurn();
        }
    }

    private void setTarget(@Nullable InGameObject target) {
        selectedTarget = target;
        updateButtonStatus();
    }

    @Override
    public void hexMoused(BoardViewEvent b) {
        if (isIgnoringEvents() || !isMyTurn() || (b.getButton() != MouseEvent.BUTTON1)) {
            return;
        }

        if (!game().getActiveFormationsAt(BoardLocation.of(b.getCoords(), 0)).isEmpty()) {
            setTarget(game().getActiveFormationsAt(BoardLocation.of(b.getCoords(), 0)).getFirst());
        } else {
            setTarget(null);
        }
        updateTargetingData();
    }

    /**
     * Recalculates toHit from the current selections for attacker and target and updates the targeting dialog
     * accordingly.
     */
    private void updateTargetingData() {
        showTargetDialog();
        targetDialog.setContent(game().getFormation(currentFormation).orElse(null),
              selectedTarget,
              getCurrentToHitData());
    }

    public void showTargetDialog() {
        targetDialog.setVisible(true);
    }

    public void hideTargetDialog() {
        targetDialog.setVisible(false);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && !isIgnoringEvents()) {
            firingUnit = e.getFirstIndex();
            updateTargetingData();
            updateButtonStatus();
        }
    }
}
