package com.bukkit.Phaed.PreciousStones.listeners;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Helper;
import com.bukkit.Phaed.PreciousStones.ChatBlock;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.Field;
import com.bukkit.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

/**
 * PreciousStones player listener
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
	
	// if bypass block, dont show message
	
	if (plugin.settings.isBypassBlock(block))
	    return;
	
	// check if its one of the stones types
	
	if (plugin.ffm.isFieldType(block) && plugin.ffm.isField(block))
	{
	    if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails)
		plugin.cm.showFieldDetails(plugin.ffm.getField(block), player);
	    else
		plugin.cm.showOwner(player, block);
	}
	else if (plugin.um.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
	{
	    plugin.cm.showOwner(player, block);
	}
	else
	{
	    Field field = plugin.ffm.isDestroyProtected(block, null);
	    
	    if (field != null)
	    {
		if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails)
		{
		    ArrayList<Field> fields = plugin.ffm.getSourceFields(block);
		    
		    for (Field fl : fields)
			plugin.cm.showProtectedLocation(fl, player);
		}
		else
		{
		    plugin.cm.showProtected(player);
		}
	    }
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
	
	// check if were on a healing field, owned by player or not
	
	ArrayList<Field> fields = plugin.ffm.getSourceFields(player);
	
	for (Field field : fields)
	{
	    FieldSettings fieldsettings = plugin.ffm.getFieldSettings(field, player.getWorld());
	    if (fieldsettings != null)
	    {
		if (PreciousStones.Permissions.has(player, "preciousstones.benefit.heal"))
		{
		    if (fieldsettings.instantHeal)
		    {
			if (player.getHealth() < 20)
			{
			    player.setHealth(20);
			    plugin.cm.showInstantHeal(player);
			    break;
			}
		    }
		    
		    if (fieldsettings.slowHeal)
		    {
			if (player.getHealth() < 20)
			{
			    player.setHealth(player.getHealth() + 1);
			    plugin.cm.showSlowHeal(player);
			    break;
			}
		    }
		}
	    }
	}
	
	// check if were on a damage field, only those not owned by player
	
	fields = plugin.ffm.getSourceFields(player, player.getName());
	
	for (Field field : fields)
	{
	    FieldSettings fieldsettings = plugin.ffm.getFieldSettings(field, player.getWorld());
	    if (fieldsettings != null)
	    {
		if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.damage"))
		{
		    if(plugin.settings.sneakingBypassesDamage && player.isSneaking())
			continue;
		    
		    if (fieldsettings.slowDamage)
		    {
			player.setHealth(player.getHealth() - 1);
			plugin.cm.showSlowDamage(player);
			break;
		    }
		    
		    if (fieldsettings.fastDamage)
		    {
			player.setHealth(player.getHealth() - 5);
			plugin.cm.showFastDamage(player);
			break;
		    }
		}
	    }
	}
	
	// check if were on a prevent entry field, only those not owned by player
	
	fields = plugin.ffm.getSourceFields(player, player.getName());
	
	for (Field field : fields)
	{
	    FieldSettings fieldsettings = plugin.ffm.getFieldSettings(field, player.getWorld());
	    if (fieldsettings != null)
	    {
		if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.entry"))
		{
		    if (fieldsettings.preventEntry)
		    {
			int sx = 0;
			int sz = 0;
			int x = 0;
			int z = 0;
			
			if (to.getBlockX() > field.getVector().getX())
			    sx = -1;
			else if (to.getBlockX() < field.getVector().getX())
			    sx = 1;
			else if (to.getBlockZ() > field.getVector().getZ())
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
			
			Block block = player.getWorld().getBlockAt(from.getBlockX(), from.getBlockY(), from.getBlockZ());
			
			int count = 0;
			
			Field fl = plugin.ffm.isEntryProtected(block, player);
			
			while (fl != null)
			{
			    block = player.getWorld().getBlockAt(block.getX() + x, block.getY() + (count > 30 ? 2 : 0), block.getZ() + z);
			    
			    // failsafe
			    
			    if (count > 150)
			    {
				block = player.getWorld().getBlockAt(0, 70, 0);
				break;
			    }
			    count++;
			}
			
			if (count == 0)
			    continue;
			
			Location loc = block.getLocation();
			loc.setX(loc.getBlockX() + .5);
			loc.setZ(loc.getBlockZ() + .5);
			loc.setPitch(player.getLocation().getPitch());
			loc.setYaw(player.getLocation().getYaw());
			
			player.teleportTo(loc);
			plugin.cm.warnEntry(player, fl);
			break;
		    }
		}
	    }
	}
    }
    
    @Override
    public void onPlayerCommand(PlayerChatEvent event)
    {
	String[] split = event.getMessage().split(" ");
	Player player = event.getPlayer();
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	
	if (split[0].equalsIgnoreCase("/pstone") || split[0].equalsIgnoreCase("/ps"))
	{
	    event.setCancelled(true);
	    
	    if (split.length == 3)
	    {
		if (split[1].equals("allowall") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.allowall"))
		{
		    String playerName = split[2];
		    
		    if (playerName.equals(player.getName()))
		    {
			int count = plugin.ffm.addAllowedAll(player.getName(), playerName);
			
			if (count == 0)
			    plugin.cm.showNotFound(player);
			else
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " added to " + count + " allowed lists");
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot add yourself to your own lists");
		    }
		    
		    return;
		}
		else if (split[1].equals("removeall") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.removeall"))
		{
		    String playerName = split[2];
		    
		    if (playerName.equals(player.getName()))
		    {
			int count = plugin.ffm.removeAllowedAll(player.getName(), playerName);
			
			if (count == 0)
			    plugin.cm.showNotFound(player);
			else
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " removed from " + count + " allowed lists");
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot remove yourself to your own lists");
		    }
		    
		    return;
		}
		else if (split[1].equals("allow") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.allow"))
		{
		    if (plugin.ffm.inOwnVector(block, player.getName()))
		    {
			String playerName = split[2];
			
			if (plugin.ffm.addAllowed(block, player.getName(), playerName))
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " added to allowed list");
			else
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " is already on the list");
		    }
		    else
		    {
			plugin.cm.showNotFound(player);
		    }
		    
		    return;
		}
		else if (split[1].equals("remove") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.remove"))
		{
		    if (!plugin.ffm.inOwnVector(block, player.getName()))
		    {
			String playerName = split[2];
			
			if (plugin.ffm.removeAllowed(block, player.getName(), playerName))
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " was removed from the allowed list");
			else
			    ChatBlock.sendMessage(player, ChatColor.RED + playerName + " not found or is the last player on the list");
		    }
		    else
		    {
			plugin.cm.showNotFound(player);
		    }
		    
		    return;
		}
		else if (split[1].equals("delete") && PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
		{
		    if (Helper.isInteger(split[2]))
		    {
			ArrayList<Field> fields = plugin.ffm.getFieldsOfType(Integer.parseInt(split[2]), player.getWorld());
			
			for (Field field : fields)
			    plugin.ffm.release(field.getVector(), player.getWorld());
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "" + fields.size() + " protective fields removed from pstones of type " + split[2]);
			
			if (plugin.settings.logBypassDelete)
			    PreciousStones.log.info("PreciousStones: " + fields.size() + " protective field removed from pstones of type " + split[2] + " by " + player.getName());
			
			return;
		    }
		}
	    }
	    else if (split.length == 2)
	    {
		if (split[1].equals("delete") && PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
		{
		    ArrayList<Field> fields = plugin.ffm.getSourceFields(block);
		    
		    for (Field field : fields)
		    {
			Vector fieldvec = field.getVector();
			plugin.ffm.release(fieldvec, player.getWorld());
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from pstone at " + fieldvec.toString());
			
			if (plugin.settings.logBypassDelete)
			    PreciousStones.log.info("PreciousStones: Protective field removed from pstone by " + player.getName() + " [" + block.getX() + " " + block.getY() + " " + block.getZ() + "]");
		    }
		    
		    if (fields.size() == 0)
			plugin.cm.showNotFound(player);
		    
		    return;
		}
		else if (split[1].equals("info") && PreciousStones.Permissions.has(player, "preciousstones.admin.info"))
		{
		    ArrayList<Field> fields = plugin.ffm.getSourceFields(block);
		    
		    for (Field field : fields)
			plugin.cm.showFieldDetails(field, player);
		    
		    if (fields.size() == 0)
			plugin.cm.showNotFound(player);
		    
		    return;
		}
		else if (split[1].equals("reload") && PreciousStones.Permissions.has(player, "preciousstones.admin.reload"))
		{
		    plugin.loadConfiguration();
		    plugin.sm.load();
		    
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "Configuration and pstone files reloaded");
		    return;
		}
	    }
	    
	    plugin.cm.showMenu(player);
	}
    }
    
}
