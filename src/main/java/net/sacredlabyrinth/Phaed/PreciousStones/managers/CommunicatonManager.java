package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.plugin.Plugin;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

import java.util.*;

/**
 * @author phaed
 */
public class CommunicatonManager
{
    private PreciousStones plugin;
    private boolean useHawkEye;
    private HashMap<String, ChatBlock> chatBlocks = new HashMap<String, ChatBlock>();

    /**
     *
     */
    public CommunicatonManager()
    {
        plugin = PreciousStones.getInstance();
        useHawkEye = useHawkEye();
    }

    private boolean useHawkEye()
    {
        if (plugin.getSettingsManager().isLogToHawkEye())
        {
            Plugin plug = plugin.getServer().getPluginManager().getPlugin("HawkEye");

            if (plug != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * For debug output
     *
     * @param msg
     */
    public void debug(String msg)
    {
        PreciousStones.log("[debug] ***************** {message}", msg);
    }

    /**
     * Return a player's chat block, contains his pending chat messages
     *
     * @param player
     * @return
     */
    public ChatBlock getChatBlock(Player player)
    {
        ChatBlock cb = chatBlocks.get(player.getName());

        if (cb == null)
        {
            cb = new ChatBlock();
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
    public ChatBlock getNewChatBlock(CommandSender sender)
    {
        ChatBlock cb = new ChatBlock();
        if (sender instanceof Player)
        {
            chatBlocks.put(sender.getName(), cb);
        }
        else
        {
            chatBlocks.put("console", cb);
        }
        return cb;
    }

    private boolean canNotify(Player player)
    {
        return !(plugin.getPermissionsManager().has(player, "preciousstones.override.notify") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canWarn(Player player)
    {
        return !(plugin.getPermissionsManager().has(player, "preciousstones.override.warn") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canAlert(Player player)
    {
        return !(plugin.getSettingsManager().isDisableAlertsForAdmins() && plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    private boolean canBypassAlert(Player player)
    {
        return !(plugin.getSettingsManager().isDisableBypassAlertsForAdmins() && plugin.getPermissionsManager().has(player, "preciousstones.admin.isadmin"));
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyPlaceU(Player player, Block unbreakableblock)
    {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Unbreakable block placed");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Place", player, unbreakableblock.getLocation(), unbreakableblock.getType().toString());
            }
            else
            {
                PreciousStones.log("{1.player} placed an unbreakable block {2.details}", player.getName(), unbreakable.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} placed an unbreakable block {2.details}", player.getName(), unbreakable.getDetails());
            }
        }
    }

    /**
     * @param count
     * @param field
     */
    public void notifyRollBack(Field field, int count)
    {
        if (field == null)
        {
            return;
        }

        Player player = plugin.getServer().getPlayerExact(field.getOwner());

        if (player != null)
        {
            if (plugin.getSettingsManager().isNotifyRollback() && canNotify(player))
            {
                ChatBlock.send(player, "{dark-gray}* {aqua}Rolled back {1.count} griefed blocks {2.coords}", count, field.getCoords());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogRollback())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Grief-Reversion", player, field.getLocation(), "blocks:" + count);
            }
            else
            {
                PreciousStones.log("{1.owner}'s {2.field-type} block reverted {3.count} blocks {4.coords}", field.getOwner(), field.getSettings().getTitle(), count, field.getCoords());
            }
        }
    }

    /**
     * @param player
     * @return
     */
    public boolean notifyStoredTranslocations(Player player)
    {
        ChatBlock cb = getNewChatBlock(player);

        cb.setAlignment("l", "c");
        cb.addRow("  " + ChatColor.YELLOW + "Name", "Blocks");

        Map<String, Integer> details = plugin.getStorageManager().getTranslocationDetails(player.getName());

        for (String name : details.keySet())
        {
            int count = details.get(name);

            cb.addRow("  " + ChatColor.WHITE + name, ChatColor.WHITE + " " + count);
        }

        if (cb.size() > 1)
        {
            ChatBlock.sendBlank(player);
            ChatBlock.saySingle(player, ChatColor.WHITE + "Stored Translocations" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(player);

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(player);
                ChatBlock.send(player, "{dark-gray}Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(player);
            return true;
        }
        else
        {
            ChatBlock.send(player, "{red}No translocations found");
            return false;
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyApplyTranslocation(Field field, Player player, int count)
    {
        if (field == null)
        {
            return;
        }

        if (player != null)
        {
            if (plugin.getSettingsManager().isNotifyTranslocation() && canNotify(player))
            {
                ChatBlock.send(player, "{aqua}Translocator {1.name} enabled. (Recording)", field.getName());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogTranslocation())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Translocation", player, field.getLocation(), "blocks:" + count);
            }
            else
            {
                PreciousStones.log("{1.owner}'s translocation {2.player} translocated {3.count} blocks {4.coords}", field.getOwner(), field.getName(), count, field.getCoords());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyClearTranslocation(Field field, Player player, int count)
    {
        if (field == null)
        {
            return;
        }

        if (player != null)
        {
            if (plugin.getSettingsManager().isNotifyTranslocation() && canNotify(player))
            {
                ChatBlock.send(player, "{aqua}Translocator {1.name} disabled. (Safe to break)", field.getName());
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogTranslocation())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Stored", player, field.getLocation(), "");
            }
            else
            {
                PreciousStones.log("{1.owner}'s translocation {2.field} stored {3.count} blocks {4.coords}", field.getOwner(), field.getName(), count, field.getCoords());

            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);
        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{field-type} placed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Place", player, fieldblock.getLocation(), fs.getTitle());
            }
            else
            {
                PreciousStones.log("{1.player} placed a {2.field-type} field {3.details}", player.getName(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} placed a {2.field-type} field", player.getName(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyPlaceCuboid(Player player, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.field-type} cuboid field closed",fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Cuboid Field close", player, field.getBlock().getLocation(), fs.getTitle());
            }
            else
            {
                PreciousStones.log("{1.player} placed a {2.field-type} field {3.details}", player.getName(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} closed a {2.field-type} cuboid field", player.getName(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceBreakableFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Breakable {1.field-type} placed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Place", player, fieldblock.getLocation(), fs.getTitle() + " (Breakable)");
            }
            else
            {
                PreciousStones.log("{1.player} placed a breakable {2.field-type} field {3.details}", player.getName(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} placed breakable {2.field-type} field", player.getName(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyDestroyU(Player player, Block unbreakableblock)
    {
        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Unbreakable block destroyed");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Break", player, unbreakableblock.getLocation(), unbreakableblock.getType().toString());
            }
            else
            {
                PreciousStones.log("{1.player} destroyed his own unbreakable block {2.details}", player.getName(), Helper.getDetails(unbreakableblock));
            }
        }
        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} destroyed his own unbreakable block {2.details}",player.getName(), Helper.getDetails(unbreakableblock));
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.field-type} destroyed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Break", player, fieldblock.getLocation(), fs.getTitle());
            }
            else
            {
                PreciousStones.log("{1.player} destroyed his {2.field-type} field {3.details}", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} destroyed his {2.field-type} field", player.getName(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void notifyDestroyFFLiquid(Player player, Field field)
    {
        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.field-type} destroyed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Break", player, field.getLocation(), fs.getTitle());
            }
            else
            {
                PreciousStones.log("{1.player} destroyed his {2.field-type} field {3.details}", player.getName(), fs.getTitle(), field.getDetails());
            }
        }
        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} destroyed his {2.field-type} field", player.getName(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyOthersFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.field-type} destroyed", fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Break", player, fieldblock.getLocation(), fs.getTitle());
            }
            else
            {
                PreciousStones.log("{1.player} destroyed {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} destroyed {2.owner}'s {3.field-type} field",player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyBreakableFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.owner}'s breakable {2.field-type} destroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Break", player, fieldblock.getLocation(), fs.getTitle() + " (Breakable)");
            }
            else
            {
                PreciousStones.log("{1.player} destroyed {2.owner}'s breakable {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} destroyed {2.owner}'s breakable {3.field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassPlace(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Block bypass-placed inside {1.owner}'s {2.field-type} field", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Place in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed a block inside {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed a block inside {2.owner}'s {3.field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param loc
     * @param field
     */
    public void notifyPaintingBypassPlace(Player player, Location loc, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Block bypass-placed inside {1.owner}'s {2.field-type} field", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Place in Field", player, loc, "PAINTING (conflict: " + field.getOwner() + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed a block inside {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed a block inside {2.owner}'s {3.field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassPlaceU(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPlace() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Unbreakable block bypass-placed inside {1.owner}'s {2.field-type} field", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Bypass Place in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed an unbreakable block inside {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed an unbreakable block inside {2.owner}'s {3.field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassDestroy(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Block bypass-destroyed in {1.owner}'s {2.field-type} field", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Destroy in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-destroyed a block {2.coords} in {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-destroyed a block {2.coords} in {3.owner}'s {4.field-type} field", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassDestroyVehicle(Player player, Vehicle block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}Vehicle bypass-destroyed in {1.owner}'s {2.field-type} field", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Destroy in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-destroyed a vehicle {2.coords} in {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(block.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-destroyed a vehicle {2.coords} in {3.owner}'s {4.field-type} field", player.getName(), (new Vec(block.getLocation())).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void notifyBypassDestroyU(Player player, Block unbreakableblock)
    {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.owner}'s unbreakable block bypass-destroyed", unbreakable.getOwner());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Bypass Destroy", player, unbreakableblock.getLocation(), unbreakableblock.getType().toString() + " (owner: " + unbreakable.getOwner() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-destroyed {2.owner}'s unbreakable block {3.details}", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.name} bypass-destroyed {2.owner}'s unbreakable block {3.details}", player.getName(), unbreakable.getOwner(), Helper.getDetails(unbreakableblock));
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void notifyBypassDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassDestroy() && canNotify(player))
        {
            ChatBlock.send(player, "{aqua}{1.owner}'s {2.field-type} field bypass-destroyed", field.getOwner(), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log-destroy"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Bypass Destroy", player, fieldblock.getLocation(), fs.getTitle() + " (owner: " + field.getOwner() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-destroyed {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.notify.bypass") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-destroyed a block in {2.owner}'s {3.field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void warnEntry(Player player, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnEntry() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot enter protected area");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogEntry())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Entry Attempt", player, player.getLocation(), "(field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted entry into {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.entry") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted entry into {2.owner}'s {field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnFire(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnFire() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place fires here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogFire())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Fire Attempt", player, block.getLocation(), "(field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to light fire in {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.fire") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to light a fire in {2.owner}'s {field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnPlace(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Block Place Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted place a block {2.coords} in {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place a block {2.coords} in {3.owner}'s {4.field-type} field", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnUse(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUse() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot use this");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUse())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Use Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to use a {2.coords} in {3.owner}'s {4.field-type} field {5.details}", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.use") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to use a {2.block} in {3.owner}'s {4.field-type} field", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnEmpty(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPlace() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Block Place Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted empty a {2.coords} in {3.owner}'s {4.field-type} field {5.details}", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.place") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to empty a {2.block} in {3.owner}'s {4.field-type} field", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param unbreakableblock
     */
    public void warnDestroyU(Player player, Block unbreakableblock)
    {
        Unbreakable unbreakable = plugin.getUnbreakableManager().getUnbreakable(unbreakableblock);

        if (plugin.getSettingsManager().isWarnDestroy() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Only the owner can remove this block");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Destroy", player, unbreakableblock.getLocation(), unbreakableblock.getType().toString());
            }
            else
            {
                PreciousStones.log("{1.player} attempted to destroy {2.owner}'s unbreakable block {3.details}", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to destroy {2.owner}'s unbreakable block {3.details}", player.getName(), unbreakable.getOwner(), unbreakable.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param fieldblock
     */
    public void warnDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field != null)
        {
            FieldSettings fs = field.getSettings();

            if (plugin.getSettingsManager().isWarnDestroy() && canWarn(player))
            {
                ChatBlock.send(player, "{aqua}Only the owner can remove this block");
            }

            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
            {
                return;
            }

            if (plugin.getSettingsManager().isLogDestroy())
            {
                if (useHawkEye)
                {
                    HawkEyeAPI.addCustomEntry(plugin, "Field Destroy", player, fieldblock.getLocation(), fs.getTitle());
                }
                else
                {
                    PreciousStones.log("{1.player} attempted to destroy {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
                }
            }

            for (Player pl : plugin.getServer().getOnlinePlayers())
            {
                if (pl.equals(player))
                {
                    continue;
                }

                if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
                {
                    ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to destroy {2.owner}'s {field-type} field", player.getName(), field.getOwner(), fs.getTitle());
                }
            }
        }
    }

    /**
     * @param player
     * @param damagedblock
     * @param field
     */
    public void warnDestroyArea(Player player, Block damagedblock, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot destroy here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Destroy Attempt", player, damagedblock.getLocation(), damagedblock.getType().toString() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to destroy a block {2.coords} inside {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to destroy a block {2.block} inside {3.owner}'s {4.field-type} field", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param vehicle
     * @param field
     */
    public void warnDestroyVehicle(Player player, Vehicle vehicle, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnDestroyArea() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot destroy this vehicle");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Vehicle destroy Attempt", player, vehicle.getLocation(), vehicle.getType().toString() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to destroy a vehicle {2.coords} inside {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(vehicle.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to destroy a vehicle {2.block} inside {3.owner}'s {4.field-type} field", player.getName(), (new Vec(vehicle.getLocation())).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictU(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place unbreakable block here. Conflicting with {1.owner}'s {2.field-type} field {3.details}", field.getOwner(), field.getSettings().getTitle(), field.getDetails());
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place unbreakable block here");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place an unbreakable block {2.coords} conflicting with {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place an unbreakable block {2.block} conflicting with {3.owner}'s {4.field-type} field", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictFF(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        FieldSettings fsconflict = plugin.getSettingsManager().getFieldSettings(block);

        if (fsconflict == null)
        {
            return;
        }

        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place field here. Conflicting with {1.owner}'s {2.field-type} field {3.details}", field.getOwner(), field.getSettings().getTitle(), field.getDetails());
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place field here");
            }
        }
        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Conflict Place", player, block.getLocation(), fsconflict.getTitle() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place a field {2.coords} conflicting with {3.owner}'s {4.field-type} field {5.details}", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place a field {2.block} conflicting with {3.owner}'s {4.field-type} field", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param ub
     */
    public void warnConflictPistonU(Player player, Block block, Unbreakable ub)
    {
        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place piston here. Conflicting with " + ub.getOwner() + " unbreakable [" + ub.getType() + "|" + ub.getX() + " " + ub.getY() + " " + ub.getZ() + "]");
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place piston here.");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Piston Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + ub.getOwner() + " " + ub.getType().toString() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place a piston conflicting with {2.owner}'s unbreakable {3.details}", player.getName(), ub.getOwner(), ub.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place an piston conflicting with {2.owner}'s unbreakable {3.details}", player.getName(), ub.getOwner(), ub.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictPistonFF(Player player, Block block, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place a piston here. Conflicting with {1.owner}'s {2.field-type} field {3.details}", field.getOwner(), field.getSettings().getTitle(), field.getDetails());
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place a piston here");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Piston Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place a piston conflicting with {2.owner}'s {3.field-type} field {4.details}", player.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place a piston conflicting with {2.owner}'s {field-type} field", player.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param pistonBlock
     */
    public void warnConflictPistonRU(Player player, Block block, Block pistonBlock)
    {
        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place a unbreakable block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place a unbreakable block here");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + pistonBlock.getType().toString() + " " + Helper.toLocationString(block.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place an unbreakable conflicting with piston {2.details}", player.getName(), Helper.getDetails(pistonBlock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place an unbreakable conflicting with a piston", player.getName());
            }
        }
    }

    /**
     * @param player
     * @param block
     * @param pistonBlock
     */
    public void warnConflictPistonRFF(Player player, Block block, Block pistonBlock)
    {
        FieldSettings fsconflict = plugin.getSettingsManager().getFieldSettings(block);

        if (fsconflict == null)
        {
            return;
        }

        if (canWarn(player))
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.send(player, "{aqua}Cannot place a field block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.send(player, "{aqua}Cannot place a field block here");
            }
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogConflictPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Field Conflict Place", player, block.getLocation(), fsconflict.getTitle() + " (conflict: " + pistonBlock.getType().toString() + " " + Helper.toLocationString(block.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place a field conflicting with piston {2.details}", player.getName(), Helper.getDetails(pistonBlock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place a field block conflicting with a piston", player.getName());
            }
        }
    }

    /**
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnPvP(Player attacker, Player victim, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnPvp() && canWarn(attacker))
        {
            ChatBlock.send(attacker, "{aqua}PvP disabled in this area");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPvp())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "PvP Attempt", attacker, victim.getLocation(), victim.getName() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.attacker} tried to attack {2.victim} in {3.owner}'s {4.field-type} field {5.details}", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.attacker} tried to attack {2.victim} in {3.owner}'s {4.field-type} field", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param attacker
     * @param victim
     */
    public void warnBypassPvPDueToCombat(Player attacker, Player victim)
    {

        if (canNotify(attacker))
        {
            ChatBlock.send(attacker, "{aqua}PvP Protection Ignored due to combat");
        }

        if (canNotify(victim))
        {
            ChatBlock.send(victim, "{aqua}PvP Protection Ignored due to combat");
        }


    }

    /**
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnBypassPvP(Player attacker, Player victim, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isNotifyBypassPvp() && canNotify(attacker))
        {
            ChatBlock.send(attacker, "{aqua}PvP bypass");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPvp())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "PvP Bypass", attacker, victim.getLocation(), victim.getName() + " (field: " + field.getOwner() + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{1.attacker} bypass-attack {2.victim} in {3.owner}'s {4.field-type} field {5.details}", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.attacker} bypass-attacked {2.victim} in {3.owner}'s {4.field-type} field", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void warnFieldPlaceUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(protectionblock);

        if (fs == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place unprotectable {1.block-type} block here", Helper.friendlyBlockType(unprotectableblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, protectionblock.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void warnUnbreakablePlaceUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place unprotectable {1.block-type} block here", Helper.friendlyBlockType(unprotectableblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, protectionblock.getLocation(), protectionblock.getType().toString() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place an unprotectable block {2.unprotectable-details} near {3.unbreakable-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void warnUnbreakablePlaceTouchingUnprotectable(Player player, Block placedblock)
    {
        Block touchingblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot protect {1.block-type}",Helper.friendlyBlockType(touchingblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, placedblock.getLocation(), placedblock.getType().toString() + " (unprotectable: " + touchingblock.getType().toString() + " " + Helper.toLocationString(touchingblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to protect an unprotectable block {2.details}", player.getName(), Helper.getDetails(touchingblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to protect an unprotectable block {2.details}", player.getName(), Helper.getDetails(touchingblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void warnFieldPlaceTouchingUnprotectable(Player player, Block placedblock)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(placedblock);

        if (fs == null)
        {
            return;
        }

        Block touchingblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot protect {1.block-type}", Helper.friendlyBlockType(touchingblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, placedblock.getLocation(), fs.getTitle() + " (unprotectable: " + touchingblock.getType().toString() + " " + Helper.toLocationString(touchingblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to protect an unprotectable block {2.details}", player.getName(), Helper.getDetails(touchingblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to protect an unprotectable block {2.details}", player.getName(), Helper.getDetails(touchingblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void warnPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot protect {1.block-type} inside this {2.field-type} field", Helper.friendlyBlockType(unprotectableblock.getType().toString()), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, field.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to protect an unprotectable block {2.unprotectable-details} inside a field {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to protect an unprotectable block {2.unprotectable-details} inside a field {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void warnPlaceFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldtypeblock);

        if (fs == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Cannot place {1.field-type} field. A {2.block-type} found in the area", fs.getTitle(), Helper.friendlyBlockType(unprotectableblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Protect Attempt", player, fieldtypeblock.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} attempted to place a field [{2.field-type}] but an unprotectable was found in the area {3.unprotectable-details}", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} attempted to place a field {2.field-type} but an unprotectable was found in the area {3.unprotectable-details}", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void notifyUnbreakableBypassUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Unprotectable block {1.block-type} bypass-placed near {2.block-type} block", Helper.friendlyBlockType(unprotectableblock.getType().toString()), Helper.friendlyBlockType(protectionblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Bypass Protect", player, protectionblock.getLocation(), protectionblock.getType().toString() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed an unprotectable block {2.unprotectable-details} near {3.unbreakable-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void notifyFieldBypassUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(protectionblock);

        if (fs == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Unprotectable block {1.block-type} bypass-placed near {2.block-type} block", Helper.friendlyBlockType(unprotectableblock.getType().toString()), Helper.friendlyBlockType(protectionblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Bypass Protect", player, protectionblock.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed an unprotectable block {2.unprotectable-details} near {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), Helper.getDetails(protectionblock));
            }
        }
    }

    /**
     * @param player
     * @param placedblock
     */
    public void notifyBypassTouchingUnprotectable(Player player, Block placedblock)
    {
        Block unprotectableblock = plugin.getUnprotectableManager().getTouchingUnprotectableBlock(placedblock);

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Unprotectable block {1.block-type} bypass-protected", Helper.friendlyBlockType(unprotectableblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Bypass Protect", player, placedblock.getLocation(), placedblock.getType().toString() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-protected an unprotectable block {2.unprotectable-details}", player.getName(), Helper.getDetails(placedblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-protected an unprotectable block {2.details} near unprotectable block {3.unprotectable-details}", player.getName(), Helper.getDetails(placedblock), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void notifyBypassPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
        if (field == null)
        {
            return;
        }

        FieldSettings fs = field.getSettings();

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}Unprotectable block {1.block-type} bypass-placed in {2.field-type} field", Helper.friendlyBlockType(unprotectableblock.getType().toString()), fs.getTitle());
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Bypass Protect", player, field.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed an unprotectable block {2.unprotectable-details} inside a field {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed an unprotectable block {2.unprotectable-details} inside a field {3.field-details}", player.getName(), Helper.getDetails(unprotectableblock), field.getDetails());
            }
        }
    }

    /**
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void notifyBypassFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldtypeblock);

        if (fs == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isWarnUnprotectable() && canWarn(player))
        {
            ChatBlock.send(player, "{aqua}{1.field-type} field bypass-placed in an area with an {2.block-type} unprotectable block", fs.getTitle(), Helper.friendlyBlockType(unprotectableblock.getType().toString()));
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUnprotectable())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unprotectable Bypass Protect", player, fieldtypeblock.getLocation(), fs.getTitle() + " (unprotectable: " + unprotectableblock.getType().toString() + " " + Helper.toLocationString(unprotectableblock.getLocation()) + ")");
            }
            else
            {
                PreciousStones.log("{1.player} bypass-placed a field [{2.field-type}] in an area with an unprotectable block {3.unprotectable-details}", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.getPermissionsManager().has(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.send(pl, "{dark-gray}[ps]{gray}{1.player} bypass-placed a field {2.field-type} in an area with an unprotectable block {3.unprotectable-details}", player.getName(), fieldtypeblock.getType(), Helper.getDetails(unprotectableblock));
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void showWelcomeMessage(Player player, Field field)
    {
        if (field == null)
        {
            return;
        }

        if (field.isNamed())
        {
            ChatBlock.send(player, "{aqua}Entering {1.field}", field.getName());
        }
        else
        {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages())
            {
                ChatBlock.send(player, "{aqua}Entering {1.owner}'s {2.field-type} field", field.getOwner(), field.getSettings().getTitle());
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void showFarewellMessage(Player player, Field field)
    {
        if (field == null)
        {
            return;
        }

        if (field.isNamed())
        {
            ChatBlock.send(player, "{aqua}Leaving {1.field}", field.getName());
        }
        else
        {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages())
            {
                ChatBlock.send(player, "{aqua}Leaving {1.owner}'s {2.field-type} field", field.getOwner(), field.getSettings().getTitle());
            }
        }
    }

    /**
     * @param sender
     */
    public void showNotFound(CommandSender sender)
    {
        ChatBlock.send(sender, "{red}No fields found");
    }
    /**
     * @param player
     */
    public void showNoPotion(Player player, String potion)
    {
        if (canWarn(player))
        {
            ChatBlock.send(player, "{white}*{1.potion} neutralized*", potion.toLowerCase().replace("_", " "));
        }
    }

    /**
     * @param player
     */
    public void showDamage(Player player)
    {
        if (plugin.getSettingsManager().isWarnFastDamage() && canWarn(player))
        {
            ChatBlock.send(player, "{dark-red}*damage*");
        }
    }

    /**
     * @param player
     */
    public void showHeal(Player player)
    {
        if (plugin.getSettingsManager().isWarnInstantHeal() && canWarn(player))
        {
            ChatBlock.send(player, "{white}*healed*");
        }
    }

    /**
     * @param player
     */
    public void showGiveAir(Player player)
    {
        if (plugin.getSettingsManager().isWarnGiveAir() && canWarn(player))
        {
            ChatBlock.send(player, "{white}*air*");
        }
    }

    /**
     * @param player
     */
    public void showLaunch(Player player)
    {
        if (plugin.getSettingsManager().isWarnLaunch() && canWarn(player))
        {
            ChatBlock.send(player, "{light-purple}*launch*");
        }
    }

    /**
     * @param player
     */
    public void showCannon(Player player)
    {
        if (plugin.getSettingsManager().isWarnCannon() && canWarn(player))
        {
            ChatBlock.send(player, "{light-purple}*boom*");
        }
    }

    /**
     * @param player
     */
    public void showMine(Player player)
    {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player))
        {
            ChatBlock.send(player, "{red}*goodbye*");
        }
    }

    /**
     * @param player
     */
    public void showLightning(Player player)
    {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player))
        {
            ChatBlock.send(player, "{red}*crash*");
        }
    }

    /**
     * @param player
     */
    public void showThump(Player player)
    {
        ChatBlock.send(player, "{dark-gray}*thump*");
    }

    /**
     * @param player
     */
    public void showFeeding(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowFeeding() && canWarn(player))
        {
            ChatBlock.send(player,  "{white}~Feeding~");
        }
    }

    /**
     * @param player
     */
    public void showRepair(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowRepair() && canWarn(player))
        {
            ChatBlock.send(player,  "{white}+repairing+");
        }
    }

    /**
     * @param player
     * @param block
     */
    public void showUnbreakableOwner(Player player, Block block)
    {
        ChatBlock.send(player, "{yellow}Owner: {aqua}{1.owner}", plugin.getUnbreakableManager().getOwner(block));
    }

    /**
     * @param player
     * @param block
     */
    public void showFieldOwner(Player player, Block block)
    {
        ChatBlock.send(player, "{yellow}Owner: {aqua}{1.owner}", plugin.getForceFieldManager().getOwner(block));
    }

    /**
     * @param block
     * @param player
     */
    public void showProtectedLocation(Player player, Block block)
    {
        List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

        ChatBlock.sendBlank(player);
        ChatBlock.send(player,  "{white}Protected");

        for (Field field : fields)
        {
            ChatBlock.send(player, "{yellow}{1.field-type}: {aqua}{2.coords}", field.getSettings().getTitle(), field.getCleanCoords());
        }
    }

    /**
     * @param unbreakable
     * @param player
     */
    public void showUnbreakableDetails(Unbreakable unbreakable, Player player)
    {
        ChatBlock.sendBlank(player);
        ChatBlock.send(player, "{yellow}Owner: {aqua}{1.owner}", unbreakable.getOwner());
    }

    /**
     * @param player
     * @param fields
     */
    public void showFieldDetails(Player player, List<Field> fields)
    {
        ChatBlock cb = getNewChatBlock(player);

        for (Field field : fields)
        {
            cb.addRow("", "", "");

            ChatColor color = field.isDisabled() ? ChatColor.RED : ChatColor.YELLOW;

            if (field.isDisabled())
            {
                cb.addRow("  " + ChatColor.RED + "Field Disabled", "", "");
            }
            FieldSettings fs = field.getSettings();

            cb.addRow("  " + color + "Type: ", ChatColor.AQUA + fs.getTitle(), "");

            if (fs.hasNameableFlag() && field.isNamed())
            {
                cb.addRow("  " + color + "Name: ", ChatColor.AQUA + field.getName(), "");
            }

            cb.addRow("  " + color + "Owner: ", ChatColor.AQUA + field.getOwner(), "");

            cb.addRow("  " + color + "Location: ", ChatColor.AQUA + "" + field.getX() + " " + field.getY() + " " + field.getZ(), "");
        }

        if (cb.size() > 0)
        {
            cb.addRow("", "", "", "");

            ChatBlock.sendBlank(player);
            ChatBlock.saySingle(player, ChatColor.WHITE + "Field Info " + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.send(player, "{dark-gray}Type /ps more to view next page.");
            }
        }
    }


    /**
     * @param player
     * @param field
     * @return
     */
    public boolean showFieldDetails(Player player, Field field)
    {
        if (field == null)
        {
            return false;
        }

        ChatBlock cb = getNewChatBlock(player);
        FieldSettings fs = field.getSettings();

        cb.addRow("", "", "");

        ChatColor color = field.isDisabled() ? ChatColor.RED : ChatColor.YELLOW;

        boolean showMessage = true;

        cb.addRow("  " + color + "Type: ", ChatColor.AQUA + fs.getTitle(), "");

        if (fs.hasNameableFlag())
        {
            if (field.isNamed())
            {
                cb.addRow("  " + color + "Name: ", ChatColor.AQUA + field.getName(), "");
            }
            else
            {
                cb.addRow("  " + color + "Name: ", ChatColor.GRAY + "NONE", "");
            }
        }

        List<String> applies = new ArrayList<String>();

        if (field.hasFlag(FieldFlag.APPLY_TO_REVERSE))
        {
            applies.add("reverse");
        }

        if (field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            applies.add("all");
        }

        if (applies.isEmpty())
        {
            applies.add("default");
        }

        cb.addRow("  " + color + "Applies to: ", ChatColor.WHITE + Helper.toMessage(applies, ","), "");

        cb.addRow("  " + color + "Owner: ", ChatColor.AQUA + field.getOwner(), "");

        if (field.getAllowed().size() > 0)
        {
            List<String> allowed = field.getAllowed();

            int rows = (int) Math.max(Math.ceil(allowed.size() / 2), 1);

            for (int i = 0; i < rows; i++)
            {
                String title = "";

                if (i == 0)
                {
                    title = color + "Allowed: ";
                }

                cb.addRow("  " + title, ChatColor.WHITE + getAllowed(allowed, i * 2), getAllowed(allowed, (i * 2) + 1));
            }
        }

        if (field.hasFlag(FieldFlag.CUBOID))
        {
            cb.addRow("  " + color + "Dimensions: ", ChatColor.AQUA + "" + (field.getMaxx() - field.getMinx() + 1) + "x" + (field.getMaxy() - field.getMiny() + 1) + "x" + (field.getMaxz() - field.getMinz() + 1), "");
        }
        else
        {
            cb.addRow("  " + color + "Dimensions: ", ChatColor.AQUA + "" + ((field.getRadius() * 2) + 1) + "x" + field.getHeight() + "x" + ((field.getRadius() * 2) + 1), "");
        }

        if (field.getVelocity() > 0)
        {
            cb.addRow("  " + color + "Velocity: ", ChatColor.AQUA + "" + field.getVelocity(), "");
        }

        if (field.getRevertSecs() > 0)
        {
            cb.addRow("  " + color + "Interval: ", ChatColor.AQUA + "" + field.getRevertSecs(), "");
        }

        cb.addRow("  " + color + "Location: ", ChatColor.AQUA + "" + field.getX() + " " + field.getY() + " " + field.getZ(), "");

        List<FieldFlag> flags = new ArrayList<FieldFlag>(field.getFlags());
        List<FieldFlag> disabledFlags = field.getDisabledFlags();
        List<FieldFlag> hardCodedFlags = new ArrayList<FieldFlag>();

        flags.remove(FieldFlag.ALL);
        flags.remove(FieldFlag.DYNMAP_NO_TOGGLE);

        flags.addAll(disabledFlags);

        hardCodedFlags.add(FieldFlag.CUBOID);
        hardCodedFlags.add(FieldFlag.APPLY_TO_REVERSE);
        hardCodedFlags.add(FieldFlag.APPLY_TO_ALL);
        hardCodedFlags.add(FieldFlag.NO_CONFLICT);
        hardCodedFlags.add(FieldFlag.NO_PLAYER_PLACE);
        hardCodedFlags.add(FieldFlag.BREAKABLE);
        hardCodedFlags.add(FieldFlag.TOGGLE_ON_DISABLED);
        hardCodedFlags.add(FieldFlag.REDEFINE_ON_DISABLED);
        hardCodedFlags.add(FieldFlag.BREAKABLE_ON_DISABLED);
        hardCodedFlags.add(FieldFlag.MODIFY_ON_DISABLED);
        hardCodedFlags.add(FieldFlag.PREVENT_UNPROTECTABLE);
        hardCodedFlags.add(FieldFlag.PLACE_DISABLED);
        hardCodedFlags.add(FieldFlag.SNEAKING_BYPASS);

        if (field.hasFlag(FieldFlag.DYNMAP_NO_TOGGLE))
        {
            hardCodedFlags.add(FieldFlag.DYNMAP_AREA);
            hardCodedFlags.add(FieldFlag.DYNMAP_MARKER);
        }

        hardCodedFlags.add(FieldFlag.SNEAKING_BYPASS);

        int rows = (int) Math.ceil(((double) flags.size()) / 2.0);

        for (int i = 0; i < rows; i++)
        {
            String title = "";

            if (i == 0)
            {
                title = color + "Flags: ";
            }

            cb.addRow("  " + title, getFlag(disabledFlags, hardCodedFlags, flags, i * 2), getFlag(disabledFlags, hardCodedFlags, flags, (i * 2) + 1));
        }

        if (field.hasFlag(FieldFlag.POTIONS))
        {
            cb.addRow("  " + color + "Potions: ", ChatColor.WHITE + field.getSettings().getPotionString(), "");
        }

        if (field.hasFlag(FieldFlag.NEUTRALIZE_POTIONS))
        {
            cb.addRow("  " + color + "Neutralizes: ", ChatColor.WHITE + field.getSettings().getNeutralizePotionString(), "");
        }

        if (cb.size() > 0)
        {
            cb.addRow("", "", "", "");

            ChatBlock.sendBlank(player);

            if (field.isDisabled())
            {
                ChatBlock.saySingle(player, ChatColor.WHITE + "Field Info " + ChatColor.RED + "(disabled)" + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");
            }
            else
            {
                ChatBlock.saySingle(player, ChatColor.WHITE + "Field Info " + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");
            }

            if (field.isDisabled())
            {
                ChatBlock.sendBlank(player);

                List<String> cond = new ArrayList<String>();

                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                {
                    cond.add("settings");
                }

                if (field.hasFlag(FieldFlag.TOGGLE_ON_DISABLED))
                {
                    cond.add("flags");
                }

                if (field.hasFlag(FieldFlag.REDEFINE_ON_DISABLED))
                {
                    cond.add("cuboid");
                }

                showMessage = false;
            }

            cb.sendBlock(player);
        }

        return showMessage;
    }

    private String getFlag(List<FieldFlag> disabledFlags, List<FieldFlag> hardCodedFlags, List<FieldFlag> flags, int index)
    {
        if (index < flags.size())
        {
            FieldFlag flag = flags.get(index);

            ChatColor color = ChatColor.WHITE;

            if (disabledFlags.contains(flag))
            {
                color = ChatColor.DARK_GRAY;
            }

            if (hardCodedFlags.contains(flag))
            {
                color = ChatColor.AQUA;
            }

            return color + Helper.toFlagStr(flag);
        }

        return "";
    }

    private String getAllowed(List<String> allowed, int index)
    {
        if (index < allowed.size())
        {
            return allowed.get(index).toString();
        }

        return "";
    }

    /**
     * Shows all the configured fields to the player
     *
     * @param player
     */
    public void showConfiguredFields(CommandSender player)
    {
        ChatBlock cb = getNewChatBlock(player);

        HashMap<BlockTypeEntry, FieldSettings> fss = plugin.getSettingsManager().getFieldSettings();

        for (FieldSettings fs : fss.values())
        {
            String customHeight = fs.getHeight() > 0 ? ChatColor.YELLOW + "h:" + ChatColor.WHITE + "" + fs.getHeight() : "";

            int id = fs.getTypeId();
            Material material = Material.getMaterial(id);
            BlockTypeEntry entry = new BlockTypeEntry(fs.getTypeId(), fs.getData());

            if (material == null)
            {
                continue;
            }

            cb.addRow(ChatColor.AQUA + fs.getTitle() + ChatColor.GRAY + " (" +Helper.friendlyBlockType(material.toString()) + ") " + ChatColor.YELLOW + "id:" + ChatColor.WHITE + "" + entry.toString() + " " + ChatColor.YELLOW + "r:" + ChatColor.WHITE + "" + fs.getRadius() + " " + customHeight);
        }

        if (cb.size() > 0)
        {
            ChatBlock.sendBlank(player);
            ChatBlock.saySingle(player, ChatColor.WHITE + "Configured Fields" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(player);

            boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(player);
                ChatBlock.send(player, "{dark-gray}Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(player);
        }
    }

    /**
     * @param sender
     * @param type
     * @return
     */
    public boolean showCounts(CommandSender sender, BlockTypeEntry type)
    {
        if (!(sender instanceof Player))
        {
            //sender = new ColouredConsoleSender((CraftServer)PreciousStones.getInstance().getServer());
            sender = PreciousStones.getInstance().getServer().getConsoleSender();
        }

        ChatBlock cb = getNewChatBlock(sender);

        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(type);

        if (fs == null)
        {
            return false;
        }

        TreeMap<String, PlayerEntry> players = plugin.getPlayerManager().getPlayers();

        cb.setAlignment("l", "c");

        cb.addRow("  " + ChatColor.GRAY + "Name", "Count");

        for (String playerName : players.keySet())
        {
            PlayerEntry data = players.get(playerName);

            int count = data.getFieldCount(type);

            if (count > 0)
            {
                cb.addRow("  " + ChatColor.AQUA + data.getName(), ChatColor.WHITE + " " + count);
            }
        }

        if (cb.size() > 1)
        {
            ChatBlock.sendBlank(sender);
            ChatBlock.saySingle(sender, "{white}{1.field-type} Counts {dark-gray} ----------------------------------------------------------------------------------------", fs.getTitle());
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.send(sender, "{dark-gray}Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.send(sender, "{aqua}No fields found");
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
    public boolean showFieldCounts(CommandSender sender, String target)
    {
        Player player = null;

        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        else
        {
            sender = PreciousStones.getInstance().getServer().getConsoleSender();
        }

        ChatBlock cb = getNewChatBlock(sender);

        boolean showLimits = player.getName().equalsIgnoreCase(target) && plugin.getSettingsManager().haveLimits();

        if (showLimits)
        {
            cb.setAlignment("l", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + "Field", "Count", "Limit");
        }
        else
        {
            cb.setAlignment("l", "c");
            cb.addRow("  " + ChatColor.GRAY + "Field", "Count");
        }

        HashMap<BlockTypeEntry, Integer> fieldCounts;

        if (target.contains(":"))
        {
            fieldCounts = plugin.getForceFieldManager().getFieldCounts(target);
        }
        else
        {
            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(target);
            fieldCounts = data.getFieldCount();
        }

        for (BlockTypeEntry type : fieldCounts.keySet())
        {
            int count = fieldCounts.get(type);

            if (count == 0)
            {
                continue;
            }

            FieldSettings fs = plugin.getSettingsManager().getFieldSettings(type);

            if (fs == null)
            {
                continue;
            }

            int limit = plugin.getLimitManager().getLimit(player, fs);

            ChatColor color = (count < limit) || limit == -1 ? ChatColor.WHITE : ChatColor.DARK_RED;

            String strLimit = limit == -1 ? "-" : limit + "";

            if (plugin.getSettingsManager().haveLimits())
            {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), color + " " + count, ChatColor.WHITE + " " + strLimit);
            }
            else
            {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + " " + count);
            }
        }

        String targetName = target;

        if (target.contains(":"))
        {
            targetName = target.substring(2);
        }
        else if (target.contains("*"))
        {
            targetName = "Everyone";
        }

        if (cb.size() > 1)
        {
            ChatBlock.sendBlank(sender);
            ChatBlock.saySingle(sender, "{1.player}'s Field Counts {dark-gray}  ----------------------------------------------------------------------------------------", targetName);
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.send(sender, "{dark-gray}Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.send(sender, "{aqua}No fields found");
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
    public void showFieldLocations(CommandSender sender, int typeid, String target)
    {
        Player player = null;

        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        else
        {
            //sender = new ColouredConsoleSender((CraftServer)PreciousStones.getInstance().getServer());
            sender = PreciousStones.getInstance().getServer().getConsoleSender();
        }

        ChatBlock cb = getNewChatBlock(sender);
        boolean admin = player == null ? true : !player.getName().equalsIgnoreCase(target);
        Location center = player == null ? new Location(plugin.getServer().getWorlds().get(0), 0, 0, 0) : player.getLocation();

        if (admin)
        {
            cb.setAlignment("l", "c", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + "Field", "Distance", "Coords", "Owner");
        }
        else
        {
            cb.setAlignment("l", "c", "c");
            cb.addRow("  " + ChatColor.GRAY + "Field", "Distance", "Coords");
        }

        List<Field> fields = new ArrayList<Field>();

        if (player != null)
        {
            fields = plugin.getForceFieldManager().getFields(target, player.getWorld());
        }
        else
        {
            for (World world : plugin.getServer().getWorlds())
            {
                fields.addAll(plugin.getForceFieldManager().getFields(target, world));
            }
        }

        sortByDistance(fields, center);

        for (Field field : fields)
        {
            // if type id supplied, then only show fields of that typeid

            if (typeid != -1)
            {
                if (typeid != field.getTypeId())
                {
                    continue;
                }
            }
        }

        String targetName = target;

        if (target.contains(":"))
        {
            targetName = target.substring(2);
        }
        else if (target.contains("*"))
        {
            targetName = "Everyone";
        }

        for (Field field : fields)
        {
            int distance = (int) field.distance(center);
            FieldSettings fs = field.getSettings();

            if (admin)
            {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + "" + distance, ChatColor.YELLOW + Helper.toLocationString(field.getLocation()), ChatColor.WHITE + field.getOwner());
            }
            else
            {
                cb.addRow("  " + ChatColor.AQUA + fs.getTitle(), ChatColor.WHITE + "" + distance, ChatColor.YELLOW + Helper.toLocationString(field.getLocation()));
            }
        }

        if (cb.size() > 1)
        {
            ChatBlock.sendBlank(sender);
            ChatBlock.saySingle(sender, "{1.player}'s {world} Field Locations {dark-gray} ----------------------------------------------------------------------------------------", targetName, Helper.capitalize(player.getWorld().getName()));
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.send(sender, "{dark-gray}Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.send(sender, "{aqua}No fields found");
        }
    }

    /**
     * Sort clan players by KDR
     *
     * @param fields
     * @param playerLocation
     * @return
     */
    public void sortByDistance(List<Field> fields, final Location playerLocation)
    {
        Collections.sort(fields, new Comparator<Field>()
        {
            public int compare(Field f1, Field f2)
            {
                Float o1 = Float.valueOf((float) f1.distance(playerLocation));
                Float o2 = Float.valueOf((float) f2.distance(playerLocation));

                return o1.compareTo(o2);
            }
        });
    }

    /**
     * @param player
     * @param field
     * @return
     */
    public boolean showSnitchList(Player player, Field field)
    {
        if (field != null)
        {
            List<SnitchEntry> snitches = field.getSnitches();

            if (snitches.isEmpty() || snitches.get(0).getAgeInSeconds() > 10)
            {
                snitches = plugin.getStorageManager().getSnitchEntries(field);
                field.updateLastUsed();
                plugin.getStorageManager().offerField(field);
            }

            String title = "Intruder log ";

            if (!snitches.isEmpty())
            {
                ChatBlock cb = getNewChatBlock(player);

                ChatBlock.sendBlank(player);
                ChatBlock.saySingle(player, ChatColor.WHITE + title + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                ChatBlock.sendBlank(player);

                cb.addRow("  " + ChatColor.GRAY + "Name", "Reason", "Details");

                for (SnitchEntry se : snitches)
                {
                    cb.addRow("  " + ChatColor.GOLD + se.getName(), se.getReasonDisplay(), ChatColor.WHITE + se.getDetails());
                }

                boolean more = cb.sendBlock(player, plugin.getSettingsManager().getLinesPerPage());

                if (more)
                {
                    ChatBlock.sendBlank(player);
                    ChatBlock.send(player, "{dark-gray}Type /ps more to view next page.");
                }

                ChatBlock.sendBlank(player);
            }

            return !snitches.isEmpty();
        }
        else
        {
            plugin.getCommunicationManager().showNotFound(player);
        }

        return false;
    }

    /**
     * @param sender
     */
    public void showMenu(CommandSender sender)
    {
        ChatColor color = ChatColor.YELLOW;
        ChatColor colorDesc = ChatColor.AQUA;

        Player player = null;

        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        else
        {
            sender = PreciousStones.getInstance().getServer().getConsoleSender();
        }

        boolean hasPlayer = player != null;

        ChatBlock cb = getNewChatBlock(sender);

        cb.addRow(ChatColor.GRAY + "  Identifiers:" + ChatColor.DARK_GRAY + " player, g:group, c:clan, *");
        cb.addRow("");

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.onoff") && hasPlayer)
        {
            cb.addRow(ChatColor.YELLOW + "  /ps on/off " + colorDesc + "- Disable/Enable the placing of pstones");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.disable") && hasPlayer)
        {
            cb.addRow(color + "  /ps enable/disable " + colorDesc + "- Enable/disable a field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allow") && hasPlayer)
        {
            cb.addRow(color + "  /ps allow [identifier(s)] " + colorDesc + "- Allow to field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowall") && hasPlayer)
        {
            cb.addRow(color + "  /ps allowall [identifier(s)] " + colorDesc + "- Allow to all fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.remove") && hasPlayer)
        {
            cb.addRow(color + "  /ps remove [identifier(s)] " + colorDesc + "- Remove from field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.removeall") && hasPlayer)
        {
            cb.addRow(color + "  /ps removeall [identifier(s)] " + colorDesc + "- Remove from all fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.whitelist.allowed") && hasPlayer)
        {
            cb.addRow(color + "  /ps allowed " + colorDesc + "- List all allowed players on field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.who") && hasPlayer)
        {
            cb.addRow(color + "  /ps who " + colorDesc + "- List all inhabitants inside the fields");
        }

        if (plugin.getSettingsManager().haveLimits() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.counts") && hasPlayer)
        {
            cb.addRow(color + "  /ps counts " + colorDesc + "- View your field counts");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.locations") && hasPlayer)
        {
            cb.addRow(color + "  /ps locations " + colorDesc + "- View your field locations");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.toggle") && hasPlayer)
        {
            cb.addRow(color + "  /ps toggle [flag]" + colorDesc + "- Enable/Disable a field's flags");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.change-owner") && hasPlayer)
        {
            cb.addRow(color + "  /ps changeowner [name] " + colorDesc + "- Change owner of field");
        }

        if (plugin.getSettingsManager().haveNameable() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setname") && hasPlayer)
        {
            cb.addRow(color + "  /ps setname [name] " + colorDesc + "- Set the name of field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.setradius") && hasPlayer)
        {
            cb.addRow(color + "  /ps setradius [radius] " + colorDesc + "- Sets the field's radius");
        }

        if (plugin.getSettingsManager().haveVelocity() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setvelocity") && hasPlayer)
        {
            cb.addRow(color + "  /ps setvelocity [.1-5] " + colorDesc + "- For launchers/cannons (0=auto)");
        }

        if (plugin.getSettingsManager().haveGriefRevert() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.setinterval") && hasPlayer)
        {
            cb.addRow(color + "  /ps setinterval [secs] " + colorDesc + "- For automatic grief-revert");
        }

        if (plugin.getSettingsManager().haveSnitch() && plugin.getPermissionsManager().has(player, "preciousstones.benefit.snitch") && hasPlayer)
        {
            cb.addRow(color + "  /ps snitch <clear> " + colorDesc + "- View/clear snitch you're pointing at");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasPlayer)
        {
            cb.addRow(color + "  /ps visualize " + colorDesc + "- Visualize the field you are on");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.visualize") && hasPlayer)
        {
            cb.addRow(color + "  /ps visualize [radius]" + colorDesc + "- Visualize fields for a radius");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.density") && hasPlayer)
        {
            cb.addRow(color + "  /ps density [1-100]" + colorDesc + "- Change visualization density");
        }

        if ((plugin.getPermissionsManager().has(player, "preciousstones.benefit.mark") && !plugin.getPermissionsManager().has(player, "preciousstones.admin.mark")) && hasPlayer)
        {
            cb.addRow(color + "  /ps mark" + colorDesc + "- Marks the location of fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.use"))
        {
            cb.addRow(color + "  /ps translocation list " + colorDesc + "- Lists stored translocations");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.import"))
        {
            cb.addRow(color + "  /ps translocation import " + colorDesc + "- Import all blocks in field");
            cb.addRow(color + "  /ps translocation import [id] [id] ... " + colorDesc + "- Import specific blocks");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.delete"))
        {
            cb.addRow(color + "  /ps translocation delete " + colorDesc + "- Delete all blocks in field");
            cb.addRow(color + "  /ps translocation delete [id] [id] ... " + colorDesc + "- Delete specific blocks");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.remove"))
        {
            cb.addRow(color + "  /ps translocation remove [id] [id] ... " + colorDesc + "- Remove specific blocks");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.translocation.unlink"))
        {
            cb.addRow(color + "  /ps translocation unlink " + colorDesc + "- Unlinks the blocks");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.insert") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps insert [flag]" + colorDesc + "- Inserts flags into fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.reset") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps reset" + colorDesc + "- Resets the flags of the field to defaults");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.mark") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps mark" + colorDesc + "- Marks the location of fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.delete"))
        {
            if (hasPlayer)
            {
                cb.addRow(ChatColor.DARK_RED + "  /ps delete " + colorDesc + "- Delete the field you're standing on");
            }
            cb.addRow(ChatColor.DARK_RED + "  /ps delete [player/typeId] " + colorDesc + "- Batch delete pstones");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.info") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps info " + colorDesc + "- Get info for the field youre standing on");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.counts"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps counts [identifier(s)] " + colorDesc + "- View field counts");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.locations"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps locations [identifier(s)] <typeid> " + colorDesc + "- View field locations");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.list") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps list [chunks-in-radius]" + colorDesc + "- Lists all pstones in area");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.setowner") && hasPlayer)
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps setowner [player] " + colorDesc + "- Of the block you're pointing at");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.reload"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps reload " + colorDesc + "- Reloads configuraton file");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.fields"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps fields " + colorDesc + "- List the configured field types");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.clean"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps clean " + colorDesc + "- Cleans up all orphan fields in the world");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.revert"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps revert " + colorDesc + "- Reverts all orphan fields in the world");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.enableall"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps enableall [flag] " + colorDesc + "- Enabled the flags on all fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.disableall"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps disableall [flag] " + colorDesc + "- Disabled the flags on all fields");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.debug"))
        {
            cb.addRow(ChatColor.DARK_RED + "  /ps debug " + colorDesc + "- Prints timing info to console");
            cb.addRow(ChatColor.DARK_RED + "  /ps debugdb " + colorDesc + "- Prints out save process info");
            cb.addRow(ChatColor.DARK_RED + "  /ps debugsql " + colorDesc + "- Prints sql queries to console");
        }

        if (cb.size() > 0)
        {
            if (hasPlayer)
            {
                ChatBlock.sendBlank(sender);
            }
            ChatBlock.saySingle(sender, "{1.plugin-name} {2.plugin-version} {dark-gray} ----------------------------------------------------------------------------------------", plugin.getDescription().getName(), plugin.getDescription().getVersion());
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.send(sender, "{dark-gray}Type /ps more to view next page.");
            }

            if (hasPlayer)
            {
                ChatBlock.sendBlank(sender);
            }
        }
    }
}
