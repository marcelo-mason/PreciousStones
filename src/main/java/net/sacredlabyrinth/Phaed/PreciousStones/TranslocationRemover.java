package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class TranslocationRemover implements Runnable
{
    private PreciousStones plugin;
    private Queue<TranslocationBlock> translocationQueue;
    private Queue<TranslocationBlock> dependentQueue = new LinkedList<TranslocationBlock>();
    private final int timerID;
    private final Player player;
    private final Field field;
    private int count;
    private int notRemovedCount;

    /**
     * @param translocationQueue
     * @param player
     */
    public TranslocationRemover(Field field, Queue<TranslocationBlock> translocationQueue, Player player)
    {
        this.field = field;
        this.translocationQueue = translocationQueue;
        this.player = player;
        this.plugin = PreciousStones.getInstance();
        field.setTranslocating(true);

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 5, 5);
    }

    public void run()
    {
        int i = 0;

        while (i < 100 && !translocationQueue.isEmpty())
        {
            TranslocationBlock tb = translocationQueue.poll();

            if (tb != null)
            {
                if (plugin.getSettingsManager().isDependentBlock(tb.getTypeId()))
                {
                    dependentQueue.add(tb);
                    continue;
                }

                boolean applied = PreciousStones.getInstance().getTranslocationManager().applyTranslocationBlock(tb, player.getWorld());

                // if the block could not be applied, due to another block being in the way
                // then don't apply it nad set it on the database as not-applied

                if (!applied)
                {
                    plugin.getStorageManager().updateTranslocationBlockApplied(field, tb, false);
                    notRemovedCount++;
                }
                else
                {
                    plugin.getStorageManager().deleteTranslocation(field, tb);
                    count++;
                    announce();

                }
            }
            i++;
        }

        if (translocationQueue.isEmpty())
        {
            while (i < 200 && !dependentQueue.isEmpty())
            {
                TranslocationBlock tb = dependentQueue.poll();

                boolean applied = PreciousStones.getInstance().getTranslocationManager().applyTranslocationBlock(tb, player.getWorld());

                // if the block could not be applied, due to another block being in the way
                // then don't apply it nad set it on the database as not-applied

                if (!applied)
                {
                    plugin.getStorageManager().updateTranslocationBlockApplied(field, tb, false);
                    notRemovedCount++;
                }
                else
                {
                    plugin.getStorageManager().deleteTranslocation(field, tb);
                    count++;
                    announce();
                }

                i++;
            }

            if (!dependentQueue.iterator().hasNext())
            {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
                field.setDisabled(false);
                field.setTranslocating(false);
                field.dirtyFlags("TranslocationRemover");
                ChatBlock.send(player, "removalComplete");

                if(notRemovedCount > 0)
                {
                    ChatBlock.send(player, "blocksSkipped", count);
                    ChatBlock.send(player, "blocksSkipped2");
                }
            }
        }
    }

    public void announce()
    {
        if (count % 25 == 0 && count != 0)
        {
            if (player != null)
            {
                ChatBlock.send(player, "removedBlocks", count);
            }
        }
    }
}
