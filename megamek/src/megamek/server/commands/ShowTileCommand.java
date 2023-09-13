package megamek.server.commands;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.Hex;
import megamek.common.options.OptionsConstants;
import megamek.server.GameManager;
import megamek.server.Server;

/**
 * This command exists to print tile information to the chat window and is primarily intended for
 * visually impaired users.
 * @author dirk
 */
public class ShowTileCommand extends ServerCommand {

    private final GameManager gameManager;

    public ShowTileCommand(Server server, GameManager gameManager) {
        super(server, "tile",
                "print the information about a tile into the chat window. " +
                        "Usage: /tile boardId 01 01 whih would show the details for the hex numbered 01 01.");
        this.gameManager = gameManager;
    }

    /**
     * Run this command with the arguments supplied
     *
     * @see megamek.server.commands.ServerCommand#run(int, java.lang.String[])
     */
    @Override
    public void run(int connId, String[] args) {
        try {
            int i = 3;
            String str = "";
            int boardId = Integer.parseInt(args[1]);
            Coords coord = new Coords(Integer.parseInt(args[2]) - 1, Integer.parseInt(args[3]) - 1);
            Hex hex;

            do {
                hex = gameManager.getGame().getBoard(boardId).getHex(coord);
                if (hex != null) {
                    str = "Details for hex (" + (coord.getX() + 1) + ", "
                          + (coord.getY() + 1) + ") : " + hex;

                    // if we are not playing in double-blind mode also list the
                    // units in this tile.
                    if (!server.getGame().getOptions().booleanOption(
                            OptionsConstants.ADVANCED_DOUBLE_BLIND)) {
                        List<Integer> entityIds = gameManager.getGame().getEntityIDsAt(coord, boardId);
                        List<String> idStrings = entityIds.stream().map(String::valueOf).collect(Collectors.toList());
                        if (!entityIds.isEmpty()) {
                            str += "; Contains entities: " + String.join(", ", idStrings);
                        }
                    }

                    server.sendServerChat(connId, str);
                } else {
                    server.sendServerChat(connId, "Hex (" + (coord.getX() + 1)
                            + ", " + (coord.getY() + 1) + ") is not on the board.");
                }

                if (i < args.length) {
                    coord = coord.translated(args[i]);
                }

                i++;
            } while (i < args.length);
        } catch (Exception ignored) {

        }
    }
}
