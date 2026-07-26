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

package megamek.client.ui.clientGUI.sbf;

import megamek.client.bot.BotClient;
import megamek.client.ui.Messages;
import megamek.client.ui.clientGUI.AmsAssignGUI;
import megamek.client.ui.panels.phaseDisplay.PointblankShotDisplay;
import megamek.client.ui.panels.phaseDisplay.StatusBarPhaseDisplay;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.compute.Compute;
import megamek.common.enums.GamePhase;
import megamek.common.enums.MoveStepType;
import megamek.common.equipment.Mounted;
import megamek.common.event.GameCFREvent;
import megamek.common.event.GameListenerAdapter;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.board.GameBoardNewEvent;
import megamek.common.event.entity.GameEntityChangeEvent;
import megamek.common.event.entity.GameEntityNewEvent;
import megamek.common.moves.MovePath;
import megamek.common.options.OptionsConstants;
import megamek.common.units.Entity;
import megamek.common.units.Targetable;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

class SBFClientGUIGameListener extends GameListenerAdapter {

    private final SBFClientGUI clientGUI;

    public SBFClientGUIGameListener(SBFClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    @Override
    public void gamePhaseChange(GamePhaseChangeEvent e) {
        //        // This is a really lame place for this, but I couldn't find a
        //        // better one without making massive changes (which didn't seem
        //        // worth it for one little feature).
        //        if (bv.getLocalPlayer() != client.getLocalPlayer()) {
        //            // The address based comparison is somewhat important.
        //            // Use of the /reset command can cause the player to get reset,
        //            // and the equals function of Player isn't powerful enough.
        //            bv.setLocalPlayer(client.getLocalPlayer());
        //        }
        //        // Make sure the ChatterBox starts out deactivated.
        //        bv.setChatterBoxActive(false);
        //
        // Swap to this phase's panel.
        clientGUI.switchPanel(e.getNewPhase());
        clientGUI.menuBar.setPhase(e.getNewPhase());
        //        clientGUI.cb.moveToEnd();
    }

    @Override
    public void gameBoardNew(GameBoardNewEvent e) {
        clientGUI.bvGame.setBoard(e.getNewBoard());
    }

    @Override
    public void gameEntityChange(GameEntityChangeEvent e) {
        clientGUI.updateFormationSprites();
    }

    @Override
    public void gameEntityNew(GameEntityNewEvent e) {
        clientGUI.updateFormationSprites();
    }

    @Override
    public void gameClientFeedbackRequest(GameCFREvent gameCFREvent) {
//        if ()
        int response = JOptionPane.showConfirmDialog(null, "Oppose?");

        clientGUI.getClient().sendOpposingMoveResponse(response == JOptionPane.YES_OPTION);
    }
}
