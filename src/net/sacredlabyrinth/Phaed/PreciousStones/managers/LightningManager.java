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
public class LightningManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public LightningManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }

    /**
     *
     * @param player
     * @param field
     */
    public void enterLightning(final Player player, final Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.bypass.lightning"))
        {
            return;
	}

	if (!plugin.ffm.isAllowed(field, player.getName()))
	{
	    FieldSettings fs = field.getSettings();

	    final int delay = fs.getLightningDelaySeconds();
	    final int leftbehind = fs.getLightningReplaceBlock();

	    if (fs.isLightning())
	    {
		plugin.cm.showLightning(player);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
                    @Override
		    public void run()
		    {
			Block block = plugin.ffm.getBlock(field);

			player.getWorld().strikeLightning(player.getLocation());

			if(leftbehind >= 0)
                        {
                            plugin.ffm.silentRelease(field);
                            block.setType(Material.getMaterial(leftbehind));
			}

		    }
		}, delay * 20L);
	    }
	}
    }
}
