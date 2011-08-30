package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author phaed
 */
public class LimitManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public LimitManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Whether the player has reached the placing limit for a field
     * @param player
     * @param fs the field settings of the field you need to get the limit of
     * @return
     */
    public boolean reachedLimit(Player player, FieldSettings fs)
    {
        List<Integer> limits = fs.getLimits();

        if (limits.isEmpty())
        {
            return false;
        }

        if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.limits"))
        {
            return false;
        }

        int limit = getLimit(player, fs);
        int count = plugin.getPlayerManager().getPlayerData(player.getName()).getFieldCount(fs.getTypeId());

        if (limit == -1)
        {
            return false;
        }

        if (limit == 0)
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "You cannot place any " + fs.getTitle());
            return true;
        }

        if (count >= limit)
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "You have reached the " + fs.getTitle() + " limit of " + limit);
            return true;
        }

        return false;
    }

    /**
     * Gets the maximum about of fields a player can place
     * @param player
     * @param fs the field settings of the field you need to get the limit of
     * @return the limit, -1 if no limit
     */
    public int getLimit(Player player, FieldSettings fs)
    {
        List<Integer> limits = fs.getLimits();

        if (limits.isEmpty())
        {
            return -1;
        }

        for (int i = limits.size() - 1; i >= 0; i--)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.limit" + (i + 1)))
            {
                return limits.get(i);
            }
        }

        return -1;
    }
}
