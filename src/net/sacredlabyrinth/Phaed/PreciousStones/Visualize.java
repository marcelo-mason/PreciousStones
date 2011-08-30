package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.Queue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author phaed
 */
public class Visualize implements Runnable
{
    private PreciousStones plugin;
    private final Visualization vis;
    private Queue<Location> visualizationQueue;
    private final int timerID;
    private final Player player;
    private final Material material;
    private final boolean reverting;

    /**
     *
     * @param vis
     * @param visualizationQueue
     * @param material
     * @param player
     * @param reverting
     */
    public Visualize(Visualization vis, Queue<Location> visualizationQueue, Material material, Player player, boolean reverting)
    {
        this.visualizationQueue = visualizationQueue;
        this.plugin = PreciousStones.getInstance();
        this.reverting = reverting;
        this.material = material;
        this.player = player;
        this.vis = vis;

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 5);
    }

    @Override
    public void run()
    {
        int i = 0;

        while (i < 100 && !visualizationQueue.isEmpty())
        {
            Location loc = visualizationQueue.poll();
            player.sendBlockChange(loc, material, (byte) 0);
            i++;
        }

        if (visualizationQueue.isEmpty())
        {
            Bukkit.getServer().getScheduler().cancelTask(timerID);

            if (!reverting)
            {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        vis.setRunning(false);
                        plugin.getVisualizationManager().revertVisualization(player);
                    }
                }, 20L * plugin.getSettingsManager().getVisualizeSeconds());
            }
        }
    }
}
