package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.Phaed.PreciousStones.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.data.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import net.sacredlabyrinth.Phaed.PreciousStones.data.mysqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.data.sqlCore;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import org.bukkit.block.Sign;
import java.util.concurrent.*;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;

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
    private File unbreakable;
    private File forcefield;
    private boolean running;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public StorageManager(PreciousStones plugin)
    {
        this.plugin = plugin;

        unbreakable = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakables.txt");
        forcefield = new File(plugin.getDataFolder().getPath() + File.separator + "forcefields.txt");

        if (unbreakable.exists())
        {
            loadUnbreakables();
            unbreakable.delete();
        }

        if (forcefield.exists())
        {
            loadFields();
            forcefield.delete();
        }

        initiateDB();
        loadWorldData();
        saveScheduler();
        sqlScheduler();
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

            field.setDirty(false);

            plugin.ffm.addToCollection(field);

            if (fieldsettings.griefUndoInterval)
            {
                plugin.gum.add(field);
            }
        }

        PreciousStones.log(Level.INFO, "{0} fields: {1}", world, fields.size());
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

        PreciousStones.log(Level.INFO, "{0} unbreakables: {1}", world, unbreakables.size());
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

                        Field field = new Field(id, x, y, z, radius, height, velocity, world, type_id, owner, name);
                        field.setPackedAllowed(packed_allowed);
                        field.setPackedSnitchList(packed_snitch_list);

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

                        Unbreakable ub = new Unbreakable(id, x, y, z, world, type_id, owner);

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
     * Insert a field into the database
     * @param field
     */
    public void insertField(Field field)
    {
        String query = "INSERT INTO `pstone_fields` (  `x`,  `y`, `z`, `world`, `radius`, `height`, `velocity`, `type_id`, `owner`, `name`, `packed_allowed`, `packed_snitch_list`) ";
        String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + field.getName() + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + Helper.escapeQuotes(field.getPackedSnitchList()) + "');";
        queue.add(query + values);
    }

    /**
     * Update a field from the database
     * @param field
     */
    public void updateField(Field field)
    {
        String query = "UPDATE `pstone_fields` SET radius = " + field.getRadius() + ", height = " + field.getHeight() + ", velocity = " + field.getVelocity() + ", owner = '" + field.getOwner() + "', name = '" + field.getName() + "', packed_allowed = '" + Helper.escapeQuotes(field.getPackedAllowed()) + "', packed_snitch_list = '" + Helper.escapeQuotes(field.getPackedSnitchList()) + "' WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        queue.add(query);
    }

    /**
     * Delete a fields form the database
     * @param field
     */
    public void deleteField(Field field)
    {
        String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        queue.add(query);
    }

    /**
     * Insert an unbreakable into the database
     * @param ub
     */
    public void insertUnbreakable(Unbreakable ub)
    {
        String query = "INSERT INTO `pstone_unbreakables` (  `x`,  `y`, `z`, `world`, `owner`, `type_id`) ";
        String values = "VALUES ( " + ub.getX() + "," + ub.getY() + "," + ub.getZ() + ",'" + ub.getWorld() + "','" + ub.getOwner() + "'," + ub.getTypeId() + ");";
        queue.add(query + values);
    }

    /**
     * Delete an unbreakabale form the database
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE x = " + ub.getX() + " AND y = " + ub.getY() + " AND z = " + ub.getZ() + " AND world = '" + ub.getWorld() + "';";
        queue.add(query);
    }

    /**
     * Record a single block grief
     * @param field
     * @param block
     */
    public void recordBlockGrief(Field field, Block block)
    {
        if (block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR))
        {
            if ((block.getData() & 0x8) == 0x8)
            {
                Block bottom = block.getRelative(BlockFace.DOWN);
                recordBlockGriefClean(field, bottom);
            }
            else
            {
                Block top = block.getRelative(BlockFace.UP);
                recordBlockGriefClean(field, top);
            }
        }

        recordBlockGriefClean(field, block);
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
        queue.add(query + values);
    }

    /**
     * Restores a field's griefed blocks
     * @param field
     * @return
     */
    public List<GriefBlock> retrieveBlockGrief(Field field)
    {
        processQueue();

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

        query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        core.deleteQuery(query);

        return out;
    }

    /**
     * Deletes all records form a specific field
     * @param field
     */
    public void deleteBlockGrief(Field field)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        queue.add(query);
    }

    /**
     * Deletes all records form a specific block
     * @param field
     */
    public void deleteBlockGrief(Block block)
    {
        String query = "DELETE FROM `pstone_grief_undo` WHERE x = " + block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND world = '" + block.getWorld().getName() + "';";
        queue.add(query);
    }

    /**
     * Imports unbreakables from old save files
     */
    public void loadUnbreakables()
    {
        int linecount = 0;
        int id = 1;

        Scanner scan;
        try
        {
            scan = new Scanner(unbreakable);

            while (scan.hasNextLine())
            {
                try
                {
                    linecount++;

                    String line = scan.nextLine();

                    if (!line.contains("["))
                    {
                        continue;
                    }

                    String[] u = line.split("\\|");

                    if (u.length < 5)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt unbreakable: seccount{0} line {1}", u.length, linecount);
                        continue;
                    }

                    String sectype = u[0];
                    String secowner = u[1];
                    String secworld = u[2];
                    String secchunk = u[3];
                    String secvec = u[4];

                    sectype = Helper.removeChar(sectype, '[');
                    secvec = Helper.removeChar(secvec, ']');

                    if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !plugin.settings.isUnbreakableType(sectype))
                    {
                        PreciousStones.log(Level.WARNING, " Corrupt unbreakable: type error {0}", linecount);
                        continue;
                    }

                    String type = sectype;

                    if (secowner.trim().length() == 0)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt unbreakable: owner error {0}", linecount);
                        continue;
                    }

                    String owner = secowner;

                    if (secworld.trim().length() == 0)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt unbreakable: owner error {0}", linecount);
                        continue;
                    }

                    String world = secworld;

                    String[] chunk = secchunk.split(",");

                    if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt unbreakable: chunk error {0}", linecount);
                        continue;
                    }

                    String[] vec = secvec.split(",");

                    if (vec.length < 3 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]))
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt unbreakable: vec error {0}", linecount);
                        continue;
                    }

                    Block block = null;

                    if (plugin.getServer().getWorld(world) != null)
                    {
                        block = plugin.getServer().getWorld(world).getBlockAt(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]));
                    }

                    if (block != null && !plugin.settings.isUnbreakableType(block))
                    {
                        PreciousStones.log(Level.WARNING, "orphan unbreakable - skipping {0}", new Vec(block).toString());
                        continue;
                    }

                    Unbreakable ub = new Unbreakable(id, Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), world, Material.getMaterial(type).getId(), owner);
                    id++;

                    insertUnbreakable(ub);
                    plugin.um.addToCollection(ub);
                }
                catch (Exception ex)
                {
                    PreciousStones.log(Level.WARNING, "Corrupt unbreakable could not be imported");
                }
            }
            PreciousStones.log(Level.INFO, "< imported {0} unbreakables", plugin.um.getCount());
        }
        catch (FileNotFoundException e)
        {
            PreciousStones.log(Level.SEVERE, "Cannot read file {0}", unbreakable.getName());
        }
    }

    /**
     * Imports fields from old save files
     */
    public void loadFields()
    {
        int linecount = 0;
        long id = 0;

        Scanner scan;
        try
        {
            scan = new Scanner(forcefield);

            while (scan.hasNextLine())
            {
                try
                {
                    linecount++;

                    String line = scan.nextLine();

                    if (!line.contains("["))
                    {
                        continue;
                    }

                    String[] u = line.split("\\|");

                    if (u.length < 7)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield: seccount{0} line {1}", u.length, linecount);
                        continue;
                    }

                    String sectype = u[0];
                    String secowner = u[1];
                    String secallowed = u[2];
                    String secworld = u[3];
                    String secchunk = u[4];
                    String secvec = u[5];
                    String secname = u[6];
                    String secsnitch = "";
                    String seccloak = "";

                    if (u.length > 7)
                    {
                        secsnitch = u[7].replace("?", "ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â½");
                    }

                    if (u.length > 8)
                    {
                        seccloak = u[8];
                    }

                    sectype = Helper.removeChar(sectype, '[');
                    secname = Helper.removeChar(secname, ']');
                    secsnitch = Helper.removeChar(secsnitch, ']');
                    seccloak = Helper.removeChar(seccloak, ']');

                    if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !(plugin.settings.isFieldType(sectype)))
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield : type error {0}", linecount);
                        continue;
                    }

                    String type = sectype;

                    if (secowner.trim().length() == 0)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield: type error {0}", linecount);
                        continue;
                    }

                    String owner = secowner;

                    if (secworld.trim().length() == 0)
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield : world error {0}", linecount);
                        continue;
                    }

                    String world = secworld;

                    String[] chunk = secchunk.split(",");

                    if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield: chunk error {0}", linecount);
                        continue;
                    }

                    String[] vec = secvec.split(",");

                    if (vec.length < 5 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]) || !Helper.isInteger(vec[3]) || !Helper.isInteger(vec[4]))
                    {
                        PreciousStones.log(Level.WARNING, "Corrupt forcefield: vec error {0}", linecount);
                        continue;
                    }

                    String name = secname;

                    Block block = null;

                    if (plugin.getServer().getWorld(world) != null)
                    {
                        block = plugin.getServer().getWorld(world).getBlockAt(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]));
                    }

                    // if the field is a cloakable field yet the material type is neither a cloaked or clokable material (means its corrupted) then we orphan it
                    // otherwise if the field is not a field type, then we orphan it as well.

                    if (block != null && !plugin.settings.isFieldType(block))
                    {
                        PreciousStones.log(Level.WARNING, "orphan field - skipping {0}", new Vec(block).toString());
                        continue;
                    }

                    Field field = new Field(id, Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]), 0, world, Material.getMaterial(type).getId(), owner, name);
                    id++;

                    insertField(field);
                    plugin.ffm.addToCollection(field);
                }
                catch (Exception ex)
                {
                    PreciousStones.log(Level.WARNING, "Corrupt field could not be imported");
                }
            }

            PreciousStones.log(Level.INFO, "< imported {0} fields", plugin.ffm.getCount());
        }
        catch (FileNotFoundException e)
        {
            PreciousStones.log(Level.SEVERE, "Cannot read file {0}", forcefield.getName());
        }
    }

    private void saveScheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                plugin.ffm.updateAll();

                PreciousStones.log(Level.INFO, "data saved.");
            }
        }, 20L * 60 * plugin.settings.saveFrequency, 20L * 60 * plugin.settings.saveFrequency);
    }

    private void sqlScheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                if (running)
                {
                    return;
                }

                running = true;
                processQueue();
                running = false;
            }
        }, 20L * 5, 20L * 5);
    }

    private void processQueue()
    {
        try
        {
            int batchSize = 0;
            core.openBatch();

            while (!queue.isEmpty())
            {
                String query = queue.take();
                core.addBatch(query);
                batchSize++;

                if (batchSize > 100)
                {
                    core.closeBatch();
                    core.openBatch();
                    batchSize = 0;
                }
            }

            core.closeBatch();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
