package megamek.client.commands;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import megamek.client.Client;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.Hex;
import megamek.common.options.OptionsConstants;

/**
 * This command exists to print tile information to the chat window, it's primarily intended for
 * visually impaired users.
 * @author dirk
 */
public class ShowTileCommand extends ClientCommand {
    public final static Set<String> directions = new HashSet<>();
    static {
        directions.add("N");
        directions.add("NW");
        directions.add("NE");
        directions.add("S");
        directions.add("SW");
        directions.add("SE");
    }

    public ShowTileCommand(Client client) {
        super(
                client,
                "tile",
                "print the information about a tile into the chat window. " +
                        "Usage: #tile 01 01 [dir1 ...] which would show the details for the hex numbered 01 01. " +
                        "The command can be followed with any number of directions (N,NE,SE,S,SW,NW) to list " +
                        "the tiles following those directions. Updates Current Hex. " +
                        "Can also list just directions to look from current tile.");
    }

    /**
     * Run this command with the arguments supplied
     *
     * @see megamek.server.commands.ServerCommand#run(int, java.lang.String[])
     */
    @Override
    public String run(String[] args) {
        try {
            int i = 2;
            String str, report = "";
            Coords coord = new Coords(Integer.parseInt(args[0]) - 1, Integer.parseInt(args[1]) - 1);
            int boardId = Integer.parseInt(args[2]);
            Hex hex;

            do {
                hex = getClient().getGame().getBoard(boardId).getHex(coord);
                if (hex != null) {
                    str = "Details for hex (" + (coord.getX() + 1) + ", "
                          + (coord.getY() + 1) + ") : " + hex;

                    // if we are not playing in double-blind mode also list the
                    // units in this tile.
                    if (!getClient().getGame().getOptions().booleanOption(OptionsConstants.ADVANCED_DOUBLE_BLIND)) {
                        List<Integer> entityIds = getClient().getGame().getEntityIDsAt(coord, boardId);
                        List<String> idStrings = entityIds.stream().map(String::valueOf).collect(Collectors.toList());
                        if (!entityIds.isEmpty()) {
                            str += "; Contains entities: " + String.join(", ", idStrings);
                        }
                    }

                    report = report + str + "\n";
                } else {
                    report = report + "Hex (" + (coord.getX() + 1) + ", "
                             + (coord.getY() + 1) + ") is not on the board.\n";
                }

                if (i < args.length) {
                    coord = coord.translated(args[i]);
                }

                i++;
            } while (i <= args.length);

            return report;
        } catch (Exception ignored) {

        }

        return "Error parsing the command.";
    }
}
