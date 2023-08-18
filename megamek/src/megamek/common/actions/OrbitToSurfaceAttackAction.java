package megamek.common.actions;

import megamek.common.*;

public class OrbitToSurfaceAttackAction extends ArtilleryAttackAction {

    public OrbitToSurfaceAttackAction(int entityId, int targetType, int targetId,
                                      int weaponId, Game game) {
        super(entityId, targetType, targetId, weaponId, game);
        EquipmentType eType = getEntity(game).getEquipment(weaponId).getType();
        WeaponType wType = (WeaponType) eType;
        Mounted mounted = getEntity(game).getEquipment(weaponId);
        if (wType.hasFlag(WeaponType.F_DIRECT_FIRE) && wType.hasFlag(WeaponType.F_ENERGY)) {
            turnsTilHit = 0;
        } else if (wType.hasFlag(WeaponType.F_DIRECT_FIRE) && wType.hasFlag(WeaponType.F_BALLISTIC)) {
            turnsTilHit = 1;
        } else if (wType.hasFlag(WeaponType.F_MISSILE)) {
            // @@MultiBoardTODO: Leave open and decide by the server?
            turnsTilHit = Compute.d6(1);
        }

    }

}
