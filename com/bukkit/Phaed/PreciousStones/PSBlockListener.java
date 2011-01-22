package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

/**
 * PreciousStones block listener
 * 
 * @author Phaed
 */
public class PSBlockListener extends BlockListener
{
    private final PreciousStones plugin;
    
    public PSBlockListener(final PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void onBlockIgnite(BlockIgniteEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (plugin.pm.isProtectedAreaForFire(block, player.getName()))
	{
	    event.setCancelled(true);
	    
	    if (plugin.psettings.warnFire)
		player.sendMessage(ChatColor.AQUA + "Cannot place fires here");
	}
    }
    
    public void onBlockRightClick(BlockRightClickEvent event)
    {
	Player player = event.getPlayer();
	Block block = event.getBlock();
	
	if (block == null || player == null)
	    return;
	
	// check if its one of the stones
	
	if (plugin.pm.isPStoneType(block))
	{
	    if (plugin.pm.isPStone(block))
		player.sendMessage(ChatColor.AQUA + "Owner: " + plugin.pm.getOwner(block));
	    
	    if (plugin.psettings.publicAllowedList || plugin.pm.getOwner(block).equals(player.getName()))
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
    
    public void onBlockDamage(BlockDamageEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (event.getDamageLevel() == BlockDamageLevel.BROKEN)
	{
	    // check if its one of our protected block types
	    
	    if (plugin.um.isType(block))
	    {
		// check if we have the block type registered as a pstone
		
		if (plugin.um.isPStone(block))
		{
		    // if owner or bypass permission
		    
		    if (plugin.um.isOwner(block, player.getName()) || plugin.psettings.bypassPlayers.contains(player.getName()))
		    {
			String owner = plugin.um.getOwner(block);
			
			// remove the block from stones list
			
			plugin.um.releaseStone(block);
			plugin.writeUnbreakable();
			
			if (plugin.psettings.bypassPlayers.contains(player.getName()))
			{
			    if (plugin.psettings.notifyBypassDestroy)
				player.sendMessage(ChatColor.YELLOW + owner + "'s unbreakable block removed");
			    
			    if (plugin.psettings.logBypassDestroy)
				PreciousStones.log.info("PreciousStones: Unbreakable block [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
			else
			{
			    if (plugin.psettings.notifyDestroy)
				player.sendMessage(ChatColor.YELLOW + "Unbreakable block removed");
			    
			    if (plugin.psettings.logDestroy)
				PreciousStones.log.info("PreciousStones: Unbreakable block [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.psettings.warnDestroyPStone)
			    player.sendMessage(ChatColor.YELLOW + "Only the owner can remove this block");
		    }
		}
	    }
	    else if (plugin.pm.isPStoneType(block))
	    {
		// check if we have the block type registered as a stone
		
		if (plugin.pm.isPStone(block))
		{
		    // if owner or bypass permission
		    
		    if (plugin.pm.isOwner(block, player.getName()) || plugin.psettings.bypassPlayers.contains(player.getName()))
		    {
			String owner = plugin.um.getOwner(block);
			
			// remove the block from stones list
			
			plugin.pm.releaseStone(block);
			plugin.writeProtection();
			
			if (plugin.psettings.bypassPlayers.contains(player.getName()))
			{
			    if (plugin.psettings.notifyBypassDestroy)
				player.sendMessage(ChatColor.AQUA + owner + "'s protection block removed");
			    
			    if (plugin.psettings.logBypassDestroy)
				PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
			else
			{
			    if (plugin.psettings.notifyDestroy)
				player.sendMessage(ChatColor.AQUA + "Protection block removed");
			    
			    if (plugin.psettings.logDestroy)
				PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.psettings.warnDestroyPStone)
			    player.sendMessage(ChatColor.AQUA + "Only the owner can remove this block");
		    }
		}
	    }
	    else
	    {
		// if protected area prevent breaking
		
		if (plugin.pm.isProtected(block, player.getName()))
		{
		    event.setCancelled(true);
		    
		    if (plugin.psettings.warnDestroy)
			player.sendMessage(ChatColor.AQUA + "This area is protected");
		}
	    }
	}
    }
    
    public void onBlockPlace(BlockPlaceEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	// prevent placement in build protected area by non owners
	
	if (plugin.pm.isBuildProtected(block, player.getName()))
	{
	    event.setCancelled(true);
	    
	    if (plugin.psettings.warnPlace)
		player.sendMessage(ChatColor.AQUA + "Cannot build here");
	    
	    return;
	}
	
	// add the stones if they are one of the types
	
	if (plugin.pm.isPStoneType(block))
	{
	    // prevent placement of protection stone near unbreakable or
	    // protection stone of different owner
	    
	    if (plugin.pm.isInConflict(block, player.getName()))
	    {
		event.setCancelled(true);
		player.sendMessage(ChatColor.AQUA + "Cannot place protection block here");
		return;
	    }
	    
	    plugin.pm.addStone(block, player.getName());
	    plugin.writeProtection();
	    
	    if (plugin.psettings.notifyPlace)
		player.sendMessage(ChatColor.AQUA + "Protection block placed");
	    
	    if (plugin.psettings.logPlace)
		PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	}
	else if (plugin.um.isType(block))
	{
	    // prevent placement of unbreakable stone near
	    // protection stone of different owner
	    
	    if (plugin.pm.isInConflict(block, player.getName()))
	    {
		event.setCancelled(true);
		player.sendMessage(ChatColor.AQUA + "Cannot place unbreakable block here");
		return;
	    }
	    
	    plugin.um.addStone(block, player.getName());
	    plugin.writeUnbreakable();
	    
	    if (plugin.psettings.notifyPlace)
		player.sendMessage(ChatColor.YELLOW + "Unbreakable block placed");
	    
	    if (plugin.psettings.logPlace)
		PreciousStones.log.info("PreciousStones: Unbreakable block [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	}
    }
}
