package megamek.common.deployment;

import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.Game;

public class UnitRelativeDeploymentZone implements DeploymentZone {

    private final int unitId;
    private final int minimumDistance;
    private final int maximumDistance;

    UnitRelativeDeploymentZone(int unitId, int minimumDistance, int maximumDistance) {
        this.unitId = unitId;
        this.minimumDistance = minimumDistance;
        this.maximumDistance = maximumDistance;
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        if ((game != null) && (coords != null) && !mustWaitForOtherDeployment(game)) {
            Entity entity = game.getEntity(unitId);
            if ((entity != null) && (entity.getBoardId() == boardId)) {
                int distance = coords.distance(entity.getPosition());
                return (distance >= minimumDistance) && (distance <= maximumDistance);
            }
        }
        return false;
    }

    @Override
    public boolean mustWaitForOtherDeployment(Game game) {
        // If the other unit doesn't exist we must not wait for it to deploy, but rather allow the present one
        // to be removed as undeployable
        return (game != null) && (game.getEntity(unitId) != null) && !game.getEntity(unitId).isDeployed();
    }
}