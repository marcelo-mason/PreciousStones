package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Visualization;
import net.sacredlabyrinth.Phaed.PreciousStones.Visualize;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    /**
     *
     * @param player
     * @param field
     */
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
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap)
    {
        Visualization vis = visualizations.get(player.getName());
        Material material = Material.getMaterial(plugin.getSettingsManager().getVisualizeBlock());

        if (vis != null && !vis.isRunning())
        {
            vis.setRunning(true);

            if (minusOverlap)
            {
                Queue<Location> subset = new LinkedList<Location>();

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
                        subset.add(loc);
                    }
                }

                Visualize visualize = new Visualize(vis, subset, material, player, false);
            }
            else
            {
                Visualize visualize = new Visualize(vis, vis.getLocs(), material, player, false);
            }
        }
    }

    /**
     * Undo the visualization
     * @param player
     */
    public void revertVisualization(Player player)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Reverting visualization...");

            Visualize visualize = new Visualize(vis, vis.getLocs(), Material.AIR, player, true);

            visualizations.remove(player.getName());
        }
    }
}
