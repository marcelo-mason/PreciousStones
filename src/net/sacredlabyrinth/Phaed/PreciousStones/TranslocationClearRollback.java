package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class TranslocationClearRollback implements Runnable
{
    private PreciousStones plugin;
    private Queue<TranslocationBlock> translocationQueue;
    private Queue<TranslocationBlock> dependentQueue = new LinkedList<TranslocationBlock>();
    private final int timerID;
    private final World world;

    /**
     * @param griefQueue
     * @param world
     */
    public TranslocationClearRollback(Queue<TranslocationBlock> translocationQueue, World world)
    {
        this.translocationQueue = translocationQueue;
        this.world = world;
        this.plugin = PreciousStones.getInstance();

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 5, 5);
    }

    public void run()
    {
        int i = 0;

        while (i < 100 && !translocationQueue.isEmpty())
        {
            TranslocationBlock tb = translocationQueue.poll();

            if (plugin.getGriefUndoManager().isDependentBlock(tb.getTypeId()))
            {
                dependentQueue.add(tb);
                continue;
            }

            PreciousStones.getInstance().getTranslocationManager().clearTranslocationBlock(tb, world);
            i++;
        }

        if (translocationQueue.isEmpty())
        {
            while (i < 200 && !dependentQueue.isEmpty())
            {
                TranslocationBlock gb = dependentQueue.poll();

                PreciousStones.getInstance().getTranslocationManager().clearTranslocationBlock(gb, world);
                i++;
            }

            if (!dependentQueue.iterator().hasNext())
            {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
            }
        }
    }
}
