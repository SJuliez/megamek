package megamek.common.weapons;

import megamek.common.Game;
import megamek.common.Report;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.enums.GamePhase;
import megamek.server.GameManager;

import java.util.Vector;

public class OrbitToSurfaceEnergyBayWeaponHandler extends OrbitToSurfaceBayWeaponHandler {

    public OrbitToSurfaceEnergyBayWeaponHandler(ToHitData t, WeaponAttackAction w, Game g, GameManager m) {
        super(t, w, g, m);
    }

    @Override
    protected void useAmmo() {
        // not necessary
    }

    @Override
    protected void prepareIncoming(GamePhase phase, Vector<Report> vPhaseReport) {
        // not necessary; hits directly
    }
}
