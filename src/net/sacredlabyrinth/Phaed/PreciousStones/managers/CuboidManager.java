package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
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

    public void addSelectionBlock(Player player, Block block)
    {
        if (openCuboids.containsKey(player.getName()))
        {
            openCuboids.get(player.getName()).addSelected(block);
        }
    }

    public boolean hasOpenCuboid(Player player)
    {
        return openCuboids.containsKey(player.getName());
    }

    public boolean isOpenCuboid(Block block)
    {
        for (String playerName : openCuboids.keySet())
        {
            CuboidEntry ce = openCuboids.get(playerName);

            if (Helper.isSameBlock(ce.getField().getLocation(), block.getLocation()))
            {
                return true;
            }

            Field field = plugin.getForceFieldManager().getField(block);

            if (field != null)
            {
                if (field.getParent() != null)
                {

                    if (Helper.isSameBlock(field.getParent().getLocation(), block.getLocation()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public CuboidEntry getOpenCuboid(Player player)
    {
        return openCuboids.get(player.getName());
    }

    public void openCuboid(final Player player, final Field field)
    {
        final CuboidEntry ce = new CuboidEntry(field);
        openCuboids.put(player.getName(), ce);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                drawSplash(field.getBlock(), player);

                ChatBlock.sendMessage(player, ChatColor.YELLOW + "Use your empty hand. Left-click to draw, right-click to finish.");
                ChatBlock.sendMessage(player, ChatColor.YELLOW + "Max size: " + ChatColor.AQUA + ce.getMaxWidth() + "x" + ce.getMaxHeight() + "x" + ce.getMaxWidth());
            }
        }, 1L);
    }

    public void openChild(final Player player, final Field field)
    {
        final CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            final Material material = Material.getMaterial(plugin.getSettingsManager().getCuboidDefiningType());

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    drawSplash(field.getBlock(), player);

                    ChatBlock.sendMessage(player, ChatColor.YELLOW + "Max size: " + ChatColor.AQUA + ce.getMaxWidth() + "x" + ce.getMaxHeight() + "x" + ce.getMaxWidth());
                }
            }, 1L);
        }
    }

    public boolean closeCuboid(final Player player)
    {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null)
        {
            plugin.getVisualizationManager().revertVisualization(player);

            final Field field = ce.getField();

            ce.calculate();

            if (ce.isExceeded())
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid exceeds maximum size");
                openCuboids.remove(player.getName());
                return false;
            }

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

            plugin.getForceFieldManager().removeSourceField(field);
            field.setCuboidDimensions(ce.getMinx(), ce.getMiny(), ce.getMinz(), ce.getMaxx(), ce.getMaxy(), ce.getMaxz());
            plugin.getForceFieldManager().addSourceField(field);

            openCuboids.remove(player.getName());
            plugin.getVisualizationManager().visualizeSingleField(player, field);
            plugin.getStorageManager().offerField(field);
            plugin.getCommunicationManager().notifyPlaceCuboid(player, field);
            return true;
        }

        return false;
    }

    /**
     * Closes a currently open cuboid definition
     *
     * @param block
     */
    public void cancelOpenCuboid(Block block)
    {
        for (String playerName : openCuboids.keySet())
        {
            Field field = openCuboids.get(playerName).getField();

            if (Helper.isSameBlock(field.getLocation(), block.getLocation()))
            {
                Player player = Helper.matchSinglePlayer(playerName);

                if (player != null)
                {
                    plugin.getVisualizationManager().revertVisualization(player);
                    openCuboids.remove(playerName);
                    ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid has been cancelled.");
                }
            }
        }
    }

    /**
     * Closes a currently open cuboid definition
     *
     * @param player
     */
    public void cancelOpenCuboid(Player player)
    {
        plugin.getVisualizationManager().revertVisualization(player);
        openCuboids.remove(player.getName());
        ChatBlock.sendMessage(player, ChatColor.RED + "Cuboid has been cancelled.");
    }


    private void drawSplash(Block block, Player player)
    {
        final Material material = Material.getMaterial(plugin.getSettingsManager().getCuboidDefiningType());
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces)
        {
            Block faceBlock = block.getRelative(face);

            if (!plugin.getSettingsManager().isThroughType(faceBlock.getTypeId()))
            {
                if (!plugin.getSettingsManager().isFieldType(faceBlock))
                {
                    addSelectionBlock(player, faceBlock);
                    plugin.getVisualizationManager().displaySingle(player, material, faceBlock);
                }

                List<BlockFace> blackFaces = new LinkedList<BlockFace>();

                if (face.equals(BlockFace.EAST) || face.equals(BlockFace.WEST))
                {
                    blackFaces.add(BlockFace.NORTH);
                    blackFaces.add(BlockFace.SOUTH);
                    blackFaces.add(BlockFace.UP);
                    blackFaces.add(BlockFace.DOWN);
                }
                else if (face.equals(BlockFace.SOUTH) || face.equals(BlockFace.SOUTH))
                {
                    blackFaces.add(BlockFace.EAST);
                    blackFaces.add(BlockFace.WEST);
                    blackFaces.add(BlockFace.UP);
                    blackFaces.add(BlockFace.DOWN);
                }
                else if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN))
                {
                    blackFaces.add(BlockFace.EAST);
                    blackFaces.add(BlockFace.WEST);
                    blackFaces.add(BlockFace.NORTH);
                    blackFaces.add(BlockFace.SOUTH);
                }

                for (BlockFace bf : blackFaces)
                {
                    Block relative = faceBlock.getRelative(bf);

                    if (!plugin.getSettingsManager().isThroughType(relative.getTypeId()))
                    {
                        if (!plugin.getSettingsManager().isFieldType(relative))
                        {
                            addSelectionBlock(player, relative);
                            plugin.getVisualizationManager().displaySingle(player, material, relative);
                        }
                    }
                }
            }
        }
    }
}
