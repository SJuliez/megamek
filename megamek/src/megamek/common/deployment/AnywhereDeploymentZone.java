package megamek.common.deployment;

import megamek.common.Coords;
import megamek.common.Game;

import java.util.HashSet;
import java.util.Set;

public class AnywhereDeploymentZone implements DeploymentZone {

    private final Set<Integer> boardIds = new HashSet<>();

    public AnywhereDeploymentZone(int... boardIds) {
        for (int boardId : boardIds) {
            this.boardIds.add(boardId);
        }
    }

    @Override
    public boolean canDeployTo(Game game, Coords coords, int boardId) {
        return boardIds.isEmpty() || boardIds.contains(boardId);
    }
}