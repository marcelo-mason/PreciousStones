package com.bukkit.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.List;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Field;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.Helper;
import com.bukkit.Phaed.PreciousStones.ChatBlock;
import com.bukkit.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

public class CommunicatonManager
{
    private transient PreciousStones plugin;
    
    public CommunicatonManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void notifyPlaceU(Player player, Block fieldblock)
    {
	if (plugin.settings.notifyPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed an unbreakable block [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed " + fieldblock.getType() + " unbreakable block");
	}
    }
    
    public void notifyPlaceFF(Player player, Block fieldblock)
    {
	if (plugin.settings.notifyPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Force-field placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed a force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed " + fieldblock.getType() + " force-field");
	}
    }
    
    public void notifyPlaceBreakableFF(Player player, Block fieldblock)
    {
	if (plugin.settings.notifyPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Breakable force-field placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed a breakable force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed breakable " + fieldblock.getType() + " force-field");
	}
    }
    
    public void notifyDestroyU(Player player, Block fieldblock)
    {
	if (plugin.settings.notifyDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed his unbreakable block [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fieldblock.getType() + " unbreakable block");
	}
    }
    
    public void notifyDestroyFF(Player player, Block fieldblock)
    {
	if (plugin.settings.notifyDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Force-field destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed his force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fieldblock.getType() + " force-field");
	}
    }
    
    public void notifyDestroyBreakableFF(Player player, Block fieldblock)
    {
	String owner = plugin.ffm.getOwner(fieldblock);
	
	if (plugin.settings.notifyDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + owner + "'s breakable force-field destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed " + owner + "'s breakable force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destoyed " + owner + "'s breakable " + fieldblock.getType() + " force-field");
	}
    }
    
    public void notifyBypassPlace(Player player, Field field)
    {
	if (plugin.settings.notifyBypassPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-placed inside " + field.getOwner() + "'s force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logBypassPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-placed a block inside " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed a block inside " + field.getOwner() + "'s force-field");
	}
    }
    
    public void notifyBypassPlaceU(Player player, Field field)
    {
	if (plugin.settings.notifyBypassPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block bypass-placed inside " + field.getOwner() + "'s force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logBypassPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-placed an unbreakable block inside " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unbreakable block inside " + field.getOwner() + "'s force-field");
	}
    }
    
    public void notifyBypassDestroy(Player player, Field field)
    {
	if (plugin.settings.notifyBypassDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-destroyed in " + field.getOwner() + "'s force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed a block in " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block in " + field.getOwner() + "'s force-field");
	}
    }
    
    public void notifyBypassDestroyU(Player player, Block fieldblock)
    {
	String owner = plugin.ffm.getOwner(fieldblock);
	
	if (plugin.settings.notifyBypassDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + owner + "'s unbreakable block bypass-destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed " + owner + "'s unbreakable block [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed " + owner + "'s " + fieldblock.getType() + " unbreakable block");
	}
    }
    
    public void notifyBypassDestroyFF(Player player, Block fieldblock)
    {
	String owner = plugin.ffm.getOwner(fieldblock);
	
	if (plugin.settings.notifyBypassDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + owner + "'s force-field bypass-destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass-destroy"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed " + owner + "'s force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block in " + owner + "'s " + fieldblock.getType() + " force-field");
	}
    }
    
    public void warnEntry(Player player, Field field)
    {
	if (plugin.settings.warnEntry)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot enter protected area");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logEntry)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted entry into " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.entry"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted entry into " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnFire(Player player, Field field)
    {
	if (plugin.settings.warnFire)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place fires here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logFire)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to light fire in " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.fire"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to light a fire in " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnPlace(Player player, Field field)
    {
	if (plugin.settings.warnPlace)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted place a block in " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.place"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a block in " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnDestroyU(Player player, Block fieldblock)
    {
	String owner = plugin.ffm.getOwner(fieldblock);
	
	if (plugin.settings.warnDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy " + owner + "'s unbreakable block [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + owner + "'s " + fieldblock.getType() + " unbreakable block");
	}
    }
    
    public void warnDestroyFF(Player player, Block fieldblock)
    {
	String owner = plugin.ffm.getOwner(fieldblock);
	
	if (plugin.settings.warnDestroy)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy " + owner + "'s force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroy"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + owner + "'s " + fieldblock.getType() + " force-field");
	}
    }
    
    public void warnDestroyArea(Player player, Field field)
    {
	if (plugin.settings.warnDestroyArea)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot destroy here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logDestroyArea)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy a block inside " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroyarea"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy a block inside " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnConflictU(Player player, Field field)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logConflictPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to place an unbreakable block conflicting with " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.conflict"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable block conflicting with " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnConflictFF(Player player, Field field)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place force-field here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logConflictPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to place a force-field conflicting with " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.conflict"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a force-field conflicting with " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnPvP(Player attacker, Player victim, Field field)
    {
	if (plugin.settings.warnPvp)
	    attacker.sendMessage(ChatColor.AQUA + "PvP disabled in this area");
	
	if (PreciousStones.Permissions.has(attacker, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logPvp)
	    PreciousStones.log.info("[ps] " + attacker.getName() + " tried to attack " + victim.getName() + " in " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(attacker))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.pvp"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " tried to attack " + victim.getName() + " in " + field.getOwner() + "'s force-field");
	}
    }
    
    public void warnBypassPvP(Player attacker, Player victim, Field field)
    {
	if (plugin.settings.notifyBypassPvp)
	    attacker.sendMessage(ChatColor.AQUA + "PvP bypass");
	
	if (PreciousStones.Permissions.has(attacker, "preciousstones.admin.bypass"))
	    return;
	
	if (plugin.settings.logBypassPvp)
	    PreciousStones.log.info("[ps] " + attacker.getName() + " bypass-attack " + victim.getName() + " in " + field.getOwner() + "'s force-field [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(attacker))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.pvp"))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " bypass-attack " + victim.getName() + " in " + field.getOwner() + "'s force-field");
	}
    }
    
    public void notifyGuardDog(Player player, Field field, String property)
    {
	if (player != null)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Owners have been notified of your " + property);
	
	if (plugin.settings.notifyGuardDog)
	{
	    for (String allowed : field.getAllAllowed())
	    {
		List<Player> allowedpl = plugin.getServer().matchPlayer(allowed);
		
		for (Player pl : allowedpl)
		{
		    if (pl.getName().equals(allowed))
			ChatBlock.sendMessage(allowedpl.get(0), ChatColor.YELLOW + Helper.capitalize(property) + " detected at " + field.getVector());
		}
	    }
	}
    }
    
    public void showMenu(Player player)
    {
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.YELLOW + plugin.getDesc().getName() + " " + plugin.getDesc().getVersion());
	ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps allow [player] - Add player to the allowed list");
	ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps remove [player] - Remove player from the allowed list");
	ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps removeall [player] - Remove player from all your pstones");
	ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps allowall [player] - All player to all your pstones");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.info"))
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps info - Get info for the field youre standing on");
	}
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps delete - Delete the field(s) you're standing on");
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps delete [blockid] - Delete the field(s) from this type");
	}
    }
    
    public void showNotFound(Player player)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "No force-fields found");
    }
    
    public void showSlowDamage(Player player)
    {
	if (plugin.settings.warnSlowDamage)
	    ChatBlock.sendMessage(player, ChatColor.RED + "Health -1");
    }
    
    public void showFastDamage(Player player)
    {
	if (plugin.settings.warnFastDamage)
	    ChatBlock.sendMessage(player, ChatColor.RED + "Health -5");
    }
    
    public void showInstantHeal(Player player)
    {
	if (plugin.settings.warnInstantHeal)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "You have been healed.");
    }
    
    public void showSlowHeal(Player player)
    {
	if (plugin.settings.warnSlowHeal)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Health +1");
    }
    
    public void showOwner(Player player, Block block)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + plugin.ffm.getOwner(block));
    }
    
    public void showProtected(Player player)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Protected");
    }
    
    public void showProtectedLocation(Field field, Player player)
    {
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Protected");
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Source: [" + field.getVector().getX() + " " + field.getVector().getY() + " " + field.getVector().getZ() + "]");
    }
    
    public void showFieldDetails(Field field, Player player)
    {
	Vector fieldvec = field.getVector();
	ArrayList<String> allowed = field.getAllowed();
	
	Block fieldblock = player.getWorld().getBlockAt(fieldvec.getX(), fieldvec.getY(), fieldvec.getZ());
	
	FieldSettings fieldsettings = plugin.ffm.getFieldSettings(fieldblock);
	
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
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + plugin.ffm.getOwner(fieldblock));
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Allowed: " + out.substring(2));
	
	if (fieldsettings.radius > 0)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Dimensions: " + ((fieldvec.getRadius() * 2) + 1) + "x" + fieldvec.getHeight() + "x" + ((fieldvec.getRadius() * 2) + 1));
	
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Location: [" + fieldblock.getX() + " " + fieldblock.getY() + " " + fieldblock.getZ() + "]");
	
	String properties = "";
	
	if (fieldsettings.preventFire)
	    properties += ", fire";
	
	if (fieldsettings.preventEntry)
	    properties += ", entry";
	
	if (fieldsettings.preventPlace)
	    properties += ", place";
	
	if (fieldsettings.preventDestroy)
	    properties += ", destroy";
	
	if (fieldsettings.preventExplosions)
	    properties += ", explosions";
	
	if (fieldsettings.preventPvP)
	    properties += ", pvp";
	
	if (fieldsettings.guarddogMode)
	    properties += ", guard-dog-mode";
	
	if (fieldsettings.instantHeal)
	    properties += ", heal";
	
	if (fieldsettings.slowHeal)
	    properties += ", slow-heal";
	
	if (fieldsettings.slowDamage)
	    properties += ", slow-damage";
	
	if (fieldsettings.fastDamage)
	    properties += ", fast-damage";
	
	if (properties.length() > 0)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Properties: " + properties.substring(2));
    }
    
    public void debug(String msg)
    {
	PreciousStones.log.info("***** " + msg);
    }
}
