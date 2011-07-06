package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        startScheduler();
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

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(255) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(255) NOT NULL,  `name` varchar(255) NOT NULL,  `packed_allowed` text NOT NULL,  `packed_snitch_list` longtext NOT NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_fields_1` (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(255) default NULL,  `owner` varchar(255) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_unbreakables_1` (`x`,`y`,`z`,`world`));";
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

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20),  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(255) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(255) NOT NULL,  `name` varchar(255) NOT NULL,  `packed_allowed` text NOT NULL,  `packed_snitch_list` longtext NOT NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
                    core.createTable(query);
                }

                if (!core.checkTable("pstone_unbreakables"))
                {
                    PreciousStones.log(Level.INFO, "Creating table: pstone_unbreakables");

                    String query = "CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20),  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(255) default NULL,  `owner` varchar(255) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE (`x`,`y`,`z`,`world`));";
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
                        field.setPackedSnitchList(packed_snitch_list);

                        out.add(field);
                    }
                    catch (Exception ex)
                    {
                        ex.getStackTrace();
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
                        ex.getStackTrace();
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
        String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + field.getWorld() + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + field.getName() + "','" + field.getPackedAllowed() + "','" + field.getPackedSnitchList() + "');";
        core.insertQuery(query + values);
    }

    /**
     * Update a field from the database
     * @param field
     */
    public void updateField(Field field)
    {
        String query = "UPDATE `pstone_fields` SET radius = " + field.getRadius() + ", height = " + field.getHeight() + ", velocity = " + field.getVelocity() + ", owner = '" + field.getOwner() + "', name = '" + field.getName() + "', packed_allowed = '" + field.getPackedAllowed() + "', packed_snitch_list = '" + field.getPackedSnitchList() + "' WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        core.updateQuery(query);
    }

    /**
     * Delete a fields form the database
     * @param field
     */
    public void deleteField(Field field)
    {
        String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
        core.deleteQuery(query);
    }

    /**
     * Insert an unbreakable into the database
     * @param ub
     */
    public void insertUnbreakable(Unbreakable ub)
    {
        String query = "INSERT INTO `pstone_unbreakables` (  `x`,  `y`, `z`, `world`, `owner`, `type_id`) ";
        String values = "VALUES ( " + ub.getX() + "," + ub.getY() + "," + ub.getZ() + ",'" + ub.getWorld() + "','" + ub.getOwner() + "'," + ub.getTypeId() + ");";
        core.insertQuery(query + values);
    }

    /**
     * Delete a fields form the database
     * @param x
     * @param y
     * @param z
     * @param world
     */
    public void deleteField(int x, int y, int z, String world)
    {
        String query = "DELETE FROM `pstone_fields` WHERE WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = '" + world + "';";
        core.deleteQuery(query);
    }

    /**
     * Delete an unbreakabale form the database
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        String query = "DELETE FROM `pstone_unbreakables` WHERE x = " + ub.getX() + " AND y = " + ub.getY() + " AND z = " + ub.getZ() + " AND world = '" + ub.getWorld() + "';";
        core.deleteQuery(query);
    }

    /**
     *
     */
    public void startScheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.ffm.updateAll();

                        PreciousStones.log(Level.INFO, "data saved.");
                    }
                }, 0, 20L * 60 * plugin.settings.saveFrequency);
            }
        }, 20L * 60 * plugin.settings.saveFrequency);
    }

    /**
     * Imports unbreakables from old save files
     */
    public void loadUnbreakables()
    {
        int linecount = 0;

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

                    Unbreakable ub = new Unbreakable(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), world, Material.getMaterial(type).getId(), owner);
                    plugin.um.addToCollection(ub);
                    insertUnbreakable(ub);
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
                        secsnitch = u[7].replace("?", "ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â½");
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

                    Field field = new Field(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]), 0, world, Material.getMaterial(type).getId(), owner, name);

                    plugin.ffm.addToCollection(field);
                    insertField(field);
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
}
