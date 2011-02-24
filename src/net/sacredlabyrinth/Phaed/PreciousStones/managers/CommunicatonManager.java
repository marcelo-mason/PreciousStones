package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.LinkedList;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

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
    
    private boolean canNotify(Player player)
    {
	return !(PreciousStones.Permissions.has(player, "preciousstones.override.notify") && !PreciousStones.Permissions.has(player, "preciousstones.admin.isadmin"));
    }
    
    private boolean canWarn(Player player)
    {
	return !(PreciousStones.Permissions.has(player, "preciousstones.override.warn") && !PreciousStones.Permissions.has(player, "preciousstones.admin.isadmin"));
    }
    
    private boolean canAlert(Player player)
    {
	return !(plugin.settings.disableAlertsForAdmins && PreciousStones.Permissions.has(player, "preciousstones.admin.isadmin"));
    }
    
    private boolean canBypassAlert(Player player)
    {
	return !(plugin.settings.disableBypassAlertsForAdmins && PreciousStones.Permissions.has(player, "preciousstones.admin.isadmin"));
    }
    
    public void notifyPlaceU(Player player, Block unbreakableblock)
    {
	Unbreakable unbreakable = plugin.um.getUnbreakable(unbreakableblock);
	
	if (plugin.settings.notifyPlace && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed an unbreakable block [" + unbreakable.getTypeId() + "|" + unbreakable.getX() + " " + unbreakable.getY() + " " + unbreakable.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed an unbreakable block [" + unbreakable.getTypeId() + "]");
	}
    }
    
    public void notifyPlaceFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyPlace && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + fieldsettings.getTitleCap() + " force-field placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed a " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed a " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyPlaceBreakableFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyPlace && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Breakable " + fieldsettings.getTitle() + " force-field placed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " placed a breakable " + fieldsettings.getTitle() + " force-field [" + fieldblock.getType() + "|" + fieldblock.getX() + " " + fieldblock.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed breakable " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyDestroyU(Player player, Block unbreakableblock)
    {
	if (plugin.settings.notifyDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed his own unbreakable block [" + unbreakableblock.getType() + "|" + unbreakableblock.getX() + " " + unbreakableblock.getY() + " " + unbreakableblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his own unbreakable block [" + unbreakableblock.getType() + "]");
	}
    }
    
    public void notifyDestroyFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + fieldsettings.getTitleCap() + " force-field destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed his " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyDestroyBreakableFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + field.getOwner() + "'s breakable " + fieldsettings.getTitle() + " force-field destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " destroyed " + field.getOwner() + "'s breakable " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destoyed " + field.getOwner() + "'s breakable " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyBypassPlace(Player player, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyBypassPlace && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-placed inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logBypassPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-placed a block inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed a block inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyBypassPlaceU(Player player, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyBypassPlace && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block bypass-placed inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logBypassPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-placed an unbreakable block inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unbreakable block inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyBypassDestroy(Player player, Block block, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyBypassDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-destroyed in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void notifyBypassDestroyU(Player player, Block fieldblock)
    {
	Unbreakable unbreakable = plugin.um.getUnbreakable(fieldblock);
	
	if (plugin.settings.notifyBypassDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + unbreakable.getOwner() + "'s unbreakable block bypass-destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "|" + unbreakable.getX() + " " + unbreakable.getY() + " " + unbreakable.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "]");
	}
    }
    
    public void notifyBypassDestroyFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyBypassDestroy && canNotify(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field bypass-destroyed");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log-destroy"))
	    return;
	
	if (plugin.settings.logBypassDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-destroyed " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.notify.bypass") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnEntry(Player player, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnEntry && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot enter protected area");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logEntry)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted entry into " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.entry") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted entry into " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnFire(Player player, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnFire && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place fires here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logFire)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to light fire in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.fire") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to light a fire in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnPlace(Player player, Block block, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnPlace && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted place a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.place") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnDestroyU(Player player, Block unbreakableblock)
    {
	Unbreakable unbreakable = plugin.um.getUnbreakable(unbreakableblock);
	
	if (plugin.settings.warnDestroy && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "|" + unbreakable.getX() + " " + unbreakable.getY() + " " + unbreakable.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "]");
	}
    }
    
    public void warnDestroyFF(Player player, Block fieldblock)
    {
	Field field = plugin.ffm.getField(fieldblock);
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnDestroy && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroy)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnDestroyArea(Player player, Block damagedblock, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnDestroyArea && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot destroy here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logDestroyArea)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to destroy a block " + (new Vec(damagedblock)).toString() + " inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy a block " + (new Vec(damagedblock)).toString() + " inside " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnConflictU(Player player, Block block, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logConflictPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to place an unbreakable block " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable block " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnConflictFF(Player player, Block block, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place force-field here");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logConflictPlace)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to place a force-field " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a force-field " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnPvP(Player attacker, Player victim, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnPvp && canWarn(attacker))
	    attacker.sendMessage(ChatColor.AQUA + "PvP disabled in this area");
	
	if (PreciousStones.Permissions.has(attacker, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logPvp)
	    PreciousStones.log.info("[ps] " + attacker.getName() + " tried to attack " + victim.getName() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(attacker))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " tried to attack " + victim.getName() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnBypassPvP(Player attacker, Player victim, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.notifyBypassPvp && canNotify(attacker))
	    attacker.sendMessage(ChatColor.AQUA + "PvP bypass");
	
	if (PreciousStones.Permissions.has(attacker, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logBypassPvp)
	    PreciousStones.log.info("[ps] " + attacker.getName() + " bypass-attack " + victim.getName() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(attacker))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " bypass-attack " + victim.getName() + " in " + field.getOwner() + "'s " + fieldsettings.getTitle() + " force-field");
	}
    }
    
    public void warnPlaceUnprotectableTouching(Player player, Block placedblock)
    {
	Block touchingblock = plugin.upm.getTouchingUnprotectableBlock(placedblock);
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(touchingblock.getType().toString()));
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to protect an unprotectable block [" + touchingblock.getType() + "|" + touchingblock.getX() + " " + touchingblock.getY() + " " + touchingblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + touchingblock.getType() + "]");
	}
    }
    
    public void warnPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " inside this " + fieldsettings.getTitle() + " force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to protect an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] inside a force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + unprotectableblock.getType() + "] inside a force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	}
    }
    
    public void warnPlaceFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldtypeblock.getTypeId());
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place " + fieldsettings.getTitle() + " force-field. A " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " found in the area");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + " attempted to place a force-field [" + fieldtypeblock.getType() + "] but an unprotectable was found in the area [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a force-field [" + fieldtypeblock.getType() + "] but an unprotectable was found in the area [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
	}
    }
    
    public void notifyBypassUnprotectableTouching(Player player, Block placedblock)
    {
	Block unprotectableblock = plugin.upm.getTouchingUnprotectableBlock(placedblock);
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-protected");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-protected an unprotectable block [" + placedblock.getType() + "|" + placedblock.getX() + " " + placedblock.getY() + " " + placedblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-protected an unprotectable block [" + placedblock.getType() + "|" + placedblock.getX() + " " + placedblock.getY() + " " + placedblock.getZ() + "]");
	}
    }
    
    public void notifyBypassPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed in " + fieldsettings.getTitle() + " force-field");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] inside a force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] inside a force-field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	}
    }
    
    public void notifyBypassFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldtypeblock.getTypeId());
	
	if (plugin.settings.warnUnprotectable && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + fieldsettings.getTitle() + " force-field bypass-placed in an area with an " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " unprotectable block");
	
	if (PreciousStones.Permissions.has(player, "preciousstones.admin.bypass.log"))
	    return;
	
	if (plugin.settings.logUnprotectable)
	    PreciousStones.log.info("[ps] " + player.getName() + "bypass-placed a force-field [" + fieldtypeblock.getType() + "] in an area with an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
	
	for (Player pl : plugin.getServer().getOnlinePlayers())
	{
	    if (pl.equals(player))
		continue;
	    
	    if (PreciousStones.Permissions.has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
		ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + "bypass-placed a force-field [" + fieldtypeblock.getType() + "] in an area with an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
	}
    }
    
    public void notifyGuardDog(Player player, Field field, String property)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	if (player != null && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Owners have been notified of your " + property);
	
	if (plugin.settings.notifyGuardDog && canNotify(player))
	{
	    for (String allowed : field.getAllAllowed())
	    {
		Player allowedPlayer = Helper.matchExactPlayer(plugin, allowed);
		
		if (allowedPlayer != null && canNotify(allowedPlayer))
		{
		    ChatBlock.sendMessage(allowedPlayer, ChatColor.YELLOW + Helper.capitalize(property) + " detected at " + fieldsettings.getTitle() + " " + field);
		}
	    }
	}
    }
    
    public void showWelcomeMessage(Player player, String name)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Entering " + name);
    }
    
    public void showFarewellMessage(Player player, String name)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Leaving " + name);
    }
    
    public void showNotFound(Player player)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "No force-fields found");
    }
    
    public void showSlowDamage(Player player)
    {
	if (plugin.settings.warnSlowDamage && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
    }
    
    public void showFastDamage(Player player)
    {
	if (plugin.settings.warnFastDamage && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
    }
    
    public void showInstantHeal(Player player)
    {
	if (plugin.settings.warnInstantHeal && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.WHITE + "*healed*");
    }
    
    public void showSlowHeal(Player player)
    {
	if (plugin.settings.warnSlowHeal && canWarn(player))
	    ChatBlock.sendMessage(player, ChatColor.WHITE + "*healing*");
    }
    
    public void showUnbreakableOwner(Player player, Block block)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + plugin.um.getOwner(block));
    }
    
    public void showFieldOwner(Player player, Block block)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + plugin.ffm.getOwner(block));
    }
    
    public void showProtected(Player player)
    {
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Protected");
    }
    
    public void showProtectedLocation(LinkedList<Field> fields, Player player)
    {
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Protected");
	for (Field field : fields)
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Source: [" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	}
    }
    
    public void showUnbreakableDetails(Unbreakable unbreakable, Player player)
    {
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + unbreakable.getOwner());
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Location: [" + unbreakable.getX() + " " + unbreakable.getY() + " " + unbreakable.getZ() + "]");
    }
    
    public void showFieldDetails(Field field, Player player)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	
	ChatBlock.sendBlank(player);
	
	if (fieldsettings.nameable && field.getName().length() > 0)
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Name: " + field.getName());
	}
	
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner: " + field.getOwner());
	
	if (field.getAllowedList() != null)
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Allowed: " + field.getAllowedList());
	}
	
	if (fieldsettings.radius > 0)
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Dimensions: " + ((field.getRadius() * 2) + 1) + "x" + field.getHeight() + "x" + ((field.getRadius() * 2) + 1));
	}
	
	ChatBlock.sendMessage(player, ChatColor.AQUA + "Location: [" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
	
	String properties = "";
	
	if (fieldsettings.preventFire)
	    properties += ", no-fire";
	
	if (fieldsettings.preventEntry)
	    properties += ", no-entry";
	
	if (fieldsettings.preventPlace)
	    properties += ", no-place";
	
	if (fieldsettings.preventDestroy)
	    properties += ", no-destroy";
	
	if (fieldsettings.preventExplosions)
	    properties += ", no-explosions";
	
	if (fieldsettings.preventPvP)
	    properties += ", no-pvp";
	
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
	
	if (fieldsettings.welcomeMessage)
	    properties += ", welcome";
	
	if (fieldsettings.farewellMessage)
	    properties += ", farewell";
	
	if (properties.length() > 0)
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "Properties: " + properties.substring(2));
    }
    
    public void debug(String msg)
    {
	PreciousStones.log.info("***** " + msg);
    }
}
