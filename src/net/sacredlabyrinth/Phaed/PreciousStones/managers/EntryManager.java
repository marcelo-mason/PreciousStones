package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 * Handles what happens inside fields
 * 
 * @author Phaed
 */
public class EntryManager
{
    private transient PreciousStones plugin;
    
    private final HashMap<String, Field> entries = new HashMap<String, Field>();
    
    public EntryManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void startScheduler()
    {
	plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
	{
	    public void run()
	    {
		for (String playername : entries.keySet())
		{
		    Field field = entries.get(playername);
		    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		    Player player = Helper.matchExactPlayer(plugin, playername);
		    
		    if (player == null)
		    {
			continue;
		    }
		    
		    if (PreciousStones.Permissions.has(player, "preciousstones.benefit.heal"))
		    {
			if (fieldsettings.instantHeal)
			{
			    if (player.getHealth() < 20)
			    {
				player.setHealth(20);
				plugin.cm.showInstantHeal(player);
				continue;
			    }
			}
			
			if (fieldsettings.slowHeal)
			{
			    if (player.getHealth() < 20)
			    {
				player.setHealth(player.getHealth() + 1);
				plugin.cm.showSlowHeal(player);
				continue;
			    }
			    
			}
		    }
		    
		    if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.damage"))
		    {
			if (!(plugin.settings.sneakingBypassesDamage && player.isSneaking()))
			{
			    if (!playername.equals(field.getOwner()))
			    {
				if (fieldsettings.slowDamage)
				{
				    if (player.getHealth() > 0)
				    {
					player.setHealth(player.getHealth() - 1);
					plugin.cm.showSlowDamage(player);
					continue;
				    }
				}
				
				if (fieldsettings.fastDamage)
				{
				    if (player.getHealth() > 0)
				    {
					if (player.getHealth() >= 2)
					{
					    player.setHealth(player.getHealth() - 2);
					}
					else
					{
					    player.setHealth(0);
					}
					plugin.cm.showFastDamage(player);
					continue;
				    }
				}
			    }
			}
		    }
		}
	    }
	}, 0, 20L);
    }
    
    public void enter(Player player, Field field)
    {
	entries.put(player.getName(), field);
    }
    
    public void leave(Player player)
    {
	entries.remove(player.getName());
    }
    
    public boolean isInsideField(Player player)
    {
	return entries.containsKey(player.getName());
    }
    
    public Field getEnvelopingField(Player player)
    {
	return entries.get(player.getName());
    }
}
