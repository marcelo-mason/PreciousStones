package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.TreeMap;
import net.sacredlabyrinth.Phaed.PreciousStones.PlayerData;

import org.bukkit.entity.Player;

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
    private TreeMap<String, PlayerData> players = new TreeMap<String, PlayerData>();

    /**
     *
     * @param plugin
     */
    public PlayerManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @return the players
     */
    public TreeMap<String, PlayerData> getPlayers()
    {
        TreeMap<String, PlayerData> p = new TreeMap<String, PlayerData>();
        p.putAll(players);
        return p;
    }

    /**
     * Get a player's data file
     * @param playerName
     * @return
     */
    public PlayerData getPlayerData(String playerName)
    {
        PlayerData data = getPlayers().get(playerName.toLowerCase());

        if (data == null)
        {
            data = new PlayerData(plugin.getSettingsManager().isOffByDefault());
            data.setName(playerName);
            players.put(playerName.toLowerCase(), data);
        }

        return data;
    }

    /**
     * Set player as online
     * @param player
     */
    public void playerLogin(Player player)
    {
        PlayerData data = getPlayerData(player.getName().toLowerCase());
        data.setOnline(true);
    }

    /**
     * Set player as offline
     * @param player
     */
    public void playerLogoff(Player player)
    {
        PlayerData data = getPlayerData(player.getName().toLowerCase());
        data.setOnline(false);
        data.setOutsideLocation(null);
    }

    /**
     * Updates a player's last known location outside an entry field
     * @param player
     */
    public void updateOutsideLocation(Player player)
    {
        PlayerData data = getPlayerData(player.getName().toLowerCase());
        data.setOutsideLocation(player.getLocation());
    }

    /**
     * Get a player's last known location outside of an entry field
     * @param player
     * @return
     */
    public Location getOutsideLocation(Player player)
    {
        PlayerData data = getPlayerData(player.getName().toLowerCase());
        Location loc = data.getOutsideLocation();

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

        if (plugin.getSettingsManager().isThroughType(type1) && plugin.getSettingsManager().isThroughType(type2))
        {
            return true;
        }

        return false;
    }
}
