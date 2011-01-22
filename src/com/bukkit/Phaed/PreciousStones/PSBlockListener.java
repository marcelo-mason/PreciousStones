package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
    
    public void onBlockDamage(BlockDamageEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (event.getDamageLevel() == BlockDamageLevel.BROKEN)
	{
	    // check if its one of our protected stone types
	    
	    if (plugin.um.isType(block.getType()))
	    {
		// check if we have the block type registered as a stone
		
		if (plugin.um.isStone(block))
		{
		    // if owner or bypass permission
		    
		    if (plugin.um.isOwner(block, player.getName()) || plugin.bypassList.contains(player.getName()))
		    {
			String owner = plugin.um.getOwner(block);
			
			// remove the block from stones list
			
			plugin.um.releaseStone(block);
			plugin.writeUnbreakable();
			
			if (plugin.bypassList.contains(player.getName()))
			{
			    if (plugin.notifyBypassRemoval)
				player.sendMessage(ChatColor.YELLOW + owner + "'s unbreakable stone removed");
			    
			    if (plugin.logBypassRemoval)
				PreciousStones.log.info("PreciousStones: Unbreakable stone [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
			else
			{
			    if (plugin.notifyRemoval)
				player.sendMessage(ChatColor.YELLOW + "Unbreakable stone removed");
			    
			    if (plugin.logRemoval)
				PreciousStones.log.info("PreciousStones: Unbreakable stone [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.warnBreakAnothersStone)
			    player.sendMessage(ChatColor.YELLOW + "Only the owner can remove this block");
		    }
		}
	    }
	    else if (plugin.pm.isType(block.getType()))
	    {
		// check if we have the block type registered as a stone
		
		if (plugin.pm.isStone(block))
		{
		    // if owner or bypass permission
		    
		    if (plugin.pm.isOwner(block, player.getName()) || plugin.bypassList.contains(player.getName()))
		    {
			String owner = plugin.um.getOwner(block);
			
			// remove the block from stones list
			
			plugin.pm.releaseStone(block);
			plugin.writeProtection();
			
			if (plugin.bypassList.contains(player.getName()))
			{
			    if (plugin.notifyBypassRemoval)
				player.sendMessage(ChatColor.AQUA + owner + "'s protection stone removed");
			    
			    if (plugin.logBypassRemoval)
				PreciousStones.log.info("PreciousStones: Protection stone [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
			else
			{
			    if (plugin.notifyRemoval)
				player.sendMessage(ChatColor.AQUA + "Protection stone removed");
			    
			    if (plugin.logRemoval)
				PreciousStones.log.info("PreciousStones: Protection stone [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			}
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.warnBreakAnothersStone)
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
		    
		    if (plugin.warnBreakOnProtected)
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
	    
	    if (plugin.warnPlaceOnProtected)
		player.sendMessage(ChatColor.AQUA + "Cannot build here");
	    
	    return;
	}
	
	// add the stones if they are one of the types
	
	if (plugin.pm.isType(block.getType()))
	{
	    // prevent placement of protection stone near unbreakable or
	    // protection stone of different owner
	    
	    if (plugin.pm.isInConflict(block, player.getName()))
	    {
		event.setCancelled(true);
		player.sendMessage(ChatColor.AQUA + "Cannot place stone here");
		return;
	    }
	    
	    plugin.pm.addStone(block, player.getName());
	    plugin.writeProtection();
	    
	    if (plugin.notifyPlacement)
		player.sendMessage(ChatColor.AQUA + "Protection stone placed");
	    
	    if (plugin.logPlacement)
		PreciousStones.log.info("PreciousStones: Protection stone [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	}
	else if (plugin.um.isType(block.getType()))
	{
	    // prevent placement of unbreakable stone near
	    // protection stone of different owner
	    
	    if (plugin.pm.isInConflict(block, player.getName()))
	    {
		event.setCancelled(true);
		player.sendMessage(ChatColor.AQUA + "Cannot place stone here");
		return;
	    }
	    
	    plugin.um.addStone(block, player.getName());
	    plugin.writeUnbreakable();
	    
	    if (plugin.notifyPlacement)
		player.sendMessage(ChatColor.YELLOW + "Unbreakable stone placed");
	    
	    if (plugin.logPlacement)
		PreciousStones.log.info("PreciousStones: Unbreakable stone [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	}
    }
}
