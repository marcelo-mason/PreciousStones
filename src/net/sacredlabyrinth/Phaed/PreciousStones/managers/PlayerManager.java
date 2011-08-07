package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public class PlayerManager
{
    private PreciousStones plugin;
    private HashMap<String, PlayerStatus> players = new HashMap<String, PlayerStatus>();
    private HashMap<String, Location> outsideLocation = new HashMap<String, Location>();

    /**
     *
     * @param plugin
     */
    public PlayerManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Updates a player's last known location outside an entry field
     * @param player
     */
    public void updateOutsideLocation(Player player)
    {
        outsideLocation.put(player.getName(), player.getLocation());
    }

    /**
     * Remove player from list
     * @param player
     */
    public void cleanOutsideLocation(Player player)
    {
        outsideLocation.remove(player.getName());
    }

    /**
     * Get a player's last known location outside of an entry field
     * @param player
     * @return
     */
    public Location getOutsideLocation(Player player)
    {
        Location loc = outsideLocation.get(player.getName());

        if (loc != null)
        {
            loc = new Location(loc.getWorld(), ((double) loc.getBlockX()) + .5, ((double) loc.getBlockY()), ((double) loc.getBlockZ()) + .5, loc.getYaw(), loc.getPitch());
        }

        return loc;
    }

    /**
     *
     * @param field
     * @param player
     * @return
     */
    public Location getOutsideFieldLocation(Field field, Player player)
    {
        World world = player.getWorld();

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY() + 1;
        int z = player.getLocation().getBlockZ();

        int edgeX1 = field.getX() + (field.getRadius() + 1);
        int edgeX2 = field.getX() - (field.getRadius() + 1);
        int edgeZ1 = field.getZ() + (field.getRadius() + 1);
        int edgeZ2 = field.getZ() - (field.getRadius() + 1);

        Location loc = world.getSpawnLocation();

        if (isEmptySpace(world, edgeX1, y, z))
        {
            loc = new Location(world, ((double) edgeX1) + .5, y, ((double) z) + .5, player.getLocation().getYaw(), player.getLocation().getPitch());
        }
        else if (isEmptySpace(world, edgeX2, y, z))
        {
            loc = new Location(world, ((double) edgeX2) + .5, y, ((double) z) + .5, player.getLocation().getYaw(), player.getLocation().getPitch());
        }
        else if (isEmptySpace(world, x, y, edgeZ1))
        {
            loc = new Location(world, ((double) x) + .5, y, ((double) edgeZ1) + .5, player.getLocation().getYaw(), player.getLocation().getPitch());
        }
        else if (isEmptySpace(world, x, y, edgeZ2))
        {
            loc = new Location(world, ((double) x) + .5, y, ((double) edgeZ2) + .5, player.getLocation().getYaw(), player.getLocation().getPitch());
        }

        return loc;
    }

    private boolean isEmptySpace(World world, int x, int y, int z)
    {
        int type1 = world.getBlockTypeIdAt(x, y, z);
        int type2 = world.getBlockTypeIdAt(x, y, z);

        if (plugin.settings.isThroughType(type1) && plugin.settings.isThroughType(type2))
        {
            return true;
        }

        return false;
    }

    /**
     *
     * @param player
     * @return
     */
    public boolean isDisabled(Player player)
    {
        PlayerStatus ps = players.get(player.getName());

        if (ps == null)
        {
            return plugin.settings.offByDefault;
        }
        else
        {
            return ps.getDisabled();
        }
    }

    /**
     *
     * @param player
     * @param disabled
     */
    public void setDisabled(Player player, boolean disabled)
    {
        PlayerStatus ps = players.get(player.getName());

        if (ps == null)
        {
            ps = new PlayerStatus();
            ps.setDisabled(disabled);

            players.put(player.getName(), ps);
        }
        else
        {
            ps.setDisabled(disabled);
        }
    }

    /**
     *
     */
    public class PlayerStatus
    {
        boolean disabled = false;

        /**
         *
         * @return
         */
        public boolean getDisabled()
        {
            return this.disabled;
        }

        /**
         *
         * @param disabled
         */
        public void setDisabled(boolean disabled)
        {
            this.disabled = disabled;
        }
    }

    /**
     *
     * @param player
     */
    public void dropInventory(Player player)
    {
        PlayerInventory inv = player.getInventory();

        if (inv != null)
        {
            for (int i = 0; i < inv.getSize(); i++)
            {
                ItemStack stack = inv.getItem(i);

                if (stack != null)
                {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                }
            }
        }
    }
}
