package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForesterManager;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author phaed
 */
public class ForesterEntry
{
    private Field field;
    private int count;
    private String playerName;
    private int growTime;
    private boolean landPrepared;
    private PreciousStones plugin;

    /**
     * @param field
     * @param playerName
     */
    public ForesterEntry(Field field, String playerName)
    {
        this.field = field;
        this.playerName = playerName;
        this.count = 0;
        this.growTime = Math.max(field.getSettings().getGrowTime(), 2);
        plugin = PreciousStones.getInstance();

        scheduleNextUpdate();
        field.setForested(true);
    }

    /**
     * @return the field
     */
    public Field getField()
    {
        return field;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ForesterEntry))
        {
            return false;
        }

        ForesterEntry other = (ForesterEntry) obj;
        return other.getField().getX() == getField().getX() && other.getField().getY() == getField().getY() && other.getField().getZ() == getField().getZ() && other.getField().getWorld().equals(getField().getWorld());
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + (this.field != null ? this.field.hashCode() : 0);
        return hash;
    }

    private void scheduleNextUpdate()
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Update(), growTime);
    }

    private class Update implements Runnable
    {
        public void run()
        {
            if (doPlantingAttempt())
            {
                scheduleNextUpdate();
            }
        }
    }

    private boolean doPlantingAttempt()
    {
        PreciousStones.debug("planting attempt");
        World world = plugin.getServer().getWorld(field.getWorld());
        Player player = plugin.getServer().getPlayer(playerName);

        if (world == null || player == null)
        {
            return false;
        }

        if (!landPrepared)
        {
            PreciousStones.debug("prepare land");
            plugin.getForesterManager().prepareLand(field, world);
            PreciousStones.debug("land prepared");
            landPrepared = true;
        }

        if (!field.getSettings().getTreeTypes().isEmpty())
        {
            PreciousStones.debug("generate tree");
            plugin.getForesterManager().generateTree(field, player, world);
        }

        count++;

        if (count >= field.getSettings().getTreeCount())
        {
            Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());
            block.setTypeId(0, false);
            block.getLocation().add(0, -1, 0).getBlock().setTypeId(field.getSettings().getGroundBlock(), false);

            if (!field.getSettings().getTreeTypes().isEmpty())
            {
                world.generateTree(block.getLocation(), ForesterManager.getTree(field.getSettings()));
            }

            plugin.getForesterManager().doCreatureSpawns(field);
            plugin.getForceFieldManager().releaseNoDrop(field);
            return false;
        }

        return true;
    }
}
