package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

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
            Block block = event.getBlock();
            Block toBlock = event.getToBlock();

            if (plugin.settings.isBlacklistedWorld(block.getWorld()))
            {
                return;
            }

            // only capture water and lava movement

            if (!block.getType().equals(Material.WATER) && !block.getType().equals(Material.LAVA))
            {
                return;
            }

            if (plugin.ffm.isFlowProtected(toBlock.getLocation()))
            {
                return;
            }

            DebugTimer dt = new DebugTimer("onBlockFromTo");

            if (plugin.ffm.isFlowProtected(block.getLocation()))
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

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null)
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockIgnite");

        if (player != null)
        {
            plugin.snm.recordSnitchIgnite(player, block);
        }

        Field field = plugin.ffm.findFireProtected(block.getLocation());

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

        Map<String, Field> players = plugin.em.getTriggerableEntryPlayers(event.getBlock());

        for (String playerName : players.keySet())
        {
            Player player = plugin.helper.matchSinglePlayer(playerName);

            if (player != null)
            {
                Field field = players.get(playerName);
                FieldSettings fs = field.getSettings();

                if (fs.isLaunch())
                {
                    plugin.vm.launchPlayer(player, field);
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

        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();

        if (brokenBlock == null || player == null)
        {
            return;
        }

        if (plugin.settings.isBlacklistedWorld(brokenBlock.getWorld()))
        {
            return;
        }

        if (plugin.settings.isBypassBlock(brokenBlock))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockBreak");

        plugin.snm.recordSnitchBlockBreak(player, brokenBlock);

        if (plugin.ffm.isField(brokenBlock))
        {
            FieldSettings fs = plugin.settings.getFieldSettings(brokenBlock.getTypeId());

            if (fs == null)
            {
                plugin.ffm.queueRelease(brokenBlock);
                return;
            }

            if (plugin.ffm.isBreakable(brokenBlock))
            {
                plugin.cm.notifyDestroyBreakableFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fs.getPrice() > 0)
                {
                    plugin.ffm.refund(player, fs.getPrice());
                }
            }
            else if (plugin.ffm.isOwner(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fs.getPrice() > 0)
                {
                    plugin.ffm.refund(player, fs.getPrice());
                }
            }
            else if (plugin.settings.allowedCanBreakPstones && plugin.ffm.isAllowed(brokenBlock, player.getName()))
            {
                plugin.cm.notifyDestroyOthersFF(player, brokenBlock);
                plugin.ffm.release(brokenBlock);

                if (fs.getPrice() > 0)
                {
                    plugin.ffm.refund(player, fs.getPrice());
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

        Field field = plugin.ffm.findDestroyProtected(brokenBlock.getLocation(), player);

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

        field = plugin.ffm.findGriefProtected(brokenBlock.getLocation(), player);

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

        Block placedblock = event.getBlock();
        Player player = event.getPlayer();

        if (placedblock == null || player == null)
        {
            return;
        }

        if (plugin.settings.isBlacklistedWorld(placedblock.getWorld()))
        {
            return;
        }

        if (plugin.settings.isBypassBlock(placedblock))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onBlockPlace");

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

                FieldSettings fs = plugin.settings.getFieldSettings(placedblock.getTypeId());

                if (fs == null)
                {
                    return;
                }

                if (fs.isPreventUnprotectable())
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

                if (fs.isForester() || fs.isForesterShrubs())
                {
                    Block floor = placedblock.getRelative(BlockFace.DOWN);

                    if (!plugin.settings.isFertileType(floor.getTypeId()) && floor.getTypeId() != 2)
                    {
                        player.sendMessage(ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " blocks must be placed of fertile land to activate");
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

            Field field = plugin.ffm.findUprotectableBlockField(placedblock.getLocation());

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

        // -------------------------------------------------------------------------------------------

        Field field = plugin.ffm.findPlaceProtected(placedblock.getLocation(), player);

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

        field = plugin.ffm.findGriefProtected(placedblock.getLocation());

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

        DebugTimer dt = new DebugTimer("onBlockDamage");

        Block scopedBlock = event.getBlock();
        Player player = event.getPlayer();
        ItemStack is = player.getItemInHand();

        if (scopedBlock == null || is == null || player == null)
        {
            return;
        }

        if (plugin.settings.isBlacklistedWorld(scopedBlock.getWorld()))
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

    /**
     *
     * @param event
     */
    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        boolean unprotected = false;

        Block piston = event.getBlock();

        if (plugin.settings.isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field field = plugin.ffm.findDestroyProtected(piston.getLocation());

        if (field == null)
        {
            unprotected = true;
        }

        List<Block> blocks = event.getBlocks();

        for (Block block : blocks)
        {
            if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
            {
                event.setCancelled(true);
            }
            if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
            {
                event.setCancelled(true);
            }
            if (unprotected)
            {
                field = plugin.ffm.findDestroyProtected(block.getLocation());

                if (field != null)
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        boolean unprotected = false;

        Block piston = event.getBlock();

        if (plugin.settings.isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field field = plugin.ffm.findDestroyProtected(piston.getLocation());

        if (field == null)
        {
            unprotected = true;
        }

        Block block = piston.getRelative(event.getDirection()).getRelative(event.getDirection());

        if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
        {
            event.setCancelled(true);
        }
        if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
        {
            event.setCancelled(true);
        }

        if (unprotected)
        {
            field = plugin.ffm.findDestroyProtected(block.getLocation());

            if (field != null)
            {
                event.setCancelled(true);
            }
        }
    }
}
