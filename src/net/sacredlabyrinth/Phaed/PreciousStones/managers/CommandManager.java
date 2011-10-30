package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author phaed
 */
public final class CommandManager implements CommandExecutor
{
    private PreciousStones plugin;

    /**
     *
     */
    public CommandManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        try
        {
            if (command.getName().equals("ps"))
            {
                Player player = null;

                if (sender instanceof Player)
                {
                    player = (Player) sender;
                }

                boolean hasplayer = player != null;
                boolean isDisabled = hasplayer ? plugin.getPlayerManager().getPlayerData(player.getName()).isDisabled() : false;

                if (hasplayer)
                {
                    if (plugin.getSettingsManager().isBlacklistedWorld(player.getWorld()))
                    {
                        ChatBlock.sendMessage(player, ChatColor.RED + "PreciousStones disabled in this world");
                        return true;
                    }
                }

                if (args.length > 0)
                {
                    String cmd = args[0];
                    args = Helper.removeFirst(args);

                    Block block = hasplayer ? player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()) : null;

                    if (cmd.equals("draw"))
                    {
                        plugin.getForceFieldManager().drawSourceFields();
                        return true;
                    }
                    else if (cmd.equals("debug") && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebug(!plugin.getSettingsManager().isDebug());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug output " + (plugin.getSettingsManager().isDebug() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if (cmd.equals("debugdb") && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugdb(!plugin.getSettingsManager().isDebugdb());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug db output " + (plugin.getSettingsManager().isDebugdb() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if (cmd.equals("debugsql") && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugsql(!plugin.getSettingsManager().isDebugsql());
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Debug sql output " + (plugin.getSettingsManager().isDebugsql() ? "enabled" : "disabled"));
                        return true;
                    }
                    else if (cmd.equals("on") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        if (isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerData(player.getName()).setDisabled(false);
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Enabled the placing of pstones");
                        }
                        else
                        {
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Pstone placement is already enabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("off") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        if (!isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerData(player.getName()).setDisabled(true);
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Disabled the placing of pstones");
                        }
                        else
                        {
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Pstone placement is already disabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("allow") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                boolean overlapped = false;

                                for (String playerName : args)
                                {
                                    if (playerName.equalsIgnoreCase("-o"))
                                    {
                                        overlapped = true;
                                        continue;
                                    }

                                    int count = plugin.getForceFieldManager().addAllowed(player, field, playerName, overlapped);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on the list");
                                    }
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("allowall") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            for (String playerName : args)
                            {
                                int count = plugin.getForceFieldManager().allowAll(player, playerName);

                                if (count > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in " + count + Helper.plural(count, " field", "s"));
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " is already on all your lists");
                                }
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("remove") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                boolean overlapped = false;

                                for (String playerName : args)
                                {
                                    if (playerName.equalsIgnoreCase("-o"))
                                    {
                                        overlapped = true;
                                        continue;
                                    }

                                    int count = plugin.getForceFieldManager().removeAllowed(player, field, playerName, overlapped);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed from " + count + Helper.plural(count, " field", "s"));
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + Helper.capitalize(playerName) + " not found or is the last player on the list");
                                    }
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("removeall") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            for (String playerName : args)
                            {
                                int count = plugin.getForceFieldManager().removeAll(player, playerName);

                                if (count > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed " + count + Helper.plural(count, " field", "s"));
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " is not in any of your lists");
                                }
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("allowed") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            boolean overlapped = false;

                            if (args.length >= 1)
                            {
                                overlapped = args[0].equalsIgnoreCase("-o");
                            }

                            List<String> allowed = plugin.getForceFieldManager().getAllowed(player, field, overlapped);

                            if (allowed.size() > 0)
                            {
                                String out = "";

                                for (String ae : allowed)
                                {
                                    out += ", " + ae;
                                }

                                ChatBlock.sendMessage(sender, ChatColor.YELLOW + "Allowed: " + ChatColor.AQUA + out.substring(2));
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "No players allowed in this field");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals("who") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            HashSet<String> inhabitants = plugin.getForceFieldManager().getWho(player, field);

                            if (inhabitants.size() > 0)
                            {
                                String out = "";

                                for (String i : inhabitants)
                                {
                                    out += ", " + i;
                                }

                                ChatBlock.sendMessage(sender, ChatColor.YELLOW + "Inhabitants: " + ChatColor.AQUA + out.substring(2));
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "No players found in these overlapped fields");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals("setname") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            String playerName = Helper.toMessage(args);

                            if (playerName.length() > 0)
                            {
                                Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                                if (field != null)
                                {
                                    int count = plugin.getForceFieldManager().setNameFields(player, field, playerName);

                                    if (count > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Renamed " + count + Helper.plural(count, " field", "s") + " to " + playerName);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "No nameable fields found");
                                    }
                                    return true;
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                                return true;
                            }
                        }
                    }
                    else if (cmd.equals("setradius") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int radius = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                FieldSettings fs = field.getSettings();

                                if (!fs.hasFlag(FieldFlag.CUBOID))
                                {
                                    if (radius >= 0 && radius <= fs.getRadius())
                                    {
                                        field.setRadius(radius);

                                        plugin.getStorageManager().offerField(field);
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Radius set to " + radius);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "Radius must be less than or equal to " + fs.getRadius());
                                    }
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Cannot change radius of a cuboid");
                                }
                                return true;
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals("setvelocity") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isFloat(args[0]))
                        {
                            float velocity = Float.parseFloat(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                FieldSettings fs = field.getSettings();

                                if (fs.hasVeocityFlag())
                                {
                                    if (velocity < 0 || velocity > 5)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "Velocity must be from 0 to 5");
                                        return true;
                                    }

                                    field.setVelocity(velocity);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Velocity set to " + velocity);
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                                return true;
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals("density") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int density = Integer.parseInt(args[0]);

                            if (density == 0)
                            {
                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Density must be larger than zero");
                                return true;
                            }

                            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getName());
                            data.setDensity(density);
                            plugin.getStorageManager().offerPlayer(player.getName(), true);

                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Visualization density changed to " + density);
                            return true;
                        }
                        else if (args.length == 0)
                        {
                            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getName());
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Your visualization density is set to " + data.getDensity());
                        }
                    }
                    else if (cmd.equals("toggle") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(flagStr) || field.hasDisabledFlag(flagStr))
                                {
                                    if (flagStr.equals("All"))
                                    {
                                        return true;
                                    }

                                    boolean enabled = field.toggleFieldFlag(flagStr);

                                    if (enabled)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "The " + flagStr + " flag has been enabled.");
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "The " + flagStr + " flag has been disabled.");
                                    }

                                    plugin.getStorageManager().offerField(field);
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "The field does not contain this flag");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if ((cmd.equals("visualize") || cmd.equals("visualise")) && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasplayer)
                    {
                        if (!plugin.getCuboidManager().hasOpenCuboid(player))
                        {
                            if (!plugin.getVisualizationManager().pendingVisualization(player))
                            {
                                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize"))
                                {
                                    if (args.length == 1 && Helper.isInteger(args[0]))
                                    {
                                        int radius = Math.min(Integer.parseInt(args[0]), plugin.getServer().getViewDistance());

                                        Set<Field> fieldsInArea = null;

                                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize"))
                                        {
                                            fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), radius / 16, FieldFlag.ALL);
                                        }
                                        else
                                        {
                                            fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), radius / 16, FieldFlag.ALL, player);
                                        }

                                        if (fieldsInArea != null && fieldsInArea.size() > 0)
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Visualizing...");

                                            for (Field f : fieldsInArea)
                                            {
                                                plugin.getVisualizationManager().addVisualizationField(player, f);
                                            }

                                            plugin.getVisualizationManager().displayVisualization(player, true);
                                            return true;
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + "No fields in area");
                                        }
                                    }
                                    else
                                    {
                                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                                        if (field != null)
                                        {
                                            HashSet<Field> fields = plugin.getForceFieldManager().getOverlappedFields(player, field);

                                            if (fields != null)
                                            {
                                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Visualizing...");

                                                for (Field f : fields)
                                                {
                                                    plugin.getVisualizationManager().addVisualizationField(player, f);
                                                }

                                                plugin.getVisualizationManager().displayVisualization(player, true);
                                            }
                                            else
                                            {
                                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not inside of a field");
                                            }
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + "You are not inside of a field");
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "A visualization is already taking place");
                            }
                        }
                        else
                        {
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Cannot visualize while defining a cuboid");
                        }
                        return true;
                    }
                    else if (cmd.equals("mark") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && hasplayer)
                    {
                        if (!plugin.getCuboidManager().hasOpenCuboid(player))
                        {
                            if (!plugin.getVisualizationManager().pendingVisualization(player))
                            {
                                if (plugin.getPermissionsManager().has(player, "preciousstones.admin.mark"))
                                {
                                    Set<Field> fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), plugin.getServer().getViewDistance(), FieldFlag.ALL);

                                    if (fieldsInArea.size() > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Marking " + fieldsInArea.size() + " field blocks...");

                                        for (Field f : fieldsInArea)
                                        {
                                            plugin.getVisualizationManager().addFieldMark(player, f);
                                        }

                                        plugin.getVisualizationManager().displayVisualization(player, false);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields in the area");
                                    }
                                }
                                else
                                {
                                    Set<Field> fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), plugin.getServer().getViewDistance(), FieldFlag.ALL);

                                    if (fieldsInArea.size() > 0)
                                    {
                                        int count = 0;
                                        for (Field f : fieldsInArea)
                                        {
                                            if (plugin.getForceFieldManager().isAllowed(f, player.getName()))
                                            {
                                                count++;
                                                plugin.getVisualizationManager().addFieldMark(player, f);
                                            }
                                        }

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Marking " + count + " field blocks...");
                                            plugin.getVisualizationManager().displayVisualization(player, false);
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields in the area");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields in the area");
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "A visualization is already taking place");
                            }
                        }
                        else
                        {
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Cannot mark fields while defining a cuboid");
                        }
                        return true;
                    }
                    else if (cmd.equals("insert") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (!field.hasFlag(flagStr) && !field.hasDisabledFlag(flagStr))
                                {
                                    if (field.insertFieldFlag(flagStr))
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "The field flag inserted");
                                        plugin.getStorageManager().offerField(field);
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "The field flag entered doesn not exist");
                                    }
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "The field already contains this flag");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals("setinterval") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int interval = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.GRIEF_REVERT);

                            if (field != null)
                            {
                                if (interval >= plugin.getSettingsManager().getGriefRevertMinInterval() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.interval"))
                                {
                                    field.setRevertSecs(interval);
                                    plugin.getGriefUndoManager().register(field);
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "The grief-revert interval has been set to " + interval + " seconds");
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "The minimum interval is " + plugin.getSettingsManager().getGriefRevertMinInterval() + " seconds");
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a grief-revert block");
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals("snitch") && !isDisabled && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasplayer)
                    {
                        if (args.length == 0)
                        {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.SNITCH);

                            if (field != null)
                            {
                                plugin.getCommunicationManager().showSnitchList(player, field);
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a snitch block");
                            }

                            return true;
                        }
                        else if (args.length == 1)
                        {
                            if (args[0].equals("clear"))
                            {
                                Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.SNITCH);

                                if (field != null)
                                {
                                    boolean cleaned = plugin.getForceFieldManager().cleanSnitchList(field);

                                    if (cleaned)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Cleared the snitch list");
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "Snitch list is empty");
                                    }
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                                return true;
                            }
                        }
                    }
                    else if (cmd.equals("more") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasplayer)
                    {
                        ChatBlock cb = plugin.getCommunicationManager().getChatBlock(player);

                        if (cb.size() > 0)
                        {
                            ChatBlock.sendBlank(player);

                            cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

                            if (cb.size() > 0)
                            {
                                ChatBlock.sendBlank(player);
                                ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
                            }
                            ChatBlock.sendBlank(player);

                            return true;
                        }

                        ChatBlock.sendMessage(sender, ChatColor.GOLD + "Nothing more to see.");
                        return true;
                    }
                    else if (cmd.equals("counts"))
                    {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasplayer)
                        {
                            if (!plugin.getCommunicationManager().showFieldCounts(player, player.getName()))
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "Player does not have any fields");
                            }
                            return true;
                        }

                        if (args.length == 1 && plugin.getPermissionsManager().has(player, "preciousstones.admin.counts"))
                        {
                            if (Helper.isInteger(args[0]))
                            {
                                int type = Integer.parseInt(args[0]);

                                if (!plugin.getCommunicationManager().showCounts(sender, type))
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Not a valid field type");
                                }
                            }
                            else if (Helper.isString(args[0]) && hasplayer)
                            {
                                String target = args[0].toString();

                                if (!plugin.getCommunicationManager().showFieldCounts(player, target))
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Player does not have any fields");
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                    else if (cmd.equals("locations"))
                    {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.locations") && hasplayer)
                        {
                            plugin.getCommunicationManager().showFieldLocations(sender, -1, sender.getName());
                            return true;
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.locations"))
                        {
                            if (args.length == 1 && Helper.isString(args[0]))
                            {
                                String targetName = args[0].toString();
                                plugin.getCommunicationManager().showFieldLocations(sender, -1, targetName);
                            }

                            if (args.length == 2 && Helper.isString(args[0]) && Helper.isInteger(args[1]))
                            {
                                String targetName = args[0].toString();
                                int type = Integer.parseInt(args[1]);
                                plugin.getCommunicationManager().showFieldLocations(sender, type, targetName);
                            }
                            return true;
                        }
                        return false;
                    }
                    else if (cmd.equals("info") && plugin.getPermissionsManager().has(player, "preciousstones.admin.info") && hasplayer)
                    {
                        Field pointing = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);
                        List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                        if (pointing != null && !fields.contains(pointing))
                        {
                            fields.add(pointing);
                        }

                        if (fields.size() == 1)
                        {
                            plugin.getCommunicationManager().showFieldDetails(player, fields.get(0));
                        }
                        else
                        {
                            plugin.getCommunicationManager().showFieldDetails(player, fields);
                        }

                        if (fields.isEmpty())
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("delete") && plugin.getPermissionsManager().has(player, "preciousstones.admin.delete"))
                    {
                        if (args.length == 0 && hasplayer)
                        {
                            List<Field> sourceFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                            if (sourceFields.size() > 0)
                            {
                                boolean overlapped = false;

                                if (args.length >= 1)
                                {
                                    overlapped = args[0].equalsIgnoreCase("-o");
                                }

                                int count = plugin.getForceFieldManager().deleteFields(player, sourceFields.get(0), overlapped);

                                if (count > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Protective field removed from " + count + Helper.plural(count, " field", "s"));

                                    if (plugin.getSettingsManager().isLogBypassDelete())
                                    {
                                        PreciousStones.log("Protective field removed from {0}{1} by {2} near {3}", count, Helper.plural(count, " field", "s"), player.getName(), sourceFields.get(0).toString());
                                    }
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                        else if (args.length == 1)
                        {
                            if (Helper.isInteger(args[0]))
                            {
                                int typeId = Integer.parseInt(args[0]);

                                if (typeId != 0)
                                {
                                    int fields = plugin.getForceFieldManager().deleteFieldsOfType(typeId);
                                    int ubs = plugin.getUnbreakableManager().deleteUnbreakablesOfType(typeId);

                                    if (fields > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + fields + " " + Material.getMaterial(typeId) + " fields");
                                    }

                                    if (ubs > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + fields + " " + Material.getMaterial(typeId) + " unbreakables");
                                    }

                                    if (ubs == 0 && fields == 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "No pstones of the type found");
                                    }
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(sender);
                                }
                            }
                            else
                            {
                                int fields = plugin.getForceFieldManager().deleteBelonging(args[0]);
                                int ubs = plugin.getUnbreakableManager().deleteBelonging(args[0]);

                                if (fields > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + args[0] + "'s " + fields + " fields");
                                }

                                if (ubs > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + args[0] + "'s " + fields + " unbreakables");
                                }

                                if (ubs == 0 && fields == 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "The player had no pstones");
                                }
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals("setowner") && plugin.getPermissionsManager().has(player, "preciousstones.admin.setowner") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String owner = args[0];

                            if (owner.contains(":"))
                            {
                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Cannot assign groups or clans as owners");
                                return true;
                            }

                            if (player.getLocation().getY() < 127)
                            {
                                Block targetBlock = player.getTargetBlock(plugin.getSettingsManager().getThroughFieldsSet(), 128);

                                if (targetBlock != null)
                                {
                                    Field field = plugin.getForceFieldManager().getField(targetBlock);

                                    if (field != null)
                                    {
                                        // transfer the count over to the new owner

                                        PlayerData oldData = plugin.getPlayerManager().getPlayerData(field.getOwner());
                                        oldData.decrementFieldCount(field.getTypeId());

                                        PlayerData newData = plugin.getPlayerManager().getPlayerData(owner);
                                        newData.incrementFieldCount(field.getTypeId());

                                        // change the owner

                                        field.setOwner(owner);
                                        plugin.getStorageManager().offerField(field);
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Owner set to " + owner);
                                        return true;
                                    }
                                }
                            }

                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "You are not pointing at a field or unbreakable block");
                            return true;
                        }
                    }
                    else if (cmd.equals("list") && plugin.getPermissionsManager().has(player, "preciousstones.admin.list") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            if (Helper.isInteger(args[0]))
                            {
                                int chunk_radius = Integer.parseInt(args[0]);

                                List<Unbreakable> unbreakables = plugin.getUnbreakableManager().getUnbreakablesInArea(player, chunk_radius);
                                Set<Field> fields = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), chunk_radius, FieldFlag.ALL);

                                for (Unbreakable u : unbreakables)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + u.toString());
                                }

                                for (Field f : fields)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + f.toString());
                                }

                                if (unbreakables.isEmpty() && fields.isEmpty())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "No field or unbreakable blocks found");
                                }
                                return true;
                            }
                        }
                    }
                    else if (cmd.equals("reload") && plugin.getPermissionsManager().has(player, "preciousstones.admin.reload"))
                    {
                        plugin.getSettingsManager().load();
                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Configuration reloaded");
                        return true;
                    }
                    else if (cmd.equals("fields") && plugin.getPermissionsManager().has(player, "preciousstones.admin.fields"))
                    {
                        plugin.getCommunicationManager().showConfiguredFields(sender);
                        return true;
                    }
                    else if (cmd.equals("clean") && plugin.getPermissionsManager().has(player, "preciousstones.admin.clean"))
                    {
                        List<World> worlds = plugin.getServer().getWorlds();

                        int cleandFF = 0;
                        int cleandU = 0;

                        for (World world : worlds)
                        {
                            cleandFF += plugin.getForceFieldManager().cleanOrphans(world);
                            cleandU += plugin.getUnbreakableManager().cleanOrphans(world);
                        }
                        if (cleandFF > 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Cleaned " + cleandFF + " orphaned fields");
                        }
                        if (cleandU > 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Cleaned " + cleandU + " orphaned unbreakable blocks");
                        }
                        if (cleandFF == 0 && cleandU == 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No orphans found");
                        }
                        return true;
                    }
                    else if (cmd.equals("revert") && plugin.getPermissionsManager().has(player, "preciousstones.admin.revert"))
                    {
                        List<World> worlds = plugin.getServer().getWorlds();

                        int cleandFF = 0;
                        int cleandU = 0;

                        for (World world : worlds)
                        {
                            cleandFF += plugin.getForceFieldManager().revertOrphans(world);
                            cleandU += plugin.getUnbreakableManager().revertOrphans(world);
                        }

                        if (cleandFF > 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Reverted " + cleandFF + " orphaned fields");
                        }
                        if (cleandU > 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Reverted " + cleandU + " orphaned unbreakable blocks");
                        }

                        if (cleandFF == 0 && cleandU == 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No orphan fields/unbreakables found");
                        }
                        return true;
                    }

                    ChatBlock.sendMessage(sender, ChatColor.RED + "Not a valid command or insufficient permissions");

                    return true;
                }

                // show the player menu

                plugin.getCommunicationManager().showMenu(player);
                return true;
            }
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Command failure: {0}", ex.getMessage());
        }

        return false;
    }
}
