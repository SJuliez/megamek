/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */

package megamek.client.generator;

import megamek.client.ratgenerator.ForceDescriptor;
import megamek.client.ui.swing.ClientGUI;
import megamek.client.ui.swing.dialog.AbstractUnitSelectorDialog;
import megamek.codeUtilities.ObjectUtility;
import megamek.common.*;
import megamek.common.containers.MunitionTree;
import megamek.common.equipment.AmmoMounted;
import megamek.common.equipment.ArmorType;
import megamek.common.options.GameOptions;
import megamek.common.options.OptionsConstants;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Map.entry;

/**
 * Notes: check out
 * - RATGenerator.java
 * - ForceDescriptor.java
 * for era-based search examples
 */

public class TeamLoadoutGenerator {

    //region Constants
    // XML file containing flat list of weights; if not found, defaults used.  If found, defaults overridden.
    public static final String LOADOUT_SETTINGS_PATH = "mmconf" + File.separator + "munitionLoadoutSettings.xml";
    public static Properties weightProperties = new Properties();
    static {
        try (InputStream is = new FileInputStream(LOADOUT_SETTINGS_PATH)) {
            weightProperties.loadFromXML(is);
        } catch (Exception e) {
            LogManager.getLogger().error("Munition weight properties could not be loaded!  Using defaults...", e);
        }
    }

    public static final ArrayList<String> AP_MUNITIONS = new ArrayList<>(List.of(
            "Armor-Piercing", "Tandem-Charge"));

    public static final ArrayList<String> FLAK_MUNITIONS = new ArrayList<>(List.of(
            "ADA", "Cluster", "Flak", "AAAMissile Ammo", "LAAMissile Ammo"));

    public static final ArrayList<String> ACCURATE_MUNITIONS = new ArrayList<>(List.of(
            "Precision"));

    public static final ArrayList<String> HIGH_POWER_MUNITIONS = new ArrayList<>(List.of(
            "Tandem-Charge", "Fuel-Air", "HE", "Dead-Fire", "Davy Crockett-M",
            "ASMissile Ammo", "FABombLarge Ammo", "FABombSmall Ammo", "AlamoMissile Ammo"));

    public static final ArrayList<String> ANTI_INF_MUNITIONS = new ArrayList<>(List.of(
            "Inferno", "Fragmentation", "Flechette", "Fuel-Air", "Anti-personnel", "Acid",
            "FABombSmall Ammo", "ClusterBomb", "HEBomb"));

    public static final ArrayList<String> ANTI_BA_MUNITIONS = new ArrayList<>(List.of(
            "Inferno", "Fuel-Air", "Tandem-Charge", "Acid", "FABombSmall Ammo", "HEBomb"));

    public static final ArrayList<String> HEAT_MUNITIONS = new ArrayList<>(List.of(
            "Inferno", "Incendiary", "InfernoBomb"));

    public static final ArrayList<String> ILLUM_MUNITIONS = new ArrayList<>(List.of(
            "Illumination", "Tracer", "Inferno", "Incendiary", "Flare", "InfernoBomb"));

    public static final ArrayList<String> UTILITY_MUNITIONS = new ArrayList<>(List.of(
            "Illumination", "Smoke", "Mine Clearance", "Anti-TSM", "Laser Inhibiting",
            "Thunder", "FASCAM", "Thunder-Active", "Thunder-Augmented", "Thunder-Vibrabomb",
            "Thunder-Inferno", "Flare", "ThunderBomb", "TAGBomb", "TorpedoBomb", "ASEWMissile Ammo"));

    public static final ArrayList<String> GUIDED_MUNITIONS = new ArrayList<>(List.of(
            "Semi-Guided", "Narc-capable", "Homing", "Copperhead", "LGBomb", "ArrowIVHomingMissile Ammo"));

    // TODO Anti-Radiation Missiles See IO pg 62 (TO 368)
    public static final ArrayList<String> SEEKING_MUNITIONS = new ArrayList<>(List.of(
            "Heat-Seeking", "Listen-Kill", "Swarm", "Swarm-I"));

    public static final ArrayList<String> AMMO_REDUCING_MUNITIONS = new ArrayList<>(List.of(
            "Acid", "Laser Inhibiting", "Follow The Leader", "Heat-Seeking", "Tandem-Charge",
            "Thunder-Active", "Thunder-Augmented", "Thunder-Vibrabomb", "Thunder-Inferno",
            "AAAMissile Ammo", "ASMissile Ammo", "ASWEMissile Ammo", "ArrowIVMissile Ammo",
            "AlamoMissile Ammo"));

    public static final ArrayList<String> TYPE_LIST = new ArrayList<String>(List.of(
            "LRM", "SRM", "AC", "ATM", "Arrow IV", "Artillery", "Artillery Cannon",
            "Mek Mortar", "Narc", "Bomb"));

    public static final Map<String, ArrayList<String>> TYPE_MAP = Map.ofEntries(
            entry("LRM", MunitionTree.LRM_MUNITION_NAMES),
            entry("SRM", MunitionTree.SRM_MUNITION_NAMES),
            entry("AC", MunitionTree.AC_MUNITION_NAMES),
            entry("ATM", MunitionTree.ATM_MUNITION_NAMES),
            entry("Arrow IV", MunitionTree.ARROW_MUNITION_NAMES),
            entry("Artillery", MunitionTree.ARTILLERY_MUNITION_NAMES),
            entry("Artillery Cannon", MunitionTree.MEK_MORTAR_MUNITION_NAMES),
            entry("Mek Mortar", MunitionTree.MEK_MORTAR_MUNITION_NAMES),
            entry("Narc", MunitionTree.NARC_MUNITION_NAMES),
            entry("Bomb", MunitionTree.BOMB_MUNITION_NAMES));

    // subregion Bombs
    // bomb types assignable to aerospace units on ground maps
    private static final int[] validBotBombs = { BombType.B_HE, BombType.B_CLUSTER, BombType.B_RL,
            BombType.B_INFERNO, BombType.B_THUNDER, BombType.B_FAE_SMALL, BombType.B_FAE_LARGE,
            BombType.B_LG, BombType.B_ARROW, BombType.B_HOMING, BombType.B_TAG };
    private static final int[] validBotAABombs = { BombType.B_RL, BombType.B_LAA, BombType.B_AAA };

    /**
     * External ordnance types that rely on TAG
     */
    private static final Collection<Integer> GUIDED_ORDNANCE = new HashSet<>(Arrays.asList(BombType.B_LG, BombType.B_HOMING));

    /**
     * Relative weight distribution of various external ordnance choices for non-pirate forces
     */
    private static final Map<String, Integer> bombMapGroundSpread = Map.ofEntries (
            Map.entry("Normal", castPropertyInt("bombMapGroundSpreadNormal", 6)),
            Map.entry("Anti-Mek", castPropertyInt("bombMapGroundSpreadAnti-Mek", 3)),
            Map.entry("Anti-conventional", castPropertyInt("bombMapGroundSpreadAnti-conventional", 2)),
            Map.entry("Standoff", castPropertyInt("bombMapGroundSpreadStandoff", 1)),
            Map.entry("Strike", castPropertyInt("bombMapGroundSpreadStrike", 2))
    );

    /**
     * Relative weight distribution of various external ordnance choices for pirate forces
     */
    private static final Map<String, Integer> bombMapPirateGroundSpread = Map.ofEntries(
            Map.entry("Normal", castPropertyInt("bombMapPirateGroundSpreadNormal", 7)),
            Map.entry("Firestorm", castPropertyInt("bombMapPirateGroundSpreadFirestorm", 3))
    );

    /**
     * Relative weight distribution of general purpose external ordnance choices
     */
    private static final Map<Integer, Integer> normalBombLoad = Map.ofEntries(
            Map.entry(BombType.B_HE, castPropertyInt("normalBombLoad_HE", 40)),
            Map.entry(BombType.B_LG, castPropertyInt("normalBombLoad_LG", 5)),
            Map.entry(BombType.B_CLUSTER, castPropertyInt("normalBombLoad_CLUSTER", 30)),
            Map.entry(BombType.B_INFERNO, castPropertyInt("normalBombLoad_INFERNO", 15)),
            Map.entry(BombType.B_THUNDER, castPropertyInt("normalBombLoad_THUNDER", 10))
    );

    /**
     * Relative weight distribution of external ordnance choices for use against Mechs
     */
    private static final Map<Integer,Integer> antiMekBombLoad = Map.ofEntries(
            Map.entry(BombType.B_HE, castPropertyInt("antiMekBombLoad_HE", 55)),
            Map.entry(BombType.B_LG, castPropertyInt("antiMekBombLoad_LG", 15)),
            Map.entry(BombType.B_INFERNO, castPropertyInt("antiMekBombLoad_INFERNO", 10)),
            Map.entry(BombType.B_THUNDER, castPropertyInt("antiMekBombLoad_THUNDER", 10)),
            Map.entry(BombType.B_HOMING, castPropertyInt("antiMekBombLoad_HOMING", 10))
    );

    /**
     * Relative weight distribution of external ordnance choices for use against ground vehicles
     * and infantry
     */
    private static final Map<Integer,Integer> antiConvBombLoad = Map.ofEntries(
            Map.entry(BombType.B_CLUSTER, castPropertyInt("antiConvBombLoad_CLUSTER", 50)),
            Map.entry(BombType.B_INFERNO, castPropertyInt("antiConvBombLoad_INFERNO", 40)),
            Map.entry(BombType.B_THUNDER, castPropertyInt("antiConvBombLoad_THUNDER", 8)),
            Map.entry(BombType.B_FAE_SMALL, castPropertyInt("antiConvBombLoad_FAE_SMALL", 2))
    );

    /**
     * Relative weight distribution of external ordnance choices for providing artillery support
     */
    private static final Map<Integer,Integer> standoffBombLoad = Map.ofEntries(
            Map.entry(BombType.B_ARROW, castPropertyInt("standoffBombLoad_ARROW", 40)),
            Map.entry(BombType.B_HOMING, castPropertyInt("standoffBombLoad_HOMING", 60))
    );

    /**
     * Relative weight distribution of external ordnance choices for attacking static targets
     */
    private static final Map<Integer,Integer> strikeBombLoad = Map.ofEntries(
            Map.entry(BombType.B_LG, castPropertyInt("strikeBombLoad_LG", 45)),
            Map.entry(BombType.B_HOMING, castPropertyInt("strikeBombLoad_HOMING", 25)),
            Map.entry(BombType.B_HE, castPropertyInt("strikeBombLoad_HE", 30))
    );

    /**
     * Relative weight distribution of external ordnance choices for low tech forces. Also used as
     * a default/fall-back selection.
     */
    private static final Map<Integer,Integer> lowTechBombLoad = Map.ofEntries(
            Map.entry(BombType.B_HE, castPropertyInt("lowTechBombLoad_HE", 35)),
            Map.entry(BombType.B_RL, castPropertyInt("lowTechBombLoad_RL", 65))
    );

    /**
     * Relative weight distribution of external ordnance choices for pirates. Low tech, high chaos
     * factor.
     */
    private static final Map<Integer,Integer> pirateBombLoad = Map.ofEntries(
            Map.entry(BombType.B_HE, castPropertyInt("pirateBombLoad_HE", 7)),
            Map.entry(BombType.B_RL, castPropertyInt("pirateBombLoad_RL", 45)),
            Map.entry(BombType.B_INFERNO, castPropertyInt("pirateBombLoad_INFERNO", 35)),
            Map.entry(BombType.B_CLUSTER, castPropertyInt("pirateBombLoad_CLUSTER", 5)),
            Map.entry(BombType.B_FAE_SMALL, castPropertyInt("pirateBombLoad_FAE_SMALL", 6)),
            Map.entry(BombType.B_FAE_LARGE, castPropertyInt("pirateBombLoad_FAE_LARGE", 2))
    );

    /**
     * External ordnance choices for pirates to set things on fire
     */
    private static final Map<Integer, Integer> pirateFirestormBombLoad = Map.ofEntries(
            Map.entry(BombType.B_INFERNO, castPropertyInt("pirateFirestormBombLoad_INFERNO", 60)),
            Map.entry(BombType.B_FAE_SMALL, castPropertyInt("pirateFirestormBombLoad_FAE_SMALL", 30)),
            Map.entry(BombType.B_FAE_LARGE, castPropertyInt("pirateFirestormBombLoad_FAE_LARGE", 10))
    );

    /**
     * External ordnance choices for air-to-air combat
     */
    private static final Map<Integer,Integer> antiAirBombLoad = Map.ofEntries(
            Map.entry(BombType.B_RL, castPropertyInt("antiAirBombLoad_RL", 40)),
            Map.entry(BombType.B_LAA, castPropertyInt("antiAirBombLoad_LAA", 40)),
            Map.entry(BombType.B_AAA, castPropertyInt("antiAirBombLoad_AAA", 15)),
            Map.entry(BombType.B_AS, castPropertyInt("antiAirBombLoad_AS", 4)),
            Map.entry(BombType.B_ASEW, castPropertyInt("antiAirBombLoad_ASEW", 1))
    );

    /**
     * External ordnance choices for attacking DropShips and other large craft
     */
    private static final Map<Integer, Integer> antiShipBombLoad = Map.ofEntries(
            Map.entry(BombType.B_AAA, castPropertyInt("antiShipBombLoad_AAA", 50)),
            Map.entry(BombType.B_AS, castPropertyInt("antiShipBombLoad_AS", 35)),
            Map.entry(BombType.B_ASEW, castPropertyInt("antiShipBombLoad_ASEW", 15))
    );

    /**
     * External ordnance choices for pirate air-to-air combat. Selects fewer high tech choices than
     * the standard loadout.
     */
    private static final Map<Integer,Integer> pirateAirBombLoad = Map.ofEntries(
            Map.entry(BombType.B_RL, castPropertyInt("pirateAntiBombLoad_RL", 60)),
            Map.entry(BombType.B_LAA, castPropertyInt("pirateAntiBombLoad_LAA", 30)),
            Map.entry(BombType.B_AAA, castPropertyInt("pirateAntiBombLoad_AAA", 10))
    );

    //endsubregion Bombs
    //endregion Constants

    private static ClientGUI cg;
    private static Game game;

    protected GameOptions gameOptions = null;
    protected boolean enableYearLimits = false;
    protected int allowedYear = AbstractUnitSelectorDialog.ALLOWED_YEAR_ANY;
    protected int gameTechLevel = TechConstants.T_SIMPLE_INTRO;
    protected SimpleTechLevel legalLevel;
    protected boolean eraBasedTechLevel = false;
    protected boolean advAeroRules = false;
    protected boolean showExtinct = false;
    protected boolean trueRandom = false;
    protected String defaultBotMunitionsFile = null;

    public TeamLoadoutGenerator(ClientGUI gui) {
        cg = gui;
        game = cg.getClient().getGame();
        gameOptions = game.getOptions();
        updateOptionValues();
    }

    public TeamLoadoutGenerator(ClientGUI gui, String defaultSettings) {
        this(gui);
        this.defaultBotMunitionsFile = defaultSettings;
    }

    public void updateOptionValues() {
        gameOptions = cg.getClient().getGame().getOptions();
        enableYearLimits = true;
        allowedYear = gameOptions.intOption(OptionsConstants.ALLOWED_YEAR);
        gameTechLevel = TechConstants.getSimpleLevel(gameOptions.stringOption(OptionsConstants.ALLOWED_TECHLEVEL));
        legalLevel = SimpleTechLevel.getGameTechLevel(game);
        eraBasedTechLevel = gameOptions.booleanOption(OptionsConstants.ALLOWED_ERA_BASED);
        advAeroRules = gameOptions.booleanOption(OptionsConstants.ADVAERORULES_AERO_ARTILLERY_MUNITIONS);
        showExtinct = gameOptions.booleanOption((OptionsConstants.ALLOWED_SHOW_EXTINCT));
    }

    // See if selected ammoType is legal under current game rules, availability, TL,
    // tech base, etc.
    public boolean checkLegality(AmmoType aType, String faction, String techBase, boolean mixedTech) {
        boolean legal = false;
        boolean clan = techBase.equals("CL");

        if (eraBasedTechLevel) {
            // Check if tech is legal to use in this game based on year, tech level, etc.
            legal = aType.isLegal(allowedYear, legalLevel, clan,
                    mixedTech, showExtinct);
            // Check if tech is widely available, or if the specific faction has access to
            // it
            legal &= aType.isAvailableIn(allowedYear, showExtinct)
                    || aType.isAvailableIn(allowedYear, clan, ITechnology.getCodeFromIOAbbr(faction));
        } else {
            // Basic year check only
            legal = aType.getStaticTechLevel().ordinal() <= legalLevel.ordinal();
        }

        // Nukes are not allowed... unless they are!
        legal &= (!aType.hasFlag(AmmoType.F_NUCLEAR)
                || cg.getClient().getGame().getOptions().booleanOption(OptionsConstants.ADVAERORULES_AT2_NUKES));

        return legal;
    }

    /**
     * Use values from the Properties file defined in TeamLoadoutGenerator class if available; else use provided default
     * @param field Field name in property file
     * @param defValue Default value to use
     * @return Double read value or default
     */
    public static Double castPropertyDouble(String field, Double defValue) {
        try {
            return Double.parseDouble(TeamLoadoutGenerator.weightProperties.getProperty(field));
        }
        catch (Exception ignored) {
            return defValue;
        }
    }

    public static int castPropertyInt(String field, int defValue) {
        try {
            return Integer.parseInt(TeamLoadoutGenerator.weightProperties.getProperty(field));
        }
        catch (Exception ignored) {
            return defValue;
        }
    }

    public void setTrueRandom(boolean value) {
        trueRandom = value;
    }

    public boolean getTrueRandom() {
        return trueRandom;
    }

    // region Check for various unit types, armor types, etc.
    private static long checkForBombers(ArrayList<Entity> el) {
        return el.stream().filter(Targetable::isBomber).count();
    }

    private static long checkForFliers(ArrayList<Entity> el) {
        return el.stream().filter(e -> e.isAero() || e.hasETypeFlag(Entity.ETYPE_VTOL)).count();
    }

    private static long checkForInfantry(ArrayList<Entity> el) {
        return el.stream().filter(e -> e.isInfantry() && !e.isBattleArmor()).count();
    }

    private static long checkForBattleArmor(ArrayList<Entity> el) {
        return el.stream().filter(Entity::isBattleArmor).count();
    }

    private static long checkForVehicles(ArrayList<Entity> el) {
        return el.stream().filter(BTObject::isVehicle).count();
    }

    private static long checkForMeks(ArrayList<Entity> el) {
        return el.stream().filter(BTObject::isMek).count();
    }

    /**
     * Quick and dirty energy boat calc; useful for selecting Laser-Inhibiting Arrow
     * and heat-based weapons
     *
     * @param el
     * @return
     */
    private static long checkForEnergyBoats(ArrayList<Entity> el) {
        return el.stream().filter(e -> e.getAmmo().isEmpty()).count();
    }

    /**
     * "Missile Boat" defined here as any unit with half or more weapons dedicated
     * to missiles
     * (This could probably be traded for a weight- or role-based check)
     *
     * @param el
     * @return
     */
    private static long checkForMissileBoats(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.getRole().isAnyOf(UnitRole.MISSILE_BOAT) || e.getWeaponList().stream().filter(
                        w -> w.getName().toLowerCase().contains("lrm") ||
                        w.getName().toLowerCase().contains("srm") ||
                        w.getName().toLowerCase().contains("atm") ||
                        w.getName().toLowerCase().contains("mml") ||
                        w.getName().toLowerCase().contains("arrow") ||
                        w.getName().toLowerCase().contains("thunder")
                ).count() >= e.getWeaponList().size()
        ).count();
    }

    private static long checkForTAG(ArrayList<Entity> el) {
        return el.stream().filter(e -> e.hasTAG()).count();
    }

    private static long checkForNARC(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.getAmmo().stream().anyMatch(
                        a -> ((AmmoType) a.getType()).getAmmoType() == AmmoType.T_NARC))
                .count();
    }

    private static long checkForAdvancedArmor(ArrayList<Entity> el) {
        // Most units have a location 0
        return el.stream().filter(
                e -> e.getArmorType(0) == ArmorType.T_ARMOR_HARDENED ||
                        e.getArmorType(0) == ArmorType.T_ARMOR_BALLISTIC_REINFORCED ||
                        e.getArmorType(0) == ArmorType.T_ARMOR_REACTIVE ||
                        e.getArmorType(0) == ArmorType.T_ARMOR_BA_REACTIVE ||
                        e.getArmorType(0) == ArmorType.T_ARMOR_FERRO_LAMELLOR)
                .count();
    }

    private static long checkForReflectiveArmor(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.getArmorType(0) == ArmorType.T_ARMOR_REFLECTIVE ||
                        e.getArmorType(0) == ArmorType.T_ARMOR_BA_REFLECTIVE)
                .count();
    }

    private static long checkForFireproofArmor(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.getArmorType(0) == ArmorType.T_ARMOR_BA_FIRE_RESIST).count();
    }

    private static long checkForFastMovers(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.getOriginalWalkMP() > 5).count();
    }

    private static long checkForOffboard(ArrayList<Entity> el) {
        return el.stream().filter(
                e -> e.shouldOffBoardDeploy(e.getDeployRound())).count();
    }

    private static long checkForECM(ArrayList<Entity> el) {
        return el.stream().filter(
                Entity::hasECM).count();
    }
    // endregion Check for various unit types, armor types, etc.

    // region generateParameters
    public ReconfigurationParameters generateParameters(Team t) {
        ArrayList<Entity> ownTeamEntities = (ArrayList<Entity>) IteratorUtils.toList(game.getTeamEntities(t));
        return generateParameters(game, gameOptions, ownTeamEntities, t.getFaction(), t);
    }

    public ReconfigurationParameters generateParameters(ArrayList<Entity> ownEntities, String ownFaction, Team t) {
        return generateParameters(game, gameOptions, ownEntities, ownFaction, t);
    }

    /**
     * Create the parameters that will determine how to configure ammo loadouts for
     * this team
     *
     * @param g
     * @param gOpts
     * @param ownEntities
     * @param friendlyFaction
     * @param team
     * @return ReconfigurationParameters with information about enemy and friendly
     *         forces
     */
    public static ReconfigurationParameters generateParameters(
            Game g,
            GameOptions gOpts,
            ArrayList<Entity> ownEntities,
            String friendlyFaction,
            Team team) {
        if (ownEntities.isEmpty()) {
            // Nothing to generate
            return new ReconfigurationParameters();
        }
        ArrayList<Entity> etEntities = new ArrayList<Entity>();
        ArrayList<String> enemyFactions = new ArrayList<>();
        boolean blind = gOpts.booleanOption(OptionsConstants.BASE_BLIND_DROP)
                        || gOpts.booleanOption(OptionsConstants.BASE_REAL_BLIND_DROP);
        boolean darkEnvironment = g.getPlanetaryConditions().getLight().isDuskOrFullMoonOrMoonlessOrPitchBack();
        boolean groundMap = g.getBoard().onGround();
        boolean spaceEnvironment = g.getBoard().inSpace();

        // This team can see the opponent teams; set appropriate options
        if (!blind) {
            for (Team et : g.getTeams()) {
                if (!et.isEnemyOf(team)) {
                    continue;
                }
                enemyFactions.add(et.getFaction());
                etEntities.addAll((ArrayList<Entity>) IteratorUtils.toList(g.getTeamEntities(et)));
            }
        }
        return generateParameters(
                ownEntities,
                etEntities,
                friendlyFaction,
                enemyFactions,
                blind,
                darkEnvironment,
                groundMap,
                spaceEnvironment
        );
    }

    public static ReconfigurationParameters generateParameters(
            ArrayList<Entity> ownTeamEntities,
            ArrayList<Entity> etEntities,
            String friendlyFaction,
            ArrayList<String> enemyFactions,
            boolean blind,
            boolean darkEnvironment,
            boolean groundMap,
            boolean spaceEnvironment
    ) {
        ReconfigurationParameters rp = new ReconfigurationParameters();

        // Set own faction
        rp.friendlyFaction = friendlyFaction;

        // Get our own side's numbers for comparison
        rp.friendlyCount = ownTeamEntities.size();

        // Estimate enemy count for ammo count purposes; may include observers. The fog
        // of war!
        rp.enemyCount = etEntities.size();

        // Record if ground map
        rp.groundMap = groundMap;
        // Record if space-based environment
        rp.spaceEnvironment = spaceEnvironment;

        // If our team can see other teams...
        if (!blind) {
                rp.enemiesVisible = true;
                rp.enemyFactions.addAll(enemyFactions);
                rp.enemyFliers += checkForFliers(etEntities);
                rp.enemyBombers += checkForBombers(etEntities);
                rp.enemyInfantry += checkForInfantry(etEntities);
                rp.enemyBattleArmor += checkForBattleArmor(etEntities);
                rp.enemyVehicles += checkForVehicles(etEntities);
                rp.enemyMeks += checkForMeks(etEntities);
                rp.enemyEnergyBoats += checkForEnergyBoats(etEntities);
                // Enemy Missile Boats might be good to know for Retro Streak weighting
                rp.enemyMissileBoats += checkForMissileBoats(etEntities);
                rp.enemyAdvancedArmorCount += checkForAdvancedArmor(etEntities);
                rp.enemyReflectiveArmorCount += checkForReflectiveArmor(etEntities);
                rp.enemyFireproofArmorCount += checkForFireproofArmor(etEntities);
                rp.enemyFastMovers += checkForFastMovers(etEntities);
                rp.enemyOffBoard = checkForOffboard(etEntities);
                rp.enemyECMCount = checkForECM(etEntities);
        } else {
            // Assume we know _nothing_ about enemies if Double Blind is on.
            rp.enemiesVisible = false;
        }

        // Friendly force info
        rp.friendlyEnergyBoats = checkForEnergyBoats(ownTeamEntities);
        rp.friendlyMissileBoats = checkForMissileBoats(ownTeamEntities);
        rp.friendlyTAGs = checkForTAG(ownTeamEntities);
        rp.friendlyNARCs = checkForNARC(ownTeamEntities);
        rp.friendlyOffBoard = checkForOffboard(ownTeamEntities);
        rp.friendlyECMCount = checkForECM(ownTeamEntities);
        rp.friendlyInfantry = checkForInfantry(ownTeamEntities);
        rp.friendlyBattleArmor = checkForBattleArmor(ownTeamEntities);

        // General parameters
        rp.darkEnvironment = darkEnvironment;

        return rp;
    }
    // endregion generateParameters

    // region Imperative mutators
    private static void setACImperatives(Entity e, MunitionTree mt, ReconfigurationParameters rp) {
        setAC20Imperatives(e, mt, rp);
    }

    // Set low-ammo-count AC20 carriers to use Caseless exclusively.
    private static boolean setAC20Imperatives(Entity e, MunitionTree mt, ReconfigurationParameters rp) {
        int ac20Count = 0;
        int ac20Ammo = 0;
        ac20Count = (int) e.getWeaponList().stream()
                .filter(w -> w.getName().toLowerCase().contains("ac") && w.getName().contains("20")).count();

        // TODO: remove this block when implementing new anti-ground Aero errata
        // Ignore Aeros, which can't use most alt munitions, and those without AC20s.
        if (e.isAero() || ac20Count == 0) {
            return false;
        }

        // Always use Caseless if AC/20 ammo tons <= count of tubes
        ac20Ammo = (int) e.getAmmo().stream()
                .filter(w -> w.getName().toLowerCase().contains("ac") && w.getName().contains("20")).count();
        if (ac20Ammo <= ac20Count) {
            mt.insertImperative(e.getFullChassis(), e.getModel(), "any", "AC/20", "Caseless");
            return true;
        }

        // Add one "Standard" to the start of the existing imperatives operating on this
        // unit.
        String[] imperatives = mt.getEffectiveImperative(e.getFullChassis(), e.getModel(), "any", "AC/20").split(":");
        if (!imperatives[0].contains("Standard")) {
            mt.insertImperative(e.getFullChassis(), e.getModel(), "any", "AC/20",
                    "Standard:" + String.join(":", imperatives));
        }

        return false;
    }

    // Set Artemis LRM carriers to use Artemis LRMs
    private static boolean setLRMImperatives(Entity e, MunitionTree mt, ReconfigurationParameters rp) {
        boolean artemis = !(e.getMiscEquipment(MiscType.F_ARTEMIS).isEmpty()
                && e.getMiscEquipment(MiscType.F_ARTEMIS_V).isEmpty());

        if (artemis) {
            for (Mounted wpn : e.getWeaponList()) {
                if (wpn.getName().toLowerCase().contains("lrm")) {
                    mt.insertImperative(e.getFullChassis(), e.getModel(), "any", wpn.getType().getShortName(),
                            "Artemis-capable");
                }
            }
            return true;
        }
        return false;
    }
    // region Imperative mutators

    // region generateMunitionTree
    public MunitionTree generateMunitionTree(ReconfigurationParameters rp, Team t) {
        ArrayList<Entity> ownTeamEntities = (ArrayList<Entity>) IteratorUtils.toList(game.getTeamEntities(t));
        return generateMunitionTree(rp, ownTeamEntities, "");
    }

    public static MunitionTree generateMunitionTree(ReconfigurationParameters rp, ArrayList<Entity> entities,
            String defaultSettingsFile) {
        // Based on various requirements from rp, set weights for some ammo types over
        // others
        MunitionWeightCollection mwc = new MunitionWeightCollection();
        return generateMunitionTree(rp, entities, defaultSettingsFile, mwc);
    }

    /**
     * Generate the list of desired ammo load-outs for this team.
     * TODO: implement generateDetailedMunitionTree with more complex breakdowns per
     * unit type
     * NOTE: if subclassing this generator, should only need to override this
     * method.
     *
     * @param rp
     * @param t
     * @param defaultSettingsFile
     * @return generated MunitionTree with imperatives for each weapon type
     */
    public static MunitionTree generateMunitionTree(ReconfigurationParameters rp, ArrayList<Entity> ownTeamEntities,
            String defaultSettingsFile, MunitionWeightCollection mwc) {

        // Either create a new tree or, if a defaults file is provided, load that as a
        // base config
        MunitionTree mt = (defaultSettingsFile == null | defaultSettingsFile.isBlank()) ? new MunitionTree()
                : new MunitionTree(defaultSettingsFile);

        // Modify weights for parameters
        if (rp.darkEnvironment) {
            // Bump munitions that light stuff up
            mwc.increaseIllumMunitions();
        } else {
            // decrease weights
            mwc.decreaseIllumMunitions();
        }

        // Adjust weights for enemy force composition
        if (rp.enemiesVisible) {
            // Drop weight of shot-reducing ammo unless this team significantly outnumbers the enemy
            if (!(rp.friendlyCount >= rp.enemyCount * castPropertyDouble("mtReducingAmmoReduceIfUnderFactor", 2.0))) {
                // Skip munitions that reduce the number of rounds because we need to shoot a lot!
                mwc.decreaseAmmoReducingMunitions();
            } else if (rp.friendlyCount >= rp.enemyCount * castPropertyDouble("mtReducingAmmoIncreaseIfOverFactor", 3.0)) {
                mwc.increaseAmmoReducingMunitions();
            }

            // Flak: bump for any bombers, or fliers > 1/4th of enemy force
            if (rp.enemyBombers > castPropertyDouble("mtFlakMinBombersExceedThreshold", 0.0)) {
                mwc.increaseFlakMunitions();
            }
            if (rp.enemyFliers >= rp.enemyCount / castPropertyDouble("mtFlakEnemyFliersFractionDivisor", 4.0)) {
                mwc.increaseFlakMunitions();
            }

            // Enemy fast movers make more precise ammo attractive
            if (rp.enemyFastMovers >= rp.enemyCount / castPropertyDouble("mtPrecisionAmmoFastEnemyFractionDivisor", 4.0)) {
                mwc.increaseAccurateMunitions();
            }

            // AP munitions are hard-countered by hardened, reactive, etc. armor
            if (rp.enemyAdvancedArmorCount > castPropertyDouble("mtHPAmmoAdvArmorEnemiesExceedThreshold", 0.0)
                    && rp.enemyAdvancedArmorCount > rp.enemyReflectiveArmorCount) {
                mwc.decreaseAPMunitions();
                mwc.increaseHighPowerMunitions();
            } else if (rp.enemyReflectiveArmorCount > rp.enemyAdvancedArmorCount) {
                // But AP munitions really hurt Reflective!
                mwc.increaseAPMunitions();
            }

            // Heat-based weapons kill infantry dead, also vehicles
            // But anti-infantry weapons are generally inferior without infantry targets
            if (rp.enemyFireproofArmorCount < rp.enemyCount / castPropertyDouble("mtFireproofMaxEnemyFractionDivisor", 4.0)) {
                if (rp.enemyInfantry >= rp.enemyCount / castPropertyDouble("mtInfantryEnemyExceedsFractionDivisor", 4.0)) {
                    mwc.increaseHeatMunitions();
                    mwc.increaseAntiInfMunitions();
                } else {
                    mwc.decreaseAntiInfMunitions();
                }
                if (rp.enemyVehicles >= rp.enemyCount / castPropertyDouble("mtVeeEnemyExceedsFractionDivisor", 4.0)) {
                    mwc.increaseHeatMunitions();
                }
                // BAs are proof against some dedicated Anti-Infantry weapons but not heat-generating rounds
                if (rp.enemyBattleArmor > rp.enemyCount / castPropertyDouble("mtBAEnemyExceedsFractionDivisor", 4.0)) {
                    mwc.increaseHeatMunitions();
                    mwc.increaseAntiBAMunitions();
                }
            } else if (rp.enemyFireproofArmorCount >= rp.enemyCount / castPropertyDouble("mtFireproofMaxEnemyFractionDivisor", 4.0)) {
                if (rp.enemyInfantry >= rp.enemyCount / castPropertyDouble("mtInfantryEnemyExceedsFractionDivisor", 4.0)) {
                    mwc.increaseAntiInfMunitions();
                }
                if (rp.enemyBattleArmor > rp.enemyCount / castPropertyDouble("mtBAEnemyExceedsFractionDivisor", 4.0)) {
                    mwc.increaseAntiBAMunitions();
                }
                mwc.decreaseHeatMunitions();
            }

            // Counter EMC by swapping Seeking in for Guided
            if (rp.enemyECMCount > castPropertyDouble("mtSeekingAmmoEnemyECMExceedThreshold", 1.0)) {
                mwc.decreaseGuidedMunitions();
                mwc.increaseSeekingMunitions();
            } else {
                // Seeking munitions are generally situational
                mwc.decreaseSeekingMunitions();
            }
        }

        // Section: Friendly capabilities

        // Guided munitions are worth more with guidance
        if (rp.friendlyTAGs >= castPropertyDouble("mtGuidedAmmoFriendlyTAGThreshold", 1.0)
                || rp.friendlyNARCs >= castPropertyDouble("mtGuidedAmmoFriendlyNARCThreshold", 1.0)) {
            mwc.increaseGuidedMunitions();

            // And worth even more with more guidance around
            if (rp.friendlyMissileBoats >= rp.friendlyCount /
                    castPropertyDouble("mtGuidedAmmoFriendlyMissileBoatFractionDivisor", 3.0)) {
                mwc.increaseGuidedMunitions();
            }
        } else {
            // Expensive waste without guidance
            mwc.decreaseGuidedMunitions();
        }

        // Downgrade utility munitions unless there are multiple units that could use them; off-board arty
        // in particular
        if (rp.friendlyOffBoard > castPropertyDouble("mtUtilityAmmoOffboardUnitsThreshold", 2.0) ) {
            // Only increase utility rounds if we have more off-board units that the other guys
            if (rp.enemyOffBoard < rp.friendlyOffBoard /
                    castPropertyDouble("mtUtilityAmmoFriendlyVsEnemyFractionDivisor", 1.0)) {
                mwc.increaseUtilityMunitions();
            }
        } else {
            // Reduce utility munition chances if we've only got a lance or so of arty
            mwc.decreaseUtilityMunitions();
        }

        // Just for LOLs: when FS fights CC in 3028 ~ 3050, set Anti-TSM weight to 15.0
        if (rp.friendlyFaction.equals("FS") && rp.enemyFactions.contains("CC")
                && (3028 <= rp.allowedYear && rp.allowedYear <= 3050)) {
            ArrayList<String> tsmOnly = new ArrayList<String>(List.of("Anti-TSM"));
            mwc.increaseMunitions(tsmOnly);
            mwc.increaseMunitions(tsmOnly);
            mwc.increaseMunitions(tsmOnly);
        }

        // Set nukes to lowest possible weight if user has set the to unusuable /for
        // this team/
        // This is a seperate mechanism from the legality check.
        if (rp.nukesBannedForMe) {
            mwc.zeroMunitionsWeight(new ArrayList<>(List.of("Davy Crockett-M", "AlamoMissile Ammo")));
        }

        // The main event!
        // Convert MWC to MunitionsTree for loading
        applyWeightsToMunitionTree(mwc, mt);

        // Handle individual cases like Artemis LRMs, AC/20s with limited ammo, etc.
        for (Entity e : ownTeamEntities) {
            // Set certain imperatives based on weapon types, due to low ammo count / low
            // utility
            setACImperatives(e, mt, rp);
            setLRMImperatives(e, mt, rp);
        }

        return mt;
    }

    /**
     * Turn a selection of the computed munition weights into imperatives to load in
     * the MunitionTree
     *
     * @param mt
     * @param mwc
     * @return
     */
    public static MunitionTree applyWeightsToMunitionTree(MunitionWeightCollection mwc, MunitionTree mt) {
        // Iterate over every entry in the set of top-weighted munitions for each category
        HashMap<String, List<String>> topWeights = mwc.getTopN(
                castPropertyInt("mtTopMunitionsSubsetCount", 4)
        );

        for (Map.Entry<String, List<String>> e : topWeights.entrySet()) {
            StringBuilder sb = new StringBuilder();
            int size = e.getValue().size();
            for (int i = 0; i < size; i++) {
                String[] fields = e.getValue().get(i).split("=");
                // Add the current munition
                sb.append(fields[0]);
                if (i < size - 1) {
                    sb.append(":");
                }
            }

            mt.insertImperative("any", "any", "any", e.getKey(), sb.toString());
        }
        return mt;
    }
    // endregion generateMunitionTree

    // region reconfigureEntities
    /**
     * Wrapper to streamline bot team configuration using standardized defaults
     *
     * @param team
     */
    public void reconfigureBotTeamWithDefaults(Team team, String faction) {
        // Load in some hard-coded defaults now before calculating more.
        reconfigureTeam(team, faction, defaultBotMunitionsFile);
    }

    /**
     * Wrapper to load a file of preset munition imperatives
     *
     * @param team
     * @param faction
     * @param adfFile
     */
    public void reconfigureTeam(Team team, String faction, String adfFile) {
        ReconfigurationParameters rp = generateParameters(team);
        rp.allowedYear = allowedYear;

        ArrayList<Entity> updateEntities = (ArrayList<Entity>) IteratorUtils.toList(
                game.getTeamEntities(team));

        MunitionTree mt = generateMunitionTree(rp, updateEntities, adfFile);
        reconfigureEntities(updateEntities, faction, mt, rp);
    }

    /**
     * More generic reconfiguration function that acts on sets of units, not teams
     *
     * @param entities ArrayList of entities, including ground and air units
     * @param faction  String code for entities' main faction
     * @param mt       MunitionTree defining all applicable loadout imperatives
     */
    public void reconfigureEntities(ArrayList<Entity> entities, String faction, MunitionTree mt, ReconfigurationParameters rp) {
        ArrayList<Entity> aeros = new ArrayList<>();
        for (Entity e : entities) {
            if (e.isAero()) {
                // TODO: Will be used when A2G attack errata are implemented
                aeros.add(e);
            } else {
                reconfigureEntity(e, mt, faction);
            }
        }

        populateAeroBombs(
            entities,
            this.allowedYear,
                rp.groundMap || rp.enemyCount > rp.enemyFliers,
            ForceDescriptor.RATING_5,
            faction.equals("PIR")
        );
    }

    /**
     * Configure Bot Team with all munitions randomized
     *
     * @param team
     * @param faction
     */
    public void randomizeBotTeamConfiguration(Team team, String faction) {
        ReconfigurationParameters rp = generateParameters(team);
        ArrayList<Entity> updateEntities = (ArrayList<Entity>) IteratorUtils.toList(
                game.getTeamEntities(team)
        );
        reconfigureEntities(updateEntities, faction, generateRandomizedMT(), rp);
    }

    public static MunitionTree generateRandomizedMT() {
        MunitionTree mt = new MunitionTree();
        for (String typeName : TYPE_LIST) {
            mt.insertImperative("any", "any", "any", typeName, "Random");
        }
        return mt;
    }
    // endregion reconfigureEntities

    // region reconfigureEntity
    /**
     * Method to apply a MunitionTree to a specific unit.
     * Main application logic
     *
     * @param e
     * @param mt
     * @param faction
     */
    public void reconfigureEntity(Entity e, MunitionTree mt, String faction) {
        String chassis = e.getFullChassis();
        String model = e.getModel();
        String pilot = e.getCrew().getName(0);

        // Create map of bin counts in unit by type
        HashMap<String, List<AmmoMounted>> binLists = new HashMap<>();

        // Populate map with _valid_, _available_ ammo
        for (AmmoMounted ammoBin : e.getAmmo()) {
            AmmoType aType = ammoBin.getType();
            String sName = ("".equals(aType.getBaseName())) ? ammoBin.getType().getShortName() : aType.getBaseName();

            // Store the actual bins under their types
            if (!binLists.containsKey(sName)) {
                binLists.put(sName, new ArrayList<>());
            }
            binLists.get(sName).add(ammoBin);
        }

        // Iterate over each type and fill it with the requested ammos (as much as
        // possible)
        for (String binName : binLists.keySet()) {
            iterativelyLoadAmmo(e, mt, binLists.get(binName), binName, faction);
        }
    }
    // endregion reconfigureEntity

    // region reconfigureAero

    /**
     * This method should mirror reconfigureEntity but with more restrictions based
     * on the types of alternate
     * munitions allowed by Aerospace rules.
     *
     * @param e
     * @param mt
     * @param faction
     */
    public void reconfigureAero(Entity e, MunitionTree mt, String faction) {

    }
    // endregion reconfigureAero

    // region iterativelyLoadAmmo
    private void iterativelyLoadAmmo(
            Entity e, MunitionTree mt, List<AmmoMounted> binList, String binName, String faction) {
        String techBase = (e.isClan()) ? "CL" : "IS";
        iterativelyLoadAmmo(e, mt, binList, binName, techBase, faction);
    }

    /**
     * Manage loading ammo bins for a given type.
     * Type can be designated by size (LRM-5) or generic (AC)
     * Logic:
     * Iterate over list of priorities and fill the first as many times as
     * requested.
     * Repeat for 2nd..Nth ammo types
     * If more bins remain than desired types are specified, fill the remainder with
     * the top priority type
     * If more desired types remain than there are bins, oh well.
     * If a requested ammo type is not available in the specified timeframe or
     * faction, skip it.
     *
     * @param e        Entity to load
     * @param mt       MunitionTree, stores required munitions in desired loading
     *                 order
     * @param binList  List of actual mounted ammo bins matching this type
     * @param binName  String bin type we are loading now
     * @param techBase "CL" or "IS"
     * @param faction  Faction to outfit for, used in ammo validity checks (uses MM,
     *                 not IO, faction codes)
     */
    private void iterativelyLoadAmmo(
            Entity e, MunitionTree mt, List<AmmoMounted> binList, String binName, String techBase, String faction) {
        Logger logger = LogManager.getLogger();
        // Copy counts that we will update, otherwise mt entry gets edited permanently.
        HashMap<String, Integer> counts = new HashMap<String, Integer>(
                mt.getCountsOfAmmosForKey(e.getFullChassis(), e.getModel(), e.getCrew().getName(0), binName));
        List<String> priorities = mt.getPriorityList(e.getFullChassis(), e.getModel(), e.getCrew().getName(0), binName);
        // Track default type for filling in unfilled bins
        AmmoType defaultType = null;
        int defaultIdx = 0;

        // If the imperative is to use Random for every bin, we need a different Random
        // for each bin
        if (priorities.size() == 1 && priorities.get(0).contains("Random")) {
            priorities = new ArrayList<>(Collections.nCopies(binList.size(), "Random"));
        }

        for (int i = 0; i < priorities.size() && !binList.isEmpty(); i++) {
            // binName is the weapon to which the bin connects: LRM-15, AC20, SRM, etc.
            // binType is the munition type loaded in currently
            // If all required bins are filled, revert to defaultType
            // If "Random", choose a random ammo type. Availability will be checked later.
            // If not trueRandom, only select from munitions that deal damage

            boolean random = priorities.get(i).contains("Random");
            String binType = (random) ? getRandomBin(binName, trueRandom) : priorities.get(i);
            Mounted bin = binList.get(0);
            AmmoType desired = null;

            // Load matching AmmoType
            if (binType.toLowerCase().contains("standard")) {
                desired = (AmmoType) EquipmentType.get(techBase + " " + binName + " " + "Ammo");
                if (desired == null) {
                    // Some ammo, like AC/XX ammo, is named funny
                    desired = (AmmoType) EquipmentType.get(techBase + " Ammo " + binName);
                }
            } else {
                // Get available munitions
                Vector<AmmoType> vAllTypes = AmmoType.getMunitionsFor(((AmmoType) bin.getType()).getAmmoType());
                if (vAllTypes == null) {
                    continue;
                }

                // Make sure the desired munition type is available and valid
                desired = vAllTypes.stream()
                        .filter(m -> m.getInternalName().startsWith(techBase) && m.getBaseName().contains(binName)
                                && m.getName().contains(binType))
                        .filter(d -> checkLegality(d, faction, techBase, e.isMixedTech()))
                        .findFirst().orElse(null);
            }

            if (desired == null) {
                // Couldn't find a bin, move on to the next priority.
                // Update default idx if we're currently setting the default
                defaultIdx = (i == defaultIdx) ? defaultIdx + 1 : defaultIdx;
                continue;
            }

            // Add one of the current binType to counts so we get a new random type every
            // bin
            if (random) {
                counts.put(binType, 1);
            }

            // Store default AmmoType
            if (i == defaultIdx) {
                defaultType = desired;
            }

            // Continue filling with this munition type until count is fulfilled or there
            // are no more bins
            while (counts.getOrDefault(binType, 0) > 0
                    && !binList.isEmpty()) {
                try {
                    // fill one ammo bin with the requested ammo type

                    if (!((AmmoType)bin.getType()).equalsAmmoTypeOnly(desired)){
                        // can't use this ammo if not
                        logger.debug("Unable to load bin " + bin.getName() + " with " + desired.getName());
                        // Unset default bin if ammo was not loadable
                        if (i == defaultIdx) {
                            defaultType = null;
                            defaultIdx += 1;
                        }
                        break;
                    }
                    // Apply ammo change
                    binList.get(0).changeAmmoType(desired);

                    // Decrement count and remove bin from list
                    counts.put(binType, counts.get(binType) - 1);
                    binList.remove(0);

                } catch (Exception ex) {
                    logger.debug("Error loading ammo bin!", ex);
                    break;
                }
            }
        }

        if (!(defaultType == null || binList.isEmpty())) {
            for (AmmoMounted bin : binList) {
                bin.changeAmmoType(defaultType);
            }
        }
    }

    /**
     * Select a random munition type that is a valid damaging ammo (for "random") or
     * truly random valid ammo
     * (for true random) for the bin type. IE "flechette" is
     *
     * @param binName
     * @param trueRandom
     * @return
     */
    private static String getRandomBin(String binName, boolean trueRandom) {
        String result = "";
        for (String typeName : TYPE_LIST) {
            if ((trueRandom || !UTILITY_MUNITIONS.contains(typeName)) &&
                    (binName.toLowerCase().contains(typeName.toLowerCase())
                            || typeName.toLowerCase().contains(binName.toLowerCase()))) {
                ArrayList<String> tList = TYPE_MAP.get(typeName);
                result = tList.get(new Random().nextInt(tList.size()));
                break;
            }
        }
        return result;
    }
    // endregion iterativelyLoadAmmo

    // region aero / bombs
    /**
     * Helper function to load bombs onto a random portion of units that can carry them
     * @param entityList        The list of entities to process
     * @param campaign          Campaign object
     * @param hasGroundTargets  true to select air-to-ground ordnance, false for air-to-air only
     * @param quality           IUnitRating enum for force quality (A/A* through F)
     * @param isPirate          true to use specific pirate ordnance loadouts
     */
    public static void populateAeroBombs(List<Entity> entityList,
                                         int year,
                                         boolean hasGroundTargets,
                                         int quality,
                                         boolean isPirate) {

        // Get all valid bombers, and sort unarmed ones to the front
        // Ignore VTOLs for now, as they suffer extra penalties for mounting bomb munitions
        List<Entity> bomberList = new ArrayList<>();
        for (Entity curEntity : entityList) {
            if (curEntity.isBomber() && !curEntity.isVehicle()) {
                // Clear existing bomb choices!
                ((IBomber) curEntity).setIntBombChoices(new int[BombType.B_NUM]);
                ((IBomber) curEntity).setExtBombChoices(new int[BombType.B_NUM]);

                if (!curEntity.getIndividualWeaponList().isEmpty()) {
                    bomberList.add(curEntity);
                } else {
                    bomberList.add(0, curEntity);
                }
            }
        }

        if (bomberList.isEmpty()) {
            return;
        }

        int minThrust;
        int maxLoad;

        // Some bombers may not be loaded; calculate percentage of total to equip
        int maxBombers = Math.min(
            (int) Math.ceil(((castPropertyInt("percentBombersToEquipMin", 40)
                + Compute.randomInt(castPropertyInt("percentBombersToEquipRange", 60))
            ) / 100.0 ) * bomberList.size()),
            bomberList.size()
        );
        int numBombers = 0;

        int[] generatedBombs;
        Map<Integer, int[]> bombsByCarrier = new HashMap<>();

        boolean forceHasGuided = false;
        for (int i = 0; i < bomberList.size(); i++) {
            bombsByCarrier.put(i, new int[BombType.B_NUM]);

            // Only generate loadouts up to the maximum number, use empty loadout for the rest
            if (numBombers >= maxBombers) {
                continue;
            }

            Entity curBomber = bomberList.get(i);
            boolean isUnarmed = curBomber.getIndividualWeaponList().isEmpty();

            // Some fighters on ground attack may be flying air cover rather than strictly
            // air-to-ground
            boolean isCAP = !hasGroundTargets ||
                    (Compute.d6() <= castPropertyInt("fightersLoadForCAPRollTargetThreshold", 1));

            // Set minimum thrust values, with lower minimums for unarmed and ground attack,
            // and use remaining thrust to limit hardpoints
            if (isCAP) {
                minThrust = isUnarmed ? castPropertyInt("fighterCAPMinUnarmedSafeThrustValue", 2)
                        : ((int) Math.ceil(curBomber.getWalkMP() /
                            castPropertyDouble("fighterCAPMinArmedSafeThrustFractionDivisor", 2.0)));
            } else {
                minThrust = isUnarmed ? castPropertyInt("bomberMinUnarmedSafeThrustValue", 2)
                        : castPropertyInt("bomberMinArmedSafeThrustValue", 3);
            }
            maxLoad = Math.min((int) Math.floor(
                    curBomber.getWeight() / castPropertyDouble("maxBomberLoadFactorDivisor", 5.0)),
                    (curBomber.getWalkMP() - minThrust) * castPropertyInt("maxBomberLoadThrustDiffFactor", 5)
            );

            // Get a random percentage (default 40 ~ 90) of the maximum bomb load for armed entities
            if (!isUnarmed) {
                maxLoad = (int) Math.ceil(
                        castPropertyInt("maxPercentBomberLoadToEquipMin", 50) +
                        (Compute.randomInt(castPropertyInt("maxPercentBomberLoadToEquipRange", 40))
                ) * maxLoad / 100.0);
            }

            if (maxLoad == 0) {
                continue;
            }

            // Generate bomb load
            generatedBombs = generateExternalOrdnance(
                    maxLoad,
                    isCAP,
                    isPirate,
                    quality,
                    year);
            // Whoops, go yell at the ordnance technician
            if (Arrays.stream(generatedBombs).sum() == 0) {
                continue;
            }

            // Set a flag to indicate at least one of the bombers is carrying guided ordnance
            forceHasGuided = forceHasGuided || hasGuidedOrdnance(generatedBombs);

            // Store the bomb selections as we might need to add in TAG later
            bombsByCarrier.put(i, generatedBombs);

            // Do not increment bomber count for unarmed entities
            if (!isUnarmed) {
                numBombers++;
            }

        }

        // Load ordnance onto units. If there is guided ordnance present then randomly add some TAG
        // pods to those without the guided ordnance.
        int tagCount = Math.min(bomberList.size(), Compute.randomInt(
                castPropertyInt("bombersToAddTagMaxCount", 3)
        ));
        for (int i = 0; i < bomberList.size(); i++) {
            Entity curBomber = bomberList.get(i);

            generatedBombs = bombsByCarrier.get(i);

            // Don't combine guided ordnance with external TAG
            if (forceHasGuided && tagCount > 0) {
                int maxLoadForTagger = Math.min((int) Math.floor(
                        curBomber.getWeight() / castPropertyDouble("maxBomberLoadFactorDivisor", 5.0)),
                        (curBomber.getWalkMP() - 2) * castPropertyInt("maxBomberLoadThrustDiffFactor", 5)
                );
                if (addExternalTAG(generatedBombs, true, maxLoadForTagger)) {
                    tagCount--;
                }
            }

            // Load the provided ordnance onto the unit
            if (generatedBombs != null && Arrays.stream(generatedBombs).sum() > 0) {
                ((IBomber) curBomber).setBombChoices(generatedBombs);
            }
        }
    }

    /**
     * Randomly generate a set of external ordnance up to the number of indicated bomb units. Lower
     * rated forces are more likely to get simpler types (HE and rockets).
     * Because TAG is only useful as one-per-fighter, it should be handled elsewhere.
     * @param bombUnits   how many bomb units to generate, some types count as more than one unit so
     *                    returned counts may be lower than this but never higher
     * @param airOnly     true to only select air-to-air ordnance
     * @param isPirate    true if force is pirate, specific low-tech/high chaos selections
     * @param quality     force rating to work with
     * @param year        current year, for tech filter
     * @return            array of integers, with each element being a bomb count using BombUnit
     *                    enums as the lookup e.g. [BombUnit.HE] will get the number of HE
     *                    bombs.
     */
    private static int[] generateExternalOrdnance (int bombUnits,
                                                   boolean airOnly,
                                                   boolean isPirate,
                                                   int quality,
                                                   int year) {

        int[] bombLoad = new int[BombType.B_NUM];

        if (bombUnits <= 0) {
            return bombLoad;
        }

        // Get a random predefined loadout
        double countWeight = 0.0;
        double completeWeight = 0.0;
        double randomThreshold = 0.0;

        // Use weighted random generation for air-to-ground loadouts. Use simple random selection
        // for air-to-air.
        Map<Integer,Integer> bombMap;
        if (!airOnly) {
            bombMap = lowTechBombLoad;

            // Randomly select a loadout using the weighted map of names. Pirates use a separate
            // map with different loadouts.
            Map<String, Integer> loadoutMap;
            List<String> mapNames = new ArrayList<>();
            List<Integer> mapWeights = new ArrayList<>();
            if (!isPirate) {
                loadoutMap = bombMapGroundSpread;
            } else {
                loadoutMap = bombMapPirateGroundSpread;
            }
            for (String curName : loadoutMap.keySet()) {
                mapNames.add(curName);
                mapWeights.add(loadoutMap.get(curName));
            }

            // Weighted random selection
            completeWeight = mapWeights.stream().mapToInt(curWeight -> curWeight).asDoubleStream().sum();
            randomThreshold = (Compute.randomInt(castPropertyInt("bomberRandomThresholdMaxPercent", 100))
                    / 100.0) * completeWeight;
            for (int i = 0; i < mapNames.size(); i++) {
                countWeight += Math.max(mapWeights.get(i), 1.0);
                if (countWeight >= randomThreshold) {

                    if (!isPirate) {
                        switch (mapNames.get(i)) {
                            case "Normal":
                                bombMap = normalBombLoad;
                                break;
                            case "Anti-Mek":
                                bombMap = antiMekBombLoad;
                                break;
                            case "Anti-conventional":
                                bombMap = antiConvBombLoad;
                                break;
                            case "Standoff":
                                bombMap = standoffBombLoad;
                                break;
                            case "Strike":
                                bombMap = strikeBombLoad;
                                break;
                            default:
                                break;
                        }
                    } else {
                        switch (mapNames.get(i)) {
                            case "Normal":
                                bombMap = pirateBombLoad;
                                break;
                            case "Firestorm":
                                bombMap = pirateFirestormBombLoad;
                                break;
                            default:
                                break;
                        }
                    }

                    break;
                }
            }

        } else {

            // Air-to-air loadouts are more limited, just use explicit random selection
            if (!isPirate) {
                if (Compute.randomInt(castPropertyInt("fighterCAPRandomPercentageRange", 100))
                        > castPropertyInt("fighterCAPAntiShipLoadoutRandomPercentageMax", 20)) {
                    bombMap = antiAirBombLoad;
                } else {
                    bombMap = antiShipBombLoad;
                }
            } else {
                bombMap = pirateAirBombLoad;
            }

        }

        // Slight hack to account for difficulties with isAvailableIn() with certain bombs
        boolean guidedAndArrowAvailable = ((year >= 2600) && (year <= 2835)) || (year > 3044);

        // Generate a working map with all the unavailable ordnance replaced with rockets or HE
        Map<Integer, Integer> workingBombMap = new HashMap<>();
        for (int curBombType : bombMap.keySet()) {
            String typeName = BombType.getBombInternalName(curBombType);
            if (curBombType == BombType.B_RL ||
                    curBombType == BombType.B_HE ||
                    (curBombType != BombType.B_LG &&
                            curBombType != BombType.B_ARROW &&
                            curBombType != BombType.B_HOMING &&
                            BombType.get(typeName).isAvailableIn(year, false)) ||
                    ((curBombType == BombType.B_LG ||
                            curBombType == BombType.B_ARROW ||
                            curBombType == BombType.B_HOMING) &&
                            guidedAndArrowAvailable)) {

                if (workingBombMap.containsKey(curBombType)) {
                    workingBombMap.put(curBombType, bombMap.get(curBombType) +
                            workingBombMap.get(curBombType));
                } else {
                    workingBombMap.put(curBombType, bombMap.get(curBombType));
                }

            } else {
                int replacementBomb = airOnly ? BombType.B_RL :
                        Compute.randomInt(castPropertyInt("bombReplacementIntRange", 2))
                                    <= castPropertyInt("bombReplacementRLThreshold", 0)
                            ? BombType.B_RL : BombType.B_HE;
                if (workingBombMap.containsKey(replacementBomb)) {
                    workingBombMap.put(replacementBomb, bombMap.get(curBombType) +
                            workingBombMap.get(replacementBomb));
                } else {
                    workingBombMap.put(replacementBomb, bombMap.get(curBombType));
                }

            }
        }

        // Generate enough bombs to meet the desired count

        int selectedBombType = -1;
        int loopSafety = 0;

        List<Integer> ordnanceIDs = new ArrayList<>();
        List<Integer> ordnanceRandomWeights = new ArrayList<>();
        for (int curID : workingBombMap.keySet()) {
            ordnanceIDs.add(curID);
            ordnanceRandomWeights.add(workingBombMap.get(curID));
        }
        completeWeight = ordnanceRandomWeights.stream().mapToInt(curWeight -> Math.max(curWeight, 1)).asDoubleStream().sum();

        for (int curLoad = 0; curLoad < bombUnits && loopSafety < castPropertyInt("maxBombApplicationLoopCount", 10);) {

            // Randomly get the ordnance type
            randomThreshold = (Compute.randomInt(
                    castPropertyInt("maxBombOrdnanceWeightPercentThreshold", 100)) / 100.0
            ) * completeWeight;
            countWeight = 0.0;
            for (int i = 0; i < ordnanceIDs.size(); i++) {
                countWeight += Math.max(ordnanceRandomWeights.get(i), 1.0);
                if (countWeight >= randomThreshold) {
                    selectedBombType = ordnanceIDs.get(i);
                    break;
                }
            }

            // If the selected ordnance doesn't exceed the provided limit increment the counter,
            // otherwise skip it and keep trying with some safeties to prevent infinite loops.
            if (selectedBombType >= 0 &&
                    curLoad + BombType.getBombCost(selectedBombType) <= bombUnits) {
                bombLoad[selectedBombType]++;
                curLoad += BombType.getBombCost(selectedBombType);
            } else {
                loopSafety++;
            }
        }

        // Oops, nothing left - rocket launchers are always popular
        if (Arrays.stream(bombLoad).sum() == 0) {
            bombLoad[BombType.B_RL] = bombUnits;
            return bombLoad;
        }

        // Randomly replace advanced ordnance with rockets or HE, depending on force rating and
        // air-air/ground preference

        List<Integer> advancedOrdnance = Arrays.asList(
                BombType.B_LG,
                BombType.B_ARROW,
                BombType.B_HOMING,
                BombType.B_LAA,
                BombType.B_AAA,
                BombType.B_THUNDER,
                BombType.B_FAE_SMALL,
                BombType.B_FAE_LARGE,
                BombType.B_AS,
                BombType.B_ASEW
        );

        switch (quality) {
            case ForceDescriptor.RATING_5:
            case ForceDescriptor.RATING_4:
                randomThreshold = castPropertyInt("bombRandomReplaceRating4PlusThreshold", 5);
                break;
            case ForceDescriptor.RATING_3:
                randomThreshold = castPropertyInt("bombRandomReplaceRating3PlusThreshold", 10);
                break;
            case ForceDescriptor.RATING_2:
                randomThreshold = castPropertyInt("bombRandomReplaceRating2PlusThreshold", 25);
                break;
            case ForceDescriptor.RATING_1:
                randomThreshold = castPropertyInt("bombRandomReplaceRating1PlusThreshold", 40);
                break;
            case ForceDescriptor.RATING_0:
                randomThreshold = castPropertyInt("bombRandomReplaceRating0PlusThreshold", 80);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized rating value: " + quality);
        }

        for (int curBomb : advancedOrdnance) {
            int loadCount = bombLoad[curBomb];

            for (int i = 0; i < loadCount; i++) {
                if (Compute.randomInt(100) < randomThreshold) {
                    if (airOnly) {
                        bombLoad[BombType.B_RL]++;
                    } else {
                        bombLoad[Compute.randomInt(
                                castPropertyInt("bombReplacementIntRange", 2)) <= castPropertyInt("bombReplacementRLThreshold", 0)
                                ? BombType.B_RL : BombType.B_HE]++;
                    }
                    bombLoad[curBomb]--;
                }
            }
        }
        return bombLoad;
    }


    /**
     * Checks to see if a bomb load contains ordnance that relies on TAG guidance, such as laser/TAG
     * guided bombs and homing Arrow IV
     *
     * @param bombLoad  array of size BombType.B_NUM, suitable for setting bombs on IBomber entities
     * @return          true if guided ordnance is carried
     */
    private static boolean hasGuidedOrdnance(int[] bombLoad) {
        if (bombLoad.length < Collections.max(GUIDED_ORDNANCE)) {
            throw new IllegalArgumentException("Invalid array LENGTH for bombLoad parameter.");
        }

        for (int curHomingBomb : GUIDED_ORDNANCE) {
            if (bombLoad[curHomingBomb] > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Updates a bomb load to include an external TAG system. If this exceeds the provided
     * maximum load (in bomb units i.e. Arrow IV counts as multiple units), then one of the basic
     * one-slot types is removed and the TAG system is added in its place.
     *
     * @param bombLoad    array of size BombType.B_NUM, suitable for setting bombs on IBomber
     *                    entities
     * @param skipGuided  true to only select external TAG for units without guided ordnance
     * @param maxLoad     Maximum external ordnance load in total bomb units (NOT bomb count)
     * @return            true, if TAG was added, false otherwise
     */
    private static boolean addExternalTAG (int[] bombLoad, boolean skipGuided, int maxLoad) {
        if (bombLoad.length < BombType.B_NUM) {
            throw new IllegalArgumentException("Invalid array length for bombLoad parameter.");
        }

        if (!skipGuided || !hasGuidedOrdnance(bombLoad)) {

            // If there's enough room, add it
            int totalLoad = IntStream.range(0, bombLoad.length).map(i -> BombType.getBombCost(i) * Math.max(bombLoad[i], 0)).sum();
            if (totalLoad < maxLoad) {
                bombLoad[BombType.B_TAG]++;
                return true;
            } else if (totalLoad == maxLoad) {

                List<Integer> replaceableTypes = Arrays.asList(
                        BombType.B_RL,
                        BombType.B_HE,
                        BombType.B_INFERNO,
                        BombType.B_CLUSTER);
                for (int i = 0; i < replaceableTypes.size(); i++) {
                    if (bombLoad[i] > 0) {
                        bombLoad[i]--;
                        bombLoad[BombType.B_TAG]++;
                        return true;
                    }
                }

            } else {
                // Already overloaded, don't bother
                return false;
            }

            bombLoad[BombType.B_TAG]++;
            return true;
        }

        return false;
    }

    //endregion aero / bombs
}

// region MunitionWeightCollection
class MunitionWeightCollection {
    private HashMap<String, Double> lrmWeights;
    private HashMap<String, Double> srmWeights;
    private HashMap<String, Double> acWeights;
    private HashMap<String, Double> atmWeights;
    private HashMap<String, Double> arrowWeights;
    private HashMap<String, Double> artyWeights;
    private HashMap<String, Double> artyCannonWeights;
    private HashMap<String, Double> mekMortarWeights;
    private HashMap<String, Double> narcWeights;
    private HashMap<String, Double> bombWeights;
    private Map<String, HashMap<String, Double>> mapTypeToWeights;

    MunitionWeightCollection() {
        resetWeights();
    }

    public void resetWeights() {
        // Initialize weights for all the weapon types using known munition names
        lrmWeights = initializeMissileWeaponWeights(MunitionTree.LRM_MUNITION_NAMES);
        srmWeights = initializeMissileWeaponWeights(MunitionTree.SRM_MUNITION_NAMES);
        acWeights = initializeWeaponWeights(MunitionTree.AC_MUNITION_NAMES);
        // ATMs are treated differently
        atmWeights = initializeATMWeights(MunitionTree.ATM_MUNITION_NAMES);
        arrowWeights = initializeWeaponWeights(MunitionTree.ARROW_MUNITION_NAMES);
        artyWeights = initializeWeaponWeights(MunitionTree.ARTILLERY_MUNITION_NAMES);
        artyCannonWeights = initializeWeaponWeights(MunitionTree.ARTILLERY_CANNON_MUNITION_NAMES);
        mekMortarWeights = initializeWeaponWeights(MunitionTree.MEK_MORTAR_MUNITION_NAMES);
        narcWeights = initializeWeaponWeights(MunitionTree.NARC_MUNITION_NAMES);
        bombWeights = initializeWeaponWeights(MunitionTree.BOMB_MUNITION_NAMES);

        mapTypeToWeights = new HashMap<>(Map.ofEntries(
                entry("LRM", lrmWeights),
                entry("SRM", srmWeights),
                entry("AC", acWeights),
                entry("ATM", atmWeights),
                entry("Arrow IV", arrowWeights),
                entry("Artillery", artyWeights),
                entry("Artillery Cannon", artyCannonWeights),
                entry("Mek Mortar", mekMortarWeights),
                entry("Narc", narcWeights),
                entry("Bomb", bombWeights)));
    }

    /**
     * Use values from the Properties file defined in TeamLoadoutGenerator class if available; else use provided default
     * @param field Field name in property file
     * @param defValue Default value to use
     * @return Double read value or default
     */
    private static Double getPropDouble(String field, Double defValue) {
        return TeamLoadoutGenerator.castPropertyDouble(field, defValue);
    }

    // Section: initializing weights
    private static HashMap<String, Double> initializeWeaponWeights(ArrayList<String> wepAL) {
        HashMap<String, Double> weights = new HashMap<String, Double>();
        for (String name: wepAL) {
            weights.put(name, getPropDouble("defaultWeaponWeight", 1.0));
        }
        // Every weight list should have a Standard set as weight 2.0
        weights.put("Standard", getPropDouble("defaultStandardMunitionWeight", 2.0));
        return weights;
    }

    private static HashMap<String, Double> initializeMissileWeaponWeights(ArrayList<String> wepAL) {
        HashMap<String, Double> weights = new HashMap<String, Double>();
        for (String name: wepAL) {
            weights.put(name, getPropDouble("defaultWeaponWeight", 1.0));
        }
        // Every missile weight list should have a Standard set as weight 2.0
        weights.put("Standard", getPropDouble("defaultMissileStandardMunitionWeight", 2.0));
        // Dead-Fire should be even higher to start
        weights.put("Dead-Fire", getPropDouble("defaultDeadFireMunitionWeight", 3.0));
        return weights;
    }

    private static HashMap<String, Double> initializeATMWeights(ArrayList<String> wepAL) {
        HashMap<String, Double> weights = new HashMap<String, Double>();
        for (String name: wepAL) {
            weights.put(name, getPropDouble("defaultATMMunitionWeight", 2.0));
        }
        // ATM Standard ammo is weighted lower due to overlap with HE and ER
        weights.put("Standard", getPropDouble("defaultATMStandardWeight", 1.0));
        return weights;
    }

    // Increase/Decrease functions.  Increase is 2x + 1, decrease is 0.5x, so items
    // voted up and down multiple times should still exceed items never voted up _or_ down.
    public void increaseMunitions(ArrayList<String> munitions) {
        mapTypeToWeights.entrySet().forEach(
                e -> modifyMatchingWeights(
                        e.getValue(),
                        munitions,
                        getPropDouble("increaseWeightFactor", 2.0),
                        getPropDouble("increaseWeightIncrement", 1.0)
                )
        );
    }

    public void decreaseMunitions(ArrayList<String> munitions) {
        mapTypeToWeights.entrySet().forEach(
                e -> modifyMatchingWeights(
                        e.getValue(),
                        munitions,
                        getPropDouble("decreaseWeightFactor", 0.5),
                        getPropDouble("decreaseWeightDecrement", 0.0)
                )
        );
    }

    public void zeroMunitionsWeight(ArrayList<String> munitions) {
        mapTypeToWeights.entrySet().forEach(
                e -> modifyMatchingWeights(
                        e.getValue(), munitions, 0.0, 0.0));
    }

    public void increaseAPMunitions() {
        increaseMunitions(TeamLoadoutGenerator.AP_MUNITIONS);
    }

    public void decreaseAPMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.AP_MUNITIONS);
    }

    public void increaseFlakMunitions() {
        increaseMunitions(TeamLoadoutGenerator.FLAK_MUNITIONS);
    }

    public void decreaseFlakMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.FLAK_MUNITIONS);
    }

    public void increaseAccurateMunitions() {
        increaseMunitions(TeamLoadoutGenerator.ACCURATE_MUNITIONS);
    }

    public void decreaseAccurateMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.ACCURATE_MUNITIONS);
    }

    public void increaseAntiInfMunitions() {
        increaseMunitions(TeamLoadoutGenerator.ANTI_INF_MUNITIONS);
    }

    public void decreaseAntiInfMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.ANTI_INF_MUNITIONS);
    }

    public void increaseAntiBAMunitions() {
        increaseMunitions(TeamLoadoutGenerator.ANTI_BA_MUNITIONS);
    }

    public void decreaseAntiBAMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.ANTI_BA_MUNITIONS);
    }

    public void increaseHeatMunitions() {
        increaseMunitions(TeamLoadoutGenerator.HEAT_MUNITIONS);
    }

    public void decreaseHeatMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.HEAT_MUNITIONS);
    }

    public void increaseIllumMunitions() {
        increaseMunitions(TeamLoadoutGenerator.ILLUM_MUNITIONS);
    }

    public void decreaseIllumMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.ILLUM_MUNITIONS);
    }

    public void increaseUtilityMunitions() {
        increaseMunitions(TeamLoadoutGenerator.UTILITY_MUNITIONS);
    }

    public void decreaseUtilityMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.UTILITY_MUNITIONS);
    }

    public void increaseGuidedMunitions() {
        increaseMunitions(TeamLoadoutGenerator.GUIDED_MUNITIONS);
    }

    public void decreaseGuidedMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.GUIDED_MUNITIONS);
    }

    public void increaseAmmoReducingMunitions() {
        increaseMunitions(TeamLoadoutGenerator.AMMO_REDUCING_MUNITIONS);
    }

    public void decreaseAmmoReducingMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.AMMO_REDUCING_MUNITIONS);
    }

    public void increaseSeekingMunitions() {
        increaseMunitions(TeamLoadoutGenerator.SEEKING_MUNITIONS);
    }

    public void decreaseSeekingMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.SEEKING_MUNITIONS);
    }

    public void increaseHighPowerMunitions() {
        increaseMunitions(TeamLoadoutGenerator.HIGH_POWER_MUNITIONS);
    }

    public void decreaseHighPowerMunitions() {
        decreaseMunitions(TeamLoadoutGenerator.HIGH_POWER_MUNITIONS);
    }

    /**
     * Update all matching types in a category by multiplying by a factor and adding
     * an increment
     * (1.0, 0.0) = no change; (2.0, 0.0) = double, (0.5, 0.0) = halve,
     * (1.0, 1.0) = increment by 1, (1.0, -1.0) = decrement by 1, etc.
     *
     * @param current
     * @param types
     * @param factor
     * @param increment
     */
    private static void modifyMatchingWeights(HashMap<String, Double> current, ArrayList<String> types, double factor,
            double increment) {
        for (String key : types) {
            if (current.containsKey(key)) {
                current.put(key, current.get(key) * factor + increment);
            }
        }
    }

    public ArrayList<String> getMunitionTypesInWeightOrder(Map<String, Double> weightMap) {
        ArrayList<String> orderedTypes = new ArrayList<>();
        weightMap.entrySet().stream()
                .sorted((E1, E2) -> E2.getValue().compareTo(E1.getValue()))
                .forEach(k -> orderedTypes.add(String.valueOf(k)));
        return orderedTypes;
    }

    public HashMap<String, List<String>> getTopN(int count) {
        HashMap<String, List<String>> topMunitionsMap = new HashMap<>();
        for (String key : TeamLoadoutGenerator.TYPE_MAP.keySet()) {
            List<String> orderedList = getMunitionTypesInWeightOrder(mapTypeToWeights.get(key));
            topMunitionsMap.put(key, (orderedList.size() >= count) ? orderedList.subList(0, count) : orderedList);
        }
        return topMunitionsMap;
    }

    public HashMap<String, Double> getLrmWeights() {
        return lrmWeights;
    }

    public HashMap<String, Double> getSrmWeights() {
        return srmWeights;
    }

    public HashMap<String, Double> getAcWeights() {
        return acWeights;
    }

    public HashMap<String, Double> getAtmWeights() {
        return atmWeights;
    }

    public HashMap<String, Double> getArrowWeights() {
        return arrowWeights;
    }

    public HashMap<String, Double> getArtyWeights() {
        return artyWeights;
    }

    public HashMap<String, Double> getBombWeights() {
        return bombWeights;
    }

    public HashMap<String, Double> getArtyCannonWeights() {
        return artyCannonWeights;
    }

    public HashMap<String, Double> getMekMortarWeights() {
        return mekMortarWeights;
    }
}
// endregion MunitionWeightCollection
