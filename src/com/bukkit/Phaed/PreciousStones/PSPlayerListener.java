package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.block.Block;

/**
 * PreciousStones
 * 
 * @author Phaed
 */
public class PSPlayerListener extends PlayerListener
{
    private final PreciousStones plugin;
    
    public PSPlayerListener(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void onPlayerItem(PlayerItemEvent event)
    {
	Player player = event.getPlayer();
	Block block = event.getBlockClicked();
	
	if (block == null || player == null)
	    return;
	
	// check if its one of the stones
	
	if (plugin.pm.isType(block.getType()))
	{
	    if (plugin.pm.isStone(block))
		player.sendMessage(ChatColor.AQUA + "Owner: " + plugin.pm.getOwner(block));
	    
	    if (plugin.publicAllowedList || plugin.pm.getOwner(block).equals(player.getName()))
	    {
		ArrayList<String> allowed = plugin.pm.getAllowedList(block);
		
		String out = "";
		
		if (allowed.size() > 1)
		{
		    for (int i = 1; i < allowed.size(); i++)
		    {
			out += ", " + allowed.get(i);
		    }
		}
		else
		{
		    out = "  none";
		}
		
		player.sendMessage(ChatColor.AQUA + "Allowed: " + out.substring(2));
	    }
	}
	else if (plugin.um.isType(block.getType()))
	{
	    if (plugin.um.isStone(block))
		player.sendMessage(ChatColor.YELLOW + "Owner: " + plugin.um.getOwner(block));
	}
	else
	{
	    // if protected area show message
	    
	    if (plugin.pm.isProtected(block, null))
		player.sendMessage(ChatColor.AQUA + "Protected");
	}
    }
    
    public void onPlayerCommand(PlayerChatEvent event)
    {
	String[] split = event.getMessage().split(" ");
	Player player = event.getPlayer();
	Location loc = player.getLocation();
	Block block = plugin.getServer().getWorlds()[0].getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
	
	if (split[0].equalsIgnoreCase("/pstone"))
	{
	    event.setCancelled(true);
	    
	    if (split.length == 3)
	    {
		if (split[1].equals("allowall"))
		{
		    String playerName = split[2];
		    
		    int areaCount = 0;
		    
		    for (HashMap<Vector, ArrayList<String>> c : plugin.pm.chunkLists.values())
		    {
			for (ArrayList<String> allowed : c.values())
			{
			    if(!allowed.contains(playerName))
			    {
				allowed.add(playerName);			    
				areaCount++;
			    }
			}
		    }
		    
		    if (areaCount > 0)
		    {
			player.sendMessage(ChatColor.AQUA + playerName + " added to " + areaCount + " allowed lists");
			plugin.writeProtection();
		    }
		    else
		    {
			player.sendMessage(ChatColor.AQUA + "No protection areas found");
		    }
		    
		    return;
		}
		if (split[1].equals("allow"))
		{
		    if (plugin.pm.isOwnVector(block, player.getName()))
		    {
			String playerName = split[2];
			
			if (plugin.pm.addAllowed(block, playerName))
			{
			    player.sendMessage(ChatColor.AQUA + playerName + " added to allowed list");
			    plugin.writeProtection();
			}
			else
			{
			    player.sendMessage(ChatColor.AQUA + playerName + " is already on the list");
			}
		    }
		    else
		    {
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
		    }
		    
		    return;
		}
		else if (split[1].equals("remove"))
		{
		    if (plugin.pm.isOwnVector(block, player.getName()))
		    {
			String playerName = split[2];
			
			if (plugin.pm.removeAllowed(block, playerName))
			{
			    player.sendMessage(ChatColor.AQUA + playerName + " was removed from the allowed list");
			    plugin.writeProtection();
			}
			else
			{
			    player.sendMessage(ChatColor.RED + playerName + " not found or is the last player on the list");
			}
		    }
		    else
		    {
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
		    }
		    
		    return;
		}
	    }
	    
	    player.sendMessage(ChatColor.AQUA + "/stone allow [player] - Add player to the allowed list");
	    player.sendMessage(ChatColor.AQUA + "/stone remove [player] - Remove player from the allowed list");
	}
    }
}
