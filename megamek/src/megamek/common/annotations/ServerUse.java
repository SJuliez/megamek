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
package megamek.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * This annotation on a method indicates that this method is intended to be used ONLY from the Server
 * (including GameManager), but not from the Client (including ClientGUI, ChatLounge, MovementDisplay etc.).
 * This is usually the case for methods that change the Game, Board and Entity objects directly. The client
 * should usually receive these changes as delivered by the server.
 * Note: Outside a running game with clients and server the method may be used freely, such as in MML, MHQ or
 * the ScenarionLoader or MegaMekGUI (= main menu).
 * Note: Many methods may be used by both client and server as e.g. the client also may change an entity
 * and then send the entity to the server to be distributed. This annotation is for those methods that are
 * server exclusive.
 * Note: A client side method may forward to a method marked with this annotation as part of what it does.
 */
@Target({ElementType.METHOD})
@Documented
@Inherited
public @interface ServerUse {

}