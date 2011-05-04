package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import me.taylorkelly.help.Help;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

/**
 *
 * @author cc_madelg
 */
public final class CommandManager implements CommandExecutor
{
    private PreciousStones plugin;
    /**
     *
     */
    public Help helpPlugin;
    private ChatBlock cacheBlock = new ChatBlock();

    /**
     *
     * @param plugin
     */
    public CommandManager(PreciousStones plugin)
    {
	this.plugin = plugin;
        registerHelpCommands();
    }

    /**
     *
     * @return
     */
    public ChatBlock getCacheBlock()
    {
	return cacheBlock;
    }

    /**
     *
     */
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
	    helpPlugin.registerCommand("ps cloak <radius>", "Cloaks the block you are looking at", plugin, true, "preciousstones.special.cloak");
	    helpPlugin.registerCommand("ps decloak ", "Decloaks the block you are looking at", plugin, true, "preciousstones.special.cloak");
	    helpPlugin.registerCommand("ps delete ", "Delete the field(s) you're standing on", plugin, true, "preciousstones.admin.delete");
	    helpPlugin.registerCommand("ps delete [blockid] ", "Delete the field(s) from this type", plugin, true, "preciousstones.admin.delete");
	    helpPlugin.registerCommand("ps info ", "Get info for the field youre standing on", plugin, true, "preciousstones.admin.info");
	    helpPlugin.registerCommand("ps list [chunks-in-radius]", "Lists all pstones in area", plugin, true, "preciousstones.admin.list");
	    helpPlugin.registerCommand("ps setowner [player] ", "Of the block you're pointing at", plugin, true, "preciousstones.admin.setowner");
	    helpPlugin.registerCommand("ps reload ", "Reload configuraton file", plugin, true, "preciousstones.admin.reload");
	    helpPlugin.registerCommand("ps save ", "Save force field files", plugin, true, "preciousstones.admin.save");
	    helpPlugin.registerCommand("ps fields ", "List the configured field types", plugin, true, "preciousstones.admin.fields");
	    helpPlugin.registerCommand("ps clean ", "Clean up all orphaned fields/unbreakables", plugin, true, "preciousstones.admin.clean");

	    PreciousStones.log.log(Level.INFO, "[{0}] ''Help'' support enabled", plugin.getDescription().getName());
	}
    }

    /**
     *
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
	try
	{
	    String[] split = args;
	    String commandName = command.getName().toLowerCase();
	    if (sender instanceof Player)
	    {
		if (commandName.equals("ps"))
		{
                    Player player = (Player) sender;

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
                        else if (split[0].equals("cloak") && plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                        {
                            Field field = plugin.ffm.getPointedField(block, player);

                            if (field != null)
                            {
                                if (plugin.settings.isCloakableType(field.getTypeId()))
                                {
                                    ChatBlock.sendMessage(player, ChatColor.RED + "This " + Helper.friendlyBlockType(field.getType()) + " is already cloaked");
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.RED + "You cannot cloak a force-field");
                                }
                                return true;
                            }

                            TargetBlock tb = new TargetBlock(player, 100, 0.2, plugin.settings.throughFields);

                            if (tb != null)
                            {
                                Block targetblock = tb.getTargetBlock();

                                if (targetblock != null && plugin.settings.isCloakableType(targetblock))
                                {
                                    plugin.ffm.add(targetblock, player);
                                    Field newfield = plugin.ffm.getField(targetblock);

                                    if (split.length > 1 && Helper.isInteger(split[1]))
                                    {
                                        int radius = Integer.parseInt(split[1]);

                                        if (radius >= plugin.settings.cloakMinRadius && radius <= plugin.settings.cloakMaxRadius && radius != 0)
                                        {
                                            newfield.setRadius(Integer.parseInt(split[1]));
                                        }
                                        else
                                        {
                                            if (radius > plugin.settings.cloakMaxRadius)
                                            {
                                                ChatBlock.sendMessage(player, ChatColor.RED + "Maximum cloak field size is " + plugin.settings.cloakMaxRadius + " blocks");
                                            }

                                            if (radius < plugin.settings.cloakMinRadius)
                                            {
                                                ChatBlock.sendMessage(player, ChatColor.RED + "Minimum cloak field size is " + plugin.settings.cloakMinRadius + " blocks");
                                            }

                                            return true;
                                        }
                                    }

                                    plugin.clm.initiate(newfield);

                                    ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.friendlyBlockType(targetblock.getType().toString()) + " has been cloaked");
                                    return true;
                                }
                            }

                            ChatBlock.sendMessage(player, ChatColor.RED + "You are not pointing at a cloakable block");
                            return true;
                        }
                        else if (split[0].equals("decloak") && plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                        {
                            if (split.length == 1)
                            {
                                Field field = plugin.ffm.getPointedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                                    if (fieldsettings.cloak)
                                    {
                                        plugin.ffm.silentRelease(field);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "The block has been decloaked");
                                        return true;
                                    }
                                }

                                ChatBlock.sendMessage(player, ChatColor.RED + "You are not pointing at a cloaked block");
                                return true;
                            }
                        }
                        else if (split[0].equals("more") && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                        {
                            if (cacheBlock.size() > 0)
                            {
                                ChatBlock.saySingle(player, ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------");
                                ChatBlock.sendBlank(player);

                                cacheBlock.sendBlock(player, plugin.settings.linesPerPage);

                                if (cacheBlock.size() > 0)
                                {
                                    ChatBlock.sendBlank(player);
                                    ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
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

                            if (pointing != null && !fields.contains(pointing))
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
                                            PreciousStones.log.log(Level.INFO, "[ps] Protective field removed from {0}{1} by {2} near {3}", new Object[]{count, Helper.plural(count, " force-field", "s"), player.getName(), sourcefields.get(0).toString()});
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
                                            PreciousStones.log.log(Level.INFO, "[ps] Protective field removed from {0}{1} of type {2} by {3} near {4}", new Object[]{fields.size(), Helper.plural(fields.size(), " force-field", "s"), split[1], player.getName(), fields.get(0).toString()});
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
                        else if (split[0].equals("help"))
                        {
                            // fall through
                        }
                    }

                    ChatColor color = plugin.plm.isDisabled(player) ? ChatColor.DARK_GRAY : ChatColor.YELLOW;
                    String status = plugin.plm.isDisabled(player) ? ChatColor.GRAY + " - disabled" : "";

                    cacheBlock.clear();

                    ChatBlock.sendBlank(player);
                    ChatBlock.saySingle(player, ChatColor.AQUA + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + status + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                    ChatBlock.sendBlank(player);

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
                    {
                        cacheBlock.addSingleRow(ChatColor.YELLOW + "/ps [on|off] " + ChatColor.AQUA + "- Disable/Enable the placing of pstones");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
                    {
                        cacheBlock.addSingleRow(color + "/ps allow [player|*] " + ChatColor.AQUA + "- Add player to overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
                    {
                        cacheBlock.addSingleRow(color + "/ps allowall [player|*] " + ChatColor.AQUA + "- Add player to all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
                    {
                        cacheBlock.addSingleRow(color + "/ps allowed " + ChatColor.AQUA + "- List all allowed players in overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
                    {
                        cacheBlock.addSingleRow(color + "/ps remove [player|*] " + ChatColor.AQUA + "- Remove player from overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
                    {
                        cacheBlock.addSingleRow(color + "/ps removeall [player|*] " + ChatColor.AQUA + "- Remove player from all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
                    {
                        cacheBlock.addSingleRow(color + "/ps who " + ChatColor.AQUA + "- List all inhabitants inside the overlapping fields");
                    }

                    if (plugin.settings.haveNameable() && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
                    {
                        cacheBlock.addSingleRow(color + "/ps setname [name] " + ChatColor.AQUA + "- Set the name of force-fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                    {
                        cacheBlock.addSingleRow(color + "/ps snitch <clear> " + ChatColor.AQUA + "- View/clear snitch you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                    {
                        cacheBlock.addSingleRow(color + "/ps cloak <radius>" + ChatColor.AQUA + "- Cloaks the block you are looking at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                    {
                        cacheBlock.addSingleRow(color + "/ps decloak " + ChatColor.AQUA + "- Decloaks the block you are looking at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps delete " + ChatColor.AQUA + "- Delete the field(s) you're standing on");
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps delete [blockid] " + ChatColor.AQUA + "- Delete the field(s) from this type");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.info"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps info " + ChatColor.AQUA + "- Get info for the field youre standing on");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.list"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps list [chunks-in-radius]" + ChatColor.AQUA + "- Lists all pstones in area");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps setowner [player] " + ChatColor.AQUA + "- Of the block you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps reload " + ChatColor.AQUA + "- Reload configuraton file");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.save"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps save " + ChatColor.AQUA + "- Save force field files");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps fields " + ChatColor.AQUA + "- List the configured field types");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
                    {
                        cacheBlock.addSingleRow(ChatColor.DARK_RED + "/ps clean " + ChatColor.AQUA + "- Clean up all orphaned fields/unbreakables");
                    }

                    boolean more = cacheBlock.sendBlock(player, plugin.settings.linesPerPage);

                    if (more)
                    {
                        ChatBlock.sendBlank(player);
                        ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
                    }

                    ChatBlock.sendBlank(player);
                    return true;
                }
            }
        }
        catch(Exception ex)
        {
            PreciousStones.log.severe(ex.getMessage());
        }

        return false;
    }
}
