/*
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */

package megamek.client.event;

import megamek.client.ui.swing.boardview.BoardView;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.BoardLocation;

/**
 * Instances of this class are sent as a result of changes in BoardView
 * 
 * @see BoardViewListener
 */
public class BoardViewEvent extends java.util.EventObject {
    private static final long serialVersionUID = -4823618884833399318L;
    public static final int BOARD_HEX_CLICKED = 0;
    public static final int BOARD_HEX_DOUBLECLICKED = 1;
    public static final int BOARD_HEX_DRAGGED = 2;

    public static final int BOARD_HEX_CURSOR = 3;
    public static final int BOARD_HEX_HIGHLIGHTED = 4;
    public static final int BOARD_HEX_SELECTED = 5;

    public static final int BOARD_FIRST_LOS_HEX = 6;
    public static final int BOARD_SECOND_LOS_HEX = 7;

    public static final int FINISHED_MOVING_UNITS = 8;
    public static final int SELECT_UNIT = 9;
    public static final int BOARD_HEX_POPUP = 10;

    private final int type;
    private final int modifiers;
    private final int entityId;
    private final int mouseButton;
    private final BoardLocation boardLocation;

    public BoardViewEvent(Object source, BoardLocation boardLocation, Entity entity, int type, int modifiers, int mouseButton) {
        this(source, boardLocation, entity == null ? Entity.NONE : entity.getId(), type, modifiers, mouseButton);
    }

    public BoardViewEvent(Object source, int type) {
        this(source, null, Entity.NONE, type, 0, 0);
    }

    public BoardViewEvent(Object source, int type, int entityId) {
        this(source, null, entityId, type, 0, 0);
    }

    public BoardViewEvent(Object source, BoardLocation boardLocation, int type) {
        this(source, boardLocation, Entity.NONE, type, 0, 0);
    }

    public BoardViewEvent(Object source, BoardLocation boardLocation, int type, int modifiers, int mouseButton) {
        this(source, boardLocation, Entity.NONE, type, modifiers, mouseButton);
    }

    private BoardViewEvent(Object source, BoardLocation boardLocation, int entityId, int type, int modifiers, int mouseButton) {
        super(source);
        this.entityId = entityId;
        this.type = type;
        this.modifiers = modifiers;
        this.boardLocation = boardLocation;
        this.mouseButton = mouseButton;
    }

    /**
     * Returns the type of event that this is
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the type of event that this is
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * @return the coordinate where this event occurred, if applicable;
     *         <code>null</code> otherwise.
     */
    public Coords getCoords() {
        return boardLocation.getCoords();
    }

    /**
     * @return the entity ID associated with this event, if applicable; 0
     *         otherwise.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * @return the id of the mouse button associated with this event if any.
     * <ul>
     * <li> 0 no button
     * <li> 1 Button 1
     * <li> 2 Button 2
     * <li> 3 Button 3
     * <li> 4 Button greater than 3
     * <li> 5 Button greater than 3
     * </ul>
     * <p>
     * 
     */
    public int getButton() {
        return mouseButton ;
    }

    public boolean hasLocation() {
        return (boardLocation != null) && (boardLocation.getCoords() != null);
    }

    public BoardLocation getBoardLocation() {
        return boardLocation;
    }

    public BoardView getBoardView() {
        return (BoardView) getSource();
    }
}
