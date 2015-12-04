package net.sacredlabyrinth.Phaed.PreciousStones.visualization;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
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
public class Visualize implements Runnable {
    private PreciousStones plugin;
    private Queue<BlockEntry> visualizationQueue = new LinkedList<BlockEntry>();
    private final int timerID;
    private final Player player;
    private final boolean reverting;
    private final boolean skipRevert;
    private final int seconds;

    public Visualize(List<BlockEntry> blocks, Player player, boolean reverting, boolean skipRevert, int seconds) {
        this.visualizationQueue.addAll(blocks);
        this.plugin = PreciousStones.getInstance();
        this.reverting = reverting;
        this.player = player;
        this.skipRevert = skipRevert;
        this.seconds = seconds;
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, PreciousStones.getInstance().getSettingsManager().getVisualizeTicksBetweenSends());
    }

    public void run() {
        int i = 0;

        while (i < PreciousStones.getInstance().getSettingsManager().getVisualizeSendSize() && !visualizationQueue.isEmpty()) {
            BlockEntry bd = visualizationQueue.poll();
            Location loc = bd.getLocation();

            if (!loc.equals(player.getLocation()) && !loc.equals(player.getLocation().add(0, 1, 0))) {
                if (reverting) {
                    Block block = bd.getBlock();
                    player.sendBlockChange(loc, block.getType(), block.getData());
                } else {
                    player.sendBlockChange(loc, bd.getTypeId(), bd.getData());
                }
            }
            i++;
        }

        if (visualizationQueue.isEmpty()) {
            Bukkit.getServer().getScheduler().cancelTask(timerID);

            if (!reverting) {
                if (!skipRevert) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.getVisualizationManager().revert(player);
                        }
                    }, 20L * seconds);
                }
            }
        }
    }
}
