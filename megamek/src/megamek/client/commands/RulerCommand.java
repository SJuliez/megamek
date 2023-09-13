/*
 * MegaMek -
 * Copyright (C) 2007 Ben Mazur (bmazur@sev.org)
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
package megamek.client.commands;

import megamek.client.Client;
import megamek.common.*;

/**
 * @author dirk
 *         This is the ruler for LOS stuff implemented in command line.
 *         There should be a more intuitive ruler.
 */
public class RulerCommand extends ClientCommand {

    public RulerCommand(Client client) {
        super(
                client,
                "ruler",
                "Show Line of Sight (LOS) information between two points of the map. " +
                        "Usage: #ruler boardId x1 y1 x2 y2 [elev1 [elev2]]. " +
                        "Where x1, y1 and x2, y2 are the coordinates of the tiles, and the optional elev " +
                        "numbers are the elevations of the targets over the terrain. " +
                        "If elev is not given 1 is assumed which is for standing mechs. " +
                        "Prone mechs and most other units are at elevation 0. ");
    }

    @Override
    public String run(String[] args) {
        try {
            int elev1 = 1, elev2 = 1;
            Coords start = null, end = null;
            String toHit1 = "", toHit2 = "";
            ToHitData thd;

            int boardId = Integer.parseInt(args[1]);
            start = new Coords(Integer.parseInt(args[2]) - 1, Integer.parseInt(args[3]) - 1);
            end = new Coords(Integer.parseInt(args[4]) - 1, Integer.parseInt(args[5]) - 1);
            if (args.length > 5) {
                try {
                    elev1 = Integer.parseInt(args[6]);
                } catch (NumberFormatException e) {
                    // leave at default value
                }
                if (args.length > 6) {
                    try {
                        elev1 = Integer.parseInt(args[7]);
                    } catch (NumberFormatException e) {
                        // leave at default value
                    }
                }
            }

            Game game = getClient().getGame();
            thd = LosEffects.calculateLos(game,
                    LosEffects.buildAttackInfo(start, end, elev1, elev2,
                            game.getBoard(boardId).getHex(start).floor(),
                            game.getBoard(boardId).getHex(end).floor(), MapType.GROUND)
            ).losModifiers(game);

            if (thd.getValue() != TargetRoll.IMPOSSIBLE) {
                toHit1 = thd.getValue() + " because ";
            }
            toHit1 += thd.getDesc();

            thd = LosEffects.calculateLos(game,
                    LosEffects.buildAttackInfo(end, start, elev2, elev1,
                            game.getBoard().getHex(end).floor(),
                            game.getBoard().getHex(start).floor(), MapType.GROUND)
            ).losModifiers(game);

            if (thd.getValue() != TargetRoll.IMPOSSIBLE) {
                toHit2 = thd.getValue() + " because  ";
            }
            toHit2 += thd.getDesc();

            return "The ToHit from hex (" + (start.getX() + 1) + ", "
                    + (start.getY() + 1) + ") at elevation " + elev1 + " to ("
                    + (end.getX() + 1) + ", " + (end.getY() + 1) + ") at elevation "
                    + elev2 + " has a range of " + start.distance(end)
                    + " and a modifier of " + toHit1
                    + " and return fire has a modifier of " + toHit2 + ".";
        } catch (Exception ignored) {

        }

        return "Error parsing the ruler command. Usage: #ruler x1 y1 x2 y2 [elev1 [elev2]] where x1, y1, x2, y2, and the optional elev arguments are integers.";
    }
}
