package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

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
	    if (plugin.settings.isBypassBlock(damagedblock))
		return;
	    
	    if (plugin.settings.isUnbreakableType(damagedblock) && plugin.um.isUnbreakable(damagedblock))
	    {
		if (plugin.um.isOwner(damagedblock, player.getName()))
		{
		    plugin.cm.notifyDestroyU(player, damagedblock);
		    plugin.um.release(damagedblock);
		}
		else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unbreakable"))
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
	    else if (plugin.settings.isFieldType(damagedblock) && plugin.ffm.isField(damagedblock))
	    {
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
		else if (PreciousStones.Permissions.has(player, "preciousstones.bypass.forcefield"))
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
	
	if (plugin.settings.isBypassBlock(placedblock))
	    return;
	
	if (plugin.settings.isUnbreakableType(placedblock) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.unbreakable"))
	{
	    Field conflictfield = plugin.ffm.isInConflict(placedblock, player.getName());
	    
	    if (conflictfield != null)
	    {
		if (PreciousStones.Permissions.has(player, "preciousstones.bypass.place"))
		{
		    plugin.um.add(placedblock, player.getName());
		    plugin.cm.notifyBypassPlaceU(player, conflictfield);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnConflictU(player, conflictfield);
		}
	    }
	    else
	    {
		if (plugin.upm.touchingUnprotectableBlock(placedblock))
		{
		    if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unprotectable"))
		    {
			plugin.um.add(placedblock, player.getName());
			plugin.cm.notifyBypassUnprotectableTouching(player, placedblock);
		    }
		    else
		    {
			event.setCancelled(true);
			plugin.cm.warnPlaceUnprotectableTouching(player, placedblock);
		    }
		}
		else
		{
		    plugin.um.add(placedblock, player.getName());
		    plugin.cm.notifyPlaceU(player, placedblock);
		}
	    }
	    return;
	}
	else if (plugin.settings.isFieldType(placedblock) && PreciousStones.Permissions.has(player, "preciousstones.benefit.create.forcefield"))
	{
	    Field conflictfield = plugin.ffm.isInConflict(placedblock, player.getName());
	    
	    if (conflictfield != null)
	    {
		event.setCancelled(true);
		plugin.cm.warnConflictFF(player, conflictfield);
	    }
	    else
	    {
		if (plugin.upm.touchingUnprotectableBlock(placedblock))
		{
		    if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unprotectable"))
		    {
			plugin.ffm.add(placedblock, player.getName());
			plugin.cm.notifyBypassUnprotectableTouching(player, placedblock);
		    }
		    else
		    {
			event.setCancelled(true);
			plugin.cm.warnPlaceUnprotectableTouching(player, placedblock);
		    }
		    return;
		}
		
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(placedblock.getTypeId());
		
		if (fieldsettings.preventUnprotectable)
		{
		    Block foundblock = plugin.upm.existsUnprotectableBlock(placedblock);
		    
		    if (foundblock != null)
		    {
			if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unprotectable"))
			{
			    plugin.ffm.add(placedblock, player.getName());
			    plugin.cm.notifyBypassFieldInUnprotectable(player, foundblock, placedblock);
			}
			else
			{
			    event.setCancelled(true);
			    plugin.cm.warnPlaceFieldInUnprotectable(player, foundblock, placedblock);
			}
			return;
		    }
		}
		
		plugin.ffm.add(placedblock, player.getName());
		
		if (plugin.ffm.isBreakable(placedblock))
		{
		    plugin.cm.notifyPlaceBreakableFF(player, placedblock);
		}
		else
		{
		    plugin.cm.notifyPlaceFF(player, placedblock);
		}
	    }
	    return;
	}
	else if (plugin.settings.isUnprotectableType(placedblock))
	{
	    if (plugin.um.touchingUnbrakableBlock(placedblock) || plugin.ffm.touchingFieldBlock(placedblock))
	    {
		if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unprotectable"))
		{
		    plugin.um.add(placedblock, player.getName());
		    plugin.cm.notifyBypassUnprotectableTouching(player, placedblock);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnPlaceUnprotectableTouching(player, placedblock);
		    return;
		}
	    }
	    
	    Field field = plugin.ffm.isUprotectableBlockField(placedblock);
	    
	    if (field != null)
	    {
		if (PreciousStones.Permissions.has(player, "preciousstones.bypass.unprotectable"))
		{
		    plugin.cm.notifyBypassPlaceUnprotectableInField(player, placedblock, field);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnPlaceUnprotectableInField(player, placedblock, field);
		}
	    }
	}
	
	Field field = plugin.ffm.isPlaceProtected(placedblock, player);
	
	if (field != null)
	{
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
