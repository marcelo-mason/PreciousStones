package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftWorld;
import net.minecraft.server.*;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class MineManager
{
    private PreciousStones plugin;
    
    public MineManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void enterMine(final Player player, final Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.bypass.mine")){
		return;
	}    	
	if (!field.isAllAllowed(player.getName()))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    final int delay = fieldsettings.mineDelaySeconds;
	    final int leftbehind = fieldsettings.mineReplaceBlock;
	    
	    if (fieldsettings.mine)
	    {
		plugin.cm.showMine(player);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
		    public void run()
		    {
			Block block = plugin.ffm.getBlock(field);
			plugin.ffm.silentRelease(field);
			
			CraftWorld world = (CraftWorld)block.getWorld();
			EntityTNTPrimed tnt = new EntityTNTPrimed(world.getHandle(), block.getX()+0.5F, block.getY()+0.5F, block.getZ()+0.5F);
			world.getHandle().a(tnt);
			
			block.setType(Material.getMaterial(leftbehind));
		    }
		}, delay * 20L);
	    }
	}
    }
}
