package megamek.common.deployment;

import megamek.common.Coords;
import megamek.common.Game;

public class InvertedDeploymentZone implements DeploymentZone {

    private final DeploymentZone targetZone;

    public InvertedDeploymentZone(DeploymentZone deploymentZone) {
        targetZone = deploymentZone;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        return !targetZone.canDeployTo(game, coords, boardId);
    }
}