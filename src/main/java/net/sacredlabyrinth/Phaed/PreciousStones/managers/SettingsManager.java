package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.MaterialName;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author phaed
 */
public final class SettingsManager {
    private int version;
    private BlockTypeEntry defaulItemCurrency;
    private int maxTargetDistance;
    private boolean commandsToRentBuy;
    private boolean warAllow;
    private boolean oncePerBlockOnMove;
    private int maxSizeTranslocation;
    private int maxSizeTranslocationForRedstone;
    private List<String> preventDestroyEverywhere;
    private List<String> preventPlaceEverywhere;
    private boolean sneakPlaceFields;
    private boolean showDefaultWelcomeFarewellMessages;
    private boolean sneakNormalBlock;
    private boolean disableGroundInfo;
    private boolean disablePlaceWhileCreative;
    private boolean disableBreakWhileCreative;
    private boolean preventRemovalIfPlayerInField;
    private boolean autoAddClan;
    private boolean autoAddTeam;
    private int globalFieldLimit;
    private boolean noRefunds;
    private BlockTypeEntry cuboidDefiningType;
    private BlockTypeEntry cuboidVisualizationType;
    private boolean disableMessages;
    private List<String> blacklistedWorlds;
    private int maxSnitchRecords;
    private int saveFrequency;
    private Set<Material> griefUndoBlackList;
    private int griefRevertMinInterval;
    private boolean visualizationNewStyle;
    private BlockTypeEntry visualizeMarkBlock;
    private BlockTypeEntry visualizeFrameBlock;
    private BlockTypeEntry visualizeBlock;
    private int visualizeSeconds;
    private int visualizeDensity;
    private int visualizeTicksBetweenSends;
    private int visualizeSendSize;
    private int visualizeMaxFields;
    private boolean visualizeOnExpand;
    private boolean visualizeEndOnMove;
    private boolean preventBreakingHidden;
    private int purgeAfterDays;
    private boolean purgeBannedPlayers;
    private boolean debug;
    private List<LinkedHashMap<String, Object>> forceFieldBlocks = new ArrayList<>();
    private List<BlockTypeEntry> unbreakableBlocks = new ArrayList<>();
    private List<BlockTypeEntry> bypassBlocks = new ArrayList<>();
    private List<BlockTypeEntry> unprotectableBlocks = new ArrayList<>();
    private List<BlockTypeEntry> hidingMaskBlocs = new ArrayList<>();
    private List<BlockTypeEntry> toolItems = new ArrayList<>();
    private List<BlockTypeEntry> repairableItems = new ArrayList<>();
    private List<String> allEntryGroups = new ArrayList<>();
    private boolean logRollback;
    private boolean logTranslocation;
    private boolean logFire;
    private boolean logEntry;
    private boolean logPlace;
    private boolean logPlaceArea;
    private boolean logUse;
    private boolean logDestroy;
    private boolean logDestroyArea;
    private boolean logUnprotectable;
    private boolean logPvp;
    private boolean logBypassPvp;
    private boolean logBypassDelete;
    private boolean logBypassPlace;
    private boolean logBypassDestroy;
    private boolean logConflictPlace;
    private boolean logRentsAndPurchases;
    private boolean notifyTranslocation;
    private boolean notifyRollback;
    private boolean notifyFlyZones;
    private boolean notifyPlace;
    private boolean notifyDestroy;
    private boolean notifyBypassPvp;
    private boolean notifyBypassPlace;
    private boolean notifyBypassDestroy;
    private boolean notifyBypassUnprotectable;
    private boolean warnInstantHeal;
    private boolean warnSlowHeal;
    private boolean warnSlowFeeding;
    private boolean warnSlowRepair;
    private boolean warnSlowDamage;
    private boolean warnFastDamage;
    private boolean warnGiveAir;
    private boolean warnPlace;
    private boolean warnUse;
    private boolean warnDestroy;
    private boolean warnDestroyArea;
    private boolean warnUnprotectable;
    private boolean warnEntry;
    private boolean warnPvp;
    private boolean warnFire;
    private boolean warnLaunch;
    private boolean warnCannon;
    private boolean warnMine;
    private boolean publicBlockDetails;
    private boolean dropOnDelete;
    private boolean disableAlertsForAdmins;
    private boolean disableBypassAlertsForAdmins;
    private boolean disableSimpleClanHook;
    private boolean offByDefault;
    private boolean useIdInSnitches;
    private int fenceMaxDepth;
    private Material[] throughFields = new Material[]{Material.AIR, Material.OAK_SAPLING, Material.WATER, Material.WATER, Material.LAVA, Material.LAVA, Material.DEAD_BUSH, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE, Material.REDSTONE_WIRE, Material.WHEAT, Material.SIGN, Material.LADDER, Material.RAIL, Material.LEVER, Material.AIR, Material.STONE_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.AIR, Material.REDSTONE_TORCH, Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE, Material.CAKE, Material.REPEATER, Material.REPEATER, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.TRIPWIRE_HOOK, Material.TRIPWIRE, Material.POTTED_CACTUS, Material.CARROTS, Material.POTATOES};
    private Material[] naturalThroughFields = new Material[]{Material.AIR, Material.OAK_SAPLING, Material.WATER, Material.WATER, Material.LAVA, Material.LAVA, Material.DEAD_BUSH, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.CARROTS, Material.POTATOES};
    private HashSet<Material> throughFieldsSet = new HashSet<>();
    private HashSet<Material> naturalThroughFieldSet = new HashSet<>();
    private int linesPerPage;
    private boolean useMysql;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;
    private final HashMap<BlockTypeEntry, FieldSettings> fieldDefinitions = new HashMap<>();
    private File main;
    private FileConfiguration config;
    private FileConfiguration cleanConfig;

    /**
     *
     */
    public SettingsManager() {
        PreciousStones plugin = PreciousStones.getInstance();
        config = plugin.getConfig();
        cleanConfig = new YamlConfiguration();
        main = new File(plugin.getDataFolder() + File.separator + "config.yml");
        load();
    }

    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void load() {

        for (Material item : throughFields) {
            throughFieldsSet.add(item);
        }

        for (Material item : naturalThroughFields) {
            naturalThroughFieldSet.add(item);
        }

        boolean exists = (main).exists();

        if (exists) {
            try {
                config.options().copyDefaults(true);
                config.load(main);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            config.options().copyDefaults(true);
        }

        // ********************************** Field configs

        unbreakableBlocks = Helper.toTypeEntries(loadStringList("unbreakable-blocks"));
        forceFieldBlocks = (ArrayList) loadObject("force-field-blocks");

        // ********************************** Lists

        bypassBlocks = Helper.toTypeEntries(loadStringList("bypass-blocks"));
        unprotectableBlocks = Helper.toTypeEntries(loadStringList("unprotectable-blocks"));
        hidingMaskBlocs = Helper.toTypeEntries(loadStringList("hiding-mask-blocks"));
        toolItems = Helper.toTypeEntries(loadStringList("tool-items"));
        repairableItems = Helper.toTypeEntries(loadStringList("repairable-items"));

        // ********************************** Log

        logTranslocation = loadBoolean("log.translocation");
        logRollback = loadBoolean("log.rollback");
        logFire = loadBoolean("log.fire");
        logEntry = loadBoolean("log.entry");
        logPlace = loadBoolean("log.place");
        logUse = loadBoolean("log.use");
        logPvp = loadBoolean("log.pvp");
        logDestroy = loadBoolean("log.destroy");
        logDestroyArea = loadBoolean("log.destroy-area");
        logPlaceArea = loadBoolean("log.place-area");
        logUnprotectable = loadBoolean("log.unprotectable");
        logBypassPvp = loadBoolean("log.bypass-pvp");
        logBypassDelete = loadBoolean("log.bypass-delete");
        logBypassPlace = loadBoolean("log.bypass-place");
        logBypassDestroy = loadBoolean("log.bypass-destroy");
        logConflictPlace = loadBoolean("log.conflict-place");
        logRentsAndPurchases = loadBoolean("log.rents-and-purchases");

        // ********************************** Notify

        notifyTranslocation = loadBoolean("notify.translocation");
        notifyRollback = loadBoolean("notify.rollback");
        notifyPlace = loadBoolean("notify.place");
        notifyDestroy = loadBoolean("notify.destroy");
        notifyBypassUnprotectable = loadBoolean("notify.bypass-unprotectable");
        notifyBypassPvp = loadBoolean("notify.bypass-pvp");
        notifyBypassPlace = loadBoolean("notify.bypass-place");
        notifyBypassDestroy = loadBoolean("notify.bypass-destroy");
        notifyFlyZones = loadBoolean("notify.fly-zones");

        // ********************************** Warn

        warnInstantHeal = loadBoolean("warn.instant-heal");
        warnSlowHeal = loadBoolean("warn.slow-heal");
        warnSlowDamage = loadBoolean("warn.slow-damage");
        warnSlowFeeding = loadBoolean("warn.slow-feeding");
        warnSlowRepair = loadBoolean("warn.slow-repair");
        warnFastDamage = loadBoolean("warn.fast-damage");
        warnGiveAir = loadBoolean("warn.air");
        warnFire = loadBoolean("warn.fire");
        warnEntry = loadBoolean("warn.entry");
        warnPlace = loadBoolean("warn.place");
        warnUse = loadBoolean("warn.use");
        warnPvp = loadBoolean("warn.pvp");
        warnDestroy = loadBoolean("warn.destroy");
        warnDestroyArea = loadBoolean("warn.destroy-area");
        warnUnprotectable = loadBoolean("warn.unprotectable");
        warnLaunch = loadBoolean("warn.launch");
        warnCannon = loadBoolean("warn.cannon");
        warnMine = loadBoolean("warn.mine");

        // ********************************** Settings

        warAllow = loadBoolean("settings.allow-waring-clans-into-protections");
        preventRemovalIfPlayerInField = loadBoolean("settings.prevent-removal-if-player-in-field");
        commandsToRentBuy = loadBoolean("settings.use-commands-to-rent");
        disableSimpleClanHook = loadBoolean("settings.disable-simpleclans-hook");
        maxSizeTranslocation = loadInt("settings.max-size-translocation");
        maxSizeTranslocationForRedstone = loadInt("settings.max-size-translocation-for-redstone");
        version = loadInt("settings.version");
        preventPlaceEverywhere = loadStringList("settings.prevent-place-everywhere");
        preventDestroyEverywhere = loadStringList("settings.prevent-destroy-everywhere");
        showDefaultWelcomeFarewellMessages = loadBoolean("settings.show-default-welcome-farewell-messages");
        sneakPlaceFields = loadBoolean("settings.sneak-to-place-field");
        sneakNormalBlock = loadBoolean("settings.sneak-to-place-normal-block");
        disableGroundInfo = loadBoolean("settings.disable-ground-info");
        globalFieldLimit = loadInt("settings.global-field-limit");
        noRefunds = loadBoolean("settings.no-refund-for-fields");
        publicBlockDetails = loadBoolean("settings.public-block-details");
        dropOnDelete = loadBoolean("settings.drop-on-delete");
        disableAlertsForAdmins = loadBoolean("settings.disable-alerts-for-admins");
        disableBypassAlertsForAdmins = loadBoolean("settings.disable-bypass-alerts-for-admins");
        offByDefault = loadBoolean("settings.off-by-default");
        linesPerPage = loadInt("settings.lines-per-page");
        debug = loadBoolean("settings.show-debug-info");
        blacklistedWorlds = loadStringList("settings.blacklisted-worlds");
        autoAddClan = loadBoolean("settings.auto-allow-clan-on-fields");
        autoAddTeam = loadBoolean("settings.auto-allow-team-on-fields");
        oncePerBlockOnMove = loadBoolean("settings.check-once-per-block-on-move");
        useIdInSnitches = loadBoolean("settings.use-blockids-in-snitches");
        fenceMaxDepth = loadInt("settings.fence-max-depth");
        maxTargetDistance = loadInt("settings.max-target-distance");
        defaulItemCurrency = loadTypeEntry("settings.default-item-currency");
        disablePlaceWhileCreative = loadBoolean("settings.disable-field-place-while-creative");
        disableBreakWhileCreative = loadBoolean("settings.disable-field-break-while-creative");
        preventBreakingHidden = loadBoolean("settings.prevent-breaking-hidden");

        // ********************************** Cuboid

        cuboidDefiningType = loadTypeEntry("cuboid.defining-blocktype");
        cuboidVisualizationType = loadTypeEntry("cuboid.visualization-blocktype");

        // ********************************** Cleanup

        purgeAfterDays = loadInt("cleanup.player-inactivity-purge-days");
        purgeBannedPlayers = loadBoolean("cleanup.purge-banned-players");

        // ********************************** Saving

        saveFrequency = loadInt("saving.frequency-seconds");
        maxSnitchRecords = loadInt("saving.max-records-per-snitch");

        // ********************************** Visualization

        visualizeFrameBlock = loadTypeEntry("visualization.frame-block-type");
        visualizeBlock = loadTypeEntry("visualization.block-type");
        visualizeSeconds = loadInt("visualization.seconds");
        visualizationNewStyle = loadBoolean("visualization.new-dotted-style");
        visualizeEndOnMove = loadBoolean("visualization.end-on-player-move");
        visualizeMarkBlock = loadTypeEntry("visualization.mark-block-type");
        visualizeDensity = loadInt("visualization.default-density");
        visualizeSendSize = loadInt("visualization.blocks-to-send");
        visualizeMaxFields = loadInt("visualization.max-fields-to-visualize-at-once");
        visualizeTicksBetweenSends = loadInt("visualization.ticks-between-sends");
        visualizeOnExpand = loadBoolean("visualization.visualize-on-expand");

        // ********************************** Grief Revert

        griefRevertMinInterval = loadInt("grief-revert.min-interval-secs");
        List<String> griefUndoNames = loadStringList("grief-revert.black-list");
        griefUndoBlackList = new HashSet<>();
        for (String name : griefUndoNames) {
            griefUndoBlackList.add(MaterialName.getBlockMaterial(name));
        }

        // ********************************** DB Settings

        useMysql = loadBoolean("mysql.enable");
        host = loadString("mysql.host");
        port = loadInt("mysql.port");
        database = loadString("mysql.database");
        username = loadString("mysql.username");
        password = loadString("mysql.password");

        addForceFieldStones(forceFieldBlocks);

        save();
    }

    private Boolean loadBoolean(String path) {
        if (config.isBoolean(path)) {
            boolean value = config.getBoolean(path);
            cleanConfig.set(path, value);
            return value;
        }
        return false;
    }

    private String loadString(String path) {
        if (config.isString(path)) {
            String value = config.getString(path);
            cleanConfig.set(path, value);
            return value;
        }

        return "";
    }

    private int loadInt(String path) {
        if (config.isInt(path)) {
            int value = config.getInt(path);
            cleanConfig.set(path, value);
            return value;
        }

        return 0;
    }

    private double loadDouble(String path) {
        if (config.isDouble(path)) {
            double value = config.getDouble(path);
            cleanConfig.set(path, value);
            return value;
        }

        return 0;
    }

    private BlockTypeEntry loadTypeEntry(String path) {
        String value = config.getString(path);
        cleanConfig.set(path, value);
        return new BlockTypeEntry(value);
    }

    private List<Integer> loadIntList(String path) {
        if (config.isList(path)) {
            List<Integer> value = config.getIntegerList(path);
            cleanConfig.set(path, value);
            return value;
        }

        return new ArrayList<>();
    }

    private List<String> loadStringList(String path) {
        if (config.isList(path)) {
            List<String> value = config.getStringList(path);
            cleanConfig.set(path, value);
            return value;
        }

        return new ArrayList<>();
    }

    private Object loadObject(String path) {
        Object value = config.get(path);
        cleanConfig.set(path, value);
        return value;
    }

    /**
     *
     */
    public void save() {
        try {
            cleanConfig.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param maps
     */
    @SuppressWarnings("unchecked")
    public void addForceFieldStones(List<LinkedHashMap<String, Object>> maps) {
        if (maps == null) {
            return;
        }

        for (LinkedHashMap<String, Object> map : maps) {
            FieldSettings fs = new FieldSettings(map);

            if (fs.isValidField()) {
                // add field definition to our collection

                fieldDefinitions.put(fs.getTypeEntry(), fs);

                if (!fs.getGroupOnEntry().isEmpty()) {
                    allEntryGroups.add(fs.getGroupOnEntry());
                }
            }
        }
    }

    /**
     * Whether any pstones have welcome or farewell flags
     *
     * @return
     */
    public boolean haveNameable() {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasNameableFlag()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have cannon or launch flag
     *
     * @return
     */
    public boolean haveVelocity() {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasVeocityFlag()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have greif revert
     *
     * @return
     */
    public boolean haveGriefRevert() {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasDefaultFlag(FieldFlag.GRIEF_REVERT)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have snitch flag
     *
     * @return
     */
    public boolean haveSnitch() {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasDefaultFlag(FieldFlag.SNITCH)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have limits
     *
     * @return
     */
    public boolean haveLimits() {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasLimit()) {
                return true;
            }
        }

        return false;
    }

    public boolean isHarmfulPotion(PotionEffectType pot) {
        return pot.equals(PotionEffectType.SLOW) ||
                pot.equals(PotionEffectType.SLOW_DIGGING) ||
                pot.equals(PotionEffectType.WEAKNESS) ||
                pot.equals(PotionEffectType.BLINDNESS) ||
                pot.equals(PotionEffectType.CONFUSION) ||
                pot.equals(PotionEffectType.HARM) ||
                pot.equals(PotionEffectType.POISON) ||
                pot.equals(PotionEffectType.HUNGER) ||
                pot.equals(PotionEffectType.UNLUCK) ||
                pot.equals(PotionEffectType.INCREASE_DAMAGE);

    }

    public boolean isCrop(Block block) {
        return block.getType().equals(Material.FARMLAND) ||
                block.getType().equals(Material.WHEAT) ||
                block.getType().equals(Material.SUGAR_CANE) ||
                block.getType().equals(Material.CARROT) ||
                block.getType().equals(Material.POTATO) ||
                block.getType().equals(Material.PUMPKIN_STEM) ||
                block.getType().equals(Material.BEETROOT) ||
                block.getType().equals(Material.MELON_STEM) ||
                block.getType().equals(Material.TURTLE_EGG);

    }

    /**
     * Whether the block depends on an adjacent block to be placed
     *
     * @param type
     * @return
     */
    public boolean isDependentBlock(Material type) {
        return type == Material.RED_BED || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL || type == Material.COBWEB || type == Material.DEAD_BUSH || type == Material.DEAD_BUSH || type == Material.DANDELION || type == Material.POPPY || type == Material.BROWN_MUSHROOM || type == Material.RED_MUSHROOM || type == Material.TORCH || type == Material.REDSTONE_WIRE || type == Material.SIGN || type == Material.OAK_DOOR || type == Material.LADDER || type == Material.RAIL || type == Material.AIR || type == Material.LEVER || type == Material.STONE_PRESSURE_PLATE || type == Material.IRON_DOOR || type == Material.OAK_PRESSURE_PLATE || type == Material.AIR || type == Material.REDSTONE_TORCH || type == Material.STONE_BUTTON || type == Material.SNOW || type == Material.OAK_FENCE || type == Material.OAK_TRAPDOOR || type == Material.BROWN_MUSHROOM_BLOCK || type == Material.RED_MUSHROOM_BLOCK || type == Material.IRON_BARS || type == Material.GLASS_PANE || type == Material.PUMPKIN_STEM || type == Material.MELON_STEM || type == Material.VINE || type == Material.OAK_FENCE_GATE || type == Material.LILY_PAD || type == Material.NETHER_BRICK_FENCE || type == Material.NETHER_WART || type == Material.END_PORTAL || type == Material.COCOA || type == Material.TRIPWIRE_HOOK || type == Material.TRIPWIRE;

    }

    /**
     * Check if a world is blacklisted
     *
     * @param world
     * @return
     */
    public boolean isBlacklistedWorld(World world) {
        return getBlacklistedWorlds().contains(world.getName());
    }

    /**
     * Check if a type is one of the unprotectable types
     *
     * @param type
     * @return
     */
    public boolean isUnprotectableType(BlockTypeEntry type) {
        return getUnprotectableBlocks().contains(type);
    }

    /**
     * Check if a type is one of the hiding mask types
     *
     * @param type
     * @return
     */
    public boolean isHidingMaskType(BlockTypeEntry type) {
        return hidingMaskBlocs.contains(type);
    }

    /**
     * Returns the first entry in the hiding mask list
     *
     * @return
     */
    public BlockTypeEntry getFirstHidingMask() {
        return hidingMaskBlocs.get(0);
    }

    /**
     * Check if a type is one of the unprotectable types
     *
     * @param block
     * @return
     */
    public boolean isUnprotectableType(Block block) {
        return getUnprotectableBlocks().contains(new BlockTypeEntry(block));
    }

    /**
     * Check if the id is one of grief undo blacklisted types
     *
     * @param type
     * @return
     */
    public boolean isGriefUndoBlackListType(Material type) {
        return getGriefUndoBlackList().contains(type);
    }

    /**
     * Check if a type is a see through block
     *
     * @param type
     * @return
     */
    public boolean isThroughType(Material type) {
        return throughFieldsSet.contains(type);
    }

    /**
     * Check if a type is a natural see through block
     *
     * @param type
     * @return
     */
    public boolean isNaturalThroughType(Material type) {
        return naturalThroughFieldSet.contains(type);
    }

    /**
     * Check if a block is one of the tool item types
     *
     * @param entry
     * @return
     */
    public boolean isToolItemType(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        return toolItems.contains(entry);
    }

    /**
     * Check if a item is one of the repairable item types
     *
     * @param entry
     * @return
     */
    public boolean isRepairableItemType(BlockTypeEntry entry) {
        if (!entry.isValid()) {
            return false;
        }

        return repairableItems.contains(entry);
    }

    /**
     * Check if a block is one of the snitch types
     *
     * @param block
     * @return
     */
    public boolean isSnitchType(Block block) {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.hasDefaultFlag(FieldFlag.SNITCH) && fs.getTypeEntry().equals(new BlockTypeEntry(block))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a type is one of the unbreakable types
     *
     * @param type
     * @return
     */
    public boolean isUnbreakableType(BlockTypeEntry type) {
        return getUnbreakableBlocks().contains(type);
    }

    /**
     * Check if a block is one of the unbreakable types
     *
     * @param block
     * @return
     */
    public boolean isUnbreakableType(Block block) {
        return isUnbreakableType(new BlockTypeEntry(block));
    }

    /**
     * Check if a block is one of the forcefeld types
     *
     * @param block
     * @return
     */
    public boolean isFieldType(Block block) {
        //PreciousStones.debug("isField: " + new BlockTypeEntry(block));

        return fieldDefinitions.containsKey(new BlockTypeEntry(block));
    }

    /**
     * Check if a block or item meta is one of the forcefeld types
     *
     * @return
     */
    public boolean isFieldType(Block block, ItemStack handItem) {
        return isFieldType(new BlockTypeEntry(block), handItem);
    }

    /**
     * Check if a type is one of the forcefeld types
     *
     * @param type
     * @return
     */
    public boolean isFieldType(BlockTypeEntry type, ItemStack handItem) {
        if (!handItem.hasItemMeta()) {
            FieldSettings settings = fieldDefinitions.get(type);

            if (settings == null) {
                return false;
            }

            if (!settings.isMetaAutoSet()) {
                if (!settings.getMetaName().isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.matchesMetaName(handItem)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an item is a field type with meta attached to it.
     *
     * @param item The item to check
     * @return true if the item is a meta field stone
     */
    public boolean isMetaFieldType(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.hasItemMeta() && isFieldType(new BlockTypeEntry(item), item);
    }

    /**
     * Whetehr the block is a bypass type
     *
     * @param block
     * @return
     */
    public boolean isBypassBlock(Block block) {
        return getBypassBlocks().contains(new BlockTypeEntry(block));
    }

    /**
     * Returns the settings for a specific block
     *
     * @param block
     * @return
     */
    public FieldSettings getFieldSettings(Block block) {
        return getFieldSettings(new BlockTypeEntry(block));
    }

    /**
     * Returns the settings for a specific field type
     *
     * @param field
     * @return
     */
    public FieldSettings getFieldSettings(Field field) {
        return getFieldSettings(field.getTypeEntry());
    }

    /**
     * Returns the settings for a specific block type
     *
     * @param type
     * @return
     */
    public FieldSettings getFieldSettings(BlockTypeEntry type) {
        return fieldDefinitions.get(type);
    }

    /**
     * Returns all the field settings
     *
     * @return
     */
    public HashMap<BlockTypeEntry, FieldSettings> getFieldSettings() {
        HashMap<BlockTypeEntry, FieldSettings> fs = new HashMap<>();
        fs.putAll(fieldDefinitions);
        return fs;
    }

    /**
     * Get a single field by name
     *
     * @return
     */
    public FieldSettings getFieldSettings(String name) {
        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.getTitle().equalsIgnoreCase(name)) {
                return fs;
            }
        }

        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.getTitle().toLowerCase().startsWith(name.toLowerCase())) {
                return fs;
            }
        }

        for (FieldSettings fs : fieldDefinitions.values()) {
            if (fs.getTitle().toLowerCase().contains(name.toLowerCase())) {
                return fs;
            }
        }

        return null;
    }

    /**
     * @return the blacklistedWorlds
     */
    public List<String> getBlacklistedWorlds() {
        return Collections.unmodifiableList(blacklistedWorlds);
    }

    /**
     * @return the maxSnitchRecords
     */
    public int getMaxSnitchRecords() {
        return maxSnitchRecords;
    }

    /**
     * @return the saveFrequency
     */
    public int getSaveFrequency() {
        return saveFrequency;
    }

    /**
     * @return the griefUndoBlackList
     */
    public Set<Material> getGriefUndoBlackList() {
        return griefUndoBlackList;
    }

    /**
     * @return the visualizeMarkBlock
     */
    public BlockTypeEntry getVisualizeMarkBlock() {
        return visualizeMarkBlock;
    }

    /**
     * @return the visualizeBlock
     */
    public BlockTypeEntry getVisualizeBlock() {
        return visualizeBlock;
    }

    /**
     * @return the visualizeSeconds
     */
    public int getVisualizeSeconds() {
        return visualizeSeconds;
    }

    /**
     * @return the visualizeEndOnMove
     */
    public boolean isVisualizeEndOnMove() {
        return visualizeEndOnMove;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the forceFieldBlocks
     */
    public List<LinkedHashMap<String, Object>> getForceFieldBlocks() {
        return Collections.unmodifiableList(forceFieldBlocks);
    }

    /**
     * @return the unbreakableBlocks
     */
    public List<BlockTypeEntry> getUnbreakableBlocks() {
        return Collections.unmodifiableList(unbreakableBlocks);
    }

    /**
     * @return the bypassBlocks
     */
    public List<BlockTypeEntry> getBypassBlocks() {
        return Collections.unmodifiableList(bypassBlocks);
    }

    /**
     * @return the unprotectableBlocks
     */
    public List<BlockTypeEntry> getUnprotectableBlocks() {
        return Collections.unmodifiableList(unprotectableBlocks);
    }

    public boolean hasUnprotectableBlocks() {
        return !unprotectableBlocks.isEmpty();
    }

    /**
     * @return the logFire
     */
    public boolean isLogFire() {
        return logFire;
    }

    /**
     * @return the logEntry
     */
    public boolean isLogEntry() {
        return logEntry;
    }

    /**
     * @return the logPlace
     */
    public boolean isLogPlace() {
        return logPlace;
    }

    /**
     * @return the logUse
     */
    public boolean isLogUse() {
        return logUse;
    }

    /**
     * @return the logDestroy
     */
    public boolean isLogDestroy() {
        return logDestroy;
    }

    /**
     * @return the logDestroyArea
     */
    public boolean isLogDestroyArea() {
        return logDestroyArea;
    }

    /**
     * @return the logUnprotectable
     */
    public boolean isLogUnprotectable() {
        return logUnprotectable;
    }

    /**
     * @return the logPvp
     */
    public boolean isLogPvp() {
        return logPvp;
    }

    /**
     * @return the logBypassPvp
     */
    public boolean isLogBypassPvp() {
        return logBypassPvp;
    }

    /**
     * @return the logBypassDelete
     */
    public boolean isLogBypassDelete() {
        return logBypassDelete;
    }

    /**
     * @return the logBypassPlace
     */
    public boolean isLogBypassPlace() {
        return logBypassPlace;
    }

    /**
     * @return the logBypassDestroy
     */
    public boolean isLogBypassDestroy() {
        return logBypassDestroy;
    }

    /**
     * @return the logConflictPlace
     */
    public boolean isLogConflictPlace() {
        return logConflictPlace;
    }

    /**
     * @return the notifyPlace
     */
    public boolean isNotifyPlace() {
        return notifyPlace;
    }

    /**
     * @return the notifyDestroy
     */
    public boolean isNotifyDestroy() {
        return notifyDestroy;
    }

    /**
     * @return the notifyBypassPvp
     */
    public boolean isNotifyBypassPvp() {
        return notifyBypassPvp;
    }

    /**
     * @return the notifyBypassPlace
     */
    public boolean isNotifyBypassPlace() {
        return notifyBypassPlace;
    }

    /**
     * @return the notifyBypassDestroy
     */
    public boolean isNotifyBypassDestroy() {
        return notifyBypassDestroy;
    }

    /**
     * @return the notifyBypassUnprotectable
     */
    public boolean isNotifyBypassUnprotectable() {
        return notifyBypassUnprotectable;
    }

    /**
     * @return the warnInstantHeal
     */
    public boolean isWarnInstantHeal() {
        return warnInstantHeal;
    }

    /**
     * @return the warnSlowFeeding
     */
    public boolean isWarnSlowFeeding() {
        return warnSlowFeeding;
    }

    /**
     * @return the warnSlowRepair
     */
    public boolean isWarnSlowRepair() {
        return warnSlowRepair;
    }


    /**
     * @return the warnSlowHeal
     */
    public boolean isWarnSlowHeal() {
        return warnSlowHeal;
    }

    /**
     * @return the warnSlowDamage
     */
    public boolean isWarnSlowDamage() {
        return warnSlowDamage;
    }

    /**
     * @return the warnFastDamage
     */
    public boolean isWarnFastDamage() {
        return warnFastDamage;
    }

    /**
     * @return the warnGiveAir
     */
    public boolean isWarnGiveAir() {
        return warnGiveAir;
    }

    /**
     * @return the warnPlace
     */
    public boolean isWarnPlace() {
        return warnPlace;
    }

    /**
     * @return the warnUse
     */
    public boolean isWarnUse() {
        return warnUse;
    }

    /**
     * @return the warnDestroy
     */
    public boolean isWarnDestroy() {
        return warnDestroy;
    }

    /**
     * @return the warnDestroyArea
     */
    public boolean isWarnDestroyArea() {
        return warnDestroyArea;
    }

    /**
     * @return the warnUnprotectable
     */
    public boolean isWarnUnprotectable() {
        return warnUnprotectable;
    }

    /**
     * @return the warnEntry
     */
    public boolean isWarnEntry() {
        return warnEntry;
    }

    /**
     * @return the warnPvp
     */
    public boolean isWarnPvp() {
        return warnPvp;
    }

    /**
     * @return the warnFire
     */
    public boolean isWarnFire() {
        return warnFire;
    }

    /**
     * @return the warnLaunch
     */
    public boolean isWarnLaunch() {
        return warnLaunch;
    }

    /**
     * @return the warnCannon
     */
    public boolean isWarnCannon() {
        return warnCannon;
    }

    /**
     * @return the warnMine
     */
    public boolean isWarnMine() {
        return warnMine;
    }

    /**
     * @return the publicBlockDetails
     */
    public boolean isPublicBlockDetails() {
        return publicBlockDetails;
    }

    /**
     * @return the dropOnDelete
     */
    public boolean isDropOnDelete() {
        return dropOnDelete;
    }

    /**
     * @return the disableAlertsForAdmins
     */
    public boolean isDisableAlertsForAdmins() {
        return disableAlertsForAdmins;
    }

    /**
     * @return the disableBypassAlertsForAdmins
     */
    public boolean isDisableBypassAlertsForAdmins() {
        return disableBypassAlertsForAdmins;
    }

    /**
     * @return the offByDefault
     */
    public boolean isOffByDefault() {
        return offByDefault;
    }

    /**
     * @return the linesPerPage
     */
    public int getLinesPerPage() {
        return linesPerPage;
    }

    /**
     * @return the useMysql
     */
    public boolean isUseMysql() {
        return useMysql;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the throughFieldsSet
     */
    public HashSet<Material> getThroughFieldsSet() {
        return throughFieldsSet;
    }

    public BlockTypeEntry getCuboidDefiningType() {
        return cuboidDefiningType;
    }

    public BlockTypeEntry getCuboidVisualizationType() {
        return cuboidVisualizationType;
    }

    public BlockTypeEntry getVisualizeFrameBlock() {
        return visualizeFrameBlock;
    }

    public int getVisualizeTicksBetweenSends() {
        return visualizeTicksBetweenSends;
    }

    public int getVisualizeSendSize() {
        return visualizeSendSize;
    }

    public int getPort() {
        return port;
    }

    public int getVisualizeDensity() {
        return visualizeDensity;
    }

    public int getGriefRevertMinInterval() {
        return griefRevertMinInterval;
    }

    public boolean isLogRollback() {
        return logRollback;
    }

    public boolean isNotifyRollback() {
        return notifyRollback;
    }

    public boolean isLogPlaceArea() {
        return logPlaceArea;
    }

    public void setVisualizeSendSize(int visualizeSendSize) {
        this.visualizeSendSize = visualizeSendSize;
    }

    public int getVisualizeMaxFields() {
        return visualizeMaxFields;
    }

    public void setVisualizeMaxFields(int visualizeMaxFields) {
        this.visualizeMaxFields = visualizeMaxFields;
    }

    public List<String> getAllEntryGroups() {
        return Collections.unmodifiableList(allEntryGroups);
    }

    public boolean isNotifyFlyZones() {
        return notifyFlyZones;
    }

    public boolean isNoRefunds() {
        return noRefunds;
    }

    public int getGlobalFieldLimit() {
        return globalFieldLimit;
    }

    public boolean isDisableGroundInfo() {
        return disableGroundInfo;
    }

    public boolean isSneakNormalBlock() {
        return sneakNormalBlock;
    }

    public boolean isShowDefaultWelcomeFarewellMessages() {
        return showDefaultWelcomeFarewellMessages;
    }

    public boolean isSneakPlaceFields() {
        return sneakPlaceFields;
    }

    public boolean isPreventDestroyEverywhere(String world) {
        return preventDestroyEverywhere.contains(world);
    }

    public boolean isPreventPlaceEverywhere(String world) {
        return preventPlaceEverywhere.contains(world);
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(int version) {
        config.set("settings.version", version);
        cleanConfig.set("settings.version", version);
        save();
        this.version = version;
    }

    public boolean isLogTranslocation() {
        return logTranslocation;
    }

    public boolean isNotifyTranslocation() {
        return notifyTranslocation;
    }

    public int getMaxSizeTranslocation() {
        return maxSizeTranslocation;
    }

    public void setMaxSizeTranslocation(int maxSizeTranslocation) {
        this.maxSizeTranslocation = maxSizeTranslocation;
    }

    public int getMaxSizeTranslocationForRedstone() {
        return maxSizeTranslocationForRedstone;
    }

    public void setMaxSizeTranslocationForRedstone(int maxSizeTranslocationForRedstone) {
        this.maxSizeTranslocationForRedstone = maxSizeTranslocationForRedstone;
    }

    public boolean isDisableSimpleClanHook() {
        return disableSimpleClanHook;
    }

    public boolean isLogRentsAndPurchases() {
        return logRentsAndPurchases;
    }

    public boolean isAutoAddClan() {
        return autoAddClan;
    }

    public boolean isNaturalFloorType(int type) {
        return type == 1 || type == 2 || type == 3 || type == 4 || type == 7 || type == 12 || type == 13 || type == 14 || type == 15 || type == 16 || type == 17 || type == 21 || type == 60 || type == 73 || type == 74 || type == 80 || type == 82 || type == 87 || type == 88 || type == 110 || type == 97 || type == 129;
    }

    public boolean isOncePerBlockOnMove() {
        return oncePerBlockOnMove;
    }

    public boolean isUseIdInSnitches() {
        return useIdInSnitches;
    }

    public int getFenceMaxDepth() {
        return fenceMaxDepth;
    }

    public boolean isCommandsToRentBuy() {
        return commandsToRentBuy;
    }

    public boolean isVisualizationNewStyle() {
        return visualizationNewStyle;
    }

    public boolean isPreventRemovalIfPlayerInField() {
        return preventRemovalIfPlayerInField;
    }

    public boolean isWarAllow() {
        return warAllow;
    }

    public void setWarAllow(boolean warAllow) {
        this.warAllow = warAllow;
    }

    public int getMaxTargetDistance() {
        return maxTargetDistance;
    }

    public BlockTypeEntry getDefaulItemCurrency() {
        return defaulItemCurrency;
    }

    public boolean isVisualizeOnExpand() {
        return visualizeOnExpand;
    }

    public boolean isAutoAddTeam() {
        return autoAddTeam;
    }

    /**
     * @return the purgeAfterDays
     */
    public int getPurgeAfterDays() {
        return purgeAfterDays;
    }

    public boolean isPurgeBannedPlayers() {
        return purgeBannedPlayers;
    }

    public boolean isDisablePlaceWhileCreative() {
        return disablePlaceWhileCreative;
    }

    public boolean isDisableBreakWhileCreative() {
        return disableBreakWhileCreative;
    }

    public boolean isPreventBreakingHidden() {
        return preventBreakingHidden;
    }
}
