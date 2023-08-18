package megamek.common.weapons;

import megamek.common.*;
import megamek.common.actions.ArtilleryAttackAction;
import megamek.common.actions.OrbitToSurfaceAttackAction;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.enums.GamePhase;
import megamek.common.options.OptionsConstants;
import megamek.server.GameManager;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class OrbitToSurfaceBayWeaponHandler extends AmmoBayWeaponHandler {

    boolean handledAmmoAndReport = false;

    public OrbitToSurfaceBayWeaponHandler(ToHitData t, WeaponAttackAction w, Game g, GameManager m) {
        super(t, w, g, m);
    }

    @Override
    public boolean cares(final GamePhase phase) {
        return phase.isOffboard() || phase.isTargeting();
    }

    @Override
    protected void useAmmo() {
        nweaponsHit = weapon.getBayWeapons().size();
        for (int wId : weapon.getBayWeapons()) {
            Mounted bayW = ae.getEquipment(wId);
            // check the currently loaded ammo
            Mounted bayWAmmo = bayW.getLinked();

            if (bayWAmmo == null) {// Can't happen. w/o legal ammo, the weapon *shouldn't* fire.
                LogManager.getLogger().debug("Handler can't find any ammo! Oh no!");
            }

            int shots = bayW.getCurrentShots();
            //By default rules, we have just one ammo bin with at least 10 shots for each weapon in the bay,
            //so we'll track ammo normally and need to resolve attacks for all bay weapons.
            for (int i = 0; i < shots; i++) {
                if (null == bayWAmmo || bayWAmmo.getUsableShotsLeft() < 1) {
                    // try loading something else
                    ae.loadWeaponWithSameAmmo(bayW);
                    bayWAmmo = bayW.getLinked();
                }
                if (null != bayWAmmo) {
                    bayWAmmo.setShotsLeft(bayWAmmo.getBaseShotsLeft() - 1);
                }
            }
        }
    }

    protected void prepareIncoming(GamePhase phase, Vector<Report> vPhaseReport) {
        String artyMsg;
        ArtilleryAttackAction aaa = (ArtilleryAttackAction) waa;
        if (phase.isTargeting()) {
            if (!handledAmmoAndReport) {
                addHeat();
                // Report the firing itself
                Report r = new Report(3121);
                r.indent();
                r.newlines = 0;
                r.subject = subjectId;
                r.add(wtype.getName());
                r.add(aaa.getTurnsTilHit());
                vPhaseReport.addElement(r);
                Report.addNewline(vPhaseReport);
                handledAmmoAndReport = true;

                artyMsg = "Orbit-to-surface fire incoming, landing on round "
                        + (game.getRoundCount() + aaa.getTurnsTilHit())
                        + ", fired by "
                        + game.getPlayer(aaa.getPlayerId()).getName();
                game.addSpecialHexDisplay(
                        aaa.getTarget(game).getBoardLocation(),
                        new SpecialHexDisplay(
                                SpecialHexDisplay.Type.ARTILLERY_INCOMING, game
                                .getRoundCount() + aaa.getTurnsTilHit(),
                                game.getPlayer(aaa.getPlayerId()), artyMsg,
                                SpecialHexDisplay.SHD_OBSCURED_TEAM));
            }
            // if this is the last targeting phase before we hit,
            // make it so the firing entity is announced in the
            // off-board attack phase that follows.
            if (aaa.getTurnsTilHit() == 0) {
                setAnnouncedEntityFiring(false);
            }
        }

    }

    protected void checkSpotters(GamePhase phase, Vector<Report> vPhaseReport) {
        OrbitToSurfaceAttackAction aaa = (OrbitToSurfaceAttackAction) waa;
        final Vector<Integer> spottersBefore = aaa.getSpotterIds();
        final int playerId = aaa.getPlayerId();
        boolean isFlak = (target instanceof VTOL) || (target.isAero());
        Entity bestSpotter = null;

        // Are there any valid spotters?
        if ((null != spottersBefore) && !isFlak) {
            // fetch possible spotters now
            Iterator<Entity> spottersAfter = game.getSelectedEntities(new EntitySelector() {
                public int player = playerId;

                public Targetable targ = target;

                @Override
                public boolean accept(Entity entity) {
                    Integer id = entity.getId();
                    return (player == entity.getOwnerId())
                            && spottersBefore.contains(id)
                            && !LosEffects.calculateLOS(game, entity, targ, true).isBlocked()
                            && entity.isActive()
                            // airborne aeros can't spot for arty
                            && !(entity.isAero() && entity.isAirborne())
                            && !entity.isINarcedWith(INarcPod.HAYWIRE);
                }
            });

            // Out of any valid spotters, pick the best.
            while (spottersAfter.hasNext()) {
                Entity ent = spottersAfter.next();
                if (bestSpotter == null) {
                    bestSpotter = ent;
                } else if (ent.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)
                        && !bestSpotter.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)) {
                    bestSpotter = ent;
                } else if (ent.getCrew().getGunnery() < bestSpotter.getCrew().getGunnery()
                        && !bestSpotter.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)) {
                    bestSpotter = ent;
                } else if (bestSpotter.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)
                        && ent.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)) {
                    if (ent.getCrew().getGunnery() < bestSpotter.getCrew().getGunnery()) {
                        bestSpotter = ent;
                    }
                }
            }
        }

        // If at least one valid spotter, then get the benefits thereof.
        if (null != bestSpotter) {
            int foMod = 0;
            if (bestSpotter.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)) {
                foMod = -1;
            }
            int mod = (bestSpotter.getCrew().getGunnery() - 4) / 2;
            mod += foMod;
            toHit.addModifier(mod, "Spotting modifier");
        }
    }

    @Override
    public boolean handle(GamePhase phase, Vector<Report> vPhaseReport) {
        if (!cares(phase)) {
            return true;
        }
        if (ae == null) {
            LogManager.getLogger().error("Firing Entity is null!");
            return true;
        }

        OrbitToSurfaceAttackAction aaa = (OrbitToSurfaceAttackAction) waa;
        prepareIncoming(phase, vPhaseReport);

        if (aaa.getTurnsTilHit() > 0) {
            aaa.decrementTurnsTilHit();
            return true;
        }

        checkSpotters(phase, vPhaseReport);



        Coords targetPos = target.getPosition();
        boolean isFlak = (target instanceof VTOL) || (target.isAero());
        Entity bestSpotter = null;
        if (ae == null) {
            LogManager.getLogger().error("Artillery Entity is null!");
            return true;
        }

        Mounted ammoUsed = ae.getEquipment(aaa.getAmmoId());
        final AmmoType atype = (AmmoType) ammoUsed.getType();


        // Is the attacker still alive and we're not shooting FLAK?
        // then adjust the target
        if (!isFlak) {
            // If the shot hit the target hex, then all subsequent
            // fire will hit the hex automatically.
            if (roll >= toHit.getValue()) {
                ae.aTracker.setModifier(TargetRoll.AUTOMATIC_SUCCESS, targetPos);
            } else if (null != bestSpotter) {
                // If the shot missed, but was adjusted by a spotter, future shots are more likely
                // to hit.
                // Note: Because artillery fire is adjusted on a per-unit basis, this can result in
                // a unit firing multiple artillery weapons at the same hex getting this bonus more
                // than once per turn. Since the Artillery Modifiers Table on TacOps p. 180 lists a
                // -1 per shot (not salvo!) previously fired at the target hex, this would in fact
                // appear to be correct.

                // only add mods if it's not an automatic success
                if (ae.aTracker.getModifier(weapon, targetPos) != TargetRoll.AUTOMATIC_SUCCESS) {
                    if (bestSpotter.hasAbility(OptionsConstants.MISC_FORWARD_OBSERVER)) {
                        ae.aTracker.setSpotterHasForwardObs(true);
                    }
                    ae.aTracker.setModifier(ae.aTracker.getModifier(weapon, targetPos) - 1, targetPos);
                }
            }
        }

        // Report weapon attack and its to-hit value.
        Report r = new Report(3120);
        r.indent();
        r.newlines = 0;
        r.subject = subjectId;
        if (wtype != null) {
            r.add(wtype.getName());
        } else {
            r.add("Error: From Nowhere");
        }

        r.add(target.getDisplayName(), true);
        vPhaseReport.addElement(r);
        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(3135);
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
            return false;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_FAIL) {
            r = new Report(3140);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(3145);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else {
            // roll to hit
            r = new Report(3150);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit);
            vPhaseReport.addElement(r);
        }

        // dice have been rolled, thanks
        r = new Report(3155);
        r.newlines = 0;
        r.subject = subjectId;
        r.add(roll);
        vPhaseReport.addElement(r);

        // do we hit?
        bMissed = roll < toHit.getValue();
        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));

        // Do this stuff first, because some weapon's miss report reference the
        // amount of shots fired and stuff.
        if (!handledAmmoAndReport) {
            addHeat();
        }

        Coords hitCoords = targetPos;
        if (!bMissed) {
            r = new Report(3199);
            r.subject = subjectId;
            r.add(nweaponsHit);
            r.add(targetPos.getBoardNum());
            r.add(atype.getShortName());
            String note = "Orbit-to-surface attack hit here on round " + game.getRoundCount()
                    + ", fired by " + game.getPlayer(aaa.getPlayerId()).getName()
                    + " (this hex is now an auto-hit)";
            game.addSpecialHexDisplay(targetPos, target.getBoardLocation().getBoardId(),
                    new SpecialHexDisplay(SpecialHexDisplay.Type.ARTILLERY_HIT,
                            game.getRoundCount(), game.getPlayer(aaa.getPlayerId()), note));
        } else {
            Coords origPos = targetPos;
            int moF = toHit.getMoS();
            if (ae.hasAbility(OptionsConstants.GUNNERY_OBLIQUE_ARTILLERY)) {
                // getMoS returns a negative MoF
                // simple math is better so lets make it positive
                if ((-moF - 2) < 1) {
                    moF = 0;
                } else {
                    moF = moF + 2;
                }
            }
            // We're only going to display one missed shot hex on the board, at the intended target
            String note = "Artillery missed here on round "
                    + game.getRoundCount() + ", fired by "
                    + game.getPlayer(aaa.getPlayerId()).getName();
            game.addSpecialHexDisplay(origPos, target.getBoardLocation().getBoardId(),
                    new SpecialHexDisplay(SpecialHexDisplay.Type.ARTILLERY_HIT, game.getRoundCount(),
                            game.getPlayer(aaa.getPlayerId()), note));
            //We'll generate a new report and scatter for each weapon fired
            hitCoords = Compute.scatterDirectArty(targetPos, moF * 2);
            if (game.getBoard(ae).contains(targetPos)) {
                // misses and scatters to another hex
                if (!isFlak) {
                    r = new Report(3202).subject(subjectId).newLines(1);
                    r.add(atype.getShortName());
                    r.add(targetPos.getBoardNum());
                    vPhaseReport.addElement(r);
                } else {
                    r = new Report(3192).subject(subjectId).newLines(1);
                    r.add(targetPos.getBoardNum());
                    vPhaseReport.addElement(r);
                }
            } else {
                // misses and scatters off-board
                vPhaseReport.addElement(new Report(3200).subject(subjectId).newLines(1));
                return true;
            }
        }

        dealDamage(hitCoords, vPhaseReport);

        return false;
    }

    protected void dealDamage(Coords coords, Vector<Report> vPhaseReport) {
        ArtilleryAttackAction aaa = (ArtilleryAttackAction) waa;
        Mounted ammoUsed = ae.getEquipment(aaa.getAmmoId());
        final AmmoType atype = (AmmoType) ammoUsed.getType();
        BoardLocation targetLocation = new BoardLocation(coords, target.getBoardId());

        possiblyClearMinefield(coords, vPhaseReport);
        gameManager.artilleryDamageArea(targetLocation, aaa.getCoords(), atype, subjectId, ae,
                calcAttackValue() * 10, calcAttackValue() * 2, false, 0, vPhaseReport, false);
    }

    protected void possiblyClearMinefield(Coords coords, Vector<Report> vPhaseReport) {
        BoardLocation targetLocation = new BoardLocation(coords, target.getBoardId());
        if (game.hasMinefieldAt(targetLocation)) {
            ArrayList<Minefield> mfRemoved = new ArrayList<>();
            for (Minefield minefield : game.getMinefields(targetLocation)) {
                if (gameManager.clearMinefield(minefield, ae, 10, vPhaseReport)) {
                    mfRemoved.add(minefield);
                }
            }
            for (Minefield mf : mfRemoved) {
                gameManager.removeMinefield(mf);
            }
        }
    }
}