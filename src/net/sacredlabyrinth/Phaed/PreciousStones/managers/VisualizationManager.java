package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author phad
 */
public class VisualizationManager
{
    private PreciousStones plugin;
    private HashMap<String, Integer> counts = new HashMap<String, Integer>();
    private HashMap<String, Visualization> visualizations = new HashMap<String, Visualization>();

    /**
     *
     */
    public VisualizationManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Visualize and display a single field
     *
     * @param player
     * @param field
     */
    public void visualizeSingleField(Player player, Field field)
    {
        addVisualizationField(player, field);
        displayVisualization(player, false);
    }

    /**
     * If the player is in the middle of a visualization
     *
     * @param player
     * @return
     */
    public boolean pendingVisualization(Player player)
    {
        return visualizations.containsKey(player.getName());
    }

    /**
     * Adds a fields perimeter to a player's visualization buffer
     *
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

        if (plugin.getCuboidManager().hasOpenCuboid(player))
        {
            return;
        }

        vis.addField(field);

        int visualizationType = field.hasFlag(FieldFlag.CUBOID) ? plugin.getSettingsManager().getCuboidVisualizationType() : plugin.getSettingsManager().getVisualizeBlock();
        int frameType = plugin.getSettingsManager().getVisualizeFrameBlock();

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - ((int) Math.floor(((double) Math.max(field.getHeight() - 1, 0)) / 2)) - 1;
        int maxy = field.getY() + ((int) Math.ceil(((double) Math.max(field.getHeight() - 1, 0)) / 2)) + 1;

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        int total = (maxx - minx) + (maxy + miny) + (maxz - minz);
        int size = total / plugin.getSettingsManager().getVisualizeSpreadDivisor();

        for (int x = minx; x <= maxx; x++)
        {
            Location loc = new Location(player.getWorld(), x, maxy, maxz);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), x, miny, minz);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), x, miny, minz);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), x, maxy, maxz);
            vis.addBlock(loc, frameType, (byte) 0);
        }

        for (int y = miny; y <= maxy; y++)
        {
            Location loc = new Location(player.getWorld(), minx, y, maxz);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), maxx, y, miny);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), minx, y, minz);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), maxx, y, maxz);
            vis.addBlock(loc, frameType, (byte) 0);
        }

        for (int z = minz; z <= maxz; z++)
        {
            Location loc = new Location(player.getWorld(), minx, maxy, z);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), maxx, miny, z);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), minx, miny, z);
            vis.addBlock(loc, frameType, (byte) 0);

            loc = new Location(player.getWorld(), maxx, maxy, z);
            vis.addBlock(loc, frameType, (byte) 0);
        }

        resetCounter(player.getName() + 1);
        resetCounter(player.getName() + 2);

        for (int y = miny; y <= maxy; y++)
        {
            if (turnCounter(player.getName() + 1, size))
            {
                for (int z = minz; z <= maxz; z++)
                {
                    if (turnCounter(player.getName() + 2, size))
                    {
                        Location loc = new Location(player.getWorld(), minx, y, z);
                        vis.addBlock(loc, visualizationType, (byte) 0);

                        loc = new Location(player.getWorld(), maxx, y, z);
                        vis.addBlock(loc, visualizationType, (byte) 0);
                    }
                }
            }
        }

        resetCounter(player.getName() + 1);
        resetCounter(player.getName() + 2);

        for (int x = minx; x <= maxx; x++)
        {
            if (turnCounter(player.getName() + 1, size))
            {
                for (int z = minz; z <= maxz; z++)
                {
                    if (turnCounter(player.getName() + 2, size))
                    {
                        Location loc = new Location(player.getWorld(), x, miny, z);
                        vis.addBlock(loc, visualizationType, (byte) 0);

                        loc = new Location(player.getWorld(), x, maxy, z);
                        vis.addBlock(loc, visualizationType, (byte) 0);
                    }
                }
            }
        }

        resetCounter(player.getName() + 1);
        resetCounter(player.getName() + 2);

        for (int y = miny; y <= maxy; y++)
        {
            if (turnCounter(player.getName() + 1, size))
            {
                for (int x = minx; x <= maxx; x++)
                {
                    if (turnCounter(player.getName() + 2, size))
                    {
                        Location loc = new Location(player.getWorld(), x, y, minz);
                        vis.addBlock(loc, visualizationType, (byte) 0);

                        loc = new Location(player.getWorld(), x, y, maxz);
                        vis.addBlock(loc, visualizationType, (byte) 0);
                    }
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    private boolean turnCounter(String name, int size)
    {
        if (size == 0)
        {
            return true;
        }

        if (counts.containsKey(name))
        {
            int count = counts.get(name);
            count += 1;

            if (count >= size)
            {
                counts.put(name, 0);
                return true;
            }

            counts.put(name, count);
        }
        else
        {
            counts.put(name, 1);
        }

        return false;
    }

    private void resetCounter(String name)
    {
        counts.put(name, 0);
    }

    /**
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
                int typeId = world.getBlockTypeIdAt(field.getX(), y, field.getZ());

                if (plugin.getSettingsManager().isThroughType(typeId))
                {
                    vis.addBlock(new Location(world, field.getX(), y, field.getZ()), plugin.getSettingsManager().getVisualizeMarkBlock(), (byte) 0);
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds and displays a visualized block to the player
     *
     * @param player
     * @param field
     */
    public void displaySingle(Player player, Material material, Block block)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), material, (byte) 0);
    }


    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            if (minusOverlap)
            {
                for (Iterator<BlockData> iter = vis.getBlocks().iterator(); iter.hasNext(); )
                {
                    BlockData bd = iter.next();
                    Location loc = bd.getLocation();

                    for (Field field : vis.getFields())
                    {
                        if (field.envelops(loc))
                        {
                            iter.remove();
                            break;
                        }
                    }
                }

                Visualize visualize = new Visualize(vis, player);
            }
            else
            {
                Visualize visualize = new Visualize(vis, player);
            }
        }
    }

    /**
     * Reverts any player's entire visualization buffer
     *
     * @param player
     */
    public void revertVisualization(Player player)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis, player, true);
        }
    }
}
