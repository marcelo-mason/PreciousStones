package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.uuid.UUIDMigration;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.apache.commons.io.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author phaed
 */
public class PlayerManager
{
    private PreciousStones plugin;
    private TreeMap<String, PlayerEntry> players = new TreeMap<String, PlayerEntry>();

    /**
     *
     */
    public PlayerManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @return the players
     */
    public TreeMap<String, PlayerEntry> getPlayers()
    {
        TreeMap<String, PlayerEntry> p = new TreeMap<String, PlayerEntry>();
        p.putAll(players);
        return p;
    }

    /**
     * Get a player's data file
     *
     * @param playerName
     * @return
     */
    public PlayerEntry getPlayerEntry(String playerName)
    {
        PlayerEntry data = players.get(playerName.toLowerCase());

        if (data == null)
        {
            data = new PlayerEntry();
            UUID pulledUUID = UUIDMigration.findPlayerUUID(playerName);
            if(pulledUUID != null)
            {
                data.setOnlineUUID(pulledUUID);
                PreciousStones.log("[Online UUID Found] Player: " + playerName + " UUID: " + pulledUUID.toString());
            }
            data.setOfflineUUID(UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + playerName ).getBytes( Charsets.UTF_8 ) ));
            data.setName(playerName);
            players.put(playerName.toLowerCase(), data);
        }

        return data;
    }

    /**
     * Set player as online
     *
     * @param playerName
     */
    public void playerLogin(String playerName)
    {
        PlayerEntry data = getPlayerEntry(playerName);
        data.setOnline(true);
    }

    /**
     * Set player as offline
     *
     * @param player
     */
    public void playerLogoff(Player player)
    {
        PlayerEntry data = getPlayerEntry(player.getName());
        data.setOnline(false);
        data.setOutsideLocation(null);
    }

    /**
     * Updates a player's last known location outside an entry field
     *
     * @param player
     */
    public void updateOutsideLocation(Player player)
    {
        PlayerEntry data = getPlayerEntry(player.getName());
        data.setOutsideLocation(player.getLocation());
    }

    /**
     * Get a player's last known location outside of an entry field
     *
     * @param player
     * @return
     */
    public Location getOutsideLocation(Player player)
    {
        PlayerEntry data = getPlayerEntry(player.getName());
        Location loc = data.getOutsideLocation();

        if (loc != null)
        {
            loc = new Location(loc.getWorld(), ((double) loc.getBlockX()) + .5, ((double) loc.getBlockY()), ((double) loc.getBlockZ()) + .5, loc.getYaw(), loc.getPitch());
        }

        return loc;
    }

    /**
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

    public void offerOnlinePlayerEntries()
    {
        Collection<Player> onlinePlayers = Helper.getOnlinePlayers();

        for (Player player : onlinePlayers)
        {
            plugin.getStorageManager().offerPlayer(player.getName());
        }
    }


    /**
     * Changes username of player
     *
     * @param oldName
     * @param newName
     */
    public void migrateUsername(String oldName, String newName)
    {
        PlayerEntry data = players.get(oldName.toLowerCase());
        data.setName(newName);

        players.remove(oldName.toLowerCase());
        players.put(newName.toLowerCase(), data);

        plugin.getStorageManager().offerPlayer(newName);
    }
}
