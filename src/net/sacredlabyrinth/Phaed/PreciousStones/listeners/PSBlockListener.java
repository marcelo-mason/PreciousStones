package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashSet;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;

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

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPhysicsEvent;

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
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
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

        if (false)
        {
            if (!plugin.tm.isTaggedArea(new ChunkVec(event.getToBlock().getChunk())))
            {
                return;
            }

            Block block = event.getBlock();
            Block toBlock = event.getToBlock();

            DebugTimer dt = new DebugTimer("onBlockFromTo");

            // only capture water and lava movement

            if (!block.getType().equals(Material.WATER) && !block.getType().equals(Material.LAVA))
            {
                return;
            }

            Field to = plugin.ffm.isFlowProtected(toBlock.getLocation());
            Field from = plugin.ffm.isFlowProtected(block.getLocation());

            if (to == null)
            {
                return;
            }

            if (from == null)
            {
                Block b = block.getFace(event.getFace());

                if (block.getType().equals(Material.WATER))
                {
                    if ((block.getType().equals(Material.WATER) || block.getType().equals(Material.STATIONARY_WATER)) && (b.getData() > 0))
                    {
                        b.setType(Material.AIR);
                    }
                }
                else
                {
                    if ((block.getType().equals(Material.LAVA) || block.getType().equals(Material.STATIONARY_LAVA)) && (b.getData() > 0))
                    {
                        b.setType(Material.AIR);
                    }
                }

                event.setCancelled(true);
            }

            if (plugin.settings.debug)
            {
                dt.logProcessTime();
            }
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

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlock().getChunk())))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockIgnite");

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null)
        {
            return;
        }

        if (player != null)
        {
            plugin.snm.recordSnitchIgnite(player, block);
        }

        Field field = plugin.ffm.isFireProtected(block.getLocation());

        if (field != null)
        {
            if (player == null || !plugin.ffm.isAllowed(field, player.getName()))
            {
                event.setCancelled(true);

                if (player != null)
                {
                    plugin.cm.warnFire(player, field);
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
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
    {
        if (event.getNewCurrent() <= event.getOldCurrent())
        {
            return;
        }

        Block redstoneblock = event.getBlock();

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlock().getChunk())))
        {
            return;
        }

        BlockFace[] faces =
        {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : faces)
        {
            Block fieldblock = redstoneblock.getRelative(face);

            if (plugin.settings.isFieldType(fieldblock) && plugin.ffm.isField(fieldblock))
            {
                Field field = plugin.ffm.getField(fieldblock);
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                if (fieldsettings == null)
                {
                    plugin.ffm.queueRelease(field);
                    return;
                }

                if (fieldsettings.cannon)
                {
                    HashSet<String> players = plugin.em.getInhabitants(field);

                    for (String pl : players)
                    {
                        Player player = plugin.helper.matchSinglePlayer(pl);

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
                        Player player = plugin.helper.matchSinglePlayer(pl);

                        if (player != null)
                        {
                            plugin.vm.launchPlayer(player, field);
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

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlock().getChunk())))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockBreak");

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

        plugin.snm.recordSnitchBlockBreak(player, brokenBlock);

        if (plugin.ffm.isField(brokenBlock))
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(brokenBlock.getTypeId());

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(brokenBlock);
                return;
            }

            if (plugin.ffm.isBreakable(brokenBlock))
            {
                plugin.cm.notifyDestroyBreakableFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fieldsettings.price > 0)
                {
                    plugin.ffm.refund(player, fieldsettings.price);
                }
            }
            else if (plugin.ffm.isOwner(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fieldsettings.price > 0)
                {
                    plugin.ffm.refund(player, fieldsettings.price);
                }
            }
            else if (plugin.settings.allowedCanBreakPstones && plugin.ffm.isAllowed(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyOthersFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fieldsettings.price > 0)
                {
                    plugin.ffm.refund(player, fieldsettings.price);
                }
            }
            else if (plugin.pm.hasPermission(player, "preciousstones.bypass.forcefield"))
            {
                plugin.cm.notifyBypassDestroyFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnDestroyFF(player, brokenBlock);
            }
            return;
        }
        else if (plugin.um.isUnbreakable(brokenBlock))
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
                event.setCancelled(true);
                plugin.cm.warnDestroyU(player, brokenBlock);
            }
            return;
        }

        // --------------------------------------------------------------------------------

        Field field = plugin.ffm.isDestroyProtected(brokenBlock.getLocation(), player);

        if (field != null)
        {

            if (plugin.pm.hasPermission(player, "preciousstones.bypass.destroy"))
            {
                plugin.cm.notifyBypassDestroy(player, brokenBlock, field);
                return;
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnDestroyArea(player, brokenBlock, field);
                return;
            }
        }

        field = plugin.ffm.isGriefProtected(brokenBlock.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.destroy"))
            {
                plugin.cm.notifyBypassDestroy(player, brokenBlock, field);
                return;
            }
            else
            {
                if (!plugin.settings.isGriefUndoBlackListType(brokenBlock.getTypeId()))
                {
                    event.setCancelled(true);
                    plugin.gum.addBlock(field, brokenBlock);
                    plugin.sm.offerGrief(field);
                    brokenBlock.setTypeId(0);
                    return;
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
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockPlace");

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
                Block pistonblock = plugin.ffm.getPistonConflictReverse(placedblock, player);

                if (pistonblock != null)
                {
                    event.setCancelled(true);
                    plugin.cm.warnConflictPistonRU(player, pistonblock, placedblock);
                    return;
                }

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
                Block pistonblock = plugin.ffm.getPistonConflictReverse(placedblock, player);

                if (pistonblock != null)
                {
                    event.setCancelled(true);
                    plugin.cm.warnConflictPistonRFF(player, pistonblock, placedblock);
                    return;
                }

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

                if (fieldsettings == null)
                {
                    return;
                }

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

                if (fieldsettings.forester || fieldsettings.foresterShrubs)
                {
                    Block floor = placedblock.getRelative(BlockFace.DOWN);

                    if (!plugin.settings.isFertileType(floor.getTypeId()) && floor.getTypeId() != 2)
                    {
                        player.sendMessage(ChatColor.AQUA + Helper.capitalize(fieldsettings.title) + " blocks must be placed of fertile land to activate");
                        return;
                    }
                }

                if (fieldsettings.price == 0 || plugin.ffm.purchase(player, fieldsettings.price))
                {
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

            Field field = plugin.ffm.isUprotectableBlockField(placedblock.getLocation());

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
        else if (placedblock.getType().equals(Material.PISTON_BASE) || placedblock.getType().equals(Material.PISTON_STICKY_BASE))
        {
            Field field = plugin.ffm.getPistonConflict(placedblock, player);

            if (field != null)
            {
                event.setCancelled(true);
                plugin.cm.warnConflictPistonFF(player, placedblock, field);
            }

            Unbreakable ub = plugin.um.getPistonConflict(placedblock, player);

            if (ub != null)
            {
                event.setCancelled(true);
                plugin.cm.warnConflictPistonU(player, placedblock, ub);
            }
        }

        // -------------------------------------------------------------------------------------------

        Field field = plugin.ffm.isPlaceProtected(placedblock.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
                plugin.cm.warnPlace(player, placedblock, field);
            }
        }

        field = plugin.ffm.isGriefProtected(placedblock.getLocation());

        if (field != null)
        {
            if (plugin.ffm.isAllowed(field, player.getName()))
            {
                plugin.sm.deleteBlockGrief(placedblock);
            }
            else
            {
                if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
                {
                    plugin.cm.notifyBypassPlace(player, field);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.cm.warnPlace(player, placedblock, field);
                    return;
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
    public void onBlockDamage(BlockDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getBlock().getChunk())))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockDamage");

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

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }
}
