package megamek.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a location (i.e. Coords) on the game board of a specific ID.
 * With a game having multiple maps, this class needs to replace Coords in many methods to identify a specific
 * position of an Entity or event.
 *
 * @implNote MapLocation is immutable.
 */
public class BoardLocation implements Serializable {

    private final Coords coords;
    private final int boardId;

    public BoardLocation(Coords coords, int boardId) {
        this.coords = coords;
        this.boardId = boardId;
    }

    public Coords getCoords() {
        return coords;
    }

    public int getBoardId() {
        return boardId;
    }

    public boolean isOnBoard(int boardId) {
        return this.boardId == boardId;
    }

    public boolean isAtCoords(Coords coords) {
        return this.coords.equals(coords);
    }

    @Override
    public String toString() {
        return coords + "; Map Id: " + boardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardLocation that = (BoardLocation) o;
        return boardId == that.boardId && Objects.equals(coords, that.coords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coords, boardId);
    }

    public String getBoardNum() {
        return coords.getBoardNum() + " (Map Id: " + boardId + ")";
    }

    public String toFriendlyString() {
        return coords.toFriendlyString() + " (Map Id: " + boardId + ")";
    }
}
