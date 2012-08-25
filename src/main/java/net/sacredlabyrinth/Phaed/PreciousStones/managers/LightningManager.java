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
public class LightningManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public LightningManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param player
     * @param field
     */
    public void enterLightning(final Player player, final Field field)
    {
        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.lightning"))
        {
            return;
        }

        if (!plugin.getForceFieldManager().isApplyToAllowed(field, player.getName()) || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            FieldSettings fs = field.getSettings();

            final int delay = fs.getLightningDelaySeconds();
            final int leftbehind = fs.getLightningReplaceBlock();

            if (field.hasFlag(FieldFlag.LIGHTNING))
            {
                plugin.getCommunicationManager().showLightning(player);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    public void run()
                    {
                        Block block = plugin.getForceFieldManager().getBlock(field);

                        player.getWorld().strikeLightning(player.getLocation());

                        if (leftbehind >= 0)
                        {
                            plugin.getForceFieldManager().silentRelease(field);
                            block.setType(Material.getMaterial(leftbehind));
                        }

                    }
                }, delay * 20L);
            }
        }
    }
}
