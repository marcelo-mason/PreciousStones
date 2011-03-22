package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class VelocityManager
{
    private PreciousStones plugin;
    private Set<String> fallDamageImmune = Collections.synchronizedSet(new HashSet<String>());
    
    public VelocityManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void launchPlayer(final Player player, final Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.launch"))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    final int launchheight = fieldsettings.launchHeight;
	    
	    if (fieldsettings.launch)
	    {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
		    public void run()
		    {
			double speed = 8;
			
			Vector loc = player.getLocation().toVector();
			
			Vector target = new Vector(field.getX(), field.getY(), field.getZ());
			
			Vector velocity = target.clone().subtract(new Vector(loc.getX(), loc.getY(), loc.getZ()));
			
			velocity.multiply(speed / velocity.length());
			
			float height = (((player.getLocation().getPitch() * -1) + 90) / 35);
			
			if (launchheight > 0)
			{
			    height = launchheight;
			}
			
			player.setVelocity(velocity.setY(height));
			
			plugin.cm.showLaunch(player);
			startFallImmunity(player);
		    }
		}, 0L);
	    }
	}
    }
    
    public void bouncePlayer(final Player player, Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.bounce"))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    final int bounceHeight = fieldsettings.bounceHeight;
	    
	    if (fieldsettings.bounce)
	    {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
		    public void run()
		    {
			float height = (((player.getLocation().getPitch() * -1) + 90) / 35);
			
			if (bounceHeight > 0)
			{
			    height = bounceHeight;
			}
			
			player.setVelocity(new Vector(0, height, 0));
			plugin.cm.showBounce(player);
			startFallImmunity(player);
		    }
		}, 5L);
	    }
	}
    }
    
    public void startFallImmunity(Player player)
    {
	fallDamageImmune.add(player.getName());
	startImmuneRemovalDelay(player);
    }
    
    public boolean isFallDamageImmune(Player player)
    {
	return fallDamageImmune.contains(player.getName());
    }
    
    public void stopFallImmunity(Player player)
    {
	fallDamageImmune.remove(player.getName());
	plugin.cm.showThump(player);
    }
    
    public void startImmuneRemovalDelay(final Player player)
    {
	final String name = player.getName();
	
	plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
	{
	    public void run()
	    {
		fallDamageImmune.remove(name);
	    }
	}, 15 * 20L);
    }
}
