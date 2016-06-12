package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForesterManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author phaed
 */
public class ForesterEntry {
    private Field field;
    private int count;
    private String playerName;
    private int growTime;
    private boolean landPrepared;
    private PreciousStones plugin;

    /**
     * @param field
     * @param player
     */
    public ForesterEntry(Field field, CommandSender player) {
        this.field = field;
        this.playerName = player.getName();
        this.count = 0;
        this.growTime = Math.max(field.getSettings().getGrowTime(), 2);
        plugin = PreciousStones.getInstance();

        scheduleNextUpdate();
        field.getForestingModule().recordForesterUse();
        field.getForestingModule().setForesting(true);

        ChatHelper.send(player, "foresterActivating");

        if (field.getForestingModule().hasForesterUse()) {
            ChatHelper.send(player, "foresterUsesLeft", field.getForestingModule().foresterUsesLeft());
        }
    }

    /**
     * @return the field
     */
    public Field getField() {
        return field;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ForesterEntry)) {
            return false;
        }

        ForesterEntry other = (ForesterEntry) obj;
        return other.getField().getX() == getField().getX() && other.getField().getY() == getField().getY() && other.getField().getZ() == getField().getZ() && other.getField().getWorld().equals(getField().getWorld());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.field != null ? this.field.hashCode() : 0);
        return hash;
    }

    private void scheduleNextUpdate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Update(), growTime);
    }

    private class Update implements Runnable {
        public void run() {
            if (doPlantingAttempt()) {
                scheduleNextUpdate();
            }
        }
    }

    private boolean doPlantingAttempt() {
        PreciousStones.debug("planting attempt");
        World world = plugin.getServer().getWorld(field.getWorld());
        Player player = plugin.getServer().getPlayer(playerName);

        if (world == null || player == null) {
            return false;
        }

        if (!landPrepared) {
            PreciousStones.debug("prepare land");
            plugin.getForesterManager().prepareLand(field, world);
            PreciousStones.debug("land prepared");
            landPrepared = true;
        }

        if (!field.getSettings().getTreeTypes().isEmpty()) {
            PreciousStones.debug("generate tree");
            plugin.getForesterManager().generateTree(field, player, world);
        }

        count++;

        if (count >= field.getSettings().getTreeCount()) {
            plugin.getForesterManager().doCreatureSpawns(field);

            if (!field.getForestingModule().hasForesterUse()) {
                Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());
                block.setTypeId(0, false);
                block.getLocation().add(0, -1, 0).getBlock().setTypeId(field.getSettings().getGroundBlock().getTypeId(), false);

                if (!field.getSettings().getTreeTypes().isEmpty()) {
                    world.generateTree(block.getLocation(), ForesterManager.getTree(field.getSettings()));
                }

                plugin.getForceFieldManager().releaseNoDrop(field);
            }

            field.getForestingModule().setForesting(false);
            return false;
        }

        return true;
    }
}
