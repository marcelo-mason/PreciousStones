package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import me.taylorkelly.help.Help;

import java.util.HashSet;
import java.util.List;
import org.bukkit.World;

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
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;

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
            helpPlugin.registerCommand("ps on/off ", " Disable/Enable the placing of pstones", plugin, true, "preciousstones.benefit.onoff");
            helpPlugin.registerCommand("ps allow [player(s)/*] ", "To overlapped fields", plugin, true, "preciousstones.whitelist.allow");
            helpPlugin.registerCommand("ps allowall [player(s)/*] ", "To all your fields", plugin, true, "preciousstones.whitelist.allowall");
            helpPlugin.registerCommand("ps allowed ", "List allowed players in overlapped fields", plugin, true, "preciousstones.whitelist.allowed");
            helpPlugin.registerCommand("ps remove [player(s)/*] ", "From overlapped fields", plugin, true, "preciousstones.whitelist.remove");
            helpPlugin.registerCommand("ps removeall [player(s)/*] ", "From all your fields", plugin, true, "preciousstones.whitelist.removeall");
            helpPlugin.registerCommand("ps who ", "List all inhabitants inside the overlapping fields", plugin, true, "preciousstones.whitelist.who");
            helpPlugin.registerCommand("ps setname [name] ", "Set the name of fields", plugin, true, "preciousstones.benefit.setname");
            helpPlugin.registerCommand("ps setradius [radius]", "Sets the field's radius", plugin, true, "preciousstones.benefit.setradius");
            helpPlugin.registerCommand("ps setheight [height]", "Sets the field's height", plugin, true, "preciousstones.benefit.setheight");
            helpPlugin.registerCommand("ps setvelocity [.1-5] ", "Sets velocity of launchers/cannons", plugin, true, "preciousstones.benefit.setvelocity");
            helpPlugin.registerCommand("ps setowner [player] ", "Of the block you're pointing at", plugin, true, "preciousstones.admin.setowner");
            helpPlugin.registerCommand("ps visualize ", "Visualizes the perimiter of the field", plugin, true, "preciousstones.benefit.visualize");
            helpPlugin.registerCommand("ps mark ", "Marks the location of all pstones", plugin, true, "preciousstones.admin.mark");
            helpPlugin.registerCommand("ps snitch <clear> ", "View/clear snitch you're pointing at", plugin, true, "preciousstones.benefit.snitch");
            helpPlugin.registerCommand("ps delete ", "Delete the field(s) you're standing on", plugin, true, "preciousstones.admin.delete");
            helpPlugin.registerCommand("ps delete [player] ", "Delete all pstones of the player", plugin, true, "preciousstones.admin.delete");
            helpPlugin.registerCommand("ps info ", "Get info for the field youre standing on", plugin, true, "preciousstones.admin.info");
            helpPlugin.registerCommand("ps list [chunks-in-radius]", "Lists all pstones in area", plugin, true, "preciousstones.admin.list");
            helpPlugin.registerCommand("ps reload ", "Reload configuraton file", plugin, true, "preciousstones.admin.reload");
            helpPlugin.registerCommand("ps fields ", "List the configured field types", plugin, true, "preciousstones.admin.fields");
            helpPlugin.registerCommand("ps clean ", "Cleans up all orphan fields in the world", plugin, true, "preciousstones.admin.clean");
            helpPlugin.registerCommand("ps clean ", "Reverts all orphan fields in the world", plugin, true, "preciousstones.admin.revert");

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
                if (command.getName().equals("ps"))
                {
                    Player player = (Player) sender;

                    if (plugin.settings.isBlacklistedWorld(player.getWorld()))
                    {
                        ChatBlock.sendMessage(player, ChatColor.RED + "PreciousStones disabled in this world");
                        return true;
                    }

                    if (args.length > 0)
                    {
                        String cmd = args[0];
                        args = Helper.removeFirst(args);

                        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());

                        if (cmd.equals("debug") && plugin.pm.hasPermission(player, "preciousstones.admin.debug"))
                        {
                            plugin.settings.debug = !plugin.settings.debug;
                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Debug output " + (plugin.settings.debug ? "enabled" : "disabled"));
                            return true;
                        }
                        if (cmd.equals("debugdb") && plugin.pm.hasPermission(player, "preciousstones.admin.debug"))
                        {
                            plugin.settings.debugdb = !plugin.settings.debugdb;
                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Debug db output " + (plugin.settings.debugdb ? "enabled" : "disabled"));
                            return true;
                        }
                        if (cmd.equals("debugsql") && plugin.pm.hasPermission(player, "preciousstones.admin.debug"))
                        {
                            plugin.settings.debugsql = !plugin.settings.debugsql;
                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Debug sql output " + (plugin.settings.debugsql ? "enabled" : "disabled"));
                            return true;
                        }
                        if (cmd.equals("on") && plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
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
                        else if (cmd.equals("off") && plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
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
                        else if (cmd.equals("allow") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
                        {
                            if (args.length >= 1)
                            {
                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    for (String playerName : args)
                                    {
                                        int count = plugin.ffm.addAllowed(player, field, playerName);

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " field", "s"));
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on the list");
                                        }
                                    }
                                }
                                else
                                {
                                    plugin.cm.showNotFound(player);
                                }

                                return true;
                            }
                        }
                        else if (cmd.equals("remove") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
                        {
                            if (args.length >= 1)
                            {
                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    for (String playerName : args)
                                    {
                                        int count = plugin.ffm.removeAllowed(player, field, playerName);

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed from " + count + Helper.plural(count, " field", "s"));
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + Helper.capitalize(playerName) + " not found or is the last player on the list");
                                        }
                                    }
                                }
                                else
                                {
                                    plugin.cm.showNotFound(player);
                                }

                                return true;
                            }
                        }
                        else if (cmd.equals("allowall") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
                        {
                            if (args.length >= 1)
                            {
                                for (String playerName : args)
                                {
                                    int count = plugin.ffm.allowAll(player, playerName);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on all your lists");
                                    }
                                }

                                return true;
                            }
                        }
                        else if (cmd.equals("removeall") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
                        {
                            if (args.length >= 1)
                            {
                                for (String playerName : args)
                                {
                                    int count = plugin.ffm.removeAll(player, playerName);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed " + count + Helper.plural(count, " field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(playerName) + " is not in any of your lists");
                                    }
                                }

                                return true;
                            }
                        }
                        else if (cmd.equals("allowed") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
                        {
                            Field field = plugin.ffm.getOneAllowedField(block, player);

                            if (field != null)
                            {
                                HashSet<String> allowed = plugin.ffm.getAllowed(player, field);

                                if (allowed.size() > 0)
                                {
                                    String out = "";

                                    for (String ae : allowed)
                                    {
                                        out += ", " + ae;
                                    }

                                    ChatBlock.sendMessage(player, ChatColor.YELLOW + "Allowed: " + ChatColor.AQUA + out.substring(2));
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.RED + "No players allowed in this field");
                                }
                            }
                            else
                            {
                                plugin.cm.showNotFound(player);
                            }

                            return true;
                        }
                        else if (cmd.equals("who") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
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
                                    ChatBlock.sendMessage(player, ChatColor.RED + "No players found in these overlapped fields");
                                }
                            }
                            else
                            {
                                plugin.cm.showNotFound(player);
                            }

                            return true;
                        }
                        else if (cmd.equals("setname") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
                        {
                            if (args.length >= 1)
                            {
                                String playerName = Helper.toMessage(args);

                                if (playerName.length() > 0)
                                {
                                    Field field = plugin.ffm.getOneAllowedField(block, player);

                                    if (field != null)
                                    {
                                        int count = plugin.ffm.setNameFields(player, field, playerName);

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Renamed " + count + Helper.plural(count, " field", "s") + " to " + playerName);
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "No nameable fields found");
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
                        else if (cmd.equals("setradius") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setradius"))
                        {
                            if (args.length == 1 && Helper.isInteger(args[0]))
                            {
                                int radius = Integer.parseInt(args[0]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fs = field.getSettings();

                                    if (radius >= 0 && radius <= fs.getRadius())
                                    {
                                        field.setRadius(radius);
                                        plugin.sm.offerField(field);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Radius set to " + radius);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + "Radius must be less than or equal to " + fs.getRadius());
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
                        else if (cmd.equals("setheight") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setheight"))
                        {
                            if (args.length == 1 && Helper.isInteger(args[0]))
                            {
                                int height = Integer.parseInt(args[0]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fs = field.getSettings();

                                    int maxHeight = (((fs.getRadius() * 2) + 1) + fs.getHeight());

                                    if (height >= 0 && height <= maxHeight)
                                    {
                                        field.setHeight(height);
                                        plugin.sm.offerField(field);
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
                        else if (cmd.equals("setvelocity") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.setvelocity"))
                        {
                            if (args.length == 1 && Helper.isFloat(args[0]))
                            {
                                float velocity = Float.parseFloat(args[0]);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    FieldSettings fs = field.getSettings();

                                    if (fs.isCannon() || fs.isLaunch())
                                    {
                                        if (velocity < 0 || velocity > 5)
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "Velocity must be from 0 to 5");
                                            return true;
                                        }

                                        field.setVelocity(velocity);
                                        plugin.sm.offerField(field);
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
                        else if (cmd.equals("visualize") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.visualize"))
                        {
                            if (plugin.pm.hasPermission(player, "preciousstones.admin.visualize"))
                            {
                                plugin.viz.revertVisualization(player);

                                List<Field> fieldsInArea = plugin.ffm.getFieldsInCustomArea(player.getLocation(), plugin.settings.visualizeAdminChunkRadius);

                                if (fieldsInArea.size() > 0)
                                {
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + "Generating visualization...");

                                    for (Field f : fieldsInArea)
                                    {
                                        plugin.viz.addVisualizationField(player, f);
                                    }

                                    plugin.viz.displayVisualization(player, true);
                                }
                            }
                            else
                            {
                                plugin.viz.revertVisualization(player);

                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    HashSet<Field> fields = plugin.ffm.getOverlappedFields(player, field);

                                    if (fields != null)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Generating visualization...");

                                        for (Field f : fields)
                                        {
                                            plugin.viz.addVisualizationField(player, f);
                                        }

                                        plugin.viz.displayVisualization(player, true);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + "You are not inside of a field");
                                    }
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.RED + "You are not inside of a field");
                                }
                            }
                            return true;
                        }
                        else if (cmd.equals("mark") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.admin.mark"))
                        {
                            List<Field> fieldsInArea = plugin.ffm.getFieldsInCustomArea(player.getLocation(), plugin.settings.visualizeMarkChunkRadius);

                            if (fieldsInArea.size() > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Marking  " + fieldsInArea.size() + " field blocks...");

                                for (Field f : fieldsInArea)
                                {
                                    plugin.viz.addFieldMark(player, f);
                                }

                                plugin.viz.displayVisualization(player, false);
                            }
                            else
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "No fields in the area");
                            }

                            return true;
                        }
                        else if (cmd.equals("snitch") && !plugin.plm.isDisabled(player) && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                        {
                            if (args.length == 0)
                            {
                                Field field = plugin.ffm.getOneAllowedField(block, player);

                                if (field != null)
                                {
                                    plugin.cm.showSnitchList(player, field);
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.RED + "You are not pointing at a snitch block");
                                }

                                return true;
                            }
                            else if (args.length == 1)
                            {
                                if (args[0].equals("clear"))
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
                        else if (cmd.equals("more") && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
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
                        else if (cmd.equals("info") && plugin.pm.hasPermission(player, "preciousstones.admin.info"))
                        {
                            Field pointing = plugin.ffm.getOneAllowedField(block, player);
                            List<Field> fields = plugin.ffm.getSourceFields(block.getLocation());

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
                        else if (cmd.equals("delete") && plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
                        {
                            if (args.length == 0)
                            {
                                List<Field> sourcefields = plugin.ffm.getSourceFields(block.getLocation());

                                if (sourcefields.size() > 0)
                                {
                                    int count = plugin.ffm.deleteFields(player, sourcefields.get(0));

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from " + count + Helper.plural(count, " field", "s"));

                                        if (plugin.settings.logBypassDelete)
                                        {
                                            PreciousStones.log(Level.INFO, "Protective field removed from {0}{1} by {2} near {3}", count, Helper.plural(count, " field", "s"), player.getName(), sourcefields.get(0).toString());
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
                            else if (args.length == 1)
                            {
                                Player badplayer = plugin.helper.matchSinglePlayer(args[1]);

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
                        else if (cmd.equals("setowner") && plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
                        {
                            if (args.length == 1)
                            {
                                String owner = args[0];

                                TargetBlock tb = new TargetBlock(player, 100, 0.2, plugin.settings.throughFields);

                                if (tb != null)
                                {
                                    Block targetblock = tb.getTargetBlock();

                                    if (targetblock != null)
                                    {
                                        Field field = plugin.ffm.getField(targetblock);

                                        if (field != null)
                                        {
                                            field.setOwner(owner);
                                            plugin.sm.offerField(field);
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner set to " + owner);
                                            return true;
                                        }
                                    }
                                }

                                ChatBlock.sendMessage(player, ChatColor.AQUA + "You are not pointing at a field or unbreakable block");
                                return true;
                            }
                        }
                        else if (cmd.equals("list") && plugin.pm.hasPermission(player, "preciousstones.admin.list"))
                        {
                            if (args.length == 1)
                            {
                                if (Helper.isInteger(args[0]))
                                {
                                    int chunk_radius = Integer.parseInt(args[0]);

                                    List<Unbreakable> unbreakables = plugin.um.getUnbreakablesInArea(player, chunk_radius);
                                    List<Field> fields = plugin.ffm.getFieldsInCustomArea(player.getLocation(), chunk_radius);

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
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "No field or unbreakable blocks found");
                                    }
                                    return true;
                                }
                            }
                        }
                        else if (cmd.equals("reload") && plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
                        {
                            plugin.settings.load();
                            ChatBlock.sendMessage(player, ChatColor.AQUA + "Configuration reloaded");
                            return true;
                        }
                        else if (cmd.equals("fields") && plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
                        {
                            plugin.cm.showConfiguredFields(player);
                            return true;
                        }
                        else if (cmd.equals("clean") && plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
                        {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleandFF = 0;
                            int cleandU = 0;

                            for (World world : worlds)
                            {
                                cleandFF += plugin.ffm.cleanOrphans(world);
                                cleandU += plugin.um.cleanOrphans(world);
                            }

                            if (cleandFF > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cleaned " + cleandFF + " orphaned fields");
                            }
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
                        else if (cmd.equals("revert") && plugin.pm.hasPermission(player, "preciousstones.admin.revert"))
                        {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleandFF = 0;
                            int cleandU = 0;

                            for (World world : worlds)
                            {
                                cleandFF =+ plugin.ffm.revertOrphans(world);
                                cleandU =+ plugin.um.revertOrphans(world);
                            }

                            if (cleandFF > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Reverted " + cleandFF + " orphaned fields");
                            }
                            if (cleandU > 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Reverted " + cleandFF + " orphaned unbreakable blocks");
                            }

                            if (cleandFF == 0 && cleandU == 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "No orphan fields/unbreakables found");
                            }
                            return true;
                        }

                        ChatBlock.sendMessage(player, ChatColor.RED + "Not a valid command or insufficient permissions");

                        return true;
                    }

                    ChatColor color = plugin.plm.isDisabled(player) ? ChatColor.DARK_GRAY : ChatColor.YELLOW;
                    String status = plugin.plm.isDisabled(player) ? ChatColor.GRAY + " - disabled" : "";

                    cacheBlock = new ChatBlock();

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.onoff"))
                    {
                        cacheBlock.addRow(ChatColor.YELLOW + "  /ps on/off " + ChatColor.AQUA + "- Disable/Enable the placing of pstones");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allow"))
                    {
                        cacheBlock.addRow(color + "  /ps allow [player(s)/*] " + ChatColor.AQUA + "- To overlapped fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowall"))
                    {
                        cacheBlock.addRow(color + "  /ps allowall [player(s)/*] " + ChatColor.AQUA + "- To all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.allowed"))
                    {
                        cacheBlock.addRow(color + "  /ps allowed " + ChatColor.AQUA + "- List all allowed players in overlapped fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.remove"))
                    {
                        cacheBlock.addRow(color + "  /ps remove [player(s)/*] " + ChatColor.AQUA + "- From overlapped fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.whitelist.removeall"))
                    {
                        cacheBlock.addRow(color + "  /ps removeall [player(s)/*] " + ChatColor.AQUA + "- From all your fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.who"))
                    {
                        cacheBlock.addRow(color + "  /ps who " + ChatColor.AQUA + "- List all inhabitants inside the overlapped fields");
                    }

                    if (plugin.settings.haveNameable() && plugin.pm.hasPermission(player, "preciousstones.benefit.setname"))
                    {
                        cacheBlock.addRow(color + "  /ps setname [name] " + ChatColor.AQUA + "- Set the name of fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.setradius"))
                    {
                        cacheBlock.addRow(color + "  /ps setradius [radius] " + ChatColor.AQUA + "- Sets the field's radius");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.setheight"))
                    {
                        cacheBlock.addRow(color + "  /ps setheight [height] " + ChatColor.AQUA + "- Sets the field's height");
                    }

                    if (plugin.settings.haveVelocity() && plugin.pm.hasPermission(player, "preciousstones.benefit.setvelocity"))
                    {
                        cacheBlock.addRow(color + "  /ps setvelocity [.1-5] " + ChatColor.AQUA + "- For launchers/cannons (0=auto)");
                    }

                    if (plugin.settings.haveSnitch() && plugin.pm.hasPermission(player, "preciousstones.benefit.snitch"))
                    {
                        cacheBlock.addRow(color + "  /ps snitch <clear> " + ChatColor.AQUA + "- View/clear snitch you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.benefit.visualize") || plugin.pm.hasPermission(player, "preciousstones.admin.visualize"))
                    {
                        cacheBlock.addRow(color + "  /ps visualize" + ChatColor.AQUA + "- Visualizes the perimiter of the field");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.delete"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps delete " + ChatColor.AQUA + "- Delete the field(s) you're standing on");
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps delete [player] " + ChatColor.AQUA + "- Delete all pstones of the player");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.info"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps info " + ChatColor.AQUA + "- Get info for the field youre standing on");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.list"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps list [chunks-in-radius]" + ChatColor.AQUA + "- Lists all pstones in area");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.setowner"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps setowner [player] " + ChatColor.AQUA + "- Of the block you're pointing at");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.mark"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps mark" + ChatColor.AQUA + "- Marks the location of all fields");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.reload"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps reload " + ChatColor.AQUA + "- Reloads configuraton file");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.fields"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps fields " + ChatColor.AQUA + "- List the configured field types");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.clean"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps clean " + ChatColor.AQUA + "- Cleans up all orphan fields in the world");
                    }

                    if (plugin.pm.hasPermission(player, "preciousstones.admin.revert"))
                    {
                        cacheBlock.addRow(ChatColor.DARK_RED + "  /ps revert " + ChatColor.AQUA + "- Reverts all orphan fields in the world");
                    }

                    if (cacheBlock.size() > 0)
                    {
                        ChatBlock.sendBlank(player);
                        ChatBlock.saySingle(player, ChatColor.WHITE + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + status + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                        ChatBlock.sendBlank(player);

                        boolean more = cacheBlock.sendBlock(player, plugin.settings.linesPerPage);

                        if (more)
                        {
                            ChatBlock.sendBlank(player);
                            ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
                        }

                        ChatBlock.sendBlank(player);
                    }
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
