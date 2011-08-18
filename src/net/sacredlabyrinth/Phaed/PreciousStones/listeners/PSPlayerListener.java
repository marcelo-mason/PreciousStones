package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
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
     * @param plugin
     */
    public PSPlayerListener(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        if (plugin.settings.isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        plugin.sm.offerPlayer(event.getPlayer().getName(), true);
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (plugin.settings.isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        plugin.sm.offerPlayer(event.getPlayer().getName(), true);
        plugin.plm.cleanOutsideLocation(event.getPlayer());
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

        if (plugin.settings.isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        plugin.sm.offerPlayer(event.getPlayer().getName(), true);
        plugin.plm.cleanOutsideLocation(event.getPlayer());
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

        if (plugin.settings.isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
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

        Field usefield = plugin.ffm.findUseProtected(block.getLocation(), player, block.getTypeId());

        if (usefield != null)
        {
            if (!plugin.pm.hasPermission(player, "preciousstones.bypass.use"))
            {
                plugin.cm.warnUse(player, block.getType(), usefield);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction().equals(Action.PHYSICAL))
        {
            plugin.snm.recordSnitchUsed(player, block);
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if (block.getType().equals(Material.WALL_SIGN))
            {
                plugin.snm.recordSnitchShop(player, block);
            }

            if (block.getType().equals(Material.WORKBENCH) || block.getType().equals(Material.BED) || block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.LEVER) || block.getType().equals(Material.MINECART) || block.getType().equals(Material.NOTE_BLOCK) || block.getType().equals(Material.JUKEBOX) || block.getType().equals(Material.STONE_BUTTON))
            {
                plugin.snm.recordSnitchUsed(player, block);
            }

            if (block.getState() instanceof ContainerBlock)
            {
                plugin.snm.recordSnitchUsed(player, block);
            }

            ItemStack is = player.getItemInHand();

            if (is != null)
            {
                if (plugin.settings.isToolItemType(is.getTypeId()))
                {
                    if (plugin.settings.isBypassBlock(block))
                    {
                        return;
                    }

                    if (plugin.settings.isSnitchType(block) && plugin.ffm.isField(block))
                    {
                        Field field = plugin.ffm.getField(block);

                        if (field.getOwner().equals(player.getName()) || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
                        {
                            if (!plugin.cm.showSnitchList(player, plugin.ffm.getField(block)))
                            {
                                showInfo(block, player);
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "There have been no intruders around here");
                                ChatBlock.sendBlank(player);
                            }
                        }
                    }
                    else if (plugin.um.isUnbreakable(block))
                    {
                        if (plugin.um.isOwner(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
                        {
                            plugin.cm.showUnbreakableDetails(plugin.um.getUnbreakable(block), player);
                        }
                        else
                        {
                            plugin.cm.showUnbreakableOwner(player, block);
                        }
                    }
                    else if (plugin.ffm.isField(block))
                    {
                        Field field = plugin.ffm.getField(block);
                        FieldSettings fs = field.getSettings();

                        if ((plugin.ffm.isAllowed(block, player.getName()) || plugin.pm.hasPermission(player, "preciousstones.admin.undo")) && (fs.isGriefUndoInterval() || fs.isGriefUndoRequest()))
                        {
                            HashSet<Field> overlapped = plugin.ffm.getOverlappedFields(player, field);

                            if (overlapped.size() == 1)
                            {
                                int size = plugin.gum.undoGrief(field);

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
                                    size += plugin.gum.undoGrief(o);
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
                        Field field = plugin.ffm.findProtected(block.getLocation());

                        if (field != null)
                        {
                            if (plugin.ffm.isAllowed(field, player.getName()) || plugin.settings.publicBlockDetails)
                            {
                                plugin.cm.showProtectedLocation(player, block);
                            }
                            else
                            {
                                plugin.cm.showProtected(player, block);
                            }
                        }
                    }
                }
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }

    private void showInfo(Block block, Player player)
    {
        if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
        {
            List<Field> fields = Arrays.asList(plugin.ffm.getField(block));
            plugin.cm.showFieldDetails(player, fields);
        }
        else
        {
            plugin.cm.showFieldOwner(player, block);
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

        if (plugin.settings.isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
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

        if (plugin.settings.visualizeEndOnMove)
        {
            if (!plugin.pm.hasPermission(player, "preciousstones.admin.visualize"))
            {
                plugin.viz.revertVisualization(player);
            }
        }

        // remove player from any entry field he is not currently in

        LinkedList<Field> entryfields = plugin.em.getPlayerEntryFields(player);

        if (entryfields != null)
        {
            for (Field entryfield : entryfields)
            {
                if (!entryfield.envelops(player.getLocation()))
                {
                    plugin.em.leaveField(player, entryfield);

                    if (!plugin.em.containsSameNameOwnedField(player, entryfield))
                    {
                        plugin.em.leaveOverlappedArea(player, entryfield);
                    }
                }
            }
        }

        // get all the fields the player is currently standing in

        List<Field> currentfields = plugin.ffm.getSourceFields(player.getLocation());

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
        {
            for (Field field : currentfields)
            {
                if (field.getSettings().isPreventEntry())
                {
                    if (!plugin.ffm.isAllowed(field, player.getName()))
                    {
                        Location loc = plugin.plm.getOutsideFieldLocation(field, player);
                        Location outside = plugin.plm.getOutsideLocation(player);

                        if (outside != null)
                        {
                            if (!plugin.ffm.isEntryProtected(outside, player))
                            {
                                loc = outside;
                            }
                        }

                        event.setTo(loc);
                        plugin.cm.warnEntry(player, field);
                        return;
                    }
                }
            }
        }

        // did not get teleported out so now we update his last known outside location

        plugin.plm.updateOutsideLocation(player);

        // enter all fields hes is not currently entered into yet

        for (Field currentfield : currentfields)
        {
            if (!plugin.em.enteredField(player, currentfield))
            {
                if (!plugin.em.containsSameNameOwnedField(player, currentfield))
                {
                    plugin.em.enterOverlappedArea(player, currentfield);
                }
                plugin.em.enterField(player, currentfield);
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }

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

        if (plugin.settings.isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onPlayerBucketFill");

        Field field = plugin.ffm.findPlaceProtected(block.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.break"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnDestroyArea(player, block, field);
            }
        }

        field = plugin.ffm.findGriefProtected(block.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.break"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnDestroyArea(player, block, field);
                return;
            }
        }

        if (plugin.settings.debug)
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

        if (plugin.settings.isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onPlayerBucketEmpty");

        Field field = plugin.ffm.findPlaceProtected(block.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnEmpty(player, mat, field);
            }
        }

        field = plugin.ffm.findGriefProtected(block.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnPlace(player, block, field);
                return;
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }
}
