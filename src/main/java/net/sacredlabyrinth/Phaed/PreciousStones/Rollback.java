package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.blocks.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author phaed
 */
public class Rollback implements Runnable {
    private PreciousStones plugin;
    private Queue<GriefBlock> griefQueue;
    private Queue<GriefBlock> dependentQueue = new LinkedList<GriefBlock>();
    private final int timerID;
    private final World world;
    private final Field field;

    /**
     * @param griefQueue
     * @param world
     */
    public Rollback(Queue<GriefBlock> griefQueue, World world, Field field) {
        this.field = field;
        this.griefQueue = griefQueue;
        this.world = world;
        this.plugin = PreciousStones.getInstance();

        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 2, 1);
    }

    public void run() {
        int i = 0;

        while (i < 500 && !griefQueue.isEmpty()) {
            GriefBlock gb = griefQueue.poll();

            if (plugin.getSettingsManager().isDependentBlock(gb.getTypeId())) {
                dependentQueue.add(gb);
                continue;
            }

            if (field.hasFlag(FieldFlag.GRIEF_REVERT_SAFETY)) {
                movePlayers(gb);
            }

            PreciousStones.getInstance().getGriefUndoManager().undoGriefBlock(gb, world);
            i++;
        }

        if (griefQueue.isEmpty()) {
            while (i < 500 && !dependentQueue.isEmpty()) {
                GriefBlock gb = dependentQueue.poll();

                if (field.hasFlag(FieldFlag.GRIEF_REVERT_SAFETY)) {
                    movePlayers(gb);
                }

                PreciousStones.getInstance().getGriefUndoManager().undoGriefBlock(gb, world);
                i++;
            }

            if (!dependentQueue.iterator().hasNext()) {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
            }
        }
    }

    private void movePlayers(GriefBlock gb) {
        for (Player player : world.getPlayers()) {
            Vec blockLocation = gb.toVec();
            Vec location = new Vec(player.getLocation());

            if (blockLocation.equals(location) || blockLocation.equals(location.add(0, 1, 0))) {
                plugin.getTeleportationManager().teleportAway(player);
            }
        }
    }
}
