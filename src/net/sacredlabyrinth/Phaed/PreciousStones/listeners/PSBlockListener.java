package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashSet;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
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
    public void onBlockFlow(BlockFromToEvent event)
    {
	Field fromfield = plugin.ffm.isPlaceProtected(event.getBlock(), null);
	Field tofield = plugin.ffm.isPlaceProtected(event.getToBlock(), null);
	
	if (fromfield == null && tofield != null)
	{
	    event.setCancelled(true);
	}
    }
    
    @Override
    public void onBlockInteract(BlockInteractEvent event)
    {
	if (event.getEntity() instanceof Player)
	{
	    Player player = (Player) event.getEntity();
	    Block block = event.getBlock();
	    
	    plugin.snm.recordSnitchUsed(player, block);
	}
    }
    
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	
	if (block == null)
	{
	    return;
	}
	
	if (player != null)
	{
	    plugin.snm.recordSnitchIgnite(player, block);
	}
	
	Field field = plugin.ffm.isFireProtected(block, player);
	
	if (field != null)
	{
	    event.setCancelled(true);
	    
	    if (player != null)
	    {
		plugin.cm.warnFire(player, field);
	    }
	}
    }
    
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
    {
	Block redstoneblock = event.getBlock();
	
	for (int x = -1; x <= 1; x++)
	{
	    for (int y = -1; y <= 1; y++)
	    {
		for (int z = -1; z <= 1; z++)
		{
		    if (x == 0 && y == 0 && z == 0)
		    {
			continue;
		    }
		    
		    Block fieldblock = redstoneblock.getRelative(x, y, z);
		    
		    if (plugin.settings.isFieldType(fieldblock))
		    {
			if (plugin.ffm.isField(fieldblock))
			{
			    if (event.getNewCurrent() > event.getOldCurrent())
			    {				
				Field field = plugin.ffm.getField(fieldblock);
				FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
				
				if (fieldsettings.bounce)
				{
				    HashSet<String> players = plugin.em.getInhabitants(field);
				    
				    for (String pl : players)
				    {
					Player player = Helper.matchExactPlayer(plugin, pl);
					
					if (player != null)
					{
					    plugin.vm.bouncePlayer(player, field);
					}
				    }
				}
				
				if (fieldsettings.launch)
				{
				    HashSet<String> players = plugin.em.getInhabitants(field);
				    
				    for (String pl : players)
				    {
					Player player = Helper.matchExactPlayer(plugin, pl);
					
					if (player != null)
					{
					    plugin.cm.debug("pl");
					    plugin.vm.launchPlayer(player, field);
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
	Block damagedblock = event.getBlock();
	Player player = event.getPlayer();
	
	if (damagedblock == null || player == null)
	{
	    return;
	}
	
	if (plugin.settings.isBypassBlock(damagedblock))
	{
	    return;
	}
	
	plugin.snm.recordSnitchBlockBreak(player, damagedblock);
	
	if (plugin.settings.isUnbreakableType(damagedblock) && plugin.um.isUnbreakable(damagedblock))
	{
	    if (plugin.um.isOwner(damagedblock, player.getName()))
	    {
		plugin.cm.notifyDestroyU(player, damagedblock);
		plugin.um.release(damagedblock);
	    }
	    else if (plugin.pm.hasPermission(player, "preciousstones.bypass.unbreakable"))
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
	    else if (plugin.settings.allowedCanBreakPstones && plugin.ffm.isAllowed(damagedblock, player.getName()))
	    {
		plugin.cm.notifyDestroyOthersFF(player, damagedblock);
		plugin.ffm.release(damagedblock);
	    }
	    else if (plugin.pm.hasPermission(player, "preciousstones.bypass.forcefield"))
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
		if (plugin.pm.hasPermission(player, "preciousstones.bypass.destroy"))
		{
		    plugin.cm.notifyBypassDestroy(player, damagedblock, field);
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnDestroyArea(player, damagedblock, field);
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
	{
	    return;
	}
	
	if (plugin.settings.isBypassBlock(placedblock))
	{
	    return;
	}
	
	plugin.snm.recordSnitchBlockPlace(player, placedblock);
	
	if (plugin.settings.isUnbreakableType(placedblock) && plugin.pm.hasPermission(player, "preciousstones.benefit.create.unbreakable"))
	{
	    Field conflictfield = plugin.ffm.unbreakableConflicts(placedblock, player);
	    
	    if (conflictfield != null)
	    {
		if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
		{
		    if (plugin.um.add(placedblock, player))
		    {
			plugin.cm.notifyBypassPlaceU(player, conflictfield);
		    }
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnConflictU(player, placedblock, conflictfield);
		}
	    }
	    else
	    {
		if (plugin.upm.touchingUnprotectableBlock(placedblock))
		{
		    if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
		    {
			if (plugin.um.add(placedblock, player))
			{
			    plugin.cm.notifyBypassTouchingUnprotectable(player, placedblock);
			}
		    }
		    else
		    {
			event.setCancelled(true);
			plugin.cm.warnPlaceTouchingUnprotectable(player, placedblock);
		    }
		}
		else
		{
		    if (plugin.um.add(placedblock, player))
		    {
			plugin.cm.notifyPlaceU(player, placedblock);
		    }
		}
	    }
	    return;
	}
	else if (plugin.settings.isFieldType(placedblock) && plugin.pm.hasPermission(player, "preciousstones.benefit.create.forcefield"))
	{
	    Field conflictfield = plugin.ffm.fieldConflicts(placedblock, player);
	    
	    if (conflictfield != null)
	    {
		event.setCancelled(true);
		plugin.cm.warnConflictFF(player, placedblock, conflictfield);
	    }
	    else
	    {
		if (plugin.upm.touchingUnprotectableBlock(placedblock))
		{
		    if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
		    {
			if (plugin.ffm.add(placedblock, player))
			{
			    plugin.cm.notifyBypassTouchingUnprotectable(player, placedblock);
			}
		    }
		    else
		    {
			event.setCancelled(true);
			plugin.cm.warnPlaceTouchingUnprotectable(player, placedblock);
		    }
		    return;
		}
		
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(placedblock.getTypeId());
		
		if (fieldsettings.preventUnprotectable)
		{
		    Block foundblock = plugin.upm.existsUnprotectableBlock(placedblock);
		    
		    if (foundblock != null)
		    {
			if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
			{
			    if (plugin.ffm.add(placedblock, player))
			    {
				plugin.cm.notifyBypassFieldInUnprotectable(player, foundblock, placedblock);
			    }
			}
			else
			{
			    event.setCancelled(true);
			    plugin.cm.warnPlaceFieldInUnprotectable(player, foundblock, placedblock);
			}
			return;
		    }
		}
		
		if (plugin.ffm.add(placedblock, player))
		{
		    if (plugin.ffm.isBreakable(placedblock))
		    {
			plugin.cm.notifyPlaceBreakableFF(player, placedblock);
		    }
		    else
		    {
			plugin.cm.notifyPlaceFF(player, placedblock);
		    }
		}
	    }
	    return;
	}
	else if (plugin.settings.isUnprotectableType(placedblock))
	{
	    Block unbreakableblock = plugin.um.touchingUnbrakableBlock(placedblock);
	    
	    if (unbreakableblock != null)
	    {
		if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
		{
		    if (plugin.um.add(placedblock, player))
		    {
			plugin.cm.notifyBypassUnprotectableTouching(player, placedblock, unbreakableblock);
		    }
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnPlaceUnprotectableTouching(player, placedblock, unbreakableblock);
		    return;
		}
	    }
	    
	    Block fieldblock = plugin.ffm.touchingFieldBlock(placedblock);
	    
	    if (fieldblock != null)
	    {
		if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
		{
		    if (plugin.um.add(placedblock, player))
		    {
			plugin.cm.notifyBypassUnprotectableTouching(player, placedblock, fieldblock);
		    }
		}
		else
		{
		    event.setCancelled(true);
		    plugin.cm.warnPlaceUnprotectableTouching(player, placedblock, fieldblock);
		    return;
		}
	    }
	    
	    Field field = plugin.ffm.isUprotectableBlockField(placedblock);
	    
	    if (field != null)
	    {
		if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
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
	    if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
	    {
		plugin.cm.notifyBypassPlace(player, field);
	    }
	    else
	    {
		event.setCancelled(true);
		plugin.cm.warnPlace(player, placedblock, field);
	    }
	}
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event)
    {
	Block scopedBlock = event.getBlock();
	Player player = event.getPlayer();
	ItemStack is = player.getItemInHand();
	
	if (scopedBlock == null || is == null || player == null)
	{
	    return;
	}
	
	Material materialInHand = is.getType();
	
	if (plugin.settings.isFieldType(materialInHand) && plugin.pm.hasPermission(player, "preciousstones.benefit.scoping"))
	{
	    if (!plugin.plm.isDisabled(player))
	    {
		HashSet<Field> touching = plugin.ffm.getTouchingFields(scopedBlock, materialInHand);
		plugin.cm.printTouchingFields(player, touching);
	    }
	}
    }
}
