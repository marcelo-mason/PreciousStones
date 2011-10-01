package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class CuboidManager
{
    private PreciousStones plugin;
    private HashMap<String, CuboidEntry> openCuboids = new HashMap<String, CuboidEntry>();

    /**
     *
     */
    public CuboidManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Whether the player has a currently open cuboid definition
     *
     * @param player
     * @return
     */
    public boolean hasOpenCuboid(Player player)
    {
        return openCuboids.containsKey(player.getName());
    }

    /**
     * If there is a currently open cuboid definition emanating from the field block or its parent
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboid(Player player, Block block)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            Field field = ce.getField();

            if (Helper.isSameBlock(field.getLocation(), block.getLocation()))
            {
                return true;
            }

            for (Field child : field.getChildren())
            {
                if (Helper.isSameBlock(child.getLocation(), block.getLocation()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * REturns the currently open cuboid definition if any
     *
     * @param player
     * @return
     */
    public CuboidEntry getOpenCuboid(Player player)
    {
        return openCuboids.get(player.getName());
    }

    /**
     * Adds a single block to the cuboid definition
     *
     * @param player
     * @param block
     */
    public void processSelectedBlock(Player player, Block block)
    {
        if (openCuboids.containsKey(player.getName()))
        {
            CuboidEntry ce = openCuboids.get(player.getName());

            int oldVolume = ce.getAvailableVolume();

            if (ce.isSelected(block))
            {
                ce.removeSelected(block);
                plugin.getVisualizationManager().displaySingle(player, block.getType(), block);
            }
            else
            {
                ce.addSelected(block);
                plugin.getVisualizationManager().displaySingle(player, Material.getMaterial(plugin.getSettingsManager().getCuboidDefiningType()), block);
            }

            int newVolume = ce.getAvailableVolume();

            if (newVolume != oldVolume)
            {
                plugin.getVisualizationManager().displayFieldOutline(player, ce);
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Available protection: " + ChatColor.YELLOW + newVolume + " blocks");
            }
        }
    }


    /**
     * Initiates a cuboid definition
     *
     * @param player
     * @param field
     */
    public void openCuboid(final Player player, final Field field)
    {
        final CuboidEntry ce = new CuboidEntry(field);
        openCuboids.put(player.getName(), ce);

        plugin.getVisualizationManager().revertVisualization(player);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                ce.addSelected(field.getBlock());

                for (Field child : field.getChildren())
                {
                    ce.addSelected(child.getBlock());
                }

                PreciousStones.getInstance().getForceFieldManager().removeSourceField(field);
                plugin.getVisualizationManager().displayFieldOutline(player, ce);

                ChatBlock.sendMessage(player, ChatColor.AQUA + "You are in drawing mode. Click on the block to finish.");
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Available protection: " + ChatColor.YELLOW + ce.getAvailableVolume() + " blocks");
            }
        }, 1L);
    }

    /**
     * Adds a child field to the currently open cuboid definition
     *
     * @param player
     * @param field
     */
    public void openChild(final Player player, final Field field)
    {
        final CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    ce.addSelected(field.getBlock());
                    PreciousStones.getInstance().getForceFieldManager().addSourceField(field);
                    ChatBlock.sendMessage(player, ChatColor.AQUA + "Available protection: " + ChatColor.YELLOW + ce.getAvailableVolume() + " blocks");
                }
            }, 1L);
        }
    }

    /**
     * End the open cuboid definition cleanly
     *
     * @param player
     * @return
     */
    public boolean closeCuboid(final Player player)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            plugin.getVisualizationManager().revertVisualization(player);
            plugin.getVisualizationManager().revertOutline(player);

            final Field field = ce.getField();

            if (ce.isExceeded())
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid exceeds maximum size");
                openCuboids.remove(player.getName());
                return false;
            }

            ce.finalizeField();

            Block foundBlock = plugin.getUnprotectableManager().existsUnprotectableBlock(field);

            if (foundBlock != null)
            {
                if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.getCommunicationManager().warnPlaceFieldInUnprotectable(player, foundBlock, field.getBlock());
                    ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid has been cancelled.");
                    openCuboids.remove(player.getName());
                    return false;
                }
            }

            plugin.getForceFieldManager().addSourceField(field);

            openCuboids.remove(player.getName());
            plugin.getVisualizationManager().visualizeSingleFieldFast(player, field);
            plugin.getStorageManager().offerField(field);
            plugin.getCommunicationManager().notifyPlaceCuboid(player, field);
            return true;
        }

        return false;
    }

    /**
     * Cancels a currently open cuboid definition
     *
     * @param block
     */
    public void cancelOpenCuboid(Player player, Block block)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            Field field = ce.getField();

            if (Helper.isSameBlock(field.getLocation(), block.getLocation()))
            {
                cancelOpenCuboid(player);
            }

            for (Field child : field.getChildren())
            {
                if (Helper.isSameBlock(child.getLocation(), block.getLocation()))
                {
                    cancelOpenCuboid(player);
                }
            }
        }
    }

    private void cancelOpenCuboid(Player player)
    {
        plugin.getVisualizationManager().revertVisualization(player);
        plugin.getVisualizationManager().revertOutline(player);
        openCuboids.remove(player.getName());
        ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid has been cancelled.");
    }
}
