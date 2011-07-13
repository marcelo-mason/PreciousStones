package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.data.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import net.sacredlabyrinth.Phaed.PreciousStones.data.mysqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.data.sqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.block.Sign;
import org.bukkit.block.BlockFace;

/**
 *
 * @author phaed
 */
public final class StorageManager
{
    /**
     *
     */
    public DBCore core;
    private Mutex mutex = new Mutex();
    private PreciousStones plugin;
    private Map<Vec, Field> pending = new ConcurrentHashMap<Vec, Field>();

    /**
     *
     * @param plugin
     */
    public StorageManager(PreciousStones plugin)
    {
        this.plugin = plugin;

        initiateDB();
        loadWorldData();
        saverScheduler();
    }

    /**
     * Initiates the db
     */
    public void initiateDB()
    {
        if (plugin.settings.useMysql)
        {
            core = new mysqlCore(PreciousStones.logger, plugin.settings.host, plugin.settings.database, plugin.settings.username, plugin.settings.password);
            core.initialize();

            if (core.checkConnection())
            {
                PreciousStones.log(Level.INFO, "MySQL Connection successful");

                if (!core.checkTable("pstone_fields"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_fields");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(255) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(25) NOT NULL,  `name` varchar(25) NOT NULL,  `packed_allowed` text NOT NULL,  `packed_snitch_list` longtext NOT NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_fields_1` (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(255) default NULL,  `owner` varchar(255) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_unbreakables_1` (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_grief_undo"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_grief_undo");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20) NOT NULL auto_increment,  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(50) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));";
                    core.createTable(query);
                }
            }
            else
            {
                PreciousStones.log(Level.INFO, "MySQL Connection failed");
            }
        }
        else
        {
            core = new sqlCore(PreciousStones.logger, "PreciousStones", plugin.getDataFolder().getPath());
            core.initialize();

            if (core.checkConnection())
            {
                PreciousStones.log(Level.INFO, "SQLite Connection successful");

                if (!core.checkTable("pstone_fields"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_fields");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(255) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(25) NOT NULL,  `name` varchar(25) NOT NULL,  `packed_allowed` text NOT NULL,  `packed_snitch_list` longtext NOT NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(255) default NULL,  `owner` varchar(255) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_grief_undo"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_grief_undo");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20),  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(50) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));";
                    core.createTable(query);
                }
            }
            else
            {
                PreciousStones.log(Level.INFO, "SQLite Connection failed");
            }
        }

    }

    /**
     * Load pstones for any world that is loaded
     */
    public void loadWorldData()
    {
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds)
        {
            loadWorldFields(world.getName());
            loadWorldUnbreakables(world.getName());
        }
    }

    /**
     * Loads all fields for a specific world into memory
     * @param world the world to load
     */
    public void loadWorldFields(String world)
    {
        List<Field> fields = getFields(world);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                continue;
            }

            plugin.ffm.addToCollection(field);

            if (fieldsettings.griefUndoInterval)
            {
                plugin.gum.add(field);
            }
        }

        if (fields.size() > 0)
        {
            PreciousStones.log(Level.INFO, "{0} fields: {1}", world, fields.size());
        }
    }

    /**
     * Loads all unbreakables for a specific world into memory
     * @param world
     */
    public void loadWorldUnbreakables(String world)
    {
        List<Unbreakable> unbreakables = getUnbreakables(world);

        for (Unbreakable ub : unbreakables)
        {
            plugin.um.addToCollection(ub);
        }

        if (unbreakables.size() > 0)
        {
            PreciousStones.log(Level.INFO, "{0} unbreakables: {1}", world, unbreakables.size());
        }
    }

    /**
     * Retrieves all fields belonging to a worlds from the database
     * @param worldName
     * @return
     */
    public List<Field> getFields(String worldName)
    {
        List<Field> out = new ArrayList<Field>();

        String query = "SELECT * FROM  `pstone_fields` WHERE world = '" + worldName + "';";
        ResultSet res = core.sqlQuery(query);

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        long id = res.getLong("id");
                        int x = res.getInt("x");
                        int y = res.getInt("y");
                        int z = res.getInt("z");
                        int radius = res.getInt("radius");
                        int height = res.getInt("height");
                        int type_id = res.getInt("type_id");
                        float velocity = res.getFloat("velocity");
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        String name = res.getString("name");
                        String packed_allowed = res.getString("packed_allowed");
                        String packed_snitch_list = res.getString("packed_snitch_list");

                        Field field = new Field(x, y, z, radius, height, velocity, world, type_id, owner, name);
                        field.setPackedAllowed(packed_allowed);
                        field.setPackedSnitchList(packed_snitch_list, plugin.settings.maxSnitchRecords);

                        out.add(field);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return out;
    }

    /**
     * Retrieves all unbreakables belonging to a worlds from the database
     * @param worldName
     * @return
     */
    public List<Unbreakable> getUnbreakables(String worldName)
    {
        List<Unbreakable> out = new ArrayList<Unbreakable>();

        String query = "SELECT * FROM  `pstone_unbreakables` WHERE world = '" + worldName + "';";
        ResultSet res = core.sqlQuery(query);

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        long id = res.getLong("id");
                        int x = res.getInt("x");
                        int y = res.getInt("y");
                        int z = res.getInt("z");
                        int type_id = res.getInt("type_id");
                        String world = res.getString("world");
                        String owner = res.getString("owner");

                        Unbreakable ub = new Unbreakable(x, y, z, world, type_id, owner);

                        out.add(ub);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return out;
    }

    /**
     * Puts the field up for future storage
     * @param field
     */
    public void offerField(Field field)
    {
        try
        {
            mutex.acquire();
            pending.put(field.toVec(), field);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            mutex.release();
        }
    }

    private void processField(Field field)
    {
        if (field.isDirty(Field.Dirty.DELETE))
        {
            deleteField(field);
            return;
        }

        String subQuery = "";

        if (field.isDirty(Field.Dirty.OWNER))
        {
            subQuery += "owner = " + field.getOwner() + ", ";
        }

        if (field.isDirty(Field.Dirty.RADIUS))
        {
            subQuery += "radius = " + field.getRadius() + ", ";
        }

        if (field.isDirty(Field.Dirty.HEIGHT))
        {
            subQuery += "height = " + field.getHeight() + ", ";
        }

        if (field.isDirty(Field.Dirty.VELOCITY))
        {
            subQuery += "velocity = " + field.getVelocity() + ", ";
        }

        if (field.isDirty(Field.Dirty.NAME))
        {
            subQuery += "name = '" + Helper.escapeQuotes(field.getName()) + "', ";
        }

        if (field.isDirty(Field.Dirty.ALLOWED))
        {
            subQuery += "packed_allowed = '" + Helper.escapeQuotes(field.getPackedAllowed()) + "', ";
        }

        if (field.isDirty(Field.Dirty.SNITCH))
        {
            subQuery += "packed_snitch_list = '" + Helper.escapeQuotes(field.getPackedSnitchList()) + "', ";
        }

        if (field.isDirty(Field.Dirty.GRIEF_BLOCKS))
        {
            List<GriefBlock> grief = field.getGrief();

            for (GriefBlock gb : grief)
            {
                recordBlockGrief(field, gb);
            }
        }

        if (!subQuery.isEmpty())
        {
            String query = "UPDATE `pstone_fields` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";

            if (plugin.settings.debug)
            {
                PreciousStones.logger.info(query);
            }
            core.addBatch(query);
        }


        field.clearDirty();
    }

    /**
     * Insert a field into the database
     * @param field
     */
    public void insertField(Field field)
    {
        String query = "INSERT INTO `pstone_fields` (  `x`,  `y`, `z`, `world`, `radius`, `height`, `velocity`, `type_id`, `owner`, `name`, `packed_allowed`, `packed_snitch_list`) ";
        String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + Helper.escapeQuotes(field.getPackedSnitchList()) + "');";

        if (plugin.settings.debug)
        {
            PreciousStones.logger.info(query + values);
        }
        core.insertQuery(query + values);
    }

    /**
     * Delete a fields form the database
     * @param field
     */
    public void deleteField(Field field)
    {
        String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        core.addBatch(query);
    }

    /**
     * Insert an unbreakable into the database
     * @param ub
     */
    public void insertUnbreakable(Unbreakable ub)
    {
        String query = "INSERT INTO `pstone_unbreakables` (  `x`,  `y`, `z`, `world`, `owner`, `type_id`) ";
        String values = "VALUES ( " + ub.getX() + "," + ub.getY() + "," + ub.getZ() + ",'" + ub.getWorld() + "','" + ub.getOwner() + "'," + ub.getTypeId() + ");";
        core.addBatch(query + values);
    }

    /**
     * Delete an unbreakabale form the database
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE x = " + ub.getX() + " AND y = " + ub.getY() + " AND z = " + ub.getZ() + " AND world = '" + ub.getWorld() + "';";
        core.addBatch(query);
    }

    /**
     * Record a single block grief
     * @param field
     * @param block
     */
    public void recordBlockGrief(Field field, GriefBlock gb)
    {
        World world = plugin.getServer().getWorld(gb.getWorld());

        if (world != null)
        {
            return;
        }

        Block block = world.getBlockAt(gb.toLocation(world));

        if (!plugin.gum.isDependentBlock(block.getTypeId()))
        {
            BlockFace[] faces =
            {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
            };

            for (BlockFace face : faces)
            {
                Block rel = block.getRelative(face);

                if (plugin.gum.isDependentBlock(rel.getTypeId()))
                {
                    recordBlockGrief(field, new GriefBlock(rel.getLocation(), rel.getTypeId(), rel.getData()));
                    rel.setTypeId(0);
                }
            }
        }

        if (block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR))
        {
            // record wood doors in correct order

            if ((block.getData() & 0x8) == 0x8)
            {
                Block bottom = block.getRelative(BlockFace.DOWN);

                recordBlockGriefClean(field, bottom);
                recordBlockGriefClean(field, block);

                bottom.setTypeId(0);
                block.setTypeId(0);
            }
            else
            {
                Block top = block.getRelative(BlockFace.UP);

                recordBlockGriefClean(field, block);
                recordBlockGriefClean(field, top);

                block.setTypeId(0);
                top.setTypeId(0);
            }
        }
        else
        {
            recordBlockGriefClean(field, block);
        }
    }

    /**
     * Record a single block grief
     * @param field
     * @param block
     */
    public void recordBlockGriefClean(Field field, Block block)
    {
        String signText = "";

        if (block.getState() instanceof Sign)
        {
            Sign sign = (Sign) block.getState();

            for (String line : sign.getLines())
            {
                signText += line + "°";
            }

            signText = Helper.stripTrailing(signText, "°");
        }

        String query = "INSERT INTO `pstone_grief_undo` ( `date_griefed`, `field_x`, `field_y` , `field_z`, `world`, `x` , `y`, `z`, `type_id`, `data`, `sign_text`) ";
        String values = "VALUES ( '" + new Timestamp((new Date()).getTime()) + "'," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + block.getX() + "," + block.getY() + "," + block.getZ() + "," + block.getTypeId() + "," + block.getData() + ",'" + Helper.escapeQuotes(signText) + "');";
        core.addBatch(query + values);
    }

    /**
     * Restores a field's griefed blocks
     * @param field
     * @return
     */
    public List<GriefBlock> retrieveBlockGrief(Field field)
    {
        processQueue(Field.Dirty.GRIEF_BLOCKS);

        List<GriefBlock> out = new ArrayList<GriefBlock>();

        String query = "SELECT * FROM  `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "' ORDER BY y ASC;";
        ResultSet res = core.sqlQuery(query);

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        int x = res.getInt("x");
                        int y = res.getInt("y");
                        int z = res.getInt("z");
                        int type_id = res.getInt("type_id");
                        byte data = res.getByte("data");
                        String signText = res.getString("sign_text");

                        GriefBlock bg = new GriefBlock(x, y, z, field.getWorld(), type_id, data);

                        bg.setSignText(signText);
                        out.add(bg);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        deleteBlockGrief(field);
        return out;
    }

    /**
     * Deletes all records form a specific field
     * @param field
     */
    public void deleteBlockGrief(Field field)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        core.addBatch(query);
    }

    /**
     * Deletes all records form a specific block
     * @param block
     */
    public void deleteBlockGrief(Block block)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE x = " + block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND world = '" + block.getWorld().getName() + "';";
        core.deleteQuery(query);
    }

    /**
     * Schedules the pending queue on save frequency
     */
    public void saverScheduler()
    {
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                processQueue(null);
            }
        }, 0, 20L * plugin.settings.saveFrequency);
    }

    /**
     * Process entire queue
     */
    public void processQueue(Field.Dirty dirtyType)
    {
        try
        {
            mutex.acquire();

            if (plugin.settings.debug && !pending.isEmpty())
            {
                PreciousStones.logger.info("[Queue] processing " + pending.size() + " queries...");
            }

            core.openBatch();

            int count = 0;

            for (Field field : pending.values())
            {
                if (dirtyType != null)
                {
                    if (!field.isDirty(dirtyType.GRIEF_BLOCKS))
                    {
                        continue;
                    }
                }

                processField(field);

                if (count >= plugin.settings.saveBatchSize)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count);
                    core.closeBatch();
                    core.openBatch();
                    count = 0;
                }
                count++;
            }

            pending.clear();

            core.closeBatch();

            if (plugin.settings.debug && !pending.isEmpty())
            {
                PreciousStones.logger.info("[Queue] done");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            mutex.release();
        }
    }
}