package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
    
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (block == null || player == null)
	    return;
	
	if (plugin.pm.isFireProtected(block, player.getName()))
	{
	    event.setCancelled(true);
	    
	    if (plugin.psettings.warnFire)
		player.sendMessage(ChatColor.AQUA + "Cannot place fires here");
	}
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (block == null || player == null)
	    return;
	
	if (event.getDamageLevel() == BlockDamageLevel.BROKEN)
	{
	    // check if its one of our protected block types
	    
	    if (plugin.um.isType(block))
	    {
		// look for the block in our pstone collection
		
		if (plugin.um.isPStone(block))
		{
		    // if owner or bypass permission
		    
		    if (plugin.um.isOwner(block, player.getName()))
		    {
			// remove the block from stones list
			
			plugin.um.releaseStone(block);
			plugin.writeUnbreakable();
			
			if (plugin.psettings.notifyDestroy)
			    player.sendMessage(ChatColor.YELLOW + "Unbreakable block removed");
			
			if (plugin.psettings.logDestroy)
			    PreciousStones.log.info("PreciousStones: Unbreakable block [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
			
		    }
		    else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.stones.unbreakable"))
		    {
			String owner = plugin.um.getOwner(block);
			
			// remove the block from stones list
			
			plugin.um.releaseStone(block);
			plugin.writeUnbreakable();
			
			if (plugin.psettings.notifyBypassDestroy)
			    player.sendMessage(ChatColor.YELLOW + owner + "'s unbreakable block removed");
			
			if (plugin.psettings.logBypassDestroy)
			    PreciousStones.log.info("PreciousStones: Unbreakable block [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.psettings.warnDestroy)
			    player.sendMessage(ChatColor.YELLOW + "Only the owner can remove this block");
		    }
		}
	    }
	    else if (plugin.pm.isPStoneType(block))
	    {
		// look for the block in our pstone collection
		
		if (plugin.pm.isPStone(block))
		{
		    if (plugin.pm.isBreakable(block))
		    {
			plugin.pm.releaseStone(block);
			plugin.writeProtection();
			
			if (plugin.psettings.notifyDestroy)
			    player.sendMessage(ChatColor.AQUA + "Breakable protection block removed");

			if (plugin.psettings.logDestroy)
			    PreciousStones.log.info("PreciousStones: Breakable protection block [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
		    }
		    else if (plugin.pm.isOwner(block, player.getName()))
		    {
			// remove the block from stones list
			
			plugin.pm.releaseStone(block);
			plugin.writeProtection();
			
			if (plugin.psettings.notifyDestroy)
			    player.sendMessage(ChatColor.AQUA + "Protection block removed");
			
			if (plugin.psettings.logDestroy)
			    PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
		    }
		    else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.stones.protection"))
		    {
			String owner = plugin.pm.getOwner(block);
			
			// remove the block from stones list
			
			plugin.pm.releaseStone(block);
			plugin.writeProtection();
			
			if (plugin.psettings.notifyBypassDestroy)
			    player.sendMessage(ChatColor.AQUA + owner + "'s protection block removed");
			
			if (plugin.psettings.logBypassDestroy)
			    PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] bypass-removed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
		    }
		    else
		    {
			// prevent from breaking
			
			event.setCancelled(true);
			
			if (plugin.psettings.warnDestroy)
			    player.sendMessage(ChatColor.AQUA + "Only the owner can remove this block");
		    }
		}
	    }
	    else
	    {
		// if protected area prevent breaking
		
		if (plugin.pm.isDestroyProtected(block, player.getName()) && !plugin.psettings.isBypassBlock(block))
		{
		    if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.destroy"))
		    	event.setCancelled(true);
		    if (plugin.psettings.warnDestroyArea)
			player.sendMessage(ChatColor.AQUA + "This area is protected");
		}
	    }
	}
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (block == null || player == null)
	    return;
	
	// prevent placement in build protected area by non owners
	
	if (plugin.pm.isPlaceProtected(block, player.getName()) && !plugin.psettings.isBypassBlock(block))
	{
	    if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.place"))
		event.setCancelled(true);
	    
	    if (plugin.psettings.warnPlace)
		player.sendMessage(ChatColor.AQUA + "Cannot build here");
	    
	    return;
	}
	
	// add the stones if they are one of the types
	
	if (plugin.pm.isPStoneType(block) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.protection"))
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
	    
	    if (plugin.pm.isBreakable(block))
	    {
		if (plugin.psettings.notifyPlace)
		    player.sendMessage(ChatColor.AQUA + "Breakable protection block placed");
		
		if (plugin.psettings.logPlace)
		    PreciousStones.log.info("PreciousStones: Breakable protection block [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	    }
	    else
	    {
		if (plugin.psettings.notifyPlace)
		    player.sendMessage(ChatColor.AQUA + "Protection block placed");

		if (plugin.psettings.logPlace)
		    PreciousStones.log.info("PreciousStones: Protection block [" + block.getType() + "] placed by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
	    }
	}
	else if (plugin.um.isType(block) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.unbreakable"))
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
