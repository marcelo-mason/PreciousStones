package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
 */
public class MineManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public MineManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param player
     * @param field
     */
    public void enterMine(final Player player, final Field field)
    {
        if (plugin.pm.hasPermission(player, "preciousstones.bypass.mine"))
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (!fs.isMine())
        {
            return;
        }

        if (!plugin.ffm.isAllowed(field, player.getName()))
        {
            final int delay = fs.getMineDelaySeconds();
            final int leftbehind = fs.getMineReplaceBlock();
            final Block block = plugin.ffm.getBlock(field);
            block.setType(Material.getMaterial(leftbehind));

            plugin.cm.showMine(player);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                @Override
                public void run()
                {
                    plugin.ffm.silentRelease(field);

                    block.getWorld().createExplosion(block.getLocation(), 4, false);
                    block.getWorld().createExplosion(block.getLocation(), 6, true);
                }
            }, delay * 20L);
        }
    }
}
