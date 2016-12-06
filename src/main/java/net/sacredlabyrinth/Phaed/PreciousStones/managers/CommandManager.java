package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author phaed
 */
public final class CommandManager implements CommandExecutor {
    private PreciousStones plugin;

    /**
     *
     */
    public CommandManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equals("ps")) {
                Player player = null;

                if (sender instanceof Player) {
                    player = (Player) sender;
                }

                boolean hasplayer = player != null;

                if (hasplayer) {
                    if (plugin.getSettingsManager().isBlacklistedWorld(player.getWorld())) {
                        ChatHelper.send(player, "psDisabled");
                        return true;
                    }
                }

                if (args.length > 0) {
                    String cmd = args[0];
                    args = Helper.removeFirst(args);

                    Block block = hasplayer ? player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()) : null;

                    if (cmd.equals(ChatHelper.format("commandDebug")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.debug")) {
                        plugin.getSettingsManager().setDebug(!plugin.getSettingsManager().isDebug());

                        if (plugin.getSettingsManager().isDebug()) {
                            ChatHelper.send(sender, "debugEnabled");
                        } else {
                            ChatHelper.send(sender, "debugDisabled");
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandFields")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.fields")) {
                        plugin.getCommunicationManager().showConfiguredFields(sender);
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandOn")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer) {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player).isDisabled();
                        if (isDisabled) {
                            plugin.getPlayerManager().getPlayerEntry(player).setDisabled(false);
                            ChatHelper.send(sender, "placingEnabled");
                        } else {
                            ChatHelper.send(sender, "placingAlreadyEnabled");
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandOff")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasplayer) {
                        boolean isDisabled = hasplayer && plugin.getPlayerManager().getPlayerEntry(player).isDisabled();
                        if (!isDisabled) {
                            plugin.getPlayerManager().getPlayerEntry(player).setDisabled(true);
                            ChatHelper.send(sender, "placingDisabled");
                        } else {
                            ChatHelper.send(sender, "placingAlreadyDisabled");
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandAllow")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasplayer) {
                        if (args.length >= 1) {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-allowing")) {
                                    if (field.hasFlag(FieldFlag.NO_ALLOWING)) {
                                        ChatHelper.send(sender, "noSharing");
                                        return true;
                                    }
                                }

                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                                    if (!field.isDisabled()) {
                                        ChatHelper.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                if (field.isGuest(player.getName())) {
                                    ChatHelper.send(sender, "cannotAllowAsGuest");
                                    return true;
                                }

                                boolean isGuest = false;
                                for (String playerName : args) {
                                    if (playerName.contains("-g")) {
                                        isGuest = true;
                                        continue;
                                    }

                                    Player allowed = Bukkit.getServer().getPlayerExact(playerName);

                                    // only those with permission can be allowed

                                    if (!field.getSettings().getRequiredPermissionAllow().isEmpty()) {
                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.required-permission")) {
                                            if (!plugin.getPermissionsManager().has(allowed, field.getSettings().getRequiredPermissionAllow())) {
                                                ChatHelper.send(sender, "noPermsForAllow", playerName);
                                                continue;
                                            }
                                        }
                                    }

                                    boolean done = plugin.getForceFieldManager().addAllowed(field, playerName, isGuest);

                                    if (done) {
                                        plugin.getEntryManager().reevaluateEnteredFields(allowed);

                                        ChatHelper.send(sender, "hasBeenAllowed", playerName);
                                    } else {
                                        ChatHelper.send(sender, "alreadyAllowed", playerName);
                                    }
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandAllowall")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasplayer) {
                        if (args.length >= 1) {
                            boolean isGuest = false;
                            for (String playerName : args) {
                                if (playerName.contains("-g")) {
                                    isGuest = true;
                                    continue;
                                }
                                int count = plugin.getForceFieldManager().allowAll(player, playerName, isGuest);

                                if (count > 0) {
                                    plugin.getEntryManager().reevaluateEnteredFields(Bukkit.getServer().getPlayerExact(playerName));

                                    ChatHelper.send(sender, "hasBeenAllowedIn", playerName, count);
                                } else {
                                    ChatHelper.send(sender, "isAlreadyAllowedOnAll", playerName);
                                }
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandRemove")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasplayer) {
                        if (args.length >= 1) {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                                    if (!field.isDisabled()) {
                                        ChatHelper.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                for (String playerName : args) {

                                    if (plugin.getSettingsManager().isPreventRemovalIfPlayerInField()) {
                                        if (field.containsPlayer(playerName)) {
                                            ChatHelper.send(sender, "cannotRemovePlayerInField");
                                            return true;
                                        }
                                    }

                                    /*
                                    int conflicted = plugin.getForceFieldManager().removeConflictingFields(field, playerName);

                                    if (conflicted > 0) {
                                        ChatHelper.send(sender, "removedConflictingFields", conflicted, playerName);
                                        return true;
                                    }*/

                                    boolean done = plugin.getForceFieldManager().removeAllowed(field, playerName);

                                    if (done) {
                                        plugin.getEntryManager().reevaluateEnteredFields(Bukkit.getServer().getPlayerExact(playerName));

                                        ChatHelper.send(sender, "removedFromField", playerName);
                                    } else {
                                        ChatHelper.send(sender, "playerNotFound", playerName);
                                    }
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandRemoveall")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasplayer) {
                        if (args.length >= 1) {
                            for (String playerName : args) {
                                int count = plugin.getForceFieldManager().removeAll(player, playerName);

                                if (count > 0) {
                                    plugin.getEntryManager().reevaluateEnteredFields(Bukkit.getServer().getPlayerExact(playerName));

                                    ChatHelper.send(sender, "removedFromFields", playerName, count);
                                } else {
                                    ChatHelper.send(sender, "nothingToBeDone");
                                }
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandAllowed")) && plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            List<String> allowed = field.getAllAllowed();

                            if (!allowed.isEmpty()) {
                                ChatHelper.send(sender, "allowedList", Helper.toMessage(new ArrayList<String>(allowed), ", "));
                            } else {
                                ChatHelper.send(sender, "noPlayersAllowedOnField");
                            }
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandCuboid")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.openclose.forcefield") && hasplayer) {
                        if (args.length >= 1) {
                            if ((args[0]).equals(ChatHelper.format("commandCuboidOpen"))) {
                                Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                                if (field != null) {
                                    if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                        ChatHelper.send(player, "noResize");
                                        return true;
                                    }

                                    if (field.isRented()) {
                                        ChatHelper.send(player, "fieldSignCannotChange");
                                        return true;
                                    }

                                    plugin.getCuboidManager().openCuboid(player, field);
                                } else {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                            } else if ((args[0]).equals(ChatHelper.format("commandCuboidClose"))) {
                                plugin.getCuboidManager().closeCuboid(player);
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandWho")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            HashSet<String> inhabitants = plugin.getForceFieldManager().getWho(player, field);

                            if (!inhabitants.isEmpty()) {
                                ChatHelper.send(sender, "inhabitantsList", Helper.toMessage(new ArrayList<String>(inhabitants), ", "));
                            } else {
                                ChatHelper.send(sender, "noPlayersFoundOnField");
                            }
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandSetname")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasplayer) {
                        String fieldName = null;

                        if (args.length >= 1) {
                            fieldName = ChatColor.translateAlternateColorCodes('&', Helper.toMessage(args));
                        }

                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            if (field.isRented()) {
                                ChatHelper.send(player, "fieldSignCannotChange");
                                return true;
                            }

                            if (field.hasFlag(FieldFlag.TRANSLOCATION)) {
                                // if switching from an existing translocation
                                // end the previous one correctly by making sure
                                // to wipe out all applied blocks from the database

                                if (field.isNamed()) {
                                    int count = plugin.getStorageManager().appliedTranslocationCount(field);

                                    if (count > 0) {
                                        plugin.getStorageManager().deleteAppliedTranslocation(field);

                                        if (!plugin.getStorageManager().existsTranslocationDataWithName(field.getName(), field.getOwner())) {
                                            plugin.getStorageManager().deleteTranslocationHead(field.getName(), field.getOwner());
                                        }

                                        ChatHelper.send(player, "translocationUnlinked", field.getName(), count);
                                    }
                                }

                                // check if one exists with that name already

                                if (plugin.getStorageManager().existsFieldWithName(fieldName, player.getName())) {
                                    ChatHelper.send(sender, "translocationExists");
                                    return true;
                                }


                                // if this is a new translocation name, create its head record
                                // this will cement the size of the cuboid

                                if (!plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner())) {
                                    plugin.getStorageManager().insertTranslocationHead(field, fieldName);
                                }

                                // updates the size of the field

                                plugin.getStorageManager().changeSizeTranslocatiorField(field, fieldName);

                                // always start off in applied (recording) mode

                                if (plugin.getStorageManager().existsTranslocationDataWithName(fieldName, field.getOwner())) {
                                    field.setDisabled(true, player);
                                    field.getFlagsModule().dirtyFlags("commandSetname1");
                                } else {
                                    boolean disabled = field.setDisabled(false, player);

                                    if (!disabled) {
                                        ChatHelper.send(player, "cannotEnable");
                                        return true;
                                    }
                                    field.getFlagsModule().dirtyFlags("commandSetname2");
                                }
                            }

                            if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                                if (!field.isDisabled()) {
                                    ChatHelper.send(sender, "onlyModWhileDisabled");
                                    return true;
                                }
                            }

                            if (fieldName == null) {
                                boolean done = plugin.getForceFieldManager().setNameField(field, "");

                                if (done) {
                                    ChatHelper.send(sender, "fieldNameCleared");
                                } else {
                                    ChatHelper.send(sender, "noNameableFieldFound");
                                }
                            } else {
                                boolean done = plugin.getForceFieldManager().setNameField(field, fieldName);

                                if (done) {
                                    if (field.hasFlag(FieldFlag.TRANSLOCATION)) {
                                        int count = plugin.getStorageManager().unappliedTranslocationCount(field);

                                        if (count > 0) {
                                            ChatHelper.send(sender, "translocationHasBlocks", fieldName, count);
                                        } else {
                                            ChatHelper.send(sender, "translocationCreated", fieldName);
                                        }
                                    } else {
                                        ChatHelper.send(sender, "translocationRenamed", fieldName);
                                    }
                                } else {
                                    ChatHelper.send(sender, "noNameableFieldFound");
                                }
                            }
                            return true;
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandSetradius")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasplayer) {
                        if (args.length == 1 && Helper.isInteger(args[0])) {
                            int radius = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                FieldSettings fs = field.getSettings();

                                if (field.hasFlag(FieldFlag.TRANSLOCATION)) {
                                    if (field.isNamed()) {
                                        if (plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner())) {
                                            ChatHelper.send(player, "translocationCannotReshape");
                                            return true;
                                        }
                                    }
                                }

                                if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                    ChatHelper.send(player, "noResize");
                                    return true;
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                if (radius < 0) {
                                    return false;
                                }

                                if (field.hasFlag(FieldFlag.CUBOID)) {
                                    int overflow = field.canSetCuboidRadius(radius);

                                    if (overflow > 0 && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.setradius")) {
                                        ChatHelper.send(sender, "radiusOverFlow", overflow);
                                        return true;
                                    }
                                } else {
                                    if (radius > fs.getRadius() && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.setradius")) {
                                        ChatHelper.send(sender, "radiusMustBeLessThan", fs.getRadius());
                                        return true;
                                    }
                                }

                                plugin.getForceFieldManager().removeSourceField(field);

                                field.setRadius(radius);

                                plugin.getStorageManager().offerField(field);

                                plugin.getForceFieldManager().addSourceField(field);

                                ChatHelper.send(sender, "radiusSet", radius);

                                return true;
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandTake")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.take") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            boolean taken = field.take(player);

                            if (taken) {
                                ChatHelper.send(sender, "taken", field.getType(), field.getCoords());
                            }
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;

                    } else if (cmd.equals(ChatHelper.format("commandExpand")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.expand") && hasplayer) {
                        if (args.length == 1 || args.length == 2) {
                            int num = 0;
                            String dir = "";

                            if (args.length == 1) {
                                num = Integer.parseInt(args[0]);
                                dir = "all";
                            } else {
                                if (Helper.isInteger(args[0])) {
                                    num = Integer.parseInt(args[0]);
                                    dir = args[1];
                                } else if (Helper.isInteger(args[1])) {
                                    num = Integer.parseInt(args[1]);
                                    dir = args[0];
                                } else {
                                    return true;
                                }
                            }

                            if (num < 0) {
                                ChatHelper.send(sender, "noNegative");
                                return true;
                            }

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                    ChatHelper.send(player, "noResize");
                                    return true;
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                boolean bypass = plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid");
                                int overflow = field.expand(num, dir, bypass);

                                if (overflow <= 0 || bypass) {
                                    ChatHelper.send(sender, "cuboidExpanded");

                                    if (plugin.getSettingsManager().isVisualizeOnExpand()) {
                                        plugin.getVisualizationManager().revert(player);
                                        plugin.getVisualizationManager().addVisualizationField(player, field);
                                        plugin.getVisualizationManager().displayVisualization(player, true);
                                    }
                                } else {
                                    ChatHelper.send(sender, "cannotExpand", overflow);
                                }

                                return true;
                            } else {
                                ChatHelper.send(sender, "noCuboidsFound");
                            }
                            return true;
                        }

                        if (args.length == 6) {
                            int u = Integer.parseInt(args[0]);
                            int d = Integer.parseInt(args[1]);
                            int n = Integer.parseInt(args[2]);
                            int s = Integer.parseInt(args[3]);
                            int e = Integer.parseInt(args[4]);
                            int w = Integer.parseInt(args[5]);

                            if (u < 0 || d < 0 || n < 0 || s < 0 || e < 0 || w < 0) {
                                ChatHelper.send(sender, "noNegative");
                                return true;
                            }

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                    ChatHelper.send(player, "noResize");
                                    return true;
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                boolean bypass = plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid");
                                int overflow = field.expand(u, d, n, s, e, w, bypass);

                                if (overflow <= 0 || bypass) {
                                    ChatHelper.send(sender, "cuboidExpanded");
                                } else {
                                    ChatHelper.send(sender, "cannotExpand", overflow);
                                }

                                return true;
                            } else {
                                ChatHelper.send(sender, "noCuboidsFound");
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandContract")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.contract") && hasplayer) {
                        if (args.length == 1 || args.length == 2) {
                            int num = 0;
                            String dir = "";

                            if (args.length == 1) {
                                num = Integer.parseInt(args[0]);
                                dir = "all";
                            } else {
                                if (Helper.isInteger(args[0])) {
                                    num = Integer.parseInt(args[0]);
                                    dir = args[1];
                                } else if (Helper.isInteger(args[1])) {
                                    num = Integer.parseInt(args[1]);
                                    dir = args[0];
                                } else {
                                    return true;
                                }
                            }

                            if (num < 0) {
                                ChatHelper.send(sender, "noNegative");
                                return true;
                            }

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                    ChatHelper.send(player, "noResize");
                                    return true;
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                field.contract(num, dir);
                                ChatHelper.send(sender, "cuboidContracted");
                                return true;
                            } else {
                                ChatHelper.send(sender, "noCuboidsFound");
                            }
                            return true;
                        }

                        if (args.length == 6) {
                            int u = Integer.parseInt(args[0]);
                            int d = Integer.parseInt(args[1]);
                            int n = Integer.parseInt(args[2]);
                            int s = Integer.parseInt(args[3]);
                            int e = Integer.parseInt(args[4]);
                            int w = Integer.parseInt(args[5]);

                            if (u < 0 || d < 0 || n < 0 || s < 0 || e < 0 || w < 0) {
                                ChatHelper.send(sender, "noNegative");
                                return true;
                            }

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.CUBOID);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.NO_RESIZE) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-resize")) {
                                    ChatHelper.send(player, "noResize");
                                    return true;
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                field.contract(u, d, n, s, e, w);
                                ChatHelper.send(sender, "cuboidContracted");

                                if (plugin.getSettingsManager().isVisualizeOnExpand()) {
                                    plugin.getVisualizationManager().revert(player);
                                    plugin.getVisualizationManager().addVisualizationField(player, field);
                                    plugin.getVisualizationManager().displayVisualization(player, true);
                                }

                                return true;
                            } else {
                                ChatHelper.send(sender, "noCuboidsFound");
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandSetvelocity")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasplayer) {
                        if (args.length == 1 && Helper.isFloat(args[0])) {
                            float velocity = Float.parseFloat(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                                    if (!field.isDisabled()) {
                                        ChatHelper.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                FieldSettings fs = field.getSettings();

                                if (fs.hasVeocityFlag()) {
                                    if (velocity < 0 || velocity > 5) {
                                        ChatHelper.send(sender, "velocityMustBe");
                                        return true;
                                    }

                                    field.setVelocity(velocity);
                                    plugin.getStorageManager().offerField(field);
                                    ChatHelper.send(sender, "velocitySetTo", velocity);
                                } else {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                                return true;
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandDisable")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.disable") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            if (!field.isDisabled()) {
                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotDisable");
                                    return true;
                                }

                                field.setDisabled(true, player);
                                field.getFlagsModule().dirtyFlags("commandDisable");
                                ChatHelper.send(sender, "fieldDisabled");

                                plugin.getEntryManager().actOnInhabitantsOnDisableToggle(field);
                            } else {
                                ChatHelper.send(sender, "fieldAlreadyDisabled");
                            }
                            return true;
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandEnable")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.enable") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            if (field.isDisabled()) {
                                boolean disabled = field.setDisabled(false, player);

                                if (!disabled) {
                                    ChatHelper.send(sender, "cannotEnable");
                                    return true;
                                }

                                field.getFlagsModule().dirtyFlags("commandEnable");
                                ChatHelper.send(sender, "fieldEnabled");

                                plugin.getEntryManager().actOnInhabitantsOnDisableToggle(field);
                            } else {
                                ChatHelper.send(sender, "fieldAlreadyEnabled");
                            }
                            return true;
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandDensity")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasplayer) {
                        if (args.length == 1 && Helper.isInteger(args[0])) {
                            int density = Integer.parseInt(args[0]);

                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player);
                            data.setDensity(density);
                            plugin.getStorageManager().offerPlayer(player.getName());

                            ChatHelper.send(sender, "visualizationChanged", density);
                            return true;
                        } else if (args.length == 0) {
                            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player);
                            ChatHelper.send(sender, "visualizationSet", data.getDensity());
                        }
                    } else if (cmd.equals(ChatHelper.format("commandToggle")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasplayer) {
                        if (args.length == 1) {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled")) {
                                    if (field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED)) {
                                        if (!field.isDisabled()) {
                                            ChatHelper.send(sender, "flagsToggledWhileDisabled");
                                            return true;
                                        }
                                    }
                                }

                                if (field.isRented()) {
                                    ChatHelper.send(player, "fieldSignCannotChange");
                                    return true;
                                }

                                if (field.hasFlag(flagStr) || field.getFlagsModule().hasDisabledFlag(flagStr)) {
                                    boolean unToggable = false;

                                    if (field.hasFlag(FieldFlag.DYNMAP_NO_TOGGLE)) {
                                        if (flagStr.equalsIgnoreCase("dynmap-area")) {
                                            unToggable = true;
                                        }

                                        if (flagStr.equalsIgnoreCase("dynmap-marker")) {
                                            unToggable = true;
                                        }
                                    }

                                    try {
                                        if (FieldFlag.getByString(flagStr).isUnToggable()) {
                                            unToggable = true;
                                        }
                                    } catch (Exception ex) {
                                        ChatHelper.send(sender, "flagNotFound");
                                        return true;
                                    }

                                    if (unToggable) {
                                        ChatHelper.send(sender, "flagCannottoggle");
                                        return true;
                                    }

                                    boolean enabled = field.getFlagsModule().toggleFieldFlag(flagStr);

                                    if (enabled) {
                                        ChatHelper.send(sender, "flagEnabled", flagStr);
                                    } else {
                                        ChatHelper.send(sender, "flagDisabled", flagStr);
                                    }

                                    plugin.getStorageManager().offerField(field);
                                } else {
                                    ChatHelper.send(sender, "flagNotFound");
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    } else if ((cmd.equals(ChatHelper.format("commandVisualize")) || cmd.equals("visualise")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasplayer) {
                        if (!plugin.getCuboidManager().hasOpenCuboid(player)) {
                            if (!plugin.getVisualizationManager().pendingVisualization(player)) {
                                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize")) {
                                    if (args.length == 1 && Helper.isInteger(args[0])) {
                                        int radius = Math.min(Integer.parseInt(args[0]), plugin.getServer().getViewDistance());

                                        Set<Field> fieldsInArea;

                                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize")) {
                                            fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), radius / 16, FieldFlag.ALL);
                                        } else {
                                            fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), radius / 16, FieldFlag.ALL, player);
                                        }

                                        if (fieldsInArea != null && !fieldsInArea.isEmpty()) {
                                            ChatHelper.send(sender, "visualizing");

                                            int count = 0;
                                            for (Field f : fieldsInArea) {
                                                if (count++ >= plugin.getSettingsManager().getVisualizeMaxFields()) {
                                                    continue;
                                                }

                                                plugin.getVisualizationManager().addVisualizationField(player, f);
                                            }

                                            plugin.getVisualizationManager().displayVisualization(player, true);
                                            return true;
                                        } else {
                                            ChatHelper.send(sender, "noFieldsInArea");
                                        }
                                    } else {
                                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                                        if (field != null) {
                                            ChatHelper.send(sender, "visualizing");

                                            plugin.getVisualizationManager().addVisualizationField(player, field);
                                            plugin.getVisualizationManager().displayVisualization(player, true);
                                        } else {
                                            ChatHelper.send(sender, "notInsideField");
                                        }
                                    }
                                }
                            } else {
                                ChatHelper.send(sender, "visualizationTakingPlace");
                            }
                        } else {
                            ChatHelper.send(sender, "visualizationNotWhileCuboid");
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandMark")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && hasplayer) {
                        if (!plugin.getCuboidManager().hasOpenCuboid(player)) {
                            if (!plugin.getVisualizationManager().pendingVisualization(player)) {
                                if (plugin.getPermissionsManager().has(player, "preciousstones.admin.mark")) {
                                    Set<Field> fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), plugin.getServer().getViewDistance(), FieldFlag.ALL);

                                    if (!fieldsInArea.isEmpty()) {
                                        ChatHelper.send(sender, "markingFields", fieldsInArea.size());

                                        for (Field f : fieldsInArea) {
                                            plugin.getVisualizationManager().addFieldMark(player, f);
                                        }

                                        plugin.getVisualizationManager().displayVisualization(player, false);
                                    } else {
                                        ChatHelper.send(sender, "noFieldsInArea");
                                    }
                                } else {
                                    Set<Field> fieldsInArea = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), plugin.getServer().getViewDistance(), FieldFlag.ALL);

                                    if (!fieldsInArea.isEmpty()) {
                                        int count = 0;
                                        for (Field f : fieldsInArea) {
                                            if (plugin.getForceFieldManager().isAllowed(f, player.getName())) {
                                                count++;
                                                plugin.getVisualizationManager().addFieldMark(player, f);
                                            }
                                        }

                                        if (count > 0) {
                                            ChatHelper.send(sender, "markingFields", count);
                                            plugin.getVisualizationManager().displayVisualization(player, false);
                                        } else {
                                            ChatHelper.send(sender, "noFieldsInArea");
                                        }
                                    } else {
                                        ChatHelper.send(sender, "noFieldsInArea");
                                    }
                                }
                            } else {
                                ChatHelper.send(sender, "visualizationTakingPlace");
                            }
                        } else {
                            ChatHelper.send(sender, "markingNotWhileCuboid");
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandInsert")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer) {
                        if (args.length == 1) {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (!field.hasFlag(flagStr) && !field.getFlagsModule().hasDisabledFlag(flagStr)) {
                                    plugin.getForceFieldManager().removeSourceField(field);

                                    if (field.getFlagsModule().insertFieldFlag(flagStr)) {
                                        field.getFlagsModule().dirtyFlags("commandInsert");
                                        plugin.getStorageManager().offerField(field);
                                        ChatHelper.send(sender, "flagInserted");
                                    } else {
                                        ChatHelper.send(sender, "flagNotExists");
                                    }
                                    plugin.getForceFieldManager().addSourceField(field);
                                } else {
                                    ChatHelper.send(sender, "flagExists");
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandClear")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasplayer) {
                        if (args.length == 1) {
                            String flagStr = args[0];

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (field.hasFlag(flagStr) || field.getFlagsModule().hasDisabledFlag(flagStr)) {
                                    plugin.getForceFieldManager().removeSourceField(field);

                                    if (field.getFlagsModule().clearFieldFlag(flagStr)) {
                                        field.getFlagsModule().dirtyFlags("commandClear");
                                        plugin.getStorageManager().offerField(field);
                                        ChatHelper.send(sender, "flagCleared");
                                    } else {
                                        ChatHelper.send(sender, "flagNotExists");
                                    }
                                    plugin.getForceFieldManager().addSourceField(field);
                                } else {
                                    ChatHelper.send(sender, "flagExists");
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandReset")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.reset") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);

                        if (field != null) {
                            field.getFlagsModule().RevertFlags();
                            plugin.getStorageManager().offerField(field);
                            ChatHelper.send(sender, "flagsReverted");
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandSetinterval")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasplayer) {
                        if (args.length == 1 && Helper.isInteger(args[0])) {
                            int interval = Integer.parseInt(args[0]);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.GRIEF_REVERT);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                                    if (!field.isDisabled()) {
                                        ChatHelper.send(sender, "onlyModWhileDisabled");
                                        return true;
                                    }
                                }

                                if (interval >= plugin.getSettingsManager().getGriefRevertMinInterval() || plugin.getPermissionsManager().has(player, "preciousstones.bypass.interval")) {
                                    field.getRevertingModule().setRevertSecs(interval);
                                    plugin.getGriefUndoManager().register(field);
                                    plugin.getStorageManager().offerField(field);
                                    ChatHelper.send(sender, "griefRevertIntervalSet", interval);
                                } else {
                                    ChatHelper.send(sender, "minInterval", plugin.getSettingsManager().getGriefRevertMinInterval());
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtGriefRevert");
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandSnitch")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasplayer) {
                        if (args.length == 0) {
                            Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.SNITCH);

                            if (field != null) {
                                plugin.getCommunicationManager().showSnitchList(player, field);
                            } else {
                                ChatHelper.send(sender, "notPointingAtSnitch");
                            }

                            return true;
                        } else if (args.length == 1) {
                            if (args[0].equals(ChatHelper.format("commandSnitchClear"))) {
                                Field field = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.SNITCH);

                                if (field != null) {
                                    boolean cleaned = plugin.getForceFieldManager().cleanSnitchList(field);

                                    if (cleaned) {
                                        ChatHelper.send(sender, "clearedSnitch");
                                    } else {
                                        ChatHelper.send(sender, "snitchEmpty");
                                    }
                                } else {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                                return true;
                            }
                        }
                    } else if (cmd.equals(ChatHelper.format("commandTranslocation")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.use") && hasplayer) {
                        if (args.length == 0) {
                            ChatHelper.send(sender, "translocationMenu1");
                            ChatHelper.send(sender, "translocationMenu2");
                            ChatHelper.send(sender, "translocationMenu3");
                            ChatHelper.send(sender, "translocationMenu4");
                            ChatHelper.send(sender, "translocationMenu5");
                            ChatHelper.send(sender, "translocationMenu6");
                            ChatHelper.send(sender, "translocationMenu7");
                            return true;
                        }

                        if (args[0].equals(ChatHelper.format("commandTranslocationList"))) {
                            plugin.getCommunicationManager().notifyStoredTranslocations(player);
                            return true;
                        }

                        if (args[0].equals(ChatHelper.format("commandTranslocationDelete")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.delete")) {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null) {
                                if (field.getTranslocatingModule().isTranslocating()) {
                                    ChatHelper.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed()) {
                                    ChatHelper.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (args.length == 0) {
                                    plugin.getStorageManager().deleteTranslocation(args[1], player.getName());
                                    ChatHelper.send(sender, "translocationDeleted", args[0]);
                                } else {
                                    for (String arg : args) {
                                        BlockTypeEntry entry = new BlockTypeEntry(arg);

                                        if (!entry.isValid()) {
                                            ChatHelper.send(sender, "notValidBlockId", arg);
                                            continue;
                                        }

                                        int count = plugin.getStorageManager().deleteBlockTypeFromTranslocation(field.getName(), player.getName(), entry);

                                        if (count > 0) {
                                            ChatHelper.send(sender, "translocationDeletedBlocks", count, Helper.friendlyBlockType(entry.getTypeId()), field.getName());
                                        } else {
                                            ChatHelper.send(sender, "noBlocksMatched", arg);
                                        }
                                    }
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatHelper.format("commandTranslocationRemove")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.remove")) {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null) {
                                if (field.getTranslocatingModule().isTranslocating()) {
                                    ChatHelper.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed()) {
                                    ChatHelper.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled()) {
                                    ChatHelper.send(sender, "translocationEnabledFirst");
                                    return true;
                                }

                                if (args.length > 0) {
                                    List<BlockTypeEntry> entries = new ArrayList<BlockTypeEntry>();

                                    for (String arg : args) {
                                        BlockTypeEntry entry = new BlockTypeEntry(arg);

                                        if (!entry.isValid()) {
                                            ChatHelper.send(sender, "notValidBlockId", arg);
                                            continue;
                                        }

                                        entries.add(entry);
                                    }

                                    if (!entries.isEmpty()) {
                                        plugin.getTranslocationManager().removeBlocks(field, player, entries);
                                    }
                                } else {
                                    ChatHelper.send(sender, "usageTranslocationRemove");
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatHelper.format("commandTranslocationUnlink")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink")) {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null) {
                                if (field.getTranslocatingModule().isTranslocating()) {
                                    ChatHelper.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed()) {
                                    ChatHelper.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled()) {
                                    ChatHelper.send(sender, "translocationEnabledToUnlink");
                                    return true;
                                }

                                int count = plugin.getStorageManager().appliedTranslocationCount(field);

                                if (count > 0) {
                                    plugin.getStorageManager().deleteAppliedTranslocation(field);

                                    if (!plugin.getStorageManager().existsTranslocationDataWithName(field.getName(), field.getOwner())) {
                                        plugin.getStorageManager().deleteTranslocationHead(field.getName(), field.getOwner());
                                    }

                                    ChatHelper.send(player, "translocationUnlinked", field.getName(), count);
                                } else {
                                    ChatHelper.send(sender, "translocationNothingToUnlink");
                                    return true;
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtTranslocation");
                            }
                            return true;
                        }

                        if (args[0].equals(ChatHelper.format("commandTranslocationImport")) && plugin.getPermissionsManager().has(player, "preciousstones.translocation.import")) {
                            args = Helper.removeFirst(args);

                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.TRANSLOCATION);

                            if (field != null) {
                                if (field.getTranslocatingModule().isTranslocating()) {
                                    ChatHelper.send(sender, "translocationTakingPlace");
                                    return true;
                                }

                                if (!field.isNamed()) {
                                    ChatHelper.send(sender, "translocationNamedFirst");
                                    return true;
                                }

                                if (field.isDisabled()) {
                                    ChatHelper.send(sender, "translocationEnabledToImport");
                                    return true;
                                }

                                if (args.length == 0) {
                                    plugin.getTranslocationManager().importBlocks(field, player, null);
                                } else {
                                    List<BlockTypeEntry> entries = new ArrayList<BlockTypeEntry>();

                                    for (String arg : args) {
                                        BlockTypeEntry entry = new BlockTypeEntry(arg);

                                        if (!entry.isValid()) {
                                            ChatHelper.send(sender, "notValidBlockId", arg);
                                            continue;
                                        }

                                        if (!field.getSettings().canTranslocate(entry)) {
                                            ChatHelper.send(sender, "blockIsBlacklisted", arg);
                                            continue;
                                        }

                                        entries.add(entry);
                                    }

                                    if (!entries.isEmpty()) {
                                        plugin.getTranslocationManager().importBlocks(field, player, entries);
                                    }
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtTranslocation");
                            }
                            return true;

                        }
                    } else if (cmd.equals(ChatHelper.format("commandMore")) && hasplayer) {
                        ChatHelper cb = plugin.getCommunicationManager().getChatBlock(player);

                        if (cb.size() > 0) {
                            ChatHelper.sendBlank(player);

                            cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

                            if (cb.size() > 0) {
                                ChatHelper.sendBlank(player);
                                ChatHelper.send(sender, "moreNextPage");
                            }
                            ChatHelper.sendBlank(player);

                            return true;
                        }

                        ChatHelper.send(sender, "moreNothingMore");
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandCounts"))) {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasplayer) {
                            if (!plugin.getCommunicationManager().showFieldCounts(player, player.getName())) {
                                ChatHelper.send(sender, "playerHasNoFields");
                            }
                            return true;
                        }

                        if (args.length == 1 && plugin.getPermissionsManager().has(player, "preciousstones.admin.counts")) {
                            if (Helper.isTypeEntry(args[0])) {
                                BlockTypeEntry entry = new BlockTypeEntry(args[0]);

                                if (!entry.isValid()) {
                                    if (!plugin.getCommunicationManager().showCounts(sender, entry)) {
                                        ChatHelper.send(sender, "notValidFieldType");
                                    }
                                }
                            } else if (Helper.isString(args[0]) && hasplayer) {
                                String target = args[0];

                                if (!plugin.getCommunicationManager().showFieldCounts(player, target)) {
                                    ChatHelper.send(sender, "playerHasNoFields");
                                }
                            }
                            return true;
                        }
                        return false;
                    } else if (cmd.equals(ChatHelper.format("commandLocations"))) {
                        if (args.length == 0 && plugin.getPermissionsManager().has(player, "preciousstones.benefit.locations") && hasplayer) {
                            plugin.getCommunicationManager().showFieldLocations(sender, -1, sender.getName());
                            return true;
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.locations")) {
                            if (args.length == 1 && Helper.isString(args[0])) {
                                String targetName = args[0];
                                plugin.getCommunicationManager().showFieldLocations(sender, -1, targetName);
                            }

                            if (args.length == 2 && Helper.isString(args[0]) && Helper.isInteger(args[1])) {
                                String targetName = args[0];
                                int type = Integer.parseInt(args[1]);
                                plugin.getCommunicationManager().showFieldLocations(sender, type, targetName);
                            }
                            return true;
                        }
                        return false;
                    } else if (cmd.equals(ChatHelper.format("commandInfo")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.info") && hasplayer) {
                        Field pointing = plugin.getForceFieldManager().getOneAllowedField(block, player, FieldFlag.ALL);
                        List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                        if (pointing != null && !fields.contains(pointing)) {
                            fields.add(pointing);
                        }

                        if (fields.isEmpty()) {
                            plugin.getCommunicationManager().showNotFound(player);
                            return true;
                        } else if (fields.size() == 1) {
                            Field field = fields.get(0);
                            block = field.getBlock();

                            if (plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().has(player, "preciousstones.admin.details")) {
                                plugin.getCommunicationManager().showFieldDetails(player, field);
                            } else {
                                plugin.getCommunicationManager().showFieldOwner(player, block);

                            }
                        } else {
                            Iterator<Field> iter = fields.iterator();
                            while (iter.hasNext()) {
                                Field f = iter.next();
                                block = f.getBlock();

                                if (!(plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().has(player, "preciousstones.admin.details"))) {
                                    iter.remove();
                                }
                            }

                            plugin.getCommunicationManager().showFieldDetails(player, fields);
                        }

                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandDelete")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.delete")) {
                        if (args.length == 0 && hasplayer) {
                            List<Field> sourceFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                            if (!sourceFields.isEmpty()) {
                                int count = plugin.getForceFieldManager().deleteFields(sourceFields);

                                if (count > 0) {
                                    ChatHelper.send(sender, "protectionRemoved");

                                    if (plugin.getSettingsManager().isLogBypassDelete()) {
                                        PreciousStones.log("protectionRemovedFrom", count);
                                    }
                                } else {
                                    plugin.getCommunicationManager().showNotFound(player);
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        } else if (args.length == 1) {
                            if (Helper.isTypeEntry(args[0])) {
                                BlockTypeEntry entry = new BlockTypeEntry(args[0]);

                                if (!entry.isValid()) {
                                    int fields = plugin.getForceFieldManager().deleteFieldsOfType(entry);
                                    int ubs = plugin.getUnbreakableManager().deleteUnbreakablesOfType(entry);

                                    if (fields > 0) {
                                        ChatHelper.send(sender, "deletedFields", fields, Helper.getMaterialString(entry.getTypeId()));
                                    }

                                    if (ubs > 0) {
                                        ChatHelper.send(sender, "deletedUnbreakables", ubs, Helper.getMaterialString(entry.getTypeId()));
                                    }

                                    if (ubs == 0 && fields == 0) {
                                        ChatHelper.send(sender, "noPstonesFound");
                                    }
                                } else {
                                    plugin.getCommunicationManager().showNotFound(sender);
                                }
                            } else {
                                int fields = plugin.getForceFieldManager().deleteBelonging(args[0]);
                                int ubs = plugin.getUnbreakableManager().deleteBelonging(args[0]);

                                if (fields > 0) {
                                    ChatHelper.send(sender, "deletedCountFields", args[0], fields);
                                }

                                if (ubs > 0) {
                                    ChatHelper.send(sender, "deletedCountUnbreakables", args[0], ubs);
                                }

                                if (ubs == 0 && fields == 0) {
                                    ChatHelper.send(sender, "playerHasNoPstones");
                                }
                            }
                            return true;
                        } else if (args.length == 2) {
                            if (Helper.isTypeEntry(args[1])) {
                                String name = args[0];
                                BlockTypeEntry entry = new BlockTypeEntry(args[1]);

                                if (!entry.isValid()) {
                                    int fields = plugin.getForceFieldManager().deletePlayerFieldsOfType(name, entry);

                                    if (fields > 0) {
                                        ChatHelper.send(sender, "deletedFields", fields, Helper.getMaterialString(entry.getTypeId()));
                                    } else {
                                        ChatHelper.send(sender, "noPstonesFound");
                                    }
                                } else {
                                    plugin.getCommunicationManager().showNotFound(sender);
                                }
                            } else {
                                ChatHelper.send(sender, "notValidBlockId", args[1]);
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandBlacklisting")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.blacklistcommand") && hasplayer) {
                        Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                        if (args.length == 0 || args.length > 1 || args[0].contains("/")) {
                            ChatHelper.send(sender, "commandBlacklistUsage");
                            return true;
                        }

                        String blacklistedCommand = args[0];

                        if (field != null) {
                            if (field.hasFlag(FieldFlag.COMMAND_BLACKLISTING)) {
                                if (blacklistedCommand.equalsIgnoreCase("clear")) {
                                    field.getListingModule().clearBlacklistedCommands();
                                    ChatHelper.send(sender, "commandBlacklistCleared");
                                } else {
                                    field.getListingModule().addBlacklistedCommand(blacklistedCommand);
                                    ChatHelper.send(sender, "commandBlacklistAdded");
                                }
                            } else {
                                ChatHelper.send(sender, "noBlacklistingFieldFound");
                            }
                        } else {
                            plugin.getCommunicationManager().showNotFound(player);
                        }

                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandSetLimit")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setlimit") && hasplayer) {
                        if (args.length == 1) {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                String period = args[0];

                                if (!SignHelper.isValidPeriod(period)) {
                                    ChatHelper.send(sender, "limitMalformed");
                                    ChatHelper.send(sender, "limitMalformed2");
                                    ChatHelper.send(sender, "limitMalformed3");
                                    return true;
                                }

                                if (!field.hasFlag(FieldFlag.RENTABLE) && !field.hasFlag(FieldFlag.SHAREABLE)) {
                                    ChatHelper.send(sender, "limitBadField");
                                    return true;
                                }

                                field.getRentingModule().setLimitSeconds(SignHelper.periodToSeconds(period));
                                ChatHelper.send(sender, "limitSet");
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandSetowner")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.setowner") && hasplayer) {
                        if (args.length == 1) {
                            String owner = args[0];

                            if (owner.contains(":")) {
                                ChatHelper.send(sender, "cannotAssignAsOwners");
                                return true;
                            }

                            TargetBlock aiming = new TargetBlock(player, plugin.getSettingsManager().getMaxTargetDistance(), 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                            Block targetBlock = aiming.getTargetBlock();

                            if (targetBlock != null) {
                                Field field = plugin.getForceFieldManager().getField(targetBlock);

                                if (field != null) {
                                    // transfer the count over to the new owner

                                    plugin.getStorageManager().changeTranslocationOwner(field, owner);
                                    plugin.getStorageManager().offerPlayer(field.getOwner());
                                    plugin.getStorageManager().offerPlayer(owner);

                                    // change the owner

                                    field.setOwner(owner);
                                    plugin.getStorageManager().offerField(field);
                                    ChatHelper.send(sender, "ownerSetTo", owner);
                                    return true;
                                }
                            }

                            ChatHelper.send(sender, "notPointingAtPstone");
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandChangeowner")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.change-owner") && hasplayer) {
                        if (args.length == 1) {
                            String owner = args[0];

                            if (owner.contains(":")) {
                                ChatHelper.send(sender, "cannotAssignAsOwners");
                                return true;
                            }

                            TargetBlock aiming = new TargetBlock(player, plugin.getSettingsManager().getMaxTargetDistance(), 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                            Block targetBlock = aiming.getTargetBlock();

                            if (targetBlock != null) {
                                Field field = plugin.getForceFieldManager().getField(targetBlock);

                                if (field != null) {
                                    if (field.isOwner(player.getName())) {
                                        if (field.hasFlag(FieldFlag.CAN_CHANGE_OWNER)) {
                                            plugin.getForceFieldManager().changeOwner(field, owner);
                                            ChatHelper.send(sender, "fieldCanBeTaken", owner);
                                            return true;
                                        } else {
                                            ChatHelper.send(sender, "fieldCannotChangeOwner");
                                        }
                                    } else {
                                        ChatHelper.send(sender, "ownerCanOnlyChangeOwner");
                                    }
                                }
                            } else {
                                ChatHelper.send(sender, "notPointingAtPstone");
                            }

                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandList")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.list") && hasplayer) {
                        if (args.length == 1) {
                            if (Helper.isInteger(args[0])) {
                                int chunk_radius = Integer.parseInt(args[0]);

                                List<Unbreakable> unbreakables = plugin.getUnbreakableManager().getUnbreakablesInArea(player, chunk_radius);
                                Set<Field> fields = plugin.getForceFieldManager().getFieldsInCustomArea(player.getLocation(), chunk_radius, FieldFlag.ALL);

                                for (Unbreakable u : unbreakables) {
                                    ChatHelper.send(sender, "{aqua}{unbreakable}", u.toString());
                                }

                                for (Field f : fields) {
                                    ChatHelper.send(sender, "{aqua}{field}", f.toString());
                                }

                                if (unbreakables.isEmpty() && fields.isEmpty()) {
                                    ChatHelper.send(sender, "noPstonesFound");
                                }
                                return true;
                            }
                        }
                    } else if (cmd.equals(ChatHelper.format("commandReload")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.reload")) {
                        plugin.getSettingsManager().load();
                        plugin.getLanguageManager().load();

                        ChatHelper.send(sender, "configReloaded");
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandBuy"))) {
                        if (plugin.getSettingsManager().isCommandsToRentBuy()) {
                            if (args.length == 0) {
                                Field field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.BUYABLE);

                                if (field != null) {
                                    FieldSign s = field.getAttachedFieldSign();

                                    if (s.isBuyable()) {
                                        if (field.getBuyingModule().buy(player, s)) {
                                            ChatHelper.send(player, "fieldSignBought");
                                        }

                                        return true;
                                    }
                                }
                            }
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandRent"))) {
                        if (plugin.getSettingsManager().isCommandsToRentBuy()) {
                            if (args.length == 0) {
                                // both sharable and rentable fields are allowed the rent command

                                Field field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.SHAREABLE);

                                if (field == null) {
                                    field = plugin.getForceFieldManager().getOneNonOwnedField(block, player, FieldFlag.RENTABLE);
                                }

                                if (field != null) {
                                    FieldSign s = field.getAttachedFieldSign();

                                    // only allow one renter if rentable

                                    if (s.isRentable()) {
                                        if (field.isRented()) {
                                            if (!field.isRenter(player.getName())) {
                                                // if already rented and player is not the renter, tell them so

                                                ChatHelper.send(player, "fieldSignAlreadyRented");
                                                plugin.getCommunicationManager().showRenterInfo(player, field);
                                                return true;
                                            } else {
                                                // if the player is the renter and they are sneaking, then abandon rent

                                                if (player.isSneaking()) {
                                                    field.getRentingModule().abandonRent(player);
                                                    ChatHelper.send(player, "fieldSignRentAbandoned");
                                                    return true;
                                                }
                                            }
                                        }
                                    }

                                    // initiate rent and set color for sign

                                    if (field.getRentingModule().rent(player, s)) {
                                        if (s.isRentable()) {
                                            s.setRentedColor();
                                        } else if (s.isShareable()) {
                                            s.setSharedColor();
                                        }

                                        return true;
                                    }
                                }
                            }

                            return true;
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.rent")) {
                            if (args.length > 0) {
                                String sub = args[0];

                                if (sub.equalsIgnoreCase(ChatHelper.format("commandRentClear"))) {
                                    Field field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.RENTABLE);

                                    if (field != null) {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.SHAREABLE);
                                    }

                                    if (field != null) {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.BUYABLE);
                                    }

                                    if (field != null) {
                                        if (field.getRentingModule().clearRents()) {
                                            ChatHelper.send(sender, "rentsCleared");
                                        } else {
                                            ChatHelper.send(sender, "rentsClearedNone");
                                        }
                                    } else {
                                        ChatHelper.send(sender, "noPstonesFound");
                                    }
                                }
                                if (sub.equalsIgnoreCase(ChatHelper.format("commandRentRemove"))) {
                                    Field field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.RENTABLE);

                                    if (field != null) {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.SHAREABLE);
                                    }

                                    if (field != null) {
                                        field = plugin.getForceFieldManager().getOneField(block, player, FieldFlag.BUYABLE);
                                    }

                                    if (field != null) {
                                        if (field.getRentingModule().removeRents()) {
                                            ChatHelper.send(sender, "rentsRemoved");
                                        } else {
                                            ChatHelper.send(sender, "rentsRemovedNone");
                                        }
                                    } else {
                                        ChatHelper.send(sender, "noPstonesFound");
                                    }
                                }
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandEnableall")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.enableall")) {
                        if (args.length == 1) {
                            String flagStr = args[0];

                            ChatHelper.send(player, "fieldsDown");

                            int count = plugin.getStorageManager().enableAllFlags(flagStr);

                            if (count == 0) {
                                ChatHelper.send(player, "noFieldsFoundWithFlag");
                            } else {
                                ChatHelper.send(player, "flagEnabledOn", count);
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandDisableall")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall")) {
                        if (args.length == 1) {
                            String flagStr = args[0];

                            ChatHelper.send(player, "fieldsDown");

                            int count = plugin.getStorageManager().disableAllFlags(flagStr);

                            if (count == 0) {
                                ChatHelper.send(player, "noFieldsFoundWithFlag");
                            } else {
                                ChatHelper.send(player, "flagDisabledOn", count);
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandClean")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.clean")) {
                        if (args.length == 1) {
                            String worldName = args[0];

                            World world = Bukkit.getServer().getWorld(worldName);

                            if (world != null) {
                                int cleanedFF = plugin.getForceFieldManager().cleanOrphans(world);
                                int cleanedU = plugin.getUnbreakableManager().cleanOrphans(world);

                                if (cleanedFF > 0) {
                                    ChatHelper.send(sender, "cleanedOrphanedFields", cleanedFF);
                                }
                                if (cleanedU > 0) {
                                    ChatHelper.send(sender, "cleanedOrphanedUnbreakables", cleanedU);
                                }
                                if (cleanedFF == 0 && cleanedU == 0) {
                                    ChatHelper.send(sender, "noOrphansFound");
                                }
                            } else {
                                ChatHelper.send(sender, "worldNotFound");
                            }
                        } else {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleanedFF = 0;
                            int cleanedU = 0;

                            for (World world : worlds) {
                                cleanedFF += plugin.getForceFieldManager().cleanOrphans(world);
                                cleanedU += plugin.getUnbreakableManager().cleanOrphans(world);
                            }
                            if (cleanedFF > 0) {
                                ChatHelper.send(sender, "cleanedOrphanedFields", cleanedFF);
                            }
                            if (cleanedU > 0) {
                                ChatHelper.send(sender, "cleanedOrphanedUnbreakables", cleanedU);
                            }
                            if (cleanedFF == 0 && cleanedU == 0) {
                                ChatHelper.send(sender, "noOrphansFound");
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandRevert")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.revert")) {
                        if (args.length == 1) {
                            String worldName = args[0];

                            World world = Bukkit.getServer().getWorld(worldName);

                            if (world != null) {
                                int cleanedFF = plugin.getForceFieldManager().revertOrphans(world);
                                int cleanedU = plugin.getUnbreakableManager().revertOrphans(world);

                                if (cleanedFF > 0) {
                                    ChatHelper.send(sender, "revertedOrphanFields", cleanedFF);
                                }
                                if (cleanedU > 0) {
                                    ChatHelper.send(sender, "revertedOrphanUnbreakables", cleanedU);
                                }

                                if (cleanedFF == 0 && cleanedU == 0) {
                                    ChatHelper.send(sender, "noOrphansFound");
                                }
                            } else {
                                ChatHelper.send(sender, "worldNotFound");
                            }
                        } else {
                            List<World> worlds = plugin.getServer().getWorlds();

                            int cleanedFF = 0;
                            int cleanedU = 0;

                            for (World world : worlds) {
                                cleanedFF += plugin.getForceFieldManager().revertOrphans(world);
                                cleanedU += plugin.getUnbreakableManager().revertOrphans(world);
                            }

                            if (cleanedFF > 0) {
                                ChatHelper.send(sender, "revertedOrphanFields", cleanedFF);
                            }
                            if (cleanedU > 0) {
                                ChatHelper.send(sender, "revertedOrphanUnbreakables", cleanedU);
                            }

                            if (cleanedFF == 0 && cleanedU == 0) {
                                ChatHelper.send(sender, "noOrphansFound");
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandPull")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.pull")) {
                        plugin.getStorageManager().loadWorldData();
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandMigrate")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.migrate")) {
                        if (args.length == 2) {
                            String oldUsername = args[0];
                            String newUsername = args[1];

                            PreciousStones.getInstance().getStorageManager().migrate(oldUsername, newUsername);
                            plugin.getStorageManager().offerPlayer(newUsername);
                            ChatHelper.send(sender, "migrateDone");
                            return true;
                        }
                    } else if (cmd.equals(ChatHelper.format("commandBypass")) && plugin.getPermissionsManager().has(player, "preciousstones.bypass.toggle")) {
                        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player);

                        if (args.length == 1) {
                            String mode = args[0];

                            if (mode.equals(ChatHelper.format("commandOn"))) {
                                entry.setBypassDisabled(false);
                                ChatHelper.send(player, "bypassEnabled");
                            } else if (mode.equals(ChatHelper.format("commandOff"))) {
                                entry.setBypassDisabled(true);
                                ChatHelper.send(player, "bypassDisabled");
                            }
                        } else {
                            if (entry.isBypassDisabled()) {
                                entry.setBypassDisabled(false);
                                ChatHelper.send(player, "bypassEnabled");
                            } else {
                                entry.setBypassDisabled(true);
                                ChatHelper.send(player, "bypassDisabled");
                            }
                        }

                        plugin.getStorageManager().offerPlayer(player.getName());
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandHide")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.hide")) {
                        if (args.length == 1) {
                            if (args[0].equals(ChatHelper.format("commandAll")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.hideall")) {
                                int count = plugin.getForceFieldManager().hideBelonging(player.getName());

                                if (count > 0) {
                                    ChatHelper.send(sender, "hideHideAll", count);
                                }
                            }
                        } else {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.HIDABLE)) {
                                    if (!field.getHidingModule().isHidden()) {
                                        if (!field.matchesBlockType()) {
                                            ChatHelper.send(sender, "cannotHideOrphan");
                                            return true;
                                        }

                                        field.getHidingModule().hide();
                                        ChatHelper.send(sender, "hideHide");
                                    } else {
                                        ChatHelper.send(sender, "hideHiddenAlready");
                                    }
                                } else {
                                    ChatHelper.send(sender, "hideCannot");
                                }
                            } else {
                                plugin.getCommunicationManager().showNotFound(player);
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandUnhide")) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.hide")) {
                        if (args.length == 1) {
                            if (args[0].equals(ChatHelper.format("commandAll")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.hideall")) {
                                int count = plugin.getForceFieldManager().unhideBelonging(player.getName());

                                if (count > 0) {
                                    ChatHelper.send(sender, "hideUnhideAll", count);
                                }
                            }
                        } else {
                            Field field = plugin.getForceFieldManager().getOneOwnedField(block, player, FieldFlag.ALL);

                            if (field != null) {
                                if (field.hasFlag(FieldFlag.HIDABLE)) {
                                    if (field.getHidingModule().isHidden()) {
                                        field.getHidingModule().unHide();
                                        ChatHelper.send(sender, "hideUnhide");
                                    } else {
                                        ChatHelper.send(sender, "hideUnHiddenAlready");
                                    }
                                } else {
                                    ChatHelper.send(sender, "hideCannot");
                                }
                            } else {
                                ChatHelper.send(sender, "hideNoneFound");
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandGive")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.give")) {
                        if (args.length >= 2) {
                            // [player] [field name] <amount>

                            String[] remaining = args.clone();

                            String playerName = remaining[0];
                            remaining = Helper.removeFirst(remaining);

                            String fieldName = "";
                            while (remaining.length > 0 && !Helper.isInteger(remaining[0])) {
                                fieldName += remaining[0] + " ";
                                remaining = Helper.removeFirst(remaining);
                            }
                            fieldName = fieldName.trim();

                            int count = 1;
                            if (remaining.length > 0) {
                                count = Integer.parseInt(remaining[0]);
                            }

                            Player recipient = plugin.getServer().getPlayer(playerName);

                            if (recipient != null) {
                                FieldSettings settings = plugin.getSettingsManager().getFieldSettings(fieldName);

                                if (settings != null) {
                                    plugin.getForceFieldManager().giveField(recipient, settings, count);
                                    ChatHelper.send(sender, "fieldsGiven", playerName, settings.getTitle(), count);
                                    ChatHelper.send(recipient, "fieldsGivenPlayer", sender.getName(), settings.getTitle(), count);
                                } else {
                                    ChatHelper.send(sender, "fieldNotFound", fieldName);
                                }
                            } else {
                                ChatHelper.send(sender, "playerNotFound", playerName);
                            }
                        }
                        return true;
                    } else if (cmd.equals(ChatHelper.format("commandPlace")) && plugin.getPermissionsManager().has(player, "preciousstones.admin.place")) {
                        if (args.length >= 6) {
                            // [owner] [field name] [x] [y] [z] <radius> <height>

                            String[] remaining = args.clone();

                            String ownerName = remaining[0];
                            remaining = Helper.removeFirst(remaining);

                            String fieldName = "";
                            while (!Helper.isInteger(remaining[0])) {
                                fieldName += remaining[0] + " ";
                                remaining = Helper.removeFirst(remaining);
                            }
                            fieldName = fieldName.trim();

                            int x = Integer.parseInt(remaining[0]);
                            remaining = Helper.removeFirst(remaining);
                            int y = Integer.parseInt(remaining[0]);
                            remaining = Helper.removeFirst(remaining);
                            int z = Integer.parseInt(remaining[0]);
                            remaining = Helper.removeFirst(remaining);

                            String worldName = remaining[0];
                            remaining = Helper.removeFirst(remaining);

                            int radius = -1;
                            if (remaining.length > 0) {
                                radius = Integer.parseInt(remaining[0]);
                                remaining = Helper.removeFirst(remaining);
                            }

                            int height = -1;
                            if (remaining.length > 0) {
                                height = Integer.parseInt(remaining[0]);
                            }

                            FieldSettings settings = plugin.getSettingsManager().getFieldSettings(fieldName);

                            if (settings != null) {
                                if (radius == -1) {
                                    radius = settings.getRadius();
                                }

                                if (height == -1) {
                                    height = settings.getCustomHeight();
                                }

                                plugin.getForceFieldManager().placeField(sender, ownerName, settings, x, y, z, worldName, radius, height);
                                ChatHelper.send(sender, "fieldPlaced", ownerName, settings.getTitle(), x, y, z, worldName, radius, height);
                            } else {
                                ChatHelper.send(sender, "fieldNotFound", fieldName);
                            }
                        }
                        return true;
                    }

                    ChatHelper.send(sender, "notValidCommand");
                    return true;
                }

                // show the player menu

                plugin.getCommunicationManager().showMenu(player);
                return true;
            }
        } catch (Exception ex) {
            System.out.print("Error: " + ex.getMessage());

            for (StackTraceElement el : ex.getStackTrace()) {
                System.out.print(el.toString());
            }
        }

        return false;
    }
}
