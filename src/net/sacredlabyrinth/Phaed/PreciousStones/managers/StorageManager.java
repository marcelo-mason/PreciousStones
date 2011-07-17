package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.data.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.World;
import org.bukkit.block.Block;
import net.sacredlabyrinth.Phaed.PreciousStones.data.mysqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.data.sqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import net.sacredlabyrinth.Phaed.PreciousStones.Dates;
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
    private PreciousStones plugin;
    private final Map<Vec, Field> pending = new HashMap<Vec, Field>();
    private final Map<Unbreakable, Boolean> pendingUb = new HashMap<Unbreakable, Boolean>();
    private final Map<String, Boolean> pendingPlayers = new HashMap<String, Boolean>();
    private final Set<Field> pendingGrief = new HashSet<Field>();
    private final List<SnitchEntry> pendingSnitchEntries = new LinkedList<SnitchEntry>();

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

                removeOldColumn();

                if (!core.checkTable("pstone_fields"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_fields");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_fields_1` (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_unbreakables_1` (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_grief_undo"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_grief_undo");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20) NOT NULL auto_increment,  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_players"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_players");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, PRIMARY KEY  (`player_name`));";
                    core.createTable(query);
                    touchAllPlayers();
                }

                if (!core.checkTable("pstone_snitches"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_snitches");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));";
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

                removeOldColumn();

                if (!core.checkTable("pstone_fields"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_fields");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_grief_undo"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_grief_undo");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20),  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_players"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_players");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, PRIMARY KEY  (`player_name`));";
                    core.createTable(query);
                    touchAllPlayers();
                }

                if (!core.checkTable("pstone_snitches"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_snitches");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));";
                    core.createTable(query);
                }
            }
            else
            {
                PreciousStones.log(Level.INFO, "SQLite Connection failed");
            }
        }

    }

    private void removeOldColumn()
    {
        if (core.checkTable("pstone_fields") && !core.checkTable("pstone_snitches"))
        {
            if (plugin.settings.useMysql)
            {
                String query = "UPDATE pstone_fields SET packed_snitch_list = '';";
                core.sqlQuery(query);
                //String query = "ALTER TABLE pstone_fields DROP COLUMN packed_snitch_list;";
                //core.updateQuery(query);
            }

            String query = "ALTER TABLE pstone_fields ADD COLUMN last_used bigint(20);";
            core.updateQuery(query);
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
            PreciousStones.log(Level.INFO, "({0}) fields: {1}", world, fields.size());
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
            PreciousStones.log(Level.INFO, "({0}) unbreakables: {1}", world, unbreakables.size());
        }
    }

    /**
     * Puts the field up for future storage
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
     * Puts the unbreakable up for future storage
     * @param field
     */
    public void offerUnbreakable(Unbreakable ub, boolean insert)
    {
        synchronized (pendingUb)
        {
            pendingUb.put(ub, insert);
        }
    }

    /**
     * Puts the field up for grief reversion
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
     * Puts the player up for future storage
     * @param player
     */
    public void offerPlayer(String playerName, boolean update)
    {
        synchronized (pendingPlayers)
        {
            pendingPlayers.put(playerName, update);
        }
    }

    /**
     * Puts the snitch list up for future storage
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
     * Retrieves all fields belonging to a worlds from the database
     * @param worldName
     * @return
     */
    public List<Field> getFields(String worldName)
    {
        List<Field> out = new ArrayList<Field>();
        int purged = 0;

        String query = "SELECT * FROM  pstone_fields LEFT JOIN pstone_players ON pstone_fields.owner = pstone_players.player_name WHERE world = '" + worldName + "';";
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
                        int radius = res.getInt("radius");
                        int height = res.getInt("height");
                        int type_id = res.getInt("type_id");
                        float velocity = res.getFloat("velocity");
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        String name = res.getString("name");
                        String packed_allowed = res.getString("packed_allowed");
                        long last_seen = res.getLong("last_seen");
                        long last_used = res.getLong("last_used");

                        Field field = new Field(x, y, z, radius, height, velocity, world, type_id, owner, name, last_used);
                        field.setPackedAllowed(packed_allowed);

                        if (last_seen > 0)
                        {
                            int lastSeenDays = (int) Dates.differenceInDays(new Date(), new Date(last_seen));

                            if (lastSeenDays > plugin.settings.purgeAfterDays)
                            {
                                offerPlayer(owner, false);
                                purged++;
                                continue;
                            }
                        }

                        if (field.getAgeInDays() > plugin.settings.purgeSnitchAfterDays)
                        {
                            FieldSettings fs = plugin.settings.getFieldSettings(field);

                            if (fs != null && fs.snitch)
                            {
                                deleteSnitchEntires(field);
                                field.markForDeletion();
                                offerField(field);
                                purged++;
                                continue;
                            }
                        }

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

        if (purged > 0)
        {
            PreciousStones.log(Level.INFO, "({0}) fields purged: {1}", worldName, purged);
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
        int purged = 0;


        String query = "SELECT * FROM  `pstone_unbreakables` LEFT JOIN pstone_players ON pstone_unbreakables.owner = pstone_players.player_name WHERE world = '" + worldName + "';";
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
                        String world = res.getString("world");
                        String owner = res.getString("owner");
                        long last_seen = res.getLong("last_seen");

                        Unbreakable ub = new Unbreakable(x, y, z, world, type_id, owner);

                        if (last_seen > 0)
                        {
                            int lastSeenDays = (int) Dates.differenceInDays(new Date(), new Date(last_seen));

                            if (lastSeenDays > plugin.settings.purgeAfterDays)
                            {
                                offerUnbreakable(ub, false);
                                offerPlayer(owner, false);
                                purged++;
                                continue;
                            }
                        }

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

        if (purged > 0)
        {
            PreciousStones.log(Level.INFO, "({0}) unbreakables purged: {1}", worldName, purged);
        }

        return out;
    }

    private void updateGrief(Field field)
    {
        if (field.isDirty(Field.Dirty.GRIEF_BLOCKS))
        {
            List<GriefBlock> grief = field.getGrief();

            for (GriefBlock gb : grief)
            {
                recordBlockGrief(field, gb);
            }
        }
    }

    private void updateField(Field field)
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

        if (field.isDirty(Field.Dirty.LASTUSED))
        {
            subQuery += "last_used = " + (new Date()).getTime() + ", ";
        }

        if (!subQuery.isEmpty())
        {
            String query = "UPDATE `pstone_fields` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";

            if (plugin.settings.debugsql)
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
        String query = "INSERT INTO `pstone_fields` (  `x`,  `y`, `z`, `world`, `radius`, `height`, `velocity`, `type_id`, `owner`, `name`, `packed_allowed`) ";
        String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "');";

        if (plugin.settings.debugsql)
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
     * Delete a fields form the database that a player owns
     * @param playerName
     */
    public void deleteFields(String playerName)
    {
        String query = "DELETE FROM `pstone_fields` WHERE owner = '" + playerName + "';";
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
     * Delete an unbreakabale form the database that a player owns
     * @param playerName
     */
    public void deleteUnbreakables(String playerName)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE owner = '" + playerName + "';";
        core.addBatch(query);
    }

    /**
     * Insert snitch entry into the database
     * @param snitch
     * @param se
     */
    public void insertSnitchEntry(Field snitch, SnitchEntry se)
    {
        if (plugin.settings.useMysql)
        {
            String query = "INSERT INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`) ";
            String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + snitch.getWorld() + "','" + se.getName() + "','" + se.getReason() + "','" + se.getDetails() + "',1) ";
            String update = "ON DUPLICATE KEY UPDATE count = count+1;";
            core.addBatch(query + values + update);
        }
        else
        {
            String query = "INSERT OR IGNORE INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`) ";
            String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + snitch.getWorld() + "','" + se.getName() + "','" + se.getReason() + "','" + se.getDetails() + "',1) ";
            String update = "UPDATE `pstone_snitches` SET count = count+1;";
            core.addBatch(query + values + update);
        }
    }

    /**
     * Delete all snitch entries for a snitch form the database
     * @param snitch
     */
    public void deleteSnitchEntires(Field snitch)
    {
        String query = "DELETE FROM `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + snitch.getWorld() + "';";

        if (plugin.settings.debugsql)
        {
            PreciousStones.logger.info(query);
        }
        core.deleteQuery(query);
    }

    /**
     * Retrieves all snitches belonging to a worlds from the database
     * @param snitch
     * @return
     */
    public List<SnitchEntry> getSnitchEntries(Field snitch)
    {
        List<SnitchEntry> workingSnitchEntries = new LinkedList<SnitchEntry>();

        synchronized (pendingSnitchEntries)
        {
            workingSnitchEntries.addAll(pendingSnitchEntries);
            pendingSnitchEntries.clear();
        }

        processSnitches(workingSnitchEntries);

        List<SnitchEntry> out = new ArrayList<SnitchEntry>();

        String query = "SELECT * FROM  `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + snitch.getWorld() + "' ORDER BY `id` DESC;";
        ResultSet res = core.sqlQuery(query);

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
     * Delete a player from the players table
     * @param playerName
     */
    public void deletePlayer(String playerName)
    {
        String query = "DELETE FROM `pstone_players` WHERE player_name = '" + playerName + "';";
        core.addBatch(query);
    }

    /**
     * Update the player's last seen date on the database
     * @param playerName
     */
    public void touchPlayer(String playerName)
    {
        long time = (new Date()).getTime();

        if (plugin.settings.useMysql)
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            String values = "VALUES ( '" + playerName + "', " + time + ") ";
            String update = "ON DUPLICATE KEY UPDATE last_seen = " + time + ";";
            core.addBatch(query + values + update);
        }
        else
        {
            String query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            String values = "VALUES ( '" + playerName + "'," + time + ");";
            String update = "UPDATE `pstone_players` SET last_seen = " + time + ";";
            core.addBatch(query + values + update);
        }
    }

    private void touchAllPlayers()
    {
        long time = (new Date()).getTime();

        core.openBatch();

        if (plugin.settings.useMysql)
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            String values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_fields ";
            core.addBatch(query + values);

            query = "INSERT IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_unbreakables ";
            core.addBatch(query + values);
        }
        else
        {
            String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            String values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_fields ";
            core.addBatch(query + values);

            query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`) ";
            values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_unbreakables ";
            core.addBatch(query + values);
        }
        core.closeBatch();
    }

    /**
     * Record a single block grief
     * @param field
     * @param gb
     */
    public void recordBlockGrief(Field field, GriefBlock gb)
    {
        World world = plugin.getServer().getWorld(gb.getWorld());

        if (world == null)
        {
            return;
        }

        if (!plugin.gum.isDependentBlock(gb.getTypeId()))
        {
            BlockFace[] faces =
            {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
            };

            for (BlockFace face : faces)
            {
                Block block = world.getBlockAt(gb.toLocation(world));
                Block rel = block.getRelative(face);

                if (plugin.gum.isDependentBlock(rel.getTypeId()))
                {
                    recordBlockGrief(field, new GriefBlock(rel.getLocation(), rel.getTypeId(), rel.getData()));
                    rel.setTypeId(0);
                }
            }
        }

        if (gb.getTypeId() == 64 || gb.getTypeId() == 71)
        {
            // record wood doors in correct order

            Block block = world.getBlockAt(gb.toLocation(world));

            if ((gb.getData() & 0x8) == 0x8)
            {
                Block bottom = block.getRelative(BlockFace.DOWN);

                recordBlockGriefClean(field, new GriefBlock(bottom));
                recordBlockGriefClean(field, gb);

                bottom.setTypeId(0);
                block.setTypeId(0);
            }
            else
            {
                Block top = block.getRelative(BlockFace.UP);

                recordBlockGriefClean(field, gb);
                recordBlockGriefClean(field, new GriefBlock(top));

                block.setTypeId(0);
                top.setTypeId(0);
            }
        }
        else
        {
            recordBlockGriefClean(field, gb);
        }
    }

    /**
     * Record a single block grief
     * @param field
     * @param block
     */
    public void recordBlockGriefClean(Field field, GriefBlock gb)
    {
        String query = "INSERT INTO `pstone_grief_undo` ( `date_griefed`, `field_x`, `field_y` , `field_z`, `world`, `x` , `y`, `z`, `type_id`, `data`, `sign_text`) ";
        String values = "VALUES ( '" + new Timestamp((new Date()).getTime()) + "'," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + gb.getX() + "," + gb.getY() + "," + gb.getZ() + "," + gb.getTypeId() + "," + gb.getData() + ",'" + Helper.escapeQuotes(gb.getSignText()) + "');";
        core.addBatch(query + values);
    }

    /**
     * Restores a field's griefed blocks
     * @param field
     * @return
     */
    public List<GriefBlock> retrieveBlockGrief(Field field)
    {
        Set<Field> workingGrief = new HashSet<Field>();

        synchronized (pendingGrief)
        {
            workingGrief.addAll(pendingGrief);
            pendingGrief.clear();
        }

        processGrief(workingGrief);

        List<GriefBlock> out = new ArrayList<GriefBlock>();

        String query = "SELECT * FROM  `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "' ORDER BY y ASC;";

        if (plugin.settings.debugsql)
        {
            PreciousStones.logger.info(query);
        }

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
        field.clearGrief();
        return out;
    }

    /**
     * Deletes all records form a specific field
     * @param field
     */
    public void deleteBlockGrief(Field field)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";

        if (plugin.settings.debugsql)
        {
            PreciousStones.logger.info(query);
        }

        core.deleteQuery(query);
    }

    /**
     * Deletes all records form a specific block
     * @param block
     */
    public void deleteBlockGrief(Block block)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE x = " + block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND world = '" + block.getWorld().getName() + "';";

        if (plugin.settings.debugsql)
        {
            PreciousStones.logger.info(query);
        }

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
                processQueue();
            }
        }, 0, 20L * plugin.settings.saveFrequency);
    }

    /**
     * Process entire queue
     * @param dirtyType
     */
    public void processQueue()
    {
        Map<Vec, Field> working = new HashMap<Vec, Field>();
        Map<Unbreakable, Boolean> workingUb = new HashMap<Unbreakable, Boolean>();
        Map<String, Boolean> workingPlayers = new HashMap<String, Boolean>();
        Set<Field> workingGrief = new HashSet<Field>();
        List<SnitchEntry> workingSnitchEntries = new LinkedList<SnitchEntry>();

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

        core.openBatch();

        processFields(working);
        processUnbreakable(workingUb);
        processGrief(workingGrief);
        processPlayers(workingPlayers);
        processSnitches(workingSnitchEntries);

        core.closeBatch();

        if (plugin.settings.debugdb && !pending.isEmpty())
        {
            PreciousStones.logger.info("[Queue] done");
        }

    }

    /**
     * Process pending pstones
     * @param dirtyType
     */
    public void processFields(Map<Vec, Field> working)
    {
        int count = 0;

        if (plugin.settings.debugdb && !working.isEmpty())
        {
            PreciousStones.logger.info("[Queue] processing " + working.size() + " pstone queries...");
        }

        for (Field field : working.values())
        {
            updateField(field);

            if (count >= plugin.settings.saveBatchSize)
            {
                if (plugin.settings.debugdb)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count + " pstone queries");
                }
                core.closeBatch();
                core.openBatch();
                count = 0;
            }
            count++;
        }

        if (plugin.settings.debugdb && count > 0)
        {
            PreciousStones.logger.info("[Queue] sent batch of " + count + " pstone queries");
        }
    }

    /**
     * Process pending grief
     */
    public void processUnbreakable(Map<Unbreakable, Boolean> workingUb)
    {
        int count = 0;

        if (plugin.settings.debugdb && !workingUb.isEmpty())
        {
            PreciousStones.logger.info("[Queue] processing " + workingUb.size() + " unbreakable queries...");
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

            if (count >= plugin.settings.saveBatchSize)
            {
                if (plugin.settings.debugdb)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count + " unbreakable queries");
                }
                core.closeBatch();
                core.openBatch();
                count = 0;
            }
            count++;
        }

        if (plugin.settings.debugdb && count > 0)
        {
            PreciousStones.logger.info("[Queue] sent batch of " + count + " unbreakable queries");
        }
    }

    /**
     * Process pending players
     */
    public void processPlayers(Map<String, Boolean> workingPlayers)
    {
        int count = 0;

        if (plugin.settings.debugdb && !workingPlayers.isEmpty())
        {
            PreciousStones.logger.info("[Queue] processing " + workingPlayers.size() + " player queries...");
        }

        for (String playerName : workingPlayers.keySet())
        {
            if (workingPlayers.get(playerName))
            {
                touchPlayer(playerName);
            }
            else
            {
                deletePlayer(playerName);
            }

            if (count >= plugin.settings.saveBatchSize)
            {
                if (plugin.settings.debugdb)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count + " player queries");
                }
                core.closeBatch();
                core.openBatch();
                count = 0;
            }
            count++;
        }

        if (plugin.settings.debugdb && count > 0)
        {
            PreciousStones.logger.info("[Queue] sent batch of " + count + " player queries");
        }
    }

    /**
     * Process pending snitches
     */
    public void processSnitches(List<SnitchEntry> workingSnitchEntries)
    {
        int count = 0;

        if (plugin.settings.debugdb && !workingSnitchEntries.isEmpty())
        {
            PreciousStones.logger.info("[Queue] sending " + workingSnitchEntries.size() + " snitch queries...");
        }

        for (SnitchEntry se : workingSnitchEntries)
        {
            insertSnitchEntry(se.getField(), se);

            if (count >= plugin.settings.saveBatchSize)
            {
                if (plugin.settings.debugdb)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count + " snitch queries");
                }
                core.closeBatch();
                core.openBatch();
                count = 0;
            }
            count++;
        }

        if (plugin.settings.debugdb && count > 0)
        {
            PreciousStones.logger.info("[Queue] sent batch of " + count + " snitch queries");
        }
    }

    /**
     * Process pending grief
     */
    public void processGrief(Set<Field> workingGrief)
    {
        int count = 0;

        if (plugin.settings.debugdb && !workingGrief.isEmpty())
        {
            PreciousStones.logger.info("[Queue] processing " + workingGrief.size() + " grief queries...");
        }

        for (Field field : workingGrief)
        {
            updateGrief(field);

            if (count >= plugin.settings.saveBatchSize)
            {
                if (plugin.settings.debugdb)
                {
                    PreciousStones.logger.info("[Queue] sent batch of " + count + " grief queries");
                }
                core.closeBatch();
                core.openBatch();
                count = 0;
            }
            count++;
        }

        if (plugin.settings.debugdb && count > 0)
        {
            PreciousStones.logger.info("[Queue] sent batch of " + count + " grief queries");
        }
    }
}