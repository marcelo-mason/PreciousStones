package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

public class PlayerManager
{
    private PreciousStones plugin;
    private HashMap<String, PlayerStatus> players = new HashMap<String, PlayerStatus>();
    
    public PlayerManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public boolean isDisabled(Player player)
    {
	PlayerStatus ps = players.get(player.getName());
	
	if (ps == null)
	{
	    return plugin.settings.offByDefault;
	}
	else
	{
	    return ps.getDisabled();
	}
    }
    
    public void setDisabled(Player player, boolean disabled)
    {
	PlayerStatus ps = players.get(player.getName());
	
	if (ps == null)
	{
	    ps = new PlayerStatus();
	    ps.setDisabled(disabled);
	    
	    players.put(player.getName(), ps);
	}
	else
	{
	    ps.setDisabled(disabled);
	}
    }
    
    private class PlayerStatus
    {
	boolean disabled = false;
	
	public boolean getDisabled()
	{
	    return this.disabled;
	}
	
	public void setDisabled(boolean disabled)
	{
	    this.disabled = disabled;
	}
    }
    
    public void dropInventory(Player player)
    {
	PlayerInventory inv = player.getInventory();
	
	if (inv != null)
	{
	    for (int i = 0; i < inv.getSize(); i++)
	    {
		ItemStack stack = inv.getItem(i);
		
		if (stack != null)
		{
		    player.getWorld().dropItemNaturally(player.getLocation(), stack);
		}
	    }
	}
    }
}
