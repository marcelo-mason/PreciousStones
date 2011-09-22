package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
     * @param event
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        plugin.getPlayerManager().playerLogin(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName(), true);
    }

    /**
     * @param event
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.getPlayerManager().playerLogoff(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName(), true);
    }

    /**
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
     * @param event
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        final Player player = event.getPlayer();
        DebugTimer dt = new DebugTimer("onPlayerInteract");
        Block block = event.getClickedBlock();
        ItemStack is = player.getItemInHand();

        if (player == null)
        {
            return;
        }

        if (block != null)
        {
            Field useField = plugin.getForceFieldManager().findUseProtected(block.getLocation(), player, block.getTypeId());

            if (useField != null)
            {
                if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.use"))
                {
                    plugin.getCommunicationManager().warnUse(player, block, useField);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (plugin.getCuboidManager().hasOpenCuboid(player))
            {
                Block target = player.getTargetBlock(plugin.getSettingsManager().getThroughFieldsSet(), 128);

                // close the cuboid when clicking back to the origin block

                if (plugin.getCuboidManager().isOpenCuboid(player, target))
                {
                    plugin.getCuboidManager().closeCuboid(player);
                    return;
                }

                // check for protections

                Field field = plugin.getForceFieldManager().getNotAllowedSourceField(player.getLocation(), player.getName(), FieldFlag.PREVENT_DESTROY);

                if (field == null)
                {
                    field = plugin.getForceFieldManager().getNotAllowedSourceField(player.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_REQUEST);
                }

                if (field == null)
                {
                    field = plugin.getForceFieldManager().getNotAllowedSourceField(player.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_INTERVAL);
                }

                if (field != null)
                {
                    if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.destroy"))
                    {
                        return;
                    }
                }

                // add to the cuboid

                CuboidEntry openCuboid = plugin.getCuboidManager().getOpenCuboid(player);

                Field conflicts = plugin.getForceFieldManager().fieldConflicts(target, player);

                if (conflicts == null)
                {
                    if (openCuboid.testOverflow(target.getLocation()))
                    {
                        Material material = Material.getMaterial(plugin.getSettingsManager().getCuboidDefiningType());

                        plugin.getCuboidManager().addSelectionBlock(player, target);
                        plugin.getVisualizationManager().displaySingle(player, material, target);
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        ChatBlock.sendMessage(player, ChatColor.RED + "Exceeding maximum dimensions");
                    }
                }
                else
                {
                    ChatBlock.sendMessage(player, ChatColor.RED + "Conflicts with someone else's field");
                }
            }
            else
            {
                Block target = player.getTargetBlock(plugin.getSettingsManager().getThroughFieldsSet(), 128);

                if (player.isSneaking())
                {
                    Field field = plugin.getForceFieldManager().getField(target);

                    if (field != null)
                    {
                        if (field.hasFlag(FieldFlag.CUBOID))
                        {
                            if (field.getParent() != null)
                            {
                                field = field.getParent();
                            }

                            plugin.getCuboidManager().openCuboid(player, field);
                            return;
                        }
                    }
                }
            }
        }

        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            Material materialInHand = is.getType();

            if (plugin.getSettingsManager().isFieldType(materialInHand) && plugin.getPermissionsManager().hasPermission(player, "preciousstones.benefit.scoping"))
            {
                if (!plugin.getPlayerManager().getPlayerData(player.getName()).isDisabled())
                {
                    HashSet<Field> touching = plugin.getForceFieldManager().getTouchingFields(event.getClickedBlock(), materialInHand);
                    plugin.getCommunicationManager().printTouchingFields(player, touching);
                }
            }
        }

        if (block != null)
        {
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

                if (is != null)
                {
                    if (plugin.getSettingsManager().isToolItemType(is.getTypeId()))
                    {
                        if (plugin.getSettingsManager().isBypassBlock(block))
                        {
                            return;
                        }

                        if (plugin.getForceFieldManager().isField(block))
                        {
                            Field field = plugin.getForceFieldManager().getField(block);

                            if (field.isChild())
                            {
                                field = field.getParent();
                            }

                            FieldSettings fs = field.getSettings();

                            if (!plugin.getCuboidManager().hasOpenCuboid(player))
                            {
                                if (field.isAllowed(player.getName()) || plugin.getPermissionsManager().hasPermission(player, "preciousstones.benefit.visualize"))
                                {
                                    if (player.isSneaking())
                                    {
                                        plugin.getVisualizationManager().visualizeSingleField(player, field);
                                        return;
                                    }
                                }
                            }
                            else
                            {
                                ChatBlock.sendMessage(player, ChatColor.RED + "Cannot visualize while defining a cuboid");
                            }

                            if (plugin.getSettingsManager().isSnitchType(block))
                            {
                                if (field.isAllowed(player.getName()) || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.details"))
                                {
                                    if (!plugin.getCommunicationManager().showSnitchList(player, plugin.getForceFieldManager().getField(block)))
                                    {
                                        showInfo(field, player);
                                        ChatBlock.sendMessage(player, ChatColor.AQUA + "There have been no intruders around here");
                                        ChatBlock.sendBlank(player);
                                    }
                                }
                            }
                            else
                            {
                                if ((plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.undo")) && (fs.hasGriefUndoFlag()))
                                {
                                    HashSet<Field> overlapped = plugin.getForceFieldManager().getOverlappedFields(player, field);

                                    int size = 0;

                                    for (Field o : overlapped)
                                    {
                                        if (!field.hasFlag(FieldFlag.GRIEF_UNDO_REQUEST))
                                        {
                                            continue;
                                        }

                                        size += plugin.getGriefUndoManager().undoGrief(o);
                                    }

                                    if (size > 0)
                                    {
                                        player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "Rolled back " + size + " griefed " + Helper.plural(size, "block", "s") + " on " + overlapped.size() + " overlapped " + Helper.plural(size, "field", "s"));
                                    }
                                    else
                                    {
                                        showInfo(field, player);
                                        player.sendMessage(ChatColor.AQUA + "No grief recorded on any of the " + overlapped.size() + " overlapped fields");
                                        ChatBlock.sendBlank(player);
                                    }
                                }
                                else
                                {
                                    showInfo(field, player);
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
    }

    private void showInfo(Field field, Player player)
    {
        Block block = field.getBlock();

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

        if (Helper.isSameLocation(event.getFrom(), event.getTo()))
        {
            return;
        }

        // undo a player's visualization if it exists

        if (!Helper.isSameBlock(event.getFrom(), event.getTo()))
        {
            if (plugin.getSettingsManager().isVisualizeEndOnMove())
            {
                if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.visualize"))
                {
                    if (!plugin.getCuboidManager().hasOpenCuboid(player))
                    {
                        plugin.getVisualizationManager().revertVisualization(player);
                    }
                }
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

        List<Field> currentfields = plugin.getForceFieldManager().getSourceFields(player.getLocation(), FieldFlag.ALL);

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

        field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_REQUEST);

        if (field == null)
        {
            field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_INTERVAL);
        }

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

        field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_REQUEST);

        if (field == null)
        {
            field = plugin.getForceFieldManager().getNotAllowedSourceField(block.getLocation(), player.getName(), FieldFlag.GRIEF_UNDO_INTERVAL);
        }

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
