package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import java.util.List;
import java.util.Map;

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
        Block affectedBlock = event.getBlock();

        //If the block is going to disappear because it's a field.(leaves, ice, etc)
        //Cancel the event

        if (plugin.getForceFieldManager().isField(affectedBlock))
        {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        DebugTimer dt = new DebugTimer("onBlockFromTo");

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

        Field destField = plugin.getForceFieldManager().getSourceField(destination.getLocation(), FieldFlag.PREVENT_FLOW);

        if (destField == null)
        {
            return;
        }

        Field sourceField = plugin.getForceFieldManager().getSourceField(source.getLocation(), FieldFlag.PREVENT_FLOW);

        if (sourceField == null || !sourceField.getOwner().equalsIgnoreCase(destField.getOwner()))
        {
            event.setCancelled(true);
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
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

        DebugTimer dt = new DebugTimer("onBlockIgnite");

        if (player != null)
        {
            plugin.getSnitchManager().recordSnitchIgnite(player, block);
        }

        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_FIRE);

        if (field != null)
        {
            if (player == null || !plugin.getForceFieldManager().isApplyToAllowed(field, player.getName()) || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                event.setCancelled(true);

                if (player != null)
                {
                    plugin.getCommunicationManager().warnFire(player, block, field);
                }
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
    {
        if (event.getNewCurrent() <= event.getOldCurrent())
        {
            return;
        }

        Map<String, Field> players = plugin.getEntryManager().getTriggerableEntryPlayers(event.getBlock());

        for (String playerName : players.keySet())
        {
            Player player = Helper.matchSinglePlayer(playerName);

            if (player != null)
            {
                Field field = players.get(playerName);

                if (field.hasFlag(FieldFlag.LAUNCH))
                {
                    plugin.getVelocityManager().launchPlayer(player, field);
                }

                if (field.hasFlag(FieldFlag.CANNON))
                {
                    plugin.getVelocityManager().shootPlayer(player, field);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakMonitor(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null || player == null)
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.ALLOW_DESTROY);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                event.setCancelled(false);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isPreventDestroyEverywhere())
        {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null || player == null)
        {
            return;
        }

        // do not allow break of non-field blocks during cuboid definition

        if (plugin.getCuboidManager().hasOpenCuboid(player) && !plugin.getSettingsManager().isFieldType(block))
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

        DebugTimer dt = new DebugTimer("onBlockBreak");

        plugin.getSnitchManager().recordSnitchBlockBreak(player, block);

        if (plugin.getForceFieldManager().isField(block))
        {
            Field field = plugin.getForceFieldManager().getField(block);
            FieldSettings fs = field.getSettings();

            if (field == null)
            {
                return;
            }

            boolean release = false;

            if (plugin.getCuboidManager().isOpenCuboidField(player, block))
            {
                plugin.getCuboidManager().cancelOpenCuboid(player, block);
                release = true;
            }

            if (release)
            {
                plugin.getForceFieldManager().release(block);

                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase"))
                {
                    if (!plugin.getSettingsManager().isNoRefunds())
                    {
                        if (fs.getPrice() > 0)
                        {
                            plugin.getForceFieldManager().refund(player, fs.getPrice());
                            return;
                        }
                    }
                }
            }

            if (field.isOwner(player.getName()))
            {
                plugin.getCommunicationManager().notifyDestroyFF(player, block);
                release = true;
            }
            else if (field.hasFlag(FieldFlag.BREAKABLE))
            {
                plugin.getCommunicationManager().notifyDestroyBreakableFF(player, block);
                release = true;
            }
            else if (field.hasFlag(FieldFlag.ALLOWED_CAN_BREAK))
            {
                if (plugin.getForceFieldManager().isAllowed(block, player.getName()))
                {
                    plugin.getCommunicationManager().notifyDestroyOthersFF(player, block);
                    release = true;
                }
            }
            else if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.forcefield"))
            {
                plugin.getCommunicationManager().notifyBypassDestroyFF(player, block);
                release = true;
            }
            else
            {
                plugin.getCommunicationManager().warnDestroyFF(player, block);
                event.setCancelled(true);
            }

            if (plugin.getForceFieldManager().hasSubFields(field))
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "Cannot remove fields that have plot-fields inside of it.  You must remove them first before you can remove this field.");
                event.setCancelled(true);
            }

            if (release)
            {
                plugin.getForceFieldManager().release(block);

                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase"))
                {
                    if (!plugin.getSettingsManager().isNoRefunds())
                    {
                        if (fs.getPrice() > 0)
                        {
                            plugin.getForceFieldManager().refund(player, fs.getPrice());
                        }
                    }
                }
            }
            return;
        }
        else if (plugin.getUnbreakableManager().isUnbreakable(block))
        {
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

        // --------------------------------------------------------------------------------

        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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

        field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                {
                    plugin.getCommunicationManager().notifyBypassDestroy(player, block, field);
                    plugin.getStorageManager().deleteBlockGrief(block);
                    return;
                }
                else
                {
                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                    {
                        plugin.getGriefUndoManager().addBlock(field, block);
                        plugin.getStorageManager().offerGrief(field);

                        if (!field.hasFlag(FieldFlag.GRIEF_REVERT_DROP))
                        {
                            block.setTypeId(0);
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

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceMonitor(BlockPlaceEvent event)
    {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null || player == null)
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.ALLOW_PLACE);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                event.setCancelled(false);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isPreventPlaceEverywhere())
        {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null || player == null)
        {
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

        DebugTimer dt = new DebugTimer("onBlockPlace");

        if (plugin.getCuboidManager().hasOpenCuboid(player))
        {
            if (!plugin.getSettingsManager().isFieldType(block))
            {
                event.setCancelled(true);
                return;
            }
            else
            {
                net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry ce = plugin.getCuboidManager().getOpenCuboid(player);
                FieldSettings fs = plugin.getSettingsManager().getFieldSettings(Helper.toRawTypeId(block));

                if (ce.getField().getSettings().getMixingGroup() != fs.getMixingGroup())
                {
                    event.setCancelled(true);
                    ChatBlock.sendMessage(player, ChatColor.RED + "The field type does not mix");
                    return;
                }
            }
        }

        plugin.getSnitchManager().recordSnitchBlockPlace(player, block);

        boolean isDisabled = plugin.getPlayerManager().getPlayerEntry(player.getName()).isDisabled();

        if (plugin.getSettingsManager().isSneakPlaceFields())
        {
            if (player.isSneaking())
            {
                isDisabled = false;
            }
        }

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
        else if (!isDisabled && plugin.getSettingsManager().isFieldType(block) && plugin.getPermissionsManager().has(player, "preciousstones.benefit.create.forcefield"))
        {
            if (plugin.getSettingsManager().isSneakNormalBlock())
            {
                if (player.isSneaking())
                {
                    return;
                }
            }

            Field conflictField = plugin.getForceFieldManager().fieldConflicts(block, player);

            if (conflictField != null)
            {
                event.setCancelled(true);
                plugin.getCommunicationManager().warnConflictFF(player, block, conflictField);
                return;
            }

            if (plugin.getUnprotectableManager().touchingUnprotectableBlock(block))
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                {
                    plugin.getCommunicationManager().warnFieldPlaceTouchingUnprotectable(player, block);
                    event.setCancelled(true);
                    return;
                }

                plugin.getCommunicationManager().notifyBypassTouchingUnprotectable(player, block);
            }

            FieldSettings fs = plugin.getSettingsManager().getFieldSettings(Helper.toRawTypeId(block));

            if (fs == null)
            {
                return;
            }

            if (fs.hasDefaultFlag(FieldFlag.NO_PLAYER_PLACE))
            {
                boolean hasPlayers = plugin.getForceFieldManager().areaHasPlayers(block, player);

                if (hasPlayers)
                {
                    ChatBlock.sendMessage(player, ChatColor.RED + "Cannot place field near players");
                    event.setCancelled(true);
                    return;
                }
            }

            if (fs.hasDefaultFlag(FieldFlag.PREVENT_UNPROTECTABLE))
            {
                Block foundblock = plugin.getUnprotectableManager().existsUnprotectableBlock(block);

                if (foundblock != null)
                {
                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable"))
                    {
                        plugin.getCommunicationManager().warnPlaceFieldInUnprotectable(player, foundblock, block);
                        event.setCancelled(true);
                        return;
                    }

                    plugin.getCommunicationManager().notifyBypassFieldInUnprotectable(player, foundblock, block);
                }
            }

            if (fs.hasDefaultFlag(FieldFlag.FORESTER))
            {
                Block floor = block.getRelative(BlockFace.DOWN);

                if (!fs.isFertileType(floor.getTypeId()) && floor.getTypeId() != fs.getGroundBlock())
                {
                    player.sendMessage(ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " blocks must be placed of fertile land to activate");
                    return;
                }
            }

            // if not allowed in this world then place as regular block

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.world"))
            {
                if (!fs.allowedWorld(block.getWorld()))
                {
                    return;
                }
            }

            // ensure placement of only those with the required permission, fail silently otherwise

            if (fs.getRequiredPermission() != null)
            {
                if (!plugin.getPermissionsManager().has(player, fs.getRequiredPermission()))
                {
                    return;
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
                        ChatBlock.sendMessage(player, ChatColor.RED + Helper.capitalize(fs.getTitle()) + " needs to be be placed inside " + fs.getAllowedOnlyInsideString());
                        event.setCancelled(true);
                        return;
                    }
                }
            }

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
                        ChatBlock.sendMessage(player, ChatColor.RED + Helper.capitalize(fs.getTitle()) + " needs to be be placed outside " + fs.getAllowedOnlyOutsideString());
                        event.setCancelled(true);
                        return;
                    }
                }
            }


            Field field = plugin.getForceFieldManager().add(block, player, event);

            if (field != null)
            {
                //field.generateFence();

                // disable flags

                if (plugin.getSettingsManager().isStartMessagesDisabled())
                {
                    field.disableFlag("welcome-message");
                    field.disableFlag("farewell-message");
                }

                if (field.hasFlag(FieldFlag.DYNMAP_DISABLED_BY_DEFAULT) || plugin.getSettingsManager().isStartDynmapFlagsDisabled())
                {
                    field.disableFlag("dynmap-area");
                    field.disableFlag("dynmap-marker");
                }

                // places the field in a disabled state

                if (field.hasFlag(FieldFlag.PLACE_DISABLED))
                {
                    field.setDisabled(true);
                }
                // allow all owners of overlapping fields into the field

                plugin.getForceFieldManager().addAllowOverlappingOwners(field);

                // start disabling process for auto-disable fields

                field.startDisabler();
                return;
            }
        }
        else if (!isDisabled && plugin.getSettingsManager().isUnprotectableType(block.getTypeId()))
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

            Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_UNPROTECTABLE);

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
        }

        // -------------------------------------------------------------------------------------------

        if (block.getType().equals(Material.CHEST))
        {
            Field field = plugin.getForceFieldManager().getConflictSourceField(block.getLocation(), player.getName(), FieldFlag.ALL);

            if (field != null)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                {
                    boolean conflicted = false;

                    Field field1 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.EAST).getLocation(), player.getName(), FieldFlag.ALL);

                    allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field1.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (field1 != null && !field1.equals(field))
                        {
                            conflicted = true;
                        }
                    }

                    Field field2 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.WEST).getLocation(), player.getName(), FieldFlag.ALL);

                    allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field2.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (field2 != null && !field2.equals(field))
                        {
                            conflicted = true;
                        }
                    }

                    Field field3 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.NORTH).getLocation(), player.getName(), FieldFlag.ALL);

                    allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field3.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (field3 != null && !field3.equals(field))
                        {
                            conflicted = true;
                        }
                    }

                    Field field4 = plugin.getForceFieldManager().getConflictSourceField(block.getRelative(BlockFace.SOUTH).getLocation(), player.getName(), FieldFlag.ALL);

                    allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field4.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (field4 != null && !field4.equals(field))
                        {
                            conflicted = true;
                        }
                    }

                    if (conflicted)
                    {
                        ChatBlock.sendMessage(player, ChatColor.RED + "Cannot place chest next so someone else's field");
                        event.setCancelled(true);
                    }
                }
            }
        }

        Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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

        field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (field.hasFlag(FieldFlag.PLACE_GRIEF))
                {
                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                    {
                        BlockState blockState = event.getBlockReplacedState();

                        plugin.getGriefUndoManager().addBlock(field, blockState);
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

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        boolean unprotected = false;

        Block piston = event.getBlock();

        if (plugin.getSettingsManager().isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getSourceField(piston.getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field == null)
        {
            unprotected = true;
        }

        List<Block> blocks = event.getBlocks();

        for (Block block : blocks)
        {
            if (plugin.getSettingsManager().isFieldType(block) && plugin.getForceFieldManager().isField(block))
            {
                event.setCancelled(true);
            }
            if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
            {
                event.setCancelled(true);
            }
            if (unprotected)
            {
                field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

                if (field != null)
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
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        boolean unprotected = false;

        Block piston = event.getBlock();

        if (plugin.getSettingsManager().isBlacklistedWorld(piston.getWorld()))
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getSourceField(piston.getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field == null)
        {
            unprotected = true;
        }

        // prevent piston from moving a field or unbreakable block

        Block block = piston.getRelative(event.getDirection()).getRelative(event.getDirection());

        if (plugin.getSettingsManager().isFieldType(block) && plugin.getForceFieldManager().isField(block))
        {
            event.setCancelled(true);
        }
        if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
        {
            event.setCancelled(true);
        }

        if (unprotected)
        {
            field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (field != null)
            {
                event.setCancelled(true);
            }
        }
    }
}
