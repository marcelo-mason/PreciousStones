package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

/**
 *
 * @author phaed
 */
public class CommunicatonManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public CommunicatonManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    private boolean canNotify(Player player)
    {
        return !(plugin.pm.hasPermission(player, "preciousstones.override.notify") && !plugin.pm.hasPermission(player, "preciousstones.admin.isadmin"));
    }

    private boolean canWarn(Player player)
    {
        return !(plugin.pm.hasPermission(player, "preciousstones.override.warn") && !plugin.pm.hasPermission(player, "preciousstones.admin.isadmin"));
    }

    private boolean canAlert(Player player)
    {
        return !(plugin.settings.disableAlertsForAdmins && plugin.pm.hasPermission(player, "preciousstones.admin.isadmin"));
    }

    private boolean canBypassAlert(Player player)
    {
        return !(plugin.settings.disableBypassAlertsForAdmins && plugin.pm.hasPermission(player, "preciousstones.admin.isadmin"));
    }

    /**
     *
     * @param player
     * @param unbreakableblock
     */
    public void notifyPlaceU(Player player, Block unbreakableblock)
    {
        Unbreakable unbreakable = plugin.um.getUnbreakable(unbreakableblock);

        if (plugin.settings.notifyPlace && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block placed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPlace)
        {
            PreciousStones.log(Level.INFO, " {0} placed an unbreakable block [{1}|{2} {3} {4}]", player.getName(), unbreakable.getTypeId(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed an unbreakable block [" + unbreakable.getTypeId() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyPlace && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + fs.getTitleCap() + " field placed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPlace)
        {
            PreciousStones.log(Level.INFO, " {0} placed a {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed a " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyPlaceBreakableFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyPlace && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Breakable " + fs.getTitle() + " field placed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPlace)
        {
            PreciousStones.log(Level.INFO, " {0} placed a breakable {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), fieldblock.getType(), fieldblock.getX(), fieldblock.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.place") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " placed breakable " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param unbreakableblock
     */
    public void notifyDestroyU(Player player, Block unbreakableblock)
    {
        if (plugin.settings.notifyDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} destroyed his own unbreakable block [{1}|{2} {3} {4}]", player.getName(), unbreakableblock.getType(), unbreakableblock.getX(), unbreakableblock.getY(), unbreakableblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his own unbreakable block [" + unbreakableblock.getType() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + fs.getTitleCap() + " field destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} destroyed his {1} field [{2}|{3} {4} {5}]", player.getName(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed his " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyOthersFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + fs.getTitleCap() + " field destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} destroyed {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destroyed " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyDestroyBreakableFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + field.getOwner() + "'s breakable " + fs.getTitle() + " field destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} destroyed {1}''s breakable {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " destoyed " + field.getOwner() + "'s breakable " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param field
     */
    public void notifyBypassPlace(Player player, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassPlace && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-placed inside " + field.getOwner() + "'s " + fs.getTitle() + " field");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassPlace)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-placed a block inside {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed a block inside " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param field
     */
    public void notifyBypassPlaceU(Player player, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassPlace && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unbreakable block bypass-placed inside " + field.getOwner() + "'s " + fs.getTitle() + " field");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassPlace)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-placed an unbreakable block inside {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.bypass-place") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unbreakable block inside " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void notifyBypassDestroy(Player player, Block block, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Block bypass-destroyed in " + field.getOwner() + "'s " + fs.getTitle() + " field");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-destroyed a block {1} in {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyBypassDestroyU(Player player, Block fieldblock)
    {
        Unbreakable unbreakable = plugin.um.getUnbreakable(fieldblock);

        if (plugin.settings.notifyBypassDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + unbreakable.getOwner() + "'s unbreakable block bypass-destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-destroyed {1}''s unbreakable block [{2}|{3} {4} {5}]", player.getName(), unbreakable.getOwner(), unbreakable.getType(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.bypass-destroy") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void notifyBypassDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassDestroy && canNotify(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + field.getOwner() + "'s " + fs.getTitle() + " field bypass-destroyed");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log-destroy"))
        {
            return;
        }

        if (plugin.settings.logBypassDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-destroyed {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.notify.bypass") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-destroyed a block in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param field
     */
    public void warnEntry(Player player, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnEntry && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot enter protected area");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logEntry)
        {
            PreciousStones.log(Level.INFO, " {0} attempted entry into {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.entry") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted entry into " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param field
     */
    public void warnFire(Player player, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnFire && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place fires here");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logFire)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to light fire in {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.fire") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to light a fire in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnPlace(Player player, Block block, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnPlace && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted place a block {1} in {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.place") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a block " + (new Vec(block)).toString() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param mat
     * @param field
     */
    public void warnUse(Player player, Material mat, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnUse && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot use this");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUse)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to use a {1} in {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), mat.toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.use") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to use a " + mat.toString() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param mat
     * @param field
     */
    public void warnEmpty(Player player, Material mat, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnPlace && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place here");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted empty a {1} in {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), mat.toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.place") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to empty a " + mat.toString() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param unbreakableblock
     */
    public void warnDestroyU(Player player, Block unbreakableblock)
    {
        Unbreakable unbreakable = plugin.um.getUnbreakable(unbreakableblock);

        if (plugin.settings.warnDestroy && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to destroy {1}''s unbreakable block [{2}|{3} {4} {5}]", player.getName(), unbreakable.getOwner(), unbreakable.getType(), unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + unbreakable.getOwner() + "'s unbreakable block [" + unbreakable.getType() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param fieldblock
     */
    public void warnDestroyFF(Player player, Block fieldblock)
    {
        Field field = plugin.ffm.getField(fieldblock);
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnDestroy && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Only the owner can remove this block");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroy)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to destroy {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.destroy") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param damagedblock
     * @param field
     */
    public void warnDestroyArea(Player player, Block damagedblock, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnDestroyArea && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot destroy here");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logDestroyArea)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to destroy a block {1} inside {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(damagedblock)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.destroyarea") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to destroy a block " + (new Vec(damagedblock)).toString() + " inside " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictU(Player player, Block block, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here. Conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unbreakable block here");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place an unbreakable block {1} conflicting with {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable block " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictFF(Player player, Block block, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place field here. Conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place field here");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place a field {1} conflicting with {2}''s {3} field [{4}|{5} {6} {7}]", player.getName(), (new Vec(block)).toString(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field " + (new Vec(block)).toString() + " conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param ub
     */
    public void warnConflictPistonU(Player player, Block block, Unbreakable ub)
    {
        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place piston here. Conflicting with " + ub.getOwner() + "'s unbreakable [" + ub.getType() + "|" + ub.getX() + " " + ub.getY() + " " + ub.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place piston here.");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place a piston conflicting with {1}''s unbreakable [{2}|{3} {4} {5}]", player.getName(), ub.getOwner(), ub.getType(), ub.getX(), ub.getY(), ub.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an piston conflicting with " + ub.getOwner() + "'s unbreakable");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictPistonFF(Player player, Block block, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a piston here. Conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a piston here");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place a piston conflicting with {1}''s {2} field [{3}|{4} {5} {6}]", player.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a piston conflicting with " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictPistonRU(Player player, Block block, Block pistonBlock)
    {
        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a unbreakable block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a unbreakable block here");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place an unbreakable conflicting with piston [{1} {2} {3}]", player.getName(), pistonBlock.getX(), pistonBlock.getY(), pistonBlock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unbreakable conflicting with a piston");
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     * @param field
     */
    public void warnConflictPistonRFF(Player player, Block block, Block pistonBlock)
    {
        if (canWarn(player))
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.viewconflicting"))
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a field block here. Conflicting with piston [" + pistonBlock.getX() + " " + pistonBlock.getY() + " " + pistonBlock.getZ() + "]");
            }
            else
            {
                ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place a field block here");
            }
        }
        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logConflictPlace)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place a field conflicting with piston [{1} {2} {3}]", player.getName(), pistonBlock.getX(), pistonBlock.getY(), pistonBlock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.conflict") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field block conflicting with a piston");
            }
        }
    }

    /**
     *
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnPvP(Player attacker, Player victim, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnPvp && canWarn(attacker))
        {
            attacker.sendMessage(ChatColor.AQUA + "PvP disabled in this area");
        }

        if (plugin.pm.hasPermission(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPvp)
        {
            PreciousStones.log(Level.INFO, " {0} tried to attack {1} in {2}''s {3} field [{4}|{5} {6} {7}]", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " tried to attack " + victim.getName() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param attacker
     * @param victim
     * @param field
     */
    public void warnBypassPvP(Player attacker, Player victim, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassPvp && canNotify(attacker))
        {
            attacker.sendMessage(ChatColor.AQUA + "PvP bypass");
        }

        if (plugin.pm.hasPermission(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassPvp)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-attack {1} in {2}''s {3} field [{4}|{5} {6} {7}]", attacker.getName(), victim.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " bypass-attack " + victim.getName() + " in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param attacker
     * @param field
     */
    public void warnPvPLavaPlace(Player attacker, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnPvp && canWarn(attacker))
        {
            attacker.sendMessage(ChatColor.AQUA + "PvP disabled in this area");
        }

        if (plugin.pm.hasPermission(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logPvp)
        {
            PreciousStones.log(Level.INFO, " {0} tried to place lava in {1}''s {2} field [{3}|{4} {5} {6}]", attacker.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " tried to palce lava in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param attacker
     * @param field
     */
    public void warnBypassPvPLavaPlace(Player attacker, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.notifyBypassPvp && canNotify(attacker))
        {
            attacker.sendMessage(ChatColor.AQUA + "PvP bypass");
        }

        if (plugin.pm.hasPermission(attacker, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logBypassPvp)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-lava-place in {1}''s {2} field [{3}|{4} {5} {6}]", attacker.getName(), field.getOwner(), fs.getTitle(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(attacker))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.pvp") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + attacker.getName() + " bypass-lava-place in " + field.getOwner() + "'s " + fs.getTitle() + " field");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void warnPlaceUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place unprotectable " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " block here");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param placedblock
     */
    public void warnPlaceTouchingUnprotectable(Player player, Block placedblock)
    {
        Block touchingblock = plugin.upm.getTouchingUnprotectableBlock(placedblock);

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(touchingblock.getType().toString()));
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to protect an unprotectable block [{1}|{2} {3} {4}]", player.getName(), touchingblock.getType(), touchingblock.getX(), touchingblock.getY(), touchingblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + touchingblock.getType() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void warnPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot protect " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " inside this " + fs.getTitle() + " field");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to protect an unprotectable block [{1}|{2} {3} {4}] inside a field [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to protect an unprotectable block [" + unprotectableblock.getType() + "] inside a field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void warnPlaceFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(fieldtypeblock.getTypeId());

        if (fs == null)
        {
            return;
        }

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Cannot place " + fs.getTitle() + " field. A " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " found in the area");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} attempted to place a field [{1}] but an unprotectable was found in the area [{2}|{3} {4} {5}]", player.getName(), fieldtypeblock.getType(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " attempted to place a field [" + fieldtypeblock.getType() + "] but an unprotectable was found in the area [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param protectionblock
     */
    public void notifyBypassUnprotectableTouching(Player player, Block unprotectableblock, Block protectionblock)
    {
        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed near " + Helper.friendlyBlockType(protectionblock.getType().toString()) + " block");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-placed an unprotectable block [{1}|{2} {3} {4}] near [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), protectionblock.getType(), protectionblock.getX(), protectionblock.getY(), protectionblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] near [" + protectionblock.getType() + "|" + protectionblock.getX() + " " + protectionblock.getY() + " " + protectionblock.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param placedblock
     */
    public void notifyBypassTouchingUnprotectable(Player player, Block placedblock)
    {
        Block unprotectableblock = plugin.upm.getTouchingUnprotectableBlock(placedblock);

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-protected");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-protected an unprotectable block [{1}|{2} {3} {4}]", player.getName(), placedblock.getType(), placedblock.getX(), placedblock.getY(), placedblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-protected an unprotectable block [" + placedblock.getType() + "|" + placedblock.getX() + " " + placedblock.getY() + " " + placedblock.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param field
     */
    public void notifyBypassPlaceUnprotectableInField(Player player, Block unprotectableblock, Field field)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(field);

        if (fs == null)
        {
            plugin.ffm.queueRelease(field);
            return;
        }

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + "Unprotectable block " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " bypass-placed in " + fs.getTitle() + " field");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-placed an unprotectable block [{1}|{2} {3} {4}] inside a field [{5}|{6} {7} {8}]", player.getName(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ(), field.getType(), field.getX(), field.getY(), field.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + " bypass-placed an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "] inside a field [" + field.getType() + "|" + field.getX() + " " + field.getY() + " " + field.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param unprotectableblock
     * @param fieldtypeblock
     */
    public void notifyBypassFieldInUnprotectable(Player player, Block unprotectableblock, Block fieldtypeblock)
    {
        FieldSettings fs = plugin.settings.getFieldSettings(fieldtypeblock.getTypeId());

        if (fs == null)
        {
            return;
        }

        if (plugin.settings.warnUnprotectable && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.AQUA + fs.getTitle() + " field bypass-placed in an area with an " + Helper.friendlyBlockType(unprotectableblock.getType().toString()) + " unprotectable block");
        }

        if (plugin.pm.hasPermission(player, "preciousstones.admin.bypass.log"))
        {
            return;
        }

        if (plugin.settings.logUnprotectable)
        {
            PreciousStones.log(Level.INFO, " {0} bypass-placed a field [{1}] in an area with an unprotectable block [{2}|{3} {4} {5}]", player.getName(), fieldtypeblock.getType(), unprotectableblock.getType(), unprotectableblock.getX(), unprotectableblock.getY(), unprotectableblock.getZ());
        }

        for (Player pl : plugin.getServer().getOnlinePlayers())
        {
            if (pl.equals(player))
            {
                continue;
            }

            if (plugin.pm.hasPermission(pl, "preciousstones.alert.warn.unprotectable") && canBypassAlert(pl))
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + "[ps] " + ChatColor.GRAY + player.getName() + "bypass-placed a field [" + fieldtypeblock.getType() + "] in an area with an unprotectable block [" + unprotectableblock.getType() + "|" + unprotectableblock.getX() + " " + unprotectableblock.getY() + " " + unprotectableblock.getZ() + "]");
            }
        }
    }

    /**
     *
     * @param player
     * @param name
     */
    public void showWelcomeMessage(Player player, String name)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Entering: " + ChatColor.AQUA + name);
    }

    /**
     *
     * @param player
     * @param name
     */
    public void showFarewellMessage(Player player, String name)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Leaving: " + ChatColor.AQUA + name);
    }

    /**
     *
     * @param player
     */
    public void showNotFound(Player player)
    {
        ChatBlock.sendMessage(player, ChatColor.RED + "No fields found");
    }

    /**
     *
     * @param player
     */
    public void showSlowDamage(Player player)
    {
        if (plugin.settings.warnSlowDamage && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
        }
    }

    /**
     *
     * @param player
     */
    public void showFastDamage(Player player)
    {
        if (plugin.settings.warnFastDamage && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.DARK_RED + "*damage*");
        }
    }

    /**
     *
     * @param player
     */
    public void showInstantHeal(Player player)
    {
        if (plugin.settings.warnInstantHeal && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*healed*");
        }
    }

    /**
     *
     * @param player
     */
    public void showGiveAir(Player player)
    {
        if (plugin.settings.warnGiveAir && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*air*");
        }
    }

    /**
     *
     * @param player
     */
    public void showLaunch(Player player)
    {
        if (plugin.settings.warnLaunch && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.LIGHT_PURPLE + "*launch*");
        }
    }

    /**
     *
     * @param player
     */
    public void showCannon(Player player)
    {
        if (plugin.settings.warnCannon && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.LIGHT_PURPLE + "*boom*");
        }
    }

    /**
     *
     * @param player
     */
    public void showMine(Player player)
    {
        if (plugin.settings.warnMine && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "*goodbye*");
        }
    }

    /**
     *
     * @param player
     */
    public void showLightning(Player player)
    {
        if (plugin.settings.warnMine && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "*crash*");
        }
    }

    /**
     *
     * @param player
     */
    public void showThump(Player player)
    {
        ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "*thump*");
    }

    /**
     *
     * @param player
     */
    public void showSlowHeal(Player player)
    {
        if (plugin.settings.warnSlowHeal && canWarn(player))
        {
            ChatBlock.sendMessage(player, ChatColor.WHITE + "*healing*");
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void showUnbreakableOwner(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + plugin.um.getOwner(block));
    }

    /**
     *
     * @param player
     * @param block
     */
    public void showFieldOwner(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + plugin.ffm.getOwner(block));
    }

    /**
     *
     * @param player
     */
    public void showProtected(Player player, Block block)
    {
        ChatBlock.sendMessage(player, ChatColor.WHITE + "Protected: " + ChatColor.GRAY + Helper.toLocationString(block.getLocation()));
    }

    /**
     *
     * @param fields
     * @param player
     */
    public void showProtectedLocation(List<Field> fields, Player player, Block block)
    {
        ChatBlock.sendBlank(player);
        ChatBlock.sendMessage(player, ChatColor.WHITE + "Protected: " + ChatColor.GRAY + Helper.toLocationString(block.getLocation()));
        for (Field field : fields)
        {
            ChatBlock.sendMessage(player, ChatColor.YELLOW + "Source: " + ChatColor.AQUA + field.getX() + " " + field.getY() + " " + field.getZ());
        }
    }

    /**
     *
     * @param unbreakable
     * @param player
     */
    public void showUnbreakableDetails(Unbreakable unbreakable, Player player)
    {
        ChatBlock.sendBlank(player);
        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Owner: " + ChatColor.AQUA + unbreakable.getOwner());
    }

    /**
     *
     * @param player
     * @param fields
     */
    public void showFieldDetails(Player player, List<Field> fields)
    {
        ChatBlock chatBlock = plugin.com.getCacheBlock();

        for (Field field : fields)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field);

            if (fs == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            chatBlock.addRow("", "");

            if ((fs.farewellMessage || fs.welcomeMessage) && field.getName().length() > 0)
            {
                chatBlock.addRow("  " + ChatColor.YELLOW + "Name: ", ChatColor.AQUA + field.getName());
            }

            chatBlock.addRow("  " + ChatColor.YELLOW + "Owner: ", ChatColor.AQUA + field.getOwner());

            if (field.getAllowedList() != null)
            {
                chatBlock.addRow("  " + ChatColor.YELLOW + "Allowed: ", ChatColor.AQUA + field.getAllowedList());
            }

            chatBlock.addRow("  " + ChatColor.YELLOW + "Dimensions: ", ChatColor.AQUA + "" + ((field.getRadius() * 2) + 1) + "x" + field.getHeight() + "x" + ((field.getRadius() * 2) + 1));

            if (field.getVelocity() > 0)
            {
                chatBlock.addRow("  " + ChatColor.YELLOW + "Velocity: ", ChatColor.AQUA + "" + field.getVelocity());
            }

            chatBlock.addRow("  " + ChatColor.YELLOW + "Location: ", ChatColor.AQUA + "" + field.getX() + " " + field.getY() + " " + field.getZ());

            chatBlock.addRow("  " + ChatColor.YELLOW + "Type: ", ChatColor.AQUA + fs.getTitle());
        }

        if (chatBlock.size() > 0)
        {
            chatBlock.addRow("", "");

            ChatBlock.sendBlank(player);
            ChatBlock.saySingle(player, ChatColor.WHITE + "Field Info " + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");

            boolean more = chatBlock.sendBlock(player, plugin.settings.linesPerPage);

            if (more)
            {
                ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }
        }
    }

    /**
     * Shows all the configured fields to the player
     * @param player
     */
    public void showConfiguredFields(Player player)
    {
        ChatBlock chatBlock = plugin.com.getCacheBlock();

        HashMap<Integer, FieldSettings> fss = plugin.settings.getFieldSettings();

        for (FieldSettings fs : fss.values())
        {
            chatBlock.addRow(ChatColor.YELLOW + "Type: " + ChatColor.AQUA + "" + Material.getMaterial(fs.blockId) + " " + ChatColor.YELLOW + "Title: " + ChatColor.AQUA + fs.title);
            chatBlock.addRow(ChatColor.YELLOW + "Radius: " + ChatColor.AQUA + "" + fs.radius + " " + ChatColor.YELLOW + "Height: " + ChatColor.AQUA + "" + fs.height);
            chatBlock.addRow("");
        }

        if (chatBlock.size() > 0)
        {
            ChatBlock.sendBlank(player);
            ChatBlock.saySingle(player, ChatColor.WHITE + "Configured Fields" + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
            ChatBlock.sendBlank(player);

            boolean more = chatBlock.sendBlock(player, plugin.settings.linesPerPage);

            if (more)
            {
                ChatBlock.sendBlank(player);
                ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Type /ps more to view next page.");
            }

            ChatBlock.sendBlank(player);
        }

    }

    /**
     *
     * @param player
     * @param field
     */
    public boolean showSnitchList(Player player, Field field)
    {
        if (field != null)
        {
            List<SnitchEntry> snitches = field.getSnitches();

            if (snitches.isEmpty() || snitches.get(0).getAgeInSeconds() > 10)
            {
                snitches = plugin.sm.getSnitchEntries(field);
                field.setSnitches(snitches);
                field.updateLastUsed();
                plugin.sm.offerField(field);
            }

            String title = "Intruder log ";

            FieldSettings fs = plugin.settings.getFieldSettings(field);

            if (fs != null)
            {
                title = fs.getTitle();
            }

            if (!snitches.isEmpty())
            {
                ChatBlock chatBlock = plugin.com.getCacheBlock();

                ChatBlock.sendBlank(player);
                ChatBlock.saySingle(player, ChatColor.WHITE + title + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------------");
                ChatBlock.sendBlank(player);

                chatBlock.addRow("  " + ChatColor.GRAY + "Name", "Reason", "Details");

                for (SnitchEntry se : snitches)
                {
                    chatBlock.addRow("  " + ChatColor.GOLD + se.getName(), se.getReasonDisplay(), ChatColor.WHITE + se.getDetails());
                }

                boolean more = chatBlock.sendBlock(player, plugin.settings.linesPerPage);

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
            plugin.cm.showNotFound(player);
        }

        return false;
    }

    /**
     *
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
     *
     * @param msg
     */
    public void debug(String msg)
    {
        PreciousStones.log(Level.INFO, "[debug] ***************** {0}", msg);
    }
}
