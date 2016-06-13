package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.*;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author phaed
 */
public class CommunicationManager {
    private PreciousStones plugin;
    private HashMap<String, ChatHelper> chatBlocks = new HashMap<String, ChatHelper>();

    /**
     *
     */
    public CommunicationManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * For debug output
     *
     * @param msg
     */
    public void debug(String msg) {
        PreciousStones.log("[debug] ***************** {message}", msg);
    }

    /**
     * Return a player's chat block, contains his pending chat messages
     *
     * @param player
     * @return
     */
    public ChatHelper getChatBlock(Player player) {
        ChatHelper cb = chatBlocks.get(player.getName());

        if (cb == null) {
            cb = new ChatHelper();
            chatBlocks.put(player.getName(), cb);
        }

        return cb;
    }

    /**
     * Return a new chat block for a player, overwriting old
     *
     * @param sender
     * @return
     */
    public ChatHelper getNewChatBlock(CommandSender sender) {
        ChatHelper cb = new ChatHelper();
        if (sender instanceof Player) {
            chatBlocks.put(sender.getName(), cb);
        } else {
            chatBlocks.put("console", cb);
        }
        return cb;
    }

    private boolean canNotify(Player player) {
        return !(plugin.getPermissionsManager().has(player, "preciousstones.override.notify") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canWarn(Player player) {
        return !(plugin.getPermissionsManager().has(player, "preciousstones.override.warn") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canAlert(Player player) {
        return !(plugin.getSettingsManager().isDisableAlertsForAdmins() && plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canBypassAlert(Player player) {
        return !(plugin.getSettingsManager().isDisableBypassAlertsForAdmins() && plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    public void logPayment(String owner, String renter, FieldSign s) {
        if (plugin.getSettingsManager().isLogRentsAndPurchases()) {
            PreciousStones.log("logPayment", renter, s.getPeriod(), owner, s.getField().getType(), s.getPrice(), (s.getItem() != null) ? s.getItem() : "", s.getField().getCoords());
        }
    }

    public void logPaymentCollect(String owner, String renter, FieldSign s) {
        if (plugin.getSettingsManager().isLogRentsAndPurchases()) {
            PreciousStones.log("logPaymentCollect", owner, s.getPrice(), (s.getItem() != null) ? s.getItem() : "", renter, s.getField().getCoords());
        }
    }

    public void logPurchase(String owner, String renter, PurchaseEntry purchase, FieldSign s) {
        if (plugin.getSettingsManager().isLogRentsAndPurchases()) {
            PreciousStones.log("logPurchase", renter, owner, s.getField().getSettings().getTitle(), purchase.getAmount(), purchase.isItemPayment() ? purchase.getItem() : "", purchase.getCoords());
        }
    }

    public void logPurchaseCollect(String owner, String renter, PurchaseEntry purchase) {
        if (plugin.getSettingsManager().isLogRentsAndPurchases()) {
            PreciousStones.log("logPurchaseCollect", owner, purchase.getAmount(), purchase.isItemPayment() ? purchase.getItem() : "", renter, purchase.getCoords());
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyPlaceU(Player player, Block unbreakableblock) {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyUnbreakablePlaced");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace()) {

            PreciousStones.log("logUnbreakablePlace", player.getName(), unbreakable.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logUnbreakablePlace", player.getName(), unbreakable.getDetails());
            }
        }
    }

    /**
     * @param count
     * @param field
     */
    public void notifyRollBack(Field field, int count) {
        if (field == null) {
            return;
        }

        Player player = plugin.getServer().getPlayerExact(field.getOwner());

        if (player != null) {
            if (plugin.getSettingsManager().isNotifyRollback() && canNotify(player)) {
                ChatHelper.send(player, "notifyRollbackGrief", count, field.getCoords());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogRollback()) {

            PreciousStones.log("logBlockReverted", field.getOwner(), field.getSettings().getTitle(), count, field.getCoords());

        }
    }

    /**
     * @param player
     * @return
     */
    public boolean notifyStoredTranslocations(Player player) {
        ChatHelper cb = getNewChatBlock(player);

        cb.setAlignment("l", "c");
        cb.addRow("  " + ChatColor.YELLOW + "Name", "Blocks");

        Map<String, Integer> details = plugin.getStorageManager().getTranslocationDetails(player.getName());

        for (Entry<String, Integer> detail : details.entrySet()) {
            int count = detail.getValue();

            cb.addRow("  " + ChatColor.WHITE + detail.getKey(), ChatColor.WHITE + " " + count);
        }

        if (cb.size() > 1) {
            ChatHelper.sendBlank(player);
            ChatHelper.saySingle(player, "sepStoredTranslocations");
            ChatHelper.sendBlank(player);

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(player);
                ChatHelper.send(player, "moreNextPage");
            }

            ChatHelper.sendBlank(player);
            return true;
        } else {
            ChatHelper.send(player, "translocationNotFound");
            return false;
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyApplyTranslocation(Field field, Player player, int count) {
        if (field == null) {
            return;
        }

        if (player != null) {
            if (plugin.getSettingsManager().isNotifyTranslocation() && canNotify(player)) {
                ChatHelper.send(player, "notifyTranslocatorEnabled", field.getName());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogTranslocation()) {

            PreciousStones.log("logTranslocationTranslocated", field.getOwner(), field.getName(), count, field.getCoords());

        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyClearTranslocation(Field field, Player player, int count) {
        if (field == null) {
            return;
        }

        if (player != null) {
            if (plugin.getSettingsManager().isNotifyTranslocation() && canNotify(player)) {
                ChatHelper.send(player, "notifyTranslocatorDisabled", field.getName());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogTranslocation()) {

            PreciousStones.log("logTranslocationStored", field.getOwner(), field.getName(), count, field.getCoords());

        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);
        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyFieldPlaced", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace()) {

            PreciousStones.log("logFieldPlaced", player.getName(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logFieldPlaced", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyPlaceCuboid(Player player, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyCuboidClosed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace()) {

            PreciousStones.log("logCuboidFieldPlaced", player.getName(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logCuboidFieldPlaced", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceBreakableFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyBreakablePlaced", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace()) {

            PreciousStones.log("logBreakablePlaced", player.getName(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logBreakablePlaced", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyDestroyU(Player player, Block unbreakableblock) {
        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyUnbreakableDestroyed");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy()) {

            PreciousStones.log("logDestroyedOwnUnbreakable", player.getName(), Helper.getDetails(unbreakableblock));

        }
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyedOwnUnbreakable", player.getName(), Helper.getDetails(unbreakableblock));
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyFieldDestroyed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy()) {

            PreciousStones.log("logDestroyedOwnField", player.getName(), fs.getTitle(), field.getDetails());

        }
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyedOwnField", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyOthersFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyFieldDestroyed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy()) {

            PreciousStones.log("logDestroyOthers", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyOthers", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyBreakableFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyBreakableDestroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy()) {

            PreciousStones.log("logDestroyBreakableField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyBreakableField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassPlace(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyBypassPlaced", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace()) {

            PreciousStones.log("logDestroyedOthersField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyedOthersField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param loc
     * @param field
     */
    public void notifyPaintingBypassPlace(Player player, Location loc, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyBypassPlaced", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace()) {

            PreciousStones.log("logBypassPlacedPainting", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassPlacedPainting", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param loc
     * @param field
     */
    public void notifyVehicleBypassCreate(Player player, Location loc, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyBypassCreateVehicle", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace()) {

            PreciousStones.log("logBypassCreateVehicle", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassCreateVehicle", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassPlaceU(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player)) {
            ChatHelper.send(player, "notifyUnbreakableBypassPlaced", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace()) {

            PreciousStones.log("logBypassPlacedUnbreakable", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassPlacedUnbreakable", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassDestroy(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyBypassDestroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy()) {

            PreciousStones.log("logBypassDestroy", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassDestroy", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassDestroyVehicle(Player player, Vehicle block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyVehicleBypassDestroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy()) {

            PreciousStones.log("logBypassDestroyVehicle", player.getName(), (new Vec(block.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassDestroyVehicle", player.getName(), (new Vec(block.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyBypassDestroyU(Player player, Block unbreakableblock) {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyUnbreakableBypassDestroyed", unbreakable.getOwner());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy()) {

            PreciousStones.log("logBypassDestroyUnbreakable", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassDestroyUnbreakable", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyBypassDestroyFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player)) {
            ChatHelper.send(player, "notifyFieldBypassDestroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log-destroy")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy()) {

            PreciousStones.log("logBypassDestroyField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassDestroyField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void warnEntry(Player player, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnEntry() && canWarn(player)) {
            ChatHelper.send(player, "warnEnterProtectedArea");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogEntry()) {

            PreciousStones.log("logEntry", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.entry") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logEntry", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnFire(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnFire() && canWarn(player)) {
            ChatHelper.send(player, "warnPlaceFires");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogFire()) {

            PreciousStones.log("warnFire", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.fire") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "warnFire", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnPlace(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnPlace");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {

            PreciousStones.log("logPlace", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlace", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }


    /**
     * @param player
     * @param hanging
     * @param field
     */
    public void warnPlaceHanging(Player player, Hanging hanging, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnPlace");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {
            PreciousStones.log("logPlace", player.getName(), (new Vec(hanging.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlace", player.getName(), (new Vec(hanging.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param item
     * @param loc
     * @param field
     */
    public void warnPlaceItem(Player player, ItemStack item, Location loc, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnPlace");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {
            PreciousStones.log("logPlace", player.getName(), (new Vec(loc)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlace", player.getName(), (new Vec(loc)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param item
     * @param loc
     * @param field
     */
    public void warnCreateVehicle(Player player, Location loc, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnCreateVehicle");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {
            PreciousStones.log("logCreateVehicle", player.getName(), (new Vec(loc)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logCreateVehicle", player.getName(), (new Vec(loc)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }


    /**
     * @param player
     * @param painting
     * @param field
     */
    public void warnPlacePainting(Player player, Painting painting, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnPlace");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {

            PreciousStones.log("logPlace", player.getName(), (new Vec(painting.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlace", player.getName(), (new Vec(painting.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnUse(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUse() && canWarn(player)) {
            ChatHelper.send(player, "warnUse");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUse()) {

            PreciousStones.log("logUse", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.use") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logUse", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnEmpty(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player)) {
            ChatHelper.send(player, "warnEmpty");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea()) {

            PreciousStones.log("logBucketEmpty", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logBucketEmpty", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void warnDestroyU(Player player, Block unbreakableblock) {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isWarnDestroy() && canWarn(player)) {
            ChatHelper.send(player, "warnDestroyUnbreakable");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy()) {

            PreciousStones.log("logDestroyUnbreakable", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyUnbreakable", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void warnDestroyFF(Player player, Block fieldblock) {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field != null) {
            FieldSettings fs = field.getSettings();

            if (plugin.getSettingsManager().isWarnDestroy() && canWarn(player)) {
                ChatHelper.send(player, "warnOwnerRemove");
            }

            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
                return;
            }

            if (plugin.getSettingsManager().isLogDestroy()) {

                PreciousStones.log("logDestroyField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

            }

            for (Player pl : plugin.getServer().getOnlinePlayers()) {
                if (pl.equals(player)) {
                    continue;
                }

                if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl)) {
                    ChatHelper.sendPs(pl, "logDestroyField", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
                }
            }
        }
    }

    /**
     * @param player
     * @param damagedblock
     * @param field
     */
    public void warnDestroyArea(Player player, Block damagedblock, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player)) {
            ChatHelper.send(player, "warnDestroy");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea()) {

            PreciousStones.log("logDestroyInField", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyInField", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param hanging
     * @param field
     */
    public void warnDestroyHanging(Player player, Hanging hanging, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player)) {
            ChatHelper.send(player, "warnDestroy");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea()) {

            PreciousStones.log("logDestroyInField", player.getName(), (new Vec(hanging.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyInField", player.getName(), (new Vec(hanging.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param painting
     * @param field
     */
    public void warnDestroyPainting(Player player, Painting painting, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player)) {
            ChatHelper.send(player, "warnDestroy");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea()) {

            PreciousStones.log("logDestroyInField", player.getName(), (new Vec(painting.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyInField", player.getName(), (new Vec(painting.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param vehicle
     * @param field
     */
    public void warnDestroyVehicle(Player player, Vehicle vehicle, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player)) {
            ChatHelper.send(player, "warnDestroyVehicle");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea()) {

            PreciousStones.log("logDestroyVehicle", player.getName(), (new Vec(vehicle.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logDestroyVehicle", player.getName(), (new Vec(vehicle.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictU(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (canWarn(player)) {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting")) {
                ChatHelper.send(player, "warnConflictUnbreakablePlace", field.getOwner(), field.getSettings().getTitle(), field.getDetails());
            } else {
                ChatHelper.send(player, "warnConflictUnbreakablePlace2");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace()) {

            PreciousStones.log("logPlaceUnbreakableConflict", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceUnbreakableConflict", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictFF(Player player, Block block, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        FieldSettings fsconflict = plugin.getSettingsManager().getFieldSettings(block);

        if (fsconflict == null) {
            return;
        }

        if (canWarn(player)) {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting")) {
                ChatHelper.send(player, "warnConflictFieldPlace", field.getOwner(), field.getSettings().getTitle(), field.getDetails());
            } else {
                ChatHelper.send(player, "warnConflictFieldPlace2");
            }
        }
        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace()) {

            PreciousStones.log("logPlaceFieldConflit", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceFieldConflit", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnPvP(Player attacker, Player victim, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPvp() && canWarn(attacker)) {
            ChatHelper.send(attacker, "warnPvP");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogPvp()) {

            PreciousStones.log("logPvP", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(attacker)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPvP", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param attacker
     * @param victim
     */
    public void warnBypassPvPDueToCombat(Player attacker, Player victim) {

        if (canNotify(attacker)) {
            ChatHelper.send(attacker, "warnProtectionIgnored");
        }

        if (canNotify(victim)) {
            ChatHelper.send(victim, "warnProtectionIgnored");
        }
    }

    /**
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnBypassPvP(Player attacker, Player victim, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPvp() && canNotify(attacker)) {
            ChatHelper.send(attacker, "notifyPvPBypass");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPvp()) {

            PreciousStones.log("logBypassAttack", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());

        }
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(attacker)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassAttack", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void warnFieldPlaceUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(protectionblock);

        if (fs == null) {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnFieldPlaceUnprotectableTouching", Helper.friendlyBlockType(unprotectableblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceUnprotectableTouchingField", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceUnprotectableTouchingField", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void warnUnbreakablePlaceUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock) {
        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnUnbreakablePlaceUnprotectableTouching", Helper.friendlyBlockType(unprotectableblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceUnprotectableTouchingUnbreakable", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceUnprotectableTouchingUnbreakable", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void warnUnbreakablePlaceTouchingUnprotectable(Player player, Block placedblock) {
        Block touchingblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnCannotProtect", Helper.friendlyBlockType(touchingblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceTouchingUnbreakableUnprotectable", player.getName(), Helper.getDetails(touchingblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceTouchingUnbreakableUnprotectable", player.getName(), Helper.getDetails(touchingblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void warnFieldPlaceTouchingUnprotectable(Player player, Block placedblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(placedblock);

        if (fs == null) {
            return;
        }

        Block touchingblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnCannotProtect", Helper.friendlyBlockType(touchingblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceTouchingFieldUnprotectable", player.getName(), Helper.getDetails(touchingblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceTouchingFieldUnprotectable", player.getName(), Helper.getDetails(touchingblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void warnPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnCannotProtectInside", Helper.friendlyBlockType(unprotectableblock.getType().getId()), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceUnprotectableInField", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceUnprotectableInField", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void warnPlaceFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldtypeblock);

        if (fs == null) {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnPlaceFieldInUnprotectable", fs.getTitle(), Helper.friendlyBlockType(unprotectableblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logPlaceFieldUnprotectableInArea", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl)) {
                ChatHelper.sendPs(pl, "logPlaceFieldUnprotectableInArea", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void notifyUnbreakableBypassUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock) {
        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnBypassPlacedUnprotectableInUnbreakable", Helper.friendlyBlockType(unprotectableblock.getType().getId()), Helper.friendlyBlockType(protectionblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logUnbreakableBypassUnprotectableTouching", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logUnbreakableBypassUnprotectableTouching", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void notifyFieldBypassUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(protectionblock);

        if (fs == null) {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnBypassPlacedUnprotectableInField", Helper.friendlyBlockType(unprotectableblock.getType().getId()), Helper.friendlyBlockType(protectionblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logFieldBypassUnprotectableTouching", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logFieldBypassUnprotectableTouching", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void notifyBypassTouchingUnprotectable(Player player, Block placedblock) {
        Block unprotectableblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnUnprotectableBypassProtected", Helper.friendlyBlockType(unprotectableblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logBypassTouchingUnprotectable", player.getName(), Helper.getDetails(placedblock), Helper.getDetails(unprotectableblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassTouchingUnprotectable", player.getName(), Helper.getDetails(placedblock), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void notifyBypassPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field) {
        if (field == null) {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnUnprotectableBypassPlaced", Helper.friendlyBlockType(unprotectableblock.getType().getId()), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logBypassPlaceUnprotectableInField", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
        }


        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassPlaceUnprotectableInField", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void notifyBypassFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldtypeblock);

        if (fs == null) {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player)) {
            ChatHelper.send(player, "warnFieldBypassPlacedUnprotectable", fs.getTitle(), Helper.friendlyBlockType(unprotectableblock.getType().getId()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log")) {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable()) {

            PreciousStones.log("logBypassFieldInUnprotectable", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));

        }

        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl.equals(player)) {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl)) {
                ChatHelper.sendPs(pl, "logBypassFieldInUnprotectable", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void showWelcomeMessage(Player player, Field field) {
        if (field == null) {
            return;
        }

        if (field.isNamed()) {
            List<String> renters = field.getRenters();
            if (renters != null && !renters.isEmpty()) {
                ChatHelper.send(player, "enteringRentedNamedField", field.getName(), Helper.toMessage(renters, ", "));
            } else {
                ChatHelper.send(player, "enteringNamedField", field.getName());
            }
        } else {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages()) {
                List<String> renters = field.getRenters();
                if (renters != null && !renters.isEmpty()) {
                    ChatHelper.send(player, "enteringField", renters.get(0), field.getSettings().getTitle());
                } else {
                    ChatHelper.send(player, "enteringField", field.getOwner(), field.getSettings().getTitle());
                }
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void showFarewellMessage(Player player, Field field) {
        if (field == null) {
            return;
        }

        if (field.isNamed()) {
            List<String> renters = field.getRenters();
            if (renters != null && !renters.isEmpty()) {
                ChatHelper.send(player, "leavingRentedNamedField", field.getName(), Helper.toMessage(renters, ", "));
            } else {
                ChatHelper.send(player, "leavingNamedField", field.getName());
            }
        } else {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages()) {
                List<String> renters = field.getRenters();
                if (renters != null && !renters.isEmpty()) {
                    ChatHelper.send(player, "leavingField", renters.get(0), field.getSettings().getTitle());
                } else {
                    ChatHelper.send(player, "leavingField", field.getOwner(), field.getSettings().getTitle());
                }
            }
        }
    }

    /**
     * @param sender
     */
    public void showNotFound(CommandSender sender) {
        ChatHelper.send(sender, "noFieldsFound");
    }

    /**
     * @param player
     */
    public void showNoPotion(Player player, String potion) {
        if (canWarn(player)) {
            ChatHelper.send(player, "potionNeutralized", potion.toLowerCase().replace("_", " "));
        }
    }

    /**
     * @param player
     */
    public void showDamage(Player player) {
        if (plugin.getSettingsManager().isWarnFastDamage() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoDamage");
        }
    }

    /**
     * @param player
     */
    public void showHeal(Player player) {
        if (plugin.getSettingsManager().isWarnInstantHeal() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoHealed");
        }
    }

    /**
     * @param player
     */
    public void showGiveAir(Player player) {
        if (plugin.getSettingsManager().isWarnGiveAir() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoAir");
        }
    }

    /**
     * @param player
     */
    public void showLaunch(Player player) {
        if (plugin.getSettingsManager().isWarnLaunch() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoLaunch");
        }
    }

    /**
     * @param player
     */
    public void showCannon(Player player) {
        if (plugin.getSettingsManager().isWarnCannon() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoCannon");
        }
    }

    /**
     * @param player
     */
    public void showMine(Player player) {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoMine");
        }
    }

    /**
     * @param player
     */
    public void showLightning(Player player) {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoLightning");
        }
    }

    /**
     * @param player
     */
    public void showThump(Player player) {
        ChatHelper.send(player, "notifyDoFall");
    }

    /**
     * @param player
     */
    public void showFeeding(Player player) {
        if (plugin.getSettingsManager().isWarnSlowFeeding() && canWarn(player)) {
            ChatHelper.send(player, "notifyDoFeeding");
        }
    }

    /**
     * @param player
     */
    public void showRepair(Player player) {
        if (plugin.getSettingsManager().isWarnSlowRepair() && canWarn(player)) {
            ChatHelper.send(player, "notifyRepairing");
        }
    }

    /**
     * @param player
     * @param block
     */
    public void showUnbreakableDetails(Player player, Block block) {
        ChatHelper.send(player, "showOwner", plugin.getUnbreakableManager().getOwner(block));
    }

    /**
     * @param block
     * @param player
     */
    public void showProtectedLocation(Player player, Block block) {
        List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

        ChatHelper.sendBlank(player);
        ChatHelper.send(player, "showProtected");

        for (Field field : fields) {
            ChatHelper.send(player, "showProtectedLocations", field.getSettings().getTitle(), field.getCleanCoords());
        }
    }

    /**
     * @param player
     * @param block
     */
    public void showFieldOwner(Player player, Block block) {
        ChatHelper.send(player, "showOwner", plugin.getForceFieldManager().getOwner(block));
    }

    /**
     * @param unbreakable
     * @param player
     */
    public void showUnbreakableDetails(Unbreakable unbreakable, Player player) {
        ChatHelper.sendBlank(player);
        ChatHelper.send(player, "showOwner", unbreakable.getOwner());
    }

    /**
     * @param player
     * @param fields
     */
    public void showFieldDetails(Player player, List<Field> fields) {
        ChatHelper cb = getNewChatBlock(player);

        for (Field field : fields) {
            cb.addRow("", "", "");

            ChatColor color = field.isDisabled() ? ChatColor.RED : ChatColor.YELLOW;

            if (field.isDisabled()) {
                cb.addRow("  " + ChatColor.RED + ChatHelper.format("_fieldDisabled"), "", "");
            }
            FieldSettings fs = field.getSettings();

            cb.addRow("  " + color + ChatHelper.format("_type") + ": ", ChatColor.AQUA + fs.getTitle(), "");

            if (fs.hasNameableFlag() && field.isNamed()) {
                cb.addRow("  " + color + ChatHelper.format("_name") + ": ", ChatColor.AQUA + field.getName(), "");
            }

            cb.addRow("  " + color + ChatHelper.format("_owner") + ": ", ChatColor.AQUA + field.getOwner(), "");

            cb.addRow("  " + color + ChatHelper.format("_location") + ": ", ChatColor.AQUA + "" + field.getX() + " " + field.getY() + " " + field.getZ(), "");
        }

        if (cb.size() > 0) {
            cb.addRow("", "", "", "");

            ChatHelper.sendBlank(player);
            ChatHelper.saySingle(player, "sepFieldInfo");

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.send(player, "moreNextPage");
            }
        }
    }

    /**
     * @param player
     * @param field
     * @return
     */
    public boolean showFieldDetails(Player player, Field field) {
        if (field == null) {
            return false;
        }

        ChatHelper cb = getNewChatBlock(player);
        FieldSettings fs = field.getSettings();

        cb.addRow("", "", "");

        ChatColor color = field.isDisabled() ? ChatColor.RED : ChatColor.YELLOW;

        boolean showMessage = true;

        cb.addRow("  " + color + ChatHelper.format("_type") + ": ", ChatColor.AQUA + fs.getTitle(), "");

        if (fs.hasMetaName()) {
            List<String> lore = fs.getMetaLore();

            boolean firstAdded = false;

            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i) == null || lore.get(i).isEmpty()) {
                    continue;
                }

                if (!firstAdded) {
                    cb.addRow("  " + color + ChatHelper.format("_lore") + ": ", lore.get(i), "");
                    firstAdded = true;
                } else

                {
                    cb.addRow("  ", ChatColor.AQUA + lore.get(i), "");
                }
            }
        }

        if (fs.hasNameableFlag()) {
            if (field.isNamed()) {
                cb.addRow("  " + color + ChatHelper.format("_name") + ": ", ChatColor.AQUA + field.getName(), "");
            } else {
                cb.addRow("  " + color + ChatHelper.format("_name") + ": ", ChatColor.GRAY + ChatHelper.format("_none"), "");
            }
        }

        if (!field.getSettings().getDeleteIfNoPermission().isEmpty()) {
            cb.addRow("  " + color + ChatHelper.format("_for") + ": ", ChatColor.AQUA + "" + field.getSettings().getDeleteIfNoPermission());
        }

        cb.addRow("  " + color + ChatHelper.format("_owner") + ": ", ChatColor.AQUA + field.getOwner(), "");

        List<String> allowed = field.getAllowed();
        if (!allowed.isEmpty()) {

            int rows = (int) Math.max(Math.ceil(allowed.size() / 2.0), 1);

            for (int i = 0; i < rows; i++) {
                String title = "";

                if (i == 0) {
                    title = color + ChatHelper.format("_allowed") + ": ";
                }

                cb.addRow("  " + title, ChatColor.WHITE + getAllowed(allowed, i * 2), getAllowed(allowed, (i * 2) + 1));
            }
        }

        if (field.hasFlag(FieldFlag.CUBOID)) {
            cb.addRow("  " + color + ChatHelper.format("_dimensions") + ": ", ChatColor.AQUA + "" + (field.getMaxx() - field.getMinx() + 1) + "x" + (field.getMaxy() - field.getMiny() + 1) + "x" + (field.getMaxz() - field.getMinz() + 1), "");
        } else {
            cb.addRow("  " + color + ChatHelper.format("_dimensions") + ": ", ChatColor.AQUA + "" + ((field.getRadius() * 2) + 1) + "x" + field.getHeight() + "x" + ((field.getRadius() * 2) + 1), "");
        }

        if (field.getVelocity() > 0) {
            cb.addRow("  " + color + ChatHelper.format("_velocity") + ": ", ChatColor.AQUA + "" + field.getVelocity(), "");
        }

        if (field.getRevertingModule().getRevertSecs() > 0) {
            cb.addRow("  " + color + ChatHelper.format("_interval") + ": ", ChatColor.AQUA + "" + field.getRevertingModule().getRevertSecs(), "");
        }

        if (field.getListingModule().hasBlacklistedComands()) {
            cb.addRow("  " + color + ChatHelper.format("_blacklistedCommands") + ": ", ChatColor.AQUA + "" + field.getListingModule().getBlacklistedCommandsList(), "");
        }

        cb.addRow("  " + color + ChatHelper.format("_location") + ": ", ChatColor.AQUA + "" + field.getX() + " " + field.getY() + " " + field.getZ(), "");

        List<FieldFlag> flags = new ArrayList<FieldFlag>(field.getFlagsModule().getFlags());
        List<FieldFlag> insertedFlags = field.getFlagsModule().getInsertedFlags();
        List<FieldFlag> disabledFlags = field.getFlagsModule().getDisabledFlags();

        flags.addAll(insertedFlags);
        flags.addAll(disabledFlags);

        for (FieldFlag hid : FieldFlag.getHidden()) {
            flags.remove(hid);
        }

        boolean addedTitle = false;

        for (FieldFlag flag : flags) {
            if (flag == null) {
                continue;
            }

            String title = "";

            if (!addedTitle) {
                title = color + ChatHelper.format("_flags") + ": ";
                addedTitle = true;
            }

            ChatColor c = ChatColor.WHITE;

            if (disabledFlags.contains(flag)) {
                c = ChatColor.DARK_GRAY;
            }

            if (flag.isUnToggable()) {
                c = ChatColor.AQUA;
            }

            String flagStr = Helper.toFlagStr(flag);

            if (field.getSettings().isReversedFlag(flag)) {
                flagStr = "~" + flagStr;
            }

            if (field.getSettings().isAlledFlag(flag)) {
                flagStr = "^" + flagStr;
            }

            cb.addRow("  " + title, c + flagStr);
        }

        if (field.hasFlag(FieldFlag.POTIONS)) {
            cb.addRow("  " + color + ChatHelper.format("_potions") + ": ", ChatColor.WHITE + field.getSettings().getPotionString(), "");
        }

        if (field.hasFlag(FieldFlag.NEUTRALIZE_POTIONS)) {
            cb.addRow("  " + color + ChatHelper.format("_neutralizes") + ": ", ChatColor.WHITE + field.getSettings().getNeutralizePotionString(), "");
        }

        if (cb.size() > 0) {
            cb.addRow("", "", "");
            ChatHelper.sendBlank(player);

            if (field.isDisabled()) {
                ChatHelper.saySingle(player, "sepFieldInfoDisabled");
            } else {
                ChatHelper.saySingle(player, "sepFieldInfo");
            }

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(player);
                ChatHelper.send(player, "moreNextPage");
            }

            if (field.isDisabled()) {
                ChatHelper.sendBlank(player);
                showMessage = false;
            }
        }

        return showMessage;
    }

    public void showRenterInfo(Player player, Field field) {
        if (field == null) {
            return;
        }

        ChatHelper cb = getNewChatBlock(player);

        if (!field.getRenters().isEmpty()) {
            List<String> renters = field.getRenters();

            int rows = (int) Math.max(Math.ceil(renters.size() / 2.0), 1);

            for (int i = 0; i < rows; i++) {
                String title = "";

                if (i == 0) {
                    if (renters.size() == 1) {
                        title = ChatColor.YELLOW + ChatHelper.format("_tenant") + ": ";
                    } else {
                        title = ChatColor.YELLOW + ChatHelper.format("_tenants") + ": ";
                    }
                }

                cb.addRow("  " + title, ChatColor.WHITE + getRenters(field, i * 2), getRenters(field, (i * 2) + 1));
            }
        }

        if (field.getRentingModule().getLimitSeconds() > 0) {
            cb.addRow("", "", "");
            cb.addRow("  " + ChatColor.YELLOW + ChatHelper.format("_rentingLimit") + ": ", SignHelper.secondsToPeriods(field.getRentingModule().getLimitSeconds()));
        }

        if (cb.size() > 0) {
            cb.sendBlock(player);
            ChatHelper.sendBlank(player);
        }
    }

    private String getAllowed(List<String> allowed, int index) {
        if (index < allowed.size()) {
            return allowed.get(index);
        }

        return "";
    }

    private String getRenters(Field field, int index) {
        List<RentEntry> entries = field.getRentingModule().getRenterEntries();

        if (index < entries.size()) {
            RentEntry entry = entries.get(index);

            return ChatColor.WHITE + entry.getPlayerName() + ChatColor.DARK_AQUA + " (" + SignHelper.secondsToPeriods(entry.remainingRent()) + ")";
        }

        return "";
    }

    /**
     * Shows all the configured fields to the player
     *
     * @param player
     */
    public void showConfiguredFields(CommandSender player) {
        ChatHelper cb = getNewChatBlock(player);

        HashMap<BlockTypeEntry, FieldSettings> fss = plugin.getSettingsManager().getFieldSettings();

        for (FieldSettings fs : fss.values()) {
            String customHeight = fs.getCustomHeight() > 0 ? " " + ChatHelper.format("headerConfiguredFieldsHeight", fs.getCustomHeight()) : "";
            String customVolume = fs.getCustomVolume() > 0 ? " " + ChatHelper.format("headerConfiguredFieldsVolume", fs.getCustomVolume()) : "";

            BlockTypeEntry entry = new BlockTypeEntry(fs.getTypeId(), fs.getData());

            if (entry.isValid()) {
                cb.addRow(ChatHelper.format("headerConfiguredFields", fs.getTitle(), entry, fs.getRadius()) + customHeight + customVolume);
            }
        }

        if (cb.size() > 0) {
            ChatHelper.sendBlank(player);
            ChatHelper.saySingle(player, "sepConfiguredFields");
            ChatHelper.sendBlank(player);

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(player);
                ChatHelper.send(player, "moreNextPage");
            }

            ChatHelper.sendBlank(player);
        }
    }

    /**
     * @param sender
     * @param type
     * @return
     */
    public boolean showCounts(CommandSender sender, BlockTypeEntry type) {
        if (!(sender instanceof Player)) {
            //sender = new ColouredConsoleSender((CraftServer)Bukkit.getServer());
            sender = Bukkit.getServer().getConsoleSender();
        }

        ChatHelper cb = getNewChatBlock(sender);

        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(type);

        if (fs == null) {
            return false;
        }

        Map<String, List<Field>> fieldsByOwner = plugin.getForceFieldManager().getFieldsByOwner();

        cb.setAlignment("l", "c");

        cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_name"), ChatHelper.format("_count"));

        for (Entry<String, List<Field>> playerFields : fieldsByOwner.entrySet()) {
            int count = playerFields.getValue().size();

            if (count > 0) {
                cb.addRow("  " + ChatColor.AQUA + playerFields.getKey(), ChatColor.WHITE + " " + count);
            }
        }

        if (cb.size() > 1) {
            ChatHelper.sendBlank(sender);
            ChatHelper.saySingle(sender, "sepCounts", fs.getTitle());
            ChatHelper.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(sender);
                ChatHelper.send(sender, "moreNextPage");
            }

            ChatHelper.sendBlank(sender);
        } else {
            ChatHelper.send(sender, "noFieldsFound");
        }

        return true;
    }

    /**
     * Shows a target's counts to a player
     *
     * @param sender seeing the counts
     * @param target
     * @return
     */
    public boolean showFieldCounts(CommandSender sender, String target) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender = Bukkit.getServer().getConsoleSender();
        }

        ChatHelper cb = getNewChatBlock(sender);

        boolean showLimits = player != null && player.getName().equalsIgnoreCase(target) && plugin.getSettingsManager().haveLimits();

        if (showLimits) {
            cb.setAlignment("l", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_field"), ChatHelper.format("_count"), ChatHelper.format("_limit"));
        } else {
            cb.setAlignment("l", "c");
            cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_field"), ChatHelper.format("_count"));
        }

        HashMap<BlockTypeEntry, Integer> fieldCounts;

        fieldCounts = plugin.getForceFieldManager().getFieldCounts(target);

        for (Entry<BlockTypeEntry, Integer> fieldCount : fieldCounts.entrySet()) {
            int count = fieldCount.getValue();

            if (count == 0) {
                continue;
            }

            FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldCount.getKey());

            if (fs == null) {
                continue;
            }

            int limit = plugin.getLimitManager().getLimit(player, fs);

            ChatColor color = (count < limit) || limit == -1 ? ChatColor.WHITE : ChatColor.DARK_RED;

            String strLimit = limit == -1 ? "-" : limit + "";

            if (plugin.getSettingsManager().haveLimits()) {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), "{yellow} " + count, color + " " + strLimit);
            } else {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + " " + count);
            }
        }

        String targetName = target;

        if (target.contains(":")) {
            targetName = target.substring(2);
        } else if (target.contains("*")) {
            targetName = ChatHelper.format("_everyone");
        }

        if (cb.size() > 1) {
            ChatHelper.sendBlank(sender);
            ChatHelper.saySingle(sender, "sepFieldCounts", targetName);
            ChatHelper.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(sender);
                ChatHelper.send(sender, "moreNextPage");
            }

            ChatHelper.sendBlank(sender);
        } else {
            ChatHelper.send(sender, "noFieldsFound");
        }

        return true;
    }

    /**
     * Show a player's field locations by type to a player
     *
     * @param sender the player to show the list to
     * @param typeid use -1 to show all types
     * @param target
     */
    public void showFieldLocations(CommandSender sender, int typeid, String target) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender = Bukkit.getServer().getConsoleSender();
        }

        ChatHelper cb = getNewChatBlock(sender);
        boolean admin = player == null || !player.getName().equalsIgnoreCase(target);
        Location center = player == null ? new Location(plugin.getServer().getWorlds().get(0), 0, 0, 0) : player.getLocation();

        if (admin) {
            cb.setAlignment("l", "c", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_field"), ChatHelper.format("_distance"), ChatHelper.format("_coords"), ChatHelper.format("_owner"));
        } else {
            cb.setAlignment("l", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_field"), ChatHelper.format("_distance"), ChatHelper.format("_coords"));
        }

        List<Field> fields = new ArrayList<Field>();

        if (player != null) {
            fields = plugin.getForceFieldManager().getFields(target, player.getWorld());
        } else {
            for (World world : plugin.getServer().getWorlds()) {
                fields.addAll(plugin.getForceFieldManager().getFields(target, world));
            }
        }

        sortByDistance(fields, center);

        // if type id supplied, then only show fields of that typeid

        if (typeid != -1) {
            for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
                Field testfield = (Field) iter.next();

                if (typeid != testfield.getTypeId()) {
                    iter.remove();
                }
            }
        }

        String targetName = target;

        if (target.contains(":")) {
            targetName = target.substring(2);
        } else if (target.contains("*")) {
            targetName = ChatHelper.format("_everyone");
        }

        for (Field field : fields) {
            int distance = (int) field.distance(center);
            FieldSettings fs = field.getSettings();

            if (admin) {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + "" + distance, ChatColor.YELLOW + Helper.toLocationString(field.getLocation()), ChatColor.WHITE + field.getOwner());
            } else {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + "" + distance, ChatColor.YELLOW + Helper.toLocationString(field.getLocation()));
            }
        }

        if (cb.size() > 1) {
            ChatHelper.sendBlank(sender);

            if (player != null) {
                ChatHelper.saySingle(sender, "sepFieldLocations", targetName, player.getWorld().getName());
            } else {
                ChatHelper.saySingle(sender, "sepFieldLocations", targetName, "");
            }

            ChatHelper.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(sender);
                ChatHelper.send(sender, "moreNextPage");
            }

            ChatHelper.sendBlank(sender);
        } else {
            ChatHelper.send(sender, "noFieldsFound");
        }
    }

    /**
     * Sort clan players by KDR
     *
     * @param fields
     * @param playerLocation
     * @return
     */

    public void sortByDistance(List<Field> fields, final Location playerLocation) {
        Collections.sort(fields, new Comparator<Field>() {
            public int compare(Field f1, Field f2) {
                Float o1 = (float) f1.distance(playerLocation);
                Float o2 = (float) f2.distance(playerLocation);

                return o1.compareTo(o2);
            }
        });
    }

    /**
     * @param player
     * @param field
     * @return
     */
    public boolean showSnitchList(Player player, Field field) {
        if (field != null) {
            List<SnitchEntry> snitches = field.getSnitchingModule().getSnitches();

            if (snitches.isEmpty() || snitches.get(0).getAgeInSeconds() > 10) {
                snitches = plugin.getStorageManager().getSnitchEntries(field);
                field.updateLastUsed();
                plugin.getStorageManager().offerField(field);
            }

            String title = ChatHelper.format("_intruderLog") + " ";

            if (!snitches.isEmpty()) {
                ChatHelper cb = getNewChatBlock(player);

                ChatHelper.sendBlank(player);
                ChatHelper.saySingle(player, ChatColor.WHITE + title + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                ChatHelper.sendBlank(player);

                cb.addRow("  " + ChatColor.GRAY + ChatHelper.format("_name"), ChatHelper.format("_reason"), ChatHelper.format("_details"));

                for (SnitchEntry se : snitches) {
                    cb.addRow("  " + ChatColor.GOLD + se.getName(), se.getReasonDisplay(), ChatColor.WHITE + se.getDetails());
                }

                boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

                if (more) {
                    ChatHelper.sendBlank(player);
                    ChatHelper.send(player, "moreNextPage");
                }

                ChatHelper.sendBlank(player);
            }

            return !snitches.isEmpty();
        } else {
            showNotFound(player);
        }

        return false;
    }

    /**
     * @param sender
     */
    public void showMenu(CommandSender sender) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender = Bukkit.getServer().getConsoleSender();
        }

        boolean hasPlayer = player != null;

        ChatHelper cb = getNewChatBlock(sender);

        cb.addRow("menuIdentifiers");
        cb.addRow("");

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.fields")) {
            cb.addRow("menu40");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasPlayer) {
            cb.addRow("menu1");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.disable") && hasPlayer) {
            cb.addRow("menu2");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.info") && hasPlayer) {
            cb.addRow("menu34");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasPlayer) {
            cb.addRow("menu3");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasPlayer) {
            cb.addRow("menu4");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasPlayer) {
            cb.addRow("menu5");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasPlayer) {
            cb.addRow("menu6");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasPlayer) {
            cb.addRow("menu7");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasPlayer) {
            cb.addRow("menu8");
        }

        if (plugin.getSettingsManager().haveLimits() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasPlayer) {
            cb.addRow("menu9");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.locations") && hasPlayer) {
            cb.addRow("menu10");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasPlayer) {
            cb.addRow("menu11");
        }

        if (plugin.getSettingsManager().isCommandsToRentBuy()) {
            cb.addRow("menu60");
            cb.addRow("menu61");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.change-owner") && hasPlayer) {
            cb.addRow("menu12");
        }

        if (plugin.getSettingsManager().haveNameable() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasPlayer) {
            cb.addRow("menu13");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasPlayer) {
            cb.addRow("menu14");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.expand") && hasPlayer) {
            cb.addRow("menu64");
            cb.addRow("menu65");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.contract") && hasPlayer) {
            cb.addRow("menu66");
            cb.addRow("menu67");
        }

        if (plugin.getSettingsManager().haveVelocity() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasPlayer) {
            cb.addRow("menu15");
        }

        if (plugin.getSettingsManager().haveGriefRevert() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasPlayer) {
            cb.addRow("menu16");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.setlimit") && hasPlayer) {
            cb.addRow("menu54");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.blacklistcommand") && hasPlayer) {
            cb.addRow("menu57");
            cb.addRow("menu58");
        }

        if (plugin.getSettingsManager().haveSnitch() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasPlayer) {
            cb.addRow("menu17");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasPlayer) {
            cb.addRow("menu18");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasPlayer) {
            cb.addRow("menu19");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasPlayer) {
            cb.addRow("menu20");
        }

        if ((plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.mark")) && hasPlayer) {
            cb.addRow("menu21");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.openclose.forcefield") && hasPlayer) {
            cb.addRow("menu53");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.use") && hasPlayer) {
            cb.addRow("menu22");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.hide") && hasPlayer) {
            cb.addRow("menu49");
            cb.addRow("menu50");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.take") && hasPlayer) {
            cb.addRow("menu68");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.hideall") && hasPlayer) {
            cb.addRow("menu51");
            cb.addRow("menu52");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.import") && hasPlayer) {
            cb.addRow("menu23");
            cb.addRow("menu24");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.delete") && hasPlayer) {
            cb.addRow("menu25");
            cb.addRow("menu26");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.remove") && hasPlayer) {
            cb.addRow("menu27");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink") && hasPlayer) {
            cb.addRow("menu28");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.toggle") && hasPlayer) {
            cb.addRow("menu48");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasPlayer) {
            cb.addRow("menu29");
            cb.addRow("menu62");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.give")) {
            cb.addRow("menu70");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.place")) {
            cb.addRow("menu71");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.reset") && hasPlayer) {
            cb.addRow("menu30");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.mark") && hasPlayer) {
            cb.addRow("menu31");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.delete")) {
            if (hasPlayer) {
                cb.addRow("menu32");
            }
            cb.addRow("menu33");
            cb.addRow("menu55");
            cb.addRow("menu56");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.counts")) {
            cb.addRow("menu35");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.locations")) {
            cb.addRow("menu36");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.list") && hasPlayer) {
            cb.addRow("menu37");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.setowner") && hasPlayer) {
            cb.addRow("menu38");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.reload")) {
            cb.addRow("menu39");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.clean")) {
            cb.addRow("menu41");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.revert")) {
            cb.addRow("menu42");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.pull")) {
            cb.addRow("menu63");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.enableall")) {
            cb.addRow("menu43");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall")) {
            cb.addRow("menu44");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.rent")) {
            cb.addRow("menu59");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.migrate")) {
            cb.addRow("menu69");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.debug")) {
            cb.addRow("menu45");
        }

        if (cb.size() > 0) {
            if (hasPlayer) {
                ChatHelper.sendBlank(sender);
            }
            ChatHelper.saySingle(sender, "sepMenu", plugin.getDescription().getName(), plugin.getDescription().getVersion());
            ChatHelper.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more) {
                ChatHelper.sendBlank(sender);
                ChatHelper.send(sender, "moreNextPage");
            }

            if (hasPlayer) {
                ChatHelper.sendBlank(sender);
            }
        }
    }
}