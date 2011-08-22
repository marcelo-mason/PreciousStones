package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings.FieldFlag;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public final class SettingsManager
{
    private boolean logToHawkEye;
    private List<String> blacklistedWorlds;
    private int purgeSnitchAfterDays;
    private int purgeAfterDays;
    private int maxSnitchRecords;
    private int saveFrequency;
    private List<Integer> griefUndoBlackList;
    private int griefIntervalSeconds;
    private int griefUndoBatchSize;
    private int griefUndoBatchDelayTicks;
    private List<Integer> foresterFertileBlocks;
    private int foresterInterval;
    private int foresterTrees;
    private int visualizeAdminChunkRadius;
    private int visualizeMarkBlock;
    private int visualizeMarkChunkRadius;
    private int visualizeBlock;
    private int visualizeSeconds;
    private int visualizeBatchSize;
    private int visualizeBatchDelayTicks;
    private boolean visualizeEndOnMove;
    private boolean debug;
    private boolean debugdb;
    private boolean debugsql;
    private ArrayList<LinkedHashMap> forceFieldBlocks;
    private List<Integer> unbreakableBlocks;
    private List<Integer> bypassBlocks;
    private List<Integer> unprotectableBlocks;
    private List<Integer> toolItems;
    private boolean logFire;
    private boolean logEntry;
    private boolean logPlace;
    private boolean logUse;
    private boolean logEmpty;
    private boolean logDestroy;
    private boolean logDestroyArea;
    private boolean logUnprotectable;
    private boolean logPvp;
    private boolean logBypassPvp;
    private boolean logBypassDelete;
    private boolean logBypassPlace;
    private boolean logBypassDestroy;
    private boolean logBypassUnprotectable;
    private boolean logConflictPlace;
    private boolean notifyPlace;
    private boolean notifyDestroy;
    private boolean notifyBypassPvp;
    private boolean notifyBypassPlace;
    private boolean notifyBypassDestroy;
    private boolean notifyBypassUnprotectable;
    private boolean warnInstantHeal;
    private boolean warnSlowHeal;
    private boolean warnSlowDamage;
    private boolean warnFastDamage;
    private boolean warnGiveAir;
    private boolean warnPlace;
    private boolean warnUse;
    private boolean warnEmpty;
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
    private boolean sneakingBypassesDamage;
    private boolean allowedCanBreakPstones;
    private boolean dropOnDelete;
    private boolean disableAlertsForAdmins;
    private boolean disableBypassAlertsForAdmins;
    private boolean offByDefault;
    private int chunksInLargestForceFieldArea;
    private int[] throughFields = new int[]
    {
        0, 6, 8, 9, 10, 11, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 69, 68, 70, 72, 75, 76, 77, 83, 92, 93, 94
    };
    private int linesPerPage;
    private boolean useMysql;
    private String host;
    private String database;
    private String username;
    private String password;
    private List<Integer> ffBlocks = new ArrayList<Integer>();
    private final HashMap<Integer, FieldSettings> fieldDefinitions = new HashMap<Integer, FieldSettings>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public SettingsManager()
    {
        plugin = PreciousStones.getInstance();
        load();
    }

    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void load()
    {
        Configuration config = plugin.getConfiguration();
        config.load();

        List<Integer> fblocks = new ArrayList<Integer>();
        fblocks.add(2);
        fblocks.add(3);
        fblocks.add(13);
        fblocks.add(87);

        List<Integer> blacklist = new ArrayList<Integer>();
        blacklist.add(92);

        forceFieldBlocks = (ArrayList) config.getProperty("force-field-blocks");
        unbreakableBlocks = config.getIntList("unbreakable-blocks", new ArrayList<Integer>());
        bypassBlocks = config.getIntList("bypass-blocks", new ArrayList<Integer>());
        unprotectableBlocks = config.getIntList("unprotectable-blocks", new ArrayList<Integer>());
        toolItems = config.getIntList("tool-items", new ArrayList<Integer>());
        logFire = config.getBoolean("log.fire", false);
        logEntry = config.getBoolean("log.entry", false);
        logPlace = config.getBoolean("log.place", false);
        logUse = config.getBoolean("log.use", false);
        logPvp = config.getBoolean("log.pvp", false);
        logDestroy = config.getBoolean("log.destroy", false);
        logDestroyArea = config.getBoolean("log.destroy-area", false);
        logUnprotectable = config.getBoolean("log.unprotectable", false);
        logBypassPvp = config.getBoolean("log.bypass-pvp", false);
        logBypassDelete = config.getBoolean("log.bypass-delete", false);
        logBypassPlace = config.getBoolean("log.bypass-place", false);
        logBypassDestroy = config.getBoolean("log.bypass-destroy", false);
        logConflictPlace = config.getBoolean("log.conflict-place", false);
        notifyPlace = config.getBoolean("notify.place", false);
        notifyDestroy = config.getBoolean("notify.destroy", false);
        notifyBypassUnprotectable = config.getBoolean("notify.bypass-unprotectable", false);
        notifyBypassPvp = config.getBoolean("notify.bypass-pvp", false);
        notifyBypassPlace = config.getBoolean("notify.bypass-place", false);
        notifyBypassDestroy = config.getBoolean("notify.bypass-destroy", false);
        warnInstantHeal = config.getBoolean("warn.instant-heal", false);
        warnSlowHeal = config.getBoolean("warn.slow-heal", false);
        warnSlowDamage = config.getBoolean("warn.slow-damage", false);
        warnFastDamage = config.getBoolean("warn.fast-damage", false);
        warnGiveAir = config.getBoolean("warn.give-air", false);
        warnFire = config.getBoolean("warn.fire", false);
        warnEntry = config.getBoolean("warn.entry", false);
        warnPlace = config.getBoolean("warn.place", false);
        warnUse = config.getBoolean("warn.use", false);
        warnPvp = config.getBoolean("warn.pvp", false);
        warnDestroy = config.getBoolean("warn.destroy", false);
        warnDestroyArea = config.getBoolean("warn.destroy-area", false);
        warnUnprotectable = config.getBoolean("warn.unprotectable", false);
        warnLaunch = config.getBoolean("warn.launch", false);
        warnCannon = config.getBoolean("warn.cannon", false);
        warnMine = config.getBoolean("warn.mine", false);
        publicBlockDetails = config.getBoolean("settings.public-block-details", false);
        sneakingBypassesDamage = config.getBoolean("settings.sneaking-bypasses-damage", false);
        allowedCanBreakPstones = config.getBoolean("settings.allowed-can-break-pstones", false);
        dropOnDelete = config.getBoolean("settings.drop-on-delete", false);
        disableAlertsForAdmins = config.getBoolean("settings.disable-alerts-for-admins", false);
        disableBypassAlertsForAdmins = config.getBoolean("settings.disable-bypass-alerts-for-admins", false);
        offByDefault = config.getBoolean("settings.off-by-default", false);
        linesPerPage = config.getInt("settings.lines-per-page", 12);
        logToHawkEye = config.getBoolean("settings.log-to-hawkeye", false);
        debugdb = config.getBoolean("settings.debug-on", false);
        blacklistedWorlds = config.getStringList("settings.blacklisted-worlds", new ArrayList<String>());
        purgeAfterDays = config.getInt("cleanup.player-inactivity-purge-days", 45);
        purgeSnitchAfterDays = config.getInt("cleanup.snitch-unused-purge-days", 60);
        saveFrequency = config.getInt("saving.frequency-seconds", 300);
        maxSnitchRecords = config.getInt("saving.max-records-per-snitch", 50);
        visualizeBlock = config.getInt("visualization.block-type", 20);
        visualizeSeconds = config.getInt("visualization.seconds", 30);
        visualizeEndOnMove = config.getBoolean("visualization.end-on-player-move", true);
        visualizeAdminChunkRadius = config.getInt("visualization.admin-chunk-radius", 10);
        visualizeMarkBlock = config.getInt("visualization.mark-block-type", 20);
        visualizeMarkChunkRadius = config.getInt("visualization.mark-chunk-radius", 10);
        visualizeBatchSize = config.getInt("visualization.batch-size", 1000);
        visualizeBatchDelayTicks = config.getInt("visualization.batch-delay-ticks", 10);
        foresterInterval = config.getInt("forester.interval-seconds", 1);
        foresterFertileBlocks = config.getIntList("forester.fertile-blocks", fblocks);
        foresterTrees = config.getInt("forester.trees", 60);
        griefIntervalSeconds = config.getInt("grief-undo.interval-seconds", 300);
        griefUndoBlackList = config.getIntList("grief-undo.black-list", blacklist);
        griefUndoBatchSize = config.getInt("grief-undo.batch-size", 1000);
        griefUndoBatchDelayTicks = config.getInt("grief-undo.batch-delay-ticks", 10);
        useMysql = config.getBoolean("mysql.enable", false);
        host = config.getString("mysql.host", "localhost");
        database = config.getString("mysql.database", "minecraft");
        username = config.getString("mysql.username", "");
        password = config.getString("mysql.password", "");

        addForceFieldStones(getForceFieldBlocks());

        save();
    }

    public void save()
    {
        Configuration config = plugin.getConfiguration();

        config.setProperty("force-field-blocks", getForceFieldBlocks());
        config.setProperty("unbreakable-blocks", getUnbreakableBlocks());
        config.setProperty("bypass-blocks", getBypassBlocks());
        config.setProperty("unprotectable-blocks", getUnprotectableBlocks());
        config.setProperty("tool-items", getToolItems());
        config.setProperty("log.fire", isLogFire());
        config.setProperty("log.entry", isLogEntry());
        config.setProperty("log.place", isLogPlace());
        config.setProperty("log.use", isLogUse());
        config.setProperty("log.pvp", isLogPvp());
        config.setProperty("log.destroy", isLogDestroy());
        config.setProperty("log.destroy-area", isLogDestroyArea());
        config.setProperty("log.unprotectable", isLogUnprotectable());
        config.setProperty("log.bypass-pvp", isLogBypassPvp());
        config.setProperty("log.bypass-delete", isLogBypassDelete());
        config.setProperty("log.bypass-place", isLogBypassPlace());
        config.setProperty("log.bypass-destroy", isLogBypassDestroy());
        config.setProperty("log.conflict-place", isLogConflictPlace());
        config.setProperty("notify.place", isNotifyPlace());
        config.setProperty("notify.destroy", isNotifyDestroy());
        config.setProperty("notify.bypass-unprotectable", isNotifyBypassUnprotectable());
        config.setProperty("notify.bypass-pvp", isNotifyBypassPvp());
        config.setProperty("notify.bypass-place", isNotifyBypassPlace());
        config.setProperty("notify.bypass-destroy", isNotifyBypassDestroy());
        config.setProperty("warn.instant-heal", isWarnInstantHeal());
        config.setProperty("warn.slow-heal", isWarnSlowHeal());
        config.setProperty("warn.slow-damage", isWarnSlowDamage());
        config.setProperty("warn.fast-damage", isWarnFastDamage());
        config.setProperty("warn.give-air", isWarnGiveAir());
        config.setProperty("warn.fire", isWarnFire());
        config.setProperty("warn.entry", isWarnEntry());
        config.setProperty("warn.place", isWarnPlace());
        config.setProperty("warn.use", isWarnUse());
        config.setProperty("warn.pvp", isWarnPvp());
        config.setProperty("warn.destroy", isWarnDestroy());
        config.setProperty("warn.destroy-area", isWarnDestroyArea());
        config.setProperty("warn.unprotectable", isWarnUnprotectable());
        config.setProperty("warn.launch", isWarnLaunch());
        config.setProperty("warn.cannon", isWarnCannon());
        config.setProperty("warn.mine", isWarnMine());
        config.setProperty("settings.public-block-details", isPublicBlockDetails());
        config.setProperty("settings.sneaking-bypasses-damage", isSneakingBypassesDamage());
        config.setProperty("settings.allowed-can-break-pstones", isAllowedCanBreakPstones());
        config.setProperty("settings.drop-on-delete", isDropOnDelete());
        config.setProperty("settings.disable-alerts-for-admins", isDisableAlertsForAdmins());
        config.setProperty("settings.disable-bypass-alerts-for-admins", isDisableBypassAlertsForAdmins());
        config.setProperty("settings.off-by-default", isOffByDefault());
        config.setProperty("settings.lines-per-page", getLinesPerPage());
        config.setProperty("settings.blacklisted-worlds", getBlacklistedWorlds());
        config.setProperty("settings.log-to-hawkeye", isLogToHawkEye());
        config.setProperty("cleanup.player-inactivity-purge-days", getPurgeAfterDays());
        config.setProperty("cleanup.snitch-unused-purge-days", getPurgeSnitchAfterDays());
        config.setProperty("saving.frequency-seconds", getSaveFrequency());
        config.setProperty("saving.max-records-per-snitch", getMaxSnitchRecords());
        config.setProperty("visualization.block-type", getVisualizeBlock());
        config.setProperty("visualization.seconds", getVisualizeSeconds());
        config.setProperty("visualization.end-on-player-move", isVisualizeEndOnMove());
        config.setProperty("visualization.admin-chunk-radius", getVisualizeAdminChunkRadius());
        config.setProperty("visualization.mark-block-type", getVisualizeMarkBlock());
        config.setProperty("visualization.mark-chunk-radius", getVisualizeMarkChunkRadius());
        config.setProperty("visualization.batch-size", getVisualizeBatchSize());
        config.setProperty("visualization.batch-delay-ticks", getVisualizeBatchDelayTicks());
        config.setProperty("forester.interval-seconds", getForesterInterval());
        config.setProperty("forester.fertile-blocks", getForesterFertileBlocks());
        config.setProperty("grief-undo.interval-seconds", getGriefIntervalSeconds());
        config.setProperty("grief-undo.black-list", getGriefUndoBlackList());
        config.setProperty("grief-undo.batch-size", getGriefUndoBatchSize());
        config.setProperty("grief-undo.batch-delay-ticks", getGriefUndoBatchDelayTicks());
        config.setProperty("mysql.enable", isUseMysql());
        config.setProperty("mysql.host", getHost());
        config.setProperty("mysql.database", getDatabase());
        config.setProperty("mysql.username", getUsername());
        config.setProperty("mysql.password", getPassword());

        config.save();
    }

    /**
     *
     * @param maps
     */
    @SuppressWarnings("unchecked")
    public void addForceFieldStones(ArrayList<LinkedHashMap> maps)
    {
        if (maps == null)
        {
            return;
        }

        double largestForceField = 0;

        for (LinkedHashMap map : maps)
        {
            FieldSettings fs = new FieldSettings(map);

            if (fs.isValidField())
            {
                // add field definition to our collection

                getFieldDefinitions().put(fs.getTypeId(), fs);

                // add the type id to our reference list

                getFfBlocks().add(fs.getTypeId());

                // see if the radius is the largest

                if (fs.getRadius() > largestForceField)
                {
                    largestForceField = fs.getRadius();
                }
            }
        }

        chunksInLargestForceFieldArea = (int) Math.max(Math.ceil(largestForceField / 16.0), 1);
    }

    /**
     * Whether any pstones have welcome or farewell flags
     * @return
     */
    public boolean haveNameable()
    {
        for (FieldSettings fs : getFieldDefinitions().values())
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
     * @return
     */
    public boolean haveVelocity()
    {
        for (FieldSettings fs : getFieldDefinitions().values())
        {
            if (fs.hasVeocityFlag())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have snitch flag
     * @return
     */
    public boolean haveSnitch()
    {
        for (FieldSettings fs : getFieldDefinitions().values())
        {
            if (fs.hasFlag(FieldFlag.SNITCH))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether any pstones have limits
     * @return
     */
    public boolean haveLimits()
    {
        for (FieldSettings fs : getFieldDefinitions().values())
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
     * @param placedblock
     * @return
     */
    public boolean isBlacklistedWorld(World world)
    {
        return getBlacklistedWorlds().contains(world.getName());
    }

    /**
     * Check if a type is one of the unprotectable types
     * @param placedblock
     * @return
     */
    public boolean isUnprotectableType(int type)
    {
        return getUnprotectableBlocks().contains(type);
    }

    /**
     * Check if the id is one of forrester fertile types
     * @param block
     * @return
     */
    public boolean isFertileType(int id)
    {
        return getForesterFertileBlocks().contains(id);
    }

    /**
     * Check if the id is one of grief undo blacklisted types
     * @param block
     * @return
     */
    public boolean isGriefUndoBlackListType(int id)
    {
        return getGriefUndoBlackList().contains(id);
    }

    /**
     * Check if a type is a see through block
     * @param block
     * @return
     */
    public boolean isThroughType(int type)
    {
        for (int i = 0; i < getThroughFields().length; i++)
        {
            if (getThroughFields()[i] == type)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a block is one of the tool item types
     * @param block
     * @return
     */
    public boolean isToolItemType(Block block)
    {
        return getToolItems().contains(block.getTypeId());
    }

    /**
     * Check if a block is one of the tool item types
     * @param typeId
     * @return
     */
    public boolean isToolItemType(int typeId)
    {
        return getToolItems().contains(typeId);
    }

    /**
     * Check if a block is one of the tool item types
     * @param type
     * @return
     */
    public boolean isToolItemType(String type)
    {
        return getToolItems().contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the snitch types
     * @param block
     * @return
     */
    public boolean isSnitchType(Block block)
    {
        for (FieldSettings fs : getFieldDefinitions().values())
        {
            if (fs.hasFlag(FieldFlag.SNITCH) && fs.getTypeId() == block.getTypeId())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a block is one of the unbreakable types
     * @param unbreakableblock
     * @return
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
        return getUnbreakableBlocks().contains(unbreakableblock.getTypeId());
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param typeId
     * @return
     */
    public boolean isUnbreakableType(int typeId)
    {
        return getUnbreakableBlocks().contains(typeId);
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param type
     * @return
     */
    public boolean isUnbreakableType(String type)
    {
        return getUnbreakableBlocks().contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the forcefeld types
     * @param block
     * @return
     */
    public boolean isFieldType(Block block)
    {
        return getFfBlocks().contains(block.getTypeId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param type
     * @return
     */
    public boolean isFieldType(String type)
    {
        return getFfBlocks().contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if the material is one of the forcefeld types
     * @param material
     * @return
     */
    public boolean isFieldType(Material material)
    {
        return getFfBlocks().contains(material.getId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param typeId
     * @return
     */
    public boolean isFieldType(int typeId)
    {
        return getFfBlocks().contains(typeId);
    }

    /**
     * Whetehr the block is a bypass type
     * @param block
     * @return
     */
    public boolean isBypassBlock(Block block)
    {
        return getBypassBlocks().contains(block.getTypeId());
    }

    /**
     * Returns the settings for a specific field type
     * @param field
     * @return
     */
    public FieldSettings getFieldSettings(Field field)
    {
        return getFieldSettings(field.getTypeId());
    }

    /**
     * Returns the settings for a specific block type
     * @param typeId
     * @return
     */
    public FieldSettings getFieldSettings(int typeId)
    {
        return getFieldDefinitions().get(typeId);
    }

    /**
     * Returns all the field settings
     * @return
     */
    public HashMap<Integer, FieldSettings> getFieldSettings()
    {
        return getFieldDefinitions();
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
    public List<String> getBlacklistedWorlds()
    {
        return blacklistedWorlds;
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
    public List<Integer> getGriefUndoBlackList()
    {
        return griefUndoBlackList;
    }

    /**
     * @return the griefIntervalSeconds
     */
    public int getGriefIntervalSeconds()
    {
        return griefIntervalSeconds;
    }

    /**
     * @return the griefUndoBatchSize
     */
    public int getGriefUndoBatchSize()
    {
        return griefUndoBatchSize;
    }

    /**
     * @return the griefUndoBatchDelayTicks
     */
    public int getGriefUndoBatchDelayTicks()
    {
        return griefUndoBatchDelayTicks;
    }

    /**
     * @return the foresterFertileBlocks
     */
    public List<Integer> getForesterFertileBlocks()
    {
        return foresterFertileBlocks;
    }

    /**
     * @return the foresterInterval
     */
    public int getForesterInterval()
    {
        return foresterInterval;
    }

    /**
     * @return the foresterTrees
     */
    public int getForesterTrees()
    {
        return foresterTrees;
    }

    /**
     * @return the visualizeAdminChunkRadius
     */
    public int getVisualizeAdminChunkRadius()
    {
        return visualizeAdminChunkRadius;
    }

    /**
     * @return the visualizeMarkBlock
     */
    public int getVisualizeMarkBlock()
    {
        return visualizeMarkBlock;
    }

    /**
     * @return the visualizeMarkChunkRadius
     */
    public int getVisualizeMarkChunkRadius()
    {
        return visualizeMarkChunkRadius;
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
     * @return the visualizeBatchSize
     */
    public int getVisualizeBatchSize()
    {
        return visualizeBatchSize;
    }

    /**
     * @return the visualizeBatchDelayTicks
     */
    public int getVisualizeBatchDelayTicks()
    {
        return visualizeBatchDelayTicks;
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
    public ArrayList<LinkedHashMap> getForceFieldBlocks()
    {
        return forceFieldBlocks;
    }

    /**
     * @return the unbreakableBlocks
     */
    public List<Integer> getUnbreakableBlocks()
    {
        return unbreakableBlocks;
    }

    /**
     * @return the bypassBlocks
     */
    public List<Integer> getBypassBlocks()
    {
        return bypassBlocks;
    }

    /**
     * @return the unprotectableBlocks
     */
    public List<Integer> getUnprotectableBlocks()
    {
        return unprotectableBlocks;
    }

    /**
     * @return the toolItems
     */
    public List<Integer> getToolItems()
    {
        return toolItems;
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
     * @return the logEmpty
     */
    public boolean isLogEmpty()
    {
        return logEmpty;
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
     * @return the logBypassUnprotectable
     */
    public boolean isLogBypassUnprotectable()
    {
        return logBypassUnprotectable;
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
     * @return the warnEmpty
     */
    public boolean isWarnEmpty()
    {
        return warnEmpty;
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
     * @return the sneakingBypassesDamage
     */
    public boolean isSneakingBypassesDamage()
    {
        return sneakingBypassesDamage;
    }

    /**
     * @return the allowedCanBreakPstones
     */
    public boolean isAllowedCanBreakPstones()
    {
        return allowedCanBreakPstones;
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
     * @return the chunksInLargestForceFieldArea
     */
    public int getChunksInLargestForceFieldArea()
    {
        return chunksInLargestForceFieldArea;
    }

    /**
     * @return the ffBlocks
     */
    public List<Integer> getFfBlocks()
    {
        return ffBlocks;
    }

    /**
     * @return the throughFields
     */
    public int[] getThroughFields()
    {
        return throughFields;
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
     * @return the fs
     */
    public HashMap<Integer, FieldSettings> getFieldDefinitions()
    {
        return fieldDefinitions;
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
}
