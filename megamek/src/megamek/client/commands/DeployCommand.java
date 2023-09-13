/*
* MegaMek -
* Copyright (C) 2007 Ben Mazur (bmazur@sev.org)
* Copyright (C) 2018 The MegaMek Team
*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free Software
* Foundation; either version 2 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*/

package megamek.client.commands;

import megamek.client.Client;
import megamek.common.Coords;
import megamek.common.BoardLocation;
import megamek.common.MapType;

/**
 * @author dirk
 */
public class DeployCommand extends ClientCommand {

    /**
     * @param client
     */
    public DeployCommand(Client client) {
        super(
                client,
                "deploy",
                "This command deploys a given unit to the specified hex. " +
                        "Usage: '#deploy unit boardId x y facing' where unit is the unit id number and x and y " +
                        "are the coordinates of the hex, and facing is the direction it's looking in. #deploy " +
                        "without any options will provide legal deployment zones.");
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.client.commands.ClientCommand#run(java.lang.String[])
     */
    // FIXME: Add error checking
    @Override
    public String run(String[] args) {
        if (args.length == 1) {
            return "The legal deployment zone is: " + legalDeploymentZone();
        } else if (args.length == 5) {
            int id = Integer.parseInt(args[1]);
            int boardId = Integer.parseInt(args[2]);
            Coords coord = new Coords(Integer.parseInt(args[3]) - 1, Integer
                    .parseInt(args[4]) - 1);
            int nFacing = getDirection(args[5]);

            getClient().deploy(id, new BoardLocation(coord, boardId), nFacing, 0);
            return "Unit " + id + " deployed to " + coord.toFriendlyString()
                    + ". (this is assuming it worked. No error checking done.)";
        }

        return "Wrong number of arguments supplied. No deployment done.";
    }

    public String legalDeploymentZone() {
        int nDir = getClient().getLocalPlayer().getStartingPos();
        String deep = "";
        if (nDir > 10) {
            // Deep deployment, the board is effectively smaller
            nDir -= 10;
            deep = "Deep ";
        }
        switch (nDir) {
            case 0: // Any
                return deep + "Deploy nearly anywhere.";
            case 1: // NW
                return deep + "Deploy NW.";
            case 2: // N
                return deep + "Deploy N.";
            case 3: // NE
                return deep + "Deploy NE.";
            case 4: // E
                return deep + "Deploy E.";
            case 5: // SE
                return deep + "Deploy SE.";
            case 6: // S
                return deep + "Deploy S.";
            case 7: // SW
                return deep + "Deploy SW.";
            case 8: // W
                return deep + "Deploy W.";
            case 9: // Edge
                return deep + "Deploy at any edge.";
            case 10: // Centre
                return deep + "Deploy in the center.";
            default: // ummm. .
                return "Something went wrong, unknown deployment schema.";
        }
    }
}
