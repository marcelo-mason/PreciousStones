package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.EntryFields;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.StackHelper;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.Map.Entry;

/**
 * Handles what happens inside fields
 *
 * @author Phaed
 */
public final class EntryManager {
    private PreciousStones plugin;
    private final List<Field> enteredFields = new ArrayList<Field>();
    private final HashMap<String, EntryFields> entriesByPlayer = new HashMap<String, EntryFields>();
    private final HashMap<String, EntryFields> dynamicEntries = new HashMap<String, EntryFields>();
    private int updateCount = 0;

    /**
     *
     */
    public EntryManager() {
        plugin = PreciousStones.getInstance();
        scheduleNextUpdate();
    }

    private boolean isDynamic(Field field) {
        return field.hasFlag(FieldFlag.DAMAGE) ||
                field.hasFlag(FieldFlag.REPAIR) ||
                field.hasFlag(FieldFlag.HEAL) ||
                field.hasFlag(FieldFlag.FEED) ||
                field.hasFlag(FieldFlag.POTIONS) ||
                field.hasFlag(FieldFlag.NEUTRALIZE_POTIONS) ||
                field.hasFlag(FieldFlag.CONFISCATE_ITEMS) ||
                field.hasFlag(FieldFlag.AIR);
    }

    private void scheduleNextUpdate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Update(), 20L);
    }

    private class Update implements Runnable {
        public void run() {
            synchronized (dynamicEntries) {
                doEffects();
            }
            scheduleNextUpdate();

            updateCount++;

            if (updateCount > 214783640) {
                updateCount = 0;
            }
        }
    }

    private void doEffects() {
        try {
            for (Entry<String, EntryFields> playerEntry : dynamicEntries.entrySet()) {
                Player player = Bukkit.getServer().getPlayerExact(playerEntry.getKey());

                if (player == null) {
                    continue;
                }

                EntryFields ef = playerEntry.getValue();
                List<Field> fields = ef.getFields();

                boolean hasDamage = false;
                boolean hasHeal = false;
                boolean hasFeeding = false;
                boolean hasAir = false;
                boolean hasRepair = false;
                boolean hasPotion = false;

                for (Field field : fields) {
                    // disabled fields shouldn't be doing things

                    if (field.isDisabled()) {
                        continue;
                    }

                    // check players inventories for items to confiscate every five seconds

                    if (updateCount % 5 == 0) {
                        if (FieldFlag.CONFISCATE_ITEMS.applies(field, player)) {
                            plugin.getConfiscationManager().confiscateItems(field, player);
                        }
                    }

                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.giveair")) {
                        if (!hasAir) {
                            if (FieldFlag.AIR.applies(field, player)) {
                                if (player.getRemainingAir() < 300) {
                                    player.setRemainingAir(600);
                                    plugin.getCommunicationManager().showGiveAir(player);
                                    hasAir = true;
                                }
                            }
                        }
                    }

                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.feed")) {
                        if (!hasFeeding) {
                            if (FieldFlag.FEED.applies(field, player)) {
                                int food = player.getFoodLevel();
                                if (food < 20) {
                                    player.setFoodLevel(food + field.getSettings().getFeed());
                                    plugin.getCommunicationManager().showFeeding(player);
                                    hasFeeding = true;
                                }
                            }
                        }
                    }

                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.heal")) {
                        if (!hasHeal) {
                            if (FieldFlag.HEAL.applies(field, player)) {
                                if (player.getHealth() < 20 && player.getHealth() > 0) {
                                    player.setHealth((int) healthCheck(player.getHealth() + field.getSettings().getHeal()));
                                    plugin.getCommunicationManager().showHeal(player);
                                    hasHeal = true;
                                }

                            }
                        }
                    }

                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.repair")) {
                        if (!hasRepair) {
                            if (FieldFlag.REPAIR.applies(field, player)) {
                                ItemStack[] contents = player.getInventory().getContents();
                                ItemStack[] armors = player.getInventory().getArmorContents();
                                ItemStack[] items = (ItemStack[])ArrayUtils.addAll(contents, armors);

                                for (ItemStack item : items) {
                                    if (item != null) {
                                        if (plugin.getSettingsManager().isRepairableItemType(new BlockTypeEntry(item.getType()))) {
                                            short dur = item.getDurability();
                                            if (dur > 0) {
                                                dur -= field.getSettings().getRepair();
                                                if (dur < 0) {
                                                    dur = 0;
                                                }
                                                item.setDurability(dur);
                                                plugin.getCommunicationManager().showRepair(player);
                                                hasRepair = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage")) {
                        if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking())) {
                            if (!hasDamage) {
                                if (FieldFlag.DAMAGE.applies(field, player)) {
                                    if (player.getHealth() > 0) {
                                        double health = healthCheck(player.getHealth() - field.getSettings().getDamage());
                                        player.setHealth((int) Math.max(health, 0));

                                        if (health <= 1) {
                                            player.playEffect(EntityEffect.DEATH);
                                        }

                                        plugin.getCommunicationManager().showDamage(player);
                                        hasDamage = true;
                                    }
                                }
                            }
                        }
                    }

                    if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.potions")) {
                        if (!hasPotion) {
                            if (FieldFlag.POTIONS.applies(field, player)) {
                                plugin.getPotionManager().applyPotions(player, field);
                                hasPotion = true;
                            }
                        }
                    }

                    if (FieldFlag.NEUTRALIZE_POTIONS.applies(field, player)) {
                        plugin.getPotionManager().neutralizePotions(player, field);
                    }
                }
            }
        } catch (Exception ex) {
            // help
        }
    }

    /**
     * @param player
     * @return
     */
    public List<Field> getPlayerEntryFields(Player player) {
        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(player.getName());

            if (ef != null) {
                List<Field> e = new ArrayList<Field>();
                e.addAll(ef.getFields());
                return e;
            }
        }

        return null;
    }

    public void reevaluateEnteredFields(Player player) {
        if (player == null) {
            return;
        }

        // refund confiscated items if not in confiscation fields

        Field confField = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.CONFISCATE_ITEMS);

        if (confField == null) {
            plugin.getConfiscationManager().returnItems(player);
        }

        // undo a player's visualization if it exists

        if (plugin.getSettingsManager().isVisualizeEndOnMove()) {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.admin.visualize")) {
                if (!plugin.getCuboidManager().hasOpenCuboid(player)) {
                    plugin.getVisualizationManager().revert(player);
                }
            }
        }

        // remove player from any entry field he is not currently in

        List<Field> entryFields = plugin.getEntryManager().getPlayerEntryFields(player);

        if (entryFields != null) {
            for (Field entryField : entryFields) {
                if (!entryField.envelops(player.getLocation())) {
                    plugin.getEntryManager().leaveField(player, entryField);

                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryField)) {
                        plugin.getEntryManager().leaveOverlappedArea(player, entryField);
                    }
                }
            }
        }

        // get all the fields the player is currently standing in

        List<Field> currentFields = plugin.getForceFieldManager().getEnabledSourceFields(player.getLocation(), FieldFlag.ALL);

        // check for prevent-entry fields and teleport him away if hes not allowed in it

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entry")) {
            for (Field field : currentFields) {
                if (FieldFlag.PREVENT_ENTRY.applies(field, player)) {
                    Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                    Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                    if (outside != null) {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(outside, FieldFlag.PREVENT_ENTRY);

                        if (f != null) {
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

        for (Field currentField : currentFields) {
            if (!plugin.getEntryManager().enteredField(player, currentField)) {
                if (!plugin.getEntryManager().containsSameNameOwnedField(player, currentField)) {
                    plugin.getEntryManager().enterOverlappedArea(player, currentField);
                }
                plugin.getEntryManager().enterField(player, currentField);
            }
        }
    }

    /**
     * Runs when a player enters an overlapped area
     *
     * @param player
     * @param field
     */
    public void enterOverlappedArea(Player player, Field field) {
        if (field.hasFlag(FieldFlag.WELCOME_MESSAGE)) {
            plugin.getCommunicationManager().showWelcomeMessage(player, field);
        }

        if (FieldFlag.TELEPORT_ON_ENTRY.applies(field, player)) {
            plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceEnter");
        }

        if (FieldFlag.GROUP_ON_ENTRY.applies(field, player)) {
            if (!field.getSettings().getGroupOnEntry().isEmpty()) {
                plugin.getPermissionsManager().addGroup(player, field.getSettings().getGroupOnEntry());
            }
        }

        if (FieldFlag.CONFISCATE_ITEMS.applies(field, player)) {
            plugin.getConfiscationManager().confiscateItems(field, player);
        }

        if (FieldFlag.ENTRY_GAME_MODE.applies(field, player)) {
            player.setGameMode(field.getSettings().getForceEntryGameMode());
        }

        if (FieldFlag.PREVENT_FLIGHT.applies(field, player)) {
            if (plugin.getSettingsManager().isNotifyFlyZones()) {
                ChatHelper.send(player, "noFlyEnter");
            }

            player.setAllowFlight(false);
        }

        if (FieldFlag.ENTRY_ALERT.applies(field, player)) {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.entryalert")) {
                if (!field.hasFlag(FieldFlag.SNEAKING_BYPASS) || !player.isSneaking()) {
                    if (!plugin.getPermissionsManager().isVanished(player)) {
                        plugin.getForceFieldManager().announceAllowedPlayers(field, ChatHelper.format("entryAnnounce", player.getName(), field.getName(), field.getCoords()));
                    }
                }
            }
        }

        if (field.hasFlag(FieldFlag.COMMANDS_ON_OVERLAP)) {
            fireEnterCommands(field, player);
        }

        if (FieldFlag.UNUSABLE_ITEMS.applies(field, player)) {
            PlayerInventory inv = player.getInventory();
            int slot = inv.getHeldItemSlot();
            ItemStack item = inv.getItem(slot);

            if (item != null) {
                if (field.getSettings().isUnusableItem(item.getTypeId(), item.getData().getData())) {
                    StackHelper.unHoldItem(player, slot);
                    ChatHelper.send(player, "cannotUseItemMoved");
                }
            }
        }
    }


    /**
     * Runs when a player leaves an overlapped area
     *
     * @param player
     * @param field
     */
    public void leaveOverlappedArea(final Player player, final Field field) {
        if (field.hasFlag(FieldFlag.FAREWELL_MESSAGE)) {
            plugin.getCommunicationManager().showFarewellMessage(player, field);
        }

        if (FieldFlag.TELEPORT_ON_EXIT.applies(field, player)) {
            plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceExit");
        }

        if (FieldFlag.GROUP_ON_ENTRY.applies(field, player)) {
            final String group = field.getSettings().getGroupOnEntry();

            if (!field.getSettings().getGroupOnEntry().isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Field sourceField = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.GROUP_ON_ENTRY);
                        boolean skip = false;

                        if (sourceField != null) {
                            if (sourceField.getSettings().getGroupOnEntry().equals(group)) {
                                skip = true;
                            }
                        }

                        if (!skip) {
                            plugin.getPermissionsManager().removeGroup(player, field.getSettings().getGroupOnEntry());
                        }
                    }
                }, 1);
            }
        }

        if (FieldFlag.CONFISCATE_ITEMS.applies(field, player)) {
            plugin.getConfiscationManager().returnItems(player);
        }

        if (FieldFlag.POTIONS.applies(field, player)) {
            HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

            for (PotionEffectType pot : potions.keySet()) {
                player.removePotionEffect(pot);
            }
        }

        if (FieldFlag.LEAVING_GAME_MODE.applies(field, player)) {
            final FieldSettings settings = field.getSettings();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.ENTRY_GAME_MODE);

                    if (field != null) {
                        player.setGameMode(settings.getForceEntryGameMode());
                    } else {
                        player.setGameMode(settings.getForceLeavingGameMode());
                    }
                }
            }, 1);
        }

        if (FieldFlag.PREVENT_FLIGHT.applies(field, player)) {
            Field sub = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_FLIGHT);

            if (sub == null) {
                if (plugin.getSettingsManager().isNotifyFlyZones()) {
                    ChatHelper.send(player, "noFlyLeave");
                }

                player.setAllowFlight(true);
            }
        }

        if (field.hasFlag(FieldFlag.COMMANDS_ON_OVERLAP)) {
            fireExitCommands(field, player);
        }
    }

    /**
     * @param player
     * @param field
     */
    public void enterField(Player player, Field field) {
        PreciousStones.debug(player.getName() + " entered a " + field.getSettings().getTitle() + " field");

        EntryFields newEntryField = new EntryFields(field);

        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(player.getName());

            if (ef != null) {
                ef.addField(field);
            } else {
                entriesByPlayer.put(player.getName(), newEntryField);
            }
        }

        if (isDynamic(field)) {
            synchronized (dynamicEntries) {
                EntryFields ef = dynamicEntries.get(player.getName());

                if (ef != null) {
                    ef.addField(field);
                } else {
                    dynamicEntries.put(player.getName(), newEntryField);
                }
            }
        }

        if (!enteredFields.contains(field)) {
            enteredFields.add(field);
        }

        enteredSingleField(player, field);
    }

    public void enteredSingleField(Player player, Field field) {
        if (!field.isDisabled()) {
            plugin.getSnitchManager().recordSnitchEntry(player, field);

            if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking())) {
                plugin.getVelocityManager().launchPlayer(player, field);
                plugin.getVelocityManager().shootPlayer(player, field);
            }

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage")) {
                if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking())) {
                    plugin.getMineManager().enterMine(player, field);
                    plugin.getLightningManager().enterLightning(player, field);
                }
            }
        }

        if (field.hasFlag(FieldFlag.MASK_ON_DISABLED)) {
            if (field.isDisabled()) {
                field.getMaskingModule().mask(player);
            } else {
                //field.getMaskingModule().unmask(player);
            }
        }

        if (field.hasFlag(FieldFlag.HIDABLE)) {
            if (field.getHidingModule().isHidden()) {
                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.hiding")) {
                    player.sendBlockChange(field.getLocation(), field.getTypeId(), (byte) field.getData());
                }
            }
        }

        if (!field.hasFlag(FieldFlag.COMMANDS_ON_OVERLAP)) {
            fireEnterCommands(field, player);
        }
    }

    /**
     * @param player
     * @param field
     */
    public void leaveField(Player player, Field field) {
        if (field == null) {
            return;
        }

        PreciousStones.debug(player.getName() + " left a " + field.getSettings().getTitle() + " field");

        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(player.getName());

            if (ef != null) {
                ef.removeField(field);

                if (ef.size() == 0) {
                    entriesByPlayer.remove(player.getName());
                }
            }
        }

        synchronized (dynamicEntries) {
            EntryFields ef = dynamicEntries.get(player.getName());

            if (ef != null) {
                ef.removeField(field);

                if (ef.size() == 0) {
                    dynamicEntries.remove(player.getName());
                }
            }
        }

        enteredFields.remove(field);

        leftSingleField(player, field);
    }

    public void leftSingleField(Player player, Field field) {
        if (!field.hasFlag(FieldFlag.COMMANDS_ON_OVERLAP)) {
            fireExitCommands(field, player);
        }
    }

    /**
     * Remove a player from all fields (used on death)
     *
     * @param player
     */
    public void leaveAllFields(Player player) {
        // remove player from all entered fields

        synchronized (entriesByPlayer) {
            if (entriesByPlayer.containsKey(player.getName())) {
                EntryFields entryFields = entriesByPlayer.get(player.getName());

                for (Field field : entryFields.getFields()) {
                    leftSingleField(player, field);
                    leaveOverlappedArea(player, field);
                }

                entriesByPlayer.remove(player.getName());
            }
        }

        synchronized (dynamicEntries) {
            if (dynamicEntries.containsKey(player.getName())) {
                dynamicEntries.remove(player.getName());
            }
        }

        // remove player from all entry groups

        List<String> allEntryGroups = plugin.getSettingsManager().getAllEntryGroups();

        for (String group : allEntryGroups) {
            plugin.getPermissionsManager().removeGroup(player, group);
        }
    }

    /**
     * Remove all players from field
     *
     * @param field
     */
    public void removeAllPlayers(Field field) {
        synchronized (entriesByPlayer) {
            for (Entry<String, EntryFields> playerEntry : entriesByPlayer.entrySet()) {
                Player player = Bukkit.getServer().getPlayerExact(playerEntry.getKey());

                if (player == null) {
                    continue;
                }

                EntryFields ef = playerEntry.getValue();
                List<Field> fields = ef.getFields();

                for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
                    Field testfield = (Field) iter.next();

                    if (field.equals(testfield)) {
                        iter.remove();
                        leftSingleField(player, field);
                        leaveOverlappedArea(player, field);
                    }
                }
            }
        }

        synchronized (dynamicEntries) {
            for (Entry<String, EntryFields> playerEntry : dynamicEntries.entrySet()) {
                Player player = Bukkit.getServer().getPlayerExact(playerEntry.getKey());

                if (player == null) {
                    continue;
                }

                EntryFields ef = playerEntry.getValue();
                List<Field> fields = ef.getFields();

                for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
                    Field testField = (Field) iter.next();

                    if (field.equals(testField)) {
                        iter.remove();
                        leftSingleField(player, field);
                        leaveOverlappedArea(player, field);
                    }
                }
            }
        }
    }

    /**
     * @param player
     * @param field
     * @return
     */
    public boolean enteredField(Player player, Field field) {
        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(player.getName());

            if (ef == null) {
                return false;
            }

            return ef.containsField(field);
        }
    }

    /**
     * @param player
     * @param field
     * @return
     */
    public boolean containsSameNameOwnedField(Player player, Field field) {
        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(player.getName());

            if (ef != null) {
                List<Field> entryFields = ef.getFields();

                for (Field entryField : entryFields) {
                    if (entryField.getOwner().equals(field.getOwner()) && entryField.getName().equals(field.getName()) && entryField.getType().equals(field.getType())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private double healthCheck(double health) {
        if (health < 0) {
            return 0;
        }

        if (health > 20) {
            return 20;
        }

        return health;
    }

    /**
     * @param field
     * @return
     */
    public boolean isInhabitant(Field field, String playerName) {
        synchronized (entriesByPlayer) {
            EntryFields ef = entriesByPlayer.get(playerName);
            List<Field> fields = ef.getFields();

            return fields.contains(field);
        }
    }

    public boolean hasInhabitants(Field field) {
        return enteredFields.contains(field);
    }

    /**
     * @param field
     * @return
     */
    public HashSet<String> getInhabitants(Field field) {
        HashSet<String> inhabitants = new HashSet<String>();

        synchronized (entriesByPlayer) {
            for (Entry<String, EntryFields> playerEntry : entriesByPlayer.entrySet()) {
                EntryFields ef = playerEntry.getValue();
                List<Field> fields = ef.getFields();

                for (Field testfield : fields) {
                    if (field.equals(testfield)) {
                        inhabitants.add(playerEntry.getKey());
                    }
                }
            }
        }

        return inhabitants;
    }

    private void fireEnterCommands(Field field, Player player) {
        if (FieldFlag.COMMAND_ON_ENTER.applies(field, player)) {
            if (!field.getSettings().getCommandsOnEnter().isEmpty()) {
                for (String cmd : field.getSettings().getCommandsOnEnter()) {
                    cmd = cmd.replace("{player}", player.getName());
                    cmd = cmd.replace("{owner}", field.getOwner());
                    cmd = cmd.replace("{x}", player.getLocation().getBlockX() + "");
                    cmd = cmd.replace("{y}", player.getLocation().getBlockY() + "");
                    cmd = cmd.replace("{z}", player.getLocation().getBlockZ() + "");
                    cmd = cmd.replace("{world}", player.getLocation().getWorld().getName());

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }

        if (FieldFlag.PLAYER_COMMAND_ON_ENTER.applies(field, player)) {
            if (!field.getSettings().getPlayerCommandsOnEnter().isEmpty()) {
                for (String cmd : field.getSettings().getPlayerCommandsOnEnter()) {
                    cmd = cmd.replace("{player}", player.getName());
                    cmd = cmd.replace("{owner}", field.getOwner());
                    cmd = cmd.replace("{x}", player.getLocation().getBlockX() + "");
                    cmd = cmd.replace("{y}", player.getLocation().getBlockY() + "");
                    cmd = cmd.replace("{z}", player.getLocation().getBlockZ() + "");
                    cmd = cmd.replace("{world}", player.getLocation().getWorld().getName());

                    player.performCommand(cmd);
                }
            }
        }
    }

    private void fireExitCommands(Field field, Player player) {
        if (FieldFlag.COMMAND_ON_EXIT.applies(field, player)) {
            if (!field.getSettings().getCommandsOnExit().isEmpty()) {
                for (String cmd : field.getSettings().getCommandsOnExit()) {
                    cmd = cmd.replace("{player}", player.getName());
                    cmd = cmd.replace("{owner}", field.getOwner());
                    cmd = cmd.replace("{x}", player.getLocation().getBlockX() + "");
                    cmd = cmd.replace("{y}", player.getLocation().getBlockY() + "");
                    cmd = cmd.replace("{z}", player.getLocation().getBlockZ() + "");
                    cmd = cmd.replace("{world}", player.getLocation().getWorld().getName());

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }

        if (FieldFlag.PLAYER_COMMAND_ON_EXIT.applies(field, player)) {
            if (!field.getSettings().getPlayerCommandsOnExit().isEmpty()) {
                for (String cmd : field.getSettings().getPlayerCommandsOnExit()) {
                    cmd = cmd.replace("{player}", player.getName());
                    cmd = cmd.replace("{owner}", field.getOwner());
                    cmd = cmd.replace("{x}", player.getLocation().getBlockX() + "");
                    cmd = cmd.replace("{y}", player.getLocation().getBlockY() + "");
                    cmd = cmd.replace("{z}", player.getLocation().getBlockZ() + "");
                    cmd = cmd.replace("{world}", player.getLocation().getWorld().getName());

                    player.performCommand(cmd);
                }
            }
        }
    }

    public void actOnInhabitantsOnDisableToggle(final Field field) {
        Set<Player> inhabitants = plugin.getForceFieldManager().getFieldInhabitants(field);

        for (final Player player : inhabitants) {
            if (player != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        if (field.isDisabled()) {
                            if (FieldFlag.POTIONS.applies(field, player)) {
                                plugin.getPotionManager().removePotions(player, field);
                            }

                            if (FieldFlag.CONFISCATE_ITEMS.applies(field, player)) {
                                plugin.getConfiscationManager().returnItems(player);
                            }
                        } else {
                            if (FieldFlag.LAUNCH.applies(field, player)) {
                                plugin.getVelocityManager().launchPlayer(player, field);
                            }

                            if (FieldFlag.CANNON.applies(field, player)) {
                                plugin.getVelocityManager().shootPlayer(player, field);
                            }

                            if (FieldFlag.POTIONS.applies(field, player)) {
                                plugin.getPotionManager().applyPotions(player, field);
                            }

                            if (FieldFlag.CONFISCATE_ITEMS.applies(field, player)) {
                                plugin.getConfiscationManager().confiscateItems(field, player);
                            }
                        }
                    }
                }, 0);
            }
        }
    }
}
