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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
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

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || player == null)
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

            if (block.getType().equals(Material.LEVER) || block.getType().equals(Material.MINECART) || block.getType().equals(Material.NOTE_BLOCK) || block.getType().equals(Material.STONE_BUTTON))
            {
                plugin.snm.recordSnitchUsed(player, block);
            }

            ItemStack is = player.getItemInHand();

            if (is == null || !plugin.settings.isToolItemType(is.getTypeId()))
            {
                return;
            }

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
            }
            else if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
            {
                if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
                {
                    List<Field> fields = new ArrayList<Field>();
                    fields.add(plugin.ffm.getField(block));
                    plugin.cm.showFieldDetails(player, fields);
                }
                else
                {
                    plugin.cm.showFieldOwner(player, block);
                }
            }
            else
            {
                Field field = plugin.ffm.isDestroyProtected(block, null);

                if (field != null)
                {
                    if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails)
                    {
                        List<Field> fields = plugin.ffm.getSourceFields(block);

                        plugin.cm.showProtectedLocation(fields, player, block);
                    }
                    else
                    {
                        plugin.cm.showProtected(player, block);
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

            if (plugin.stm.isRival(player.getName(), field.getOwner()) && plugin.stm.isAnyOnline(field.getOwner()))
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

                                if (plugin.stm.isRival(player.getName(), currentfield.getOwner()))
                                {
                                    plugin.stm.bypassAnnounce(currentfield, player.getName());
                                }
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

    /**
     *
     * @param event
     */
    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {
        DebugTimer dt = new DebugTimer("onPlayerBucketEmpty");

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Material mat = event.getBucket();

        Field field = plugin.ffm.isPlaceProtected(block, player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                if (plugin.stm.isTeamMate(player.getName(), field.getOwner()))
                {
                    // do nothing
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnEmpty(player, mat, field);
                }
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }
}
