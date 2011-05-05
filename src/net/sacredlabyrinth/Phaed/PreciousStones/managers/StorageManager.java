package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.AllowedEntry;

import org.bukkit.Material;
import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

/**
 *
 * @author phaed
 */
public final class StorageManager
{
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

                plugin.um.saveUnbreakable(ub);
             }

            PreciousStones.log(Level.INFO, "[{0}] < imported {1} unbreakables", plugin.getDescription().getName(), plugin.um.getCount());
        }
        catch (FileNotFoundException e)
        {
            PreciousStones.log(Level.SEVERE, "[{0}] Cannot read file {1}", plugin.getDescription().getName(), unbreakable.getName());
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
                    secsnitch = u[7].replace("?", "ï¿½");
                }

                if (u.length > 8)
                {
                    seccloak = u[8];
                }

                sectype = Helper.removeChar(sectype, '[');
                secname = Helper.removeChar(secname, ']');
                secsnitch = Helper.removeChar(secsnitch, ']');
                seccloak = Helper.removeChar(seccloak, ']');

                if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !(plugin.settings.isFieldType(sectype) || plugin.settings.isCloakableType(sectype)))
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

                List<String> temp = Arrays.asList(secallowed.split(","));
                List<AllowedEntry> allowed = new ArrayList<AllowedEntry>();

                for (String t : temp)
                {
                    if (t.trim().length() > 0)
                    {
                        allowed.add(new AllowedEntry(t, "all"));
                    }
                }

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

                if (block != null && !plugin.settings.isFieldType(block) && !(plugin.settings.isCloakableType(Material.getMaterial(type).getId()) && (plugin.settings.isCloakType(block) || plugin.settings.isCloakableType(block))))
                {
                    PreciousStones.log(Level.WARNING, "orphan field - skipping {0}", new Vec(block).toString());
                    continue;
                }

                Field field = new Field(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]), world, Material.getMaterial(type).getId(), owner, name);
                field.setAllowed(allowed);

                plugin.ffm.saveField(field);
            }

            PreciousStones.log(Level.INFO, "[{0}] < imported {1} fields", plugin.getDescription().getName(), plugin.ffm.getCount());
        }
        catch (FileNotFoundException e)
        {
            PreciousStones.log(Level.SEVERE, "[{0}] Cannot read file {1}", plugin.getDescription().getName(), forcefield.getName());
        }
    }
}
