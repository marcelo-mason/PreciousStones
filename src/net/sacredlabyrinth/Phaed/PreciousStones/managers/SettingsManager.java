package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author phaed
 */
public final class SettingsManager
{
    private boolean preventDestroyEverywhere;
    private boolean preventPlaceEverywhere;
    private boolean startDynmapFlagsDisabled;
    private boolean sneakPlaceFields;
    private boolean showDefaultWelcomeFarewellMessages;
    private boolean sneakNormalBlock;
    private boolean startMessagesDisabled;
    private boolean disableGroundInfo;
    private boolean autoDownloadVault;
    private int globalFieldLimit;
    private boolean noRefunds;
    private int cuboidDefiningType;
    private int cuboidVisualizationType;
    private boolean logToHawkEye;
    private List<Object> blacklistedWorlds;
    private int purgeSnitchAfterDays;
    private int purgeAfterDays;
    private int maxSnitchRecords;
    private int saveFrequency;
    private List<Object> griefUndoBlackList;
    private int griefRevertMinInterval;
    private int visualizeMarkBlock;
    private int visualizeFrameBlock;
    private int visualizeBlock;
    private int visualizeSeconds;
    private int visualizeDensity;
    private int visualizeTicksBetweenSends;
    private int visualizeSendSize;
    private int visualizeMaxFields;
    private boolean visualizeEndOnMove;
    private boolean debugging;
    private boolean debug;
    private boolean debugdb;
    private boolean debugsql;
    private List<LinkedHashMap<String, Object>> forceFieldBlocks = new ArrayList<LinkedHashMap<String, Object>>();
    private List<Object> unbreakableBlocks = new ArrayList<Object>();
    private List<Object> bypassBlocks = new ArrayList<Object>();
    private List<Object> unprotectableBlocks = new ArrayList<Object>();
    private List<Object> toolItems = new ArrayList<Object>();
    private List<Object> repairableItems = new ArrayList<Object>();
    private List<String> allEntryGroups = new ArrayList<String>();
    private boolean logRollback;
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
    private boolean offByDefault;
    private byte[] throughFields = new byte[]{0, 6, 8, 9, 10, 11, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 69, 68, 70, 72, 75, 76, 77, 83, 92, 93, 94, 104, 105, 106};
    private HashSet<Byte> throughFieldsSet = new HashSet<Byte>();
    private int linesPerPage;
    private boolean useMysql;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;
    private List<Integer> ffBlocks = new ArrayList<Integer>();
    private final HashMap<Integer, FieldSettings> fieldDefinitions = new HashMap<Integer, FieldSettings>();
    private PreciousStones plugin;
    private File main;
    private FileConfiguration config;

    /**
     *
     */
    public SettingsManager()
    {
        plugin = PreciousStones.getInstance();
        config = plugin.getConfig();
        main = new File(plugin.getDataFolder() + File.separator + "config.yml");
        load();
    }

    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void load()
    {
        for (byte throughField : throughFields)
        {
            throughFieldsSet.add(throughField);
        }

        boolean exists = (main).exists();

        if (exists)
        {
            try
            {
                config.options().copyDefaults(true);
                config.load(main);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            config.options().copyDefaults(true);
        }

        forceFieldBlocks = (ArrayList) config.get("force-field-blocks");
        bypassBlocks = config.getList("bypass-blocks");
        unprotectableBlocks = config.getList("unprotectable-blocks");
        unbreakableBlocks = config.getList("unbreakable-blocks");
        toolItems = config.getList("tool-items");
        repairableItems = config.getList("repairable-items");
        logRollback = config.getBoolean("log.rollback");
        logFire = config.getBoolean("log.fire");
        logEntry = config.getBoolean("log.entry");
        logPlace = config.getBoolean("log.place");
        logUse = config.getBoolean("log.use");
        logPvp = config.getBoolean("log.pvp");
        logDestroy = config.getBoolean("log.destroy");
        logDestroyArea = config.getBoolean("log.destroy-area");
        logPlaceArea = config.getBoolean("log.place-area");
        logUnprotectable = config.getBoolean("log.unprotectable");
        logBypassPvp = config.getBoolean("log.bypass-pvp");
        logBypassDelete = config.getBoolean("log.bypass-delete");
        logBypassPlace = config.getBoolean("log.bypass-place");
        logBypassDestroy = config.getBoolean("log.bypass-destroy");
        logConflictPlace = config.getBoolean("log.conflict-place");
        notifyRollback = config.getBoolean("notify.rollback");
        notifyPlace = config.getBoolean("notify.place");
        notifyDestroy = config.getBoolean("notify.destroy");
        notifyBypassUnprotectable = config.getBoolean("notify.bypass-unprotectable");
        notifyBypassPvp = config.getBoolean("notify.bypass-pvp");
        notifyBypassPlace = config.getBoolean("notify.bypass-place");
        notifyBypassDestroy = config.getBoolean("notify.bypass-destroy");
        notifyFlyZones = config.getBoolean("notify.fly-zones");
        warnInstantHeal = config.getBoolean("warn.instant-heal");
        warnSlowHeal = config.getBoolean("warn.slow-heal");
        warnSlowDamage = config.getBoolean("warn.slow-damage");
        warnSlowFeeding = config.getBoolean("warn.slow-feeding");
        warnSlowRepair = config.getBoolean("warn.slow-repair");
        warnFastDamage = config.getBoolean("warn.fast-damage");
        warnGiveAir = config.getBoolean("warn.air");
        warnFire = config.getBoolean("warn.fire");
        warnEntry = config.getBoolean("warn.entry");
        warnPlace = config.getBoolean("warn.place");
        warnUse = config.getBoolean("warn.use");
        warnPvp = config.getBoolean("warn.pvp");
        warnDestroy = config.getBoolean("warn.destroy");
        warnDestroyArea = config.getBoolean("warn.destroy-area");
        warnUnprotectable = config.getBoolean("warn.unprotectable");
        warnLaunch = config.getBoolean("warn.launch");
        warnCannon = config.getBoolean("warn.cannon");
        warnMine = config.getBoolean("warn.mine");
        preventPlaceEverywhere = config.getBoolean("settings.prevent-place-everywhere");
        preventDestroyEverywhere = config.getBoolean("settings.prevent-destroy-everywhere");
        showDefaultWelcomeFarewellMessages = config.getBoolean("settings.show-default-welcome-farewell-messages");
        sneakNormalBlock = config.getBoolean("settings.sneak-to-place-field");
        sneakPlaceFields = config.getBoolean("settings.sneak-to-place-normal-block");
        startMessagesDisabled = config.getBoolean("settings.welcome-farewell-disabled-by-default");
        startDynmapFlagsDisabled = config.getBoolean("settings.dynmap-flags-disabled-by-default");
        disableGroundInfo = config.getBoolean("settings.disable-ground-info");
        autoDownloadVault = config.getBoolean("settings.auto-download-vault");
        globalFieldLimit = config.getInt("settings.global-field-limit");
        noRefunds = config.getBoolean("settings.no-refund-for-fields");
        publicBlockDetails = config.getBoolean("settings.public-block-details");
        dropOnDelete = config.getBoolean("settings.drop-on-delete");
        disableAlertsForAdmins = config.getBoolean("settings.disable-alerts-for-admins");
        disableBypassAlertsForAdmins = config.getBoolean("settings.disable-bypass-alerts-for-admins");
        offByDefault = config.getBoolean("settings.off-by-default");
        linesPerPage = config.getInt("settings.lines-per-page");
        logToHawkEye = config.getBoolean("settings.log-to-hawkeye");
        debugging = config.getBoolean("settings.show-debug-info");
        blacklistedWorlds = config.getList("settings.blacklisted-worlds");
        cuboidDefiningType = config.getInt("cuboid.defining-blocktype");
        cuboidVisualizationType = config.getInt("cuboid.visualization-blocktype");
        purgeAfterDays = config.getInt("cleanup.player-inactivity-purge-days");
        purgeSnitchAfterDays = config.getInt("cleanup.snitch-unused-purge-days");
        saveFrequency = config.getInt("saving.frequency-seconds");
        maxSnitchRecords = config.getInt("saving.max-records-per-snitch");
        visualizeFrameBlock = config.getInt("visualization.frame-block-type");
        visualizeBlock = config.getInt("visualization.block-type");
        visualizeSeconds = config.getInt("visualization.seconds");
        visualizeEndOnMove = config.getBoolean("visualization.end-on-player-move");
        visualizeMarkBlock = config.getInt("visualization.mark-block-type");
        visualizeDensity = config.getInt("visualization.default-density");
        visualizeSendSize = config.getInt("visualization.blocks-to-send");
        visualizeMaxFields = config.getInt("visualization.max-fields-to-visualize-at-once");
        visualizeTicksBetweenSends = config.getInt("visualization.ticks-between-sends");
        griefRevertMinInterval = config.getInt("grief-undo.min-interval-secs");
        griefUndoBlackList = config.getList("grief-undo.black-list");
        useMysql = config.getBoolean("mysql.enable");
        host = config.getString("mysql.host");
        port = config.getInt("mysql.port");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");

        addForceFieldStones(forceFieldBlocks);

        save();
    }

    /**
     *
     */
    public void save()
    {
        try
        {
            config.save(main);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param maps
     */
    @SuppressWarnings("unchecked")
    public void addForceFieldStones(List<LinkedHashMap<String, Object>> maps)
    {
        if (maps == null)
        {
            return;
        }

        for (LinkedHashMap<String, Object> map : maps)
        {
            FieldSettings fs = new FieldSettings(map);

            if (fs.isValidField())
            {
                // add field definition to our collection

                fieldDefinitions.put(fs.getRawTypeId(), fs);

                if (fs.getGroupOnEntry() != null)
                {
                    allEntryGroups.add(fs.getGroupOnEntry());
                }

                // add the type id to our reference list

                ffBlocks.add(fs.getRawTypeId());

                if (!ffBlocks.contains(fs.getTypeId()))
                {
                    ffBlocks.add(fs.getTypeId());
                }
            }
        }
    }

    /**
     * Whether any pstones have welcome or farewell flags
     *
     * @return
     */
    public boolean haveNameable()
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasNameableFlag())
            {
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
    public boolean haveVelocity()
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasVeocityFlag())
            {
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
    public boolean haveGriefRevert()
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasDefaultFlag(FieldFlag.GRIEF_REVERT))
            {
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
    public boolean haveSnitch()
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasDefaultFlag(FieldFlag.SNITCH))
            {
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
    public boolean haveLimits()
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasLimit())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a world is blacklisted
     *
     * @param world
     * @return
     */
    public boolean isBlacklistedWorld(World world)
    {
        return getBlacklistedWorlds().contains(world.getName());
    }

    /**
     * Check if a type is one of the unprotectable types
     *
     * @param type
     * @return
     */
    public boolean isUnprotectableType(int type)
    {
        return getUnprotectableBlocks().contains(type);
    }

    /**
     * Check if the id is one of grief undo blacklisted types
     *
     * @param id
     * @return
     */
    public boolean isGriefUndoBlackListType(int id)
    {
        return getGriefUndoBlackList().contains(id);
    }

    /**
     * Check if a type is a see through block
     *
     * @param type
     * @return
     */
    public boolean isThroughType(int type)
    {
        for (byte throughField : throughFields)
        {
            if (throughField == type)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a block is one of the tool item types
     *
     * @param typeId
     * @return
     */
    public boolean isToolItemType(int typeId)
    {
        return getToolItems().contains(typeId);
    }

    /**
     * Check if a item is one of the repairable item types
     *
     * @param typeId
     * @return
     */
    public boolean isRepairableItemType(int typeId)
    {
        return getRepairableItems().contains(typeId);
    }

    /**
     * Check if a block is one of the snitch types
     *
     * @param block
     * @return
     */
    public boolean isSnitchType(Block block)
    {
        for (FieldSettings fs : fieldDefinitions.values())
        {
            if (fs.hasDefaultFlag(FieldFlag.SNITCH) && plugin.getForceFieldManager().isSameBlock(fs.getRawTypeId(), Helper.toRawTypeId(block)))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a type is one of the unbreakable types
     *
     * @param typeId
     * @return
     */
    public boolean isUnbreakableType(int typeId)
    {
        boolean contains = false;

        if (typeId == 74)
        {
            contains = getFfBlocks().contains(73);
        }

        if (typeId == 62)
        {
            contains = getFfBlocks().contains(61);
        }


        return contains || getUnbreakableBlocks().contains(typeId);
    }

    /**
     * Check if a block is one of the unbreakable types
     *
     * @param unbreakableblock
     * @return
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
        return isUnbreakableType(unbreakableblock.getTypeId());
    }

    /**
     * Check if a type is one of the unbreakable types
     *
     * @param type
     * @return
     */
    public boolean isUnbreakableType(String type)
    {
        return isUnbreakableType(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the forcefeld types
     *
     * @param block
     * @return
     */
    public boolean isFieldType(Block block)
    {
        return isFieldType(block.getTypeId());
    }

    /**
     * Check if a type is one of the forcefeld types
     *
     * @param type
     * @return
     */
    public boolean isFieldType(String type)
    {
        return isFieldType(Material.getMaterial(type).getId());
    }

    /**
     * Check if the material is one of the forcefeld types
     *
     * @param material
     * @return
     */
    public boolean isFieldType(Material material)
    {
        return isFieldType(material.getId());
    }

    /**
     * Check if a type is one of the forcefeld types
     *
     * @param typeId
     * @return
     */
    public boolean isFieldType(int typeId)
    {
        boolean contains = false;

        if (typeId == 74)
        {
            contains = getFfBlocks().contains(73);
        }

        if (typeId == 62)
        {
            contains = getFfBlocks().contains(61);
        }

        return contains || getFfBlocks().contains(typeId);
    }

    /**
     * Whetehr the block is a bypass type
     *
     * @param block
     * @return
     */
    public boolean isBypassBlock(Block block)
    {
        return getBypassBlocks().contains(block.getTypeId());
    }

    /**
     * Returns the settings for a specific field type
     *
     * @param field
     * @return
     */
    public FieldSettings getFieldSettings(Field field)
    {
        return getFieldSettings(field.getRawTypeId());
    }

    /**
     * Returns the settings for a specific block type
     *
     * @param typeId
     * @return
     */
    public FieldSettings getFieldSettings(int typeId)
    {
        return fieldDefinitions.get(typeId);
    }

    /**
     * Returns all the field settings
     *
     * @return
     */
    public HashMap<Integer, FieldSettings> getFieldSettings()
    {
        HashMap<Integer, FieldSettings> fs = new HashMap<Integer, FieldSettings>();
        fs.putAll(fieldDefinitions);
        return fs;
    }

    /**
     * @return the logToHawkEye
     */
    public boolean isLogToHawkEye()
    {
        return logToHawkEye;
    }

    /**
     * @return the blacklistedWorlds
     */
    public List<Object> getBlacklistedWorlds()
    {
        return Collections.unmodifiableList(blacklistedWorlds);
    }

    /**
     * @return the purgeSnitchAfterDays
     */
    public int getPurgeSnitchAfterDays()
    {
        return purgeSnitchAfterDays;
    }

    /**
     * @return the purgeAfterDays
     */
    public int getPurgeAfterDays()
    {
        return purgeAfterDays;
    }

    /**
     * @return the maxSnitchRecords
     */
    public int getMaxSnitchRecords()
    {
        return maxSnitchRecords;
    }

    /**
     * @return the saveFrequency
     */
    public int getSaveFrequency()
    {
        return saveFrequency;
    }

    /**
     * @return the griefUndoBlackList
     */
    public List<Object> getGriefUndoBlackList()
    {
        return Collections.unmodifiableList(griefUndoBlackList);
    }

    /**
     * @return the visualizeMarkBlock
     */
    public int getVisualizeMarkBlock()
    {
        return visualizeMarkBlock;
    }

    /**
     * @return the visualizeBlock
     */
    public int getVisualizeBlock()
    {
        return visualizeBlock;
    }

    /**
     * @return the visualizeSeconds
     */
    public int getVisualizeSeconds()
    {
        return visualizeSeconds;
    }

    /**
     * @return the visualizeEndOnMove
     */
    public boolean isVisualizeEndOnMove()
    {
        return visualizeEndOnMove;
    }

    /**
     * @return the debug
     */
    public boolean isDebug()
    {
        return debug;
    }

    /**
     * @return the debugdb
     */
    public boolean isDebugdb()
    {
        return debugdb;
    }

    /**
     * @return the debugsql
     */
    public boolean isDebugsql()
    {
        return debugsql;
    }

    /**
     * @return the forceFieldBlocks
     */
    public List<LinkedHashMap<String, Object>> getForceFieldBlocks()
    {
        return Collections.unmodifiableList(forceFieldBlocks);
    }

    /**
     * @return the unbreakableBlocks
     */
    public List<Object> getUnbreakableBlocks()
    {
        return Collections.unmodifiableList(unbreakableBlocks);
    }

    /**
     * @return the bypassBlocks
     */
    public List<Object> getBypassBlocks()
    {
        return Collections.unmodifiableList(bypassBlocks);
    }

    /**
     * @return the unprotectableBlocks
     */
    public List<Object> getUnprotectableBlocks()
    {
        return Collections.unmodifiableList(unprotectableBlocks);
    }

    /**
     * @return the toolItems
     */
    public List<Object> getToolItems()
    {
        return Collections.unmodifiableList(toolItems);
    }

    /**
     * @return the repairableItems
     */
    public List<Object> getRepairableItems()
    {
        return Collections.unmodifiableList(repairableItems);
    }

    /**
     * @return the logFire
     */
    public boolean isLogFire()
    {
        return logFire;
    }

    /**
     * @return the logEntry
     */
    public boolean isLogEntry()
    {
        return logEntry;
    }

    /**
     * @return the logPlace
     */
    public boolean isLogPlace()
    {
        return logPlace;
    }

    /**
     * @return the logUse
     */
    public boolean isLogUse()
    {
        return logUse;
    }

    /**
     * @return the logDestroy
     */
    public boolean isLogDestroy()
    {
        return logDestroy;
    }

    /**
     * @return the logDestroyArea
     */
    public boolean isLogDestroyArea()
    {
        return logDestroyArea;
    }

    /**
     * @return the logUnprotectable
     */
    public boolean isLogUnprotectable()
    {
        return logUnprotectable;
    }

    /**
     * @return the logPvp
     */
    public boolean isLogPvp()
    {
        return logPvp;
    }

    /**
     * @return the logBypassPvp
     */
    public boolean isLogBypassPvp()
    {
        return logBypassPvp;
    }

    /**
     * @return the logBypassDelete
     */
    public boolean isLogBypassDelete()
    {
        return logBypassDelete;
    }

    /**
     * @return the logBypassPlace
     */
    public boolean isLogBypassPlace()
    {
        return logBypassPlace;
    }

    /**
     * @return the logBypassDestroy
     */
    public boolean isLogBypassDestroy()
    {
        return logBypassDestroy;
    }

    /**
     * @return the logConflictPlace
     */
    public boolean isLogConflictPlace()
    {
        return logConflictPlace;
    }

    /**
     * @return the notifyPlace
     */
    public boolean isNotifyPlace()
    {
        return notifyPlace;
    }

    /**
     * @return the notifyDestroy
     */
    public boolean isNotifyDestroy()
    {
        return notifyDestroy;
    }

    /**
     * @return the notifyBypassPvp
     */
    public boolean isNotifyBypassPvp()
    {
        return notifyBypassPvp;
    }

    /**
     * @return the notifyBypassPlace
     */
    public boolean isNotifyBypassPlace()
    {
        return notifyBypassPlace;
    }

    /**
     * @return the notifyBypassDestroy
     */
    public boolean isNotifyBypassDestroy()
    {
        return notifyBypassDestroy;
    }

    /**
     * @return the notifyBypassUnprotectable
     */
    public boolean isNotifyBypassUnprotectable()
    {
        return notifyBypassUnprotectable;
    }

    /**
     * @return the warnInstantHeal
     */
    public boolean isWarnInstantHeal()
    {
        return warnInstantHeal;
    }

    /**
     * @return the warnSlowFeeding
     */
    public boolean isWarnSlowFeeding()
    {
        return warnSlowFeeding;
    }

    /**
     * @return the warnSlowRepair
     */
    public boolean isWarnSlowRepair()
    {
        return warnSlowRepair;
    }


    /**
     * @return the warnSlowHeal
     */
    public boolean isWarnSlowHeal()
    {
        return warnSlowHeal;
    }

    /**
     * @return the warnSlowDamage
     */
    public boolean isWarnSlowDamage()
    {
        return warnSlowDamage;
    }

    /**
     * @return the warnFastDamage
     */
    public boolean isWarnFastDamage()
    {
        return warnFastDamage;
    }

    /**
     * @return the warnGiveAir
     */
    public boolean isWarnGiveAir()
    {
        return warnGiveAir;
    }

    /**
     * @return the warnPlace
     */
    public boolean isWarnPlace()
    {
        return warnPlace;
    }

    /**
     * @return the warnUse
     */
    public boolean isWarnUse()
    {
        return warnUse;
    }

    /**
     * @return the warnDestroy
     */
    public boolean isWarnDestroy()
    {
        return warnDestroy;
    }

    /**
     * @return the warnDestroyArea
     */
    public boolean isWarnDestroyArea()
    {
        return warnDestroyArea;
    }

    /**
     * @return the warnUnprotectable
     */
    public boolean isWarnUnprotectable()
    {
        return warnUnprotectable;
    }

    /**
     * @return the warnEntry
     */
    public boolean isWarnEntry()
    {
        return warnEntry;
    }

    /**
     * @return the warnPvp
     */
    public boolean isWarnPvp()
    {
        return warnPvp;
    }

    /**
     * @return the warnFire
     */
    public boolean isWarnFire()
    {
        return warnFire;
    }

    /**
     * @return the warnLaunch
     */
    public boolean isWarnLaunch()
    {
        return warnLaunch;
    }

    /**
     * @return the warnCannon
     */
    public boolean isWarnCannon()
    {
        return warnCannon;
    }

    /**
     * @return the warnMine
     */
    public boolean isWarnMine()
    {
        return warnMine;
    }

    /**
     * @return the publicBlockDetails
     */
    public boolean isPublicBlockDetails()
    {
        return publicBlockDetails;
    }

    /**
     * @return the dropOnDelete
     */
    public boolean isDropOnDelete()
    {
        return dropOnDelete;
    }

    /**
     * @return the disableAlertsForAdmins
     */
    public boolean isDisableAlertsForAdmins()
    {
        return disableAlertsForAdmins;
    }

    /**
     * @return the disableBypassAlertsForAdmins
     */
    public boolean isDisableBypassAlertsForAdmins()
    {
        return disableBypassAlertsForAdmins;
    }

    /**
     * @return the offByDefault
     */
    public boolean isOffByDefault()
    {
        return offByDefault;
    }

    /**
     * @return the ffBlocks
     */
    public List<Integer> getFfBlocks()
    {
        return Collections.unmodifiableList(ffBlocks);
    }

    /**
     * @return the linesPerPage
     */
    public int getLinesPerPage()
    {
        return linesPerPage;
    }

    /**
     * @return the useMysql
     */
    public boolean isUseMysql()
    {
        return useMysql;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the database
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    /**
     * @param debugdb the debugdb to set
     */
    public void setDebugdb(boolean debugdb)
    {
        this.debugdb = debugdb;
    }

    /**
     * @param debugsql the debugsql to set
     */
    public void setDebugsql(boolean debugsql)
    {
        this.debugsql = debugsql;
    }

    /**
     * @return the throughFieldsSet
     */
    public HashSet<Byte> getThroughFieldsSet()
    {
        return new HashSet<Byte>(throughFieldsSet);
    }

    public int getCuboidDefiningType()
    {
        return cuboidDefiningType;
    }

    public int getCuboidVisualizationType()
    {
        return cuboidVisualizationType;
    }

    public int getVisualizeFrameBlock()
    {
        return visualizeFrameBlock;
    }

    public int getVisualizeTicksBetweenSends()
    {
        return visualizeTicksBetweenSends;
    }

    public int getVisualizeSendSize()
    {
        return visualizeSendSize;
    }

    public int getPort()
    {
        return port;
    }

    public int getVisualizeDensity()
    {
        return visualizeDensity;
    }

    public int getGriefRevertMinInterval()
    {
        return griefRevertMinInterval;
    }

    public boolean isLogRollback()
    {
        return logRollback;
    }

    public boolean isNotifyRollback()
    {
        return notifyRollback;
    }

    public boolean isLogPlaceArea()
    {
        return logPlaceArea;
    }

    public void setVisualizeSendSize(int visualizeSendSize)
    {
        this.visualizeSendSize = visualizeSendSize;
    }

    public int getVisualizeMaxFields()
    {
        return visualizeMaxFields;
    }

    public void setVisualizeMaxFields(int visualizeMaxFields)
    {
        this.visualizeMaxFields = visualizeMaxFields;
    }

    public List<String> getAllEntryGroups()
    {
        return Collections.unmodifiableList(allEntryGroups);
    }

    public boolean isNotifyFlyZones()
    {
        return notifyFlyZones;
    }

    public boolean isNoRefunds()
    {
        return noRefunds;
    }

    public int getGlobalFieldLimit()
    {
        return globalFieldLimit;
    }

    public boolean isAutoDownloadVault()
    {
        return autoDownloadVault;
    }

    public boolean isDisableGroundInfo()
    {
        return disableGroundInfo;
    }

    public boolean isStartMessagesDisabled()
    {
        return startMessagesDisabled;
    }

    public boolean isDebugging()
    {
        return debugging;
    }

    public boolean isSneakNormalBlock()
    {
        return sneakNormalBlock;
    }

    public boolean isShowDefaultWelcomeFarewellMessages()
    {
        return showDefaultWelcomeFarewellMessages;
    }

    public boolean isSneakPlaceFields()
    {
        return sneakPlaceFields;
    }

    public boolean isStartDynmapFlagsDisabled()
    {
        return startDynmapFlagsDisabled;
    }

    public boolean isPreventDestroyEverywhere()
    {
        return preventDestroyEverywhere;
    }

    public boolean isPreventPlaceEverywhere()
    {
        return preventPlaceEverywhere;
    }
}
