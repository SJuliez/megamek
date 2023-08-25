package megamek.common.deployment;

import megamek.common.*;

import java.util.Objects;

public class OwnerDeploymentZone implements DeploymentZone {

    private final Entity entity;

    public OwnerDeploymentZone(Entity entity) {
        this.entity = Objects.requireNonNull(entity);
    }

//    public OwnerDeploymentZone(int entityId) {
//        this.entityId = entityId;
//    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        if (game == null) {
            return false;
        }
//        Entity entity = game.getEntity(entityId);
//        if (entity == null) {
//            return false;
//        }
        Player owner = entity.getOwner();
        return (owner != null) && owner.getDeploymentZone().canDeployTo(game, coords, boardId);
    }
}