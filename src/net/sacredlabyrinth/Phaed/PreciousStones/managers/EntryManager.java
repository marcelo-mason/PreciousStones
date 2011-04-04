package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.EntryFields;

/**
 * Handles what happens inside fields
 * 
 * @author Phaed
 */
public class EntryManager
{
    private PreciousStones plugin;
    
    private final HashMap<String, EntryFields> entries = new HashMap<String, EntryFields>();
    
    public EntryManager(PreciousStones plugin)
    {
	this.plugin = plugin;
	
	startScheduler();
    }
    
    public EntryFields getEntryFields(String name)
    {
	return entries.get(name);
    }
    
    public void startScheduler()
    {
	plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
	{
	    public void run()
	    {
		for (String playername : entries.keySet())
		{
		    EntryFields ef = entries.get(playername);
		    LinkedList<Field> fields = ef.getFields();
		    
		    for (Field field : fields)
		    {
			FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
			Player player = Helper.matchExactPlayer(plugin, playername);
			
			if (player == null)
			{
			    continue;
			}
			
			if (plugin.pm.hasPermission(player, "preciousstones.benefit.giveair"))
			{
			    if (fieldsettings.giveAir)
			    {
				if (player.getRemainingAir() < 300)
				{
				    player.setRemainingAir(600);
				    plugin.cm.showGiveAir(player);
				    continue;
				}
			    }
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
				    player.setHealth(healthCheck(player.getHealth() + 1));
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
					    player.setHealth(healthCheck(player.getHealth() - 1));
					    plugin.cm.showSlowDamage(player);
					    continue;
					}
				    }
				    
				    if (fieldsettings.fastDamage)
				    {
					if (player.getHealth() > 0)
					{
					    player.setHealth(healthCheck(player.getHealth() - 4));
					    plugin.cm.showFastDamage(player);
					    continue;
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}, 0, 20L);
    }
    
    public static Vector Reposition(Vector Pos, float Ang, float Hyp, float y)
    {
	float r = Ang * (float) Math.PI / 180.0f;
	float a = (float) (Math.sin(r)) * Hyp;
	float b = (float) (Math.cos(r)) * Hyp;
	return new Vector((double) (Pos.getX() + b), y, (double) (Pos.getZ() + a));
    }
    
    public static float Heading(Vector Origin, Vector Dest)
    {
	double ang = (double) Math.atan2((Dest.getZ() - Origin.getZ()), (Dest.getX() - Origin.getX()));
	return (float) Math.toDegrees(ang);
    }
    
    public LinkedList<Field> getPlayerEntryFields(Player player)
    {
	if (entries.containsKey(player.getName()))
	{
	    return entries.get(player.getName()).getFields();
	}
	
	return null;
    }
    
    public void enterField(Player player, Field field)
    {
	if (entries.containsKey(player.getName()))
	{
	    EntryFields ef = entries.get(player.getName());
	    ef.addField(field);
	}
	else
	{
	    entries.put(player.getName(), new EntryFields(field));
	}
	
	plugin.snm.recordSnitchEntry(player, field);
	
	if (!plugin.ffm.isRedstoneHookedDisabled(field))
	{
	    plugin.vm.launchPlayer(player, field);
	    plugin.vm.shootPlayer(player, field);
	}
	
	plugin.mm.enterMine(player, field);
    }
    
    public void leaveField(Player player, Field field)
    {
	EntryFields ef = entries.get(player.getName());
	ef.removeField(field);
	
	if (ef.size() == 0)
	{
	    entries.remove(player.getName());
	}
    }
    
    public boolean isInsideField(Player player, Field field)
    {
	EntryFields ef = entries.get(player.getName());
	
	if (ef == null)
	{
	    return false;
	}
	
	return ef.containsField(field);
    }
    
    public boolean containsSameNameOwnedField(Player player, Field field)
    {
	if (entries.containsKey(player.getName()))
	{
	    EntryFields ef = entries.get(player.getName());
	    LinkedList<Field> entryfields = ef.getFields();
	    
	    for (Field entryfield : entryfields)
	    {
		if (entryfield.getOwner().equals(field.getOwner()) && entryfield.getStoredName().equals(field.getStoredName()))
		{
		    return true;
		}
	    }
	}
	
	return false;
    }
    
    private int healthCheck(int health)
    {
	if (health < 0)
	{
	    return 0;
	}
	
	if (health > 20)
	{
	    return 20;
	}
	
	return health;
    }
    
    public HashSet<String> getInhabitants(Field field)
    {
	HashSet<String> inhabitants = new HashSet<String>();
	
	for (String playername : entries.keySet())
	{
	    EntryFields ef = entries.get(playername);
	    LinkedList<Field> fields = ef.getFields();
	    
	    for (Field testfield : fields)
	    {
		if (field.equals(testfield))
		{
		    inhabitants.add(playername);
		}
	    }
	}
	
	return inhabitants;
    }
}
