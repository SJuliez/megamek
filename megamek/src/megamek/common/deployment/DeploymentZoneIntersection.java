package megamek.common.deployment;

import megamek.common.BoardLocation;
import megamek.common.Coords;
import megamek.common.Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeploymentZoneIntersection implements DeploymentZone {

    private final DeploymentZone zone1;
    private final DeploymentZone zone2;

    public DeploymentZoneIntersection(DeploymentZone zone1, DeploymentZone zone2) {
        this.zone1 = zone1;
        this.zone2 = zone2;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        return zone1.canDeployTo(game, coords, boardId) && zone2.canDeployTo(game, coords, boardId);
    }
}