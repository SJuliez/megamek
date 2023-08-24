package megamek.common.deployment;

import megamek.common.Coords;
import megamek.common.Game;

public class DeploymentZoneDifference implements DeploymentZone {

    private final DeploymentZone zone1;
    private final DeploymentZone zone2;

    public DeploymentZoneDifference(DeploymentZone zone, DeploymentZone toSubtract) {
        zone1 = zone;
        zone2 = toSubtract;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        return zone1.canDeployTo(game, coords, boardId) && !zone2.canDeployTo(game, coords, boardId);
    }
}