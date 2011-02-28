package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.HashSet;

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
    private PreciousStones plugin;
    
    private final HashMap<String, Field> entries = new HashMap<String, Field>();
    
    public EntryManager(PreciousStones plugin)
    {
	this.plugin = plugin;
	
	startScheduler();
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
		    
		    if (plugin.pm.hasPermission(player, "preciousstones.benefit.heal"))
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
				player.setHealth(Math.max(player.getHealth() + 1, 20));
				plugin.cm.showSlowHeal(player);
				continue;
			    }
			    
			}
		    }
		    
		    if (!plugin.pm.hasPermission(player, "preciousstones.bypass.damage"))
		    {
			if (!(plugin.settings.sneakingBypassesDamage && player.isSneaking()))
			{
			    if (!field.isAllAllowed(playername))
			    {
				if (fieldsettings.slowDamage)
				{
				    if (player.getHealth() > 0)
				    {
					player.setHealth(Math.min(player.getHealth() - 1, 0));
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
					    player.setHealth(Math.min(player.getHealth() - 2, 0));
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
    
    public HashSet<String> getInhabitants(Field field)
    {
	HashSet<String> inhabitants = new HashSet<String>();
	
	for (String playername : entries.keySet())
	{
	   Field testField = entries.get(playername);
	   
	   if(field.equals(testField))
	   {
	       inhabitants.add(playername);
	   }
	}
	
	return inhabitants;
    }
}
