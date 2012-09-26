package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
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
                        ChatBlock.send(player, "{red}PreciousStones disabled in this world");
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

                        if(plugin.getSettingsManager().isDebug())
                        {
                            ChatBlock.send(sender, "{aqua}Debug output enabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "{aqua}Debug output disabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("debugdb") && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugdb(!plugin.getSettingsManager().isDebugdb());
                        if(plugin.getSettingsManager().isDebugdb())
                        {
                            ChatBlock.send(sender, "{aqua}Debug db output enabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "{aqua}Debug db output disabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("debugsql") && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebugsql(!plugin.getSettingsManager().isDebugsql());
                        if(plugin.getSettingsManager().isDebugsql())
                        {
                            ChatBlock.send(sender, "{aqua}Debug sql output enabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "{aqua}Debug sql output disabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("on") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled();
                        if (isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(false);
                            ChatBlock.send(sender, "{aqua}Enabled the placing of pstones");
                        }
                        else
                        {
                            ChatBlock.send(sender, "{red}Pstone placement is already enabled");
                        }
                        return true;
                    }
                    else if (cmd.equals("off") && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled();
                        if (!isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(true);
                            ChatBlock.send(sender, "{aqua}Disabled the placing of pstones");
                        }
                        else
                        {
                            ChatBlock.send(sender, "{red}Pstone placement is already disabled");
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
                                        ChatBlock.send(sender, "{red}This field can only be modified while disabled");
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
                                            ChatBlock.send(sender, "{red}{1.player} does not have permissions to be allowed", playerName);
                                            continue;
                                        }
                                    }

                                    boolean done = plugin.getForceFieldManager().addAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.send(sender, "{aqua}{1.player} has been allowed in the field", playerName);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}{1.player} is already on the list", playerName);
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
                                    ChatBlock.send(sender, "{aqua}{1.player} has been allowed in {2.count} fields", playerName, count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{aqua}{1.player} is already on all your lists", playerName);
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
                                        ChatBlock.send(sender, "{red}This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args)
                                {
                                    if (field.containsPlayer(playerName))
                                    {
                                        ChatBlock.send(sender, "{red}Cannot remove a player that's currently in your field");
                                        return true;
                                    }

                                    if (plugin.getForceFieldManager().conflictOfInterestExists(field, playerName))
                                    {
                                        ChatBlock.send(sender, "{red}You cannot disallow {1.player}, one of his fields is overlapping yours", playerName);
                                        return true;
                                    }

                                    boolean done = plugin.getForceFieldManager().removeAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.send(sender, "{aqua}{1.player} was removed from the field", playerName);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{red}{1.player} not found", playerName);
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
                                    ChatBlock.send(sender, "{aqua}{1.player} was removed {2.count} fields", playerName, count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{aqua}Nothing to be done");
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
                            List<String> allowed = field.getAllAllowed();

                            if (allowed.size() > 0)
                            {
                                ChatBlock.send(sender, "{yellow}Allowed: {aqua}{1.allowed}", Helper.toMessage(new ArrayList<String>(allowed), ", "));
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}No players allowed in this field");
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
                                ChatBlock.send(sender, "{yellow}Inhabitants: {aqua}{1.inhabitants}", Helper.toMessage(new ArrayList<String>(inhabitants), ", "));
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}No players found in the field");
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

                                        ChatBlock.send(player, "{yellow}Translocation {1.field} unlinked from {2.count} blocks", field.getName(), count);
                                    }
                                }

                                // check if one exists with that name already

                                if (plugin.getStorageManager().existsFieldWithName(fieldName, player.getName()))
                                {
                                    ChatBlock.send(sender, "{red}A translocation block already exists with that name");
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
                                    ChatBlock.send(sender, "{red}This field can only be modified while disabled");
                                    return true;
                                }
                            }

                            if (fieldName == null)
                            {
                                boolean done = plugin.getForceFieldManager().setNameField(field, "");

                                if (done)
                                {
                                    ChatBlock.send(sender, "{aqua}Field's name has been cleared");
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}No nameable fields found");
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
                                            ChatBlock.send(sender, "{aqua}Translocation {1.field} has {2.count} stored blocks", fieldName, count);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "{aqua}Translocation {1.field} created. Recoding changes...", fieldName);
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}Renamed field to {1.field}", fieldName);
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}No nameable fields found");
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
                                            ChatBlock.send(player, "{red}Cannot reshape a translocation cuboid once its in use");
                                            return true;
                                        }
                                    }
                                }

                                if (!field.hasFlag(FieldFlag.CUBOID))
                                {
                                    if (radius >= 0 && (radius <= fs.getRadius() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.setradius")))
                                    {
                                        plugin.getForceFieldManager().removeSourceField(field);

                                        field.setRadius(radius);
                                        plugin.getStorageManager().offerField(field);

                                        plugin.getForceFieldManager().addSourceField(field);

                                        ChatBlock.send(sender, "{aqua}Radius set to {1.radius}", radius);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{red}Radius must be less than or equal to {1.radius}", fs.getRadius());
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}Cannot change radius of a cuboid");
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
                                        ChatBlock.send(sender, "{red}This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                FieldSettings fs = field.getSettings();

                                if (fs.hasVeocityFlag())
                                {
                                    if (velocity < 0 || velocity > 5)
                                    {
                                        ChatBlock.send(sender, "{red}Velocity must be from 0 to 5");
                                        return true;
                                    }

                                    field.setVelocity(velocity);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.send(sender, "{aqua}Velocity set to {1.velocity}", velocity);
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
                                ChatBlock.send(sender, "{aqua}Field has been disabled");
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}Field is already disabled");
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
                                ChatBlock.send(sender, "{aqua}Field has been enabled");
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}Field is already enabled");
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

                            ChatBlock.send(sender, "{aqua}Visualization density changed to {1.density}", density);
                            return true;
                        }
                        else if (args.length == 0)
                        {
                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
                            ChatBlock.send(sender, "{aqua}Your visualization density is set to {1.density}", data.getDensity());
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
                                            ChatBlock.send(sender, "{red}This field's flags can only be toggled while disabled");
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

                                    if (plugin.getSettingsManager().isUnToggable(flagStr))
                                    {
                                        unToggable = true;
                                    }

                                    if (unToggable)
                                    {
                                        ChatBlock.send(sender, "{red}This flag cannot be toggled");
                                        return true;
                                    }

                                    boolean enabled = field.toggleFieldFlag(flagStr);

                                    if (enabled)
                                    {
                                        ChatBlock.send(sender, "{aqua}The {1.flag} flag has been enabled.", flagStr);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}The {1.flag} flag has been disabled.", flagStr);
                                    }

                                    field.dirtyFlags();
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}The field does not contain this flag");
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

                                        Set<Field> fieldsInArea ;

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
                                            ChatBlock.send(sender, "{aqua}Visualizing...");

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
                                            ChatBlock.send(sender, "{red}No fields in area");
                                        }
                                    }
                                    else
                                    {
                                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                                        if (field != null)
                                        {
                                            ChatBlock.send(sender, "{aqua}Visualizing...");

                                            plugin.getVisualizationManager().addVisualizationField(player, field);
                                            plugin.getVisualizationManager().displayVisualization(player, true);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "{red}You are not inside of a field");
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}A visualization is already taking place");
                            }
                        }
                        else
                        {
                            ChatBlock.send(sender, "{red}Cannot visualize while defining a cuboid");
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
                                        ChatBlock.send(sender, "{aqua}Marking {1.count} field blocks...", fieldsInArea.size());

                                        for (Field f : fieldsInArea)
                                        {
                                            plugin.getVisualizationManager().addFieldMark(player, f);
                                        }

                                        plugin.getVisualizationManager().displayVisualization(player, false);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}No fields in the area");
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
                                            ChatBlock.send(sender, "{aqua}Marking {1.count} field blocks...", count);
                                            plugin.getVisualizationManager().displayVisualization(player, false);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "{aqua}No fields in the area");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}No fields in the area");
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}A visualization is already taking place");
                            }
                        }
                        else
                        {
                            ChatBlock.send(sender, "{red}Cannot mark fields while defining a cuboid");
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
                                        ChatBlock.send(sender, "{aqua}The field flag inserted");
                                        plugin.getStorageManager().offerField(field);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{red}The field flag entered does not exist");
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}The field already contains this flag");
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
                            ChatBlock.send(sender, "{aqua}The field flags have been reverted to default.");
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
                                        ChatBlock.send(sender, "{red}This field can only be modified while disabled");
                                        return true;
                                    }
                                }

                                if (interval >= plugin.getSettingsManager().getGriefRevertMinInterval() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.interval"))
                                {
                                    field.setRevertSecs(interval);
                                    plugin.getGriefUndoManager().register(field);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.send(sender, "{aqua}The grief-revert interval has been set to {1.count} seconds", interval);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}The minimum interval is {1.count} seconds", plugin.getSettingsManager().getGriefRevertMinInterval());
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}You are not pointing at a grief-revert block");
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
                                ChatBlock.send(sender, "{red}You are not pointing at a snitch block");
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
                                        ChatBlock.send(sender, "{aqua}Cleared the snitch list");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{red}Snitch list is empty");
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
                            ChatBlock.send(sender,  "* All commands (except for list) require you to be pointing at a field block or standing in the field");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation list");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation import {gray}* imports everything");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation delete {gray}* deletes everything");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation import [id] [id] ...");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation remove [id] [id] ...");
                            ChatBlock.send(sender, "{red}Usage: /ps translocation delete [id] [id] ...");
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
                                    ChatBlock.send(sender, "{red}A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "{red}You must name your translocation field first");
                                    return true;
                                }

                                if (args.length == 0)
                                {
                                    plugin.getStorageManager().deleteTranslocation(args[1], player.getName());
                                    ChatBlock.send(sender, "{aqua}{1.translocation} has been deleted", args[0]);
                                }
                                else
                                {
                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.send(sender, "{red}{1.blockid} is not a valid block id, skipped.", arg);
                                            continue;
                                        }

                                        int count = plugin.getStorageManager().deleteBlockTypeFromTranslocation(field.getName(), player.getName(), entry);

                                        if (count > 0)
                                        {
                                            ChatBlock.send(sender, "{aqua}Deleted {1.count} {2.blockid} from {3.field}",count, Helper.friendlyBlockType(Material.getMaterial(entry.getTypeId()).toString()), field.getName());
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "{red}No blocks matched ", arg);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}You are not pointing at a translocation block");
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
                                    ChatBlock.send(sender, "{red}A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "{red}You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "{red}Translocation field must be enabled to remove blocks");
                                    return true;
                                }

                                if (args.length > 0)
                                {
                                    List<BlockTypeEntry> entries = new ArrayList<BlockTypeEntry>();

                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.send(sender, "{red}{1.blockid} is not a valid block id, skipped.", arg);
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
                                    ChatBlock.send(sender, "{red}Usage: /ps translocation remove [id] [id] ...");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}You are not pointing at a translocation block");
                            }
                            return true;
                        }

                        if (args[0].equals("unlink") && plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink"))
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.send(sender, "{red}A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "{red}You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "{red}Translocation field must be enabled to unlink");
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

                                    ChatBlock.send(player, "{yellow}Translocation {1.field} unlinked from {2.count} blocks", field.getName(), count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "{red}No blocks to unlink");
                                    return true;
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "{red}You are not pointing at a translocation block");
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
                                    ChatBlock.send(sender, "{red}A translocation is currently taking place");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "{red}You must name your translocation field first");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "{red}Translocation field must be enabled to import blocks");
                                    return true;
                                }

                                if (args.length == 0)
                                {
                                    plugin.getTranslocationManager().importBlocks(field, player, null);
                                }
                                else
                                {
                                    List<BlockTypeEntry> entries = new ArrayList<BlockTypeEntry>();

                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.send(sender, "{red}{1.blockid} is not a valid block id, skipped.", arg);
                                            continue;
                                        }

                                        if (!field.getSettings().canTranslocate(entry))
                                        {
                                            ChatBlock.send(sender, "{red}{1.blockid} is blacklisted, skipped.", arg);
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
                                ChatBlock.send(sender, "{red}You are not pointing at a translocation block");
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
                                ChatBlock.send(sender, "{dark-gray}Type /ps more to view next page.");
                            }
                            ChatBlock.sendBlank(player);

                            return true;
                        }

                        ChatBlock.send(sender, "{gold}Nothing more to see.");
                        return true;
                    }
                    else if (cmd.equals("counts"))
                    {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasplayer)
                        {
                            if (!plugin.getCommunicationManager().showFieldCounts(player, player.getName()))
                            {
                                ChatBlock.send(sender, "{red}Player does not have any fields");
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
                                        ChatBlock.send(sender, "{red}Not a valid field type");
                                    }
                                }
                            }
                            else if (Helper.isString(args[0]) && hasplayer)
                            {
                                String target = args[0];

                                if (!plugin.getCommunicationManager().showFieldCounts(player, target))
                                {
                                    ChatBlock.send(sender, "{red}Player does not have any fields");
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
                                String targetName = args[0];
                                plugin.getCommunicationManager().showFieldLocations(sender, -1, targetName);
                            }

                            if (args.length == 2 && Helper.isString(args[0]) && Helper.isInteger(args[1]))
                            {
                                String targetName = args[0];
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
                                    ChatBlock.send(sender, "{aqua}Protective field removed from the field");

                                    if (plugin.getSettingsManager().isLogBypassDelete())
                                    {
                                        PreciousStones.log("Protective field removed from {1.count} fields", count);
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
                                        ChatBlock.send(sender, "{aqua}Deleted {1.count} {2.block-type} fields", fields, Material.getMaterial(type.getTypeId()));
                                    }

                                    if (ubs > 0)
                                    {
                                        ChatBlock.send(sender, "{aqua}Deleted {1.count} {2.block-type} unbreakables", ubs, Material.getMaterial(type.getTypeId()));
                                    }

                                    if (ubs == 0 && fields == 0)
                                    {
                                        ChatBlock.send(sender, "{aqua}No pstones of the type found");
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
                                    ChatBlock.send(sender, "{aqua}Deleted {1.player}'s {2.count} fields", args[0], fields);
                                }

                                if (ubs > 0)
                                {
                                    ChatBlock.send(sender, "{aqua}Deleted {1.player}'s {2.count} unbreakables", args[0], ubs);
                                }

                                if (ubs == 0 && fields == 0)
                                {
                                    ChatBlock.send(sender, "{aqua}The player had no pstones");
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
                                ChatBlock.send(sender, "{aqua}Cannot assign groups or clans as owners");
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
                                    ChatBlock.send(sender, "{aqua}Owner set to {1.player}", owner);
                                    return true;
                                }
                            }

                            ChatBlock.send(sender, "{aqua}You are not pointing at a field or unbreakable block");
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
                                ChatBlock.send(sender, "{aqua}Cannot assign groups or clans as owners");
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
                                            ChatBlock.send(sender, "{aqua}Field can now be taken by {1.player} via right-click", owner);
                                            return true;
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "{aqua}Field type does not support the changing of ownership");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "{aqua}Only the owner of the field can change its owner");
                                    }
                                }
                            }

                            ChatBlock.send(sender, "{aqua}You are not pointing at a field or unbreakable block");
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
                                    ChatBlock.send(sender, "{aqua}{unbreakable}", u.toString());
                                }

                                for (Field f : fields)
                                {
                                    ChatBlock.send(sender, "{aqua}{field}", f.toString());
                                }

                                if (unbreakables.isEmpty() && fields.isEmpty())
                                {
                                    ChatBlock.send(sender, "{aqua}No field or unbreakable blocks found");
                                }
                                return true;
                            }
                        }
                    }
                    else if (cmd.equals("reload") && plugin.getPermissionsManager().has(player, "preciousstones.admin.reload"))
                    {
                        plugin.getSettingsManager().load();
                        ChatBlock.send(sender, "{aqua}Configuration reloaded");
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

                            ChatBlock.send(player, "{aqua}All fields are temporarily down while being changed");

                            int count = plugin.getStorageManager().enableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.send(player, "{aqua}No fields found with that flag enabled");
                            }
                            else
                            {
                                ChatBlock.send(player, "{aqua}Flag enabled on {1.count} fields", count);
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals("disableall") && plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall"))
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            ChatBlock.send(player, "{aqua}All fields are temporarily down while being changed");

                            int count = plugin.getStorageManager().disableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.send(player, "{aqua}No fields found with that flag disabled");
                            }
                            else
                            {
                                ChatBlock.send(player, "{aqua}Flag disabled on {1.count} fields", count);
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
                            ChatBlock.send(sender, "{aqua}Cleaned {1.count} orphaned fields", cleandFF);
                        }
                        if (cleandU > 0)
                        {
                            ChatBlock.send(sender, "{aqua}Cleaned {1.count} orphaned unbreakable blocks", cleandU);
                        }
                        if (cleandFF == 0 && cleandU == 0)
                        {
                            ChatBlock.send(sender, "{aqua}No orphans found");
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
                            ChatBlock.send(sender, "{aqua}Reverted {1.count} orphaned fields", cleandFF);
                        }
                        if (cleandU > 0)
                        {
                            ChatBlock.send(sender, "{aqua}Reverted {1.count} orphaned unbreakable blocks", cleandU);
                        }

                        if (cleandFF == 0 && cleandU == 0)
                        {
                            ChatBlock.send(sender, "{aqua}No orphan fields/unbreakables found");
                        }
                        return true;
                    }

                    ChatBlock.send(sender, "{red}Not a valid command or insufficient permissions");

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
