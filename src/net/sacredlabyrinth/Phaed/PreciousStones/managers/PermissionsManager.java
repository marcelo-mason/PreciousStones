package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * @author phaed
 */
public final class PermissionsManager
{
    public static Permission permission = null;
    private static Economy economy = null;
    private PermissionHandler handler = null;
    private PermissionsPlugin pbukkit = null;
    private LWC lwc = null;
    private WorldGuardPlugin wg = null;
    private PreciousStones plugin;

    /**
     *
     */
    public PermissionsManager()
    {
        plugin = PreciousStones.getInstance();
        detectPermissionsBukkit();
        detectPermissions();
        detectWorldGuard();
        detectLWC();

        try
        {
            Class.forName("net.milkbowl.vault.permission.Permission");

            setupEconomy();
            setupPermissions();
        }
        catch (ClassNotFoundException e)
        {
            //SimpleClans.log("[PreciousStones] Vault.jar not found. No economy support.");
            //no need to spam everyone who doesnt use vault
        }
    }

    private void detectWorldGuard()
    {
        if (wg == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

            if (test != null)
            {
                this.wg = (WorldGuardPlugin) test;
            }
        }
    }

    private void detectLWC()
    {
        Plugin lwcPlugin = plugin.getServer().getPluginManager().getPlugin("LWC");

        if (lwcPlugin != null)
        {
            lwc = ((LWCPlugin) lwcPlugin).getLWC();
        }
    }

    private void detectPermissions()
    {
        if (handler == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");

            if (test != null)
            {
                handler = ((Permissions) test).getHandler();
            }
        }
    }

    private void detectPermissionsBukkit()
    {
        if (pbukkit == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");

            if (test != null)
            {
                pbukkit = ((PermissionsPlugin) test);
            }
        }
    }

    private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
        {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null)
        {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    /**
     * Check whether a player has a permission
     *
     * @param player
     * @param permission
     * @return
     */
    public boolean has(Player player, String perm)
    {
        if (player == null)
        {
            return true;
        }

        if (permission != null)
        {
            if (permission.has(player, "preciousstones.blacklist") && !permission.has(player, "preciousstones.admintest"))
            {
                return false;
            }

            return permission.has(player, perm);
        }
        else if (handler != null)
        {
            if (handler.has(player, "preciousstones.blacklist") && !handler.has(player, "preciousstones.admintest"))
            {
                return false;
            }

            return handler.has(player, perm);
        }
        else
        {
            return player.hasPermission(perm);
        }
    }

    /**
     * Check whether a player belongs to a group
     *
     * @param playerName
     * @param group
     * @param world
     * @return
     */
    public boolean inGroup(String playerName, World world, String group)
    {
        if (pbukkit != null)
        {
            List<Group> groups = pbukkit.getGroups(playerName);

            for (Group g : groups)
            {
                if (g.getName().equalsIgnoreCase(group))
                {
                    return true;
                }
            }
            return false;
        }
        else if (permission != null)
        {
            return permission.playerInGroup(world, playerName, group);
        }
        else if (handler != null)
        {
            if (handler.getGroup(world.getName(), playerName).equalsIgnoreCase(group))
            {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Get a player's groups
     *
     * @param worldName
     * @param playerName
     * @return
     */
    public List<String> getGroups(String worldName, String playerName)
    {
        List<String> groups = new LinkedList<String>();

        if (pbukkit != null)
        {
            List<Group> gs = pbukkit.getGroups(playerName);

            for (Group group : gs)
            {
                groups.add(group.getName().toLowerCase());
            }
            return groups;
        }
        else if (permission != null)
        {
            World world = plugin.getServer().getWorld(worldName);

            if (world != null)
            {
                String[] groupList = permission.getPlayerGroups(world, playerName);

                for (String g : groupList)
                {
                    groups.add(g);
                }
            }
        }
        else if (handler != null)
        {
            @SuppressWarnings("deprecation") String group = handler.getGroup(worldName, playerName);

            if (group != null)
            {
                groups.add(group.toLowerCase());
            }
        }

        return groups;
    }


    /**
     * Gives the player permissions linked to a clan
     *
     * @param cp
     */
    public void addGroup(Player player, String group)
    {
        if (permission != null)
        {
            if (player != null)
            {
                permission.playerAddGroup(player, group);
            }
        }
    }

    /**
     * Removes permissions linked to a clan from the player
     *
     * @param cp
     */
    public void removeGroup(Player player, String group)
    {
        if (permission != null)
        {
            if (player != null)
            {
                permission.playerRemoveGroup(player, group);
            }
        }
    }

    /**
     * Whether exonomy plugin exists and is enabled
     *
     * @return
     */
    public boolean hasEconomy()
    {
        if (economy != null && economy.isEnabled())
        {
            return true;
        }
        return false;
    }

    /**
     * Charge player money
     *
     * @param player
     * @param amount
     * @return
     */
    public boolean playerCharge(Player player, double amount)
    {
        return economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
    }

    /**
     * Return money to player
     *
     * @param player
     * @param amount
     * @return
     */
    public boolean playerCredit(Player player, double amount)
    {
        return economy.depositPlayer(player.getName(), amount).transactionSuccess();
    }

    /**
     * Check whether player has money
     *
     * @param player
     * @param amount
     * @return
     */
    public static boolean hasMoney(Player player, double amount)
    {
        return economy.has(player.getName(), amount);
    }


    /**
     * If the user has rights in the area based on WorldGuard
     *
     * @param loc
     * @param player
     * @return
     */
    public boolean worldGuardCanBreak(Location loc, Player player)
    {
        if (wg != null)
        {
            return wg.canBuild(player, loc);
        }
        return true;
    }

    public boolean lwcProtected(Block block)
    {
        if(lwc == null)
        {
            return false;
        }

        Protection protection = lwc.findProtection(block);  // also accepts x, y, z

        if (protection != null)
        {
            return true;
        }
        return false;
    }
}
