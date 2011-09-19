package net.sacredlabyrinth.Phaed.PreciousStones;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class Visualize implements Runnable
{
    private PreciousStones plugin;
    private Queue<BlockData> visualizationQueue = new LinkedList<BlockData>();
    private final int timerID;
    private final Player player;
    private final boolean revert;

    /**
     * @param vis
     * @param visualizationQueue
     * @param material
     * @param player
     * @param reverting
     */
    public Visualize(Visualization vis, Player player)
    {
        this.visualizationQueue.addAll(vis.getBlocks());
        this.plugin = PreciousStones.getInstance();
        this.revert = false;
        this.player = player;
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 5);
    }

    /**
     * @param vis
     * @param visualizationQueue
     * @param material
     * @param player
     * @param reverting
     */
    public Visualize(Visualization vis, Player player, boolean revert)
    {
        this.visualizationQueue.addAll(vis.getBlocks());
        this.plugin = PreciousStones.getInstance();
        this.revert = revert;
        this.player = player;
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 5);
    }

    public void run()
    {
        int i = 0;

        while (i < 100 && !visualizationQueue.isEmpty())
        {
            BlockData bd = visualizationQueue.poll();
            Location loc = bd.getLocation();

            if (!revert)
            {
                player.sendBlockChange(loc, bd.getTypeId(), bd.getData());
            }
            else
            {
                Block block = bd.getBlock();
                player.sendBlockChange(loc, block.getType(), block.getData());
            }

            i++;
        }

        if (visualizationQueue.isEmpty())
        {
            Bukkit.getServer().getScheduler().cancelTask(timerID);

            if (!revert)
            {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    public void run()
                    {
                        plugin.getVisualizationManager().revertVisualization(player);
                    }
                }, 20L * plugin.getSettingsManager().getVisualizeSeconds());
            }
        }
    }
}
