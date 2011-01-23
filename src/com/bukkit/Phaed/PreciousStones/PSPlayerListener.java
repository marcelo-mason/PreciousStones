package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;
import com.bukkit.Phaed.PreciousStones.PSettings.PStone;

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
	
	if (plugin.pm.isPStoneType(block))
	{
	    if (plugin.pm.isPStone(block))
		player.sendMessage(ChatColor.AQUA + "Owner: " + plugin.pm.getOwner(block));
	    
	    boolean isOwner = plugin.pm.getOwner(block).equals(player.getName());
	    
	    if (plugin.psettings.publicBlockDetails || isOwner)
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
		
		if (isOwner)
		{
		    PStone psettings = plugin.psettings.getPStoneSettings(block);
		    
		    if (psettings.radius > 0)
			player.sendMessage(ChatColor.AQUA + "Dimensions: " + psettings.radius + "x" + (psettings.radius + psettings.extraHeight) + "x" + psettings.radius);
		    
		    String protection = "";
		    
		    if (psettings.preventDestroy)
			protection += ", destroy";
		    
		    if (psettings.preventPlace)
			protection += ", place";
		    
		    if (psettings.preventEntry)
			protection += ", entry";
		    
		    if (psettings.preventExplosions)
			protection += ", explosions";
		    
		    if (psettings.preventFire)
			protection += ", fire";
		    
		    if (psettings.preventPvP)
			protection += ", pvp";
		    
		    protection = protection.length() > 0 ? protection.substring(2) : "none";
		    
		    player.sendMessage(ChatColor.AQUA + "Protection: " + protection);
		}
	    }
	}
	else if (plugin.um.isType(block))
	{
	    if (plugin.um.isPStone(block))
		player.sendMessage(ChatColor.YELLOW + "Owner: " + plugin.um.getOwner(block));
	}
	else
	{
	    // if protected area show message
	    
	    if (plugin.pm.isProtected(block, null))
		player.sendMessage(ChatColor.AQUA + "Protected");
	}
    }
    
    public void onPlayerMove(PlayerMoveEvent event)
    {
	Location from = event.getFrom();
	Location to = event.getTo();
	
	if ((new Vector(from).equals(new Vector(to))))
	    return;
	
	Player player = event.getPlayer();
	Location loc = player.getLocation();
	Block block = plugin.getServer().getWorlds()[0].getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	Block source = plugin.pm.getProtectedAreaSource(block, player.getName());
	PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
	
	if (psettings != null && psettings.preventEntry)
	{
	    int sx = 0;
	    int sz = 0;
	    int x = 0;
	    int z = 0;
	    
	    if (to.getBlockX() > source.getX())
		sx = -1;
	    else if (to.getBlockX() < source.getX())
		sx = 1;
	    else if (to.getBlockZ() > source.getZ())
		sz = -1;
	    else
		sz = 1;
	    
	    if (to.getBlockX() > from.getBlockX())
		x = -1;
	    else if (to.getBlockX() < from.getBlockX())
		x = 1;
	    else if (to.getBlockZ() > from.getBlockZ())
		z = -1;
	    else
		z = 1;
	    
	    // dont teleport if running away from force field source
	    
	    if (sx != 0 && sx == x)
		return;
	    
	    if (sz != 0 && sz == z)
		return;
	    
	    block = plugin.getServer().getWorlds()[0].getBlockAt(from.getBlockX(), from.getBlockY(), from.getBlockZ());
	    
	    int count = 0;
	    
	    while (plugin.pm.isProtectedAreaForEntry(block, player.getName()))
	    {
		block = plugin.getServer().getWorlds()[0].getBlockAt(block.getX() + x, block.getY() + (count > 30 ? 2 : 0), block.getZ() + z);
		
		// failsafe
		
		if (count > 150)
		{
		    block = plugin.getServer().getWorlds()[0].getBlockAt(0, 70, 0);
		    break;
		}
		count++;
	    }
	    
	    if (count == 0)
		return;
	    
	    loc = block.getLocation();
	    loc.setX(loc.getBlockX() + .5);
	    loc.setZ(loc.getBlockZ() + .5);
	    loc.setPitch(player.getLocation().getPitch());
	    loc.setYaw(player.getLocation().getYaw());
	    
	    player.teleportTo(loc);
	    PreciousStones.log.info("F[" + from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + "] TO[" + to.getBlockX() + " " + to.getBlockY() + " " + to.getBlockZ() + "]  T[" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]");
	    
	    if (plugin.psettings.warnEntry)
		player.sendMessage(ChatColor.AQUA + "Cannot enter protected area");
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
		    
		    if (playerName.equals(player.getName()))
		    {
			player.sendMessage(ChatColor.AQUA + "Cannot add yourself to your own lists");
			return;
		    }
		    
		    int areaCount = 0;
		    
		    for (HashMap<Vector, ArrayList<String>> c : plugin.pm.chunkLists.values())
		    {
			for (ArrayList<String> allowed : c.values())
			{
			    if (allowed.size() > 0 && allowed.get(0).equals(player.getName()) && !allowed.contains(playerName))
			    {
				allowed.add(playerName);
				areaCount++;
			    }
			}
		    }
		    
		    if (areaCount == 0)
			player.sendMessage(ChatColor.AQUA + "No protection areas found");
		    
		    player.sendMessage(ChatColor.AQUA + playerName + " added to " + areaCount + " allowed lists");
		    plugin.writeProtection();
		    
		    return;
		}
		else if (split[1].equals("allow"))
		{
		    if (!plugin.pm.isOwnVector(block, player.getName()))
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
		    
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
		    
		    return;
		}
		else if (split[1].equals("remove"))
		{
		    if (!plugin.pm.isOwnVector(block, player.getName()))
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
		    
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
		    
		    return;
		}
	    }
	    else if (split.length == 2)
	    {
		if (split[1].equals("clean"))
		{
		    int pcount = 0;
		    int ucount = 0;
		    
		    for (HashMap<Vector, ArrayList<String>> c : plugin.pm.chunkLists.values())
		    {
			for (Vector vec : c.keySet())
			{
			    Block pstone = plugin.getServer().getWorlds()[0].getBlockAt(vec.getX(), vec.getY(), vec.getZ());
			    
			    PSettings ps = new PSettings();
			    
			    if(pstone != null && !ps.isPStoneType(pstone))
			    {
				plugin.pm.releaseStone(pstone);
				pcount++;
			    }
			}
		    }
		    
		    for (HashMap<Vector, String> c : plugin.um.chunkLists.values())
		    {
			for (Vector vec : c.keySet())
			{
			    Block pstone = plugin.getServer().getWorlds()[0].getBlockAt(vec.getX(), vec.getY(), vec.getZ());
			    
			    if(pstone != null && !plugin.um.isType(pstone))
			    {
				plugin.um.releaseStone(pstone);
				ucount++;
			    }
			}
		    }
		    
		    if(ucount > 0)
		    {
			plugin.writeUnbreakable();
			player.sendMessage(ChatColor.AQUA + "" + ucount + " unbreakable stones cleaned");
		    }
		    
		    if(pcount > 0)
		    {
			plugin.writeProtection();
			player.sendMessage(ChatColor.AQUA + "" + pcount + " protection stones cleaned");		    
		    }
		    
		    if(ucount == 0 && pcount == 0)
			player.sendMessage(ChatColor.AQUA + "Nothing to clean");		    
		    
		    return;
		}
	    }
	    
	    player.sendMessage(ChatColor.AQUA + "/pstone allow [player] - Add player to the allowed list");
	    player.sendMessage(ChatColor.AQUA + "/pstone remove [player] - Remove player from the allowed list");
	    player.sendMessage(ChatColor.AQUA + "/pstone allowall [player] - All player to all your pstones");
	    
	    if (plugin.psettings.isBypass(player))
	    {
		player.sendMessage(ChatColor.RED + "/pstone clean - Delete force-fields without sources");
	    }
	}
    }
}
