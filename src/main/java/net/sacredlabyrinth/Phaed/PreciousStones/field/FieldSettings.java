package net.sacredlabyrinth.Phaed.PreciousStones.field;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author phaed
 */
public class FieldSettings {
    protected String metaName = "";
    protected boolean metaAutoSet = false;
    protected List<String> metaLore = new ArrayList<>();
    protected int foresterUses = 1;
    protected BlockTypeEntry groundBlock;
    protected int treeCount = 64;
    protected int creatureCount = 6;
    protected int growTime = 20;
    protected int shrubDensity = 64;
    protected boolean validField = true;
    protected BlockTypeEntry type;
    protected int radius = 0;
    protected int fenceItem = 0;
    protected int fenceItemPrice = 0;
    protected int heal = 0;
    protected int damage = 0;
    protected int maskOnDisabled = 49;
    protected int maskOnEnabled = 49;
    protected int feed = 0;
    protected int repair = 0;
    protected int launchHeight = 0;
    protected int cannonHeight = 0;
    protected int customHeight = 0;
    protected int customVolume = 0;
    protected int mineDelaySeconds = 0;
    protected int lightningDelaySeconds = 0;
    protected int lightningReplaceBlock = 0;
    protected int mixingGroup = 0;
    protected int autoDisableTime = 0;
    protected int mustBeAbove = 0;
    protected int mustBeBelow = 0;
    protected boolean mineHasFire = false;
    protected int mine = 6;
    protected String groupOnEntry = "";
    protected String requiredPermissionAllow = "";
    protected String requiredPermissionUse = "";
    protected String requiredPermission = "";
    protected String deleteIfNoPermission = "";
    protected GameMode forceEntryGameMode = null;
    protected GameMode forceLeavingGameMode = null;
    protected String title;
    protected int price = 0;
    protected int refund = -1;
    protected int teleportCost = 0;
    protected int teleportBackAfterSeconds = 0;
    protected int teleportMaxDistance = 0;
    protected int griefRevertInterval = 0;
    protected int payToEnable = 0;
    protected int rentsLimit = 0;
    protected List<String> commandOnEnter = new ArrayList<>();
    protected List<String> commandOnExit = new ArrayList<>();
    protected List<String> playerCommandOnEnter = new ArrayList<>();
    protected List<String> playerCommandOnExit = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfHoldingItems = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfNotHoldingItems = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfHasItems = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfNotHasItems = new ArrayList<>();
    protected List<BlockTypeEntry> unusableItems = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfWalkingOn = new ArrayList<>();
    protected List<BlockTypeEntry> teleportIfNotWalkingOn = new ArrayList<>();
    protected List<Integer> treeTypes = new ArrayList<>();
    protected List<Integer> shrubTypes = new ArrayList<>();
    protected List<String> creatureTypes = new ArrayList<>();
    protected List<BlockTypeEntry> fertileBlocks = new ArrayList<>();
    protected List<Integer> limits = new ArrayList<>();
    protected List<BlockTypeEntry> surfaces = new ArrayList<>();
    protected List<BlockTypeEntry> translocationBlacklist = new ArrayList<>();
    protected List<BlockTypeEntry> preventPlaceBlacklist = new ArrayList<>();
    protected List<BlockTypeEntry> preventDestroyBlacklist = new ArrayList<>();
    protected List<BlockTypeEntry> preventUse = new ArrayList<>();
    protected List<BlockTypeEntry> confiscatedItems = new ArrayList<>();
    protected List<String> allowedWorlds = new ArrayList<>();
    protected List<String> allowedOnlyInside = new ArrayList<>();
    protected List<String> allowedOnlyOutside = new ArrayList<>();
    protected List<String> commandBlackList = new ArrayList<>();
    protected List<FieldFlag> defaultFlags = new ArrayList<>();
    protected List<FieldFlag> reversedFlags = new ArrayList<>();
    protected List<FieldFlag> alledflags = new ArrayList<>();
    protected List<FieldFlag> disabledFlags = new ArrayList<>();
    protected List<BlockTypeEntry> allowGrief = new ArrayList<>();
    protected HashMap<PotionEffectType, Integer> potions = new HashMap<>();
    protected List<PotionEffectType> neutralizePotions = new ArrayList<>();
    protected List<String> allowedPlayers = new ArrayList<>();
    protected List<String> deniedPlayers = new ArrayList<>();
    protected List<String> potionTargets = new ArrayList<>();
    protected LinkedHashMap<String, Object> map;
    private Set<FieldSettings> mergedFields = new HashSet<>();
    private double priceMultiplier = 0;

    /**
     * @param map
     */
    public FieldSettings(LinkedHashMap<String, Object> map) {
        this.map = map;

        if (map == null) {
            return;
        }

        defaultFlags.add(FieldFlag.ALL);

        if (!validation()) {
            return;
        }

        parseSettings();
    }

    protected boolean validation() {
        title = loadString("title");

        if (title == null) {
            validField = false;
            return false;
        }

        type = loadTypeEntry("block");

        if (type == null) {
            validField = false;
            return false;
        }

        return true;
    }

    protected void parseSettings() {
        PreciousStones.debug("**********************");

        //************************** custom height

        customHeight = loadInt("custom-height");

        if (customHeight > 0) {
            if (customHeight % 2 == 0) {
                customHeight++;
            }
        }

        //************************** game modes

        String entryGameMode = loadString("entry-game-mode");

        if (entryGameMode.equalsIgnoreCase("creative")) {
            forceEntryGameMode = GameMode.CREATIVE;
        }
        if (entryGameMode.equalsIgnoreCase("survival")) {
            forceEntryGameMode = GameMode.SURVIVAL;
        }

        String leavingGameMode = loadString("leaving-game-mode");

        if (leavingGameMode.equalsIgnoreCase("creative")) {
            forceLeavingGameMode = GameMode.CREATIVE;
        }
        if (leavingGameMode.equalsIgnoreCase("survival")) {
            forceLeavingGameMode = GameMode.SURVIVAL;
        }

        //************************** potions

        List<String> pts = loadStringList("potions");
        List<Integer> intensities = loadIntList("potion-intensity");

        int pos = 0;

        for (String name : pts) {
            int i = 1;

            if (intensities != null) {
                i = intensities.get(pos);
            }

            if (PotionEffectType.getByName(name) != null) {
                potions.put(PotionEffectType.getByName(name), i);
            }
            pos++;
        }

        List<String> npts = loadStringList("neutralize-potions");

        for (String name : npts) {
            if (PotionEffectType.getByName(name) != null) {
                neutralizePotions.add(PotionEffectType.getByName(name));
            }
        }

        //**************************

        loadBoolean("no-resize");
        loadBoolean("prevent-fire");
        loadBoolean("prevent-fire-spread");
        loadBoolean("enable-with-redstone");
        loadBoolean("allow-place");
        loadBoolean("allow-destroy");
        loadBoolean("prevent-place");
        loadBoolean("prevent-destroy");
        loadBoolean("prevent-vehicle-destroy");
        loadBoolean("prevent-vehicle-create");
        loadBoolean("prevent-enderman-destroy");
        loadBoolean("prevent-explosions");
        loadBoolean("prevent-creeper-explosions");
        loadBoolean("prevent-wither-explosions");
        loadBoolean("prevent-tnt-explosions");
        loadBoolean("rollback-explosions");
        loadBoolean("prevent-pvp");
        loadBoolean("prevent-teleport");
        loadBoolean("prevent-mob-damage");
        loadBoolean("prevent-mob-spawn");
        loadBoolean("prevent-animal-spawn");
        loadBoolean("prevent-entry");
        loadBoolean("prevent-unprotectable");
        loadBoolean("prevent-potion-splash");
        loadBoolean("prevent-portal-enter");
        loadBoolean("prevent-portal-creation");
        loadBoolean("prevent-portal-destination");
        loadBoolean("prevent-potion-splash");
        loadBoolean("prevent-vehicle-enter");
        loadBoolean("prevent-vehicle-exit");
        loadBoolean("prevent-item-frame-take");
        loadBoolean("protect-armor-stands");
        loadBoolean("prevent-entity-interact");
        loadBoolean("protect-animals");
        loadBoolean("protect-villagers");
        loadBoolean("protect-crops");
        loadBoolean("protect-mobs");
        loadBoolean("protect-lwc");
        loadBoolean("protect-inventories");
        loadBoolean("remove-mob");
        loadBoolean("worldguard-repellent");
        loadBoolean("breakable");
        loadBoolean("welcome-message");
        loadBoolean("farewell-message");
        loadBoolean("air");
        loadBoolean("snitch");
        loadBoolean("no-conflict");
        loadBoolean("no-owner");
        loadBoolean("launch");
        loadBoolean("cannon");
        loadBoolean("lightning");
        loadBoolean("no-fall-damage");
        loadBoolean("sneak-to-place");
        loadBoolean("sneak-to-place-only");
        loadBoolean("plot");
        loadBoolean("prevent-flow");
        loadBoolean("forester");
        loadBoolean("grief-revert");
        loadBoolean("grief-revert-drop");
        loadBoolean("grief-revert-safety");
        loadBoolean("entry-alert");
        loadBoolean("cuboid");
        loadBoolean("visualize-on-src");
        loadBoolean("visualize-on-place");
        loadBoolean("keep-chunks-loaded");
        loadBoolean("place-grief");
        loadBoolean("toggle-on-disabled");
        loadBoolean("redefine-on-disabled");
        loadBoolean("modify-on-disabled");
        loadBoolean("enable-on-src");
        loadBoolean("breakable-on-disabled");
        loadBoolean("no-player-place");
        loadBoolean("no-projectile-throw");
        loadBoolean("no-dropping-items");
        loadBoolean("no-player-sprint");
        loadBoolean("translocation");
        loadBoolean("translocation-safety");
        loadBoolean("prevent-flight");
        loadBoolean("allowed-can-break");
        loadBoolean("sneaking-bypass");
        loadBoolean("dynmap-area");
        loadBoolean("dynmap-marker");
        loadBoolean("dynmap-disabled");
        loadBoolean("dynmap-no-toggle");
        loadBoolean("dynmap-hide-players");
        loadBoolean("dynmap-show-players");
        loadBoolean("can-change-owner");
        loadBoolean("no-allowing");
        loadBoolean("hidable");
        loadBoolean("teleport-before-death");
        loadBoolean("teleport-on-damage");
        loadBoolean("teleport-on-feeding");
        loadBoolean("teleport-mobs-on-enable");
        loadBoolean("teleport-animals-on-enable");
        loadBoolean("teleport-players-on-enable");
        loadBoolean("teleport-villagers-on-enable");
        loadBoolean("teleport-on-fire");
        loadBoolean("teleport-on-pvp");
        loadBoolean("teleport-on-block-place");
        loadBoolean("teleport-on-block-break");
        loadBoolean("teleport-on-sneak");
        loadBoolean("teleport-on-entry");
        loadBoolean("teleport-on-exit");
        loadBoolean("teleport-explosion-effect");
        loadBoolean("teleport-relatively");
        loadBoolean("teleport-announce");
        loadBoolean("teleport-destination");
        loadBoolean("disable-when-online");
        loadBoolean("no-growth");
        loadBoolean("single-use");
        loadBoolean("commands-on-overlap");
        loadBoolean("shareable");
        loadBoolean("buyable");
        loadBoolean("rentable");
        loadBoolean("command-blacklisting");
        loadBoolean("anti-plot");
        loadBoolean("potion-ignore-player");

        metaAutoSet = loadBoolean("meta-autoset");
        metaName = loadString("meta-name");
        metaLore = loadStringList("meta-lore");
        foresterUses = loadInt("forester-uses");
        surfaces = loadTypeEntries("surfaces");
        requiredPermission = loadString("required-permission");
        requiredPermissionUse = loadString("required-permission-use");
        requiredPermissionAllow = loadString("required-permission-allow");
        deleteIfNoPermission = loadString("delete-if-no-permission");
        groupOnEntry = loadString("group-on-entry");
        autoDisableTime = loadPeriodSeconds("auto-disable");
        radius = loadInt("radius");
        mixingGroup = loadInt("mixing-group");
        customVolume = loadInt("custom-volume");
        launchHeight = loadInt("launch-velocity");
        cannonHeight = loadInt("cannon-velocity");
        mineDelaySeconds = loadInt("mine-delay-seconds");
        mineHasFire = loadBoolean("mine-has-fire");
        lightningReplaceBlock = loadInt("lightning-replace-block");
        lightningDelaySeconds = loadInt("lightning-delay-seconds");
        treeCount = loadInt("tree-count");
        growTime = loadInt("grow-time");
        shrubDensity = loadInt("shrub-density");
        groundBlock = loadTypeEntry("ground-block");
        preventUse = loadTypeEntries("prevent-use");
        confiscatedItems = loadTypeEntries("confiscate-items");
        allowedPlayers = loadStringList("always-allow-players");
        deniedPlayers = loadStringList("always-deny-players");
        allowGrief = loadTypeEntries("allow-grief");
        treeTypes = loadIntList("tree-types");
        shrubTypes = loadIntList("shrub-types");
        creatureTypes = loadStringList("creature-types");
        fertileBlocks = loadTypeEntries("fertile-blocks");
        allowedWorlds = loadStringList("allowed-worlds");
        creatureCount = loadInt("creature-count");
        limits = loadIntList("limits");
        price = loadInt("price");
        refund = loadInt("refund", -1);
        unusableItems = loadTypeEntries("unusable-items");
        translocationBlacklist = loadTypeEntries("translocation-blacklist");
        preventPlaceBlacklist = loadTypeEntries("prevent-place-blacklist");
        preventDestroyBlacklist = loadTypeEntries("prevent-destroy-blacklist");
        allowedOnlyInside = loadStringList("allowed-only-inside");
        allowedOnlyOutside = loadStringList("allowed-only-outside");
        heal = loadInt("heal");
        feed = loadInt("feed");
        repair = loadInt("repair");
        damage = loadInt("damage");
        maskOnDisabled = loadInt("mask-on-disabled");
        maskOnEnabled = loadInt("mask-on-enabled");
        mine = loadInt("mine");
        heal = loadInt("heal");
        griefRevertInterval = loadInt("grief-revert-interval");
        commandOnEnter = loadStringList("command-on-enter");
        commandOnExit = loadStringList("command-on-exit");
        playerCommandOnEnter = loadStringList("player-command-on-enter");
        playerCommandOnExit = loadStringList("player-command-on-exit");
        commandBlackList = loadStringList("command-blacklist");
        teleportCost = loadInt("teleport-cost");
        teleportBackAfterSeconds = loadInt("teleport-back-after-seconds");
        teleportMaxDistance = loadInt("teleport-max-distance");
        teleportIfWalkingOn = loadTypeEntries("teleport-if-walking-on");
        teleportIfNotWalkingOn = loadTypeEntries("teleport-if-not-walking-on");
        teleportIfHoldingItems = loadTypeEntries("teleport-if-holding-items");
        teleportIfNotHoldingItems = loadTypeEntries("teleport-if-not-holding-items");
        teleportIfHasItems = loadTypeEntries("teleport-if-has-items");
        teleportIfNotHasItems = loadTypeEntries("teleport-if-not-has-items");
        mustBeAbove = loadInt("must-be-above");
        mustBeBelow = loadInt("must-be-below");
        payToEnable = loadInt("pay-to-enable");
        fenceItem = loadInt("fence-on-place");
        fenceItemPrice = loadInt("price-per-fence");
        rentsLimit = loadInt("rents-limit");
        potionTargets = loadStringList("potion-targets");
        priceMultiplier = loadDouble("price-multiplier");
    }

    protected boolean loadBoolean(String flagStr) {
        if (containsKey(flagStr)) {
            boolean value = Boolean.parseBoolean(getValue(flagStr).toString());

            if (value) {
                loadFlags(getKey(flagStr));
            }

            PreciousStones.debug("   %s: %s", flagStr, value);
            return value;
        }
        return false;
    }

    protected int loadInt(String flagStr) {
        return loadInt(flagStr, 0);
    }

    protected int loadInt(String flagStr, int defaultValue) {
        if (containsKey(flagStr)) {
            if (Helper.isInteger(getValue(flagStr))) {
                int value = (Integer) getValue(flagStr);

                loadFlags(getKey(flagStr));

                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return defaultValue;
    }

    protected double loadDouble(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isDouble(getValue(flagStr)) || Helper.isInteger(getValue(flagStr))) {
                double value = ((Number) getValue(flagStr)).doubleValue();

                loadFlags(getKey(flagStr));

                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return 0;
    }

    protected String loadString(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isString(getValue(flagStr))) {
                String value = (String) getValue(flagStr);

                if (value != null) {
                    if (!value.isEmpty()) {
                        loadFlags(getKey(flagStr));
                    }

                    PreciousStones.debug("   %s: %s", flagStr, value);
                    return ChatColor.translateAlternateColorCodes('&', value);
                } else {
                    PreciousStones.log(Level.WARNING, "** Malformed Flag %s", flagStr);
                    return null;
                }
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return "";
    }

    protected int loadPeriodSeconds(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isInteger(getValue(flagStr))) {
                int value = (Integer) getValue(flagStr);

                loadFlags(getKey(flagStr));

                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }

            if (Helper.isString(getValue(flagStr))) {
                String str = (String) getValue(flagStr);

                int value = SignHelper.periodToSeconds(str);

                loadFlags(getKey(flagStr));

                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return 0;
    }

    protected BlockTypeEntry loadTypeEntry(String flagStr) {
        if (containsKey(flagStr)) {
            Object typeStr = getValue(flagStr);
            BlockTypeEntry value = new BlockTypeEntry(typeStr.toString());

            if (value.isValid()) {
                loadFlags(getKey(flagStr));
                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return null;
    }

    protected List<String> loadStringList(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isStringList(getValue(flagStr))) {
                List<String> value = (List<String>) getValue(flagStr);

                if (value != null) {
                    if (!value.isEmpty()) {
                        loadFlags(getKey(flagStr));
                    }

                    PreciousStones.debug("   %s: %s", flagStr, value);

                    List<String> colored = new ArrayList<>();

                    for (String s : value) {
                        if (s == null || s.isEmpty()) {
                            colored.add("");
                        } else {
                            colored.add(ChatColor.translateAlternateColorCodes('&', s));
                        }
                    }
                    return colored;
                } else {
                    PreciousStones.log(Level.WARNING, "** Malformed Flag %s", flagStr);
                }
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return new ArrayList<>();
    }

    protected List<BlockTypeEntry> loadTypeEntries(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isStringList(getValue(flagStr))) {
                List<BlockTypeEntry> value = Helper.toTypeEntriesBlind((List<Object>) getValue(flagStr));

                if (!value.isEmpty()) {
                    loadFlags(getKey(flagStr));
                }

                PreciousStones.debug("   %s: %s", flagStr, value);
                return value;
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return new ArrayList<>();
    }

    protected List<Integer> loadIntList(String flagStr) {
        if (containsKey(flagStr)) {
            if (Helper.isIntList(getValue(flagStr))) {
                List<Integer> value = (List<Integer>) getValue(flagStr);

                if (value != null) {
                    if (!value.isEmpty()) {
                        loadFlags(getKey(flagStr));
                    }

                    PreciousStones.debug("   %s: %s", flagStr, value);
                    return value;
                } else {
                    PreciousStones.log(Level.WARNING, "** Malformed Flag %s", flagStr);
                }
            }
            PreciousStones.debug("   %s: *bad*", flagStr);
        }
        return new ArrayList<>();
    }

    protected boolean containsKey(String flagStr) {
        if (map.containsKey(flagStr)) {
            return true;
        }

        if (map.containsKey("~" + flagStr)) {
            return true;
        }

        if (map.containsKey("^" + flagStr)) {
            return true;
        }

        return map.containsKey("?" + flagStr);

    }

    protected String getKey(String flagStr) {
        if (map.containsKey(flagStr)) {
            return flagStr;
        }

        if (map.containsKey("~" + flagStr)) {
            return "~" + flagStr;
        }

        if (map.containsKey("^" + flagStr)) {
            return "^" + flagStr;
        }

        if (map.containsKey("?" + flagStr)) {
            return "?" + flagStr;
        }

        return null;
    }

    protected Object getValue(String flagStr) {
        if (map.get(flagStr) != null) {
            return map.get(flagStr);
        }

        if (map.get("~" + flagStr) != null) {
            return map.get("~" + flagStr);
        }

        if (map.get("^" + flagStr) != null) {
            return map.get("^" + flagStr);
        }

        if (map.get("?" + flagStr) != null) {
            return map.get("?" + flagStr);
        }

        return null;
    }

    protected void loadFlags(String flagStr) {
        if (flagStr == null || flagStr.isEmpty()) {
            return;
        }

        if (flagStr.startsWith("^")) {
            FieldFlag flag = FieldFlag.getByString(flagStr);

            if (flag != null) {
                if (!reversedFlags.contains(flag)) {
                    alledflags.add(flag);
                }
                loadFlags(flagStr.substring(1));
            }
            return;
        }

        if (flagStr.startsWith("~")) {
            FieldFlag flag = FieldFlag.getByString(flagStr);

            if (flag != null) {
                if (!alledflags.contains(flag)) {
                    reversedFlags.add(flag);
                }
                loadFlags(flagStr.substring(1));
            }
            return;
        }

        if (flagStr.startsWith("?")) {
            FieldFlag flag = FieldFlag.getByString(flagStr);

            if (flag != null) {
                disabledFlags.add(flag);
                loadFlags(flagStr.substring(1));
            }
            return;
        }

        FieldFlag flag = FieldFlag.getByString(flagStr);

        if (flag != null) {
            defaultFlags.add(flag);
        }
    }

    /**
     * Check if the field has a flag
     *
     * @param flag
     * @return
     */
    public boolean hasDefaultFlag(FieldFlag flag) {
        return defaultFlags.contains(flag);
    }

    /**
     * @return
     */
    public boolean hasNameableFlag() {
        for (FieldFlag flag : defaultFlags) {
            if (flag.isNameable()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return
     */
    public boolean hasVeocityFlag() {
        return defaultFlags.contains(FieldFlag.CANNON) || defaultFlags.contains(FieldFlag.LAUNCH);
    }

    /**
     * @return
     */
    public boolean hasLimit() {
        return !limits.isEmpty();
    }

    /**
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return
     */
    public int getCustomHeight() {
        return this.customHeight;
    }

    /**
     * Whether the block can be translocated or not based on the blacklist
     *
     * @param type
     * @return
     */
    public boolean canTranslocate(BlockTypeEntry type) {
        return !translocationBlacklist.contains(type);
    }

    /**
     * Tells you if the player should be teleported teleport based on the block hes standing on
     *
     * @return
     */
    public boolean teleportDueToWalking(Location loc, Field field, Player player) {
        Block standingOn = new Vec(loc).subtract(0, 1, 0).getBlock();

        if (standingOn.getTypeId() == 0) {
            return false;
        }

        boolean teleport = false;

        if (FieldFlag.TELEPORT_IF_WALKING_ON.applies(field, player)) {
            teleport = teleportIfWalkingOn.contains(new BlockTypeEntry(standingOn));
        }

        if (FieldFlag.TELEPORT_IF_NOT_WALKING_ON.applies(field, player)) {
            teleport = !teleportIfNotWalkingOn.contains(new BlockTypeEntry(standingOn));
        }

        return teleport;
    }

    /**
     * Can destroy (not in blacklist)
     *
     * @param block
     * @return
     */
    public boolean inDestroyBlacklist(Block block) {
        BlockTypeEntry type = new BlockTypeEntry(block);

        return preventDestroyBlacklist.contains(type);
    }

    /**
     * Can place (not in blacklist)
     *
     * @param block
     * @return
     */
    public boolean inPlaceBlacklist(Block block) {
        BlockTypeEntry type = new BlockTypeEntry(block);

        return preventPlaceBlacklist.contains(type);
    }

    /**
     * Whether a command is in the canceled list
     *
     * @param command
     * @return
     */
    public boolean isCanceledCommand(String command) {
        command = command.replace("/", "");

        int i = command.indexOf(' ');

        if (i > -1) {
            command = command.substring(0, i);
        }

        return commandBlackList.contains(command);
    }

    /**
     * Checks to see if a player should be teleported for holding this item
     *
     * @param entry
     * @return
     */
    public boolean isTeleportHoldingItem(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return true;
        }

        if (teleportIfHasItems.contains(new BlockTypeEntry(0)) && entry.getTypeId() != 0) {
            return true;
        }

        return teleportIfHoldingItems.contains(entry);
    }

    /**
     * Checks to see if a player should be teleported for not holding this item
     *
     * @param entry
     * @return
     */
    public boolean isTeleportNotHoldingItem(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return true;
        }

        return teleportIfNotHoldingItems.contains(entry);
    }

    /**
     * Checks to see if a player should be teleported for having this item
     *
     * @param entry
     * @return
     */
    public boolean isTeleportHasItem(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        if (teleportIfHasItems.contains(new BlockTypeEntry(0)) && entry.getTypeId() != 0) {
            return true;
        }

        return teleportIfHasItems.contains(entry);
    }

    /**
     * Checks to see if a player should be teleported for not having this item
     *
     * @param entry
     * @return
     */
    public boolean isTeleportHasNotItem(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        return teleportIfNotHasItems.contains(entry);
    }

    /**
     * Whether the flag has been reversed
     *
     * @param flag
     * @return
     */
    public boolean isReversedFlag(FieldFlag flag) {
        return reversedFlags.contains(flag);
    }

    /**
     * Whether the flag has been set to all
     *
     * @param flag
     * @return
     */
    public boolean isAlledFlag(FieldFlag flag) {
        return alledflags.contains(flag);
    }

    /**
     * Whether a block type can be used in this field
     *
     * @param entry
     * @return
     */
    public boolean canUse(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return true;
        }

        return !preventUse.contains(entry);
    }

    /**
     * Whether an item is an unusable item
     *
     * @param type
     * @return
     */
    public boolean isUnusableItem(int type, byte data) {
        for (BlockTypeEntry entry : unusableItems) {
            // if the banned item has no data, then that means
            // they want to ban all ids for that block

            // otherwise match the type and data exactly

            if (entry.getData() == 0) {
                if (entry.getTypeId() == type) {
                    return true;
                }
            } else {
                if (entry.getTypeId() == type && entry.getData() == data) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Whether a block type can be used in this field
     *
     * @param type
     * @return
     */
    public boolean canCarry(int type, byte data) {
        if (confiscatedItems.isEmpty()) {
            return true;
        }

        for (BlockTypeEntry entry : confiscatedItems) {
            // if the banned item has no data, then that means
            // they want to ban all ids for that block

            // otherwise match the type and data exactly

            if (entry.getData() == 0) {
                if (entry.getTypeId() == type) {
                    return false;
                }
            } else {
                if (entry.getTypeId() == type && entry.getData() == data) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Retuns a string with all the potions
     *
     * @return
     */
    public String getPotionString() {
        String out = "";
        for (PotionEffectType potion : potions.keySet()) {
            out += Helper.friendlyName(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    /**
     * Returns a tring with all the neutralized potions
     *
     * @return
     */
    public String getNeutralizePotionString() {
        String out = "";

        for (PotionEffectType potion : neutralizePotions) {
            out += Helper.friendlyName(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    public Set<FieldSettings> getMergedFields() {
        return mergedFields;
    }

    public List<BlockTypeEntry> getMergedFieldsTypeEntries() {
        return mergedFields.stream().map(FieldSettings::getTypeEntry).collect(Collectors.toList());
    }

    public void addMergedField(BlockTypeEntry entry) {
        SettingsManager sm = PreciousStones.getInstance().getSettingsManager();
        FieldSettings fs = sm.getFieldSettings(entry);
        this.mergedFields.add(fs);

        for (FieldFlag flag : fs.getDefaultFlags()) {
            if (flag.equals(FieldFlag.GROUP_ON_ENTRY)) {
                this.groupOnEntry = fs.groupOnEntry;
            }
            if (flag.equals(FieldFlag.LAUNCH)) {
                this.launchHeight = (int) ((this.launchHeight + fs.launchHeight) / 1.5);
            }
            if (flag.equals(FieldFlag.CANNON)) {
                this.cannonHeight = (int) ((this.cannonHeight + fs.cannonHeight) / 1.5);
            }
            if (flag.equals(FieldFlag.MINE)) {
                this.mineDelaySeconds = fs.mineDelaySeconds;
                this.mineHasFire = fs.mineHasFire;
                if (fs.mine > 0) {
                    if (this.mine > 0) {
                        this.mine = (this.mine + fs.mine) / 2;
                    } else {
                        this.mine = fs.mine;
                    }
                }
            }
            if (flag.equals(FieldFlag.LIGHTNING)) {
                this.lightningReplaceBlock = fs.lightningReplaceBlock;
                this.lightningDelaySeconds = fs.lightningDelaySeconds;
            }
            if (flag.equals(FieldFlag.FORESTER)) {
                if (fs.treeCount > 0) {
                    if (this.treeCount > 0) {
                        this.treeCount = (this.treeCount + fs.treeCount) / 2;
                    } else {
                        this.treeCount = fs.treeCount;
                    }
                }
                if (fs.shrubDensity > 0) {
                    if (this.shrubDensity > 0) {
                        this.shrubDensity = (this.shrubDensity + fs.shrubDensity) / 2;
                    } else {
                        this.shrubDensity = fs.shrubDensity;
                    }
                }
                if (fs.creatureCount > 0) {
                    if (this.creatureCount > 0) {
                        this.creatureCount = (this.creatureCount + fs.creatureCount) / 2;
                    } else {
                        this.creatureCount = fs.creatureCount;
                    }
                }
                if (fs.growTime > 0) {
                    if (this.growTime > 0) {
                        this.growTime = (this.growTime + fs.growTime) / 2;
                    } else {
                        this.growTime = fs.growTime;
                    }
                }
                this.groundBlock = fs.groundBlock;
                Helper.addUnique(this.treeTypes, fs.treeTypes);
                Helper.addUnique(this.shrubTypes, fs.shrubTypes);
                Helper.addUnique(this.creatureTypes, fs.creatureTypes);
                Helper.addUnique(this.fertileBlocks, fs.fertileBlocks);
            }

            Helper.addUnique(this.preventUse, fs.preventUse);
            Helper.addUnique(this.confiscatedItems, fs.confiscatedItems);
            Helper.addUnique(this.allowedPlayers, fs.allowedPlayers);
            Helper.addUnique(this.deniedPlayers, fs.deniedPlayers);
            Helper.addUnique(this.allowGrief, fs.allowGrief);
            Helper.addUnique(this.allowedWorlds, fs.allowedWorlds);
            Helper.addUnique(this.limits, fs.limits);
            Helper.addUnique(this.unusableItems, fs.unusableItems);
            Helper.addUnique(this.translocationBlacklist, fs.translocationBlacklist);
            Helper.addUnique(this.preventPlaceBlacklist, fs.preventPlaceBlacklist);
            Helper.addUnique(this.preventDestroyBlacklist, fs.preventDestroyBlacklist);
            Helper.addUnique(this.allowedOnlyInside, fs.allowedOnlyInside);
            Helper.addUnique(this.allowedOnlyOutside, fs.allowedOnlyOutside);
            if (fs.heal > 0) {
                if (this.heal > 0) {
                    this.heal = (this.heal + fs.heal) / 2;
                } else {
                    this.heal = fs.heal;
                }
            }
            if (fs.feed > 0) {
                if (this.feed > 0) {
                    this.feed = (this.feed + fs.feed) / 2;
                } else {
                    this.feed = fs.feed;
                }
            }
            if (fs.repair > 0) {
                if (this.repair > 0) {
                    this.repair = (this.repair + fs.repair) / 2;
                } else {
                    this.repair = fs.repair;
                }
            }
            if (fs.damage > 0) {
                if (this.damage > 0) {
                    this.damage = (this.damage + fs.damage) / 2;
                } else {
                    this.damage = fs.damage;
                }
            }
            this.maskOnEnabled = this.maskOnEnabled | fs.maskOnEnabled;
            this.maskOnDisabled = this.maskOnDisabled | fs.maskOnDisabled;

            if (fs.griefRevertInterval > 0) {
                if (this.griefRevertInterval > 0) {
                    this.griefRevertInterval = (this.griefRevertInterval + fs.griefRevertInterval) / 2;
                } else {
                    this.griefRevertInterval = fs.griefRevertInterval;
                }
            }

            Helper.addUnique(this.playerCommandOnEnter, fs.playerCommandOnEnter);
            Helper.addUnique(this.playerCommandOnExit, fs.playerCommandOnExit);
            Helper.addUnique(this.commandOnEnter, fs.commandOnEnter);
            Helper.addUnique(this.commandOnExit, fs.commandOnExit);
            Helper.addUnique(this.commandBlackList, fs.commandBlackList);

            if (fs.teleportCost > 0) {
                if (this.teleportCost > 0) {
                    this.teleportCost = (this.teleportCost + fs.teleportCost) / 2;
                } else {
                    this.teleportCost = fs.teleportCost;
                }
            }
            if (fs.teleportBackAfterSeconds > 0) {
                if (this.teleportBackAfterSeconds > 0) {
                    this.teleportBackAfterSeconds = (this.teleportBackAfterSeconds + fs.teleportBackAfterSeconds) / 2;
                } else {
                    this.teleportBackAfterSeconds = fs.teleportBackAfterSeconds;
                }
            }
            if (fs.teleportMaxDistance > 0) {
                if (this.teleportMaxDistance > 0) {
                    this.teleportMaxDistance = (this.teleportMaxDistance + fs.teleportMaxDistance) / 2;
                } else {
                    this.teleportMaxDistance = fs.teleportMaxDistance;
                }
            }
            Helper.addUnique(this.teleportIfWalkingOn, fs.teleportIfWalkingOn);
            Helper.addUnique(this.teleportIfNotWalkingOn, fs.teleportIfNotWalkingOn);
            Helper.addUnique(this.teleportIfHoldingItems, fs.teleportIfHoldingItems);
            Helper.addUnique(this.teleportIfNotHoldingItems, fs.teleportIfNotHoldingItems);
            Helper.addUnique(this.teleportIfHasItems, fs.teleportIfHasItems);
            Helper.addUnique(this.teleportIfNotHasItems, fs.teleportIfNotHasItems);
            if (fs.mustBeAbove > 0) {
                if (this.mustBeAbove > 0) {
                    this.mustBeAbove = (this.mustBeAbove + fs.mustBeAbove) / 2;
                } else {
                    this.mustBeAbove = fs.mustBeAbove;
                }
            }
            if (fs.mustBeBelow > 0) {
                if (this.mustBeBelow > 0) {
                    this.mustBeBelow = (this.mustBeBelow + fs.mustBeBelow) / 2;
                } else {
                    this.mustBeBelow = fs.mustBeBelow;
                }
            }
            if (fs.payToEnable > 0) {
                if (this.payToEnable > 0) {
                    this.payToEnable = (this.payToEnable + fs.payToEnable) / 2;
                } else {
                    this.payToEnable = fs.payToEnable;
                }
            }
            if (fs.fenceItem > 0) {
                if (this.fenceItem > 0) {
                    this.fenceItem = fs.fenceItem;
                }
            }
            if (fs.fenceItemPrice > 0) {
                if (this.fenceItemPrice > 0) {
                    this.fenceItemPrice = (this.fenceItemPrice + fs.fenceItemPrice) / 2;
                } else {
                    this.fenceItemPrice = fs.fenceItemPrice;
                }
            }
            if (fs.rentsLimit > 0) {
                if (this.rentsLimit > 0) {
                    this.rentsLimit = (this.rentsLimit + fs.rentsLimit) / 2;
                } else {
                    this.rentsLimit = fs.rentsLimit;
                }
            }
            if (fs.priceMultiplier > 0) {
                if (this.priceMultiplier > 0) {
                    this.priceMultiplier = (this.priceMultiplier + fs.priceMultiplier) / 2;
                } else {
                    this.priceMultiplier = fs.priceMultiplier;
                }
            }
            Helper.addUnique(this.potionTargets, fs.potionTargets);
        }
    }


    /**
     * Whether the player is in the allowed list
     *
     * @param playerName
     * @return
     */
    public boolean inAllowedList(String playerName) {
        return allowedPlayers.contains(playerName);
    }

    /**
     * Whether th eplayer is in the denied list
     *
     * @param playerName
     * @return
     */
    public boolean inDeniedList(String playerName) {
        return deniedPlayers.contains(playerName);
    }

    /**
     * Whether a block type can be griefed in a grief revert field
     *
     * @param entry
     * @return
     */
    public boolean canGrief(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        return allowGrief.contains(entry);
    }

    /**
     * If the field can be placed in a world
     *
     * @param world
     * @return
     */
    public boolean allowedWorld(World world) {
        return allowedWorlds.isEmpty() || allowedWorlds.contains(world.getName());
    }

    /**
     * Whether the field has allowed only fields set
     *
     * @return
     */
    public boolean hasAllowedOnlyInside() {
        return !allowedOnlyInside.isEmpty();
    }

    /**
     * If the field is inside and allowed field
     *
     * @return
     */
    public boolean isAllowedOnlyInside(Field field) {
        return allowedOnlyInside.contains(field.getSettings().getTitle());
    }

    /**
     * Returns a formatted string with all the allowed only fields
     *
     * @return
     */
    public String getAllowedOnlyInsideString() {
        return Helper.toMessage(allowedOnlyInside, " or ");
    }

    /**
     * Whether the field has allowed only fields set
     *
     * @return
     */
    public boolean hasAllowedOnlyOutside() {
        return !allowedOnlyOutside.isEmpty();
    }

    /**
     * If the field is inside and allowed field
     *
     * @return
     */
    public boolean isAllowedOnlyOutside(Field field) {
        return allowedOnlyOutside.contains(field.getSettings().getTitle());
    }

    /**
     * Returns a formatted string with all the allowed only fields
     *
     * @return
     */
    public String getAllowedOnlyOutsideString() {
        return Helper.toMessage(allowedOnlyOutside, " or ");
    }

    /**
     * @return the block type id
     */
    public int getTypeId() {
        return type.getTypeId();
    }

    /**
     * @return the block data
     */
    public byte getData() {
        return type.getData();
    }

    /**
     * @return the type entry
     */
    public BlockTypeEntry getTypeEntry() {
        return type;
    }

    /**
     * @return the radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * @return the launchHeight
     */
    public int getLaunchHeight() {
        return launchHeight;
    }

    /**
     * @return the cannonHeight
     */
    public int getCannonHeight() {
        return cannonHeight;
    }

    /**
     * @return the mineDelaySeconds
     */
    public int getMineDelaySeconds() {
        return mineDelaySeconds;
    }

    /**
     * @return the lightningDelaySeconds
     */
    public int getLightningDelaySeconds() {
        return lightningDelaySeconds;
    }

    /**
     * @return the lightningReplaceBlock
     */
    public int getLightningReplaceBlock() {
        return lightningReplaceBlock;
    }

    /**
     * @return the price
     */
    public int getPrice() {
        return price;
    }

    /**
     * @return the price multiplied by price-multiplier
     */
    public int getMultipliedPrice(Player player) {
        List<Field> playerFields = PreciousStones.getInstance().getForceFieldManager().getPlayerFields(player.getName(), this.getTypeEntry());
        int count = playerFields.size();

        return getMultipliedPrice(count);
    }

    /**
     * @return the refund price multiplied by price-multiplier
     */
    public int getMultipliedRefundPrice(Player player) {
        List<Field> playerFields = PreciousStones.getInstance().getForceFieldManager().getPlayerFields(player.getName(), this.getTypeEntry());
        int count = playerFields.size() - 1;

        return getMultipliedPrice(count);
    }

    private int getMultipliedPrice(int count){
        if (count <= 0) {
            return price;
        }

        if (priceMultiplier == 0) {
            return price;
        } else if (priceMultiplier < 1) {
            return (int) (price * priceMultiplier * (count + 2));
        } else if (priceMultiplier == 1) {
            return (int) (price * priceMultiplier * (count + 1));
        } else {
            return (int) (price * priceMultiplier * count);
        }
    }

    /**
     * @return the validField
     */
    public boolean isValidField() {
        return validField;
    }

    /**
     * @return the limits
     */
    public List<Integer> getLimits() {
        return Collections.unmodifiableList(limits);
    }

    public List<FieldFlag> getDefaultFlags() {
        return Collections.unmodifiableList(defaultFlags);
    }

    public int getCustomVolume() {
        return customVolume;
    }

    public int getMixingGroup() {
        return mixingGroup;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public int getAutoDisableTime() {
        return autoDisableTime;
    }

    public String getGroupOnEntry() {
        return groupOnEntry;
    }

    public GameMode getForceEntryGameMode() {
        return forceEntryGameMode;
    }

    public GameMode getForceLeavingGameMode() {
        return forceLeavingGameMode;
    }

    public int getHeal() {
        return heal;
    }

    public int getDamage() {
        return damage;
    }

    public int getFeed() {
        return feed;
    }

    public int getRepair() {
        return repair;
    }

    public List<Integer> getTreeTypes() {
        return new ArrayList<>(treeTypes);
    }

    public List<Integer> getShrubTypes() {
        return new ArrayList<>(shrubTypes);
    }

    public int getShrubDensity() {
        return shrubDensity;
    }

    public int getTreeCount() {
        return treeCount;
    }

    public int getGrowTime() {
        return growTime;
    }

    public boolean isFertileType(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        return fertileBlocks.contains(entry);
    }

    public BlockTypeEntry getGroundBlock() {
        return groundBlock;
    }

    public List<String> getCreatureTypes() {
        return creatureTypes;
    }

    public int getCreatureCount() {
        return creatureCount;
    }

    public boolean isMineHasFire() {
        return mineHasFire;
    }

    public int getMineStrength() {
        return mine;
    }

    public HashMap<PotionEffectType, Integer> getPotions() {
        return potions;
    }

    public List<PotionEffectType> getNeutralizePotions() {
        return neutralizePotions;
    }

    public int getMaskOnDisabledBlock() {
        return maskOnDisabled;
    }

    public int getMaskOnEnabledBlock() {
        return maskOnEnabled;
    }

    public String getRequiredPermissionAllow() {
        return requiredPermissionAllow;
    }

    public String getRequiredPermissionUse() {
        return requiredPermissionUse;
    }

    public int getRefund(Player player) {
        int refunded = -1;

        if (refund > -1) {
            refunded = refund;
        } else {
            if (price > 0) {
                refunded = getMultipliedRefundPrice(player);
            }
        }

        return refunded;
    }

    public int getTeleportCost() {
        return teleportCost;
    }

    public int getTeleportBackAfterSeconds() {
        return teleportBackAfterSeconds;
    }

    public int getTeleportMaxDistance() {
        return teleportMaxDistance;
    }

    public int getGriefRevertInterval() {
        return griefRevertInterval;
    }

    public List<String> getCommandsOnEnter() {
        return commandOnEnter;
    }

    public List<String> getCommandsOnExit() {
        return commandOnExit;
    }

    public List<String> getPlayerCommandsOnEnter() {
        return playerCommandOnEnter;
    }

    public List<String> getPlayerCommandsOnExit() {
        return playerCommandOnExit;
    }

    public List<FieldFlag> getDisabledFlags() {
        return disabledFlags;
    }

    public int getMustBeAbove() {
        return mustBeAbove;
    }

    public int getMustBeBelow() {
        return mustBeBelow;
    }

    public int getPayToEnable() {
        return payToEnable;
    }

    public String getDeleteIfNoPermission() {
        return deleteIfNoPermission;
    }

    public int getFenceItem() {
        return fenceItem;
    }

    public int getFenceItemPrice() {
        return fenceItemPrice;
    }

    public boolean isSurface(Block fieldBlock) {
        if (surfaces.isEmpty()) {
            return true;
        }

        return surfaces.contains(new BlockTypeEntry(fieldBlock.getLocation().add(0, -1, 0).getBlock()));
    }

    public String getSurfaceString() {
        String out = "";

        for (BlockTypeEntry entry : surfaces) {
            out += entry + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    public int getForesterUses() {
        return foresterUses;
    }

    public boolean matchesMetaName(ItemStack item) {
        if (!hasMetaName()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.getDisplayName() != null) {
            return meta.getDisplayName().equals(metaName);
        }

        return false;
    }

    public boolean hasMetaName() {
        return metaName != null && !metaName.isEmpty();
    }

    public String getMetaName() {
        return metaName;
    }

    public List<String> getMetaLore() {
        return metaLore;
    }

    public boolean isMetaAutoSet() {
        return metaAutoSet;
    }

    public int getRentsLimit() {
        return rentsLimit;
    }

    public List<String> getPotionTargets() {
        return potionTargets;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
