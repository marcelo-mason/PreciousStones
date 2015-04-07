package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class TranslocationApplier implements Runnable
{
    private PreciousStones plugin;
    private Queue<TranslocationBlock> translocationQueue;
    private Queue<TranslocationBlock> dependentQueue = new LinkedList<TranslocationBlock>();
    private final int timerID;
    private final World world;
    private final Field field;

    /**
     * @param field
     * @param translocationQueue
     * @param world
     */
    public TranslocationApplier(Field field, Queue<TranslocationBlock> translocationQueue, World world)
    {
        this.field = field;
        this.translocationQueue = translocationQueue;
        this.world = world;
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

                if (field.hasFlag(FieldFlag.TRANSLOCATION_SAFETY))
                {
                    movePlayers(tb);
                }

                boolean applied = PreciousStones.getInstance().getTranslocationManager().applyTranslocationBlock(tb, world);

                // if the block could not be applied, due to another block being in the way
                // then don't apply it nad set it on the database as not-applied

                if (!applied)
                {
                    plugin.getStorageManager().updateTranslocationBlockApplied(field, tb, false);
                }
            }
            i++;
        }

        if (translocationQueue.isEmpty())
        {
            while (i < 200 && !dependentQueue.isEmpty())
            {
                TranslocationBlock tb = dependentQueue.poll();

                if (field.hasFlag(FieldFlag.TRANSLOCATION_SAFETY))
                {
                    movePlayers(tb);
                }

                boolean applied = PreciousStones.getInstance().getTranslocationManager().applyTranslocationBlock(tb, world);

                // if the block could not be applied, due to another block being in the way
                // then don't apply it nad set it on the database as not-applied

                if (!applied)
                {
                    plugin.getStorageManager().updateTranslocationBlockApplied(field, tb, false);
                }

                i++;
            }

            if (!dependentQueue.iterator().hasNext())
            {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
                field.setDisabled(false);
                field.setTranslocating(false);
                field.dirtyFlags("TranslocationApplier");
            }
        }
    }


    private void movePlayers(TranslocationBlock tb)
    {
        for (Player player : world.getPlayers())
        {
            Vec blockLocation = tb.toVec();
            Vec location = new Vec(player.getLocation());

            if (blockLocation.equals(location) || blockLocation.equals(location.add(0, 1, 0)))
            {
                plugin.getTeleportationManager().teleportAway(player);
            }
        }
    }
}
