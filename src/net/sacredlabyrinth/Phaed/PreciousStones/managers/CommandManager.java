package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import me.taylorkelly.help.Help;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

public class CommandManager
{
    private PreciousStones plugin;
    public Help helpPlugin;
    
    public CommandManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void registerHelpCommands()
    {
	Plugin test = plugin.getServer().getPluginManager().getPlugin("Help");
	
	if (helpPlugin == null)
	{
	    if (test != null)
	    {
		helpPlugin = ((Help) test);
	    }
	}
	
	if (helpPlugin != null)
	{
	    helpPlugin.registerCommand("ps [on|off] ", " Disable/Enable the placing of pstones", plugin, true, "preciousstones.benefit.onoff");
	    helpPlugin.registerCommand("ps allow [player|*] ", "Add player to overlapping fields", plugin, true, "preciousstones.whitelist.allow");
	    helpPlugin.registerCommand("ps allowall [player|*] ", "Add player to all your fields", plugin, true, "preciousstones.whitelist.allowall");
	    helpPlugin.registerCommand("ps allowed ", "List allowed players in overlapping fields", plugin, true, "preciousstones.whitelist.allowed");
	    helpPlugin.registerCommand("ps remove [player|*] ", "Remove player from overlapping fields", plugin, true, "preciousstones.whitelist.remove");
	    helpPlugin.registerCommand("ps removeall [player|*] ", "Remove player from all your fields", plugin, true, "preciousstones.whitelist.removeall");
	    helpPlugin.registerCommand("ps who ", "List all inhabitants inside the overlapping fields", plugin, true, "preciousstones.whitelist.who");
	    helpPlugin.registerCommand("ps setname [name] ", "Set the name of force-fields", plugin, true, "preciousstones.benefit.setname");
	    helpPlugin.registerCommand("ps snitch <clear> ", "View/clear snitch you're pointing at", plugin, true, "preciousstones.benefit.snitch");
	    helpPlugin.registerCommand("ps delete ", "Delete the field(s) you're standing on", plugin, true, "preciousstones.admin.delete");
	    helpPlugin.registerCommand("ps delete [blockid] ", "Delete the field(s) from this type", plugin, true, "preciousstones.admin.delete");
	    helpPlugin.registerCommand("ps info ", "Get info for the field youre standing on", plugin, true, "preciousstones.admin.info");
	    helpPlugin.registerCommand("ps list [chunks-in-radius]", "Lists all pstones in area", plugin, true, "preciousstones.admin.list");
	    helpPlugin.registerCommand("ps setowner [player] ", "Of the block you're pointing at", plugin, true, "preciousstones.admin.setowner");
	    helpPlugin.registerCommand("ps reload ", "Reload configuraton file", plugin, true, "preciousstones.admin.reload");
	    helpPlugin.registerCommand("ps save ", "Save force field files", plugin, true, "preciousstones.admin.save");
	    helpPlugin.registerCommand("ps fields ", "List the configured field types", plugin, true, "preciousstones.admin.fields");
	    helpPlugin.registerCommand("ps clean ", "Clean up all orphaned fields/unbreakables", plugin, true, "preciousstones.admin.clean");
	    
	    PreciousStones.log.info("[" + plugin.getDescription().getName() + "] 'Help' support enabled");
	}
    }
    
    public boolean processCommand(Player player, String[] split)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.benefit.ps"))
	{
	    return false;
	}
	
	if (split.length > 0)
	{
	    Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	    
	    if (split[0].equals("on") && plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
	    {
		if (plugin.plm.isDisabled(player))
		{
		    plugin.plm.setDisabled(player, false);
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "Enabled the placing of pstones");
		}
		else
		{
		    ChatBlock.sendMessage(player, ChatColor.RED + "Pstone placement is already enabled");
		}
		return true;
	    }
	    else if (split[0].equals("off") && plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
	    {
		if (!plugin.plm.isDisabled(player))
		{
		    plugin.plm.setDisabled(player, true);
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "Disabled the placing of pstones");
		}
		else
		{
		    ChatBlock.sendMessage(player, ChatColor.RED + "Pstone placement is already disabled");
		}
		return true;
	    }
	    else if (split[0].equals("allow") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
	    {
		if (split.length == 2)
		{
		    
		    Field field = plugin.ffm.getOneAllowedField(block, player);
		    
		    if (field != null)
		    {
			String playerName = split[1];
			
			int count = plugin.ffm.addAllowed(player, field, playerName);
			
			if (count > 0)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " has been allowed in " + count + Helper.plural(count, " force-field", "s"));
			}
			else
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " is already on the list");
			}
		    }
		    else
		    {
			plugin.cm.showNotFound(player);
		    }
		    
		    return true;
		}
	    }
	    else if (split[0].equals("allowed") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
	    {
		Field field = plugin.ffm.getOneAllowedField(block, player);
		
		if (field != null)
		{
		    HashSet<String> allallowed = plugin.ffm.getAllAllowed(player, field);
		    
		    if (allallowed.size() > 0)
		    {
			String out = "";
			
			for (String i : allallowed)
			{
			    out += ", " + i;
			}
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Allowed: " + out.substring(2));
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.RED + "No players allowed in this force-field");
		    }
		}
		else
		{
		    plugin.cm.showNotFound(player);
		}
		
		return true;
	    }
	    else if (split[0].equals("remove") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
	    {
		if (split.length == 2)
		{
		    Field field = plugin.ffm.getOneAllowedField(block, player);
		    
		    if (field != null)
		    {
			String playerName = split[1];
			
			int count = plugin.ffm.removeAllowed(player, field, playerName);
			
			if (count > 0)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " was removed from " + count + Helper.plural(count, " force-field", "s"));
			}
			else
			{
			    ChatBlock.sendMessage(player, ChatColor.RED + playerName + " not found or is the last player on the list");
			}
		    }
		    else
		    {
			plugin.cm.showNotFound(player);
		    }
		    
		    return true;
		}
	    }
	    else if (split[0].equals("allowall") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
	    {
		if (split.length == 2)
		{
		    String playerName = split[1];
		    
		    int count = plugin.ffm.allowAll(player, playerName);
		    
		    if (count > 0)
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " has been allowed in " + count + Helper.plural(count, " force-field", "s"));
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " is already on all your lists");
		    }
		    
		    return true;
		}
	    }
	    else if (split[0].equals("removeall") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
	    {
		if (split.length == 2)
		{
		    String playerName = split[1];
		    
		    int count = plugin.ffm.removeAll(player, playerName);
		    
		    if (count > 0)
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " was removed " + count + Helper.plural(count, " force-field", "s"));
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " is not in any of your lists");
		    }
		    
		    return true;
		}
	    }
	    else if (split[0].equals("who") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
	    {
		Field field = plugin.ffm.getOneAllowedField(block, player);
		
		if (field != null)
		{
		    HashSet<String> inhabitants = plugin.ffm.getWho(player, field);
		    
		    if (inhabitants.size() > 0)
		    {
			String out = "";
			
			for (String i : inhabitants)
			{
			    out += ", " + i;
			}
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Inhabitants: " + out.substring(2));
		    }
		    else
		    {
			ChatBlock.sendMessage(player, ChatColor.RED + "No players found in these overlapped force-fields");
		    }
		}
		else
		{
		    plugin.cm.showNotFound(player);
		}
		
		return true;
	    }
	    else if (split[0].equals("setname") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
	    {
		if (split.length >= 2)
		{
		    String playerName = "";
		    
		    for (int i = 1; i < split.length; i++)
		    {
			playerName += split[i] + " ";
		    }
		    playerName = playerName.trim();
		    
		    if (playerName.length() > 0)
		    {
			Field field = plugin.ffm.getOneAllowedField(block, player);
			
			if (field != null)
			{
			    int count = plugin.ffm.setNameFields(player, field, playerName);
			    
			    if (count > 0)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + "Renamed " + count + Helper.plural(count, " force-field", "s") + " to " + playerName);
			    }
			    else
			    {
				plugin.cm.showNotFound(player);
			    }
			    return true;
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
			return true;
		    }
		}
	    }
	    else if (split[0].equals("snitch") && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
	    {
		if (split.length == 1)
		{
		    Field field = plugin.ffm.getOneAllowedField(block, player);
		    
		    if (field != null)
		    {
			plugin.snm.showIntruderList(player, field);
		    }
		    
		    ChatBlock.sendMessage(player, ChatColor.RED + "You are not pointing at a snitch block");
		    
		    return true;
		}
		else if (split.length == 2)
		{
		    if (split[1].equals("clear"))
		    {
			Field field = plugin.ffm.getOneAllowedField(block, player);
			
			if (field != null)
			{
			    int cleaned = plugin.ffm.cleanSnitchLists(player, field);
			    
			    if (cleaned > 0)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleaned the snitch list of " + cleaned + " fields");
			    }
			    else
			    {
				ChatBlock.sendMessage(player, ChatColor.RED + "No intruders found in these overlapped force-field's");
			    }
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
			return true;
		    }
		}
	    }
	    else if (split[0].equals("more") && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
	    {
		ChatBlock cacheblock = plugin.snm.getCacheBlock();
		
		if (cacheblock.size() > 0)
		{
		    ChatBlock.saySingle(player, ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------");
		    ChatBlock.sendBlank(player);
		    
		    cacheblock.addRow(new String[] { "    " + ChatColor.GRAY + "Name", "Last Seen" });
		    
		    cacheblock.sendBlock(player, 12);
		    
		    if (cacheblock.size() > 0)
		    {
			ChatBlock.sendBlank(player);
			ChatBlock.sendMessage(player, ChatColor.GOLD + "Type " + ChatColor.WHITE + "/ps more " + ChatColor.GOLD + "to view next page.");
		    }
		    ChatBlock.sendBlank(player);
		    
		    return true;
		}
		
		ChatBlock.sendMessage(player, ChatColor.GOLD + "Nothing more to see.");
		return true;
	    }
	    else if (split[0].equals("info") && plugin.pm.hasPermission(player, "preciousstones.admin.info"))
	    {
		Field pointing = plugin.ffm.getOneAllowedField(block, player);
		LinkedList<Field> fields = plugin.ffm.getSourceFields(block);
		
		if(pointing != null && !fields.contains(pointing))
		{
		    fields.addLast(pointing);
		}
		
		for (Field field : fields)
		{
		    plugin.cm.showFieldDetails(field, player);
		}
		
		if (fields.size() == 0)
		{
		    plugin.cm.showNotFound(player);
		}
		return true;
	    }
	    else if (split[0].equals("delete") && plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
	    {
		if (split.length == 1)
		{
		    LinkedList<Field> sourcefields = plugin.ffm.getSourceFields(block);
		    
		    if (sourcefields.size() > 0)
		    {
			int count = plugin.ffm.deleteFields(null, sourcefields.get(0));
			
			if (count > 0)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from " + count + Helper.plural(count, " force-field", "s"));
			    
			    if (plugin.settings.logBypassDelete)
			    {
				PreciousStones.log.info("[ps] Protective field removed from " + count + Helper.plural(count, " force-field", "s") + " by " + player.getName() + " near " + sourcefields.get(0).toString());
			    }
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
		    }
		    else
		    {
			plugin.cm.showNotFound(player);
		    }
		    
		    return true;
		}
		else if (split.length == 2)
		{
		    if (Helper.isInteger(split[1]))
		    {
			LinkedList<Field> fields = plugin.ffm.getFieldsOfType(Integer.parseInt(split[1]), player.getWorld());
			
			for (Field field : fields)
			{
			    plugin.ffm.release(field);
			}
			
			if (fields.size() > 0)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from " + fields.size() + Helper.plural(fields.size(), " force-field", "s") + " of type " + split[1]);
			    
			    if (plugin.settings.logBypassDelete)
			    {
				PreciousStones.log.info("[ps] Protective field removed from " + fields.size() + Helper.plural(fields.size(), " force-field", "s") + " of type " + split[1] + " by " + player.getName() + " near " + fields.get(0).toString());
			    }
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
			return true;
		    }
		}
	    }
	    else if (split[0].equals("setowner") && plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
	    {
		if (split.length == 2)
		{
		    String owner = split[1];
		    
		    TargetBlock tb = new TargetBlock(player, 100, 0.2, plugin.settings.throughFields);
		    
		    if (tb != null)
		    {
			Block targetblock = tb.getTargetBlock();
			
			if (targetblock != null)
			{
			    if (plugin.settings.isUnbreakableType(targetblock))
			    {
				Unbreakable unbreakable = plugin.um.getUnbreakable(targetblock);
				
				if (unbreakable != null)
				{
				    unbreakable.setOwner(owner);
				    ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner set to " + owner);
				    return true;
				}
			    }
			    
			    if (plugin.settings.isFieldType(targetblock))
			    {
				Field field = plugin.ffm.getField(targetblock);
				
				if (field != null)
				{
				    field.setOwner(owner);
				    ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner set to " + owner);
				    return true;
				}
			    }
			}
		    }
		    
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "You are not pointing at a force-field or unbreakable block");
		    return true;
		}
	    }
	    else if (split[0].equals("list") && plugin.pm.hasPermission(player, "preciousstones.admin.list"))
	    {
		if (split.length == 2)
		{
		    if (Helper.isInteger(split[1]))
		    {
			LinkedList<Unbreakable> unbreakables = plugin.um.getUnbreakablesInArea(player, Integer.parseInt(split[1]));
			LinkedList<Field> fields = plugin.ffm.getFieldsInArea(player, Integer.parseInt(split[1]));
			
			for (Unbreakable u : unbreakables)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + u.toString());
			}
			
			for (Field f : fields)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + f.toString());
			}
			
			if (unbreakables.size() == 0 && fields.size() == 0)
			{
			    ChatBlock.sendMessage(player, ChatColor.AQUA + "No force-field or unbreakable blocks found");
			}
			return true;
		    }
		}
	    }
	    else if (split[0].equals("reload") && plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
	    {
		plugin.settings.loadConfiguration();
		
		ChatBlock.sendMessage(player, ChatColor.AQUA + "Configuration reloaded");
		return true;
	    }
	    else if (split[0].equals("save") && plugin.pm.hasPermission(player, "preciousstones.admin.save"))
	    {
		plugin.sm.save();
		
		ChatBlock.sendMessage(player, ChatColor.AQUA + "PStones saved to files");
		return true;
	    }
	    else if (split[0].equals("fields") && plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
	    {
		HashMap<Integer, FieldSettings> fieldsettings = plugin.settings.getFieldSettings();
		
		for (FieldSettings fs : fieldsettings.values())
		{
		    ChatBlock.sendBlank(player);
		    ChatBlock.sendMessage(player, ChatColor.AQUA + fs.toString());
		}
		
		return true;
	    }
	    else if (split[0].equals("clean") && plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
	    {
		int cleandFF = plugin.ffm.cleanOrphans();
		
		if (cleandFF > 0)
		{
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleaned " + cleandFF + " orphaned force-fields");
		}
		
		int cleandU = plugin.um.cleanOrphans();
		
		if (cleandU > 0)
		{
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleaned " + cleandFF + " orphaned unbreakable blocks");
		}
		
		if (cleandFF == 0 && cleandU == 0)
		{
		    ChatBlock.sendMessage(player, ChatColor.AQUA + "No orphan fields/unbreakables found");
		}
		return true;
	    }
	}
	
	ChatColor color = plugin.plm.isDisabled(player) ? ChatColor.GRAY : ChatColor.AQUA;
	String status = plugin.plm.isDisabled(player) ? ChatColor.GRAY + " - disabled" : "";
	
	ChatBlock.sendBlank(player);
	ChatBlock.sendMessage(player, ChatColor.YELLOW + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + status);
	
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
	{
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps [on|off] " + ChatColor.GRAY + "- Disable/Enable the placing of pstones");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
	{
	    ChatBlock.sendMessage(player, color + "/ps allow [player|*] " + ChatColor.GRAY + "- Add player to overlapping fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
	{
	    ChatBlock.sendMessage(player, color + "/ps allowall [player|*] " + ChatColor.GRAY + "- Add player to all your fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
	{
	    ChatBlock.sendMessage(player, color + "/ps allowed " + ChatColor.GRAY + "- List all allowed players in overlapping fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
	{
	    ChatBlock.sendMessage(player, color + "/ps remove [player|*] " + ChatColor.GRAY + "- Remove player from overlapping fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
	{
	    ChatBlock.sendMessage(player, color + "/ps removeall [player|*] " + ChatColor.GRAY + "- Remove player from all your fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
	{
	    ChatBlock.sendMessage(player, color + "/ps who " + ChatColor.GRAY + "- List all inhabitants inside the overlapping fields");
	}
	
	if (plugin.settings.haveNameable() && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
	{
	    ChatBlock.sendMessage(player, color + "/ps setname [name] " + ChatColor.GRAY + "- Set the name of force-fields");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
	{
	    ChatBlock.sendMessage(player, color + "/ps snitch <clear> " + ChatColor.GRAY + "- View/clear snitch you're pointing at");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps delete " + ChatColor.GRAY + "- Delete the field(s) you're standing on");
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps delete [blockid] " + ChatColor.GRAY + "- Delete the field(s) from this type");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.info"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps info " + ChatColor.GRAY + "- Get info for the field youre standing on");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.list"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps list [chunks-in-radius]" + ChatColor.GRAY + "- Lists all pstones in area");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps setowner [player] " + ChatColor.GRAY + "- Of the block you're pointing at");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps reload " + ChatColor.GRAY + "- Reload configuraton file");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.save"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps save " + ChatColor.GRAY + "- Save force field files");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps fields " + ChatColor.GRAY + "- List the configured field types");
	}
	
	if (plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
	{
	    ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps clean " + ChatColor.GRAY + "- Clean up all orphaned fields/unbreakables");
	}
	
	return true;
    }
}
