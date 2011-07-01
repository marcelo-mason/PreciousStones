package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashSet;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.Chunk;

/**
 * PreciousStones block listener
 *
 * @author Phaed
 */
public class PSBlockListener extends BlockListener
{
    private final PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public PSBlockListener(final PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockFromTo(BlockFromToEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        // only capture water and lava movement

        if (!event.getBlock().getType().equals(Material.WATER) && !event.getBlock().getType().equals(Material.LAVA))
        {
            return;
        }

        // skip areas that don't have pstones

        Chunk chunk = event.getToBlock().getChunk();

        if (!plugin.tm.isTaggedArea(new ChunkVec(chunk)))
        {
            return;
        }

        Field to = plugin.ffm.isFlowProtected(event.getToBlock());

        if (to == null)
        {
            return;
        }

        Field from = plugin.ffm.isFlowProtected(event.getBlock());

        if (from == null)
        {
            event.setCancelled(true);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        // skip areas that don't have pstones

        Chunk chunk = event.getBlock().getChunk();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null)
        {
            return;
        }

        if (!plugin.tm.isTaggedArea(new ChunkVec(chunk)))
        {
            return;
        }

        if (player != null)
        {
            plugin.snm.recordSnitchIgnite(player, block);
        }

        Field field = plugin.ffm.isFireProtected(block, player);

        if (field != null)
        {
            event.setCancelled(true);

            if (player != null)
            {
                plugin.cm.warnFire(player, field);
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
    {
        Chunk chunk = event.getBlock().getChunk();
        Block redstoneblock = event.getBlock();

        // skip areas that don't have pstones

        if (!plugin.tm.isTaggedArea(new ChunkVec(chunk)))
        {
            return;
        }

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && y == 0 && z == 0)
                    {
                        continue;
                    }

                    Block fieldblock = redstoneblock.getRelative(x, y, z);

                    if (plugin.settings.isFieldType(fieldblock))
                    {
                        if (plugin.ffm.isField(fieldblock))
                        {
                            if (event.getNewCurrent() > event.getOldCurrent())
                            {
                                Field field = plugin.ffm.getField(fieldblock);
                                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                                if (fieldsettings.cannon)
                                {
                                    HashSet<String> players = plugin.em.getInhabitants(field);

                                    for (String pl : players)
                                    {
                                        Player player = Helper.matchExactPlayer(plugin, pl);

                                        if (player != null)
                                        {
                                            plugin.vm.shootPlayer(player, field);
                                        }
                                    }
                                }

                                if (fieldsettings.launch)
                                {
                                    HashSet<String> players = plugin.em.getInhabitants(field);

                                    for (String pl : players)
                                    {
                                        Player player = Helper.matchExactPlayer(plugin, pl);

                                        if (player != null)
                                        {
                                            plugin.vm.launchPlayer(player, field);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();

        if (brokenBlock == null || player == null)
        {
            return;
        }

        if (plugin.settings.isBypassBlock(brokenBlock))
        {
            return;
        }

        // skip areas that don't have pstones

        if (!plugin.tm.isTaggedArea(new ChunkVec(chunk)))
        {
            return;
        }

        plugin.snm.recordSnitchBlockBreak(player, brokenBlock);

        if ((plugin.settings.isFieldType(brokenBlock) || plugin.settings.isCloakableType(brokenBlock)) && plugin.ffm.isField(brokenBlock))
        {
            if (plugin.ffm.isBreakable(brokenBlock))
            {
                plugin.cm.notifyDestroyBreakableFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);
            }
            else if (plugin.ffm.isOwner(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);
            }
            else if (plugin.settings.allowedCanBreakPstones && plugin.ffm.isAllowed(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyOthersFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);
            }
            else if (plugin.pm.hasPermission(player, "preciousstones.bypass.forcefield"))
            {
                plugin.cm.notifyBypassDestroyFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);
            }
            else
            {
                Field field = plugin.ffm.getField(brokenBlock);
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                if (field != null && plugin.stm.isTeamMate(player.getName(), field.getOwner()))
                {
                    plugin.stm.addBB(player, Helper.capitalize(player.getName()) + " has taken " + field.getOwner() + "'s " + fieldsettings.getTitle() + " from " + field.getX() + " " + field.getY() + " " + field.getZ() + " " + field.getWorld());
                    plugin.ffm.release(brokenBlock);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnDestroyFF(player, brokenBlock);
                }
            }
        }
        else if (plugin.settings.isUnbreakableType(brokenBlock) && plugin.um.isUnbreakable(brokenBlock))
        {
            if (plugin.um.isOwner(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyU(player, brokenBlock);
                plugin.um.release(brokenBlock);
            }
            else if (plugin.pm.hasPermission(player, "preciousstones.bypass.unbreakable"))
            {
                plugin.cm.notifyBypassDestroyU(player, brokenBlock);
                plugin.um.release(brokenBlock);
            }
            else
            {
                Unbreakable ub = plugin.um.getUnbreakable(brokenBlock);

                if (ub != null && plugin.stm.isTeamMate(player.getName(), ub.getOwner()))
                {
                    plugin.stm.addBB(player, Helper.capitalize(player.getName()) + " has taken " + ub.getOwner() + "'s " + ub.getType() + " from " + ub.getX() + " " + ub.getY() + " " + ub.getZ() + " " + ub.getWorld());
                    plugin.ffm.release(brokenBlock);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnDestroyU(player, brokenBlock);
                }
            }
        }
        else
        {
            Field field = plugin.ffm.isDestroyProtected(brokenBlock, player);

            if (field != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.destroy"))
                {
                    plugin.cm.notifyBypassDestroy(player, brokenBlock, field);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnDestroyArea(player, brokenBlock, field);
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Block placedblock = event.getBlock();
        Player player = event.getPlayer();

        if (placedblock == null || player == null)
        {
            return;
        }

        if (plugin.settings.isBypassBlock(placedblock))
        {
            return;
        }

        plugin.snm.recordSnitchBlockPlace(player, placedblock);

        if (plugin.settings.isUnbreakableType(placedblock) && plugin.pm.hasPermission(player, "preciousstones.benefit.create.unbreakable"))
        {
            Field conflictfield = plugin.ffm.unbreakableConflicts(placedblock, player);

            if (conflictfield != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
                {
                    if (plugin.um.add(placedblock, player))
                    {
                        plugin.cm.notifyBypassPlaceU(player, conflictfield);
                    }
                }
                else
                {
                    Unbreakable ub = plugin.um.getUnbreakable(placedblock);

                    if (ub == null || !plugin.stm.isTeamMate(player.getName(), ub.getOwner()))
                    {
                        event.setCancelled(true);
                        plugin.cm.warnConflictU(player, placedblock, conflictfield);
                    }
                }
            }
            else
            {
                if (plugin.upm.touchingUnprotectableBlock(placedblock))
                {
                    if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                    {
                        if (plugin.um.add(placedblock, player))
                        {
                            plugin.cm.notifyBypassTouchingUnprotectable(player, placedblock);
                        }
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.cm.warnPlaceTouchingUnprotectable(player, placedblock);
                    }
                }
                else
                {
                    if (plugin.um.add(placedblock, player))
                    {
                        plugin.cm.notifyPlaceU(player, placedblock);
                    }
                }
            }
            return;
        }
        else if (plugin.settings.isFieldType(placedblock) && plugin.pm.hasPermission(player, "preciousstones.benefit.create.forcefield"))
        {
            Field conflictfield = plugin.ffm.fieldConflicts(placedblock, player);

            if (conflictfield != null)
            {
                event.setCancelled(true);
                plugin.cm.warnConflictFF(player, placedblock, conflictfield);
            }
            else
            {
                if (plugin.upm.touchingUnprotectableBlock(placedblock))
                {
                    if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                    {
                        if (plugin.ffm.add(placedblock, player))
                        {
                            plugin.cm.notifyBypassTouchingUnprotectable(player, placedblock);
                        }
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.cm.warnPlaceTouchingUnprotectable(player, placedblock);
                    }
                    return;
                }

                FieldSettings fieldsettings = plugin.settings.getFieldSettings(placedblock.getTypeId());

                if (fieldsettings.preventUnprotectable)
                {
                    Block foundblock = plugin.upm.existsUnprotectableBlock(placedblock);

                    if (foundblock != null)
                    {
                        if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                        {
                            if (plugin.ffm.add(placedblock, player))
                            {
                                plugin.cm.notifyBypassFieldInUnprotectable(player, foundblock, placedblock);
                            }
                        }
                        else
                        {
                            event.setCancelled(true);
                            plugin.cm.warnPlaceFieldInUnprotectable(player, foundblock, placedblock);
                        }
                        return;
                    }
                }

                if (plugin.ffm.add(placedblock, player))
                {
                    if (plugin.ffm.isBreakable(placedblock))
                    {
                        plugin.cm.notifyPlaceBreakableFF(player, placedblock);
                    }
                    else
                    {
                        plugin.cm.notifyPlaceFF(player, placedblock);
                    }
                }
            }
            return;
        }
        else if (plugin.settings.isUnprotectableType(placedblock.getTypeId()))
        {
            Block unbreakableblock = plugin.um.touchingUnbrakableBlock(placedblock);

            if (unbreakableblock != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.cm.notifyBypassUnprotectableTouching(player, placedblock, unbreakableblock);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPlaceUnprotectableTouching(player, placedblock, unbreakableblock);
                    return;
                }
            }

            Block fieldblock = plugin.ffm.touchingFieldBlock(placedblock);

            if (fieldblock != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.cm.notifyBypassUnprotectableTouching(player, placedblock, fieldblock);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPlaceUnprotectableTouching(player, placedblock, fieldblock);
                    return;
                }
            }

            Field field = plugin.ffm.isUprotectableBlockField(placedblock);

            if (field != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.cm.notifyBypassPlaceUnprotectableInField(player, placedblock, field);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPlaceUnprotectableInField(player, placedblock, field);
                }
            }
        }

        Field field = plugin.ffm.isPlaceProtected(placedblock, player);

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
                else if (plugin.stm.isRival(player.getName(), field.getOwner()) && plugin.stm.isAnyOnline(field.getOwner()))
                {
                    // do nothing
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPlace(player, placedblock, field);
                }
            }
        }

        if (placedblock.getType().equals(Material.LAVA))
        {
            Field nopvpfield = plugin.ffm.isPvPProtected(player);

            if (nopvpfield != null)
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.pvp"))
                {
                    plugin.cm.warnBypassPvPLavaPlace(player, nopvpfield);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPvPLavaPlace(player, nopvpfield);
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockDamage(BlockDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Block scopedBlock = event.getBlock();
        Player player = event.getPlayer();
        ItemStack is = player.getItemInHand();

        if (scopedBlock == null || is == null || player == null)
        {
            return;
        }

        Material materialInHand = is.getType();

        if (plugin.settings.isFieldType(materialInHand) && plugin.pm.hasPermission(player, "preciousstones.benefit.scoping"))
        {
            if (!plugin.plm.isDisabled(player))
            {
                HashSet<Field> touching = plugin.ffm.getTouchingFields(scopedBlock, materialInHand);
                plugin.cm.printTouchingFields(player, touching);
            }
        }
    }
}
