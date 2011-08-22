package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
 */
public class SnitchManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public SnitchManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     *
     * @param player
     * @param field
     */
    public void recordSnitchEntry(Player player, Field field)
    {
        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            FieldSettings fs = field.getSettings();

            if (fs.hasFlag(FieldFlag.SNITCH))
            {
                if (!field.isOwner(player.getName()))
                {
                    DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a z");
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Entry", dateFormat.format(new Date()), 1));
                }
            }
        }

    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchBlockBreak(Player player, Block block)
    {
        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (!field.isOwner(player.getName()))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Block Break", toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchBlockPlace(Player player, Block block)
    {
        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (!field.isOwner(player.getName()))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Block Place", toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchUsed(Player player, Block block)
    {
        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (!field.isOwner(player.getName()))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Used", toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchShop(Player player, Block block)
    {
        Sign sign = (Sign) block.getState();

        if (sign.getLines().length == 0)
        {
            return;
        }

        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (!field.isOwner(player.getName()))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Shopped", toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchIgnite(Player player, Block block)
    {
        if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (!field.isOwner(player.getName()))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Ignite", toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * Returns formatted coordinates
     * @param block
     * @return
     */
    public static String toBlockDetails(Block block)
    {
        return Helper.friendlyBlockType(block.getType().toString()) + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
    }
}
