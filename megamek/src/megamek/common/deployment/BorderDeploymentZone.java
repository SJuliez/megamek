package megamek.common.deployment;

import megamek.common.Board;
import megamek.common.Coords;
import megamek.common.Game;

import static megamek.common.Board.*;

public class BorderDeploymentZone implements DeploymentZone {

    private final int edgeOffset;
    private final int width;
    private final int boardId;
    private final int borderType;

    public BorderDeploymentZone(int borderType, int boardId) {
        this(borderType, 3, 0, boardId);
    }

    public BorderDeploymentZone(int borderType, int width, int boardId) {
        this(borderType, width, 0, boardId);
    }

    public BorderDeploymentZone(int borderType, int width, int edgeOffset, int boardId) {
        this.width = width;
        this.edgeOffset = edgeOffset;
        this.boardId = boardId;
        this.borderType = borderType;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        if ((game == null) || (coords == null)
                || ((this.boardId != boardId) && (this.boardId != ANY_BOARD))) {
            return false;
        }
        Board board = game.getBoard(boardId);
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();
        int maxx = boardWidth - edgeOffset;
        int maxy = boardHeight - edgeOffset;

        switch (borderType) {
            case START_ANY:
                return true;
            case START_NW:
                return ((coords.getX() < (edgeOffset + width)) && (coords.getX() >= edgeOffset) && (coords.getY() >= edgeOffset) && (coords.getY() < (boardHeight / 2)))
                        || ((coords.getY() < (edgeOffset + width)) && (coords.getY() >= edgeOffset) && (coords.getX() >= edgeOffset) && (coords.getX() < (boardWidth / 2)));
            case START_N:
                return (coords.getY() < (edgeOffset + width)) && (coords.getY() >= edgeOffset);
            case START_NE:
                return ((coords.getX() >= (maxx - width)) && (coords.getX() < maxx) && (coords.getY() >= edgeOffset) && (coords.getY() < (boardHeight / 2)))
                        || ((coords.getY() < (edgeOffset + width)) && (coords.getY() >= edgeOffset) && (coords.getX() < maxx) && (coords.getX() > (boardWidth / 2)));
            case START_E:
                return (coords.getX() >= (maxx - width)) && (coords.getX() < maxx);
            case START_SE:
                return ((coords.getX() >= (maxx - width)) && (coords.getX() < maxx) && (coords.getY() < maxy) && (coords.getY() > (boardHeight / 2)))
                        || ((coords.getY() >= (maxy - width)) && (coords.getY() < maxy) && (coords.getX() < maxx) && (coords.getX() > (boardWidth / 2)));
            case START_S:
                return (coords.getY() >= (maxy - width)) && (coords.getY() < maxy);
            case START_SW:
                return ((coords.getX() < (edgeOffset + width)) && (coords.getX() >= edgeOffset) && (coords.getY() < maxy) && (coords.getY() > (boardHeight / 2)))
                        || ((coords.getY() >= (maxy - width)) && (coords.getY() < maxy) && (coords.getX() >= edgeOffset) && (coords.getX() < (boardWidth / 2)));
            case START_W:
                return (coords.getX() < (edgeOffset + width)) && (coords.getX() >= edgeOffset);
            case START_EDGE:
                return ((coords.getX() < (edgeOffset + width)) && (coords.getX() >= edgeOffset) && (coords.getY() >= edgeOffset) && (coords.getY() < maxy))
                        || ((coords.getY() < (edgeOffset + width)) && (coords.getY() >= edgeOffset) && (coords.getX() >= edgeOffset) && (coords.getX() < maxx))
                        || ((coords.getX() >= (maxx - width)) && (coords.getX() < maxx) && (coords.getY() >= edgeOffset) && (coords.getY() < maxy))
                        || ((coords.getY() >= (maxy - width)) && (coords.getY() < maxy) && (coords.getX() >= edgeOffset) && (coords.getX() < maxx));
            case START_CENTER:
                return (coords.getX() >= (boardWidth / 3)) && (coords.getX() <= ((2 * boardWidth) / 3)) && (coords.getY() >= (boardHeight / 3))
                        && (coords.getY() <= ((2 * boardHeight) / 3));
            default:
                return false;
        }
    }
}