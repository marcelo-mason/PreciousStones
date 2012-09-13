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
        PreciousStones.log("[debug] ***************** {0}", msg);
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block placed");
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
                PreciousStones.log("{0} placed an unbreakable block [{1}|{2} {3} {4}]", player.getName(), unbreakable.getTypeId(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed an unbreakable block [" + unbreakable.getTypeId() + "]");
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
                player.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + "Rolled back " + count + " griefed " + Helper.plural(count, "block", "s") + " " + field.getCoords());
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
                PreciousStones.log(ChatColor.AQUA + Helper.capitalize(field.getOwner()) + "'s " + field.getSettings().getTitle() + " block reverted " + count + " blocks " + field.getCoords());
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
                ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(player);
            return true;
        }
        else
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "No translocations found");
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
                player.sendMessage(ChatColor.AQUA + "Translocator " + field.getName() + " enabled.  (Recording)");
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
                PreciousStones.log(Helper.capitalize(field.getOwner()) + "''s translocation " + field.getName() + " translocated " + count + " blocks " + field.getCoords());
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
                player.sendMessage(ChatColor.AQUA + "Translocator " + field.getName() + " disabled. (Safe to break)");
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
                PreciousStones.log(Helper.capitalize(field.getOwner()) + "''s translocatior " + field.getName() + " stored " + count + " blocks " + field.getCoords());
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " placed");
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
                PreciousStones.log("{0} placed a {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed a " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " cuboid field closed");
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
                PreciousStones.log("{0} placed a {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " closed a " + fs.getTitle() + " cuboid field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Breakable " + fs.getTitle() + " placed");
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
                PreciousStones.log("{0} placed a breakable {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), fieldblock.getType(), fieldblock.getX(), fieldblock.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed breakable " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block destroyed");
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
                PreciousStones.log("{0} destroyed his own unbreakable block [{1}|{2} {3} {4}]", player.getName(), unbreakableblock.getType(), unbreakableblock.getX(), unbreakableblock.getY(), unbreakableblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his own unbreakable block [" + unbreakableblock.getType() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " destroyed");
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
                PreciousStones.log("{0} destroyed his {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " destroyed");
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
                PreciousStones.log("{0} destroyed his {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.capitalize(fs.getTitle()) + " destroyed");
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
                PreciousStones.log("{0} destroyed {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.posessive(field.getOwner()) + " breakable " + fs.getTitle() + " destroyed");
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
                PreciousStones.log("{0} destroyed {1}s breakable {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destoyed " + Helper.posessive(field.getOwner()) + " breakable " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-placed inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Place in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-placed a block inside {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed a block inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-placed inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Place in Field", player, loc, "PAINTING (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-placed a block inside {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed a block inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block bypass-placed inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPlace())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Bypass Place in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-placed an unbreakable block inside {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unbreakable block inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-destroyed in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Destroy in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-destroyed a block {1} in {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block " + (new Vec(block)).toString() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Vehicle bypass-destroyed in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassDestroy())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Bypass Destroy in Field", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " " + field.toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-destroyed a vehicle {1} in {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a vehicle " + (new Vec(block.getLocation())).toString() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.posessive(unbreakable.getOwner()) + " unbreakable block bypass-destroyed");
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
                PreciousStones.log("{0} bypass-destroyed {1}s unbreakable block [{2}|{3} {4} {5}]", player.getName(), unbreakable.getOwner(), unbreakable.getType(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed " + Helper.posessive(unbreakable.getOwner()) + " unbreakable block [" + unbreakable.getType() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field bypass-destroyed");
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
                PreciousStones.log("{0} bypass-destroyed {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot enter protected area");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogEntry())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Entry Attempt", player, player.getLocation(), "(field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted entry into {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted entry into " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place fires here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogFire())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Fire Attempt", player, block.getLocation(), "(field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to light fire in {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to light a fire in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Block Place Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted place a block {1} in {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a block " + (new Vec(block)).toString() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot use this");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogUse())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Use Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to use a {1} in {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to use a " + block.getType().toString() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPlaceArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Block Place Attempt", player, block.getLocation(), block.getType().toString() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted empty a {1} in {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), block.getType().toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to empty a " + block.getType().toString() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
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
                PreciousStones.log("{0} attempted to destroy {1}s unbreakable block [{2}|{3} {4} {5}]", player.getName(), unbreakable.getOwner(), unbreakable.getType(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + Helper.posessive(unbreakable.getOwner()) + " unbreakable block [" + unbreakable.getType() + "]");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
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
                    PreciousStones.log("{0} attempted to destroy {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                    ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot destroy here");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Destroy Attempt", player, damagedblock.getLocation(), damagedblock.getType().toString() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to destroy a block {1} inside {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy a block " + (new Vec(damagedblock)).toString() + " inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot destroy this vehicle");
        }

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogDestroyArea())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "Vehicle destroy Attempt", player, vehicle.getLocation(), vehicle.getType().toString() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to destroy a vehicle {1} inside {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(vehicle.getLocation())).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy a vehicle " + (new Vec(vehicle.getLocation())).toString() + " inside " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here. Conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here");
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
                HawkEyeAPI.addCustomEntry(plugin, "Unbreakable Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to place an unbreakable block {1} conflicting with {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable block " + (new Vec(block)).toString() + " conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place field here. Conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place field here");
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
                HawkEyeAPI.addCustomEntry(plugin, "Field Conflict Place", player, block.getLocation(), fsconflict.getTitle() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to place a field {1} conflicting with {2}s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field " + (new Vec(block)).toString() + " conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place piston here. Conflicting with " + Helper.posessive(ub.getOwner()) + " unbreakable [" + ub.getType() + "|" + ub.getX() + " " + ub.getY() + " " + ub.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place piston here.");
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
                HawkEyeAPI.addCustomEntry(plugin, "Piston Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(ub.getOwner()) + " " + ub.getType().toString() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to place a piston conflicting with {1}s unbreakable [{2}|{3} {4} {5}]", player.getName(), ub.getOwner(), ub.getType(), ub.getX(), ub.getY(), ub.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an piston conflicting with " + Helper.posessive(ub.getOwner()) + " unbreakable");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a piston here. Conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a piston here");
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
                HawkEyeAPI.addCustomEntry(plugin, "Piston Conflict Place", player, block.getLocation(), block.getType().toString() + " (conflict: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} attempted to place a piston conflicting with {1}s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a piston conflicting with " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a unbreakable block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a unbreakable block here");
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
                PreciousStones.log("{0} attempted to place an unbreakable conflicting with piston [{1} {2} {3}]", player.getName(), pistonBlock.getX(), pistonBlock.getY(), pistonBlock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable conflicting with a piston");
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
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a field block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a field block here");
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
                PreciousStones.log("{0} attempted to place a field conflicting with piston [{1} {2} {3}]", player.getName(), pistonBlock.getX(), pistonBlock.getY(), pistonBlock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field block conflicting with a piston");
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
            attacker.sendMessage(ChatColor.AQUA + "PvP disabled in this area");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogPvp())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "PvP Attempt", attacker, victim.getLocation(), victim.getName() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} tried to attack {1} in {2}s {3} field [{4}|{5} {6} {7}]", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " tried to attack " + victim.getName() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            attacker.sendMessage(ChatColor.AQUA + "PvP Protection Ignored due to combat");
        }

        if (canNotify(victim))
        {
            victim.sendMessage(ChatColor.AQUA + "PvP Protection Ignored due to combat");
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
            attacker.sendMessage(ChatColor.AQUA + "PvP bypass");
        }

        if (plugin.getPermissionsManager().has(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.getSettingsManager().isLogBypassPvp())
        {
            if (useHawkEye)
            {
                HawkEyeAPI.addCustomEntry(plugin, "PvP Bypass", attacker, victim.getLocation(), victim.getName() + " (field: " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + ")");
            }
            else
            {
                PreciousStones.log("{0} bypass-attack {1} in {2}s {3} field [{4}|{5} {6} {7}]", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " bypass-attack " + victim.getName() + " in " + Helper.posessive(field.getOwner()) + " " + fs.getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unprotectable " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " block here");
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
                PreciousStones.log("{0} attempted to place an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unprotectable " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " block here");
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
                PreciousStones.log("{0} attempted to place an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(touchingblock.getType().toString()));
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
                PreciousStones.log("{0} attempted to protect an unprotectable block [{1}|{2} {3} {4}]", player.getName(), touchingblock.getType(), touchingblock.getX(), touchingblock.getY(), touchingblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + touchingblock.getType() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(touchingblock.getType().toString()));
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
                PreciousStones.log("{0} attempted to protect an unprotectable block [{1}|{2} {3} {4}]", player.getName(), touchingblock.getType(), touchingblock.getX(), touchingblock.getY(), touchingblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + touchingblock.getType() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " inside this " + fs.getTitle() + " field");
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
                PreciousStones.log("{0} attempted to protect an unprotectable block [{1}|{2} {3} {4}] inside a field [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + unprotectableblock.getType() + "] inside a field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place " + fs.getTitle() + " field. A " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " found in the area");
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
                PreciousStones.log("{0} attempted to place a field [{1}] but an unprotectable was found in the area [{2}|{3} {4} {5}]", player.getName(), fieldtypeblock.getType(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field [" + fieldtypeblock.getType() + "] but an unprotectable was found in the area [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed near " + Helper.friendlyBlockType(protectionblock.getType().toString()) + " block");
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
                PreciousStones.log("{0} bypass-placed an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed near " + Helper.friendlyBlockType(protectionblock.getType().toString()) + " block");
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
                PreciousStones.log("{0} bypass-placed an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-protected");
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
                PreciousStones.log("{0} bypass-protected an unprotectable block [{1}|{2} {3} {4}]", player.getName(), placedblock.getType(), placedblock.getX(), placedblock.getY(), placedblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-protected an unprotectable block [" + placedblock.getType() + "|" + placedblock.getX() + " " + placedblock.getY() + " " + placedblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed in " + fs.getTitle() + " field");
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
                PreciousStones.log("{0} bypass-placed an unprotectable block [{1}|{2} {3} {4}] inside a field [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), field.getType(), field.getX(), field.getY(), field.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] inside a field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + fs.getTitle() + " field bypass-placed in an area with an " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " unprotectable block");
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
                PreciousStones.log("{0} bypass-placed a field [{1}] in an area with an unprotectable block [{2}|{3} {4} {5}]", player.getName(), fieldtypeblock.getType(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ());
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
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + "bypass-placed a field [" + fieldtypeblock.getType() + "] in an area with an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Entering " + field.getName());
        }
        else
        {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages())
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Entering " + Helper.capitalize(field.getOwner()) + "'s " + field.getSettings().getTitle() + " field");
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
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Leaving " + field.getName());
        }
        else
        {
            if (plugin.getSettingsManager().isShowDefaultWelcomeFarewellMessages())
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Leaving " + Helper.capitalize(field.getOwner()) + "'s " + field.getSettings().getTitle() + " field");
            }
        }
    }

    /**
     * @param sender
     */
    public void showNotFound(CommandSender sender)
    {
        ChatBlock.sendMessage(sender, ChatColor.RED + "No fields found");
    }

    /**
     * @param player
     */
    public void showSlowDamage(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowDamage() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
        }
    }

    /**
     * @param player
     */
    public void showPotion(Player player, String potion)
    {
        if (canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*" + potion.toLowerCase().replace("_", " ") + "*");
        }
    }

    /**
     * @param player
     */
    public void showNoPotion(Player player, String potion)
    {
        if (canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*" + potion.toLowerCase().replace("_", " ") + " neutralized*");
        }
    }

    /**
     * @param player
     */
    public void showFastDamage(Player player)
    {
        if (plugin.getSettingsManager().isWarnFastDamage() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
        }
    }

    /**
     * @param player
     */
    public void showInstantHeal(Player player)
    {
        if (plugin.getSettingsManager().isWarnInstantHeal() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*healed*");
        }
    }

    /**
     * @param player
     */
    public void showGiveAir(Player player)
    {
        if (plugin.getSettingsManager().isWarnGiveAir() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*air*");
        }
    }

    /**
     * @param player
     */
    public void showLaunch(Player player)
    {
        if (plugin.getSettingsManager().isWarnLaunch() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.LIGHT_PURPLE + "*launch*");
        }
    }

    /**
     * @param player
     */
    public void showCannon(Player player)
    {
        if (plugin.getSettingsManager().isWarnCannon() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.LIGHT_PURPLE + "*boom*");
        }
    }

    /**
     * @param player
     */
    public void showMine(Player player)
    {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "*goodbye*");
        }
    }

    /**
     * @param player
     */
    public void showLightning(Player player)
    {
        if (plugin.getSettingsManager().isWarnMine() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "*crash*");
        }
    }

    /**
     * @param player
     */
    public void showThump(Player player)
    {
        ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "*thump*");
    }

    /**
     * @param player
     */
    public void showSlowFeeding(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowFeeding() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "~Feeding~");
        }
    }

    /**
     * @param player
     */
    public void showSlowHeal(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowHeal() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*healing*");
        }
    }

    /**
     * @param player
     */
    public void showSlowRepair(Player player)
    {
        if (plugin.getSettingsManager().isWarnSlowRepair() && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "+repairing+");
        }
    }

    /**
     * @param player
     * @param block
     */
    public void showUnbreakableOwner(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + plugin.getUnbreakableManager().getOwner(block));
    }

    /**
     * @param player
     * @param block
     */
    public void showFieldOwner(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + plugin.getForceFieldManager().getOwner(block));
    }

    /**
     * @param player
     * @param block
     */
    public void showProtected(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.WHITE + "Protected: " + ChatColor.GRAY + Helper.toLocationString(block.getLocation()));
    }

    /**
     * @param block
     * @param player
     */
    public void showProtectedLocation(Player player, Block block)
    {
        List<Field> fields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.ALL);

        ChatBlock.sendBlank(player);
        ChatBlock.sendMessage(player, ChatColor.WHITE + "Protected");

        for (Field field : fields)
        {
            ChatBlock.sendMessage(player, ChatColor.YELLOW + field.getSettings().getTitle() + ": " + ChatColor.AQUA + field.getX() + " " + field.getY() + " " + field.getZ());
        }
    }

    /**
     * @param unbreakable
     * @param player
     */
    public void showUnbreakableDetails(Unbreakable unbreakable, Player player)
    {
        ChatBlock.sendBlank(player);
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + unbreakable.getOwner());
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
                ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
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
                ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
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
            ChatBlock.saySingle(sender, ChatColor.WHITE + Helper.capitalize(fs.getTitle()) + " Counts" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields found");
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
            ChatBlock.saySingle(sender, ChatColor.WHITE + Helper.posessive(Helper.capitalize(targetName)) + " Field Counts" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields found");
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

            if (player == null)
            {
                ChatBlock.saySingle(sender, ChatColor.WHITE + Helper.posessive(Helper.capitalize(targetName)) + " Field Locations" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            }
            else
            {
                ChatBlock.saySingle(sender, ChatColor.WHITE + Helper.posessive(Helper.capitalize(targetName)) + " " + Helper.capitalize(player.getWorld().getName()) + " Field Locations" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            }
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(sender);
        }
        else
        {
            ChatBlock.sendMessage(sender, ChatColor.AQUA + "No fields found");
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
                    ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
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
     * @param player
     * @param scoped
     */
    public void printTouchingFields(Player player, HashSet<Field> scoped)
    {
        if (scoped != null && scoped.size() > 0)
        {
            ChatBlock.sendBlank(player);
            ChatBlock.sendMessage(player, ChatColor.WHITE + "Touching fields:");

            for (Field field : scoped)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.AQUA);
                sb.append(field.getCoords());
                sb.append(" ");
                sb.append(ChatColor.YELLOW);
                sb.append("Owner: ");
                sb.append(ChatColor.AQUA);
                sb.append(field.getOwner());
                sb.append(ChatColor.YELLOW);

                ChatBlock.sendMessage(player, sb.toString());
            }
        }
        else
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Your field would touch no other field if placed here");
        }
    }

    /**
     * @param sender
     */
    public void showMenu(CommandSender sender)
    {
        ChatColor color = ChatColor.YELLOW;
        ChatColor colorDesc = ChatColor.AQUA;
        String status = "";

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
            ChatBlock.saySingle(sender, ChatColor.WHITE + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + status + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(sender);

            boolean more = cb.sendBlock(sender, plugin.getSettingsManager().getLinesPerPage());

            if (more)
            {
                ChatBlock.sendBlank(sender);
                ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            if (hasPlayer)
            {
                ChatBlock.sendBlank(sender);
            }
        }
    }
}
