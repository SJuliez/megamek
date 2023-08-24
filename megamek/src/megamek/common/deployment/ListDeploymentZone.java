package megamek.common.deployment;

import megamek.common.BoardLocation;
import megamek.common.Coords;
import megamek.common.Game;

import java.util.*;
import java.util.stream.Collectors;

public class ListDeploymentZone implements DeploymentZone {

    private final Set<BoardLocation> boardLocations = new HashSet<>();

    public ListDeploymentZone(Collection<BoardLocation> boardLocations) {
        this.boardLocations.addAll(boardLocations);
    }

    public ListDeploymentZone(int boardId, Collection<Coords> coords) {
        this(coords.stream().map(c -> new BoardLocation(c, boardId)).collect(Collectors.toSet()));
    }

    public ListDeploymentZone(int boardId, Coords coords, Coords... moreCoords) {
        this(boardId, collectToList(coords, moreCoords));
    }

    private static List<Coords> collectToList(Coords coords, Coords... moreCoords) {
        List<Coords> coordsList = new ArrayList<>();
        coordsList.add(coords);
        coordsList.addAll(Arrays.asList(moreCoords));
        return coordsList;
    }

    @Override
    public boolean canDeployTo(Game game, BoardLocation boardLocation) {
        return boardLocations.contains(boardLocation);
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        return canDeployTo(game, new BoardLocation(coords, boardId));
    }
}