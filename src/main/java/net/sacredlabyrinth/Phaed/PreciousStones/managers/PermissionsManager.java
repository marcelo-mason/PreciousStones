package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.gmail.nossr50.mcMMO;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import net.gravitydevelopment.anticheat.api.AntiCheatAPI;
import net.gravitydevelopment.anticheat.check.CheckType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.yi.acru.bukkit.Lockette.Lockette;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phaed
 */
public final class PermissionsManager {
    public static Permission permission = null;
    private static Economy economy = null;
    private PermissionHandler handler = null;
    private PermissionsPlugin pbukkit = null;
    private PermissionsEx pex = null;
    private LWC lwc = null;
    private Lockette lockette = null;
    private PreciousStones plugin;
    private mcMMO mcmmo = null;

    /**
     *
     */
    public PermissionsManager() {
        plugin = PreciousStones.getInstance();
        detectPermissionsBukkit();
        detectPermissionsEx();
        detectPermissions();
        detectLWC();
        detectLockette();
        detectMcMMO();

        try {
            Class.forName("net.milkbowl.vault.permission.Permission");

            setupEconomy();
            setupPermissions();
        } catch (ClassNotFoundException e) {
            //SimpleClans.log("[PreciousStones] Vault.jar not found. No economy support.");
            //no need to spam everyone who doesnt use vault
        }
    }

    private void detectMcMMO() {
        Plugin plug = plugin.getServer().getPluginManager().getPlugin("mcMMO");

        if (plug != null) {
            mcmmo = ((mcMMO) plug);
        }
    }

    private void detectLWC() {
        Plugin plug = plugin.getServer().getPluginManager().getPlugin("LWC");

        if (plug != null) {
            lwc = ((LWCPlugin) plug).getLWC();
        }
    }

    private void detectLockette() {
        Plugin plug = plugin.getServer().getPluginManager().getPlugin("Lockette");

        if (plug != null) {
            lockette = ((Lockette) plug);
        }
    }

    private void detectPermissions() {
        if (handler == null) {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");

            if (test != null) {
                handler = ((Permissions) test).getHandler();
            }
        }
    }

    private void detectPermissionsEx() {
        if (pex == null) {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");

            if (test != null) {
                pex = (PermissionsEx) test;
            }
        }
    }

    private void detectPermissionsBukkit() {
        if (pbukkit == null) {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");

            if (test != null) {
                pbukkit = ((PermissionsPlugin) test);
            }
        }
    }

    private Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public boolean canBuild(Player player, Location loc) {
        WorldGuardManager wm = PreciousStones.getInstance().getWorldGuardManager();
        RedProtectManager rm = PreciousStones.getInstance().getRedProtectManager();

        if (!wm.canBuild(player, loc)) {
            return false;
        }
        if (!rm.canBuild(player, loc)) {
            return false;
        }
        return true;
    }

    public boolean canBuildField(Player player, Block block, FieldSettings fs) {
        WorldGuardManager wm = PreciousStones.getInstance().getWorldGuardManager();
        RedProtectManager rm = PreciousStones.getInstance().getRedProtectManager();

        if (!wm.canBuildField(player, block, fs)) {
            return false;
        }
        if (!rm.canBuildField(player, block, fs)) {
            return false;
        }
        return true;
    }

    /**
     * Check whether a player has a permission
     *
     * @param player
     * @param perm
     * @return
     */
    public boolean has(Player player, String perm) {
        if (player == null) {
            return true;
        }

        if (!perm.contains("preciousstones.bypass.toggle")) {
            if (perm.contains("preciousstones.bypass.") || perm.contains("preciousstones.admin.allowed")) {
                PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player);

                if (entry.isBypassDisabled()) {
                    return false;
                }
            }
        }

        if (permission != null) {
            if (permission.has(player, "preciousstones.blacklist") && !permission.has(player, "preciousstones.admin.isadmin")) {
                return false;
            }

            return permission.has(player, perm);
        } else if (handler != null) {
            if (handler.has(player, "preciousstones.blacklist") && !handler.has(player, "preciousstones.admin.isadmin")) {
                return false;
            }

            return handler.has(player, perm);
        } else {
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
    public boolean inGroup(String playerName, World world, String group) {
        try {
            if (pex != null) {
                PermissionUser user = PermissionsEx.getUser(playerName);

                if (user != null) {
                    return user.inGroup(group);
                }

                return false;
            } else if (pbukkit != null) {
                List<Group> groups = pbukkit.getGroups(playerName);

                for (Group g : groups) {
                    if (g.getName().equalsIgnoreCase(group)) {
                        return true;
                    }
                }
                return false;
            } else if (permission != null) {
                return permission.playerInGroup(world, playerName, group);
            } else if (handler != null) {
                if (handler.getGroup(world.getName(), playerName).equalsIgnoreCase(group)) {
                    return true;
                }
                return false;
            }
        } catch (Exception ex) {
            // no group support
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
    public List<String> getGroups(String worldName, String playerName) {
        List<String> groups = new ArrayList<String>();

        try {
            if (pex != null) {
                PermissionUser user = PermissionsEx.getUser(playerName);

                if (user != null) {
                    String[] pexGroups = user.getGroupsNames();

                    for (String g : pexGroups) {
                        groups.add(g);
                    }
                }
            } else if (pbukkit != null) {
                List<Group> gs = pbukkit.getGroups(playerName);

                for (Group group : gs) {
                    groups.add(group.getName().toLowerCase());
                }
                return groups;
            } else if (permission != null) {
                World world = plugin.getServer().getWorld(worldName);

                if (world != null) {
                    String[] groupList = permission.getPlayerGroups(world, playerName);

                    for (String g : groupList) {
                        groups.add(g);
                    }
                }
            } else if (handler != null) {
                @SuppressWarnings("deprecation") String group = handler.getGroup(worldName, playerName);

                if (group != null) {
                    groups.add(group.toLowerCase());
                }
            }
        } catch (Exception ex) {
            // no group support
        }

        return groups;
    }


    /**
     * Gives the player permissions linked to a clan
     *
     * @param player
     * @param group
     */
    public void addGroup(Player player, String group) {
        /*if (pex != null)
        {
            PermissionUser user = PermissionsEx.getUser(player.getName());

            if (user != null)
            {
                user.addGroup(player.getName(), group);
            }
        }
        else*/

        if (permission != null) {
            if (player != null) {
                permission.playerAddGroup(player, group);
            }
        }
    }

    /**
     * Removes permissions linked to a clan from the player
     *
     * @param player
     * @param group
     */
    public void removeGroup(Player player, String group) {
        if (permission != null) {
            if (player != null) {
                permission.playerRemoveGroup(player, group);
            }
        }
    }

    /**
     * Whether exonomy plugin exists and is enabled
     *
     * @return
     */
    public boolean hasEconomy() {
        if (economy != null && economy.isEnabled()) {
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
    public boolean playerCharge(Player player, double amount) {
        return economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
    }

    /**
     * Charge player money
     *
     * @param playerName
     * @param amount
     * @return
     */
    public boolean playerCharge(String playerName, double amount) {
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    /**
     * Return money to player
     *
     * @param player
     * @param amount
     * @return
     */
    public boolean playerCredit(Player player, double amount) {
        return economy.depositPlayer(player.getName(), amount).transactionSuccess();
    }

    /**
     * Check whether player has money
     *
     * @param player
     * @param amount
     * @return
     */
    public static boolean hasMoney(Player player, double amount) {
        return economy.has(player.getName(), amount);
    }

    /**
     * Check whether player has money
     *
     * @param playerName
     * @param amount
     * @return
     */
    public static boolean hasMoney(String playerName, double amount) {
        return economy.has(playerName, amount);
    }

    public boolean lwcProtected(Player player, Block block) {
        if (lwc == null) {
            return false;
        }

        Protection protection = lwc.findProtection(block);

        if (protection != null) {
            return !lwc.canAccessProtection(player, block);
        }

        return false;
    }

    public boolean locketteProtected(Player player, Block block) {
        if (lockette == null) {
            return false;
        }

        if (Lockette.isProtected(block)) {
            String owner = Lockette.getProtectedOwner(block);
            return !owner.equalsIgnoreCase(player.getName());
        }

        return false;
    }

    public boolean hasMcMMO() {
        return mcmmo != null;
    }

    public boolean hasLWC() {
        return lwc != null;
    }

    public boolean isVanished(Player player) {
        List<MetadataValue> values = player.getMetadata("vanished");

        for (MetadataValue value : values) {
            if (value.asBoolean()) {
                return true;
            }
        }

        return false;
    }

    public void allowFly(Player player) {
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
            AntiCheatAPI.exemptPlayer(player, CheckType.FLY);
        }
    }

    public void resetFly(Player player) {
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
            AntiCheatAPI.unexemptPlayer(player, CheckType.FLY);
        }
    }

    public void allowFast(Player player) {
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
            AntiCheatAPI.exemptPlayer(player, CheckType.FAST_BREAK);
            AntiCheatAPI.exemptPlayer(player, CheckType.FAST_PLACE);
        }
    }

    public void resetFast(Player player) {
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
            AntiCheatAPI.unexemptPlayer(player, CheckType.FAST_BREAK);
            AntiCheatAPI.unexemptPlayer(player, CheckType.FAST_PLACE);
        }
    }
}
