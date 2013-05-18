package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();

        Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.ALL);

        if (field != null)
        {
            if (field.isBlacklistedCommand(event.getMessage()))
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.commandblacklist"))
                {
                    ChatBlock.send(player, "commandCanceled");
                    event.setCancelled(true);
                }
            }
        }

        field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.COMMAND_BLACKLIST);

        if (field != null)
        {
            if (FieldFlag.COMMAND_BLACKLIST.applies(field, player))
            {
                if (field.getSettings().isCanceledCommand(event.getMessage()))
                {
                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.commandblacklist"))
                    {
                        ChatBlock.send(player, "commandCanceled");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final String playerName = event.getPlayer().getName();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                plugin.getPlayerManager().playerLogin(playerName);
                plugin.getStorageManager().offerPlayer(playerName);
                plugin.getForceFieldManager().enableFieldsOnLogon(playerName);
                plugin.getForceFieldManager().removeFieldsIfNoPermission(playerName);
            }
        }, 10);
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
        plugin.getForceFieldManager().disableFieldsOnLogoff(event.getPlayer().getName());

    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_ON_SNEAK);

        if (field != null)
        {
            if (FieldFlag.TELEPORT_ON_SNEAK.applies(field, player))
            {
                plugin.getTeleportationManager().teleport(player, field);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                handlePlayerSpawn(player);
            }
        }, 5);
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                handlePlayerSpawn(player);
            }
        }, 5);
    }

    private void handlePlayerSpawn(Player player)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(player.getWorld()))
        {
            return;
        }

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
                    plugin.getVisualizationManager().revert(player);
                }
            }
        }

        // remove player from any entry field he is not currently in

        List<Field> entryFields = plugin.getEntryManager().getPlayerEntryFields(player);

        if (entryFields != null)
        {
            for (Field entryField : entryFields)
            {
                if (!entryField.envelops(player.getLocation()))
                {
                    plugin.getEntryManager().leaveField(player, entryField);

                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryField))
                    {
                        plugin.getEntryManager().leaveOverlappedArea(player, entryField);
                    }
                }
            }
        }

        // get all the fields the player is currently standing in

        List<Field> currentFields = plugin.getForceFieldManager().getEnabledSourceFields(player.getLocation(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry"))
        {
            for (Field field : currentFields)
            {
                if (FieldFlag.PREVENT_ENTRY.applies(field, player))
                {
                    Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                    Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                    if (outside != null)
                    {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

                        if (f != null)
                        {
                            loc = outside;
                        }
                    }

                    player.teleport(loc);
                    plugin.getCommunicationManager().warnEntry(player, field);
                    return;
                }
            }
        }

        // did not get teleported out so now we update his last known outside location

        plugin.getPlayerManager().updateOutsideLocation(player);

        // enter all fields hes is not currently entered into yet

        for (Field currentField : currentFields)
        {
            if (!plugin.getEntryManager().enteredField(player, currentField))
            {
                if (!plugin.getEntryManager().containsSameNameOwnedField(player, currentField))
                {
                    plugin.getEntryManager().enterOverlappedArea(player, currentField);
                }
                plugin.getEntryManager().enterField(player, currentField);
            }
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

        if (event.getFrom() == null || event.getTo() == null)
        {
            return;
        }

        if (Helper.isSameLocation(event.getFrom(), event.getTo()))
        {
            return;
        }

        Player player = event.getPlayer();

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getTo(), FieldFlag.PREVENT_TELEPORT);

        if (field != null)
        {
            if (FieldFlag.PREVENT_TELEPORT.applies(field, player))
            {
                if (!plugin.getPermissionsManager().has(event.getPlayer(), "preciousstones.bypass.teleport"))
                {
                    event.setCancelled(true);
                    ChatBlock.send(player, "cannotTeleportInsideField");
                    return;
                }
            }
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
                        plugin.getVisualizationManager().revert(player);
                    }
                }
            }
        }

        // remove player from any entry field he is currently in that he is not going to be standing in

        List<Field> currentFields = plugin.getEntryManager().getPlayerEntryFields(player);

        if (currentFields != null)
        {
            for (Field entryField : currentFields)
            {
                if (!entryField.envelops(event.getTo()))
                {
                    plugin.getEntryManager().leaveField(player, entryField);

                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryField))
                    {
                        plugin.getEntryManager().leaveOverlappedArea(player, entryField);
                    }
                }
            }
        }

        // get all the fields the player is going to be standing in

        List<Field> futureFields = plugin.getForceFieldManager().getEnabledSourceFields(event.getTo(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry"))
        {
            for (Field futureField : futureFields)
            {
                if (FieldFlag.PREVENT_ENTRY.applies(futureField, player))
                {
                    Location loc = plugin.getPlayerManager().getOutsideFieldLocation(futureField, player);
                    Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                    if (outside != null)
                    {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

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

        // did not get teleported out so now we update his last known outside location

        plugin.getPlayerManager().updateOutsideLocation(player);

        // enter all future fields hes is not currently entered into yet

        for (Field futureField : futureFields)
        {
            if (!plugin.getEntryManager().enteredField(player, futureField))
            {
                if (!plugin.getEntryManager().containsSameNameOwnedField(player, futureField))
                {
                    plugin.getEntryManager().enterOverlappedArea(player, futureField);
                }
                plugin.getEntryManager().enterField(player, futureField);
            }
        }
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

        if (event.getFrom() == null || event.getTo() == null)
        {
            return;
        }

        if (Helper.isSameLocation(event.getFrom(), event.getTo()))
        {
            return;
        }

        if (plugin.getSettingsManager().isOncePerBlockOnMove())
        {
            if (Helper.isSameBlock(event.getFrom(), event.getTo()))
            {
                return;
            }
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        final Player player = event.getPlayer();

        // undo a player's visualization if it exists

        if (!Helper.isSameBlock(event.getFrom(), event.getTo()))
        {
            if (plugin.getSettingsManager().isVisualizeEndOnMove())
            {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize"))
                {
                    if (!plugin.getCuboidManager().hasOpenCuboid(player))
                    {
                        plugin.getVisualizationManager().revert(player);
                    }
                }
            }
        }

        // remove player from any entry field he is currently in that he is not going to be standing in

        List<Field> currentFields = plugin.getEntryManager().getPlayerEntryFields(player);

        if (currentFields != null)
        {
            for (Field entryField : currentFields)
            {
                if (!entryField.envelops(event.getTo()))
                {
                    plugin.getEntryManager().leaveField(player, entryField);

                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryField))
                    {
                        plugin.getEntryManager().leaveOverlappedArea(player, entryField);
                    }
                }
            }
        }

        // get all the fields the player is going to be standing in

        List<Field> futureFields = plugin.getForceFieldManager().getEnabledSourceFields(event.getTo(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry"))
        {
            for (Field field : futureFields)
            {
                if (FieldFlag.PREVENT_ENTRY.applies(field, player))
                {
                    Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                    Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                    if (outside != null)
                    {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

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

        // teleport due to walking on blocks

        for (Field field : futureFields)
        {
            if (field.hasFlag(FieldFlag.TELEPORT_IF_NOT_WALKING_ON) || field.hasFlag(FieldFlag.TELEPORT_IF_WALKING_ON))
            {
                if (field.getSettings().teleportDueToWalking(event.getTo(), field, player))
                {
                    plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceWalking");
                }
            }
        }

        // did not get teleported out so now we update his last known outside location

        plugin.getPlayerManager().updateOutsideLocation(player);

        // enter all future fields hes is not currently entered into yet

        for (final Field futureField : futureFields)
        {
            if (!plugin.getEntryManager().enteredField(player, futureField))
            {
                if (!plugin.getEntryManager().containsSameNameOwnedField(player, futureField))
                {
                    plugin.getEntryManager().enterOverlappedArea(player, futureField);
                }
                plugin.getEntryManager().enterField(player, futureField);
            }

            if (futureField.hasFlag(FieldFlag.TELEPORT_IF_HAS_ITEMS) || futureField.hasFlag(FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS))
            {
                PlayerInventory inventory = player.getInventory();
                ItemStack[] contents = inventory.getContents();
                boolean hasItem = false;

                for (ItemStack stack : contents)
                {
                    if (stack == null || stack.getTypeId() == 0)
                    {
                        continue;
                    }

                    if (futureField.getSettings().isTeleportHasItem(stack.getTypeId()))
                    {
                        if (FieldFlag.TELEPORT_IF_HAS_ITEMS.applies(futureField, player))
                        {
                            PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

                            if (!entry.isTeleporting())
                            {
                                entry.setTeleporting(true);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        plugin.getTeleportationManager().teleport(player, futureField, "teleportAnnounceHasItems");
                                    }
                                }, 0);
                            }
                            return;
                        }
                    }

                    if (FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS.applies(futureField, player))
                    {
                        if (futureField.getSettings().isTeleportHasNotItem(stack.getTypeId()))
                        {
                            hasItem = true;
                        }
                    }
                }

                if (!hasItem)
                {
                    if (FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS.applies(futureField, player))
                    {
                        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

                        if (!entry.isTeleporting())
                        {
                            entry.setTeleporting(true);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    plugin.getTeleportationManager().teleport(player, futureField, "teleportAnnounceNotHasItems");
                                }
                            }, 0);
                        }
                        return;
                    }
                }
            }

            ItemStack itemInHand = player.getItemInHand();

            if (itemInHand != null && itemInHand.getTypeId() != 0)
            {
                if (futureField.getSettings().isTeleportHoldingItem(itemInHand.getTypeId()))
                {
                    if (FieldFlag.TELEPORT_IF_HOLDING_ITEMS.applies(futureField, player))
                    {
                        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

                        if (!entry.isTeleporting())
                        {
                            entry.setTeleporting(true);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    plugin.getTeleportationManager().teleport(player, futureField, "teleportAnnounceHoldingItems");
                                }
                            }, 0);
                        }
                        return;
                    }
                }
            }

            itemInHand = player.getItemInHand();

            if (itemInHand != null && itemInHand.getTypeId() != 0)
            {
                if (!futureField.getSettings().isTeleportNotHoldingItem(itemInHand.getTypeId()))
                {
                    if (FieldFlag.TELEPORT_IF_NOT_HOLDING_ITEMS.applies(futureField, player))
                    {
                        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

                        if (!entry.isTeleporting())
                        {
                            entry.setTeleporting(true);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    plugin.getTeleportationManager().teleport(player, futureField, "teleportAnnounceNotHoldingItems");
                                }
                            }, 0);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        final Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity.getType().equals(EntityType.ITEM_FRAME))
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.item-frame-take"))
            {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_ITEM_FRAME_TAKE);

                if (field != null)
                {
                    if (FieldFlag.PREVENT_ITEM_FRAME_TAKE.applies(field, player))
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entity-interact"))
        {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_ENTITY_INTERACT);

            if (field != null)
            {
                if (FieldFlag.PREVENT_ENTITY_INTERACT.applies(field, player))
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
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld()))
        {
            return;
        }

        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack is = player.getItemInHand();

        if (player == null)
        {
            return;
        }

        // -------------------------------------------------------------------------------- interacting with use protected block

        if (block != null)
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.use"))
            {
                Field useField = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_USE);

                if (useField != null)
                {
                    if (FieldFlag.PREVENT_USE.applies(useField, player))
                    {
                        if (!useField.getSettings().canUse(block.getTypeId()))
                        {
                            plugin.getCommunicationManager().warnUse(player, block, useField);
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- renting time

        if (block != null)
        {
            if (SignHelper.isSign(block))
            {
                FieldSign s = new FieldSign(block);

                if (s.isValid())
                {
                    Field field = s.getField();

                    if (!field.isOwner(player.getName()))
                    {
                        if (field.isDisabled())
                        {
                            ChatBlock.send(player, "fieldSignCannotRentDisabled");
                            event.setCancelled(true);
                            return;
                        }

                        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                        {
                            if (plugin.getSettingsManager().isCommandsToRentBuy())
                            {
                                if (s.isRentable() || s.isShareable())
                                {
                                    ChatBlock.send(player, "rentQuestion");
                                }

                                if (s.isBuyable())
                                {
                                    ChatBlock.send(player, "buyQuestion");
                                }

                                event.setCancelled(true);
                                return;
                            }
                            else
                            {
                                if (s.isRentable() || s.isShareable())
                                {
                                    if (s.isRentable())
                                    {
                                        if (field.isRented())
                                        {
                                            if (!field.isRenter(player.getName()))
                                            {
                                                ChatBlock.send(player, "fieldSignAlreadyRented");
                                                plugin.getCommunicationManager().showRenterInfo(player, field);
                                                event.setCancelled(true);
                                                return;
                                            }
                                            else
                                            {
                                                if (player.isSneaking())
                                                {
                                                    field.abandonRent(player);
                                                    ChatBlock.send(player, "fieldSignRentAbandoned");
                                                    event.setCancelled(true);
                                                    return;
                                                }
                                            }
                                        }
                                    }

                                    if (field.rent(player, s))
                                    {
                                        if (s.isRentable())
                                        {
                                            s.setRentedColor();
                                        }
                                        else if (s.isShareable())
                                        {
                                            s.setSharedColor();
                                        }

                                        event.setCancelled(true);
                                        return;
                                    }
                                    return;
                                }

                                if (s.isBuyable())
                                {
                                    if (field.hasPendingPurchase())
                                    {
                                        ChatBlock.send(player, "fieldSignAlreadyBought");
                                    }
                                    else if (field.buy(player, s))
                                    {
                                        s.setBoughtColor(player);

                                        PreciousStones.getInstance().getForceFieldManager().addAllowed(field, player.getName());

                                        ChatBlock.send(player, "fieldSignBoughtAndAllowed");
                                    }

                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }

                        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                        {
                            if (s.isRentable())
                            {
                                if (field.isRented() && !field.isRenter(player.getName()))
                                {
                                    ChatBlock.send(player, "fieldSignAlreadyRented");
                                    plugin.getCommunicationManager().showRenterInfo(player, field);
                                    event.setCancelled(true);
                                    return;
                                }
                            }

                            plugin.getVisualizationManager().visualizeSingleOutline(player, field, true);
                            plugin.getCommunicationManager().showFieldDetails(player, field);
                            plugin.getCommunicationManager().showRenterInfo(player, field);
                        }

                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                        {
                            if (field.hasPendingPurchase())
                            {
                                field.retrievePurchase(player);
                                s.eject();

                                event.setCancelled(true);
                                return;
                            }

                            if (field.isRented())
                            {
                                if (field.hasPendingPayments())
                                {
                                    field.retrievePayment(player);
                                }
                                else
                                {
                                    plugin.getCommunicationManager().showRenterInfo(player, field);
                                }
                            }
                            else
                            {
                                ChatBlock.send(player, "fieldSignNoTennant");
                            }
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------- soil interaction

        if (block != null)
        {
            if (plugin.getSettingsManager().isCrop(block))
            {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PROTECT_CROPS);

                if (field != null)
                {
                    if (FieldFlag.PROTECT_CROPS.applies(field, player))
                    {
                        event.setCancelled(true);
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

                    if (target == null)
                    {
                        return;
                    }

                    // close the cuboid if the player shift clicks any block

                    if (player.isSneaking())
                    {
                        event.setCancelled(true);
                        plugin.getCuboidManager().closeCuboid(player);
                        return;
                    }

                    // close the cuboid when clicking back to the origin block

                    if (plugin.getCuboidManager().isOpenCuboid(player, target))
                    {
                        event.setCancelled(true);
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
                        boolean applies = FieldFlag.PROTECT_CROPS.applies(field, player);
                        boolean applies2 = FieldFlag.GRIEF_REVERT.applies(field, player);

                        if (applies || applies2)
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
                // -------------------------------------------------------------------------------- creating a cuboid

                if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                {
                    try
                    {
                        TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
                        Block target = aiming.getTargetBlock();

                        if (target == null)
                        {
                            return;
                        }

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
                                        if (field.hasFlag(FieldFlag.TRANSLOCATION))
                                        {
                                            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid"))
                                            {
                                                if (field.isNamed())
                                                {
                                                    if (plugin.getStorageManager().existsTranslocatior(field.getName(), field.getOwner()))
                                                    {
                                                        ChatBlock.send(player, "cannotReshapeWhileCuboid");
                                                        return;
                                                    }
                                                }
                                            }
                                        }

                                        if (plugin.getForceFieldManager().hasSubFields(field))
                                        {
                                            ChatBlock.send(player, "cannotRedefineWhileCuboid");
                                            return;
                                        }

                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
                                        {
                                            if (field.hasFlag(FieldFlag.REDEFINE_ON_DISABLED))
                                            {
                                                if (!field.isDisabled())
                                                {
                                                    ChatBlock.send(player, "redefineWhileDisabled");
                                                    return;
                                                }
                                            }
                                        }

                                        if (field.isRented())
                                        {
                                            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                                            {
                                                ChatBlock.send(player, "fieldSignCannotChange");
                                                return;
                                            }
                                        }

                                        event.setCancelled(true);
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
                                if (!field.getSettings().inDestroyBlacklist(block))
                                {
                                    if (FieldFlag.PREVENT_DESTROY.applies(field, player))
                                    {
                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                                        {
                                            canDestroy = false;
                                        }
                                    }
                                }
                            }

                            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

                            if (field != null && !field.getSettings().canGrief(block.getTypeId()))
                            {
                                if (FieldFlag.GRIEF_REVERT.applies(field, player))
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

                            if (plugin.getPermissionsManager().lwcProtected(player, block))
                            {
                                canDestroy = false;
                            }

                            if (plugin.getPermissionsManager().locketteProtected(player, block))
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
                                        ChatBlock.send(player, "cannotRemoveWithSubplots");
                                    }

                                    if (release)
                                    {
                                        plugin.getForceFieldManager().releaseNoDrop(field);

                                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase"))
                                        {
                                            if (!plugin.getSettingsManager().isNoRefunds())
                                            {
                                                int refund = fs.getRefund();

                                                if (refund > -1)
                                                {
                                                    plugin.getForceFieldManager().refund(player, refund);
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
                if (block.getTypeId() == 68) // wall sign
                {
                    plugin.getSnitchManager().recordSnitchShop(player, block);
                }

                if (block.getTypeId() == 58 || // workbench
                        block.getTypeId() == 355 || // bed
                        block.getTypeId() == 64 || //wood door
                        block.getTypeId() == 69 ||  //lever
                        block.getTypeId() == 328 ||  // cart
                        block.getTypeId() == 28 ||  /// note
                        block.getTypeId() == 84 || // juke
                        block.getTypeId() == 77) // button
                {
                    plugin.getSnitchManager().recordSnitchUsed(player, block);
                }

                if (block.getState() instanceof InventoryHolder)
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

                            // only those with permission can use fields

                            if (!field.getSettings().getRequiredPermissionUse().isEmpty())
                            {
                                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.required-permission"))
                                {
                                    if (!plugin.getPermissionsManager().has(player, field.getSettings().getRequiredPermissionUse()))
                                    {
                                        return;
                                    }
                                }
                            }

                            // -------------------------------------------------------------------------------- handle changing owners

                            if (field.getNewOwner() != null)
                            {
                                if (field.getNewOwner().equalsIgnoreCase(player.getName()))
                                {
                                    plugin.getStorageManager().changeTranslocationOwner(field, field.getNewOwner());

                                    String oldOwnerName = field.getOwner();

                                    field.changeOwner();

                                    plugin.getStorageManager().offerPlayer(field.getOwner());
                                    plugin.getStorageManager().offerPlayer(oldOwnerName);
                                    plugin.getStorageManager().offerField(field);

                                    ChatBlock.send(player, "takenFieldOwnership", oldOwnerName);

                                    Player oldOwner = Bukkit.getServer().getPlayerExact(oldOwnerName);

                                    if (oldOwner != null)
                                    {
                                        ChatBlock.send(oldOwner, "tookOwnership", player.getName());
                                    }
                                    return;
                                }
                                else
                                {
                                    ChatBlock.send(player, "cannotTakeOwnership", field.getNewOwner());
                                }
                            }

                            // -------------------------------------------------------------------------------- visualize/enable on sneaking right click

                            if (player.isSneaking())
                            {
                                if (FieldFlag.VISUALIZE_ON_SRC.applies(field, player))
                                {
                                    if (plugin.getCuboidManager().hasOpenCuboid(player))
                                    {
                                        ChatBlock.send(player, "visualizationNotWhileCuboid");
                                    }
                                    else
                                    {
                                        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize"))
                                        {
                                            ChatBlock.send(player, "visualizing");
                                            plugin.getVisualizationManager().visualizeSingleField(player, field);
                                        }
                                    }
                                }

                                if (!field.hasFlag(FieldFlag.TRANSLOCATION))
                                {
                                    if (FieldFlag.ENABLE_ON_SRC.applies(field, player))
                                    {
                                        if (field.isDisabled())
                                        {
                                            ChatBlock.send(player, "fieldTypeEnabled", field.getSettings().getTitle());
                                            boolean disabled = field.setDisabled(false, player);

                                            if (!disabled)
                                            {
                                                ChatBlock.send(player, "cannotEnable");
                                                return;
                                            }
                                            field.dirtyFlags();
                                        }
                                        else
                                        {
                                            ChatBlock.send(player, "fieldTypeDisabled", field.getSettings().getTitle());
                                            field.setDisabled(true, player);
                                            field.dirtyFlags();
                                        }
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
                                            ChatBlock.send(player, "noIntruders");
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
                                        ChatBlock.send(player, "noGriefRecorded");
                                        ChatBlock.sendBlank(player);
                                    }
                                    return;
                                }

                                // -------------------------------------------------------------------------------- right click translocation

                                boolean showTranslocations = false;

                                if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.use"))
                                {
                                    if (field.hasFlag(FieldFlag.TRANSLOCATION) && plugin.getForceFieldManager().isAllowed(block, player.getName()))
                                    {
                                        if (!field.isTranslocating())
                                        {
                                            if (field.isNamed())
                                            {
                                                if (!field.isDisabled())
                                                {
                                                    if (plugin.getStorageManager().appliedTranslocationCount(field) > 0)
                                                    {
                                                        PreciousStones.debug("clearing");
                                                        int size = plugin.getTranslocationManager().clearTranslocation(field);
                                                        plugin.getCommunicationManager().notifyClearTranslocation(field, player, size);
                                                        return;
                                                    }
                                                    else
                                                    {
                                                        PreciousStones.debug("disabled");
                                                        field.setDisabled(true, player);
                                                        field.dirtyFlags();
                                                        return;
                                                    }
                                                }
                                                else
                                                {
                                                    if (plugin.getStorageManager().unappliedTranslocationCount(field) > 0)
                                                    {
                                                        PreciousStones.debug("applying");
                                                        int size = plugin.getTranslocationManager().applyTranslocation(field);
                                                        plugin.getCommunicationManager().notifyApplyTranslocation(field, player, size);
                                                        return;
                                                    }
                                                    else
                                                    {
                                                        PreciousStones.debug("recording");
                                                        boolean disabled = field.setDisabled(false, player);

                                                        if (!disabled)
                                                        {
                                                            ChatBlock.send(player, "cannotEnable");
                                                            return;
                                                        }
                                                        field.dirtyFlags();
                                                        return;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                showTranslocations = true;
                                            }
                                        }
                                    }
                                }

                                // -------------------------------------------------------------------------------- show info right click action

                                if (showInfo(field, player))
                                {
                                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle"))
                                    {
                                        if (showTranslocations)
                                        {
                                            plugin.getCommunicationManager().notifyStoredTranslocations(player);
                                        }
                                        else if (!field.isDisabled() && !field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED))
                                        {
                                            ChatBlock.send(player, "usageToggle");
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
                                plugin.getCommunicationManager().showUnbreakableDetails(player, block);
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
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        final Block liquid = block.getRelative(event.getBlockFace());

        if (block == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        // snitch

        plugin.getSnitchManager().recordSnitchBucketFill(player, block);

        // -------------------------------------------------------------------------------------- prevent pickup up fields

        if (plugin.getForceFieldManager().isField(block))
        {
            event.setCancelled(true);
            return;
        }

        // -------------------------------------------------------------------------------------- breaking in a prevent-destroy area

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
                    }
                }
            }
        }

        // -------------------------------------------------------------------------------------- breaking in a grief revert area

        field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

        if (field != null)
        {
            if (FieldFlag.GRIEF_REVERT.applies(field, player))
            {
                if (field.getSettings().canGrief(block.getTypeId()))
                {
                    return;
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
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

        // -------------------------------------------------------------------------------------- breaking in a translocation area

        if (liquid.isLiquid())
        {
            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

            if (field != null)
            {
                if (field.isNamed())
                {
                    plugin.getTranslocationManager().removeBlock(field, block);
                    plugin.getTranslocationManager().flashFieldBlock(field, player);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        final Block liquid = block.getRelative(event.getBlockFace());

        Material mat = event.getBucket();

        if (plugin.getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld()))
        {
            return;
        }

        // snitch

        if (mat.equals(Material.LAVA_BUCKET))
        {
            plugin.getSnitchManager().recordSnitchBucketEmpty(player, block, "LAVA");
        }

        if (mat.equals(Material.WATER_BUCKET))
        {
            plugin.getSnitchManager().recordSnitchBucketEmpty(player, block, "WATER");
        }

        if (mat.equals(Material.MILK_BUCKET))
        {
            plugin.getSnitchManager().recordSnitchBucketEmpty(player, block, "MILK");
        }

        // -------------------------------------------------------------------------------------- placing in a prevent-place area


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
                        plugin.getCommunicationManager().warnEmpty(player, block, field);
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

        // -------------------------------------------------------------------------------------- placing in a translocation area


        if (!liquid.isLiquid())
        {
            return;
        }

        field = plugin.getForceFieldManager().getEnabledSourceField(liquid.getLocation(), FieldFlag.TRANSLOCATION);

        if (field != null)
        {
            if (FieldFlag.TRANSLOCATION.applies(field, player))
            {
                if (field.getSettings().canTranslocate(new BlockTypeEntry(liquid)))
                {
                    if (field.getName().length() == 0)
                    {
                        ChatBlock.send(player, "translocatorNameToBegin");
                        event.setCancelled(true);
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
                            plugin.getTranslocationManager().addBlock(finalField, liquid);
                            plugin.getTranslocationManager().flashFieldBlock(finalField, player);
                        }
                    }, 5);
                }
            }
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
            if (field.getSettings().getForceEntryGameMode() != null)
            {
                if (FieldFlag.ENTRY_GAME_MODE.applies(field, player))
                {
                    if (!gameMode.equals(field.getSettings().getForceEntryGameMode()))
                    {
                        ChatBlock.send(player, "cannotChangeGameMode");
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
                    ChatBlock.send(player, "pickaxeDisabled");
                }
                else
                {
                    data.setSuperduperpickaxe(true);
                    ChatBlock.send(player, "pickaxeEnabled");
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

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPortalEnter(PlayerPortalEvent event)
    {
        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getPlayer().getLocation(), FieldFlag.PREVENT_PORTAL_ENTER);

        if (field != null)
        {
            if (FieldFlag.PREVENT_PORTAL_ENTER.applies(field, event.getPlayer()))
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSprint(PlayerToggleSprintEvent event)
    {
        if (event.isSprinting())
        {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getPlayer().getLocation(), FieldFlag.NO_PLAYER_SPRINT);

        if (field != null)
        {
            if (FieldFlag.NO_PLAYER_SPRINT.applies(field, event.getPlayer()))
            {
                event.setCancelled(true);
            }
        }
    }
}
