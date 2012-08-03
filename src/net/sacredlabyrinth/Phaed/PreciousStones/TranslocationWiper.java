package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class TranslocationWiper implements Runnable
{
    private PreciousStones plugin;
    private Queue<TranslocationBlock> translocationQueue = new LinkedList<TranslocationBlock>();
    private Queue<TranslocationBlock> dependentQueue = new LinkedList<TranslocationBlock>();
    private Queue<TranslocationBlock> clearDependentQueue = new LinkedList<TranslocationBlock>();
    private final int timerID;
    private final World world;
    private final Player player;
    private final Field field;
    private int count;

    /**
     * @param griefQueue
     * @param world
     */
    public TranslocationWiper(Field field, Queue<TranslocationBlock> translocationQueue, Player player)
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
        this.world = player.getWorld();
        this.player = player;

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 2, 1);
    }

    public void run()
    {
        int i = 0;
        boolean added = false;

        while (i < 500 && !dependentQueue.isEmpty())
        {
            TranslocationBlock tb = dependentQueue.poll();

            boolean cleared = plugin.getTranslocationManager().wipeTranslocationBlock(field, tb);

            // when it comes time to clear the blocks off the world, if a block
            // doesn't match whats in the database, then cancel the translocation of it
            // by deleting it from the database

            if (cleared)
            {
                this.clearDependentQueue.add(tb);
                added = true;
                count++;
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

                    boolean cleared = plugin.getTranslocationManager().wipeTranslocationBlock(field, tb);

                    // when it comes time to clear the blocks off the world, if a block
                    // doesn't match whats in the database, then cancel the translocation of it
                    // by deleting it from the database

                    if (!cleared)
                    {
                        plugin.getStorageManager().deleteTranslocation(field, tb);
                    }
                    else
                    {
                        added = true;
                        count++;
                    }
                    i++;
                }

                if (!translocationQueue.iterator().hasNext())
                {
                    Bukkit.getServer().getScheduler().cancelTask(timerID);
                    plugin.getStorageManager().updateTranslocationApplyMode(field, false);
                    field.setDisabled(true);
                    field.setTranslocating(false);
                }
            }
        }

        if (added)
        {
            if (count % 50 == 0 && count != 0)
            {
                if (player != null)
                {
                    player.sendMessage(ChatColor.AQUA + "Imported " + count + "blocks");
                }
            }
        }
    }
}
