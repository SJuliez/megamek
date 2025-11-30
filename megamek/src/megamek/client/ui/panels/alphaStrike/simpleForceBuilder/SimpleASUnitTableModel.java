/*
 * Copyright (C) 2020-2025 The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import javax.swing.table.AbstractTableModel;

import megamek.client.ui.panels.phaseDisplay.lobby.LobbyMekCellFormatter;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.game.InGameObject;

class SimpleASUnitTableModel extends AbstractTableModel {

    public static final int COL_UNIT = 0;
    public static final int COL_SKILL = 1;
    public static final int COL_PV = 2;
    public static final int N_COL = 3;

    /** Previous list contents used for the undo function */
    private final Stack<List<AlphaStrikeElement>> previousUnitLists = new Stack<>();

    /** Previous list contents used for the redo function */
    private final Stack<List<AlphaStrikeElement>> redoUnitLists = new Stack<>();

    /** The displayed entities. This list is the actual table data. */
    private final List<AlphaStrikeElement> units = new ArrayList<>();
    /** The displayed contents of the Unit column. */
    private final List<String> unitCells = new ArrayList<>();

    @Override
    public Object getValueAt(int row, int col) {
        final AlphaStrikeElement element = units.get(row);
        if (element == null) {
            return "Error: Unit not found";
        }
        return switch (col) {
            case COL_UNIT -> unitCells.get(row);
            case COL_PV -> "%d  ".formatted(element.getPointValue()); // right-alignment spacing
            case COL_SKILL -> "%d".formatted(element.getSkill());
            default -> "";
        };
    }

    @Override
    public int getRowCount() {
        return units.size();
    }

    private void prepareUndo() {
        previousUnitLists.push(new ArrayList<>(units));
    }

    /** Adds the given entity to the table and builds the display content. */
    public void addUnit(AlphaStrikeElement entity) {
        addUnits(List.of(entity));
    }

    /** Adds the given entity to the table and builds the display content. */
    public void addUnits(Collection<AlphaStrikeElement> newUnits) {
        prepareUndo();
        redoUnitLists.clear(); // Doing anything other than undo removes the option to redo

        units.addAll(newUnits);
        newUnits.forEach(this::addCellData);
        fireTableDataChanged();
    }

    public void removeUnit(Integer rowIndex) {
        removeUnits(List.of(rowIndex));
    }

    public void removeUnits(int[] rowIndexes) {
        removeUnits(Arrays.stream(rowIndexes).boxed().toList());
    }

    public void removeUnits(Collection<Integer> rowIndices) {
        prepareUndo();
        redoUnitLists.clear(); // Doing anything other than undo removes the option to redo

        List<Integer> rowIndicesCopy = new ArrayList<>(rowIndices);
        rowIndicesCopy.sort(Comparator.reverseOrder());
        rowIndicesCopy.stream().mapToInt(i -> i).forEach(units::remove);
        rowIndicesCopy.stream().mapToInt(i -> i).forEach(unitCells::remove);
        fireTableDataChanged();
    }

    /**
     * Adds display content for the given entity. The entity is assumed to be the last entity added to the table and the
     * display content will be added as a new last table row.
     */
    private void addCellData(InGameObject entity) {
        unitCells.add(LobbyMekCellFormatter.unitTableEntry(entity, null, false, false));
    }

    /**
     * Returns the column header for the given column. The header text is HTML and scaled according to the GUI scale.
     */
    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case COL_UNIT -> "Unit";
            case COL_PV -> "PV";
            case COL_SKILL -> "Skill";
            default -> "??";
        };
    }

    /** Returns the entity of the given table row. */
    public AlphaStrikeElement getUnitAt(int row) {
        return units.get(row);
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }

    /** Returns the entity of the given table row. */
    public Optional<AlphaStrikeElement> unitAt(int row) {
        if (row < 0 || row >= units.size()) {
            return Optional.empty();
        } else {
            return Optional.of(units.get(row));
        }
    }

    @Override
    public int getColumnCount() {
        return N_COL;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    public List<AlphaStrikeElement> getUnits() {
        return Collections.unmodifiableList(units);
    }

    /**
     * @return True when there is a previous unit list available to reset the unit list to (i.e., when the last change
     *       can be un-done).
     */
    public boolean canUseUndo() {
        return !previousUnitLists.isEmpty();
    }

    /**
     * Changes the unit list to the first one in the undo list. This will typically reverse the latest change to the
     * list, either by adding or deleting units manually or by using "redo".
     */
    public void undoLastChange() {
        if (!previousUnitLists.isEmpty()) {
            redoUnitLists.push(new ArrayList<>(units));
            clearData();
            units.addAll(previousUnitLists.pop());
            units.forEach(this::addCellData);
            fireTableDataChanged();
        }
    }

    /**
     * @return True when there is at least one entry in the redo-List, i.e., the last changes were "undo" actions that
     *       can be reversed.
     */
    public boolean canUseRedo() {
        return !redoUnitLists.isEmpty();
    }

    /**
     * Changes the unit list to the first one in the redo list. This will typically reverse the latest undo action.
     */
    public void redo() {
        if (!redoUnitLists.isEmpty()) {
            prepareUndo();
            clearData();
            units.addAll(redoUnitLists.pop());
            units.forEach(this::addCellData);
            fireTableDataChanged();
        }
    }

    /**
     * Clears the unit data and rendered unit HTML cells. Only for internal use. Does not properly prepare undo.
     */
    private void clearData() {
        units.clear();
        unitCells.clear();
        fireTableDataChanged();
    }

    void removeAllUnits() {
        prepareUndo();
        redoUnitLists.clear(); // Doing anything other than undo removes the option to redo
        units.clear();
        unitCells.clear();
        fireTableDataChanged();
    }
}
