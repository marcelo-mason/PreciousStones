package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author phad
 */
public class VisualizationManager
{
    private PreciousStones plugin;
    private HashMap<String, List<Location>> visualizations = new HashMap<String, List<Location>>();

    /**
     *
     * @param plugin
     */
    public VisualizationManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Project the perimiter of a field to the player
     * @param player
     * @param field
     */
    public void visualize(final Player player, Field field)
    {
        List<Location> locs = visualizations.get(player.getName());

        if (locs == null)
        {
            locs = new ArrayList<Location>();
        }

        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2);
        int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2);

        for (int y = miny; y <= maxy; y++)
        {
            for (int z = minz; z <= maxz; z++)
            {
                int material = player.getWorld().getBlockTypeIdAt(minx, y, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), minx, y, z);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(maxx, y, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), maxx, y, z);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }
            }
        }

        for (int x = minx; x <= maxx; x++)
        {
            for (int z = minz; z <= maxz; z++)
            {
                int material = player.getWorld().getBlockTypeIdAt(x, miny, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, miny, z);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(x, maxy, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, maxy, z);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }
            }
        }

        for (int y = miny; y <= maxy; y++)
        {
            for (int x = minx; x <= maxx; x++)
            {
                int material = player.getWorld().getBlockTypeIdAt(x, y, minz);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, y, minz);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(x, y, maxz);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, y, maxz);
                    player.sendBlockChange(loc, Material.getMaterial(plugin.settings.visualizeBlock), (byte) 0);
                    locs.add(loc);
                }
            }
        }

        visualizations.put(player.getName(), locs);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                revertVisualization(player);
            }
        }, 20L * plugin.settings.visualizeSeconds);
    }

    /**
     * Undo the visualization
     * @param player
     */
    public void revertVisualization(Player player)
    {
        List<Location> locs = visualizations.get(player.getName());

        if (locs == null)
        {
            return;
        }

        for (Location loc : locs)
        {
            player.sendBlockChange(loc, Material.AIR, (byte) 0);
        }

        visualizations.remove(player.getName());
    }
}
