package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author phaed
 */
public class MineManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public MineManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param player
     * @param field
     */
    public void enterMine(final Player player, final Field field)
    {
        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.mine"))
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (!field.hasFlag(FieldFlag.MINE))
        {
            return;
        }

        if (!plugin.getForceFieldManager().isApplyToAllowed(field, player.getName()) || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            final int delay = fs.getMineDelaySeconds();
            final Block block = plugin.getForceFieldManager().getBlock(field);

            if(!plugin.getWorldGuardManager().canBuild(player, block.getLocation()))
            {
                return;
            }

            plugin.getCommunicationManager().showMine(player);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    plugin.getForceFieldManager().silentRelease(field);

                    block.getWorld().createExplosion(block.getLocation(), field.getSettings().getMineStrength(), field.getSettings().isMineHasFire());
                }
            }, delay * 20L);
        }
    }
}
