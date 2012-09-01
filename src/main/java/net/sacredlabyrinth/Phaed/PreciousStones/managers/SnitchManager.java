package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author phaed
 */
public class SnitchManager
{
    private PreciousStones plugin;

    /**
     *
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
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            if (field.hasFlag(FieldFlag.SNITCH))
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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
    public void recordSnitchBlucketEmpty(Player player, Block block, String type)
    {
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                {
                    String details = Helper.friendlyBlockType(type) + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";

                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Bucket Empty", details, 1));
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchBlucketFill(Player player, Block block)
    {
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), "Bucket Filled", toBlockDetails(block), 1));
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
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
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
