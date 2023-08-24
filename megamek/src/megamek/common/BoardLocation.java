package megamek.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * Returns a list of all six adjacent coordinates (distance = 1). Does not check if those are on the board or
     * if the board of the present boardId exists. This is equivalent to {@link Coords#allAdjacent()} with
     * the boardId of the present BoardLocation added in. This is also equivalent to calling 
     * allAtDistance(1). 
     *
     * @return A list of adjacent BoardLocations
     */
    public List<BoardLocation> allAdjacent() {
        return allAtDistance(1);
    }

    /**
     * Returns a list of all coordinates at the given distance dist. Does not check if those are on the board or
     * if the board of the present boardId exists. Returns an empty list for dist of less than 0 and the calling 
     * BoardLocation itself for dist == 0. This is equivalent to {@link Coords#allAtDistance(int)} with
     * the boardId of the present BoardLocation added in. 
     * 
     * @return A list of BoardLocations centered on this BoardLocation and at the given distance
     */
    public List<BoardLocation> allAtDistance(final int dist) {
        return coords.allAtDistance(dist).stream().map(c -> new BoardLocation(c, boardId)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all coordinates at the given distance dist
     * and anything less than dist as well.
     */
    public ArrayList<BoardLocation> allAtDistanceOrLess(int dist) {
        return allAtDistances(0, dist);
    }

    /**
     * Returns a list of all coordinates at the given distance dist
     * and anything less than dist as well.
     */
    public ArrayList<BoardLocation> allAtDistances(int minimumDistance, int maximumDistance) {
        ArrayList<BoardLocation> result = new ArrayList<>();
        for (int radius = minimumDistance; radius <= maximumDistance; radius++) {
            result.addAll(allAtDistance(radius));
        }
        return result;
    }

    /**
     * Returns the coordinate 1 unit in the specified direction dir.
     */
    public BoardLocation translated(int dir) {
        return new BoardLocation(coords.translated(dir, 1), boardId);
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
