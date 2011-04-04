package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class VelocityManager
{
    private PreciousStones plugin;
    private HashMap<String, Integer> fallDamageImmune = new HashMap<String, Integer>();
    
    public VelocityManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void launchPlayer(final Player player, final Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.launch"))
	{
	    if (field.isAllAllowed(player.getName()))
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
			    
			    Vector velocity = target.clone().subtract(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			    
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
		    }, 5L);
		}
	    }
	}
    }
    
    public void shootPlayer(final Player player, Field field)
    {
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.bounce"))
	{
	    if (field.isAllAllowed(player.getName()))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		final int bounceHeight = fieldsettings.cannonHeight;
		
		if (fieldsettings.cannon)
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
			    plugin.cm.showCannon(player);
			    startFallImmunity(player);
			}
		    }, 5L);
		}
	    }
	}
    }
    
    public void startFallImmunity(final Player player)
    {	
	if (fallDamageImmune.containsKey(player.getName()))
	{
	    int current = fallDamageImmune.get(player.getName());
	    
	    plugin.getServer().getScheduler().cancelTask(current);
	}

	fallDamageImmune.put(player.getName(), startImmuneRemovalDelay(player));
    }
    
    public boolean isFallDamageImmune(final Player player)
    {
	return fallDamageImmune.containsKey(player.getName());
    }
    
    public int startImmuneRemovalDelay(final Player player)
    {
	final String name = player.getName();
	
	return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	{
	    public void run()
	    {
		fallDamageImmune.remove(name);
	    }
	}, 15 * 20L);
    }
}
