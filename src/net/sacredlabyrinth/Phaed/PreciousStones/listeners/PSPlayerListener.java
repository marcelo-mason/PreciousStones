package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;

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
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
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
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onPlayerInteract");

        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || player == null)
        {
            return;
        }

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getClickedBlock().getChunk())))
        {
            return;
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

            ItemStack is = player.getItemInHand();

            if (is != null)
            {
                if (plugin.settings.isToolItemType(is.getTypeId()))
                {
                    if (plugin.settings.isBypassBlock(block))
                    {
                        return;
                    }

                    if (block.getState() instanceof ContainerBlock)
                    {
                        plugin.snm.recordSnitchUsed(player, block);
                    }

                    if (plugin.settings.isSnitchType(block) && plugin.ffm.isField(block))
                    {
                        plugin.cm.showIntruderList(player, plugin.ffm.getField(block));
                        return;
                    }
                    else if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
                    {
                        if (plugin.um.isOwner(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
                        {
                            plugin.cm.showUnbreakableDetails(plugin.um.getUnbreakable(block), player);
                        }
                        else
                        {
                            plugin.cm.showUnbreakableOwner(player, block);
                        }
                        return;
                    }
                    else if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
                    {
                        Field field = plugin.ffm.getField(block);
                        FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                        if (fieldsettings == null)
                        {
                            plugin.ffm.queueRelease(field);
                            return;
                        }

                        if ((plugin.ffm.isAllowed(block, player.getName()) || plugin.pm.hasPermission(player, "preciousstones.admin.undo")) && (fieldsettings.griefUndoInterval || fieldsettings.griefUndoRequest))
                        {
                            int size = plugin.gum.undoGrief(field);

                            if (size > 0)
                            {
                                player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "Rolled back " + size + " griefed blocks");
                            }
                            else
                            {
                                player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "No grief recorded");
                            }
                        }
                        else if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
                        {
                            List<Field> fields = new ArrayList<Field>();
                            fields.add(plugin.ffm.getField(block));
                            plugin.cm.showFieldDetails(player, fields);
                        }
                        else
                        {
                            plugin.cm.showFieldOwner(player, block);
                        }
                        return;
                    }
                }

                //----------------------------------------------------------------------

                Field field = plugin.ffm.isDestroyProtected(block.getLocation(), null);

                if (field != null)
                {
                    if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails)
                    {
                        List<Field> fields = plugin.ffm.getSourceFields(block);

                        plugin.cm.showProtectedLocation(fields, player, block);
                    }
                    else
                    {
                        if (is.getType().equals(Material.BUCKET))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        else
                        {
                            if (plugin.settings.isToolItemType(is.getTypeId()))
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

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getTo().getBlock().getChunk())))
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

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getTo().getBlock().getChunk())))
        {
            return;
        }

        if (new Vec(event.getFrom()).equals(new Vec(event.getTo())))
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
            plugin.viz.revertVisualization(player);
        }

        // remove player form any entry field he is not currently in

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
                        FieldSettings fieldsettings = plugin.settings.getFieldSettings(entryfield);

                        if (fieldsettings == null)
                        {
                            plugin.ffm.queueRelease(entryfield);
                            return;
                        }

                        if (fieldsettings.farewellMessage)
                        {
                            if (entryfield.getName().length() > 0)
                            {
                                plugin.cm.showFarewellMessage(player, entryfield.getName());
                            }
                            else if ((fieldsettings.griefUndoRequest || fieldsettings.griefUndoInterval) && !entryfield.isAllowed(player.getName()))
                            {
                                plugin.cm.showWelcomeMessage(player, "Grief revertable area");
                            }
                        }
                    }
                }
            }
        }

        List<Field> currentfields = plugin.ffm.getSourceFields(player);

        // check if were on a prevent entry field the player is no allowed in

        for (Field field : currentfields)
        {
            if (field.isAllowed(player.getName()))
            {
                continue;
            }

            if (plugin.stm.isTeamMate(player.getName(), field.getOwner()))
            {
                continue;
            }

            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                return;
            }

            if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
            {
                if (fieldsettings.preventEntry)
                {
                    event.setTo(event.getFrom());
                    plugin.cm.warnEntry(player, field);
                    return;
                }
            }
        }

        // enter all fields the player just moved into

        if (currentfields != null)
        {
            for (Field currentfield : currentfields)
            {
                if (!plugin.em.isInsideField(player, currentfield))
                {
                    if (!plugin.em.containsSameNameOwnedField(player, currentfield))
                    {
                        FieldSettings fieldsettings = plugin.settings.getFieldSettings(currentfield);

                        if (fieldsettings == null)
                        {
                            plugin.ffm.queueRelease(currentfield);
                            return;
                        }

                        if (fieldsettings.welcomeMessage)
                        {
                            if (currentfield.getName().length() > 0)
                            {
                                plugin.cm.showWelcomeMessage(player, currentfield.getName());

                                //plugin.stm.bypassAnnounce(currentfield, player.getName());
                            }
                            else if ((fieldsettings.griefUndoRequest || fieldsettings.griefUndoInterval) && !currentfield.isAllowed(player.getName()))
                            {
                                plugin.cm.showWelcomeMessage(player, "Grief revertable area");
                            }
                        }
                    }

                    plugin.em.enterField(player, currentfield);
                }
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

        DebugTimer dt = new DebugTimer("onPlayerBucketFill");

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Material mat = event.getBucket();

        if (block == null)
        {
            return;
        }

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlockClicked().getChunk())))
        {
            return;
        }

        Field field = plugin.ffm.isPlaceProtected(block, player);

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

        field = plugin.ffm.isUndoGriefField(block, player);

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

        DebugTimer dt = new DebugTimer("onPlayerBucketEmpty");

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Material mat = event.getBucket();

        if (block == null)
        {
            return;
        }

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlockClicked().getChunk())))
        {
            return;
        }

        Field field = plugin.ffm.isPlaceProtected(block, player);

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

        field = plugin.ffm.isUndoGriefField(block, player);

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
