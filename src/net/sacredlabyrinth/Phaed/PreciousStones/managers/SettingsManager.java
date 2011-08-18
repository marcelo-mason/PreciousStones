package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;

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
    public List<String> blacklistedWorlds;
    public int purgeSnitchAfterDays;
    public int purgeAfterDays;
    public int maxSnitchRecords;
    public int saveFrequency;
    public List<Integer> griefUndoBlackList;
    public int griefIntervalSeconds;
    public int griefUndoBatchSize;
    public int griefUndoBatchDelayTicks;
    public List<Integer> foresterFertileBlocks;
    public int foresterInterval;
    public int foresterTrees;
    public int visualizeAdminChunkRadius;
    public int visualizeMarkBlock;
    public int visualizeMarkChunkRadius;
    public int visualizeBlock;
    public int visualizeSeconds;
    public int visualizeBatchSize;
    public int visualizeBatchDelayTicks;
    public boolean visualizeEndOnMove;
    public boolean debug;
    public boolean debugdb;
    public boolean debugsql;
    public ArrayList<LinkedHashMap> forceFieldBlocks;
    public List<Integer> unbreakableBlocks;
    public List<Integer> bypassBlocks;
    public List<Integer> unprotectableBlocks;
    public List<Integer> toolItems;
    public boolean logFire;
    public boolean logEntry;
    public boolean logPlace;
    public boolean logUse;
    public boolean logEmpty;
    public boolean logDestroy;
    public boolean logDestroyArea;
    public boolean logUnprotectable;
    public boolean logPvp;
    public boolean logBypassPvp;
    public boolean logBypassDelete;
    public boolean logBypassPlace;
    public boolean logBypassDestroy;
    public boolean logBypassUnprotectable;
    public boolean logConflictPlace;
    public boolean notifyPlace;
    public boolean notifyDestroy;
    public boolean notifyBypassPvp;
    public boolean notifyBypassPlace;
    public boolean notifyBypassDestroy;
    public boolean notifyBypassUnprotectable;
    public boolean warnInstantHeal;
    public boolean warnSlowHeal;
    public boolean warnSlowDamage;
    public boolean warnFastDamage;
    public boolean warnGiveAir;
    public boolean warnPlace;
    public boolean warnUse;
    public boolean warnEmpty;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnUnprotectable;
    public boolean warnEntry;
    public boolean warnPvp;
    public boolean warnFire;
    public boolean warnLaunch;
    public boolean warnCannon;
    public boolean warnMine;
    public boolean publicBlockDetails;
    public boolean sneakingBypassesDamage;
    public boolean allowedCanBreakPstones;
    public boolean dropOnDelete;
    public boolean disableAlertsForAdmins;
    public boolean disableBypassAlertsForAdmins;
    public boolean offByDefault;
    public int chunksInLargestForceFieldArea;
    public List<Integer> ffBlocks = new ArrayList<Integer>();
    public int[] throughFields = new int[]
    {
        0, 6, 8, 9, 10, 11, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 69, 68, 70, 72, 75, 76, 77, 83, 92, 93, 94
    };
    public int linesPerPage;
    public boolean useMysql;
    public String host;
    public String database;
    public String username;
    public String password;
    private final HashMap<Integer, FieldSettings> fs = new HashMap<Integer, FieldSettings>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public SettingsManager(PreciousStones plugin)
    {
        this.plugin = plugin;
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

        addForceFieldStones(forceFieldBlocks);

        save();
    }

    public void save()
    {
        Configuration config = plugin.getConfiguration();

        config.setProperty("force-field-blocks", forceFieldBlocks);
        config.setProperty("unbreakable-blocks", unbreakableBlocks);
        config.setProperty("bypass-blocks", bypassBlocks);
        config.setProperty("unprotectable-blocks", unprotectableBlocks);
        config.setProperty("tool-items", toolItems);
        config.setProperty("log.fire", logFire);
        config.setProperty("log.entry", logEntry);
        config.setProperty("log.place", logPlace);
        config.setProperty("log.use", logUse);
        config.setProperty("log.pvp", logPvp);
        config.setProperty("log.destroy", logDestroy);
        config.setProperty("log.destroy-area", logDestroyArea);
        config.setProperty("log.unprotectable", logUnprotectable);
        config.setProperty("log.bypass-pvp", logBypassPvp);
        config.setProperty("log.bypass-delete", logBypassDelete);
        config.setProperty("log.bypass-place", logBypassPlace);
        config.setProperty("log.bypass-destroy", logBypassDestroy);
        config.setProperty("log.conflict-place", logConflictPlace);
        config.setProperty("notify.place", notifyPlace);
        config.setProperty("notify.destroy", notifyDestroy);
        config.setProperty("notify.bypass-unprotectable", notifyBypassUnprotectable);
        config.setProperty("notify.bypass-pvp", notifyBypassPvp);
        config.setProperty("notify.bypass-place", notifyBypassPlace);
        config.setProperty("notify.bypass-destroy", notifyBypassDestroy);
        config.setProperty("warn.instant-heal", warnInstantHeal);
        config.setProperty("warn.slow-heal", warnSlowHeal);
        config.setProperty("warn.slow-damage", warnSlowDamage);
        config.setProperty("warn.fast-damage", warnFastDamage);
        config.setProperty("warn.give-air", warnGiveAir);
        config.setProperty("warn.fire", warnFire);
        config.setProperty("warn.entry", warnEntry);
        config.setProperty("warn.place", warnPlace);
        config.setProperty("warn.use", warnUse);
        config.setProperty("warn.pvp", warnPvp);
        config.setProperty("warn.destroy", warnDestroy);
        config.setProperty("warn.destroy-area", warnDestroyArea);
        config.setProperty("warn.unprotectable", warnUnprotectable);
        config.setProperty("warn.launch", warnLaunch);
        config.setProperty("warn.cannon", warnCannon);
        config.setProperty("warn.mine", warnMine);
        config.setProperty("settings.public-block-details", publicBlockDetails);
        config.setProperty("settings.sneaking-bypasses-damage", sneakingBypassesDamage);
        config.setProperty("settings.allowed-can-break-pstones", allowedCanBreakPstones);
        config.setProperty("settings.drop-on-delete", dropOnDelete);
        config.setProperty("settings.disable-alerts-for-admins", disableAlertsForAdmins);
        config.setProperty("settings.disable-bypass-alerts-for-admins", disableBypassAlertsForAdmins);
        config.setProperty("settings.off-by-default", offByDefault);
        config.setProperty("settings.lines-per-page", linesPerPage);
        config.setProperty("settings.blacklisted-worlds", blacklistedWorlds);
        config.setProperty("cleanup.player-inactivity-purge-days", purgeAfterDays);
        config.setProperty("cleanup.snitch-unused-purge-days", purgeSnitchAfterDays);
        config.setProperty("saving.frequency-seconds", saveFrequency);
        config.setProperty("saving.max-records-per-snitch", maxSnitchRecords);
        config.setProperty("visualization.block-type", visualizeBlock);
        config.setProperty("visualization.seconds", visualizeSeconds);
        config.setProperty("visualization.end-on-player-move", visualizeEndOnMove);
        config.setProperty("visualization.admin-chunk-radius", visualizeAdminChunkRadius);
        config.setProperty("visualization.mark-block-type", visualizeMarkBlock);
        config.setProperty("visualization.mark-chunk-radius", visualizeMarkChunkRadius);
        config.setProperty("visualization.batch-size", visualizeBatchSize);
        config.setProperty("visualization.batch-delay-ticks", visualizeBatchDelayTicks);
        config.setProperty("forester.interval-seconds", foresterInterval);
        config.setProperty("forester.fertile-blocks", foresterFertileBlocks);
        config.setProperty("grief-undo.interval-seconds", griefIntervalSeconds);
        config.setProperty("grief-undo.black-list", griefUndoBlackList);
        config.setProperty("grief-undo.batch-size", griefUndoBatchSize);
        config.setProperty("grief-undo.batch-delay-ticks", griefUndoBatchDelayTicks);
        config.setProperty("mysql.enable", useMysql);
        config.setProperty("mysql.host", host);
        config.setProperty("mysql.database", database);
        config.setProperty("mysql.username", username);
        config.setProperty("mysql.password", password);

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
            FieldSettings pstone = new FieldSettings(map);

            if (pstone.isBlockDefined())
            {
                // add stone to our collection
                fs.put(pstone.getBlockId(), pstone);

                // add the values to our reference lists
                ffBlocks.add(pstone.getBlockId());

                // see if the radius is the largest
                if (pstone.getRadius() > largestForceField)
                {
                    largestForceField = pstone.getRadius();
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
        for (FieldSettings setting : fs.values())
        {
            if (setting.isWelcomeMessage() || setting.isFarewellMessage())
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
        for (FieldSettings setting : fs.values())
        {
            if (setting.isCannon() || setting.isLaunch())
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
        for (FieldSettings setting : fs.values())
        {
            if (setting.isSnitch())
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
        return blacklistedWorlds.contains(world.getName());
    }

    /**
     * Check if a type is one of the unprotectable types
     * @param placedblock
     * @return
     */
    public boolean isUnprotectableType(int type)
    {
        return unprotectableBlocks.contains(type);
    }

    /**
     * Check if the id is one of forrester fertile types
     * @param block
     * @return
     */
    public boolean isFertileType(int id)
    {
        return foresterFertileBlocks.contains(id);
    }

    /**
     * Check if the id is one of grief undo blacklisted types
     * @param block
     * @return
     */
    public boolean isGriefUndoBlackListType(int id)
    {
        return griefUndoBlackList.contains(id);
    }

    /**
     * Check if a type is a see through block
     * @param block
     * @return
     */
    public boolean isThroughType(int type)
    {
        for(int i = 0; i < throughFields.length; i++)
        {
            if(throughFields[i] == type)
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
        return toolItems.contains(block.getTypeId());
    }

    /**
     * Check if a block is one of the tool item types
     * @param typeId
     * @return
     */
    public boolean isToolItemType(int typeId)
    {
        return toolItems.contains(typeId);
    }

    /**
     * Check if a block is one of the tool item types
     * @param type
     * @return
     */
    public boolean isToolItemType(String type)
    {
        return toolItems.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the snitch types
     * @param block
     * @return
     */
    public boolean isSnitchType(Block block)
    {
        for (FieldSettings setting : fs.values())
        {
            if (setting.isSnitch() && setting.getBlockId() == block.getTypeId())
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
        return unbreakableBlocks.contains(unbreakableblock.getTypeId());
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param typeId
     * @return
     */
    public boolean isUnbreakableType(int typeId)
    {
        return unbreakableBlocks.contains(typeId);
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param type
     * @return
     */
    public boolean isUnbreakableType(String type)
    {
        return unbreakableBlocks.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the forcefeld types
     * @param block
     * @return
     */
    public boolean isFieldType(Block block)
    {
        return ffBlocks.contains(block.getTypeId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param type
     * @return
     */
    public boolean isFieldType(String type)
    {
        return ffBlocks.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if the material is one of the forcefeld types
     * @param material
     * @return
     */
    public boolean isFieldType(Material material)
    {
        return ffBlocks.contains(material.getId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param typeId
     * @return
     */
    public boolean isFieldType(int typeId)
    {
        return ffBlocks.contains(typeId);
    }

    /**
     * Whetehr the block is a bypass type
     * @param block
     * @return
     */
    public boolean isBypassBlock(Block block)
    {
        return bypassBlocks.contains(block.getTypeId());
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
        return fs.get(typeId);
    }

    /**
     * Returns all the field settings
     * @return
     */
    public HashMap<Integer, FieldSettings> getFieldSettings()
    {
        return fs;
    }
}
