package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

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
     * If the block is a cuboid field or one of its children
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboid(Player player, Block block)
    {
        return isOpenCuboidField(player, block) || isOpenCuboidChild(player, block);
    }

    /**
     * If there is a currently open cuboid definition emanating from the field block
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboidField(Player player, Block block)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            Field field = ce.getField();

            if (field != null)
            {
                if (Helper.isSameBlock(field.getLocation(), block.getLocation()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * If the block belongs to a child field inside an open cuboid definition
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboidChild(Player player, Block block)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            Field field = ce.getField();

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
     * Returns the players open cuboid definition if it exists
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
    public boolean processSelectedBlock(Player player, Block block)
    {
        CuboidEntry openCuboid = getOpenCuboid(player);

        if (!plugin.getVisualizationManager().isOutlineBlock(player, block))
        {
            if (openCuboid.testOverflow(block.getLocation()) || plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid"))
            {
                if (plugin.getWorldGuardManager().canBuild(player, block.getLocation()))
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

                            if (newVolume > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Available protection: " + ChatColor.YELLOW + newVolume + " blocks");
                            }
                            else
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Available protection: " + ChatColor.RED + newVolume + " blocks (bypassing)");
                            }
                        }
                    }
                    return true;
                }
                else
                {
                    ChatBlock.sendMessage(player, ChatColor.RED + "Cannot extend inside WorldGuard region");
                }
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid exceeds available volume");
            }
        }
        else
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "Cannot click on the outline");
        }

        return false;
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

        field.setOpen(true);
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
            final Field field = ce.getField();

            if (ce.isExceeded())
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid"))
                {
                    ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid exceeds available volume");
                    cancelOpenCuboid(player);
                    return false;
                }
            }

            if (plugin.getForceFieldManager().fieldConflicts(ce, player) != null)
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "The cuboid conflicts with someone else's field");
                cancelOpenCuboid(player);
                return false;
            }

            List<Vector> corners = field.getCorners();
            corners.add(field.getLocation().toVector());

            for (Vector corner : corners)
            {
                Location location = corner.toLocation(player.getWorld());

                if(!plugin.getWorldGuardManager().canBuild(player, location))
                {
                    ChatBlock.sendMessage(player, ChatColor.RED + "The cuboid conflicts with a worldguard region");
                    cancelOpenCuboid(player);
                    return false;
                }
            }

            if (field.hasFlag(FieldFlag.PREVENT_UNPROTECTABLE))
            {
                Block foundBlock = plugin.getUnprotectableManager().existsUnprotectableBlock(field);

                if (foundBlock != null)
                {
                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                    {
                        plugin.getCommunicationManager().warnPlaceFieldInUnprotectable(player, foundBlock, field.getBlock());
                        cancelOpenCuboid(player);
                        return false;
                    }
                }
            }

            plugin.getVisualizationManager().revertVisualization(player);
            plugin.getVisualizationManager().revertOutline(player);

            ce.finalizeField();
            field.setOpen(false);

            openCuboids.remove(player.getName());

            plugin.getVisualizationManager().visualizeSingleFieldFast(player, field);
            plugin.getStorageManager().offerField(field);
            plugin.getCommunicationManager().notifyPlaceCuboid(player, field);
            return true;
        }

        return false;
    }

    /**
     * Remove a child block from an open field
     *
     * @param player
     * @param block
     */
    public void removeChild(Player player, Block block)
    {
        CuboidEntry ce = openCuboids.get(player.getName());
        Field child = plugin.getForceFieldManager().getField(block);

        if (ce != null && child != null)
        {
            ce.getField().getChildren().remove(child);
        }
    }

    /**
     * Cancels a currently open cuboid definition if one of the field blocks are broken
     *
     * @param block the block that is being broken
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

    /**
     * Cancel an open cuboid and revert all visualizations
     *
     * @param player
     */
    public void cancelOpenCuboid(Player player)
    {
        plugin.getVisualizationManager().revertVisualization(player);
        plugin.getVisualizationManager().revertOutline(player);
        openCuboids.remove(player.getName());
        ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid has been cancelled.");
    }

    /**
     * Revert the last selected block
     *
     * @param player
     */
    public void revertLastSelection(Player player)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            BlockEntry selected = ce.getLastSelected();

            if (selected != null)
            {
                ce.revertLastSelected();
                plugin.getVisualizationManager().revertSingle(player, selected.getBlock());
                plugin.getVisualizationManager().displayFieldOutline(player, ce);
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Selection reverted");
            }
        }
    }

    /**
     * Expand the cuboid in the direction the player is facing by one block
     *
     * @param player
     */
    public void expandDirection(Player player)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            Block block = ce.getExpandedBlock(player);

            if (block != null)
            {
                processSelectedBlock(player, block);
            }

            plugin.getVisualizationManager().displayFieldOutline(player, ce);
        }
    }
}
