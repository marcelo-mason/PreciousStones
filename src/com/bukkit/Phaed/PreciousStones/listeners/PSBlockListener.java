package com.bukkit.Phaed.PreciousStones.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Field;

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
	
	// if fire protected cancel ignite
	
	Field field = plugin.ffm.isFireProtected(block, player);
	
	if (field != null)
	{
	    event.setCancelled(true);
	    plugin.cm.warnFire(player, field);
	}
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event)
    {
	Block damagedblock = event.getBlock();
	Player player = event.getPlayer();
	
	if (damagedblock == null || player == null)
	    return;
	
	if (event.getDamageLevel() == BlockDamageLevel.BROKEN)
	{
	    // do nothing for bypass blocks
	    
	    if (plugin.settings.isBypassBlock(damagedblock))
		return;
	    
	    // handle pstones and regular blocks
	    
	    if (plugin.um.isUnbreakableType(damagedblock) && plugin.um.isUnbreakable(damagedblock))
	    {
		if (plugin.um.isOwner(damagedblock, player.getName()))
		{
		    plugin.cm.notifyDestroyU(player, damagedblock);
		    plugin.um.release(damagedblock);
		}
		else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.stones.unbreakable"))
		{
		    plugin.cm.notifyBypassDestroyU(player, damagedblock);
		    plugin.um.release(damagedblock);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnDestroyU(player, damagedblock);
		}
	    }
	    else if (plugin.ffm.isFieldType(damagedblock) && plugin.ffm.isField(damagedblock))
	    {
		// look for the block in our pstone collection
		
		if (plugin.ffm.isBreakable(damagedblock))
		{
		    plugin.cm.notifyDestroyBreakableFF(player, damagedblock);
		    plugin.ffm.release(damagedblock);
		}
		else if (plugin.ffm.isOwner(damagedblock, player.getName()))
		{
		    plugin.cm.notifyDestroyFF(player, damagedblock);
		    plugin.ffm.release(damagedblock);
		}
		else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.stones.forcefield"))
		{
		    plugin.cm.notifyBypassDestroyFF(player, damagedblock);
		    plugin.ffm.release(damagedblock);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnDestroyFF(player, damagedblock);
		}
	    }
	    else
	    {
		Field field = plugin.ffm.isDestroyProtected(damagedblock, player);
		
		if (field != null)
		{
		    if (PreciousStones.Permissions.has(player, "preciousstones.bypass.destroy"))
		    {
			plugin.cm.notifyBypassDestroy(player, field);
		    }
		    else
		    {
			event.setCancelled(true);
			plugin.cm.warnDestroyArea(player, field);
		    }
		}
	    }
	}
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
	Block placedblock = event.getBlock();
	Player player = event.getPlayer();
	
	if (placedblock == null || player == null)
	    return;
	
	// do nothing for bypass block placements
	
	if (plugin.settings.isBypassBlock(placedblock))
	    return;
	
	// add the stones if they are one of the types
	
	if (plugin.um.isUnbreakableType(placedblock) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.unbreakable"))
	{
	    Field field = plugin.ffm.isInConflict(placedblock, player.getName());
	    
	    if (field != null)
	    {
		if (PreciousStones.Permissions.has(player, "preciousstones.bypass.place"))
		{
		    plugin.um.add(placedblock, player.getName());
		    plugin.cm.notifyBypassPlaceU(player, field);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnConflictU(player, field);
		}
	    }
	    else
	    {
		if (plugin.settings.chestNoTouch && plugin.ffm.touchingChest(placedblock))
		{
		    event.setCancelled(true);
		    plugin.cm.warnPlace(player, plugin.ffm.getField(placedblock));
		}
		else
		{
		    plugin.um.add(placedblock, player.getName());
		    plugin.cm.notifyPlaceU(player, placedblock);
		}
	    }
	    return;
	}
	else if (plugin.ffm.isFieldType(placedblock) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.forcefield"))
	{
	    Field field = plugin.ffm.isInConflict(placedblock, player.getName());
	    
	    if (field != null)
	    {
		event.setCancelled(true);
		plugin.cm.warnConflictFF(player, field);
	    }
	    else
	    {
		plugin.ffm.add(placedblock, player.getName());
		
		if (plugin.ffm.isBreakable(placedblock))
		    plugin.cm.notifyPlaceBreakableFF(player, placedblock);
		else
		    plugin.cm.notifyPlaceFF(player, placedblock);
	    }
	    
	    return;
	}
	
	Field field = plugin.ffm.isPlaceProtected(placedblock, player);
	
	if (field != null)
	{
	    // if bypass player then let him place
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.bypass.place"))
	    {
		plugin.cm.notifyBypassPlace(player, field);
	    }
	    else
	    {
		event.setCancelled(true);
		plugin.cm.warnPlace(player, field);
	    }
	}
    }
}
