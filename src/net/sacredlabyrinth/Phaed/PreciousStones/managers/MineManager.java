package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftWorld;
import net.minecraft.server.*;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;

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
	if (plugin.pm.hasPermission(player, "preciousstones.bypass.mine")){
		return;
	}
	if (!field.isAllowed(player.getName()) && !plugin.stm.isTeamMate(player.getName(), field.getOwner()))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                return;
            }

	    final int delay = fieldsettings.mineDelaySeconds;
	    final int leftbehind = fieldsettings.mineReplaceBlock;

	    if (fieldsettings.mine)
	    {
		plugin.cm.showMine(player);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
                    @Override
		    public void run()
		    {
			Block block = plugin.ffm.getBlock(field);
			plugin.ffm.silentRelease(field);

			CraftWorld world = (CraftWorld)block.getWorld();
			block.setType(Material.getMaterial(leftbehind));
                        Location loc = new Location(block.getWorld(), block.getX()+.5, block.getY()+.5, block.getZ()+.5);
                        block.getWorld().spawn(loc, TNTPrimed.class);
		    }
		}, delay * 20L);
	    }
	}
    }
}
