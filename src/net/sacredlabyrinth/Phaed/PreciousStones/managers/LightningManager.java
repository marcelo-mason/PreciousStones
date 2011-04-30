package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class LightningManager
{
    private PreciousStones plugin;
    
    public LightningManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void enterLightning(final Player player, final Field field)
    {
	if (!field.isAllAllowed(player.getName()))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    final int delay = fieldsettings.lightningDelaySeconds;
	    final int leftbehind = fieldsettings.lightningReplaceBlock;
	    
	    if (fieldsettings.lightning)
	    {
		plugin.cm.showLightning(player);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
		    public void run()
		    {
			Block block = plugin.ffm.getBlock(field);
			
			
			player.getWorld().strikeLightning(player.getLocation());
			
			if(leftbehind >= 0){
				plugin.ffm.silentRelease(field);
				block.setType(Material.getMaterial(leftbehind));	
			}
			
		    }
		}, delay * 20L);
	    }
	}
    }
}
