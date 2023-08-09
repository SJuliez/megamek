/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.swing.lobby;

import megamek.client.ui.Messages;
import megamek.client.ui.swing.util.UIUtil;

import javax.swing.*;
import java.text.MessageFormat;

/** Contains static methods that show common info/error messages for the lobby. */
public final class LobbyErrors {
    
    private static final String SINGLE_OWNER = "For this action, the selected units must have a single owner.";
    private static final String CONFIG_ENEMY = "Cannot configure units of other players except units of your bots.";
    private static final String VIEW_HIDDEN = "Cannot view or set details on hidden units.";
    private static final String SINGLE_UNIT = "Cannot {0} for more than one unit at a time.";
    private static final String SINGLE_UNIT_OR_FORCE = "Please select a single unit or a single force.";
    private static final String TEN_UNITS = "Please select fewer than 10 units.";
    private static final String HEAT_TRACKING = "Cannot apply a heat setting to units that do not track heat.";
    private static final String ONLY_MEKS = "This setting can only be applied to Meks.";
    private static final String NOT_THIS_MAPTYPE = "Ât least one of the selected units cannot survive on that map type.";
    private static final String NO_MAP = "At least one of ground, atmosphere and space map must be used.";
    private static final String ONLY_C3M = "Only units with a C3M can be set to be Company Masters.";
    private static final String SAME_C3 = "The C3 systems of the selected units don't match. Select only the same type of C3 units.";
    private static final String EXCEED_C3_CAPACITY = "Connecting the selected units exceed this C3 system's capacity.";
    private static final String LOAD_ONLY_ALLIED = "Can only load units that are allied with each other.";
    private static final String ONLY_FIGHTERS = "Only aerospace and conventional fighters can join squadrons.";
    private static final String NO_BAY = "The unit does not have that bay.";
    private static final String ONLY_OWN_BOT = "Can only remove bots that were added in this lobby.";
    private static final String NO_DUAL_LOAD = "It is not possible to re-load two units to a new transport where one " +
            "unit currently carries the other. Please unload the units first.";
    private static final String ONLY_TEAM = "Combinations like loading, C3 connections and shared forces are only valid within a team.";
    private static final String ENTITY_OR_FORCE = "Please select either only forces or only units.";
    private static final String FORCE_ASSIGN_ONLYTEAM = "Can only reassign a force to a teammate when reassigning without units.";
    private static final String FORCE_ATTACH_TOSUB = "Cannot attach a force to its own subforce.";
    private static final String SBF_CONVERSIONERROR = "At least some of the forces you selected cannot be " +
            "converted to SBF Formations. Please select only the topmost forces to be converted, no subforces. " +
            "A converted force must conform to the rules given in Interstellar Operations. Conversion " +
            "will typically work with companies created in the Force Generator.";

    public static void showOnlyOwnBot(JFrame owner) {
        showErrorDialog(owner, ONLY_OWN_BOT);
    }
    
    public static void showOnlySingleEntityOrForce(JFrame owner) {
        showErrorDialog(owner, SINGLE_UNIT_OR_FORCE);
    }
    
    public static void showSingleOwnerRequired(JFrame owner) {
        showErrorDialog(owner, SINGLE_OWNER);
    }
    
    public static void showForceNoAttachSubForce(JFrame owner) {
        showErrorDialog(owner, FORCE_ATTACH_TOSUB);
    }
    
    public static void showOnlyTeam(JFrame owner) {
        showErrorDialog(owner, ONLY_TEAM);
    }
    
    public static void showOnlyC3M(JFrame owner) {
        showErrorDialog(owner, ONLY_C3M);
    }
    
    public static void showNoDualLoad(JFrame owner) {
        showErrorDialog(owner, NO_DUAL_LOAD);
    }
    
    public static void showNoSuchBay(JFrame owner) {
        showErrorDialog(owner, NO_BAY);
    }
    
    public static void showSquadronTooMany(JFrame owner) {
        showErrorDialog(owner, Messages.getString("FighterSquadron.toomany"));
    }
    
    public static void showOnlyFighter(JFrame owner) {
        showErrorDialog(owner, ONLY_FIGHTERS);
    }
    
    public static void showLoadOnlyAllied(JFrame owner) {
        showErrorDialog(owner, LOAD_ONLY_ALLIED);
    }
    
    public static void showExceedC3Capacity(JFrame owner) {
        showErrorDialog(owner, EXCEED_C3_CAPACITY);
    }

    public static void showSameC3(JFrame owner) {
        showErrorDialog(owner, SAME_C3);
    }

    public static void showCannotConfigEnemies(JFrame owner) {
        showErrorDialog(owner, CONFIG_ENEMY);
    }
    
    public static void showCannotViewHidden(JFrame owner) {
        showErrorDialog(owner, VIEW_HIDDEN);
    }
    
    public static void showSingleUnit(JFrame owner, String action) {
        showErrorDialog(owner, MessageFormat.format(SINGLE_UNIT, action));
    }
    
    public static void showTenUnits(JFrame owner) {
        showErrorDialog(owner, TEN_UNITS);
    }
    
    public static void showHeatTracking(JFrame owner) {
        showErrorDialog(owner, HEAT_TRACKING);
    }
    
    public static void showOnlyMeks(JFrame owner) {
        showErrorDialog(owner, ONLY_MEKS);
    }

    public static void showMapTypeNotAllowed(JFrame owner) {
        showErrorDialog(owner, NOT_THIS_MAPTYPE);
    }

    public static void showOnlyTeammate(JFrame owner) {
        showErrorDialog(owner, FORCE_ASSIGN_ONLYTEAM);
    }
    
    public static void showOnlyEntityOrForce(JFrame owner) {
        showErrorDialog(owner, ENTITY_OR_FORCE);
    }

    public static void showSBFConversion(JFrame owner) {
        showErrorDialog(owner, SBF_CONVERSIONERROR);
    }

    public static void showMustUseMap(JFrame owner) {
        showErrorDialog(owner, NO_MAP);
    }
    
    private static void showErrorDialog(JFrame owner, String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(owner, "Error");
        UIUtil.adjustDialog(dialog,  UIUtil.FONT_SCALE1);
        dialog.pack();
        dialog.setVisible(true);
    }

    private LobbyErrors() { }
}