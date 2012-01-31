package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
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

        if (!plugin.getForceFieldManager().isApplyToAllowed(field, player.getName()))
        {
            final int delay = fs.getMineDelaySeconds();
            final int leftbehind = fs.getMineReplaceBlock();
            final Block block = plugin.getForceFieldManager().getBlock(field);
            block.setType(Material.getMaterial(leftbehind));

            plugin.getCommunicationManager().showMine(player);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    plugin.getForceFieldManager().silentRelease(field);

                    block.getWorld().createExplosion(block.getLocation(), 4, false);
                    block.getWorld().createExplosion(block.getLocation(), 6, true);
                }
            }, delay * 20L);
        }
    }
}
