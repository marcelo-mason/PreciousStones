package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class TranslocationUpdater implements Runnable
{
    private PreciousStones plugin;
    private Queue<TranslocationBlock> translocationQueue = new LinkedList<TranslocationBlock>();
    private Queue<TranslocationBlock> dependentQueue = new LinkedList<TranslocationBlock>();
    private Queue<TranslocationBlock> clearDependentQueue = new LinkedList<TranslocationBlock>();
    private final int timerID;
    private final World world;
    private final Field field;

    /**
     * @param translocationQueue
     * @param world
     */
    public TranslocationUpdater(Field field, Queue<TranslocationBlock> translocationQueue, World world)
    {
        this.plugin = PreciousStones.getInstance();

        for (TranslocationBlock tb : translocationQueue)
        {
            if (tb != null)
            {
                if (plugin.getSettingsManager().isDependentBlock(tb.getTypeId()))
                {
                    this.dependentQueue.add(tb);
                }
                else
                {
                    this.translocationQueue.add(tb);
                }
            }
        }

        this.field = field;
        this.world = world;
        field.setTranslocating(true);

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 5, 5);
    }

    public void run()
    {
        int i = 0;

        while (i < 500 && !dependentQueue.isEmpty())
        {
            TranslocationBlock tb = dependentQueue.poll();

            boolean cleared = plugin.getTranslocationManager().updateTranslationBlock(field, tb, false);

            // when it comes time to clear the blocks off the world, if a block
            // doesn't match whats in the database, then cancel the translocation of it
            // by deleting it from the database

            if (cleared)
            {
                this.clearDependentQueue.add(tb);
            }
            else
            {
                plugin.getStorageManager().deleteTranslocation(field, tb);
            }
            i++;
        }

        if (dependentQueue.isEmpty())
        {
            while (i < 500 && !clearDependentQueue.isEmpty())
            {
                TranslocationBlock tb = clearDependentQueue.poll();

                plugin.getTranslocationManager().zeroOutBlock(tb);
                i++;
            }

            if (clearDependentQueue.isEmpty())
            {
                while (i < 500 && !translocationQueue.isEmpty())
                {
                    TranslocationBlock tb = translocationQueue.poll();

                    boolean cleared = plugin.getTranslocationManager().updateTranslationBlock(field, tb, true);

                    // when it comes time to clear the blocks off the world, if a block
                    // doesn't match whats in the database, then cancel the translocation of it
                    // by deleting it from the database

                    if (!cleared)
                    {
                        plugin.getStorageManager().deleteTranslocation(field, tb);
                    }

                    i++;
                }

                if (!translocationQueue.iterator().hasNext())
                {
                    Bukkit.getServer().getScheduler().cancelTask(timerID);
                    field.setDisabled(true);
                    field.setTranslocating(false);
                    field.dirtyFlags("TranslocationUpdater");
                }
            }
        }
    }
}
