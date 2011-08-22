package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Visualization;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author phad
 */
public class VisualizationManager
{
    private PreciousStones plugin;
    private HashMap<String, Visualization> visualizations = new HashMap<String, Visualization>();

    /**
     *
     * @param plugin
     */
    public VisualizationManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Adds a fields perimeter to a player's visualization buffer
     * @param player
     * @param field
     */
    public void addVisualizationField(Player player, Field field)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        vis.addField(field);

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - ((int) Math.floor(((double) Math.max(field.getHeight() - 1, 0)) / 2)) - 1;
        int maxy = field.getY() + ((int) Math.ceil(((double) Math.max(field.getHeight() - 1, 0)) / 2)) + 1;

        for (int y = miny; y <= maxy; y++)
        {
            for (int z = minz; z <= maxz; z++)
            {
                int material = player.getWorld().getBlockTypeIdAt(minx, y, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), minx, y, z);
                    vis.addLocation(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(maxx, y, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), maxx, y, z);
                    vis.addLocation(loc);
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
                    vis.addLocation(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(x, maxy, z);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, maxy, z);
                    vis.addLocation(loc);
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
                    vis.addLocation(loc);
                }

                material = player.getWorld().getBlockTypeIdAt(x, y, maxz);

                if (material == 0)
                {
                    Location loc = new Location(player.getWorld(), x, y, maxz);
                    vis.addLocation(loc);
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    public void addFieldMark(Player player, Field field)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        vis.addField(field);

        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            for (int y = 0; y < 128; y++)
            {
                int typeid = world.getBlockTypeIdAt(field.getX(), y, field.getZ());

                if (typeid == 0)
                {
                    vis.addLocation(new Location(world, field.getX(), y, field.getZ()));
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     * @param player
     */
    public void displayVisualization(final Player player, boolean minusOverlap)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            return;
        }

        List<Location> batch = new LinkedList<Location>();
        int delay = 0;

        for (Location loc : vis.getLocs())
        {
            boolean skip = false;

            if (minusOverlap)
            {
                for (Field field : vis.getFields())
                {
                    if (field.envelops(loc))
                    {
                        skip = true;
                        break;
                    }
                }
            }

            if (!skip)
            {
                batch.add(loc);
            }

            if (batch.size() >= plugin.getSettingsManager().getVisualizeBatchSize())
            {
                sendBatch(batch, player, Material.getMaterial(plugin.getSettingsManager().getVisualizeBlock()), delay);

                batch = new LinkedList<Location>();
                delay += plugin.getSettingsManager().getVisualizeBatchDelayTicks();
            }
        }

        if (!batch.isEmpty())
        {
            sendBatch(batch, player, Material.getMaterial(plugin.getSettingsManager().getVisualizeBlock()), delay);
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                revertVisualization(player);
            }
        }, 20L * plugin.getSettingsManager().getVisualizeSeconds() + delay);
    }

    /**
     * Undo the visualization
     * @param player
     */
    public void revertVisualization(Player player)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            return;
        }

        ChatBlock.sendMessage(player, ChatColor.AQUA + "Reverting visualization...");

        List<Location> batch = new LinkedList<Location>();
        int delay = 0;

        for (Location loc : vis.getLocs())
        {
            batch.add(loc);

            if (batch.size() >= plugin.getSettingsManager().getVisualizeBatchSize())
            {
                sendBatch(batch, player, Material.AIR, delay);

                batch = new LinkedList<Location>();
                delay += plugin.getSettingsManager().getVisualizeBatchDelayTicks();
            }
        }

        if (!batch.isEmpty())
        {
            sendBatch(batch, player, Material.AIR, delay);
        }

        visualizations.remove(player.getName());
    }

    private void sendBatch(final List<Location> locs, final Player player, final Material mat, int delay)
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                for (Location loc : locs)
                {
                    player.sendBlockChange(loc, mat, (byte) 0);
                }
            }
        }, delay);
    }
}
