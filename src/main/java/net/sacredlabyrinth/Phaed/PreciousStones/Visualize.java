package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author phaed
 */
public class Visualize implements Runnable
{
    private PreciousStones plugin;
    private Queue<BlockEntry> visualizationQueue = new LinkedList<BlockEntry>();
    private final int timerID;
    private final Player player;
    private final boolean revert;
    private final boolean skipRevert;
    private final int seconds;

    public Visualize(List<BlockEntry> blocks, Player player, boolean revert, boolean skipRevert, int seconds)
    {
        this.visualizationQueue.addAll(blocks);
        this.plugin = PreciousStones.getInstance();
        this.revert = revert;
        this.player = player;
        this.skipRevert = skipRevert;
        this.seconds = seconds;
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, PreciousStones.getInstance().getSettingsManager().getVisualizeTicksBetweenSends());
    }

    public void run()
    {
        int i = 0;

        while (i < PreciousStones.getInstance().getSettingsManager().getVisualizeSendSize() && !visualizationQueue.isEmpty())
        {
            BlockEntry bd = visualizationQueue.poll();
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
                if (!skipRevert)
                {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        public void run()
                        {
                            plugin.getVisualizationManager().revertVisualization(player);
                        }
                    }, 20L * seconds);
                }
            }
        }
    }
}
