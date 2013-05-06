package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.Bukkit;
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
                        ChatBlock.send(player, "psDisabled");
                        return true;
                    }
                }

                if (args.length > 0)
                {
                    String cmd = args[0];
                    args = Helper.removeFirst(args);

                    Block block = hasplayer ? player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()) : null;

                    if (cmd.equals(ChatBlock.format("commandDebug")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
                    {
                        plugin.getSettingsManager().setDebug(!plugin.getSettingsManager().isDebug());

                        if (plugin.getSettingsManager().isDebug())
                        {
                            ChatBlock.send(sender, "debugEnabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "debugDisabled");
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandFields")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.fields"))
                    {
                        plugin.getCommunicationManager().showConfiguredFields(sender);
                        return true;
                    }

                    else if (cmd.equals(ChatBlock.format("commandOn")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled();
                        if (isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(false);
                            ChatBlock.send(sender, "placingEnabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "placingAlreadyEnabled");
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandOff")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer)
                    {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled();
                        if (!isDisabled)
                        {
                            plugin.getPlayerManager().getPlayerEntry(player.getName()).setDisabled(true);
                            ChatBlock.send(sender, "placingDisabled");
                        }
                        else
                        {
                            ChatBlock.send(sender, "placingAlreadyDisabled");
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandAllow")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-allowing"))
                                {
                                    if (field.hasFlag(FieldFlag.NO_ALLOWING))
                                    {
                                        ChatBlock.send(sender, "noSharing");
                                        return true;
                                    }
                                }

                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args)
                                {
                                    Player allowed = Bukkit.getServer().getPlayerExact(playerName);

                                    // only those with permission can be allowed

                                    if (!field.getSettings().getRequiredPermissionAllow().isEmpty())
                                    {
                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.required-permission"))
                                        {
                                            if (!plugin.getPermissionsManager().has(allowed, field.getSettings().getRequiredPermissionAllow()))
                                            {
                                                ChatBlock.send(sender, "noPermsForAllow", playerName);
                                                continue;
                                            }
                                        }
                                    }

                                    boolean done = plugin.getForceFieldManager().addAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.send(sender, "hasBeenAllowed", playerName);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "alreadyAllowed", playerName);
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
                    else if (cmd.equals(ChatBlock.format("commandAllowall")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            for (String playerName : args)
                            {
                                int count = plugin.getForceFieldManager().allowAll(player, playerName);

                                if (count > 0)
                                {
                                    ChatBlock.send(sender, "hasBeenAllowedIn", playerName, count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "isAlreadyAllowedOnAll", playerName);
                                }
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandRemove")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args)
                                {
                                    if (field.containsPlayer(playerName))
                                    {
                                        ChatBlock.send(sender, "cannotRemovePlayerInField");
                                        return true;
                                    }

                                    if (plugin.getForceFieldManager().conflictOfInterestExists(field, playerName))
                                    {
                                        ChatBlock.send(sender, "cannotDisallowWhenOverlap", playerName);
                                        return true;
                                    }

                                    boolean done = plugin.getForceFieldManager().removeAllowed(field, playerName);

                                    if (done)
                                    {
                                        ChatBlock.send(sender, "removedFromField", playerName);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "playerNotFound", playerName);
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
                    else if (cmd.equals(ChatBlock.format("commandRemoveall")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            for (String playerName : args)
                            {
                                int count = plugin.getForceFieldManager().removeAll(player, playerName);

                                if (count > 0)
                                {
                                    ChatBlock.send(sender, "removedFromFields", playerName, count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "nothingToBeDone");
                                }
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandAllowed")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            List<String> allowed = field.getAllAllowed();

                            if (allowed.size() > 0)
                            {
                                ChatBlock.send(sender, "allowedList", Helper.toMessage(new ArrayList<String>(allowed), ", "));
                            }
                            else
                            {
                                ChatBlock.send(sender, "noPlayersAllowedOnField");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandCuboid")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.create.forcefield") && hasplayer)
                    {
                        if (args.length >= 1)
                        {
                            if ((args[0]).equals(ChatBlock.format("commandCuboidOpen")))
                            {
                                Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                                if (field != null)
                                {
                                    if (field.isRented())
                                    {
                                        ChatBlock.send(player, "fieldSignCannotChange");
                                        return true;
                                    }

                                    plugin.getCuboidManager().openCuboid(player, field);
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                            }
                            else if ((args[0]).equals(ChatBlock.format("commandCuboidClose")))
                            {
                                plugin.getCuboidManager().closeCuboid(player);
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandWho")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            HashSet<String> inhabitants = plugin.getForceFieldManager().getWho(player, field);

                            if (inhabitants.size() > 0)
                            {
                                ChatBlock.send(sender, "inhabitantsList", Helper.toMessage(new ArrayList<String>(inhabitants), ", "));
                            }
                            else
                            {
                                ChatBlock.send(sender, "noPlayersFoundOnField");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandSetname")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasplayer)
                    {
                        String fieldName = null;

                        if (args.length >= 1)
                        {
                            fieldName = Helper.toMessage(args);
                        }

                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            if (field.isRented())
                            {
                                ChatBlock.send(player, "fieldSignCannotChange");
                                return true;
                            }

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

                                        ChatBlock.send(player, "translocationUnlinked", field.getName(), count);
                                    }
                                }

                                // check if one exists with that name already

                                if (plugin.getStorageManager().existsFieldWithName(fieldName, player.getName()))
                                {
                                    ChatBlock.send(sender, "translocationExists");
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
                                    field.setDisabled(true, player);
                                    field.dirtyFlags();
                                }
                                else
                                {
                                    boolean disabled = field.setDisabled(false, player);

                                    if (!disabled)
                                    {
                                        ChatBlock.send(player, "cannotEnable");
                                        return true;
                                    }
                                    field.dirtyFlags();
                                }
                            }

                            if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                            {
                                if (!field.isDisabled())
                                {
                                    ChatBlock.send(sender, "onlyModWhileDisabled");
                                    return true;
                                }
                            }

                            if (fieldName == null)
                            {
                                boolean done = plugin.getForceFieldManager().setNameField(field, "");

                                if (done)
                                {
                                    ChatBlock.send(sender, "fieldNameCleared");
                                }
                                else
                                {
                                    ChatBlock.send(sender, "noNameableFieldFound");
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
                                            ChatBlock.send(sender, "translocationHasBlocks", fieldName, count);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "translocationCreated", fieldName);
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "translocationRenamed", fieldName);
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "noNameableFieldFound");
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
                    else if (cmd.equals(ChatBlock.format("commandSetradius")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int radius = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                FieldSettings fs = field.getSettings();

                                if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                {
                                    if (field.isNamed())
                                    {
                                        if (plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner()))
                                        {
                                            ChatBlock.send(player, "translocationCannotReshape");
                                            return true;
                                        }
                                    }
                                }

                                if (field.isRented())
                                {
                                    ChatBlock.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                if (!field.hasFlag(FieldFlag.CUBOID))
                                {
                                    if (radius >= 0 && (radius <= fs.getRadius() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.setradius")))
                                    {
                                        plugin.getForceFieldManager().removeSourceField(field);

                                        field.setRadius(radius);
                                        plugin.getStorageManager().offerField(field);

                                        plugin.getForceFieldManager().addSourceField(field);

                                        ChatBlock.send(sender, "radiusSet", radius);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "radiusMustBeLessThan", fs.getRadius());
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "cuboidCannotChangeRadius");
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
                    else if (cmd.equals(ChatBlock.format("commandSetvelocity")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isFloat(args[0]))
                        {
                            float velocity = Float.parseFloat(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                FieldSettings fs = field.getSettings();

                                if (fs.hasVeocityFlag())
                                {
                                    if (velocity < 0 || velocity > 5)
                                    {
                                        ChatBlock.send(sender, "velocityMustBe");
                                        return true;
                                    }

                                    field.setVelocity(velocity);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.send(sender, "velocitySetTo", velocity);
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
                    else if (cmd.equals(ChatBlock.format("commandDisable")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.disable") && hasplayer)
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

                                if (field.isRented())
                                {
                                    ChatBlock.send(player, "fieldSignCannotDisable");
                                    return true;
                                }

                                field.setDisabled(true, player);
                                field.dirtyFlags();
                                ChatBlock.send(sender, "fieldDisabled");
                            }
                            else
                            {
                                ChatBlock.send(sender, "fieldAlreadyDisabled");
                            }
                            return true;
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandEnable")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.enable") && hasplayer)
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

                                boolean disabled = field.setDisabled(false, player);

                                if (!disabled)
                                {
                                    ChatBlock.send(sender, "cannotEnable");
                                    return true;
                                }

                                field.dirtyFlags();
                                ChatBlock.send(sender, "fieldEnabled");
                            }
                            else
                            {
                                ChatBlock.send(sender, "fieldAlreadyEnabled");
                            }
                            return true;
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandDensity")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int density = Integer.parseInt(args[0]);

                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
                            data.setDensity(density);
                            plugin.getStorageManager().offerPlayer(player.getName());

                            ChatBlock.send(sender, "visualizationChanged", density);
                            return true;
                        }
                        else if (args.length == 0)
                        {
                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
                            ChatBlock.send(sender, "visualizationSet", data.getDensity());
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandToggle")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
                                {
                                    if (field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED))
                                    {
                                        if (!field.isDisabled())
                                        {
                                            ChatBlock.send(sender, "flagsToggledWhileDisabled");
                                            return true;
                                        }
                                    }
                                }

                                if (field.isRented())
                                {
                                    ChatBlock.send(player, "fieldSignCannotChange");
                                    return true;
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

                                    try
                                    {
                                        if (FieldFlag.getByString(flagStr).isUnToggable())
                                        {
                                            unToggable = true;
                                        }
                                    }
                                    catch (Exception ex)
                                    {
                                        ChatBlock.send(sender, "flagNotFound");
                                        return true;
                                    }

                                    if (unToggable)
                                    {
                                        ChatBlock.send(sender, "flagCannottoggle");
                                        return true;
                                    }

                                    boolean enabled = field.toggleFieldFlag(flagStr);

                                    if (enabled)
                                    {
                                        ChatBlock.send(sender, "flagEnabled", flagStr);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "flagDisabled", flagStr);
                                    }

                                    plugin.getStorageManager().offerField(field);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "flagNotFound");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if ((cmd.equals(ChatBlock.format("commandVisualize")) || cmd.equals("visualise")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasplayer)
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

                                        Set<Field> fieldsInArea;

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
                                            ChatBlock.send(sender, "visualizing");

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
                                            ChatBlock.send(sender, "noFieldsInArea");
                                        }
                                    }
                                    else
                                    {
                                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                                        if (field != null)
                                        {
                                            ChatBlock.send(sender, "visualizing");

                                            plugin.getVisualizationManager().addVisualizationField(player, field);
                                            plugin.getVisualizationManager().displayVisualization(player, true);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "notInsideField");
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "visualizationTakingPlace");
                            }
                        }
                        else
                        {
                            ChatBlock.send(sender, "visualizationNotWhileCuboid");
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandMark")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && hasplayer)
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
                                        ChatBlock.send(sender, "markingFields", fieldsInArea.size());

                                        for (Field f : fieldsInArea)
                                        {
                                            plugin.getVisualizationManager().addFieldMark(player, f);
                                        }

                                        plugin.getVisualizationManager().displayVisualization(player, false);
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "noFieldsInArea");
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
                                            ChatBlock.send(sender, "markingFields", count);
                                            plugin.getVisualizationManager().displayVisualization(player, false);
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "noFieldsInArea");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "noFieldsInArea");
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "visualizationTakingPlace");
                            }
                        }
                        else
                        {
                            ChatBlock.send(sender, "markingNotWhileCuboid");
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandInsert")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (!field.hasFlag(flagStr) && !field.hasDisabledFlag(flagStr))
                                {
                                    plugin.getForceFieldManager().removeSourceField(field);

                                    if (field.insertFieldFlag(flagStr))
                                    {
                                        field.dirtyFlags();
                                        plugin.getStorageManager().offerField(field);
                                        ChatBlock.send(sender, "flagInserted");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "flagNotExists");
                                    }
                                    plugin.getForceFieldManager().addSourceField(field);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "flagExists");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandClear")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(flagStr) || field.hasDisabledFlag(flagStr))
                                {
                                    plugin.getForceFieldManager().removeSourceField(field);

                                    if (field.clearFieldFlag(flagStr))
                                    {
                                        field.dirtyFlags();
                                        plugin.getStorageManager().offerField(field);
                                        ChatBlock.send(sender, "flagCleared");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "flagNotExists");
                                    }
                                    plugin.getForceFieldManager().addSourceField(field);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "flagExists");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandReset")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.reset") && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null)
                        {
                            field.RevertFlags();
                            plugin.getStorageManager().offerField(field);
                            ChatBlock.send(sender, "flagsReverted");
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandSetinterval")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasplayer)
                    {
                        if (args.length == 1 && Helper.isInteger(args[0]))
                        {
                            int interval = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.GRIEF_REVERT);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                                {
                                    if (!field.isDisabled())
                                    {
                                        ChatBlock.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                if (interval >= plugin.getSettingsManager().getGriefRevertMinInterval() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.interval"))
                                {
                                    field.setRevertSecs(interval);
                                    plugin.getGriefUndoManager().register(field);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.send(sender, "griefRevertIntervalSet", interval);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "minInterval", plugin.getSettingsManager().getGriefRevertMinInterval());
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "notPointingAtGriefRevert");
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandSnitch")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasplayer)
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
                                ChatBlock.send(sender, "notPointingAtSnitch");
                            }

                            return true;
                        }
                        else if (args.length == 1)
                        {
                            if (args[0].equals(ChatBlock.format("commandSnitchClear")))
                            {
                                Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.SNITCH);

                                if (field != null)
                                {
                                    boolean cleaned = plugin.getForceFieldManager().cleanSnitchList(field);

                                    if (cleaned)
                                    {
                                        ChatBlock.send(sender, "clearedSnitch");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "snitchEmpty");
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
                    else if (cmd.equals(ChatBlock.format("commandTranslocation")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.use") && hasplayer)
                    {
                        if (args.length == 0)
                        {
                            ChatBlock.send(sender, "translocationMenu1");
                            ChatBlock.send(sender, "translocationMenu2");
                            ChatBlock.send(sender, "translocationMenu3");
                            ChatBlock.send(sender, "translocationMenu4");
                            ChatBlock.send(sender, "translocationMenu5");
                            ChatBlock.send(sender, "translocationMenu6");
                            ChatBlock.send(sender, "translocationMenu7");
                            return true;
                        }

                        if (args[0].equals(ChatBlock.format("commandTranslocationList")))
                        {
                            plugin.getCommunicationManager().notifyStoredTranslocations(player);
                            return true;
                        }

                        if (args[0].equals(ChatBlock.format("commandTranslocationDelete")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.delete"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (args.length == 0)
                                {
                                    plugin.getStorageManager().deleteTranslocation(args[1], player.getName());
                                    ChatBlock.send(sender, "translocationDeleted", args[0]);
                                }
                                else
                                {
                                    for (String arg : args)
                                    {
                                        BlockTypeEntry entry = Helper.toTypeEntry(arg);

                                        if (entry == null || !entry.isValid())
                                        {
                                            ChatBlock.send(sender, "notValidBlockId", arg);
                                            continue;
                                        }

                                        int count = plugin.getStorageManager().deleteBlockTypeFromTranslocation(field.getName(), player.getName(), entry);

                                        if (count > 0)
                                        {
                                            ChatBlock.send(sender, "translocationDeletedBlocks", count, Helper.friendlyBlockType(entry.getTypeId()), field.getName());
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "noBlocksMatched", arg);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatBlock.format("commandTranslocationRemove")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.remove"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "translocationEnabledFirst");
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
                                            ChatBlock.send(sender, "notValidBlockId", arg);
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
                                    ChatBlock.send(sender, "usageTranslocationRemove");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatBlock.format("commandTranslocationUnlink")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink"))
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "translocationEnabledToUnlink");
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

                                    ChatBlock.send(player, "translocationUnlinked", field.getName(), count);
                                }
                                else
                                {
                                    ChatBlock.send(sender, "translocationNothingToUnlink");
                                    return true;
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatBlock.format("commandTranslocationImport")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.import"))
                        {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null)
                            {
                                if (field.isTranslocating())
                                {
                                    ChatBlock.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed())
                                {
                                    ChatBlock.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled())
                                {
                                    ChatBlock.send(sender, "translocationEnabledToImport");
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
                                            ChatBlock.send(sender, "notValidBlockId", arg);
                                            continue;
                                        }

                                        if (!field.getSettings().canTranslocate(entry))
                                        {
                                            ChatBlock.send(sender, "blockIsBlacklisted", arg);
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
                                ChatBlock.send(sender, "notPointingAtTranslocation");
                            }
                            return true;

                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandMore")) && hasplayer)
                    {
                        ChatBlock cb = plugin.getCommunicationManager().getChatBlock(player);

                        if (cb.size() > 0)
                        {
                            ChatBlock.sendBlank(player);

                            cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

                            if (cb.size() > 0)
                            {
                                ChatBlock.sendBlank(player);
                                ChatBlock.send(sender, "moreNextPage");
                            }
                            ChatBlock.sendBlank(player);

                            return true;
                        }

                        ChatBlock.send(sender, "moreNothingMore");
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandCounts")))
                    {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasplayer)
                        {
                            if (!plugin.getCommunicationManager().showFieldCounts(player, player.getName()))
                            {
                                ChatBlock.send(sender, "playerHasNoFields");
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
                                        ChatBlock.send(sender, "notValidFieldType");
                                    }
                                }
                            }
                            else if (Helper.isString(args[0]) && hasplayer)
                            {
                                String target = args[0];

                                if (!plugin.getCommunicationManager().showFieldCounts(player, target))
                                {
                                    ChatBlock.send(sender, "playerHasNoFields");
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                    else if (cmd.equals(ChatBlock.format("commandLocations")))
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
                    else if (cmd.equals(ChatBlock.format("commandInfo")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.info") && hasplayer)
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
                    else if (cmd.equals(ChatBlock.format("commandDelete")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.delete"))
                    {
                        if (args.length == 0 && hasplayer)
                        {
                            List<Field> sourceFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                            if (sourceFields.size() > 0)
                            {
                                int count = plugin.getForceFieldManager().deleteFields(sourceFields);

                                if (count > 0)
                                {
                                    ChatBlock.send(sender, "protectionRemoved");

                                    if (plugin.getSettingsManager().isLogBypassDelete())
                                    {
                                        PreciousStones.log("protectionRemovedFrom", count);
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
                                        ChatBlock.send(sender, "deletedFields", fields, Helper.getMaterialString(type.getTypeId()));
                                    }

                                    if (ubs > 0)
                                    {
                                        ChatBlock.send(sender, "deletedUnbreakables", ubs, Helper.getMaterialString(type.getTypeId()));
                                    }

                                    if (ubs == 0 && fields == 0)
                                    {
                                        ChatBlock.send(sender, "noPstonesFound");
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
                                    ChatBlock.send(sender, "deletedCountFields", args[0], fields);
                                }

                                if (ubs > 0)
                                {
                                    ChatBlock.send(sender, "deletedCountUnbreakables", args[0], ubs);
                                }

                                if (ubs == 0 && fields == 0)
                                {
                                    ChatBlock.send(sender, "playerHasNoPstones");
                                }
                            }
                            return true;
                        }
                        else if (args.length == 2)
                        {
                            if (Helper.isTypeEntry(args[1]))
                            {
                                String name = args[0];
                                BlockTypeEntry type = Helper.toTypeEntry(args[1]);

                                if (type != null)
                                {
                                    int fields = plugin.getForceFieldManager().deletePlayerFieldsOfType(name, type);

                                    if (fields > 0)
                                    {
                                        ChatBlock.send(sender, "deletedFields", fields, Helper.getMaterialString(type.getTypeId()));
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "noPstonesFound");
                                    }
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showNotFound(sender);
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "notValidBlockId", args[1]);
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandBlacklisting")) && hasplayer)
                    {
                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (args.length == 0 || args.length > 1 || args[0].contains("/"))
                        {
                            ChatBlock.send(sender, "commandBlacklistUsage");
                            return true;
                        }

                        String blacklistedCommand = args[0];

                        if (field != null)
                        {
                            if (field.hasFlag(FieldFlag.COMMAND_BLACKLISTING))
                            {
                                if (blacklistedCommand.equalsIgnoreCase("clear"))
                                {
                                    field.clearBlacklistedCommands();
                                    ChatBlock.send(sender, "commandBlacklistCleared");
                                }
                                else
                                {
                                    field.addBlacklistedCommand(blacklistedCommand);
                                    ChatBlock.send(sender, "commandBlacklistAdded");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "noBlacklistingFieldFound");
                            }
                        }
                        else
                        {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandSetLimit")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setlimit") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                String period = args[0];

                                if (!SignHelper.isValidPeriod(period))
                                {
                                    ChatBlock.send(sender, "limitMalformed");
                                    ChatBlock.send(sender, "limitMalformed2");
                                    ChatBlock.send(sender, "limitMalformed3");
                                    return true;
                                }

                                if (!field.hasFlag(FieldFlag.RENTABLE) && !field.hasFlag(FieldFlag.SHAREABLE))
                                {
                                    ChatBlock.send(sender, "limitBadField");
                                    return true;
                                }

                                field.setLimitSeconds(SignHelper.periodToSeconds(period));
                                ChatBlock.send(sender, "limitSet");
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandSetowner")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.setowner") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String owner = args[0];

                            if (owner.contains(":"))
                            {
                                ChatBlock.send(sender, "cannotAssignAsOwners");
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

                                    plugin.getStorageManager().changeTranslocationOwner(field, owner);
                                    plugin.getStorageManager().offerPlayer(field.getOwner());
                                    plugin.getStorageManager().offerPlayer(owner);

                                    // change the owner

                                    field.setOwner(owner);
                                    plugin.getStorageManager().offerField(field);
                                    ChatBlock.send(sender, "ownerSetTo", owner);
                                    return true;
                                }
                            }

                            ChatBlock.send(sender, "notPointingAtPstone");
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandChangeowner")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.change-owner") && hasplayer)
                    {
                        if (args.length == 1)
                        {
                            String owner = args[0];

                            if (owner.contains(":"))
                            {
                                ChatBlock.send(sender, "cannotAssignAsOwners");
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
                                            if (field.isBought())
                                            {
                                                ChatBlock.send(player, "fieldSignCannotChange");
                                                return true;
                                            }

                                            plugin.getForceFieldManager().changeOwner(field, owner);
                                            ChatBlock.send(sender, "fieldCanBeTaken", owner);
                                            return true;
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "fieldCannotChangeOwner");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "ownerCanOnlyChangeOwner");
                                    }
                                }
                            }

                            ChatBlock.send(sender, "notPointingAtPstone");
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandList")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.list") && hasplayer)
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
                                    ChatBlock.send(sender, "noPstonesFound");
                                }
                                return true;
                            }
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandReload")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.reload"))
                    {
                        plugin.getSettingsManager().load();
                        plugin.getLanguageManager().load();

                        ChatBlock.send(sender, "configReloaded");
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandBuy")))
                    {
                        if (plugin.getSettingsManager().isCommandsToRentBuy())
                        {
                            if (args.length == 0)
                            {
                                Field field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.BUYABLE);

                                if (field != null)
                                {
                                    FieldSign s = field.getAttachedFieldSign();

                                    if (s.isBuyable())
                                    {
                                        if (field.hasPendingPurchase())
                                        {
                                            ChatBlock.send(player, "fieldSignAlreadyBought");
                                        }
                                        else if (field.buy(player, s))
                                        {
                                            s.setBoughtColor(player);

                                            PreciousStones.getInstance().getForceFieldManager().addAllowed(field, player.getName());

                                            ChatBlock.send(player, "fieldSignBoughtAndAllowed");
                                        }

                                        return true;
                                    }
                                }
                            }
                            return true;
                        }
                    }
                    else if (cmd.equals(ChatBlock.format("commandRent")))
                    {
                        if (plugin.getSettingsManager().isCommandsToRentBuy())
                        {
                            if (args.length == 0)
                            {
                                Field field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.SHAREABLE);

                                if (field == null)
                                {
                                    field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.RENTABLE);
                                }

                                if (field != null)
                                {
                                    FieldSign s = field.getAttachedFieldSign();

                                    if (s.isRentable())
                                    {
                                        if (field.isRented())
                                        {
                                            if (!field.isRenter(player.getName()))
                                            {
                                                ChatBlock.send(player, "fieldSignAlreadyRented");
                                                plugin.getCommunicationManager().showRenterInfo(player, field);
                                                return true;
                                            }
                                            else
                                            {
                                                if (player.isSneaking())
                                                {
                                                    field.abandonRent(player);
                                                    ChatBlock.send(player, "fieldSignRentAbandoned");
                                                    return true;
                                                }
                                            }
                                        }
                                    }

                                    if (field.rent(player, s))
                                    {
                                        if (s.isRentable())
                                        {
                                            s.setRentedColor();
                                        }
                                        else if (s.isShareable())
                                        {
                                            s.setSharedColor();
                                        }

                                        return true;
                                    }
                                }
                            }

                            return true;
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.rent"))
                        {
                            if (args.length > 0)
                            {
                                String sub = args[0];

                                if (sub.equalsIgnoreCase(ChatBlock.format("commandRentClear")))
                                {
                                    Field field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.RENTABLE);

                                    if (field != null)
                                    {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.SHAREABLE);
                                    }

                                    if (field != null)
                                    {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.BUYABLE);
                                    }

                                    if (field != null)
                                    {
                                        if (field.clearRents())
                                        {
                                            ChatBlock.send(sender, "rentsCleared");
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "rentsClearedNone");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "noPstonesFound");
                                    }
                                }
                                if (sub.equalsIgnoreCase(ChatBlock.format("commandRentRemove")))
                                {
                                    Field field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.RENTABLE);

                                    if (field != null)
                                    {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.SHAREABLE);
                                    }

                                    if (field != null)
                                    {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.BUYABLE);
                                    }

                                    if (field != null)
                                    {
                                        if (field.removeRents())
                                        {
                                            ChatBlock.send(sender, "rentsRemoved");
                                        }
                                        else
                                        {
                                            ChatBlock.send(sender, "rentsRemovedNone");
                                        }
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "noPstonesFound");
                                    }
                                }
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandEnableall")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.enableall"))
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            ChatBlock.send(player, "fieldsDown");

                            int count = plugin.getStorageManager().enableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.send(player, "noFieldsFoundWithFlag");
                            }
                            else
                            {
                                ChatBlock.send(player, "flagEnabledOn", count);
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandDisableall")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall"))
                    {
                        if (args.length == 1)
                        {
                            String flagStr = args[0];

                            ChatBlock.send(player, "fieldsDown");

                            int count = plugin.getStorageManager().disableAllFlags(flagStr);

                            if (count == 0)
                            {
                                ChatBlock.send(player, "noFieldsFoundWithFlag");
                            }
                            else
                            {
                                ChatBlock.send(player, "flagDisabledOn", count);
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandClean")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.clean"))
                    {
                        if (args.length == 1)
                        {
                            String worldName = args[0];

                            World world = Bukkit.getServer().getWorld(worldName);

                            if (world != null)
                            {
                                int cleanedFF = plugin.getForceFieldManager().cleanOrphans(world);
                                int cleanedU = plugin.getUnbreakableManager().cleanOrphans(world);

                                if (cleanedFF > 0)
                                {
                                    ChatBlock.send(sender, "cleanedOrphanedFields", cleanedFF);
                                }
                                if (cleanedU > 0)
                                {
                                    ChatBlock.send(sender, "cleanedOrphanedUnbreakables", cleanedU);
                                }
                                if (cleanedFF == 0 && cleanedU == 0)
                                {
                                    ChatBlock.send(sender, "noOrphansFound");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "worldNotFound");
                            }
                        }
                        else
                        {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleanedFF = 0;
                            int cleanedU = 0;

                            for (World world : worlds)
                            {
                                cleanedFF += plugin.getForceFieldManager().cleanOrphans(world);
                                cleanedU += plugin.getUnbreakableManager().cleanOrphans(world);
                            }
                            if (cleanedFF > 0)
                            {
                                ChatBlock.send(sender, "cleanedOrphanedFields", cleanedFF);
                            }
                            if (cleanedU > 0)
                            {
                                ChatBlock.send(sender, "cleanedOrphanedUnbreakables", cleanedU);
                            }
                            if (cleanedFF == 0 && cleanedU == 0)
                            {
                                ChatBlock.send(sender, "noOrphansFound");
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandRevert")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.revert"))
                    {
                        if (args.length == 1)
                        {
                            String worldName = args[0];

                            World world = Bukkit.getServer().getWorld(worldName);

                            if (world != null)
                            {
                                int cleanedFF = plugin.getForceFieldManager().revertOrphans(world);
                                int cleanedU = plugin.getUnbreakableManager().revertOrphans(world);

                                if (cleanedFF > 0)
                                {
                                    ChatBlock.send(sender, "revertedOrphanFields", cleanedFF);
                                }
                                if (cleanedU > 0)
                                {
                                    ChatBlock.send(sender, "revertedOrphanUnbreakables", cleanedU);
                                }

                                if (cleanedFF == 0 && cleanedU == 0)
                                {
                                    ChatBlock.send(sender, "noOrphansFound");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "worldNotFound");
                            }
                        }
                        else
                        {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleanedFF = 0;
                            int cleanedU = 0;

                            for (World world : worlds)
                            {
                                cleanedFF += plugin.getForceFieldManager().revertOrphans(world);
                                cleanedU += plugin.getUnbreakableManager().revertOrphans(world);
                            }

                            if (cleanedFF > 0)
                            {
                                ChatBlock.send(sender, "revertedOrphanFields", cleanedFF);
                            }
                            if (cleanedU > 0)
                            {
                                ChatBlock.send(sender, "revertedOrphanUnbreakables", cleanedU);
                            }

                            if (cleanedFF == 0 && cleanedU == 0)
                            {
                                ChatBlock.send(sender, "noOrphansFound");
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandPull")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.pull"))
                    {
                        plugin.getStorageManager().loadWorldData();
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandBypass")) && plugin.getPermissionsManager().has(player, "preciousstones.bypass.toggle"))
                    {
                        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

                        if (args.length == 1)
                        {
                            String mode = args[0];

                            if (mode.equals(ChatBlock.format("commandOn")))
                            {
                                entry.setBypassDisabled(false);
                                ChatBlock.send(player, "bypassEnabled");
                            }
                            else if (mode.equals(ChatBlock.format("commandOff")))
                            {
                                entry.setBypassDisabled(true);
                                ChatBlock.send(player, "bypassDisabled");
                            }
                        }
                        else
                        {
                            if (entry.isBypassDisabled())
                            {
                                entry.setBypassDisabled(false);
                                ChatBlock.send(player, "bypassEnabled");
                            }
                            else
                            {
                                entry.setBypassDisabled(true);
                                ChatBlock.send(player, "bypassDisabled");
                            }
                        }

                        plugin.getStorageManager().offerPlayer(player.getName());
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandHide")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.hide"))
                    {
                        if (args.length == 1)
                        {
                            if (args[0].equals(ChatBlock.format("commandAll")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.hideall"))
                            {
                                int count = plugin.getForceFieldManager().hideBelonging(player.getName());

                                if (count > 0)
                                {
                                    ChatBlock.send(sender, "hideHideAll", count);
                                }
                            }
                        }
                        else
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.HIDABLE))
                                {
                                    if (!field.isHidden())
                                    {
                                        if (!field.matchesBlockType())
                                        {
                                            ChatBlock.send(sender, "cannotHideOrphan");
                                            return true;
                                        }

                                        field.hide();
                                        ChatBlock.send(sender, "hideHide");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "hideHiddenAlready");
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "hideCannot");
                                }
                            }
                            else
                            {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                        }
                        return true;
                    }
                    else if (cmd.equals(ChatBlock.format("commandUnhide")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.hide"))
                    {
                        if (args.length == 1)
                        {
                            if (args[0].equals(ChatBlock.format("commandAll")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.hideall"))
                            {
                                int count = plugin.getForceFieldManager().unhideBelonging(player.getName());

                                if (count > 0)
                                {
                                    ChatBlock.send(sender, "hideUnhideAll", count);
                                }
                            }
                        }
                        else
                        {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null)
                            {
                                if (field.hasFlag(FieldFlag.HIDABLE))
                                {
                                    if (field.isHidden())
                                    {
                                        field.unHide();
                                        ChatBlock.send(sender, "hideUnhide");
                                    }
                                    else
                                    {
                                        ChatBlock.send(sender, "hideUnHiddenAlready");
                                    }
                                }
                                else
                                {
                                    ChatBlock.send(sender, "hideCannot");
                                }
                            }
                            else
                            {
                                ChatBlock.send(sender, "hideNoneFound");
                            }
                        }
                        return true;
                    }

                    ChatBlock.send(sender, "notValidCommand");
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
