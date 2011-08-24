package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Block;
import org.bukkit.Material;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * PreciousStones player listener
 *
 * @author Phaed
 */
public class PSPlayerListener extends PlayerListener
{
    private final PreciousStones plugin;

    /**
     *
     */
    public PSPlayerListener()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        plugin.getPlayerManager().playerLogin(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName(), true);
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.getPlayerManager().playerLogoff(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName(), true);
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerKick(PlayerKickEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        plugin.getPlayerManager().playerLogoff(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName(), true);
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        DebugTimer dt = new DebugTimer("onPlayerInteract");

        if (block == null || player == null)
        {
            return;
        }

        Field usefield = plugin.getForceFieldManager().findUseProtected(block.getLocation(), player, block.getTypeId());

        if (usefield != null)
        {
            if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.use"))
            {
                plugin.getCommunicationManager().warnUse(player, block, usefield);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction().equals(Action.PHYSICAL))
        {
            plugin.getSnitchManager().recordSnitchUsed(player, block);
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if (block.getType().equals(Material.WALL_SIGN))
            {
                plugin.getSnitchManager().recordSnitchShop(player, block);
            }

            if (block.getType().equals(Material.WORKBENCH) || block.getType().equals(Material.BED) || block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.LEVER) || block.getType().equals(Material.MINECART) || block.getType().equals(Material.NOTE_BLOCK) || block.getType().equals(Material.JUKEBOX) || block.getType().equals(Material.STONE_BUTTON))
            {
                plugin.getSnitchManager().recordSnitchUsed(player, block);
            }

            if (block.getState() instanceof ContainerBlock)
            {
                plugin.getSnitchManager().recordSnitchUsed(player, block);
            }

            ItemStack is = player.getItemInHand();

            if (is != null)
            {
                if (plugin.getSettingsManager().isToolItemType(is.getTypeId()))
                {
                    if (plugin.getSettingsManager().isBypassBlock(block))
                    {
                        return;
                    }

                    if (plugin.getSettingsManager().isSnitchType(block) && plugin.getForceFieldManager().isField(block))
                    {
                        Field field = plugin.getForceFieldManager().getField(block);

                        if (field.getOwner().equals(player.getName()) || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.details"))
                        {
                            if (!plugin.getCommunicationManager().showSnitchList(player, plugin.getForceFieldManager().getField(block)))
                            {
                                showInfo(block, player);
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "There have been no intruders around here");
                                ChatBlock.sendBlank(player);
                            }
                        }
                    }
                    else if (plugin.getUnbreakableManager().isUnbreakable(block))
                    {
                        if (plugin.getUnbreakableManager().isOwner(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.details"))
                        {
                            plugin.getCommunicationManager().showUnbreakableDetails(plugin.getUnbreakableManager().getUnbreakable(block), player);
                        }
                        else
                        {
                            plugin.getCommunicationManager().showUnbreakableOwner(player, block);
                        }
                    }
                    else if (plugin.getForceFieldManager().isField(block))
                    {
                        Field field = plugin.getForceFieldManager().getField(block);
                        FieldSettings fs = field.getSettings();

                        if ((plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.undo")) && (fs.hasGriefUndoFlag()))
                        {
                            HashSet<Field> overlapped = plugin.getForceFieldManager().getOverlappedFields(player, field);

                            if (overlapped.size() == 1)
                            {
                                int size = plugin.getGriefUndoManager().undoGrief(field);

                                if (size > 0)
                                {
                                    player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "Rolled back " + size + " griefed " + Helper.plural(size, "block", "s"));
                                }
                                else
                                {
                                    showInfo(block, player);
                                    player.sendMessage(ChatColor.AQUA + "No grief recorded");
                                    ChatBlock.sendBlank(player);
                                }
                            }
                            else
                            {
                                int size = 0;

                                for (Field o : overlapped)
                                {
                                    size += plugin.getGriefUndoManager().undoGrief(o);
                                }

                                if (size > 0)
                                {
                                    player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "Rolled back " + size + " griefed " + Helper.plural(size, "block", "s") + " on " + overlapped.size() + " overlapped fields");
                                }
                                else
                                {
                                    showInfo(block, player);
                                    player.sendMessage(ChatColor.AQUA + "No grief recorded on any of the " + overlapped.size() + " overlapped fields");
                                    ChatBlock.sendBlank(player);
                                }
                            }
                        }
                        else
                        {
                            showInfo(block, player);
                        }
                    }
                    else
                    {
                        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

                        if (field != null)
                        {
                            if (plugin.getForceFieldManager().isAllowed(field, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails())
                            {
                                plugin.getCommunicationManager().showProtectedLocation(player, block);
                            }
                            else
                            {
                                plugin.getCommunicationManager().showProtected(player, block);
                            }
                        }
                    }
                }
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    private void showInfo(Block block, Player player)
    {
        if (plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.details"))
        {
            List<Field> fields = Arrays.asList(plugin.getForceFieldManager().getField(block));
            plugin.getCommunicationManager().showFieldDetails(player, fields);
        }
        else
        {
            plugin.getCommunicationManager().showFieldOwner(player, block);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        handlePlayerMove(event);
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        handlePlayerMove(event);
    }

    private void handlePlayerMove(PlayerMoveEvent event)
    {
        DebugTimer dt = new DebugTimer("onPlayerMove");
        Player player = event.getPlayer();

        // undo a player's visualization if it exists

        if (plugin.getSettingsManager().isVisualizeEndOnMove())
        {
            if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.visualize"))
            {
                plugin.getVisualizationManager().revertVisualization(player);
            }
        }

        // remove player from any entry field he is not currently in

        List<Field> entryfields = plugin.getEntryManager().getPlayerEntryFields(player);

        if (entryfields != null)
        {
            for (Field entryfield : entryfields)
            {
                if (!entryfield.envelops(player.getLocation()))
                {
                    plugin.getEntryManager().leaveField(player, entryfield);

                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryfield))
                    {
                        plugin.getEntryManager().leaveOverlappedArea(player, entryfield);
                    }
                }
            }
        }

        // get all the fields the player is currently standing in

        List<Field> currentfields = plugin.getForceFieldManager().getSourceFields(player.getLocation());

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.entry"))
        {
            for (Field field : currentfields)
            {
                FieldSettings fs = field.getSettings();

                if (fs.hasFlag(FieldFlag.PREVENT_ENTRY))
                {
                    if (!plugin.getForceFieldManager().isAllowed(field, player.getName()))
                    {
                        Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                        Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                        if (outside != null)
                        {
                            Field f = plugin.getForceFieldManager().getNotAllowedSourceField(outside, player.getName(), FieldFlag.PREVENT_ENTRY);

                            if (f != null)
                            {
                                loc = outside;
                            }
                        }

                        event.setTo(loc);
                        plugin.getCommunicationManager().warnEntry(player, field);
                        return;
                    }
                }
            }
        }

        // did not get teleported out so now we update his last known outside location

        plugin.getPlayerManager().updateOutsideLocation(player);

        // enter all fields hes is not currently entered into yet

        for (Field currentfield : currentfields)
        {
            if (!plugin.getEntryManager().enteredField(player, currentfield))
            {
                if (!plugin.getEntryManager().containsSameNameOwnedField(player, currentfield))
                {
                    plugin.getEntryManager().enterOverlappedArea(player, currentfield);
                }
                plugin.getEntryManager().enterField(player, currentfield);
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();

        if (block == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onPlayerBucketFill");

        Field field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.break"))
            {
                plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnDestroyArea(player, block, field);
            }
        }

        field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_INTERVAL, FieldFlag.GRIEF_UNDO_REQUEST);

        if (field != null)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.break"))
            {
                plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnDestroyArea(player, block, field);
                return;
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Material mat = event.getBucket();

        if (block == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onPlayerBucketEmpty");

        Field field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnEmpty(player, block, field);
            }
        }

        field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_INTERVAL, FieldFlag.GRIEF_UNDO_REQUEST);

        if (field != null)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnPlace(player, block, field);
                return;
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }
}
