package megamek.common.deployment;

import megamek.common.*;

import java.util.List;

public class TerrainDeploymentZone implements DeploymentZone {

    private final int minimumDistance;
    private final int maximumDistance;
    private final int boardId;
    private final Terrain terrain;

    TerrainDeploymentZone(int terrainType, int boardId) {
        this(terrainType, 0, 0, boardId);
    }

    TerrainDeploymentZone(int terrainType, int maximumDistance, int boardId) {
        this(terrainType, maximumDistance, 0, boardId);
    }

    TerrainDeploymentZone(int terrainType, int maximumDistance, int minimumDistance, int boardId) {
        this(new Terrain(terrainType, Terrain.WILDCARD), maximumDistance, minimumDistance, boardId);
    }

    TerrainDeploymentZone(Terrain terrain, int maximumDistance, int minimumDistance, int boardId) {
        this.maximumDistance = maximumDistance;
        this.minimumDistance = minimumDistance;
        this.boardId = boardId;
        this.terrain = terrain;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        if ((game != null) && (coords != null) && (this.boardId == boardId) && game.hasBoard(boardId)) {
            List<Coords> candidatesForTerrain = coords.allAtDistances(minimumDistance, maximumDistance);
            Board board = game.getBoard(boardId);
            for (Coords candidate : candidatesForTerrain) {
                Hex hex = board.getHex(candidate);
                if ((hex != null) && matchesTerrain(hex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesTerrain(Hex hex) {
        return (hex.containsTerrain(terrain.getType()) && (terrain.getLevel() == Terrain.WILDCARD))
                || hex.containsTerrain(terrain.getType(), terrain.getLevel());
    }
}