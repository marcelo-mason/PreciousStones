package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
     * Visualize and display a single field for 2 seconds
     *
     * @param player
     * @param field
     */
    public void visualizeSingleFieldFast(Player player, Field field)
    {
        addVisualizationField(player, field);
        displayVisualization(player, false, 2);
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
     * Reverts all current visualizations
     */
    public void revertAll()
    {
        for (String playerName : visualizations.keySet())
        {
            Visualization vis = visualizations.get(playerName);
            Player player = Bukkit.getServer().getPlayerExact(playerName);

            if (player != null)
            {
                Visualize visualize = new Visualize(vis.getBlocks(), player, true, false, 0);
            }
        }
        visualizations.clear();
        counts.clear();
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

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());

        if (data.getDensity() == 0)
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
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        Location loc = null;

        for (int x = minx; x <= maxx; x++)
        {
            int frame = (x == minx || x == maxx) ? 89 : frameType;

            loc = new Location(player.getWorld(), x, miny, maxz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), x, maxy, minz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), x, miny, minz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), x, maxy, maxz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }
        }

        for (int y = miny; y <= maxy; y++)
        {
            loc = new Location(player.getWorld(), minx, y, maxz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, 89, (byte) 0);
            }

            loc = new Location(player.getWorld(), maxx, y, minz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, 89, (byte) 0);
            }

            loc = new Location(player.getWorld(), minx, y, minz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, 89, (byte) 0);
            }

            loc = new Location(player.getWorld(), maxx, y, maxz);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, 89, (byte) 0);
            }
        }

        for (int z = minz; z <= maxz; z++)
        {
            int frame = (z == minz || z == maxz) ? 89 : frameType;

            loc = new Location(player.getWorld(), minx, maxy, z);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), maxx, miny, z);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), minx, miny, z);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }

            loc = new Location(player.getWorld(), maxx, maxy, z);
            if (Helper.isAirOrWater(loc))
            {
                vis.addBlock(loc, frame, (byte) 0);
            }
        }

        visualizations.put(player.getName(), vis);
    }

    private boolean turnCounter(String name, int size)
    {
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

    /**
     * Visualizes a single field's outline
     *
     * @param player
     * @param field
     */
    public void visualizeSingleOutline(Player player, Field field)
    {
        visualizeSingleOutline(player, field);
    }

    /**
     * Visualizes a single field's outline
     *
     * @param player
     * @param field
     */
    public void visualizeSingleOutline(Player player, Field field, boolean revert)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<BlockEntry> newBlocks = new ArrayList<BlockEntry>();

        int frameType = plugin.getSettingsManager().getVisualizeFrameBlock();

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        // add  the blocks for the new outline

        if (plugin.getSettingsManager().isVisualizationNewStyle())
        {
            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
            int spacing = ((Math.max(Math.max((maxx - minx), (maxy - miny)), (maxz - minz)) + 2) / data.getDensity()) + 1;

            for (int x = minx; x <= maxx; x++)
            {
                int frame = (x == minx || x == maxx) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), x, miny, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), x, maxy, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), x, miny, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), x, maxy, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }

            for (int y = miny; y <= maxy; y++)
            {
                int frame = (y == miny || y == maxy) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, y, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, y, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), minx, y, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, y, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }

            for (int z = minz; z <= maxz; z++)
            {
                int frame = (z == minz || z == maxz) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, maxy, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, miny, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), minx, miny, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, maxy, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }
        }
        else
        {
            for (int x = minx; x <= maxx; x++)
            {
                int frame = (x == minx || x == maxx) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), x, miny, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, maxy, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, miny, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, maxy, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }

            for (int y = miny; y <= maxy; y++)
            {
                int frame = (y == miny || y == maxy) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, y, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, y, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), minx, y, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, y, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }

            for (int z = minz; z <= maxz; z++)
            {
                int frame = (z == minz || z == maxz) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, maxy, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, miny, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), minx, miny, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, maxy, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }
        }

        // visualize all the new blocks that are left to visualize

        Visualize visualize = new Visualize(newBlocks, player, false, !revert, plugin.getSettingsManager().getVisualizeSeconds());
        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds a fields outline to a player's visualization buffer
     *
     * @param player
     * @param ce
     */
    public void displayFieldOutline(Player player, CuboidEntry ce)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<BlockEntry> oldBlocks = new ArrayList<BlockEntry>(vis.getOutlineBlocks());
        List<BlockEntry> newBlocks = new ArrayList<BlockEntry>();

        int frameType = plugin.getSettingsManager().getVisualizeFrameBlock();

        int offset = ce.selectedCount() > 1 ? 1 : 0;

        int minx = ce.getMinx() - offset;
        int miny = ce.getMiny() - offset;
        int minz = ce.getMinz() - offset;
        int maxx = ce.getMaxx() + offset;
        int maxy = ce.getMaxy() + offset;
        int maxz = ce.getMaxz() + offset;

        // add  the blocks for the new outline

        if (plugin.getSettingsManager().isVisualizationNewStyle())
        {
            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
            int spacing = ((Math.max(Math.max((maxx - minx), (maxy - miny)), (maxz - minz)) + 2) / data.getDensity()) + 1;

            for (int x = minx; x <= maxx; x++)
            {
                int frame = (x == minx || x == maxx) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), x, miny, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), x, maxy, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), x, miny, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
                loc = new Location(player.getWorld(), x, maxy, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }

            for (int y = miny; y <= maxy; y++)
            {
                int frame = (y == miny || y == maxy) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, y, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, y, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), minx, y, minz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, y, maxz);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }

            for (int z = minz; z <= maxz; z++)
            {
                int frame = (z == minz || z == maxz) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, maxy, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, miny, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), minx, miny, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }

                loc = new Location(player.getWorld(), maxx, maxy, z);
                if (Helper.isAirOrWater(loc))
                {
                    newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
                }
            }
        }
        else
        {
            for (int x = minx; x <= maxx; x++)
            {
                int frame = (x == minx || x == maxx) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), x, miny, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, maxy, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, miny, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), x, maxy, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }

            for (int y = miny; y <= maxy; y++)
            {
                int frame = (y == miny || y == maxy) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, y, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, y, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), minx, y, minz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, y, maxz);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }

            for (int z = minz; z <= maxz; z++)
            {
                int frame = (z == minz || z == maxz) ? 89 : frameType;

                Location loc = new Location(player.getWorld(), minx, maxy, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, miny, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), minx, miny, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));

                loc = new Location(player.getWorld(), maxx, maxy, z);
                newBlocks.add(new BlockEntry(loc, frame, (byte) 0));
            }
        }

        // revert the blocks that are no longer in the new set and should be reverted

        List<BlockEntry> revertible = new ArrayList<BlockEntry>(oldBlocks);
        revertible.removeAll(newBlocks);

        Visualize revert = new Visualize(revertible, player, true, false, plugin.getSettingsManager().getVisualizeSeconds());

        // visualize all the new blocks that are left to visualize

        List<BlockEntry> missing = new ArrayList<BlockEntry>(newBlocks);
        missing.removeAll(oldBlocks);

        Visualize visualize = new Visualize(missing, player, false, true, plugin.getSettingsManager().getVisualizeSeconds());

        vis.setOutlineBlocks(newBlocks);
        visualizations.put(player.getName(), vis);
    }

    /**
     * Whether the block is currently visualized as outline
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOutlineBlock(Player player, Block block)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        return vis.getOutlineBlocks().contains(new BlockEntry(block));
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
            for (int y = 0; y < 256; y++)
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
     * @param material
     * @param block
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
     * Revert a single a visualized block to the player
     *
     * @param player
     * @param block
     */
    public void revertSingle(Player player, Block block)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null)
        {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), block.getType(), (byte) 0);
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap)
    {
        displayVisualization(player, minusOverlap, plugin.getSettingsManager().getVisualizeSeconds());
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap, int seconds)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            if (minusOverlap)
            {
                for (Iterator<BlockEntry> iter = vis.getBlocks().iterator(); iter.hasNext(); )
                {
                    BlockEntry bd = iter.next();
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

                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            }
            else
            {
                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            }
        }
    }

    /**
     * Reverts any player's entire visualization buffer
     *
     * @param player
     */
    public void revert(Player player)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis.getBlocks(), player, true, false, 0);
        }
    }

    /**
     * Reverts the player's outline blocks
     *
     * @param player
     */
    public void revertOutline(Player player)
    {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null)
        {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis.getOutlineBlocks(), player, true, false, 0);
        }
    }
}
