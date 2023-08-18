package megamek.common.weapons.bayweapons;

import megamek.common.*;
import megamek.common.actions.OrbitToSurfaceAttackAction;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.weapons.AmmoBayWeaponHandler;
import megamek.common.weapons.AttackHandler;
import megamek.common.weapons.OrbitToSurfaceBayWeaponHandler;
import megamek.server.GameManager;

public abstract class CapitalAmmoBayWeapon extends AmmoBayWeapon {

    @Override
    protected AttackHandler getCorrectHandler(ToHitData toHit, WeaponAttackAction waa, Game game,
                                              GameManager manager) {
        if (waa instanceof OrbitToSurfaceAttackAction) {
            Entity ae = game.getEntity(waa.getEntityId());
            boolean useHoming = false;
            for (int wId : ae.getEquipment(waa.getWeaponId()).getBayWeapons()) {
                Mounted bayW = ae.getEquipment(wId);
                // check the currently loaded ammo
                Mounted bayWAmmo = bayW.getLinked();
                waa.setAmmoId(ae.getEquipmentNum(bayWAmmo));
                waa.setAmmoMunitionType(((AmmoType) bayWAmmo.getType()).getMunitionType());
                waa.setAmmoCarrier(ae.getId());
                if (bayWAmmo.isHomingAmmoInHomingMode()) {
                    useHoming = true;
                }
                //We only need to get this information for the first weapon in the bay to return the right handler
                break;
            }
            return new OrbitToSurfaceBayWeaponHandler(toHit, waa, game, manager);
        } else {
            return new AmmoBayWeaponHandler(toHit, waa, game, manager);
        }
    }
}
