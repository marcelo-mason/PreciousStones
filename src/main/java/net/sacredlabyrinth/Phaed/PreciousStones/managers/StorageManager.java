package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.MySQLCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.SQLiteCore;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author phaed
 */
public class StorageManager
{
    /**
     *
     */
    private DBCore core;
    private PreciousStones plugin;
    private final Map<Vec, Field> pending = new HashMap<Vec, Field>();
    private final Set<Field> pendingGrief = new HashSet<Field>();
    private final Map<Unbreakable, Boolean> pendingUb = new HashMap<Unbreakable, Boolean>();
    private final Map<String, Boolean> pendingPlayers = new HashMap<String, Boolean>();
    private final List<SnitchEntry> pendingSnitchEntries = new ArrayList<SnitchEntry>();

    /**
     *
     */
    public StorageManager()
    {
        plugin = PreciousStones.getInstance();

        initiateDB();
        loadWorldData();
        saverScheduler();
    }

    private void initiateDB()
    {
        if (plugin.getSettingsManager().isUseMysql())
        {
            core = new MySQLCore(plugin.getSettingsManager().getHost(), plugin.getSettingsManager().getPort(), plugin.getSettingsManager().getDatabase(), plugin.getSettingsManager().getUsername(), plugin.getSettingsManager().getPassword());

            if (core.checkConnection())
            {
                PreciousStones.log("dbMysqlConnected");

                if (!core.existsTable("pstone_cuboids"))
                {
                    PreciousStones.log("Creating table: pstone_cuboids");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_cuboids` (  `id` bigint(20) NOT NULL auto_increment, `parent` bigint(20) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL,  `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL, `data` tinyint default 0,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_cuboid_fields_1`  (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_fields"))
                {
                    PreciousStones.log("Creating table: pstone_fields");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `data` tinyint default 0, `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_fields_1` (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_unbreakables"))
                {
                    PreciousStones.log("Creating table: pstone_unbreakables");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL,  `data` tinyint default 0, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_unbreakables_1` (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_grief_undo"))
                {
                    PreciousStones.log("Creating table: pstone_grief_undo");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20) NOT NULL auto_increment,  `date_griefed` bigint(20), `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));");
                }

                if (!core.existsTable("pstone_translocations"))
                {
                    PreciousStones.log("Creating table: pstone_translocations");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_translocations` (  `id` bigint(20) NOT NULL auto_increment,  `name` varchar(36) NOT NULL, `player_name` varchar(16) NOT NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL, `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_trans_1` (`name`,`player_name`));");
                }

                if (!core.existsTable("pstone_storedblocks"))
                {
                    PreciousStones.log("Creating table: pstone_storedblocks");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_storedblocks` (  `id` bigint(20) NOT NULL auto_increment, `name` varchar(36) NOT NULL, `player_name` varchar(16) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `world` varchar(25) NOT NULL, `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, `applied` bit default 0, `contents` TEXT NOT NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_trans_2` (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_players"))
                {
                    PreciousStones.log("Creating table: pstone_players");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, flags TEXT default NULL, PRIMARY KEY  (`player_name`));");
                    touchAllPlayers();
                }

                if (!core.existsTable("pstone_snitches"))
                {
                    PreciousStones.log("Creating table: pstone_snitches");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, `date` varchar(25) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));");
                }
            }
            else
            {
                PreciousStones.log("dbMysqlFailed");
            }
        }
        else
        {
            core = new SQLiteCore("PreciousStones", plugin.getDataFolder().getPath());

            if (core.checkConnection())
            {
                PreciousStones.log("dbSqliteConnected");

                if (!core.existsTable("pstone_cuboids"))
                {
                    PreciousStones.log("Creating table: pstone_cuboids");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_cuboids` (  `id` INTEGER PRIMARY KEY,  `parent` bigint(20) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL,  `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `data` tinyint default 0, `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, UNIQUE (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_fields"))
                {
                    PreciousStones.log("Creating table: pstone_fields");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` INTEGER PRIMARY KEY, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `data` tinyint default 0, `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, UNIQUE (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_unbreakables"))
                {
                    PreciousStones.log("Creating table: pstone_unbreakables");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` INTEGER PRIMARY KEY, `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL,`data` tinyint default 0, UNIQUE (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_grief_undo"))
                {
                    PreciousStones.log("Creating table: pstone_grief_undo");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` INTEGER PRIMARY KEY,  `date_griefed` bigint(20), `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL);");
                }

                if (!core.existsTable("pstone_translocations"))
                {
                    PreciousStones.log("Creating table: pstone_translocations");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_translocations` (  `id` INTEGER PRIMARY KEY,  `name` varchar(36) NOT NULL, `player_name` varchar(16) NOT NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL, `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL, UNIQUE (`name`,`player_name`));");
                }

                if (!core.existsTable("pstone_storedblocks"))
                {
                    PreciousStones.log("Creating table: pstone_storedblocks");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_storedblocks` (  `id` INTEGER PRIMARY KEY,  `name` varchar(36) NOT NULL, `player_name` varchar(16) NOT NULL,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `world` varchar(25) NOT NULL, `type_id` int(11) NOT NULL, `data` TINYINT NOT NULL, `sign_text` varchar(75) NOT NULL, `applied` bit default 0, `contents` TEXT NOT NULL, UNIQUE (`x`,`y`,`z`,`world`));");
                }

                if (!core.existsTable("pstone_players"))
                {
                    PreciousStones.log("Creating table: pstone_players");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, flags TEXT default NULL, PRIMARY KEY (`player_name`));");
                    touchAllPlayers();
                }

                if (!core.existsTable("pstone_snitches"))
                {
                    PreciousStones.log("Creating table: pstone_snitches");

                    core.execute("CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, `date` varchar(25) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));");
                }
            }
            else
            {
                PreciousStones.log("dbSqliteFailed");
            }
        }

        if (plugin.getSettingsManager().getVersion() < 9)
        {
            addData();
            plugin.getSettingsManager().setVersion(9);
        }

        if (plugin.getSettingsManager().getVersion() < 10)
        {
            addSnitchDate();
            plugin.getSettingsManager().setVersion(10);
        }

        if (plugin.getSettingsManager().isUseMysql())
        {
            if (plugin.getSettingsManager().getVersion() < 12)
            {
                resetLastSeem();
                plugin.getSettingsManager().setVersion(12);
            }
        }
    }

    private void resetLastSeem()
    {
        PreciousStones.log("Updating last seen dates to new time format");

        if (!core.getDataType("pstone_grief_undo", "date_griefed").equals("bigint"))
        {
            core.execute("alter table pstone_grief_undo modify date_griefed bigint");
            core.execute("update pstone_grief_undo date_griefed = " + (new DateTime()).getMillis());
        }

        if (!core.getDataType("pstone_fields", "last_used").equals("bigint"))
        {
            core.execute("alter table pstone_fields modify last_used bigint");
            core.execute("update pstone_fields last_used = " + (new DateTime()).getMillis());
        }

        if (!core.getDataType("pstone_cuboids", "last_used").equals("bigint"))
        {
            core.execute("alter table pstone_cuboids modify last_used bigint");
            core.execute("update pstone_cuboids last_used = " + (new DateTime()).getMillis());
        }

        if (!core.getDataType("pstone_players", "last_seen").equals("bigint"))
        {
            core.execute("alter table pstone_players modify last_seen bigint");
            core.execute("update pstone_players last_seen = " + (new DateTime()).getMillis());
        }
    }

    private void addData()
    {
        if (!core.getDataType("pstone_fields", "data").equals("tinyint"))
        {
            core.execute("alter table pstone_fields add column data tinyint default 0");
        }

        if (!core.getDataType("pstone_cuboids", "data").equals("tinyint"))
        {
            core.execute("alter table pstone_cuboids add column data tinyint default 0");
        }

        if (!core.getDataType("pstone_unbreakables", "data").equals("tinyint"))
        {
            core.execute("alter table pstone_unbreakables add column data tinyint default 0");
        }
    }

    private void addSnitchDate()
    {
        if (!core.getDataType("pstone_snitches", "date").equals("varchar"))
        {
            core.execute("alter table pstone_snitches add column date varchar(25) default NULL");
        }
    }

    /**
     * Closes DB connection
     */
    public void closeConnection()
    {
        core.close();
    }

    /**
     * Load all pstones for any world that is loaded
     */
    public void loadWorldData()
    {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                plugin.getForceFieldManager().doFinalize();
                processQueue();

                plugin.getForceFieldManager().clearChunkLists();
                plugin.getUnbreakableManager().clearChunkLists();

                List<World> worlds = plugin.getServer().getWorlds();
                Map<String, World> filtered = new HashMap<String, World>();

                for (World world : worlds)
                {
                    filtered.put(world.getName(), world);
                }

                for (World world : filtered.values())
                {
                    loadWorldFields(world.getName());
                    loadWorldUnbreakables(world.getName());
                }
            }
        }, 0);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                extractPlayers();
            }
        }, 20);
    }

    /**
     * Loads all fields for a specific world into memory
     *
     * @param world the world to load
     */
    public void loadWorldFields(String world)
    {
        int fieldCount;
        int cuboidCount;

        List<Field> fields;

        synchronized (this)
        {
            fields = getFields(world);

            fieldCount = fields.size();

            Collection<Field> cuboids = getCuboidFields(world);

            cuboidCount = cuboids.size();

            fields.addAll(cuboids);
        }

        for (Field field : fields)
        {
            // add to collection

            plugin.getForceFieldManager().addToCollection(field);

            // register grief reverts

            if (field.hasFlag(FieldFlag.GRIEF_REVERT) && field.getRevertSecs() > 0)
            {
                plugin.getGriefUndoManager().register(field);
            }

            // set the initial applied status to the field form the database

            if (field.hasFlag(FieldFlag.TRANSLOCATION))
            {
                if (field.isNamed())
                {
                    boolean applied = isTranslocationApplied(field.getName(), field.getOwner());
                    field.setDisabled(!applied);

                    int count = totalTranslocationCount(field.getName(), field.getOwner());
                    field.setTranslocationSize(count);
                }
            }

            // start renter scheduler

            if (field.hasFlag(FieldFlag.RENTABLE) || field.hasFlag(FieldFlag.SHAREABLE))
            {
                field.scheduleNextRentUpdate();
            }
        }

        if (fieldCount > 0)
        {
            PreciousStones.log("countsFields", world, fieldCount);
        }

        if (cuboidCount > 0)
        {
            PreciousStones.log("countsCuboids", world, cuboidCount);
        }
    }

    public int enableAllFlags(String flagStr)
    {
        int changed = 0;
        List<Field> fields = new ArrayList<Field>();

        synchronized (this)
        {
            List<World> worlds = plugin.getServer().getWorlds();

            for (World world : worlds)
            {
                fields.addAll(getFields(world.getName()));
                fields.addAll(getCuboidFields(world.getName()));
            }
        }

        plugin.getForceFieldManager().clearChunkLists();

        for (Field field : fields)
        {
            if (field.hasFlag(flagStr))
            {
                changed++;
                field.disableFlag(flagStr);
                plugin.getStorageManager().offerField(field);
            }

            plugin.getForceFieldManager().addToCollection(field);
        }

        return changed;
    }

    public int disableAllFlags(String flagStr)
    {
        int changed = 0;
        List<Field> fields = new ArrayList<Field>();

        synchronized (this)
        {
            List<World> worlds = plugin.getServer().getWorlds();

            for (World world : worlds)
            {
                fields.addAll(getFields(world.getName()));
                fields.addAll(getCuboidFields(world.getName()));
            }
        }

        plugin.getForceFieldManager().clearChunkLists();

        for (Field field : fields)
        {
            if (field.hasDisabledFlag(flagStr))
            {
                changed++;
                field.enableFlag(flagStr);
                field.dirtyFlags();
            }

            plugin.getForceFieldManager().addToCollection(field);
        }

        return changed;
    }

    /**
     * Loads all unbreakables for a specific world into memory
     *
     * @param world
     */
    public void loadWorldUnbreakables(String world)
    {
        List<Unbreakable> unbreakables;

        synchronized (this)
        {
            unbreakables = getUnbreakables(world);
        }

        if (unbreakables != null)
        {
            for (Unbreakable ub : unbreakables)
            {
                plugin.getUnbreakableManager().addToCollection(ub);
            }

            if (unbreakables.size() > 0)
            {
                PreciousStones.log("countsUnbreakables", world, unbreakables.size());
            }
        }
    }

    /**
     * Puts the field up for future storage
     *
     * @param field
     */
    public void offerField(Field field)
    {
        synchronized (pending)
        {
            pending.put(field.toVec(), field);
        }
    }

    /**
     * Puts the field up for grief reversion
     *
     * @param field
     */
    public void offerGrief(Field field)
    {
        synchronized (pendingGrief)
        {
            pendingGrief.add(field);
        }
    }


    /**
     * Puts the unbreakable up for future storage
     *
     * @param ub
     * @param insert
     */
    public void offerUnbreakable(Unbreakable ub, boolean insert)
    {
        synchronized (pendingUb)
        {
            pendingUb.put(ub, insert);
        }
    }

    /**
     * Puts the player up for future storage
     *
     * @param playerName
     */
    public void offerPlayer(String playerName)
    {
        synchronized (pendingPlayers)
        {
            pendingPlayers.put(playerName, true);
        }
    }

    /**
     * Puts the player up for future storage
     *
     * @param playerName
     */
    public void offerDeletePlayer(String playerName)
    {
        synchronized (pendingPlayers)
        {
            pendingPlayers.put(playerName, false);
        }
    }


    /**
     * Puts the snitch list up for future storage
     *
     * @param se
     */
    public void offerSnitchEntry(SnitchEntry se)
    {
        synchronized (pendingSnitchEntries)
        {
            pendingSnitchEntries.add(se);
        }
    }

    /**
     * Retrieves all fields belonging to a world from the database
     *
     * @param worldName
     * @return
     */
    public List<Field> getFields(String worldName)
    {
        List<Field> out = new ArrayList<Field>();
        boolean foundInWrongTable = false;
        int purged = 0;

        String query = "SELECT pstone_fields.id as id, x, y, z, radius, height, type_id, data, velocity, world, owner, name, packed_allowed, last_used, flags FROM pstone_fields WHERE world = '" + Helper.escapeQuotes(worldName) + "';";

        ResultSet res = core.select(query);

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
                        byte data = res.getByte("data");
                        float velocity = res.getFloat("velocity");
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        String name = res.getString("name");
                        String flags = res.getString("flags");
                        String packed_allowed = res.getString("packed_allowed");
                        long last_used = res.getLong("last_used");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        Field field = new Field(x, y, z, radius, height, velocity, world, type, owner, name, last_used);
                        field.setPackedAllowed(packed_allowed);
                        field.setId(id);

                        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

                        if (fs != null)
                        {
                            field.setSettings(fs);
                            field.setFlags(flags);

                            if (fs.getAutoDisableSeconds() > 0)
                            {
                                field.setDisabled(true);
                            }

                            out.add(field);

                            // check for fields in the wrong table

                            if (fs.hasDefaultFlag(FieldFlag.CUBOID))
                            {
                                deleteFieldFromBothTables(field);
                                insertField(field);
                                foundInWrongTable = true;
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        System.out.print(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                System.out.print(ex.getMessage());
                ex.printStackTrace();
            }
        }

        if (foundInWrongTable)
        {
            System.out.print("[PreciousStones] Fields found in wrong table, moving...");
        }

        if (purged > 0)
        {
            PreciousStones.log("countsPurgedSnitches", worldName, purged);
        }
        return out;
    }

    /**
     * Retrieves all of the cuboid fields belonging to a world from the database
     *
     * @param worldName
     * @return
     */
    public Collection<Field> getCuboidFields(String worldName)
    {
        HashMap<Long, Field> out = new HashMap<Long, Field>();
        int purged = 0;
        boolean foundInWrongTable = false;

        String query = "SELECT pstone_cuboids.id as id, x, y, z, minx, miny, minz, maxx, maxy, maxz, type_id, data, velocity, world, owner, name, packed_allowed, last_used, flags  FROM  pstone_cuboids WHERE pstone_cuboids.parent = 0 AND world = '" + Helper.escapeQuotes(worldName) + "';";

        ResultSet res = core.select(query);

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
                        int minx = res.getInt("minx");
                        int miny = res.getInt("miny");
                        int minz = res.getInt("minz");
                        int maxx = res.getInt("maxx");
                        int maxy = res.getInt("maxy");
                        int maxz = res.getInt("maxz");
                        int type_id = res.getInt("type_id");
                        byte data = res.getByte("data");
                        float velocity = res.getFloat("velocity");
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        String name = res.getString("name");
                        String flags = res.getString("flags");
                        String packed_allowed = res.getString("packed_allowed");
                        long last_used = res.getLong("last_used");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        Field field = new Field(x, y, z, minx, miny, minz, maxx, maxy, maxz, velocity, world, type, owner, name, last_used);
                        field.setPackedAllowed(packed_allowed);
                        field.setId(id);

                        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

                        if (fs != null)
                        {
                            field.setSettings(fs);
                            field.setFlags(flags);

                            if (fs.getAutoDisableSeconds() > 0)
                            {
                                field.setDisabled(true);
                            }

                            out.put(id, field);

                            // check for fields in the wrong table

                            if (!fs.hasDefaultFlag(FieldFlag.CUBOID))
                            {
                                deleteFieldFromBothTables(field);
                                insertField(field);
                                foundInWrongTable = true;
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        System.out.print(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                System.out.print(ex.getMessage());
                ex.printStackTrace();
            }
        }

        query = "SELECT pstone_cuboids.id as id, parent, x, y, z, minx, miny, minz, maxx, maxy, maxz, type_id, data, velocity, world, owner, name, packed_allowed, last_used, flags FROM  pstone_cuboids WHERE pstone_cuboids.parent > 0 AND world = '" + Helper.escapeQuotes(worldName) + "';";

        res = core.select(query);

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        long id = res.getLong("id");
                        long parent = res.getLong("parent");
                        int x = res.getInt("x");
                        int y = res.getInt("y");
                        int z = res.getInt("z");
                        int minx = res.getInt("minx");
                        int miny = res.getInt("miny");
                        int minz = res.getInt("minz");
                        int maxx = res.getInt("maxx");
                        int maxy = res.getInt("maxy");
                        int maxz = res.getInt("maxz");
                        int type_id = res.getInt("type_id");
                        byte data = res.getByte("data");
                        float velocity = res.getFloat("velocity");
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        String name = res.getString("name");
                        String flags = res.getString("flags");
                        String packed_allowed = res.getString("packed_allowed");
                        long last_used = res.getLong("last_used");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        Field field = new Field(x, y, z, minx, miny, minz, maxx, maxy, maxz, velocity, world, type, owner, name, last_used);
                        field.setPackedAllowed(packed_allowed);

                        Field parentField = out.get(parent);

                        if (parentField != null)
                        {
                            field.setParent(parentField);
                            parentField.addChild(field);
                        }
                        else
                        {
                            field.markForDeletion();
                            offerField(field);
                        }

                        field.setId(id);

                        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

                        if (fs != null)
                        {
                            field.setSettings(fs);
                            field.setFlags(flags);
                            out.put(id, field);
                        }
                    }
                    catch (Exception ex)
                    {
                        System.out.print(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
            catch (SQLException ex)
            {
                System.out.print(ex.getMessage());
                ex.printStackTrace();
            }
        }

        if (foundInWrongTable)
        {
            PreciousStones.log("fieldsInWrongTable");
        }

        if (purged > 0)
        {
            PreciousStones.log("countsPurgedSnitches", worldName, purged);
        }

        return out.values();
    }

    /**
     * Retrieves all players from the database
     */
    public void extractPlayers()
    {
        String query = "SELECT * FROM pstone_players;";
        ResultSet res = core.select(query);

        Set<OfflinePlayer> bannedPlayers = Bukkit.getBannedPlayers();
        List<String> banned = new ArrayList<String>();

        if (plugin.getSettingsManager().isPurgeBannedPlayers())
        {
            for (OfflinePlayer player : bannedPlayers)
            {
                banned.add(player.getName());
            }
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        String name = res.getString("player_name");
                        long last_seen = res.getLong("last_seen");
                        String flags = res.getString("flags");

                        if (last_seen > 0)
                        {
                            int lastSeenDays = Days.daysBetween(new DateTime(last_seen), new DateTime()).getDays();

                            PreciousStones.debug("Player last seen: %s [%s]", lastSeenDays, name);

                            if (banned.contains(name) || lastSeenDays >= plugin.getSettingsManager().getPurgeAfterDays())
                            {
                                PreciousStones.debug("PURGED %s", name);

                                int purged = plugin.getForceFieldManager().deleteBelonging(name);

                                if (purged > 0)
                                {
                                    PreciousStones.log("countsPurgedFields", name, purged);
                                }

                                purged = plugin.getUnbreakableManager().deleteBelonging(name);

                                if (purged > 0)
                                {
                                    PreciousStones.log("countsPurgedUnbreakables", name, purged);
                                }

                                offerDeletePlayer(name);
                                continue;
                            }
                        }

                        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(name);
                        data.setFlags(flags);
                    }
                    catch (Exception ex)
                    {
                        //PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                System.out.print(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Retrieves all unbreakables belonging to a worlds from the database
     *
     * @param worldName
     * @return
     */
    public List<Unbreakable> getUnbreakables(String worldName)
    {
        List<Unbreakable> out = new ArrayList<Unbreakable>();
        int purged = 0;

        String query = "SELECT * FROM  `pstone_unbreakables` WHERE world = '" + Helper.escapeQuotes(worldName) + "';";

        ResultSet res = core.select(query);

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
                        String world = res.getString("world");
                        String owner = res.getString("owner");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        Unbreakable ub = new Unbreakable(x, y, z, world, type, owner);

                        out.add(ub);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                System.out.print(ex.getMessage());
                ex.printStackTrace();
            }
        }

        if (purged > 0)
        {
            PreciousStones.log("countsPurgedUnbreakabes2", worldName, purged);
        }

        return out;
    }

    private void updateGrief(Field field)
    {
        if (field.isDirty(DirtyFieldReason.GRIEF_BLOCKS))
        {
            Queue<GriefBlock> grief = field.getGrief();

            for (GriefBlock gb : grief)
            {
                insertBlockGrief(field, gb);
            }
        }
    }

    public void updateField(Field field)
    {
        String subQuery = "";

        if (field.isDirty(DirtyFieldReason.OWNER))
        {
            subQuery += "owner = '" + Helper.escapeQuotes(field.getOwner()) + "', ";
        }

        if (field.isDirty(DirtyFieldReason.RADIUS))
        {
            subQuery += "radius = " + field.getRadius() + ", ";
        }

        if (field.isDirty(DirtyFieldReason.HEIGHT))
        {
            subQuery += "height = " + field.getHeight() + ", ";
        }

        if (field.isDirty(DirtyFieldReason.VELOCITY))
        {
            subQuery += "velocity = " + field.getVelocity() + ", ";
        }

        if (field.isDirty(DirtyFieldReason.NAME))
        {
            subQuery += "name = '" + Helper.escapeQuotes(field.getName()) + "', ";
        }

        if (field.isDirty(DirtyFieldReason.ALLOWED))
        {
            subQuery += "packed_allowed = '" + Helper.escapeQuotes(field.getPackedAllowed()) + "', ";
        }

        if (field.isDirty(DirtyFieldReason.LASTUSED))
        {
            subQuery += "last_used = " + (new DateTime()).getMillis() + ", ";
        }

        if (field.isDirty(DirtyFieldReason.FLAGS))
        {
            subQuery += "flags = '" + Helper.escapeQuotes(field.getFlagsAsString()) + "', ";
        }

        if (field.isDirty(DirtyFieldReason.DIMENSIONS))
        {
            subQuery += "minx = " + field.getMinx() + ", " + "miny = " + field.getMiny() + ", " + "minz = " + field.getMinz() + ", " + "maxx = " + field.getMaxx() + ", " + "maxy = " + field.getMaxy() + ", " + "maxz = " + field.getMaxz() + ", ";
        }

        if (!subQuery.isEmpty())
        {
            String query = "UPDATE `pstone_fields` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";

            if (field.hasFlag(FieldFlag.CUBOID))
            {
                query = "UPDATE `pstone_cuboids` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";
            }

            core.execute(query);

        }

        field.clearDirty();
    }

    /**
     * Insert a field into the database
     *
     * @param field
     */
    public void insertField(Field field)
    {
        Vec vec = field.toVec();

        if (pending.containsKey(vec))
        {
            processSingleField(pending.get(field.toVec()));
        }

        String query = "INSERT INTO `pstone_fields` (  `x`,  `y`, `z`, `world`, `radius`, `height`, `velocity`, `type_id`, `data`, `owner`, `name`, `packed_allowed`, `last_used`, `flags`) ";
        String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + "," + field.getData() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + (new DateTime()).getMillis() + "','" + Helper.escapeQuotes(field.getFlagsAsString()) + "');";

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            query = "INSERT INTO `pstone_cuboids` ( `parent`, `x`,  `y`, `z`, `world`, `minx`, `miny`, `minz`, `maxx`, `maxy`, `maxz`, `velocity`, `type_id`, `data`, `owner`, `name`, `packed_allowed`, `last_used`, `flags`) ";
            values = "VALUES ( " + (field.getParent() == null ? 0 : field.getParent().getId()) + "," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + field.getMinx() + "," + field.getMiny() + "," + field.getMinz() + "," + field.getMaxx() + "," + field.getMaxy() + "," + field.getMaxz() + "," + field.getVelocity() + "," + field.getTypeId() + "," + field.getData() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + (new DateTime()).getMillis() + "','" + Helper.escapeQuotes(field.getFlagsAsString()) + "');";
        }

        synchronized (this)
        {
            field.setId(core.insert(query + values));
        }
    }

    /**
     * Delete a field from the database
     *
     * @param field
     */
    public void deleteField(Field field)
    {
        String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            query = "DELETE FROM `pstone_cuboids` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";
        }

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes the field/cuboid from both tables
     *
     * @param field
     */
    public void deleteFieldFromBothTables(Field field)
    {
        String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";
        String query2 = "DELETE FROM `pstone_cuboids` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";


        synchronized (this)
        {
            core.delete(query);
            core.delete(query2);
        }
    }


    /**
     * Delete a field from the database that a player owns
     *
     * @param playerName
     */
    public void deleteFields(String playerName)
    {
        String query = "DELETE FROM `pstone_fields` WHERE owner = '" + Helper.escapeQuotes(playerName) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Delete a unbreakables from the database that a player owns
     *
     * @param playerName
     */
    public void deleteUnbreakables(String playerName)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE owner = '" + Helper.escapeQuotes(playerName) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Insert an unbreakable into the database
     *
     * @param ub
     */
    public void insertUnbreakable(Unbreakable ub)
    {
        String query = "INSERT INTO `pstone_unbreakables` (  `x`,  `y`, `z`, `world`, `owner`, `type_id`, `data`) ";
        String values = "VALUES ( " + ub.getX() + "," + ub.getY() + "," + ub.getZ() + ",'" + Helper.escapeQuotes(ub.getWorld()) + "','" + ub.getOwner() + "'," + ub.getTypeId() + "," + ub.getData() + ");";

        synchronized (this)
        {
            core.insert(query + values);
        }
    }

    /**
     * Delete an unbreakable from the database
     *
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE x = " + ub.getX() + " AND y = " + ub.getY() + " AND z = " + ub.getZ() + " AND world = '" + Helper.escapeQuotes(ub.getWorld()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Insert snitch entry into the database
     *
     * @param snitch
     * @param se
     */
    public void insertSnitchEntry(Field snitch, SnitchEntry se)
    {
        if (plugin.getSettingsManager().isUseMysql())
        {
            String query = "INSERT INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`, `date`) ";
            String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + Helper.escapeQuotes(snitch.getWorld()) + "','" + Helper.escapeQuotes(se.getName()) + "','" + Helper.escapeQuotes(se.getReason()) + "','" + Helper.escapeQuotes(se.getDetails()) + "',1, '" + (new DateTime()).getMillis() + "') ";
            String update = "ON DUPLICATE KEY UPDATE count = count+1;";

            synchronized (this)
            {
                core.insert(query + values + update);
            }
        }
        else
        {
            String query = "INSERT OR IGNORE INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`, `date`) ";
            String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + Helper.escapeQuotes(snitch.getWorld()) + "','" + Helper.escapeQuotes(se.getName()) + "','" + Helper.escapeQuotes(se.getReason()) + "','" + Helper.escapeQuotes(se.getDetails()) + "',1, '" + (new DateTime()).getMillis() + "');";
            String update = "UPDATE `pstone_snitches` SET count = count+1;";

            synchronized (this)
            {
                core.insert(query + values + update);
            }
        }
    }

    /**
     * Delete all snitch entries for a snitch form the database
     *
     * @param snitch
     */
    public void deleteSnitchEntries(Field snitch)
    {
        String query = "DELETE FROM `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + Helper.escapeQuotes(snitch.getWorld()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Retrieves all snitches belonging to a worlds from the database
     *
     * @param snitch
     * @return
     */
    public List<SnitchEntry> getSnitchEntries(Field snitch)
    {
        List<SnitchEntry> workingSnitchEntries = new ArrayList<SnitchEntry>();

        synchronized (pendingSnitchEntries)
        {
            workingSnitchEntries.addAll(pendingSnitchEntries);
            pendingSnitchEntries.clear();
        }

        processSnitches(workingSnitchEntries);

        List<SnitchEntry> out = new ArrayList<SnitchEntry>();

        String query = "SELECT * FROM  `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + Helper.escapeQuotes(snitch.getWorld()) + "' ORDER BY `date` DESC;";

        ResultSet res;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        String name = res.getString("name");
                        String reason = res.getString("reason");
                        String details = res.getString("details");
                        int count = res.getInt("count");

                        SnitchEntry ub = new SnitchEntry(null, name, reason, details, count);

                        out.add(ub);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
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
     * Delete a player from the players table
     *
     * @param playerName
     */
    public void deletePlayer(String playerName)
    {
        String query = "DELETE FROM `pstone_players` WHERE player_name = '" + playerName + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Update the player's last seen date on the database
     *
     * @param playerName
     */
    public void updatePlayer(String playerName)
    {
        long time = (new DateTime()).getMillis();

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(playerName);

        if (plugin.getSettingsManager().isUseMysql())
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            String values = "VALUES ( '" + playerName + "', " + time + ",'" + Helper.escapeQuotes(data.getFlags()) + "') ";
            String update = "ON DUPLICATE KEY UPDATE last_seen = " + time + ", flags = '" + Helper.escapeQuotes(data.getFlags()) + "'";

            synchronized (this)
            {
                core.insert(query + values + update);
            }
        }
        else
        {
            String query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            String values = "VALUES ( '" + playerName + "'," + time + ",'" + Helper.escapeQuotes(data.getFlags()) + "');";
            String update = "UPDATE `pstone_players` SET last_seen = " + time + ", flags = '" + Helper.escapeQuotes(data.getFlags()) + "' WHERE player_name = '" + playerName + "';";

            synchronized (this)
            {
                core.insert(query + values + update);
            }
        }
    }

    private void touchAllPlayers()
    {
        long time = (new DateTime()).getMillis();

        if (plugin.getSettingsManager().isUseMysql())
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            String values = "SELECT DISTINCT `owner`, " + time + " as last_seen, '' as flags FROM pstone_fields ";

            synchronized (this)
            {
                core.insert(query + values);
            }

            query = "INSERT IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            values = "SELECT DISTINCT `owner`, " + time + " as last_seen, '' as flags FROM pstone_unbreakables ";

            synchronized (this)
            {
                core.insert(query + values);
            }
        }
        else
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            String values = "SELECT DISTINCT `owner`, " + time + " as last_seen, '' as flags FROM pstone_fields ";

            synchronized (this)
            {
                core.insert(query + values);
            }

            query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
            values = "SELECT DISTINCT `owner`, " + time + " as last_seen, '' as flags FROM pstone_unbreakables ";

            synchronized (this)
            {
                core.insert(query + values);
            }
        }
    }

    /**
     * Record a single block grief
     *
     * @param field
     * @param gb
     */
    public void insertBlockGrief(Field field, GriefBlock gb)
    {
        String query = "INSERT INTO `pstone_grief_undo` ( `date_griefed`, `field_x`, `field_y` , `field_z`, `world`, `x` , `y`, `z`, `type_id`, `data`, `sign_text`) ";
        String values = "VALUES ( '" + (new DateTime()).getMillis() + "'," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + gb.getX() + "," + gb.getY() + "," + gb.getZ() + "," + gb.getTypeId() + "," + gb.getData() + ",'" + Helper.escapeQuotes(gb.getSignText()) + "');";

        synchronized (this)
        {
            core.insert(query + values);
        }
    }

    /**
     * Restores a field's griefed blocks
     *
     * @param field
     * @return
     */
    public Queue<GriefBlock> retrieveBlockGrief(Field field)
    {
        Set<Field> workingGrief = new HashSet<Field>();

        synchronized (pendingGrief)
        {
            workingGrief.addAll(pendingGrief);
            pendingGrief.clear();
        }

        processGrief(workingGrief);

        Queue<GriefBlock> out = new LinkedList<GriefBlock>();

        String query = "SELECT * FROM  `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "' ORDER BY y ASC;";

        ResultSet res;

        synchronized (this)
        {
            res = core.select(query);
        }

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

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        GriefBlock gb = new GriefBlock(x, y, z, field.getWorld(), type);

                        if (type_id == 0 || type_id == 8 || type_id == 9 || type_id == 10 || type_id == 11)
                        {
                            gb.setEmpty(true);
                        }

                        gb.setSignText(signText);
                        out.add(gb);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            PreciousStones.debug("Extracted %s griefed blocks from the db", out.size());

            if (!out.isEmpty())
            {
                PreciousStones.debug("Deleting grief from the db");
                deleteBlockGrief(field);
            }
        }

        return out;
    }

    /**
     * Deletes all records from a specific field
     *
     * @param field
     */
    public void deleteBlockGrief(Field field)
    {
        synchronized (pendingGrief)
        {
            pendingGrief.remove(field);
        }

        String query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes all records from a specific block
     *
     * @param block
     */
    public void deleteBlockGrief(Block block)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE x = " + block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND world = '" + Helper.escapeQuotes(block.getWorld().getName()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Checks if the translocation head record exists
     *
     * @param name
     * @param playerName
     * @return
     */
    public boolean existsTranslocatior(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM `pstone_translocations` WHERE `name` ='" + Helper.escapeQuotes(name) + "' AND `player_name` = '" + Helper.escapeQuotes(playerName) + "'";
        ResultSet res;
        boolean exists = false;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    exists = res.getInt(1) > 0;
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return exists;
    }

    /**
     * Sets the size of the field
     *
     * @param field
     * @param fieldName
     * @return
     */
    public boolean changeSizeTranslocatiorField(Field field, String fieldName)
    {
        String query = "SELECT * FROM `pstone_translocations` WHERE `name` ='" + Helper.escapeQuotes(fieldName) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' LIMIT 1";
        ResultSet res;
        boolean exists = false;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        field.setRelativeCuboidDimensions(res.getInt("minx"), res.getInt("miny"), res.getInt("minz"), res.getInt("maxx"), res.getInt("maxy"), res.getInt("maxz"));
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return exists;
    }

    /**
     * Add the head record for the translocation
     *
     * @param field
     * @param name
     */
    public void insertTranslocationHead(Field field, String name)
    {
        boolean exists = existsTranslocatior(name, field.getOwner());

        if (exists)
        {
            return;
        }

        String query = "INSERT INTO `pstone_translocations` ( `name`, `player_name`, `minx`, `miny`, `minz`, `maxx`, `maxy`, `maxz`) ";
        String values = "VALUES ( '" + Helper.escapeQuotes(name) + "','" + Helper.escapeQuotes(field.getOwner()) + "'," + field.getRelativeMin().getBlockX() + "," + field.getRelativeMin().getBlockY() + "," + field.getRelativeMin().getBlockZ() + "," + field.getRelativeMax().getBlockX() + "," + field.getRelativeMax().getBlockY() + "," + field.getRelativeMax().getBlockZ() + ");";

        synchronized (this)
        {
            core.insert(query + values);
        }
    }

    /**
     * Record a single translocation block
     *
     * @param field
     * @param tb
     */
    public void insertTranslocationBlock(Field field, TranslocationBlock tb)
    {
        insertTranslocationBlock(field, tb, true);
    }

    /**
     * Record a single translocation block
     *
     * @param field
     * @param tb
     * @param applied
     */
    public void insertTranslocationBlock(Field field, TranslocationBlock tb, boolean applied)
    {
        String query = "INSERT INTO `pstone_storedblocks` ( `name`, `player_name`, `world`, `x` , `y`, `z`, `type_id`, `data`, `contents`, `sign_text`, `applied`) ";
        String values = "VALUES ( '" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getOwner()) + "','" + Helper.escapeQuotes(field.getWorld()) + "'," + tb.getRx() + "," + tb.getRy() + "," + tb.getRz() + "," + tb.getTypeId() + "," + tb.getData() + ",'" + tb.getContents() + "','" + Helper.escapeQuotes(tb.getSignText()) + "', " + (applied ? 1 : 0) + ");";

        synchronized (this)
        {
            core.insert(query + values);
        }
    }

    /**
     * Retrieves the count of applied translocation blocks
     *
     * @param field
     * @return
     */
    public int appliedTranslocationCount(Field field)
    {
        return appliedTranslocationCount(field.getName(), field.getOwner());
    }

    /**
     * Retrieves the count of applied translocation blocks
     *
     * @param name
     * @param playerName
     * @return
     */
    public int appliedTranslocationCount(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(name) + "' AND `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `applied` = 1";
        ResultSet res;
        int count = 0;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    count = res.getInt(1);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return count;
    }

    /**
     * Retrieves the count of applied translocation blocks
     *
     * @param name
     * @param playerName
     * @return
     */
    public int totalTranslocationCount(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(name) + "' AND `player_name` = '" + Helper.escapeQuotes(playerName) + "'";
        ResultSet res;
        int count = 0;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    count = res.getInt(1);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return count;
    }

    /**
     * Retrieves the count of unapplied translocation blocks
     *
     * @param field
     * @return
     */
    public int unappliedTranslocationCount(Field field)
    {
        return unappliedTranslocationCount(field.getName(), field.getOwner());
    }

    /**
     * Retrieves the count of unapplied translocation blocks
     *
     * @param name
     * @param playerName
     * @return
     */
    public int unappliedTranslocationCount(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(name) + "' AND `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `applied` = 0";
        ResultSet res;
        int count = 0;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    count = res.getInt(1);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return count;
    }

    /**
     * Returns the translocation blocks, and marks them as not-applied on the database
     *
     * @param field
     * @return
     */
    public Queue<TranslocationBlock> retrieveClearTranslocation(Field field)
    {
        Queue<TranslocationBlock> out = new LinkedList<TranslocationBlock>();

        String query = "SELECT * FROM  `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `applied` = 1 ORDER BY y ASC;";

        ResultSet res;

        synchronized (this)
        {
            res = core.select(query);
        }

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

                        World world = plugin.getServer().getWorld(field.getWorld());

                        Location location = new Location(world, x, y, z);
                        location = location.add(field.getLocation());

                        int type_id = res.getInt("type_id");
                        byte data = res.getByte("data");
                        String signText = res.getString("sign_text");
                        String contents = res.getString("contents");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        TranslocationBlock tb = new TranslocationBlock(location, type);

                        if (type_id == 0 || type_id == 8 || type_id == 9 || type_id == 10 || type_id == 11)
                        {
                            tb.setEmpty(true);
                        }

                        tb.setContents(contents);
                        tb.setRelativeCoords(x, y, z);
                        tb.setSignText(signText);
                        out.add(tb);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        clearTranslocation(field);
        return out;
    }

    /**
     * Returns the translocation blocks, and marks them as not-applied on the database
     *
     * @param field
     * @return
     */
    public Queue<TranslocationBlock> retrieveTranslocation(Field field)
    {
        Queue<TranslocationBlock> out = new LinkedList<TranslocationBlock>();

        String query = "SELECT * FROM  `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `applied` = 0 ORDER BY y ASC;";

        ResultSet res;

        synchronized (this)
        {
            res = core.select(query);
        }

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

                        World world = plugin.getServer().getWorld(field.getWorld());

                        Location location = new Location(world, x, y, z);
                        location = location.add(field.getLocation());

                        int type_id = res.getInt("type_id");
                        byte data = res.getByte("data");
                        String signText = res.getString("sign_text");
                        String contents = res.getString("contents");

                        BlockTypeEntry type = new BlockTypeEntry(type_id, data);

                        TranslocationBlock tb = new TranslocationBlock(location, type);

                        if (type_id == 0 || type_id == 8 || type_id == 9 || type_id == 10 || type_id == 11)
                        {
                            tb.setEmpty(true);
                        }

                        tb.setContents(contents);
                        tb.setRelativeCoords(x, y, z);
                        tb.setSignText(signText);
                        out.add(tb);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        applyTranslocation(field);
        return out;
    }

    /**
     * Returns the players stored translocations and their sizes
     *
     * @param playerName
     * @return
     */
    public Map<String, Integer> getTranslocationDetails(String playerName)
    {
        Map<String, Integer> out = new HashMap<String, Integer>();

        String query = "SELECT name, COUNT(name) FROM  `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' GROUP BY `name`;";

        ResultSet res;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        String name = res.getString(1);
                        int count = res.getInt(2);

                        out.put(name, count);
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
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
     * Returns whether a field with that name by that player exists
     *
     * @param name
     * @param playerName
     * @return
     */
    public boolean existsFieldWithName(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM  `pstone_fields` WHERE `owner` = '" + Helper.escapeQuotes(playerName) + "' AND `name` ='" + Helper.escapeQuotes(name) + "'";
        ResultSet res;
        boolean exists = false;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        int count = res.getInt(1);
                        exists = count > 0;
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        query = "SELECT COUNT(*) FROM  `pstone_cuboids` WHERE `owner` = '" + Helper.escapeQuotes(playerName) + "' AND `name` ='" + Helper.escapeQuotes(name) + "'";

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        int count = res.getInt(1);
                        exists = exists || count > 0;
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return exists;
    }

    /**
     * Returns whether there is data witht tha name for that player
     *
     * @param name
     * @param playerName
     * @return
     */
    public boolean existsTranslocationDataWithName(String name, String playerName)
    {
        String query = "SELECT COUNT(*) FROM  `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `name` ='" + Helper.escapeQuotes(name) + "'";
        ResultSet res;
        boolean exists = false;

        synchronized (this)
        {
            res = core.select(query);
        }

        if (res != null)
        {
            try
            {
                while (res.next())
                {
                    try
                    {
                        int count = res.getInt(1);
                        exists = count > 0;
                    }
                    catch (Exception ex)
                    {
                        PreciousStones.getLog().info(ex.getMessage());
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return exists;
    }

    /**
     * Marks all translocation blocks as applied for a given field
     *
     * @param field
     */
    public void applyTranslocation(Field field)
    {
        String query = "UPDATE `pstone_storedblocks` SET `applied` = 1 WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `applied` = 0;";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Marks all translocation blocks as not-applied for a given field
     *
     * @param field
     */
    public void clearTranslocation(Field field)
    {
        String query = "UPDATE `pstone_storedblocks` SET `applied` = 0 WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `applied` = 1;";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Deletes all records from a specific field
     *
     * @param field
     */
    public void deleteAppliedTranslocation(Field field)
    {
        String query = "DELETE FROM `pstone_storedblocks` WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `applied` = 1;";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes a specific block from a translocation field
     *
     * @param field
     * @param tb
     */
    public void deleteTranslocation(Field field, TranslocationBlock tb)
    {
        Location location = tb.getRelativeLocation();

        String query = "DELETE FROM `pstone_storedblocks` WHERE x = " + location.getBlockX() + " AND y = " + location.getBlockY() + " AND z = " + location.getBlockZ() + " AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `name` = '" + Helper.escapeQuotes(field.getName()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes all records from a player
     *
     * @param playerName
     */
    public void deleteTranslocation(String playerName)
    {
        String query = "DELETE FROM `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes all of a translocation's blocks
     *
     * @param name
     * @param playerName
     */
    public void deleteTranslocation(String name, String playerName)
    {
        String query = "DELETE FROM `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `name` = '" + Helper.escapeQuotes(name) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes the translocation's head record
     *
     * @param name
     * @param playerName
     */
    public void deleteTranslocationHead(String name, String playerName)
    {
        String query = "DELETE FROM `pstone_translocations` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `name` = '" + Helper.escapeQuotes(name) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Deletes the translocation's head record
     *
     * @param name
     * @param playerName
     * @param block
     * @return
     */
    public int deleteBlockTypeFromTranslocation(String name, String playerName, BlockTypeEntry block)
    {
        int beforeCount = totalTranslocationCount(name, playerName);

        String query = "DELETE FROM `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `name` = '" + Helper.escapeQuotes(name) + "' AND `type_id` = " + block.getTypeId() + ";";

        if (block.getData() > 0)
        {
            query = "DELETE FROM `pstone_storedblocks` WHERE `player_name` = '" + Helper.escapeQuotes(playerName) + "' AND `name` = '" + Helper.escapeQuotes(name) + "' AND `type_id` = " + block.getTypeId() + " AND `data` = " + block.getData() + ";";
        }

        synchronized (this)
        {
            core.delete(query);
        }

        int afterCount = totalTranslocationCount(name, playerName);

        return beforeCount - afterCount;
    }

    /**
     * Changes the owner of a translocation block
     *
     * @param field
     * @param newOwner
     */
    public void changeTranslocationOwner(Field field, String newOwner)
    {
        String query = "UPDATE `pstone_storedblocks` SET `player_name` = '" + Helper.escapeQuotes(newOwner) + "' WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "';";

        synchronized (this)
        {
            core.delete(query);
        }
    }

    /**
     * Mark a single translocation block as applied in a field
     *
     * @param field
     * @param tb
     * @param applied
     */
    public void updateTranslocationBlockApplied(Field field, TranslocationBlock tb, boolean applied)
    {
        Location location = tb.getRelativeLocation();

        String query = "UPDATE `pstone_storedblocks` SET `applied` = " + (applied ? 1 : 0) + " WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `x` = " + location.getBlockX() + " AND `y` = " + location.getBlockY() + " AND `z` = " + location.getBlockZ() + ";";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Returns whether the translocation is applied or not
     *
     * @param name
     * @param playerName
     * @return
     */
    public boolean isTranslocationApplied(String name, String playerName)
    {
        return appliedTranslocationCount(name, playerName) > 0;
    }

    /**
     * Update a block's data on the database
     *
     * @param field
     * @param tb
     */
    public void updateTranslocationBlockData(Field field, TranslocationBlock tb)
    {
        Location location = tb.getRelativeLocation();

        String query = "UPDATE `pstone_storedblocks` SET `data` = " + tb.getData() + " WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `x` = " + location.getBlockX() + " AND `y` = " + location.getBlockY() + " AND `z` = " + location.getBlockZ() + ";";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Update a block's content on the database
     *
     * @param field
     * @param tb
     */
    public void updateTranslocationBlockContents(Field field, TranslocationBlock tb)
    {
        Location location = tb.getRelativeLocation();

        String query = "UPDATE `pstone_storedblocks` SET `contents` = '" + tb.getContents() + "' WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `x` = " + location.getBlockX() + " AND `y` = " + location.getBlockY() + " AND `z` = " + location.getBlockZ() + ";";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Update a block's signtext on the database
     *
     * @param field
     * @param tb
     */
    public void updateTranslocationSignText(Field field, TranslocationBlock tb)
    {
        Location location = tb.getRelativeLocation();

        String query = "UPDATE `pstone_storedblocks` SET `sign_text` = '" + tb.getSignText() + "' WHERE `name` ='" + Helper.escapeQuotes(field.getName()) + "' AND `player_name` = '" + Helper.escapeQuotes(field.getOwner()) + "' AND `x` = " + location.getBlockX() + " AND `y` = " + location.getBlockY() + " AND `z` = " + location.getBlockZ() + ";";

        synchronized (this)
        {
            core.update(query);
        }
    }

    /**
     * Schedules the pending queue on save frequency
     *
     * @return
     */
    public BukkitTask saverScheduler()
    {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable()
        {
            public void run()
            {
                processQueue();
            }
        }, 0, 20L * plugin.getSettingsManager().getSaveFrequency());
    }

    /**
     * Process entire queue
     */
    public void processQueue()
    {
        Map<Vec, Field> working = new HashMap<Vec, Field>();
        Map<Unbreakable, Boolean> workingUb = new HashMap<Unbreakable, Boolean>();
        Map<String, Boolean> workingPlayers = new HashMap<String, Boolean>();
        Set<Field> workingGrief = new HashSet<Field>();
        List<SnitchEntry> workingSnitchEntries = new ArrayList<SnitchEntry>();

        synchronized (pending)
        {
            working.putAll(pending);
            pending.clear();
        }
        synchronized (pendingUb)
        {
            workingUb.putAll(pendingUb);
            pendingUb.clear();
        }
        synchronized (pendingGrief)
        {
            workingGrief.addAll(pendingGrief);
            pendingGrief.clear();
        }
        synchronized (pendingPlayers)
        {
            workingPlayers.putAll(pendingPlayers);
            pendingPlayers.clear();
        }
        synchronized (pendingSnitchEntries)
        {
            workingSnitchEntries.addAll(pendingSnitchEntries);
            pendingSnitchEntries.clear();
        }

        if (!working.isEmpty())
        {
            processFields(working);
        }

        if (!workingUb.isEmpty())
        {
            processUnbreakable(workingUb);
        }

        if (!workingGrief.isEmpty())
        {
            processGrief(workingGrief);
        }

        if (!workingPlayers.isEmpty())
        {
            processPlayers(workingPlayers);
        }

        if (!workingSnitchEntries.isEmpty())
        {
            processSnitches(workingSnitchEntries);
        }
    }

    /**
     * Process suingle field
     *
     * @param field
     */
    public void processSingleField(Field field)
    {
        if (plugin.getSettingsManager().isDebug())
        {
            PreciousStones.getLog().info("[Queue] processing single query");
        }

        if (field.isDirty(DirtyFieldReason.DELETE))
        {
            deleteField(field);
        }
        else
        {
            updateField(field);
        }

        synchronized (this)
        {
            pending.remove(field.toVec());
        }
    }

    /**
     * Process pending pstones
     *
     * @param working
     */
    public void processFields(Map<Vec, Field> working)
    {
        if (plugin.getSettingsManager().isDebug() && !working.isEmpty())
        {
            PreciousStones.getLog().info("[Queue] processing " + working.size() + " pstone queries...");
        }

        for (Field field : working.values())
        {
            if (field.isDirty(DirtyFieldReason.DELETE))
            {
                deleteField(field);
            }
            else
            {
                updateField(field);
            }
        }
    }

    /**
     * Process pending grief
     *
     * @param workingUb
     */
    public void processUnbreakable(Map<Unbreakable, Boolean> workingUb)
    {
        if (plugin.getSettingsManager().isDebug() && !workingUb.isEmpty())
        {
            PreciousStones.getLog().info("[Queue] processing " + workingUb.size() + " unbreakable queries...");
        }

        for (Unbreakable ub : workingUb.keySet())
        {
            if (workingUb.get(ub))
            {
                insertUnbreakable(ub);
            }
            else
            {
                deleteUnbreakable(ub);
            }
        }
    }

    /**
     * Process pending players
     *
     * @param workingPlayers
     */
    public void processPlayers(Map<String, Boolean> workingPlayers)
    {
        if (plugin.getSettingsManager().isDebug() && !workingPlayers.isEmpty())
        {
            PreciousStones.getLog().info("[Queue] processing " + workingPlayers.size() + " player queries...");
        }

        for (String playerName : workingPlayers.keySet())
        {
            if (workingPlayers.get(playerName))
            {
                updatePlayer(playerName);
            }
            else
            {
                deletePlayer(playerName);
                deleteTranslocation(playerName);
                deleteFields(playerName);
                deleteUnbreakables(playerName);
            }
        }
    }

    /**
     * Process pending snitches
     *
     * @param workingSnitchEntries
     */
    public void processSnitches(List<SnitchEntry> workingSnitchEntries)
    {
        if (plugin.getSettingsManager().isDebug() && !workingSnitchEntries.isEmpty())
        {
            PreciousStones.getLog().info("[Queue] sending " + workingSnitchEntries.size() + " snitch queries...");
        }

        for (SnitchEntry se : workingSnitchEntries)
        {
            insertSnitchEntry(se.getField(), se);
        }
    }

    /**
     * Process pending grief
     *
     * @param workingGrief
     */
    public void processGrief(Set<Field> workingGrief)
    {
        if (plugin.getSettingsManager().isDebug() && !workingGrief.isEmpty())
        {
            PreciousStones.getLog().info("[Queue] processing " + workingGrief.size() + " grief queries...");
        }

        for (Field field : workingGrief)
        {
            updateGrief(field);
        }
    }
}
