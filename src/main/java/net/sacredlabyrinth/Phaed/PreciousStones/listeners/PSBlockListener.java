package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;
import java.util.Set;

/**
 * PreciousStones block listener
 *
 * @author Phaed
 */
public class PSBlockListener implements Listener
{
    private PreciousStones plugin;

    /**
     *
     */
    public PSBlockListener()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFade(BlockFadeEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        //If the block is going to disappear because it's a field.(leaves, ice, etc)
        //Cancel the event

        if (plugin.getForceFieldManager().isField(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Block source = event.getBlock();
        Block destination = event.getToBlock();

        if (plugin.getSettingsManager().isBlacklistedWorld(source.getWorld()))
        {
            return;
        }

        if (Helper.isSameBlock(source.getLocation(), destination.getLocation()))
        {
            return;
        }

        // if the block itself is a field cancel its flow
        // this allows for water and lava pstones

        Field blockField = plugin.getForceFieldManager().getField(source);

        if (blockField != null)
        {
            event.setCancelled(true);
        }

        // if the destination area is not protected, don't bother

        Field destField = plugin.getForceFieldManager().getEnabledSourceField(destination.getLocation(), FieldFlag.PREVENT_FLOW);

        if (destField == null)
        {
            return;
        }

        // if the source is outside protection, or if its protected by a different owner, then block the water

        Field sourceField = plugin.getForceFieldManager().getEnabledSourceField(source.getLocation(), FieldFlag.PREVENT_FLOW);

        if (sourceField == null || !sourceField.getOwner().equalsIgnoreCase(destField.getOwner()))
        {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
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

        if (player != null)
        {
            plugin.getSnitchManager().recordSnitchIgnite(player, block);
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_FIRE);

        if (field != null)
        {
            if (player == null)
            {
                event.setCancelled(true);
            }
            else
            {
                if (FieldFlag.PREVENT_FIRE.applies(field, player))
                {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().warnFire(player, block, field);
                }
            }
        }

        if (player != null)
        {
            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TELEPORT_ON_FIRE);

            if (field != null)
            {
                if (FieldFlag.TELEPORT_ON_FIRE.applies(field, player))
                {
                    event.setCancelled(true);
                    plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceFire");
                }
            }
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
    {
        Block block = plugin.getForceFieldManager().touchingFieldBlock(event.getBlock());

        if (block == null)
        {
            return;
        }

        final Field field = plugin.getForceFieldManager().getField(block);

        // only act on fields that are being touched by this wire

        if (!plugin.getForceFieldManager().powersField(field, event.getBlock()))
        {
            return;
        }

        // only act on fields wth the enable-with-redstone flag

        if (!field.hasFlag(FieldFlag.ENABLE_WITH_REDSTONE))
        {
            return;
        }

        // enable/disable the field (except translocation fields)

        if (event.getNewCurrent() > event.getOldCurrent())
        {
            if (field.isDisabled())
            {
                field.setDisabled(false);
            }

            PreciousStones.debug("redstone enabled");
        }
        else if (event.getNewCurrent() == 0)
        {
            if (!field.isDisabled())
            {
                if (!field.isRented())
                {
                    field.setDisabled(true);
                }
            }

            PreciousStones.debug("redstone disabled");
        }

        // after this point we only care about actionable fields

        if (!field.getSettings().hasDefaultFlag(FieldFlag.LAUNCH) &&
                !field.getSettings().hasDefaultFlag(FieldFlag.CANNON) &&
                !field.getSettings().hasDefaultFlag(FieldFlag.POTIONS) &&
                !field.getSettings().hasDefaultFlag(FieldFlag.TRANSLOCATION) &&
                !field.getSettings().hasDefaultFlag(FieldFlag.CONFISCATE_ITEMS))
        {
            return;
        }

        // toggle translocation fields

        if (field.hasFlag(FieldFlag.TRANSLOCATION) && !field.isTranslocating())
        {
            if (field.isNamed())
            {
                if (field.isDisabled())
                {
                    // only apply via redstone if were not over the max

                    if (!field.isOverRedstoneMax())
                    {
                        plugin.getTranslocationManager().applyTranslocation(field);
                        field.setDisabled(false);
                        field.dirtyFlags();
                    }
                }
                else
                {
                    plugin.getTranslocationManager().clearTranslocation(field);
                    field.setDisabled(true);
                    field.dirtyFlags();
                }
            }
            return;
        }

        // act on the players inside the fields after enabling/disaling

        Set<Player> inhabitants = plugin.getForceFieldManager().getFieldInhabitants(field);

        for (final Player player : inhabitants)
        {
            if (player != null)
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    public void run()
                    {
                        if (field.isDisabled())
                        {
                            if (FieldFlag.POTIONS.applies(field, player))
                            {
                                plugin.getPotionManager().removePotions(player, field);
                            }

                            if (FieldFlag.CONFISCATE_ITEMS.applies(field, player))
                            {
                                plugin.getConfiscationManager().returnItems(player);
                            }
                        }
                        else
                        {
                            if (FieldFlag.LAUNCH.applies(field, player))
                            {
                                plugin.getVelocityManager().launchPlayer(player, field);
                            }

                            if (FieldFlag.CANNON.applies(field, player))
                            {
                                plugin.getVelocityManager().shootPlayer(player, field);
                            }

                            if (FieldFlag.POTIONS.applies(field, player))
                            {
                                plugin.getPotionManager().applyPotions(player, field);
                            }

                            if (FieldFlag.CONFISCATE_ITEMS.applies(field, player))
                            {
                                plugin.getConfiscationManager().confiscateItems(field, player);
                            }
                        }
                    }
                }, 0);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null || player == null)
        {
            return;
        }

        handleBreak(player, block, event);
    }

    private void handleBreak(Player player, Block block, Cancellable event)
    {
        // do not allow break of non-field blocks during cuboid definition

        if (plugin.getCuboidManager().hasOpenCuboid(player) && !plugin.getForceFieldManager().isField(block))
        {
            event.setCancelled(true);
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(block.getWorld()))
        {
            return;
        }

        if (plugin.getSettingsManager().isBypassBlock(block))
        {
            return;
        }

        plugin.getSnitchManager().recordSnitchBlockBreak(player, block);

        // -------------------------------------------------------------------------------- prevent destroy everywhere

        if (plugin.getSettingsManager().isPreventDestroyEverywhere(block.getWorld().getName()) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
        {
            boolean isAllowBlock = false;

            Field field = plugin.getForceFieldManager().getField(block);

            if (field != null)
            {
                if (field.hasFlag(FieldFlag.ALLOW_DESTROY) || field.hasFlag(FieldFlag.ALLOW_PLACE))
                {
                    isAllowBlock = true;
                }
            }

            if (!isAllowBlock)
            {
                field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.ALLOW_DESTROY);

                if (field != null)
                {
                    boolean applies = FieldFlag.ALLOW_DESTROY.applies(field, player);

                    if (!applies)
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------- breaking a field sign

        if (SignHelper.cannotBreakFieldSign(block, player))
        {
            event.setCancelled(true);
            return;
        }

        // -------------------------------------------------------------------------------- breaking a field

        if (plugin.getForceFieldManager().isField(block))
        {
            Field field = plugin.getForceFieldManager().getField(block);

            if (breakingFieldChecks(player, block, field, event))
            {
                return;
            }
        }
        else if (plugin.getUnbreakableManager().isUnbreakable(block))
        {
            // ------------------------------------------------------------------------------- breaking an unbreakable

            if (plugin.getUnbreakableManager().isOwner(block, player.getName()))
            {
                plugin.getCommunicationManager().notifyDestroyU(player, block);
                plugin.getUnbreakableManager().release(block);
            }
            else if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.unbreakable"))
            {
                plugin.getCommunicationManager().notifyBypassDestroyU(player, block);
                plugin.getUnbreakableManager().release(block);
            }
            else
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnDestroyU(player, block);
            }
            return;
        }

        // -------------------------------------------------------------------------------- breaking a prevent destroy area

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field != null)
        {
            if (!field.getSettings().inDestroyBlacklist(block))
            {
                if (FieldFlag.PREVENT_DESTROY.applies(field, player))
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                    {
                        plugin.getCommunicationManager().notifyBypassDestroy(player, block, field);
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnDestroyArea(player, block, field);
                        return;
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- breaking a grief revert area

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            if (!plugin.getPermissionsManager().lwcProtected(player, block))
            {
                if (!plugin.getPermissionsManager().locketteProtected(player, block))
                {
                    if (FieldFlag.GRIEF_REVERT.applies(field, player))
                    {
                        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy") || field.getSettings().canGrief(block.getTypeId()))
                        {
                            PreciousStones.debug("bypassed");

                            if (field.getSettings().canGrief(block.getTypeId()))
                            {
                                PreciousStones.debug("can-grief");
                            }

                            plugin.getCommunicationManager().notifyBypassDestroy(player, block, field);
                            plugin.getStorageManager().deleteBlockGrief(block);
                            return;
                        }
                        else
                        {
                            PreciousStones.debug("is grief");

                            if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                            {
                                PreciousStones.debug("adding block");

                                boolean clear = !field.hasFlag(FieldFlag.GRIEF_REVERT_DROP);

                                plugin.getGriefUndoManager().addBlock(field, block, clear);
                                plugin.getStorageManager().offerGrief(field);

                                if (clear)
                                {
                                    PreciousStones.debug("cleared");
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                    else
                    {
                        plugin.getStorageManager().deleteBlockGrief(block);
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- breaking inside a translocation revert area

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

        if (field != null)
        {
            if (field.isNamed())
            {
                plugin.getTranslocationManager().removeBlock(field, block);
                plugin.getTranslocationManager().flashFieldBlock(field, player);
            }
        }

        // -------------------------------------------------------------------------------- breaking inside a teleport field

        field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_ON_BLOCK_BREAK);

        if (field != null)
        {
            if (FieldFlag.TELEPORT_ON_BLOCK_BREAK.applies(field, player))
            {
                event.setCancelled(true);
                plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceBreak");
            }
        }

        // -------------------------------------------------------------------------------- about to break a field sign, clear it

        FieldSign s = SignHelper.getFieldSign(block);

        if (s != null)
        {
            Field f = s.getField();

            if (f != null)
            {
                if (!s.isBuyable())
                {
                    f.clearRents();
                }
            }
        }
    }

    private boolean breakingFieldChecks(Player player, Block block, Field field, Cancellable event)
    {
        if (field.isRented() || field.isBought())
        {
            ChatBlock.send(player, "fieldSignCannotDestroy");
            event.setCancelled(true);
            return true;
        }

        field.unHide();

        // now that it's unhidden it should pass this check, if not then no block for you

        if (!plugin.getSettingsManager().isFieldType(block))
        {
            return false;
        }

        // cancel cuboid if still drawing it

        if (plugin.getCuboidManager().isOpenCuboidField(player, block))
        {
            plugin.getCuboidManager().cancelOpenCuboid(player, block);
            removeAndRefundBlock(player, block, field, event);
            return true;
        }

        if (field.isOwner(player.getName()))
        {
            plugin.getCommunicationManager().notifyDestroyFF(player, block);
        }
        else if (field.hasFlag(FieldFlag.BREAKABLE))
        {
            plugin.getCommunicationManager().notifyDestroyBreakableFF(player, block);
        }
        else if (field.hasFlag(FieldFlag.ALLOWED_CAN_BREAK))
        {
            if (plugin.getForceFieldManager().isAllowed(block, player.getName()))
            {
                plugin.getCommunicationManager().notifyDestroyOthersFF(player, block);
            }
            else
            {
                event.setCancelled(true);
                return true;
            }
        }
        else if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.forcefield"))
        {
            plugin.getCommunicationManager().notifyBypassDestroyFF(player, block);
        }
        else
        {
            plugin.getCommunicationManager().warnDestroyFF(player, block);
            event.setCancelled(true);
            return true;
        }

        if (plugin.getForceFieldManager().hasSubFields(field))
        {
            ChatBlock.send(player, "cannotRemoveWithSubplots");
            event.setCancelled(true);
            return true;
        }

        // -------------------------------------------------------------------------------- breaking a transloctor block

        if (field.hasFlag(FieldFlag.TRANSLOCATION))
        {
            if (field.isNamed())
            {
                int count = plugin.getStorageManager().appliedTranslocationCount(field);

                if (count > 0)
                {
                    plugin.getTranslocationManager().clearTranslocation(field);
                }
            }
        }

        removeAndRefundBlock(player, block, field, event);
        return true;
    }

    private void removeAndRefundBlock(Player player, Block block, Field field, Cancellable event)
    {
        PreciousStones.debug("releasing field");

        if (field.hasFlag(FieldFlag.SINGLE_USE))
        {
            event.setCancelled(true);
            plugin.getForceFieldManager().releaseWipe(block);
        }
        else
        {
            if (block.getTypeId() == field.getTypeId())
            {
                if (plugin.getSettingsManager().isFragileBlock(block))
                {
                    PreciousStones.debug("fragile block broken");
                    event.setCancelled(true);
                    plugin.getForceFieldManager().release(block);
                }
                else
                {
                    PreciousStones.debug("silent break");
                    event.setCancelled(false);
                    plugin.getForceFieldManager().releaseNoDrop(block);
                }
            }
            else
            {
                if (plugin.getSettingsManager().isFragileBlock(new BlockTypeEntry(field.getTypeId(), field.getData())))
                {
                    PreciousStones.debug("fragile block broken");
                    plugin.getForceFieldManager().releaseNoClean(field);
                }
                else
                {
                    PreciousStones.debug("silent break");
                    plugin.getForceFieldManager().releaseNoDrop(block);
                }
            }
        }
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase"))
        {
            if (!plugin.getSettingsManager().isNoRefunds())
            {
                int refund = field.getSettings().getRefund();

                if (refund > -1)
                {
                    // refund the block, accounts for parent/child relationships

                    if (field.isChild() || field.isParent())
                    {
                        Field parent = field;

                        if (field.isChild())
                        {
                            parent = field.getParent();
                        }

                        plugin.getForceFieldManager().refund(player, refund);

                        for (Field child : parent.getChildren())
                        {
                            refund = child.getSettings().getRefund();

                            plugin.getForceFieldManager().refund(player, refund);
                        }
                    }
                    else
                    {
                        plugin.getForceFieldManager().refund(player, refund);
                    }
                }
            }
        }

        plugin.getVisualizationManager().revert(player);
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (block == null || player == null)
        {
            return;
        }

        handlePlace(player, block, event);
    }

    private void handlePlace(final Player player, final Block block, Cancellable event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(block.getWorld()))
        {
            return;
        }

        if (plugin.getSettingsManager().isBypassBlock(block))
        {
            return;
        }

        // -------------------------------------------------------------------------------------- placing a block on top of a field

        if (event instanceof BlockPlaceEvent)
        {
            BlockPlaceEvent e = (BlockPlaceEvent) event;

            BlockState state = e.getBlockReplacedState();

            Field existingField = plugin.getForceFieldManager().getField(state.getLocation());

            if (existingField != null)
            {
                if (state.getTypeId() > 0)
                {
                    if (!breakingFieldChecks(player, block, existingField, event))
                    {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------------- placing blocks touching a field block that you don't own

        Block fieldBlock = plugin.getForceFieldManager().touchingFieldBlock(block);

        if (fieldBlock != null)
        {
            Field field = plugin.getForceFieldManager().getField(fieldBlock);

            if (field.hasFlag(FieldFlag.ENABLE_WITH_REDSTONE))
            {
                boolean allowed = plugin.getForceFieldManager().isAllowed(field, player.getName());

                if (!allowed)
                {
                    ChatBlock.send(player, "cannotPlaceNextToRedstone");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------------- placing with an open cuboid

        if (plugin.getCuboidManager().hasOpenCuboid(player))
        {
            if (!plugin.getSettingsManager().isFieldType(block))
            {
                event.setCancelled(true);
                return;
            }
            else
            {
                CuboidEntry ce = plugin.getCuboidManager().getOpenCuboid(player);
                FieldSettings fs = plugin.getSettingsManager().getFieldSettings(block);

                if (ce.getField().getSettings().getMixingGroup() != fs.getMixingGroup())
                {
                    event.setCancelled(true);
                    ChatBlock.send(player, "fieldsDontMix");
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------------- prevent place everywhere

        if (plugin.getSettingsManager().isPreventPlaceEverywhere(block.getWorld().getName()) && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
        {
            boolean isAllowBlock = false;

            if (plugin.getSettingsManager().isFieldType(block) && plugin.getSettingsManager().getFieldSettings(block) != null)
            {
                FieldSettings fs = plugin.getSettingsManager().getFieldSettings(block);

                if (fs.hasDefaultFlag(FieldFlag.ALLOW_DESTROY) || fs.hasDefaultFlag(FieldFlag.ALLOW_PLACE))
                {
                    isAllowBlock = true;
                }
            }

            if (!isAllowBlock)
            {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.ALLOW_PLACE);

                if (field != null)
                {
                    boolean applies = FieldFlag.ALLOW_PLACE.applies(field, player);

                    if (!applies)
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------------- placing a field

        boolean isDisabled = false;

        // allow place field if sneaking

        if (plugin.getSettingsManager().isSneakPlaceFields())
        {
            if (!player.isSneaking())
            {
                isDisabled = true;
            }
        }

        // bypass place field if sneaking

        if (plugin.getSettingsManager().isSneakNormalBlock())
        {
            if (player.isSneaking())
            {
                isDisabled = true;
            }
        }

        // allow or bypass field placement based on sneak-to-place flag

        FieldSettings settings = plugin.getSettingsManager().getFieldSettings(block);

        if (settings != null)
        {
            if (settings.hasDefaultFlag(FieldFlag.SNEAK_TO_PLACE))
            {
                isDisabled = !player.isSneaking();
            }
        }

        // if the user has it manually off then disable placing

        if (plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled())
        {
            isDisabled = true;
        }

        // -------------------------------------------------------------------------------------- placing a field

        if (!isDisabled && plugin.getSettingsManager().isFieldType(block) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.create.forcefield"))
        {
            PreciousStones.debug("before check");
            if (placingFieldChecks(player, block, event))
            {
                PreciousStones.debug("after check");
                plugin.getForceFieldManager().add(block, player, (BlockPlaceEvent) event);
            }

            if (event.isCancelled())
            {
                PreciousStones.debug("post check canceleld");
                return;
            }
        }
        else
        {
            // -------------------------------------------------------------------------------------- placing an unbreakable

            if (!isDisabled && plugin.getSettingsManager().isUnbreakableType(block) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.create.unbreakable"))
            {
                Field conflictField = plugin.getForceFieldManager().unbreakableConflicts(block, player);

                if (conflictField != null)
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                    {
                        if (plugin.getUnbreakableManager().add(block, player))
                        {
                            plugin.getCommunicationManager().notifyBypassPlaceU(player, block, conflictField);
                        }
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnConflictU(player, block, conflictField);
                    }
                }
                else
                {
                    if (plugin.getUnprotectableManager().touchingUnprotectableBlock(block))
                    {
                        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                        {
                            if (plugin.getUnbreakableManager().add(block, player))
                            {
                                plugin.getCommunicationManager().notifyBypassTouchingUnprotectable(player, block);
                            }
                        }
                        else
                        {
                            event.setCancelled(true);
                            plugin.getCommunicationManager().warnUnbreakablePlaceTouchingUnprotectable(player, block);
                        }
                    }
                    else
                    {
                        if (plugin.getUnbreakableManager().add(block, player))
                        {
                            plugin.getCommunicationManager().notifyPlaceU(player, block);
                        }
                    }
                }
                return;
            }

            // -------------------------------------------------------------------------------------- placing an unprotectable

            if (plugin.getSettingsManager().isUnprotectableType(block))
            {
                Block unbreakableblock = plugin.getUnbreakableManager().touchingUnbrakableBlock(block);

                if (unbreakableblock != null)
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                    {
                        plugin.getCommunicationManager().notifyUnbreakableBypassUnprotectableTouching(player, block, unbreakableblock);
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnUnbreakablePlaceUnprotectableTouching(player, block, unbreakableblock);
                        return;
                    }
                }

                Block fieldblock = plugin.getForceFieldManager().touchingFieldBlock(block);

                if (fieldblock != null)
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                    {
                        plugin.getCommunicationManager().notifyFieldBypassUnprotectableTouching(player, block, fieldblock);
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnFieldPlaceUnprotectableTouching(player, block, fieldblock);
                        return;
                    }
                }

                Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_UNPROTECTABLE);

                if (field != null)
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                    {
                        plugin.getCommunicationManager().notifyBypassPlaceUnprotectableInField(player, block, field);
                    }
                    else
                    {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnPlaceUnprotectableInField(player, block, field);
                    }
                }

                field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

                if (field != null)
                {
                    ChatBlock.send(player, "noUnbreakableInsideTranslocation");
                    event.setCancelled(true);
                }
            }

            // ------------------------------------------------------------------------------------------- placing a chest next to a field

            if (block.getTypeId() == 54)
            {
                Field field = plugin.getForceFieldManager().getConflictSourceField(block.getLocation(), player.getName(), FieldFlag.ALL);

                boolean conflicted = false;

                if (block.getRelative(BlockFace.EAST).getTypeId() == 54)
                {
                    Field field1 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.EAST).getLocation(), player.getName(), FieldFlag.ALL);

                    if (field1 != null)
                    {
                        if (field == null || field != field1)
                        {
                            conflicted = true;
                        }
                    }
                }

                if (block.getRelative(BlockFace.WEST).getTypeId() == 54)
                {
                    Field field2 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.WEST).getLocation(), player.getName(), FieldFlag.ALL);

                    if (field2 != null)
                    {
                        if (field == null || field != field2)
                        {
                            conflicted = true;
                        }
                    }
                }

                if (block.getRelative(BlockFace.NORTH).getTypeId() == 54)
                {
                    Field field3 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.NORTH).getLocation(), player.getName(), FieldFlag.ALL);

                    if (field3 != null)
                    {
                        if (field == null || field != field3)
                        {
                            conflicted = true;
                        }
                    }
                }

                if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54)
                {
                    Field field4 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.SOUTH).getLocation(), player.getName(), FieldFlag.ALL);

                    if (field4 != null)
                    {
                        if (field == null || field != field4)
                        {
                            conflicted = true;
                        }
                    }
                }

                if (conflicted)
                {
                    ChatBlock.send(player, "noChestNextToField");
                    event.setCancelled(true);
                    return;
                }
            }

            // -------------------------------------------------------------------------------------- placing in a prevent place area

            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

            if (field != null)
            {
                if (!field.getSettings().inPlaceBlacklist(block))
                {
                    if (FieldFlag.PREVENT_PLACE.applies(field, player))
                    {
                        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                        {
                            plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
                        }
                        else
                        {
                            event.setCancelled(true);
                            plugin.getCommunicationManager().warnPlace(player, block, field);
                        }
                    }
                }
            }

            // -------------------------------------------------------------------------------------- placing in a grief revert area

            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

            if (field != null)
            {
                if (FieldFlag.GRIEF_REVERT.applies(field, player))
                {
                    if (field.hasFlag(FieldFlag.PLACE_GRIEF))
                    {
                        if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                        {
                            if (event instanceof BlockPlaceEvent)
                            {
                                BlockPlaceEvent e = (BlockPlaceEvent) event;
                                BlockState blockState = e.getBlockReplacedState();

                                plugin.getGriefUndoManager().addBlock(field, blockState);
                            }

                            plugin.getStorageManager().offerGrief(field);
                        }
                    }
                    else
                    {
                        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                        {
                            plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
                            plugin.getStorageManager().deleteBlockGrief(block);
                        }
                        else
                        {
                            event.setCancelled(true);
                            plugin.getCommunicationManager().warnPlace(player, block, field);
                        }
                    }
                }
                else
                {
                    plugin.getStorageManager().deleteBlockGrief(block);
                }
            }

            // -------------------------------------------------------------------------------------- placing in a translocation area

            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

            if (field != null)
            {
                if (FieldFlag.TRANSLOCATION.applies(field, player))
                {
                    if (field.getSettings().canTranslocate(new BlockTypeEntry(block)))
                    {
                        if (field.getName().length() == 0)
                        {
                            ChatBlock.send(player, "translocatorNameToBegin");
                            event.setCancelled(true);
                            return;
                        }

                        if (field.isOverTranslocationMax(1))
                        {
                            ChatBlock.send(player, "translocationReachedSize");
                            event.setCancelled(true);
                            return;
                        }

                        final Field finalField = field;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                        {
                            public void run()
                            {
                                plugin.getTranslocationManager().addBlock(finalField, block);
                                plugin.getTranslocationManager().flashFieldBlock(finalField, player);
                            }
                        }, 10);
                    }
                }
            }

            // -------------------------------------------------------------------------------- placing inside a teleport field

            field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_ON_BLOCK_PLACE);

            if (field != null)
            {
                if (FieldFlag.TELEPORT_ON_BLOCK_PLACE.applies(field, player))
                {
                    event.setCancelled(true);
                    plugin.getTeleportationManager().teleport(player, field, "teleportAnnouncePlace");
                }
            }
        }

        // --------------------------------------------------------------------------------------

        plugin.getSnitchManager().recordSnitchBlockPlace(player, block);
    }

    private boolean placingFieldChecks(Player player, Block block, Cancellable event)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(block);

        if (fs == null)
        {
            return false;
        }

        // cannot place field in prevent place area

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            if (!field.getSettings().inPlaceBlacklist(block))
            {
                if (FieldFlag.PREVENT_PLACE.applies(field, player))
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                    {
                        plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
                    }
                    else
                    {
                        plugin.getCommunicationManager().warnPlace(player, block, field);
                        event.setCancelled(true);
                        PreciousStones.debug("1");
                        return false;
                    }
                }
            }
        }

        // cannot place a field that conflicts with other fields

        Field conflictField = plugin.getForceFieldManager().fieldConflicts(block, player);

        if (conflictField != null)
        {
            event.setCancelled(true);
            PreciousStones.debug("2");
            plugin.getCommunicationManager().warnConflictFF(player, block, conflictField);
            return false;
        }

        // if not allowed in this world then place as regular block

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.world"))
        {
            if (!fs.allowedWorld(block.getWorld()))
            {
                return false;
            }
        }

        // ensure placement of only those with the required permission, fail silently otherwise

        if (!fs.getRequiredPermission().isEmpty())
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.required-permission"))
            {
                if (!plugin.getPermissionsManager().has(player, fs.getRequiredPermission()))
                {
                    return false;
                }
            }
        }

        // cannot place a field touching an unprotectable block

        if (plugin.getUnprotectableManager().touchingUnprotectableBlock(block))
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
            {
                plugin.getCommunicationManager().warnFieldPlaceTouchingUnprotectable(player, block);
                event.setCancelled(true);
                PreciousStones.debug("3");
                return false;
            }

            plugin.getCommunicationManager().notifyBypassTouchingUnprotectable(player, block);
        }

        // must obey y coord

        if (fs.hasDefaultFlag(FieldFlag.MUST_BE_ABOVE))
        {
            if (block.getLocation().getBlockY() <= fs.getMustBeAbove())
            {
                ChatBlock.send(player, "mustBeAbove", fs.getMustBeAbove());
                event.setCancelled(true);
                PreciousStones.debug("4");
                return false;
            }
        }

        if (fs.hasDefaultFlag(FieldFlag.MUST_BE_BELOW))
        {
            if (block.getLocation().getBlockY() >= fs.getMustBeBelow())
            {
                ChatBlock.send(player, "mustBeBelow", fs.getMustBeBelow());
                event.setCancelled(true);
                PreciousStones.debug("5");
                return false;
            }
        }

        // cannot place a confiscate field below a field or unbreakable

        if (fs.hasDefaultFlag(FieldFlag.CONFISCATE_ITEMS))
        {
            Block north = block.getRelative(BlockFace.NORTH);

            if (plugin.getForceFieldManager().isField(north))
            {
                ChatBlock.send(player, "noConfiscatingBelowField");
                event.setCancelled(true);
                PreciousStones.debug("6");
                return false;
            }

            if (plugin.getUnbreakableManager().isUnbreakable(north))
            {
                ChatBlock.send(player, "noConfiscatingBelowUnbreakable");
                event.setCancelled(true);
                PreciousStones.debug("7");
                return false;
            }
        }

        // cannot place a field that contains players inside of it if no-player-place flag exists

        if (fs.hasDefaultFlag(FieldFlag.NO_PLAYER_PLACE))
        {
            boolean hasPlayers = plugin.getForceFieldManager().fieldTouchesPlayers(block, player);

            if (hasPlayers)
            {
                ChatBlock.send(player, "noFieldNearPlayer");
                event.setCancelled(true);
                PreciousStones.debug("8");
                return false;
            }
        }

        // cannot place a field that contains players inside of worldguard regions

        if (fs.hasDefaultFlag(FieldFlag.WORLDGUARD_REPELLENT))
        {
            if (plugin.getWorldGuardManager().isWGRegion(block))
            {
                ChatBlock.send(player, "noPlaceInWG");
                event.setCancelled(true);
                PreciousStones.debug("9");
                return false;
            }
        }

        // cannot place a field that contains unprotectable blocks inside of it if prevent-unprotectable flag exists

        if (fs.hasDefaultFlag(FieldFlag.PREVENT_UNPROTECTABLE))
        {
            Block foundblock = plugin.getUnprotectableManager().existsUnprotectableBlock(block);

            if (foundblock != null)
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.getCommunicationManager().warnPlaceFieldInUnprotectable(player, foundblock, block);
                    event.setCancelled(true);
                    PreciousStones.debug("10");
                    return false;
                }

                plugin.getCommunicationManager().notifyBypassFieldInUnprotectable(player, foundblock, block);
            }
        }

        // forester blocks need to be placed in fertile lands

        if (fs.hasDefaultFlag(FieldFlag.FORESTER))
        {
            Block floor = block.getRelative(BlockFace.DOWN);

            if (!fs.isFertileType(floor.getTypeId()) && floor.getTypeId() != fs.getGroundBlock())
            {
                ChatBlock.send(player, "foresterNeedsFertile", fs.getTitle());
                return false;
            }
        }

        // ensure placement inside allowed only fields

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.allowed-only-inside"))
        {
            if (fs.hasAllowedOnlyInside())
            {
                List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                boolean allowed = false;

                for (Field surroundingField : fields)
                {
                    if (fs.isAllowedOnlyInside(surroundingField))
                    {
                        allowed = true;
                        break;
                    }
                }

                if (!allowed)
                {
                    ChatBlock.send(player, "fieldInsideAllowedInside", fs.getTitle(), fs.getAllowedOnlyInsideString());
                    event.setCancelled(true);
                    PreciousStones.debug("11");
                    return false;
                }
            }
        }

        // ensure placement outside allowed only outside fields

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.allowed-only-outside"))
        {
            if (fs.hasAllowedOnlyOutside())
            {
                List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

                boolean notAllowed = false;

                for (Field surroundingField : fields)
                {
                    if (fs.isAllowedOnlyOutside(surroundingField))
                    {
                        notAllowed = true;
                        break;
                    }
                }

                if (notAllowed)
                {
                    ChatBlock.send(player, "fieldOutsideAllowedOutside", fs.getTitle(), fs.getAllowedOnlyOutsideString());
                    event.setCancelled(true);
                    PreciousStones.debug("12");
                    return false;
                }
            }
        }

        // cannot place field in translocation fields

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

        if (field != null)
        {
            ChatBlock.send(player, "translocationNoFields");
            event.setCancelled(true);
            PreciousStones.debug("13");
            return false;
        }

        // cannot place field in grief revert fields

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            ChatBlock.send(player, "translocationNoFields");
            event.setCancelled(true);
            PreciousStones.debug("14");
            return false;
        }

        return true;
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent event)
    {
        if (event.getBlock() == null)
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getBlock().getLocation(), FieldFlag.NO_GROWTH);

        if (field != null)
        {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        Block piston = event.getBlock();

        if (plugin.getSettingsManager().isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field pistonField = plugin.getForceFieldManager().getEnabledSourceField(piston.getLocation(), FieldFlag.PREVENT_DESTROY);

        List<Block> blocks = event.getBlocks();

        for (Block block : blocks)
        {
            if (SignHelper.cannotBreakFieldSign(block, null))
            {
                event.setCancelled(true);
                PreciousStones.debug("Cancelling field sign move");
                return;
            }

            if (plugin.getForceFieldManager().isField(block))
            {
                PreciousStones.debug("Cancelling field move");
                event.setCancelled(true);
                return;
            }

            if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
            {
                event.setCancelled(true);
                PreciousStones.debug("Cancelling unbreakable move");
                return;
            }

            Field blockField = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (pistonField != null && blockField != null)
            {
                if (blockField.isAllowed(pistonField.getOwner()))
                {
                    continue;
                }
            }

            if (blockField != null)
            {
                if (!blockField.getSettings().inDestroyBlacklist(block))
                {
                    event.setCancelled(true);
                    PreciousStones.debug("Cancelling field conflict move");
                    return;
                }
            }
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        boolean unprotected = false;

        Block piston = event.getBlock();

        if (plugin.getSettingsManager().isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(piston.getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field == null)
        {
            unprotected = true;
        }

        // prevent piston from moving a field or unbreakable block

        Block block = piston.getRelative(event.getDirection()).getRelative(event.getDirection());

        if (SignHelper.cannotBreakFieldSign(block, null))
        {
            event.setCancelled(true);
            return;
        }

        if (plugin.getForceFieldManager().isField(block))
        {
            event.setCancelled(true);
            return;
        }
        if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
        {
            event.setCancelled(true);
            return;
        }

        if (unprotected)
        {
            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (field != null)
            {
                if (!field.getSettings().inDestroyBlacklist(block))
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event)
    {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        String[] lines = event.getLines();

        final FieldSign s = new FieldSign(block, lines, player);

        if (s.isValid())
        {
            Block fieldBlock = SignHelper.getAttachedBlock(block);
            FieldSign attachedFieldSign = SignHelper.getAttachedFieldSign(fieldBlock);

            if (attachedFieldSign != null)
            {
                ChatBlock.send(player, "fieldSignOnlyOne");
                event.setCancelled(true);
                return;
            }

            event.setLine(0, ChatColor.BLACK + "" + ChatColor.BOLD + ChatColor.stripColor(lines[0]));

            if (s.isRentable())
            {
                ChatBlock.send(player, "fieldSignRentCreated");
            }

            if (s.isBuyable())
            {
                ChatBlock.send(player, "fieldSignBuyCreated");
            }
        }
        else
        {
            if (s.isFieldSign())
            {
                if (s.foundField())
                {
                    ChatBlock.send(player, "fieldSignBadFormat");
                }
                else
                {
                    ChatBlock.send(player, "fieldSignMustBeOnField");
                }

                if (s.isNoEconomy())
                {
                    ChatBlock.send(player, "fieldSignNoEco");
                }

                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpenEvent(InventoryOpenEvent event)
    {
        if (event.getInventory() == null || !(event.getPlayer() instanceof Player))
        {
            return;
        }

        if (!event.getInventory().getType().equals(InventoryType.PLAYER))
        {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getPlayer().getLocation(), FieldFlag.PROTECT_INVENTORIES);

            if (field != null)
            {
                Player player = (Player) event.getPlayer();

                if (FieldFlag.PROTECT_INVENTORIES.applies(field, player))
                {
                    event.setCancelled(true);
                    ChatBlock.send(player, "inventoryDeny");
                }
            }
        }
    }
}


