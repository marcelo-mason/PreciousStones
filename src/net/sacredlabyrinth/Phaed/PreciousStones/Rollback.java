package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.GriefBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class Rollback implements Runnable
{
    private PreciousStones plugin;
    private Queue<GriefBlock> griefQueue;
    private Queue<GriefBlock> dependentQueue = new LinkedList<GriefBlock>();
    private final int timerID;
    private final World world;

    /**
     * @param griefQueue
     * @param world
     */
    public Rollback(Queue<GriefBlock> griefQueue, World world)
    {
        this.griefQueue = griefQueue;
        this.world = world;
        this.plugin = PreciousStones.getInstance();

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 2);
    }

    public void run()
    {
        int i = 0;

        while (i < 100 && !griefQueue.isEmpty())
        {
            GriefBlock gb = griefQueue.poll();

            if (plugin.getGriefUndoManager().isDependentBlock(gb.getTypeId()))
            {
                dependentQueue.add(gb);
                continue;
            }

            PreciousStones.getInstance().getGriefUndoManager().undoGriefBlock(gb, world);
            i++;
        }

        if (griefQueue.isEmpty())
        {
            while (i < 200 && !dependentQueue.isEmpty())
            {
                GriefBlock gb = dependentQueue.poll();

                PreciousStones.getInstance().getGriefUndoManager().undoGriefBlock(gb, world);
                i++;
            }

            if (!dependentQueue.iterator().hasNext())
            {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
            }
        }
    }
}
