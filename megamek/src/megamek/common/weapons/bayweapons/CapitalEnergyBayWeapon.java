package megamek.common.weapons.bayweapons;

import megamek.common.*;
import megamek.common.actions.OrbitToSurfaceAttackAction;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.weapons.AmmoBayWeaponHandler;
import megamek.common.weapons.AttackHandler;
import megamek.common.weapons.OrbitToSurfaceBayWeaponHandler;
import megamek.server.GameManager;

public abstract class CapitalEnergyBayWeapon extends BayWeapon {

    @Override
    protected AttackHandler getCorrectHandler(ToHitData toHit, WeaponAttackAction waa, Game game,
                                              GameManager manager) {
        if (waa instanceof OrbitToSurfaceAttackAction) {
            return new OrbitToSurfaceBayWeaponHandler(toHit, waa, game, manager);
        } else {
            return new AmmoBayWeaponHandler(toHit, waa, game, manager);
        }
    }
}
