package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
                        boolean isDisabled = hasplayer ? plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled() : false;
                        if (isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(false);
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
                        boolean isDisabled = hasplayer ? plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled() : false;
                        if (!isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(true);
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Disabled the placing of pstones");
                        }
                        else
                        {
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Pstone placement is already disabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("allow") && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args)
                                {
                                    Player allowed = Helper.matchSinglePlayer(playerName);

                                    // only those with permission can be allowed

                                    if (field.getSettings().getRequiredPermissionAllow() != null)
                                    {
                                        if (!plugin.getPermissionsManager().has(allowed, field.getSettings().getRequiredPermissionAllow()))
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + Helper.capitalize(playerName) + " does not have permissions to be allowed");
                                            continue;
                                        }
                                    }

                                    boolean done = plugin.getForceFieldManager().addAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " has been allowed in the field");
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
                    else if (cmd.equals("allowall") && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasplayer)
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
                    else if (cmd.equals("remove") && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args)
                                {
                                    if (field.containsPlayer(playerName))
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "Cannot remove a player thats currently in your field");
                                        return true;
                                    }

                                    if (plugin.getForceFieldManager().conflictOfInterestExists(field, playerName))
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "You cannot disallow " + playerName + ", one of his fields is overlapping yours");
                                        return true;
                                    }

                                    boolean done = plugin.getForceFieldManager().removeAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(playerName) + " was removed from the field");
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
                    else if (cmd.equals("removeall") && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasplayer)
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
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Nothing to be done");
                                }
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("allowed") && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            List<String> allowed = plugin.getForceFieldManager().getAllowed(player, field);

                            if (allowed.size() > 0)
                            {
                                ChatBlock.sendMessage(sender, ChatColor.YELLOW + "Allowed: " + ChatColor.AQUA + Helper.toMessage(new LinkedList<String>(allowed), ", "));
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
                    else if (cmd.equals("cuboid") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.create.forcefield") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            if ((args[0]).equals("open"))
                            {
                                Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                                if (field != null)
                                {
                                    plugin.getCuboidManager().openCuboid(player, field);
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                            }
                            else if ((args[0]).equals("close"))
                            {
                                plugin.getCuboidManager().closeCuboid(player);
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals("who") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            HashSet<String> inhabitants = plugin.getForceFieldManager().getWho(field);

                            if (inhabitants.size() > 0)
                            {
                                ChatBlock.sendMessage(sender, ChatColor.YELLOW + "Inhabitants: " + ChatColor.AQUA + Helper.toMessage(new LinkedList<String>(inhabitants), ", "));
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "No players found in the field");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals("setname") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasplayer)
                    {
                        String fieldName = null;

                        if (args.length >= 1)
                        {
                            fieldName = Helper.toMessage(args);
                        }

                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            if (field.hasFlag(FieldFlag.TRANSLOCATION))
                            {
                                // if switching from an existing translocation
                                // end the previous one correctly by making sure
                                // to wipe out all applied blocks from the database

                                if (field.isNamed())
                                {
                                    int count = plugin.getStorageManager().appliedTranslocationCount(field);

                                    if (count > 0)
                                    {
                                        plugin.getStorageManager().deleteAppliedTranslocation(field);

                                        if (!plugin.getStorageManager().existsTranslocationDataWithName(field.getName(), field.getOwner()))
                                        {
                                            plugin.getStorageManager().deleteTranslocationHead(field.getName(), field.getOwner());
                                        }

                                        ChatBlock.sendMessage(player, ChatColor.GRAY + " * " + ChatColor.YELLOW + "Translocation " + field.getName() + " unlinked from " + count + " blocks");
                                    }
                                }

                                // check if one exists with that name already

                                if (plugin.getStorageManager().existsFieldWithName(fieldName, player.getName()))
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "A translocation block already exists with that name");
                                    return true;
                                }

                                // if this is a new translocation name, create its head record
                                // this will cement the size of the cuboid

                                if (!plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner()))
                                {
                                    plugin.getStorageManager().insertTranslocationHead(field, fieldName);
                                }

                                // updates the size of the field

                                plugin.getStorageManager().changeSizeTranslocatiorField(field, fieldName);

                                // always start off in applied (recording) mode

                                if (plugin.getStorageManager().existsTranslocationDataWithName(fieldName, field.getOwner()))
                                {
                                    field.setDisabled(true);
                                }
                                else
                                {
                                    field.setDisabled(false);
                                }
                            }

                            if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                            {
                                if (!field.isDisabled())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "This field can only be modified while disabled");
                                    return true;
                                }
                            }

                            if (fieldName == null)
                            {
                                boolean done = plugin.getForceFieldManager().setNameField(field, "");

                                if (done)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Field's name has been cleared");
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "No nameable fields found");
                                }
                            }
                            else
                            {
                                boolean done = plugin.getForceFieldManager().setNameField(field, fieldName);

                                if (done)
                                {
                                    if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                    {
                                        int count = plugin.getStorageManager().unappliedTranslocationCount(field);

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Translocation " + fieldName + " has " + count + " stored blocks");
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Translocation " + fieldName + " created.  Recoding changes...");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Renamed field to " + fieldName);
                                    }
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "No nameable fields found");
                                }
                            }
                            return true;
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("setradius") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int radius = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                FieldSettings fs = field.getSettings();

                                if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                {
                                    if (field.isNamed())
                                    {
                                        if (plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner()))
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "Cannot reshape a translocation cuboid once its in use");
                                            return true;
                                        }
                                    }
                                }

                                if (!field.hasFlag(FieldFlag.CUBOID))
                                {
                                    if (radius >= 0 && (radius <= fs.getRadius() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.setradius")))
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
                    else if (cmd.equals("setvelocity") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isFloat(args[0]))
                        {
                            float velocity = Float.parseFloat(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "This field can only be modified while disabled");
                                        return true;
                                    }
                                }

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
                    else if (cmd.equals("disable") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.disable") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            if (!field.isDisabled())
                            {
                                if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                {
                                    if (field.isNamed())
                                    {
                                        plugin.getTranslocationManager().clearTranslocation(field);
                                    }
                                }

                                field.setDisabled(true);
                                plugin.getStorageManager().offerField(field);
                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Field has been disabled");
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "Field is already disabled");
                            }
                            return true;
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("enable") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.enable") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            if (field.isDisabled())
                            {
                                // update translocation

                                if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                {
                                    if (field.isNamed())
                                    {
                                        plugin.getTranslocationManager().applyTranslocation(field);
                                    }
                                }

                                field.setDisabled(false);
                                plugin.getStorageManager().offerField(field);
                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Field has been enabled");
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "Field is already enabled");
                            }
                            return true;
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("density") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int density = Integer.parseInt(args[0]);

                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
                            data.setDensity(density);
                            plugin.getStorageManager().offerPlayer(player.getName());

                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Visualization density changed to " + density);
                            return true;
                        }
                        else if (args.length == 0)
                        {
                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Your visualization density is set to " + data.getDensity());
                        }
                    }
                    else if (cmd.equals("toggle") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
                                {
                                    if (field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED))
                                    {
                                        if (!field.isDisabled())
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + "This field's flags can only be toggled while disabled");
                                            return true;
                                        }
                                    }
                                }

                                if (field.hasFlag(flagStr) || field.hasDisabledFlag(flagStr))
                                {
                                    boolean unToggable = false;

                                    if (field.hasFlag(FieldFlag.DYNMAP_NO_TOGGLE))
                                    {
                                        if (flagStr.equalsIgnoreCase("dynmap-area"))
                                        {
                                            unToggable = true;
                                        }

                                        if (flagStr.equalsIgnoreCase("dynmap-marker"))
                                        {
                                            unToggable = true;
                                        }
                                    }

                                    if (flagStr.equalsIgnoreCase("dynmap-no-toggle"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("tekkit-block"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("dynmap-disabled-by-default"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("all"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("cuboid"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("apply-to-reverse"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("apply-to-all"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("no-player-place"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("no-conflict"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("toggle-on-disabled"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("prevent-unprotectable"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("redefine-on-disabled"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("modify-on-disabled"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("breakable-on-disabled"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("sneaking-bypass"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("place-disabled"))
                                    {
                                        unToggable = true;
                                    }

                                    if (flagStr.equalsIgnoreCase("worldguard-repellent"))
                                    {
                                        unToggable = true;
                                    }

                                    if (unToggable)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "This flag cannot be toggled");
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

                                    field.dirtyFlags();
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
                    else if ((cmd.equals("visualize") || cmd.equals("visualise")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasplayer)
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

                                            int count = 0;
                                            for (Field f : fieldsInArea)
                                            {
                                                if (count++ >= plugin.getSettingsManager().getVisualizeMaxFields())
                                                {
                                                    continue;
                                                }

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
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Visualizing...");

                                            plugin.getVisualizationManager().addVisualizationField(player, field);
                                            plugin.getVisualizationManager().displayVisualization(player, true);
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
                    else if (cmd.equals("mark") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && hasplayer)
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
                    else if (cmd.equals("insert") && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer)
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
                                        plugin.getForceFieldManager().addSourceField(field);
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
                    else if (cmd.equals("reset") && plugin.getPermissionsManager().has(player, "preciousstones.admin.reset") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            field.RevertFlags();
                            plugin.getStorageManager().offerField(field);
                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "The field flags have been reverted to default.");
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals("setinterval") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int interval = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.GRIEF_REVERT);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                if (interval >= plugin.getSettingsManager().getGriefRevertMinInterval() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.interval"))
                                {
                                    field.setRevertSecs(interval);
                                    plugin.getGriefUndoManager().register(field);
                                    plugin.getStorageManager().offerField(field);
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
                    else if (cmd.equals("snitch") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasplayer)
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
                    else if (cmd.equals("translocation") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.use") && hasplayer)
                    {
                        if (args.length == 0)
                        {
                            ChatBlock.sendMessage(sender, ChatColor.GRAY + "* All commands (except for list) require you to be pointing at a field block or standing in the field");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation list");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation import " + ChatColor.GRAY + "* imports everything");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation delete " + ChatColor.GRAY + "* deletes everything");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation import [id] [id] ...");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation remove [id] [id] ...");
                            ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation delete [id] [id] ...");
                            return true;
                        }

                        if (args[0].equals("list"))
                        {
                            plugin.getCommunicationManager().notifyStoredTranslocations(player);
                            return true;
                        }

                        if (args[0].equals("delete") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.delete"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "You must name your translocation field first");
                                    return true;
                                }

                                if (args.length == 0)
                                {
                                    plugin.getStorageManager().deleteTranslocation(args[1], player.getName());
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + Helper.capitalize(args[0]) + " has been deleted");
                                }
                                else
                                {
                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + arg + " is not a valid block id, skipped.");
                                            continue;
                                        }

                                        int count = plugin.getStorageManager().deleteBlockTypeFromTranslocation(field.getName(), player.getName(), entry);

                                        if (count > 0)
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + count + " " + Helper.friendlyBlockType(Material.getMaterial(entry.getTypeId()).toString()) + " from " + field.getName());
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + "No blocks matched " + arg);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a translocation block");
                            }
                            return true;
                        }

                        if (args[0].equals("remove") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.remove"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Translocation field must be enabled to remove blocks");
                                    return true;
                                }

                                if (args.length > 0)
                                {
                                    List<BlockTypeEntry> entries = new LinkedList<BlockTypeEntry>();

                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + arg + " is not a valid block id, skipped.");
                                            continue;
                                        }

                                        entries.add(entry);
                                    }

                                    if (!entries.isEmpty())
                                    {
                                        plugin.getTranslocationManager().removeBlocks(field, player, entries);
                                    }
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Usage: /ps translocation remove [id] [id] ...");
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a translocation block");
                            }
                            return true;
                        }

                        if (args[0].equals("unlink") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Translocation field must be enabled to unlink");
                                    return true;
                                }

                                int count = plugin.getStorageManager().appliedTranslocationCount(field);

                                if (count > 0)
                                {
                                    plugin.getStorageManager().deleteAppliedTranslocation(field);

                                    if (!plugin.getStorageManager().existsTranslocationDataWithName(field.getName(), field.getOwner()))
                                    {
                                        plugin.getStorageManager().deleteTranslocationHead(field.getName(), field.getOwner());
                                    }

                                    ChatBlock.sendMessage(player, ChatColor.GRAY + " * " + ChatColor.YELLOW + "Translocation " + field.getName() + " unlinked from " + count + " blocks");
                                }
                                else
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "No blocks to unlink");
                                    return true;
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a translocation block");
                            }
                            return true;
                        }

                        if (args[0].equals("import") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.import"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.RED + "Translocation field must be enabled to import blocks");
                                    return true;
                                }

                                if (args.length == 0)
                                {
                                    plugin.getTranslocationManager().importBlocks(field, player, null);
                                }
                                else
                                {
                                    List<BlockTypeEntry> entries = new LinkedList<BlockTypeEntry>();

                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + arg + " is not a valid block id, skipped.");
                                            continue;
                                        }

                                        if (!field.getSettings().canTranslocate(entry))
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.RED + arg + " is blacklisted, skipped.");
                                            continue;
                                        }

                                        entries.add(entry);
                                    }

                                    if (!entries.isEmpty())
                                    {
                                        plugin.getTranslocationManager().importBlocks(field, player, entries);
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(sender, ChatColor.RED + "You are not pointing at a translocation block");
                            }
                            return true;

                        }
                    }
                    else if (cmd.equals("more") && hasplayer)
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
                            if (Helper.isTypeEntry(args[0]))
                            {
                                BlockTypeEntry type = Helper.toTypeEntry(args[0]);

                                if (type != null)
                                {
                                    if (!plugin.getCommunicationManager().showCounts(sender, type))
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.RED + "Not a valid field type");
                                    }
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
                                int count = plugin.getForceFieldManager().deleteFields(sourceFields);

                                if (count > 0)
                                {
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Protective field removed from the field");

                                    if (plugin.getSettingsManager().isLogBypassDelete())
                                    {
                                        PreciousStones.log("Protective field removed from {0} {1}", count, Helper.plural(count, "field", "'s"));
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
                            if (Helper.isTypeEntry(args[0]))
                            {
                                BlockTypeEntry type = Helper.toTypeEntry(args[0]);

                                if (type != null)
                                {
                                    int fields = plugin.getForceFieldManager().deleteFieldsOfType(type);
                                    int ubs = plugin.getUnbreakableManager().deleteUnbreakablesOfType(type);

                                    if (fields > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + fields + " " + Material.getMaterial(type.getTypeId()) + " fields");
                                    }

                                    if (ubs > 0)
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Deleted " + fields + " " + Material.getMaterial(type.getTypeId()) + " unbreakables");
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


                            TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                            Block targetBlock = aiming.getTargetBlock();

                            if (targetBlock != null)
                            {
                                Field field = plugin.getForceFieldManager().getField(targetBlock);

                                if (field != null)
                                {
                                    // transfer the count over to the new owner

                                    PlayerEntry oldData = plugin.getPlayerManager().getPlayerEntry(field.getOwner());
                                    oldData.decrementFieldCount(field.getSettings().getTypeEntry());

                                    PlayerEntry newData = plugin.getPlayerManager().getPlayerEntry(owner);
                                    newData.incrementFieldCount(field.getSettings().getTypeEntry());

                                    plugin.getStorageManager().changeTranslocationOwner(field, owner);
                                    plugin.getStorageManager().offerPlayer(field.getOwner());
                                    plugin.getStorageManager().offerPlayer(owner);

                                    // change the owner

                                    field.setOwner(owner);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.sendMessage(sender, ChatColor.AQUA + "Owner set to " + owner);
                                    return true;
                                }
                            }

                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "You are not pointing at a field or unbreakable block");
                            return true;
                        }
                    }
                    else if (cmd.equals("changeowner") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.change-owner") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String owner = args[0];

                            if (owner.contains(":"))
                            {
                                ChatBlock.sendMessage(sender, ChatColor.AQUA + "Cannot assign groups or clans as owners");
                                return true;
                            }

                            TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                            Block targetBlock = aiming.getTargetBlock();

                            if (targetBlock != null)
                            {
                                Field field = plugin.getForceFieldManager().getField(targetBlock);

                                if (field != null)
                                {
                                    if (field.isOwner(player.getName()))
                                    {
                                        if (field.hasFlag(FieldFlag.CAN_CHANGE_OWNER))
                                        {
                                            field.setNewOwner(owner);
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Field can now be taken by " + owner + " via right-click");
                                            return true;
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(sender, ChatColor.AQUA + "Field type does not support the changing of ownership");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.sendMessage(sender, ChatColor.AQUA + "Only the owner of the field can change its owner");
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
                    else if (cmd.equals("enableall") && plugin.getPermissionsManager().has(player, "preciousstones.admin.enableall"))
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            ChatBlock.sendMessage(player, ChatColor.AQUA + "All fields are temporarily down while being changed");

                            int count = plugin.getStorageManager().enableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "No fields found with that flag enabled");
                            }
                            else
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Flag enabled on " + count + " fields");
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals("disableall") && plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall"))
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            ChatBlock.sendMessage(player, ChatColor.AQUA + "All fields are temporarily down while being changed");

                            int count = plugin.getStorageManager().disableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "No fields found with that flag disabled");
                            }
                            else
                            {
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Flag disabled on " + count + " fields");
                            }
                        }
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
            System.out.print("Error: " + ex.getMessage());

            for (StackTraceElement el : ex.getStackTrace())
            {
                System.out.print(el.toString());
            }
        }

        return false;
    }
}
