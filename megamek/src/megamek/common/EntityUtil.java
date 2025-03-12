/*
 * Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 */
package megamek.common;

import megamek.common.equipment.AmmoMounted;
import megamek.common.equipment.MiscMounted;
import megamek.common.equipment.WeaponMounted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains utility methods for Entity that fall outside the scope of gameplay and thus are kept out of Entity
 * for size reasons. They are (at the time of introduction) ported from MML's UnitUtil which cannot be easily moved over to MM.
 */
public final class EntityUtil {

    /**
     * Removes a piece of equipment from the Entity
     *
     * @param unit  The entity The Entity
     * @param mount The equipment
     */
    public static void removeMounted(Entity unit, Mounted<?> mount) {
        removeCriticals(unit, mount);

        // Some special checks for BA
        if (unit instanceof BattleArmor) {
            // If we're removing a DWP and it has an attached weapon, we need
            // to detach the weapon
            if (mount.getType().hasFlag(MiscType.F_DETACHABLE_WEAPON_PACK)
                && (mount.getLinked() != null)) {
                Mounted<?> link = mount.getLinked();
                link.setDWPMounted(false);
                link.setLinked(null);
                link.setLinkedBy(null);
            }
            // If we are removing a weapon that is mounted in an DWP, we need
            // to clear the mounted status of the DWP
            if ((mount.getLinkedBy() != null)
                && mount.getLinkedBy().getType().hasFlag(MiscType.F_DETACHABLE_WEAPON_PACK)) {
                Mounted<?> dwp = mount.getLinkedBy();
                dwp.setLinked(null);
                dwp.setLinkedBy(null);
            }
            // If we're removing an APM and it has an attached weapon, we need
            // to detach the weapon
            if (mount.getType().hasFlag(MiscType.F_AP_MOUNT) && (mount.getLinked() != null)) {
                Mounted<?> link = mount.getLinked();
                link.setAPMMounted(false);
                link.setLinked(null);
                link.setLinkedBy(null);
            }
            // If we are removing a weapon that is mounted in an APM, we need
            // to clear the mounted status of the AP Mount
            if ((mount.getLinkedBy() != null)
                && mount.getLinkedBy().getType().hasFlag(MiscType.F_AP_MOUNT)) {
                Mounted<?> apm = mount.getLinkedBy();
                apm.setLinked(null);
                apm.setLinkedBy(null);
            }
        }
        // We will need to reset the equipment numbers of the bay ammo and weapons
        Map<WeaponMounted, List<WeaponMounted>> bayWeapons = new HashMap<>();
        Map<WeaponMounted, List<AmmoMounted>> bayAmmo = new HashMap<>();
        for (WeaponMounted bay : unit.getWeaponBayList()) {
            bayWeapons.put(bay, bay.getBayWeapons());
            bayAmmo.put(bay, bay.getBayAmmo());
        }
        // Some special checks for Aeros
        if (unit instanceof Aero) {
            if (mount instanceof WeaponMounted) {
                // Aeros have additional weapon lists that need to be cleared
                unit.getTotalWeaponList().remove(mount);
                unit.getWeaponBayList().remove(mount);
                unit.getWeaponGroupList().remove(mount);
            }
        }
        unit.getEquipment().remove(mount);
        if (mount instanceof MiscMounted) {
            unit.getMisc().remove(mount);
        } else if (mount instanceof AmmoMounted) {
            unit.getAmmo().remove(mount);
        } else if (mount instanceof WeaponMounted) {
            unit.getWeaponList().remove(mount);
            unit.getTotalWeaponList().remove(mount);
        }
        if (bayWeapons.containsKey(mount)) {
            bayWeapons.get(mount).forEach(w -> {
                removeCriticals(unit, w);
                changeMountStatus(unit, w, Entity.LOC_NONE, Entity.LOC_NONE, false);
            });
            bayAmmo.get(mount).forEach(a -> {
                removeCriticals(unit, a);
                Mounted<?> moveTo = findUnallocatedAmmo(unit, a.getType());
                if (null != moveTo) {
                    moveTo.setShotsLeft(moveTo.getBaseShotsLeft() + a.getBaseShotsLeft());
                    removeMounted(unit, a);
                }
                changeMountStatus(unit, a, Entity.LOC_NONE, Entity.LOC_NONE, false);
            });
            bayWeapons.remove(mount);
            bayAmmo.remove(mount);
        }
        for (WeaponMounted bay : bayWeapons.keySet()) {
            bay.clearBayWeapons();
            for (WeaponMounted w : bayWeapons.get(bay)) {
                if (mount != w) {
                    bay.addWeaponToBay(w);
                }
            }
        }
        for (WeaponMounted bay : bayAmmo.keySet()) {
            bay.clearBayAmmo();
            for (AmmoMounted a : bayAmmo.get(bay)) {
                if (mount != a) {
                    bay.addAmmoToBay(a);
                }
            }
        }
        // Remove ammo added for a one-shot launcher
        if ((mount.getType() instanceof WeaponType) && mount.isOneShot()) {
            List<AmmoMounted> osAmmo = new ArrayList<>();
            for (AmmoMounted ammo = (AmmoMounted) mount.getLinked(); ammo != null; ammo = (AmmoMounted) ammo
                .getLinked()) {
                osAmmo.add(ammo);
            }
            osAmmo.forEach(m -> {
                unit.getEquipment().remove(m);
                unit.getAmmo().remove(m);
            });
        }
        // It's possible that the equipment we are removing was linked to
        // something else, and so the linkedBy state may be set. We should
        // remove it. Using getLinked could be unreliable, so we'll brute force it
        // An example of this would be removing a linked Artemis IV FCS
        for (Mounted<?> m : unit.getEquipment()) {
            if (mount.equals(m.getLinkedBy())) {
                m.setLinkedBy(null);
            }
        }
        if ((mount.getType() instanceof MiscType)
            && (mount.getType().hasFlag(MiscType.F_HEAD_TURRET)
            || mount.getType().hasFlag(MiscType.F_SHOULDER_TURRET)
            || mount.getType().hasFlag(MiscType.F_QUAD_TURRET))) {
            for (Mounted<?> m : unit.getEquipment()) {
                if (m.getLocation() == mount.getLocation()) {
                    m.setMekTurretMounted(false);
                }
            }
        }
        if ((mount.getType() instanceof MiscType)
            && mount.getType().hasFlag(MiscType.F_SPONSON_TURRET)) {
            for (Mounted<?> m : unit.getEquipment()) {
                m.setSponsonTurretMounted(false);
            }
        }
        if ((mount.getType() instanceof MiscType)
            && mount.getType().hasFlag(MiscType.F_PINTLE_TURRET)) {
            for (Mounted<?> m : unit.getEquipment()) {
                if (m.getLocation() == mount.getLocation()) {
                    m.setPintleTurretMounted(false);
                }
            }
        }
        unit.recalculateTechAdvancement();
    }

    /**
     * Removes the given Mounted equipment from all the critical slots it is in.
     * <p>
     * In a superheavy mek, when the first Mounted in a crit slot with two Mounteds is removed, the second Mounted is moved up
     * to the first spot.
     * <p>
     * This method makes no other changes, i.e. crit slots are neither condensed nor resorted. The Mounted's location is
     * neither changed nor considered, nor is the Mounted removed from the equipment lists.
     *
     * @param unit    The Entity to change
     * @param mounted The equipment to remove
     */
    public static void removeCriticals(Entity unit, Mounted<?> mounted) {
        for (int location = 0; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                removeCritical(unit, mounted, location, slot);
            }
        }
    }

    /**
     * Removes the given Mounted equipment from all the critical slots it is in, but only in the given location.
     * <p>
     * In a superheavy mek, when the first Mounted in a crit slot with two Mounteds is removed, the second Mounted is moved up
     * to the first spot.
     * <p>
     * This method makes no other changes, i.e. crit slots are neither condensed nor resorted. The Mounted's location is
     * neither changed nor considered, nor is the Mounted removed from the equipment lists.
     *
     * @param unit     The Entity to change
     * @param mounted  The equipment to remove
     * @param location The location to check
     */
    public static void removeCriticals(Entity unit, Mounted<?> mounted, int location) {
        for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
            removeCritical(unit, mounted, location, slot);
        }
    }

    /**
     * Removes the given mounted from the given slot in the given location on the unit, if the slot exists and the
     * mounted is there. In a superheavy mek, when the first Mounted in a crit slot with two Mounteds is removed,
     * the second Mounted is moved up to the first spot.
     *
     * @param unit    The Entity
     * @param mounted The Mounted equipment
     * @param loc     The location on the unit
     * @param slot    The slot number to look at
     */
    private static void removeCritical(Entity unit, Mounted<?> mounted, int loc, int slot) {
        CriticalSlot cs = unit.getCritical(loc, slot);
        if ((cs != null) && (cs.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
            if (cs.getMount().equals(mounted)) {
                if (cs.getMount2() != null) {
                    // If there are two pieces of equipment in this slot, move up the second ...
                    cs.setMount(cs.getMount2());
                    cs.setMount2(null);
                } else {
                    // ... otherwise clear the slot
                    unit.setCritical(loc, slot, null);
                }
            } else if ((cs.getMount2() != null) && cs.getMount2().equals(mounted)) {
                cs.setMount2(null);
            }
        }
    }

    /**
     * Find unallocated ammo of the same type. Used by large aerospace units when removing ammo
     * from a location to find the group to add it to.
     *
     * @param unit The entity The Entity
     * @param at   The type of ammo to match
     * @return An unallocated non-oneshot ammo mount of the same type, or null if
     * there is not one.
     */
    public static Mounted<?> findUnallocatedAmmo(Entity unit, EquipmentType at) {
        for (Mounted<?> m : unit.getAmmo()) {
            if ((m.getLocation() == Entity.LOC_NONE) && at.equals(m.getType())
                && ((m.getLinkedBy() == null) || !m.getLinkedBy().getType().hasFlag(WeaponType.F_ONESHOT))) {
                return m;
            }
        }
        return null;
    }

    /**
     * For the given Mek, adds Clan CASE in every location that has potentially
     * explosive equipment (this includes PPC Capacitors) and removes it from all
     * other locations.
     * Calls {@link Mek#addClanCase()}. This method does not check if other
     * CASE types are already present on a location.
     *
     * @param mek the mek to update
     */
    public static void updateClanCasePlacement(Mek mek) {
        if (mek.isClan()) {
            removeAllMounteds(mek, EquipmentType.get(EquipmentTypeLookup.CLAN_CASE));
            mek.addClanCase();
        }
    }

    public static void removeAllMounteds(Entity unit, EquipmentType et) {
        for (int pos = unit.getEquipment().size() - 1; pos >= 0; pos--) {
            Mounted<?> mount = unit.getEquipment().get(pos);
            if (mount.getType().equals(et)) {
                removeMounted(unit, mount);
            }
        }
    }

    /**
     * Changes the location for a Mounted instance. Note: for BattleArmor, this
     * effects which suit the equipment is placed on (as that is what
     * Mounted.location means for BA), but not where on the suit
     * it's located (ie, BAMountLocation isn't affected). BattleArmor should
     * change this outside of this method.
     *
     * @param unit              The unit being modified
     * @param eq                The equipment to move
     * @param location          The location to move the mount to
     * @param secondaryLocation The secondary location for split equipment,
     *                          otherwise {@link Entity#LOC_NONE Entity.LOC_NONE}
     * @param rear              Whether to mount with a rear facing
     */
    public static void changeMountStatus(Entity unit, Mounted<?> eq, int location, int secondaryLocation, boolean rear) {
        if ((location != eq.getLocation() && !eq.isOneShot())) {
            removeDirectLinks(eq);
        }
        eq.setLocation(location, rear);
        eq.setSecondLocation(secondaryLocation, rear);
        eq.setSplit(secondaryLocation > -1);
        // If we're adding it to a location on the unit, check equipment linkages
        if (location > Entity.LOC_NONE) {
            try {
                MekFileParser.postLoadInit(unit);
            } catch (Exception ignored) {
                // Exception thrown for not having equipment to link to yet, which is acceptable
                // here
            }
        }
        if (unit instanceof Mek) {
            MekUtil.updateClanCasePlacement((Mek) unit);
        }
    }

    /**
     * Clears the linked() and linkedBy() content from the given mounted. For any linked/linkedBy other equipment, the back-link
     * to the given mounted on that other equipment is also cleared, regardless of what those back-links actually contained.
     * <p>
     * This method does not check if other, unrelated equipment has a link to the given mounted (in other words, the unit is
     * not scanned for links, only direct links are cleaned). This means, this method does not do any sort of error correction
     * on the unit.
     *
     * @param mounted The mounted equipment to unlink
     */
    private static void removeDirectLinks(Mounted<?> mounted) {
        if (mounted.getLinked() != null) {
            mounted.getLinked().setLinkedBy(null);
            mounted.setLinked(null);
        }
        if (mounted.getLinkedBy() != null) {
            mounted.getLinkedBy().setLinked(null);
            mounted.setLinkedBy(null);
        }
    }

    private EntityUtil() { }
}
