package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import me.taylorkelly.help.Help;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.AllowedEntry;

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
 * @author phaed
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
        cacheBlock = new ChatBlock();
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
            helpPlugin.registerCommand("ps setradius [radius]", "Sets the field's radius", plugin, true, "preciousstones.benefit.setradius");
            helpPlugin.registerCommand("ps setheight [height]", "Sets the field's height", plugin, true, "preciousstones.benefit.setheight");
            helpPlugin.registerCommand("ps setvelocity [.1-5] ", "Sets velocity of launchers/cannons", plugin, true, "preciousstones.benefit.setvelocity");
            helpPlugin.registerCommand("ps setowner [player] ", "Of the block you're pointing at", plugin, true, "preciousstones.admin.setowner");
            helpPlugin.registerCommand("ps snitch <clear> ", "View/clear snitch you're pointing at", plugin, true, "preciousstones.benefit.snitch");
            helpPlugin.registerCommand("ps cloak <radius>", "Cloaks the block you are looking at", plugin, true, "preciousstones.special.cloak");
            helpPlugin.registerCommand("ps decloak ", "Decloaks the block you are looking at", plugin, true, "preciousstones.special.cloak");
            helpPlugin.registerCommand("ps delete ", "Delete the field(s) you're standing on", plugin, true, "preciousstones.admin.delete");
            helpPlugin.registerCommand("ps delete [player] ", "Delete all pstones of the player", plugin, true, "preciousstones.admin.delete");
            helpPlugin.registerCommand("ps info ", "Get info for the field youre standing on", plugin, true, "preciousstones.admin.info");
            helpPlugin.registerCommand("ps list [chunks-in-radius]", "Lists all pstones in area", plugin, true, "preciousstones.admin.list");
            helpPlugin.registerCommand("ps reload ", "Reload configuraton file", plugin, true, "preciousstones.admin.reload");
            helpPlugin.registerCommand("ps save ", "Save pstones to database", plugin, true, "preciousstones.admin.save");
            helpPlugin.registerCommand("ps fields ", "List the configured field types", plugin, true, "preciousstones.admin.fields");
            helpPlugin.registerCommand("ps clean ", "Cleans up all orphan fields in the world", plugin, true, "preciousstones.admin.clean");

            PreciousStones.log(Level.INFO, "Help plugin support enabled");
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
            if (sender instanceof Player)
            {
                String[] split = args;

                if (command.getName().equals("ps"))
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
                            if (split.length >= 2)
                            {
                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    String playerName = split[1];
                                    String perm = split.length == 3 ? split[2] : "all";

                                    int count = plugin.ffm.addAllowed(player, field, playerName, perm);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " force-field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on the list");
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
                                HashSet<AllowedEntry> allowed = plugin.ffm.getAllowed(player, field);

                                if (allowed.size() > 0)
                                {
                                    String out = "";

                                    for (AllowedEntry ae : allowed)
                                    {
                                        out += ", " + ae;
                                    }

                                    ChatBlock.sendMessage(player, ChatColor.YELLOW + "Allowed: " + ChatColor.AQUA + out.substring(2));
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
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed from " + count + Helper.plural(count, " force-field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + Helper.capitalize(playerName) + " not found or is the last player on the list");
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
                            if (split.length >= 2)
                            {
                                String playerName = split[1];
                                String perm = split.length == 3 ? split[2] : "all";

                                int count = plugin.ffm.allowAll(player, playerName, perm);

                                if (count > 0)
                                {
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " force-field", "s"));
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on all your lists");
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
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed " + count + Helper.plural(count, " force-field", "s"));
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is not in any of your lists");
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

                                    ChatBlock.sendMessage(player, ChatColor.YELLOW + "Inhabitants: " + ChatColor.AQUA + out.substring(2));
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
                        else if (split[0].equals("setradius") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setradius"))
                        {
                            if (split.length == 2 && Helper.isInteger(split[1]))
                            {
                                int radius = Integer.parseInt(split[1]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                                    if (radius >= 0 && radius <= fieldsettings.radius)
                                    {
                                        field.setRadius(radius);
                                        field.setDirty(true);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Radius set to " + radius);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + "Radius must be less than or equal to " + fieldsettings.radius);
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
                        else if (split[0].equals("setheight") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setheight"))
                        {
                            if (split.length == 2 && Helper.isInteger(split[1]))
                            {
                                int height = Integer.parseInt(split[1]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                                    int maxHeight = (((fieldsettings.radius * 2) + 1) + fieldsettings.height);

                                    if (height >= 0 && height <= maxHeight)
                                    {
                                        field.setHeight(height);
                                        field.setDirty(true);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Height set to " + height);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + "Height must be less than or equal to " + maxHeight);
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
                        else if (split[0].equals("setvelocity") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setvelocity"))
                        {
                            if (split.length == 2 && Helper.isFloat(split[1]))
                            {
                                float velocity = Float.parseFloat(split[1]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                                    if (fieldsettings.cannon || fieldsettings.launch)
                                    {
                                        if (velocity < 0 || velocity > 5)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "Velocity must be from 0 to 5");
                                            return true;
                                        }

                                        field.setVelocity(velocity);
                                        field.setDirty(true);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Velocity set to " + velocity);
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
                        else if (split[0].equals("snitch") && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                        {
                            if (split.length == 1)
                            {
                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    plugin.cm.showIntruderList(player, field);
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
                                        boolean cleaned = plugin.ffm.cleanSnitchList(player, field);

                                        if (cleaned)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleared the snitch list");
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "Snitch list is empty");
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

                                    int radius = plugin.settings.cloakDefaultRadius;

                                    if (split.length > 1 && Helper.isInteger(split[1]))
                                    {
                                        radius = Integer.parseInt(split[1]);

                                        if (radius >= plugin.settings.cloakMinRadius && radius <= plugin.settings.cloakMaxRadius && radius != 0)
                                        {
                                            newfield.setRadius(radius);
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

                                    if (tb.getDistanceToBlock() > radius)
                                    {
                                        plugin.clm.cloak(newfield);
                                    }

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
                                        if (!plugin.settings.isCloakableType(block.getTypeId()))
                                        {
                                            plugin.clm.decloak(field);
                                        }

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
                            List<Field> fields = plugin.ffm.getSourceFields(block);

                            if (pointing != null && !fields.contains(pointing))
                            {
                                fields.add(pointing);
                            }

                            plugin.cm.showFieldDetails(player, fields);

                            if (fields.isEmpty())
                            {
                                plugin.cm.showNotFound(player);
                            }
                            return true;
                        }
                        else if (split[0].equals("delete") && plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
                        {
                            if (split.length == 1)
                            {
                                List<Field> sourcefields = plugin.ffm.getSourceFields(block);

                                if (sourcefields.size() > 0)
                                {
                                    int count = plugin.ffm.deleteFields(null, sourcefields.get(0));

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from " + count + Helper.plural(count, " force-field", "s"));

                                        if (plugin.settings.logBypassDelete)
                                        {
                                            PreciousStones.log(Level.INFO, "Protective field removed from {0}{1} by {2} near {3}", count, Helper.plural(count, " force-field", "s"), player.getName(), sourcefields.get(0).toString());
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
                                Player badplayer = Helper.matchExactPlayer(plugin, split[1]);

                                if (badplayer != null)
                                {
                                    int fields = plugin.ffm.deleteBelonging(badplayer.getName());
                                    int ubs = plugin.ffm.deleteBelonging(badplayer.getName());

                                    if (fields > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Deleted " + badplayer.getName() + "'s " + fields + " fields");
                                    }

                                    if (ubs > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Deleted " + badplayer.getName() + "'s " + fields + " unbreakables");
                                    }

                                    if (ubs == 0 && fields == 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "The player had no pstones");
                                    }
                                }
                                else
                                {
                                    plugin.cm.showNotFound(player);
                                }
                                return true;
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
                                    List<Unbreakable> unbreakables = plugin.um.getUnbreakablesInArea(player, Integer.parseInt(split[1]));
                                    List<Field> fields = plugin.ffm.getFieldsInArea(player, Integer.parseInt(split[1]));

                                    for (Unbreakable u : unbreakables)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + u.toString());
                                    }

                                    for (Field f : fields)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + f.toString());
                                    }

                                    if (unbreakables.isEmpty() && fields.isEmpty())
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
                            plugin.um.saveAll();
                            plugin.ffm.saveAll();

                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Data saved.");
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
                            int cleandFF = plugin.ffm.cleanOrphans(player.getWorld().getName());

                            if (cleandFF > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleaned " + cleandFF + " orphaned force-fields");
                            }

                            int cleandU = plugin.um.cleanOrphans(player.getWorld().getName());

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

                        ChatBlock.sendMessage(player, ChatColor.RED + "Not a valid command or syntax error.  Try agian.");

                        return true;
                    }

                    ChatColor color = plugin.plm.isDisabled(player) ? ChatColor.DARK_GRAY : ChatColor.YELLOW;
                    String status = plugin.plm.isDisabled(player) ? ChatColor.GRAY + " - disabled" : "";

                    cacheBlock = new ChatBlock();

                    ChatBlock.sendBlank(player);
                    ChatBlock.saySingle(player, ChatColor.WHITE + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + status + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                    ChatBlock.sendBlank(player);

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
                    {
                        cacheBlock.addRow(ChatColor.YELLOW + "/ps [on|off] " + ChatColor.AQUA + "- Disable/Enable the placing of pstones");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
                    {
                        cacheBlock.addRow(color + "/ps allow [player|*] " + ChatColor.AQUA + "- Add player to overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
                    {
                        cacheBlock.addRow(color + "/ps allowall [player|*] " + ChatColor.AQUA + "- Add player to all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
                    {
                        cacheBlock.addRow(color + "/ps allowed " + ChatColor.AQUA + "- List all allowed players in overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
                    {
                        cacheBlock.addRow(color + "/ps remove [player|*] " + ChatColor.AQUA + "- Remove player from overlapping fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
                    {
                        cacheBlock.addRow(color + "/ps removeall [player|*] " + ChatColor.AQUA + "- Remove player from all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
                    {
                        cacheBlock.addRow(color + "/ps who " + ChatColor.AQUA + "- List all inhabitants inside the overlapping fields");
                    }

                    if (plugin.settings.haveNameable() && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
                    {
                        cacheBlock.addRow(color + "/ps setname [name] " + ChatColor.AQUA + "- Set the name of force-fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.setradius"))
                    {
                        cacheBlock.addRow(color + "/ps setradius [radius] " + ChatColor.AQUA + "- Sets the field's radius");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.setheight"))
                    {
                        cacheBlock.addRow(color + "/ps setheight [height] " + ChatColor.AQUA + "- Sets the field's height");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.setvelocity"))
                    {
                        cacheBlock.addRow(color + "/ps setvelocity [.1-5] " + ChatColor.AQUA + "- For launchers and cannons (0=auto)");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                    {
                        cacheBlock.addRow(color + "/ps snitch <clear> " + ChatColor.AQUA + "- View/clear snitch you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                    {
                        cacheBlock.addRow(color + "/ps cloak <radius>" + ChatColor.AQUA + "- Cloaks the block you are looking at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.special.cloak"))
                    {
                        cacheBlock.addRow(color + "/ps decloak " + ChatColor.AQUA + "- Decloaks the block you are looking at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps delete " + ChatColor.AQUA + "- Delete the field(s) you're standing on");
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps delete [player] " + ChatColor.AQUA + "- Delete all pstones of the player");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.info"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps info " + ChatColor.AQUA + "- Get info for the field youre standing on");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.list"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps list [chunks-in-radius]" + ChatColor.AQUA + "- Lists all pstones in area");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps setowner [player] " + ChatColor.AQUA + "- Of the block you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps reload " + ChatColor.AQUA + "- Reloads configuraton file");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.save"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps save " + ChatColor.AQUA + "- Saves pstones to database");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps fields " + ChatColor.AQUA + "- List the configured field types");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "/ps clean " + ChatColor.AQUA + "- Cleans up all orphan fields in the world");
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
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Command entry error: {0}", ex.getMessage());
        }

        return false;
    }
}
