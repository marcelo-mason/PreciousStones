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
import java.util.Set;

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

    public CuboidEntry getOpenCuboid(Player player)
    {
        return openCuboids.get(player.getName());
    }

    public void openCuboid(final Player player, final Field field)
    {
        final CuboidEntry ce = new CuboidEntry(field);
        openCuboids.put(player.getName(), ce);

        final Material material = Material.getMaterial(plugin.getSettingsManager().getCuboidDefiningType());

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                Block block = field.getBlock();
                addSelectionBlock(player, block);
                plugin.getVisualizationManager().displaySingle(player, material, block);

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
                    Set<Field> family = ce.getField().getFamily();
                    family.add(field);

                    for (Field child : family)
                    {
                        Block block = child.getBlock();
                        addSelectionBlock(player, field.getBlock());
                        plugin.getVisualizationManager().displaySingle(player, material, block);
                    }

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
    public void closeOpenCuboid(Block block)
    {
        for (String playerName : openCuboids.keySet())
        {
            Field field = openCuboids.get(playerName).getField();

            if (Helper.isSameBlock(field.getLocation(), block.getLocation()))
            {
                openCuboids.remove(playerName);
            }
        }
    }
}
