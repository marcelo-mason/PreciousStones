package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author phaed
 */
public class LegacyManager
{
    private PreciousStones plugin;
    private File unbreakable;
    private File forcefield;

    /**
     *
     * @param plugin
     */
    public LegacyManager()
    {
        plugin = PreciousStones.getInstance();

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

                    if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !plugin.getSettingsManager().isUnbreakableType(sectype))
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

                    if (block != null && !plugin.getSettingsManager().isUnbreakableType(block))
                    {
                        PreciousStones.log(Level.WARNING, "orphan unbreakable - skipping {0}", new Vec(block).toString());
                        continue;
                    }

                    Unbreakable ub = new Unbreakable(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), world, Material.getMaterial(type).getId(), owner);

                    plugin.getStorageManager().offerUnbreakable(ub, true);
                    plugin.getUnbreakableManager().addToCollection(ub);
                }
                catch (Exception ex)
                {
                    PreciousStones.log(Level.WARNING, "Corrupt unbreakable could not be imported");
                }
            }
            PreciousStones.log(Level.INFO, "< imported {0} unbreakables", plugin.getUnbreakableManager().getCount());
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
                        secsnitch = u[7].replace("?", "ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¾ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â½");
                    }

                    if (u.length > 8)
                    {
                        seccloak = u[8];
                    }

                    sectype = Helper.removeChar(sectype, '[');
                    secname = Helper.removeChar(secname, ']');
                    secsnitch = Helper.removeChar(secsnitch, ']');
                    seccloak = Helper.removeChar(seccloak, ']');

                    if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !(plugin.getSettingsManager().isFieldType(sectype)))
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

                    if (block != null && !plugin.getSettingsManager().isFieldType(block))
                    {
                        PreciousStones.log(Level.WARNING, "orphan field - skipping {0}", new Vec(block).toString());
                        continue;
                    }

                    Field field = new Field(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]), 0, world, Material.getMaterial(type).getId(), owner, name, 0);


                    FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field.getTypeId());

                    if (fs == null)
                    {
                        continue;
                    }

                    field.setSettings(fs);

                    plugin.getStorageManager().offerField(field);
                    plugin.getForceFieldManager().addToCollection(field);
                }
                catch (Exception ex)
                {
                    PreciousStones.log(Level.WARNING, "Corrupt field could not be imported");
                }
            }

            PreciousStones.log(Level.INFO, "< imported {0} fields", plugin.getForceFieldManager().getCount());
        }
        catch (FileNotFoundException e)
        {
            PreciousStones.log(Level.SEVERE, "Cannot read file {0}", forcefield.getName());
        }
    }
}
