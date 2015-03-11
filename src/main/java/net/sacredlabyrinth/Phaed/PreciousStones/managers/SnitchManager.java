package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
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
     * @param player
     * @param entity
     */
    public void recordSnitchEntityKill(Player player, Entity entity)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(entity.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_kill"), entity.getType().getName(), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param victim
     */
    public void recordSnitchPlayerKill(Player player, Player victim)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(victim.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_kill"), victim.getName(), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void recordSnitchEntry(Player player, Field field)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            if (FieldFlag.SNITCH.applies(field, player))
            {
                DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a z");
                plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_entry"), dateFormat.format(new Date()), 1));
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchBlockBreak(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_blockBreak"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchBucketEmpty(Player player, Block block, String type)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    String details = type + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";

                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_bucketEmpty"), details, 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchBucketFill(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_bucketFilled"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchBlockPlace(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_blockPlace"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchUsed(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_used"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchLWC(Player player, Block block, Set<String> actions)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    for (String action : actions)
                    {
                        plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_LWC") + " " + action, Helper.toLocationString(block.getLocation()), 1));
                    }
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchShop(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

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
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_shopped"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * @param player
     * @param block
     */
    public void recordSnitchIgnite(Player player, Block block)
    {
        if (plugin.getPermissionsManager().isVanished(player))
        {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.snitch"))
        {
            List<Field> snitchFields = plugin.getForceFieldManager().getEnabledSourceFields(block.getLocation(), FieldFlag.SNITCH);

            for (Field field : snitchFields)
            {
                if (FieldFlag.SNITCH.applies(field, player))
                {
                    plugin.getStorageManager().offerSnitchEntry(new SnitchEntry(field, player.getName(), ChatBlock.format("_ignited"), toBlockDetails(block), 1));
                }
            }
        }
    }

    /**
     * Returns formatted coordinates
     *
     * @param block
     * @return
     */
    public static String toBlockDetails(Block block)
    {
        if (PreciousStones.getInstance().getSettingsManager().isUseIdInSnitches())
        {
            return (new BlockTypeEntry(block).toString()) + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
        }

        return new BlockTypeEntry(block).toString() + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
    }
}
