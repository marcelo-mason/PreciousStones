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
    
    @Override
    public void onPlayerItem(PlayerItemEvent event)
    {
	Player player = event.getPlayer();
	Block block = event.getBlockClicked();
	
	if (block == null || player == null)
	    return;
	
	// check if its one of the stones types
	
	if (plugin.pm.isPStoneType(block))
	{
	    // look to see if its one of the psones in our collection
	    
	    if (plugin.pm.isPStone(block))
	    {
		player.sendMessage(ChatColor.AQUA + "Owner: " + plugin.pm.getOwner(block));
		
		// show details if owner or details are public
		
		if (plugin.pm.isOwner(block, player.getName()) || plugin.psettings.publicBlockDetails)
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
		    
		    if (protection.length() > 0)
		    {
			player.sendMessage(ChatColor.AQUA + "Protection: " + protection.substring(2));
		    }
		    
		    String properties = "";
		    
		    if (psettings.instantHeal)
			properties += ", heal";
		    
		    if (properties.length() > 0)
		    {
			player.sendMessage(ChatColor.AQUA + "Properties: " + properties.substring(2));
		    }
		}
	    }
	}
	else if (plugin.um.isType(block))
	{
	    // if its an indestructable block say it
	    
	    if (plugin.um.isPStone(block))
		player.sendMessage(ChatColor.YELLOW + "Owner: " + plugin.um.getOwner(block));
	}
	else
	{
	    // if protected area show message
	    
	    if (plugin.pm.isDestroyProtected(block, null))
		player.sendMessage(ChatColor.AQUA + "Protected");
	}
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
	Location from = event.getFrom();
	Location to = event.getTo();
	
	if ((new Vector(from).equals(new Vector(to))))
	    return;
	
	Player player = event.getPlayer();
	Location loc = player.getLocation();
	Block block = plugin.getServer().getWorlds()[0].getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	
	// all fields including ones owned by player
	
	HashMap<Vector, Block> sources = plugin.pm.getSourcePStone(block, null);
	
	boolean hasInstantHealed = false;
	boolean hasSlowHealed = false;
	
	for (Block source : sources.values())
	{
	    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
	    
	    // check if hes in a instant heal area
	    
	    if (psettings != null && !hasInstantHealed && psettings.instantHeal)
	    {
		if (player.getHealth() < 20)
		{
		    player.setHealth(20);
		    
		    if (plugin.psettings.warnInstantHeal)
			player.sendMessage(ChatColor.AQUA + "You have been healed.");
		}
		hasInstantHealed = true;
	    }
	    
	    // check if hes in a slow heal area
	    
	    if (psettings != null && !hasSlowHealed && psettings.slowHeal)
	    {
		if (player.getHealth() < 20)
		{
		    player.setHealth(player.getHealth() + 1);
		    
		    if (plugin.psettings.warnSlowHeal)
			player.sendMessage(ChatColor.AQUA + "Health +1");
		}
		hasSlowHealed = true;
	    }
	}
	
	// only fields not owned by player
	
	sources = plugin.pm.getSourcePStone(block, player.getName());
	
	boolean hasSlowDamaged = false;
	boolean hasBlockedEntry = false;
	
	for (Block source : sources.values())
	{
	    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
	    
	    // check if hes in a slow damage area

	    if (psettings != null && !hasSlowDamaged && psettings.slowDamage)
	    {
		player.setHealth(player.getHealth() - 1);
		
		if (plugin.psettings.warnSlowDamage)
		    player.sendMessage(ChatColor.RED + "Health -1");
		
		hasSlowDamaged = true;
	    }
	    
	    // check if hes in a block entry area
	    
	    if (psettings != null && !hasBlockedEntry && psettings.preventEntry)
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
		    continue;
		
		if (sz != 0 && sz == z)
		    continue;
		
		block = plugin.getServer().getWorlds()[0].getBlockAt(from.getBlockX(), from.getBlockY(), from.getBlockZ());
		
		int count = 0;
		
		while (plugin.pm.isEntryProtected(block, player.getName()))
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
		    continue;
		
		loc = block.getLocation();
		loc.setX(loc.getBlockX() + .5);
		loc.setZ(loc.getBlockZ() + .5);
		loc.setPitch(player.getLocation().getPitch());
		loc.setYaw(player.getLocation().getYaw());
		
		player.teleportTo(loc);
		PreciousStones.log.info("F[" + from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + "] TO[" + to.getBlockX() + " " + to.getBlockY() + " " + to.getBlockZ() + "]  T[" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]");
		
		if (plugin.psettings.warnEntry)
		    player.sendMessage(ChatColor.AQUA + "Cannot enter protected area");
		
		hasBlockedEntry = true;
	    }
	}
    }
    
    @Override
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
		    if (!plugin.pm.isInVector(block, player.getName()))
		    {
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
			return;
		    }
		    
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
		    if (!plugin.pm.isInVector(block, player.getName()))
		    {
			player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area you own");
			return;
		    }
		    
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
		if (split[1].equals("delete") && plugin.psettings.isBypass(player))
		{
		    HashMap<Vector, Block> sources = plugin.pm.getSourcePStone(block, null);
		    
		    for (Vector vec : sources.keySet())
		    {
			if (vec != null)
			{
			    plugin.pm.releaseStone(vec);
			    plugin.writeProtection();
			    
			    player.sendMessage(ChatColor.AQUA + "Protective field removed from pstone at " + vec.toString());
			    
			    if (plugin.psettings.logBypassDelete)
				PreciousStones.log.info("PreciousStones: Protective field removed from pstone by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
			else
			{
			    player.sendMessage(ChatColor.AQUA + "You must be standing in a protected area");
			}
		    }
		    return;
		}
		else if (split[1].equals("info") && plugin.psettings.isBypass(player))
		{
		    HashMap<Vector, Block> sources = plugin.pm.getSourcePStone(block, null);
		    
		    for (Vector vec : sources.keySet())
		    {
			if (vec != null)
			{
			    Block vecblock = plugin.getServer().getWorlds()[0].getBlockAt(vec.x, vec.y, vec.z);
			    
			    player.sendMessage(ChatColor.AQUA + "Owner: " + plugin.pm.getOwner(vecblock));
			    
			    // show details if owner or details are public
			    
			    ArrayList<String> allowed = plugin.pm.getAllowedList(vecblock);
			    
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
			    
			    PStone psettings = plugin.psettings.getPStoneSettings(vecblock);
			    
			    if (psettings.radius > 0)
				player.sendMessage(ChatColor.AQUA + "Dimensions: " + psettings.radius + "x" + (psettings.radius + psettings.extraHeight) + "x" + psettings.radius);
			    
			    player.sendMessage(ChatColor.AQUA + "Location: " + vec.toString());
			    
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
			
			continue;
		    }
		    
		    return;
		}
	    }
	    
	    player.sendMessage(ChatColor.AQUA + "/pstone allow [player] - Add player to the allowed list");
	    player.sendMessage(ChatColor.AQUA + "/pstone remove [player] - Remove player from the allowed list");
	    player.sendMessage(ChatColor.AQUA + "/pstone allowall [player] - All player to all your pstones");
	    
	    if (plugin.psettings.isBypass(player))
	    {
		player.sendMessage(ChatColor.RED + "/pstone info - Get info for the field youre standing on");
		player.sendMessage(ChatColor.RED + "/pstone delete - Delete the protective field(s) you're standing on");
	    }
	}
    }
}
