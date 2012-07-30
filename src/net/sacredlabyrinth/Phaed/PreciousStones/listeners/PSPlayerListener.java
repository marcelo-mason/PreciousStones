package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

/**
 * PreciousStones player listener
 *
 * @author Phaed
 */
public class PSPlayerListener implements Listener
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
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        plugin.getPlayerManager().playerLogin(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName());
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.getPlayerManager().playerLogoff(event.getPlayer());
        plugin.getStorageManager().offerPlayer(event.getPlayer().getName());
        plugin.getEntryManager().leaveAllFields(event.getPlayer());
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(final PlayerRespawnEvent event)
    {
        handlePlayerSpawn(event.getPlayer());
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(final PlayerJoinEvent event)
    {
        handlePlayerSpawn(event.getPlayer());
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
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

        // -------------------------------------------------------------------------------- interacting with use protected block

        if (block != null)
        {
            Field useField = plugin.getForceFieldManager().findUseProtected(block.getLocation(), player, block.getTypeId());

            if (useField != null)
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.use"))
                {
                    plugin.getCommunicationManager().warnUse(player, block, useField);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // -------------------------------------------------------------------------------- soil interaction

        if (block != null)
        {
            if (block.getType().equals(Material.SOIL))
            {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_DESTROY);

                if (field != null)
                {
                    boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                        {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }


        // -------------------------------------------------------------------------------- actions during an open cuboid

        boolean hasCuboidHand = is == null || is.getTypeId() == 0 || plugin.getSettingsManager().isToolItemType(is.getTypeId()) || plugin.getSettingsManager().isFieldType(new BlockTypeEntry(is.getTypeId(), is.getData().getData()));

        if (hasCuboidHand)
        {
            if (plugin.getCuboidManager().hasOpenCuboid(player))
            {
                if (player.isSneaking())
                {
                    // handle cuboid undo

                    if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                    {
                        plugin.getCuboidManager().revertLastSelection(player);
                        return;
                    }
                }

                // handle cuboid expand

                if (event.getAction().equals(Action.RIGHT_CLICK_AIR))
                {
                    plugin.getCuboidManager().expandDirection(player);
                    return;
                }

                // handle open cuboid commands

                if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                {
                    TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                    Block target = aiming.getTargetBlock();

                    // close the cuboid if the player shift clicks any block

                    if (player.isSneaking())
                    {
                        plugin.getCuboidManager().closeCuboid(player);
                        return;
                    }

                    // close the cuboid when clicking back to the origin block

                    if (plugin.getCuboidManager().isOpenCuboid(player, target))
                    {
                        plugin.getCuboidManager().closeCuboid(player);
                        return;
                    }

                    // do not select field blocks

                    if (plugin.getForceFieldManager().getField(target) != null)
                    {
                        return;
                    }

                    // or add to the cuboid selection

                    Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_DESTROY);

                    if (field == null)
                    {
                        field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.GRIEF_REVERT);
                    }

                    if (field != null)
                    {
                        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                            {
                                return;
                            }
                        }
                    }

                    // add to the cuboid

                    if (plugin.getCuboidManager().processSelectedBlock(player, target))
                    {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            else
            {
                // handle closed cuboid commands

                if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                {
                    try
                    {
                        TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                        Block target = aiming.getTargetBlock();

                        if (player.isSneaking())
                        {
                            Field field = plugin.getForceFieldManager().getField(target);

                            if (field != null)
                            {
                                if (field.getBlock().getType().equals(Material.AIR))
                                {
                                    return;
                                }

                                if (field.hasFlag(FieldFlag.CUBOID))
                                {
                                    if (field.getParent() != null)
                                    {
                                        field = field.getParent();
                                    }

                                    if (field.isOwner(player.getName()) || plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid"))
                                    {
                                        if (plugin.getForceFieldManager().hasSubFields(field))
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "The field has sub-fields inside of it thus cannot be redifined.");
                                            return;
                                        }

                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
                                        {
                                            if (field.hasFlag(FieldFlag.REDEFINE_ON_DISABLED))
                                            {
                                                if (!field.isDisabled())
                                                {
                                                    ChatBlock.sendMessage(player, ChatColor.RED + "This field's cuboid can only be redefined while disabled");
                                                    return;
                                                }
                                            }
                                        }

                                        plugin.getCuboidManager().openCuboid(player, field);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- super pickaxes

        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (is != null)
            {
                if (is.getTypeId() == 270 || is.getTypeId() == 274 || is.getTypeId() == 278 || is.getTypeId() == 285)
                {
                    PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());

                    if (data.isSuperduperpickaxe())
                    {
                        boolean canDestroy = true;

                        // if superduper then get target block

                        if (data.isSuperduperpickaxe())
                        {
                            if (event.getAction().equals(Action.LEFT_CLICK_AIR))
                            {
                                try
                                {
                                    TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                                    Block targetBlock = aiming.getTargetBlock();

                                    if (targetBlock != null)
                                    {
                                        block = targetBlock;
                                    }
                                }
                                catch (Exception ex)
                                {

                                }
                            }
                        }

                        if (block != null)
                        {
                            // check for protections

                            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

                            if (field != null)
                            {
                                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                                {
                                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                                    {
                                        canDestroy = false;
                                    }
                                }
                            }

                            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

                            if (field != null && !field.getSettings().canGrief(block.getTypeId()))
                            {
                                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                                {
                                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                                    {
                                        canDestroy = false;
                                    }
                                }
                                else
                                {
                                    plugin.getStorageManager().deleteBlockGrief(block);
                                }
                            }

                            if (!plugin.getWorldGuardManager().canBuild(player, block.getLocation()))
                            {
                                canDestroy = false;
                            }

                            if (plugin.getPermissionsManager().lwcProtected(block))
                            {
                                canDestroy = false;
                            }

                            // go ahead and do the block destruction

                            if (canDestroy)
                            {
                                // if block is a field then remove it

                                if (plugin.getForceFieldManager().isField(block))
                                {
                                    field = plugin.getForceFieldManager().getField(block);
                                    FieldSettings fs = field.getSettings();

                                    if (field == null)
                                    {
                                        return;
                                    }

                                    boolean release = false;

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
                                    }

                                    if (plugin.getForceFieldManager().hasSubFields(field))
                                    {
                                        ChatBlock.sendMessage(player, ChatColor.RED + "Cannot remove fields that have plot-fields inside of it.  You must remove them first before you can remove this field.");
                                    }

                                    if (release)
                                    {
                                        plugin.getForceFieldManager().silentRelease(field);

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
                                }

                                // if block is an unbreakable remove it

                                if (plugin.getUnbreakableManager().isUnbreakable(block))
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
                                }

                                // do the final destruction

                                Helper.dropBlock(block);
                                block.setTypeIdAndData(0, (byte) 0, false);
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- snitch record right click actions

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

                        // -------------------------------------------------------------------------------- right clicking on fields

                        try
                        {
                            // makes sure water/see-through fields can be right clicked

                            TargetBlock aiming = new TargetBlock(player, 1000, 0.2, new int[]{0});
                            Block targetBlock = aiming.getTargetBlock();

                            if (targetBlock != null && plugin.getForceFieldManager().isField(targetBlock))
                            {
                                block = targetBlock;
                            }
                        }
                        catch (Exception ex)
                        {

                        }

                        if (plugin.getForceFieldManager().isField(block))
                        {
                            Field field = plugin.getForceFieldManager().getField(block);

                            if (field.isChild())
                            {
                                field = field.getParent();
                            }

                            // -------------------------------------------------------------------------------- handle changing owners

                            if (field.getNewOwner() != null)
                            {
                                if (field.getNewOwner().equalsIgnoreCase(player.getName()))
                                {
                                    PreciousStones plugin = PreciousStones.getInstance();

                                    PlayerEntry oldData = plugin.getPlayerManager().getPlayerEntry(field.getOwner());
                                    oldData.decrementFieldCount(field.getSettings().getTypeEntry());

                                    PlayerEntry newData = plugin.getPlayerManager().getPlayerEntry(field.getNewOwner());
                                    newData.incrementFieldCount(field.getSettings().getTypeEntry());

                                    PreciousStones.getInstance().getStorageManager().changeTranslocationOwner(field, field.getNewOwner());

                                    String oldOwnerName = field.getOwner();

                                    field.changeOwner();

                                    plugin.getStorageManager().offerPlayer(field.getOwner());
                                    plugin.getStorageManager().offerPlayer(field.getNewOwner());
                                    PreciousStones.getInstance().getStorageManager().offerField(field);

                                    ChatBlock.sendMessage(player, ChatColor.AQUA + "You have taken ownership of " + oldOwnerName + "'s field");

                                    Player oldOwner = Helper.matchSinglePlayer(oldOwnerName);

                                    if (oldOwner != null)
                                    {
                                        ChatBlock.sendMessage(oldOwner, ChatColor.AQUA + Helper.capitalize(player.getName()) + " has taken ownership of your field");
                                    }
                                    return;
                                }
                                else
                                {
                                    ChatBlock.sendMessage(player, ChatColor.AQUA + "You cannot take ownership of this field.  It has been given to: " + field.getNewOwner());
                                }
                            }

                            // -------------------------------------------------------------------------------- visualize/enable on sneaking right click

                            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                            if (player.isSneaking())
                            {
                                if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                                {
                                    if (field.hasFlag(FieldFlag.VISUALIZE_ON_SRC))
                                    {
                                        if (plugin.getCuboidManager().hasOpenCuboid(player))
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.RED + "Cannot visualize while defining a cuboid");
                                        }
                                        else
                                        {
                                            if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize"))
                                            {
                                                ChatBlock.sendMessage(player, ChatColor.AQUA + "Visualizing...");
                                                plugin.getVisualizationManager().visualizeSingleField(player, field);
                                            }
                                        }
                                    }

                                    if (field.hasFlag(FieldFlag.ENABLE_ON_SRC))
                                    {
                                        if (field.isDisabled())
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(field.getSettings().getTitle()) + " field has been enabled");
                                            field.setDisabled(false);
                                        }
                                        else
                                        {
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(field.getSettings().getTitle()) + " field has been disabled");
                                            field.setDisabled(true);
                                        }
                                        field.dirtyFlags();
                                    }
                                }
                            }
                            else
                            {
                                // -------------------------------------------------------------------------------- snitch block right click action

                                if (plugin.getSettingsManager().isSnitchType(block))
                                {
                                    if (plugin.getForceFieldManager().isAllowed(field, player.getName()) || plugin.getPermissionsManager().has(player, "preciousstones.admin.details"))
                                    {
                                        if (!plugin.getCommunicationManager().showSnitchList(player, plugin.getForceFieldManager().getField(block)))
                                        {
                                            showInfo(field, player);
                                            ChatBlock.sendMessage(player, ChatColor.AQUA + "There have been no intruders around here");
                                            ChatBlock.sendBlank(player);
                                        }
                                        return;
                                    }
                                }

                                // -------------------------------------------------------------------------------- grief revert right click action

                                if ((field.hasFlag(FieldFlag.GRIEF_REVERT)) && (plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getPermissionsManager().has(player, "preciousstones.admin.undo")))
                                {
                                    int size = plugin.getGriefUndoManager().undoGrief(field);

                                    if (size == 0)
                                    {
                                        showInfo(field, player);
                                        player.sendMessage(ChatColor.AQUA + "No grief recorded on the field");
                                        ChatBlock.sendBlank(player);
                                    }
                                    return;
                                }

                                // -------------------------------------------------------------------------------- right click translocator

                                if (field.hasFlag(FieldFlag.TRANSLOCATOR) && plugin.getForceFieldManager().isAllowed(block, player.getName()))
                                {
                                    if (plugin.getStorageManager().hasTranslocation(field))
                                    {
                                        int size = plugin.getTranslocationManager().revertTranslocation(field);
                                        plugin.getCommunicationManager().notifyTranslocation(field, player, size);
                                    }
                                    else
                                    {
                                        int size = plugin.getTranslocationManager().clearTranslocation(field);
                                        plugin.getCommunicationManager().notifyTranslocationClean(field, player, size);
                                    }
                                    return;
                                }

                                // -------------------------------------------------------------------------------- show info right click action

                                if (showInfo(field, player))
                                {
                                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle"))
                                    {
                                        if (!field.isDisabled() && !field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED))
                                        {
                                            player.sendMessage(ChatColor.DARK_GRAY + "Use '/ps toggle [flag]' to disable individual flags");
                                        }

                                        ChatBlock.sendBlank(player);
                                    }
                                }
                            }
                        }
                        else if (plugin.getUnbreakableManager().isUnbreakable(block))
                        {
                            // -------------------------------------------------------------------------------- unbreakable info right click

                            if (plugin.getUnbreakableManager().isOwner(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().has(player, "preciousstones.admin.details"))
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
                            // -------------------------------------------------------------------------------- protected surface right click action

                            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.ALL);

                            if (field != null)
                            {
                                if (plugin.getForceFieldManager().isAllowed(field, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails())
                                {
                                    if (!plugin.getSettingsManager().isDisableGroundInfo())
                                    {
                                        plugin.getCommunicationManager().showProtectedLocation(player, block);
                                    }
                                }
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


    private boolean showInfo(Field field, Player player)
    {
        Block block = field.getBlock();

        if (plugin.getForceFieldManager().isAllowed(block, player.getName()) || plugin.getSettingsManager().isPublicBlockDetails() || plugin.getPermissionsManager().has(player, "preciousstones.admin.details"))
        {
            if (plugin.getCommunicationManager().showFieldDetails(player, field))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            plugin.getCommunicationManager().showFieldOwner(player, block);
            return false;
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getTo(), FieldFlag.PREVENT_TELEPORT);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, event.getPlayer().getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (!plugin.getPermissionsManager().has(event.getPlayer(), "preciousstones.bypass.teleport"))
                {
                    //event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to teleport to that location");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        handlePlayerMove(event);
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
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
                if (!plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize"))
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

        List<Field> currentfields = plugin.getForceFieldManager().getEnabledSourceFields(player.getLocation(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry"))
        {
            for (Field field : currentfields)
            {
                if (field.hasFlag(FieldFlag.PREVENT_ENTRY))
                {
                    boolean allowedEntry = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowedEntry || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                        Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                        if (outside != null)
                        {
                            Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

                            if (f != null)
                            {
                                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                                {
                                    loc = outside;
                                }
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

    private void handlePlayerSpawn(Player player)
    {
        // refund confiscated items if not in confiscation fields

        Field confField = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.CONFISCATE_ITEMS);

        if (confField == null)
        {
            plugin.getConfiscationManager().returnItems(player);
        }

        // undo a player's visualization if it exists

        if (plugin.getSettingsManager().isVisualizeEndOnMove())
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize"))
            {
                if (!plugin.getCuboidManager().hasOpenCuboid(player))
                {
                    plugin.getVisualizationManager().revertVisualization(player);
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

        List<Field> currentfields = plugin.getForceFieldManager().getEnabledSourceFields(player.getLocation(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry"))
        {
            for (Field field : currentfields)
            {
                if (field.hasFlag(FieldFlag.PREVENT_ENTRY))
                {
                    boolean allowedEntry = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowedEntry || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                        Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                        if (outside != null)
                        {
                            Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

                            if (f != null)
                            {
                                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                                {
                                    loc = outside;
                                }
                            }
                        }

                        player.teleport(loc);
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
    }


    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
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

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.break"))
                {
                    plugin.getCommunicationManager().notifyBypassPlace(player, block, field);
                }
                else
                {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().warnDestroyArea(player, block, field);
                }
            }
        }

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (field.getSettings().canGrief(block.getTypeId()))
                {
                    return;
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.break"))
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

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

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
                    plugin.getCommunicationManager().warnEmpty(player, block, field);
                }
            }
        }

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (field.hasFlag(FieldFlag.PLACE_GRIEF))
                {
                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                    {
                        plugin.getGriefUndoManager().addBlock(field, block, true);
                        plugin.getStorageManager().offerGrief(field);
                    }
                }
                else
                {
                    if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
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
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();
        GameMode gameMode = event.getNewGameMode();

        Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.ALL);

        if (field != null)
        {
            if (!plugin.getForceFieldManager().isApplyToAllowed(field, player.getName()) || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (field.getSettings().getForceEntryGameMode() != null)
                {
                    if (!gameMode.equals(field.getSettings().getForceEntryGameMode()))
                    {
                        ChatBlock.sendMessage(player, ChatColor.RED + "Cannot change your game mode in this field");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        String[] split = event.getMessage().substring(1).split(" ");

        if (split.length == 0)
        {
            return;
        }

        Player player = event.getPlayer();
        String command = split[0];

        if (command.equals("//"))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.superduperpickaxe"))
            {
                PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());

                if (data.isSuperduperpickaxe())
                {
                    data.setSuperduperpickaxe(false);
                    ChatBlock.sendMessage(player, ChatColor.AQUA + "Super duper pick axe disabled");
                }
                else
                {
                    data.setSuperduperpickaxe(true);
                    ChatBlock.sendMessage(player, ChatColor.AQUA + "Super duper pick axe enabled");
                }
                plugin.getStorageManager().offerPlayer(player.getName());
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionSplash(PotionSplashEvent event)
    {
        boolean hasHarm = false;

        ThrownPotion potion = event.getPotion();
        Collection<PotionEffect> effects = potion.getEffects();

        for (PotionEffect effect : effects)
        {
            if (effect.getType().equals(PotionEffectType.BLINDNESS) ||
                    effect.getType().equals(PotionEffectType.CONFUSION) ||
                    effect.getType().equals(PotionEffectType.HARM) ||
                    effect.getType().equals(PotionEffectType.POISON) ||
                    effect.getType().equals(PotionEffectType.WEAKNESS) ||
                    effect.getType().equals(PotionEffectType.SLOW) ||
                    effect.getType().equals(PotionEffectType.SLOW_DIGGING))
            {
                hasHarm = true;
            }
        }

        if (hasHarm)
        {
            Collection<LivingEntity> entities = event.getAffectedEntities();

            for (LivingEntity entity : entities)
            {
                if (entity instanceof Player)
                {
                    Player player = (Player) entity;

                    Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_PVP);

                    if (field != null)
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
