package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import br.net.fabiozumbi12.RedProtect.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RedProtectManager {
    private PreciousStones plugin;
    private static boolean hasRedProtect;

    /**
     *
     */
    public RedProtectManager() {
        plugin = PreciousStones.getInstance();
        hasRedProtect = checkRedProtect();
    }

    private boolean checkRedProtect() {
        Plugin pRP = Bukkit.getPluginManager().getPlugin("RedProtect");
        if (pRP != null && pRP.isEnabled()){
            return true;
        }
        return false;
    }

    public boolean isRegion(Block block) {
        try {
            if (!hasRedProtect) {
                return false;
            }

            Region region = RedProtectAPI.getRegion(block.getLocation());

            return region != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean canBuild(Player player, Location loc) {
        try {
            if (!hasRedProtect) {
                return true;
            }

            // if null passed then pick up some random player

            if (player == null) {
                player = plugin.getServer().getWorlds().get(0).getPlayers().get(0);
            }

            if (player == null) {
                return false;
            }

            Region region = RedProtectAPI.getRegion(loc);

            if (region == null) {
                return true;
            }

            return region.canBuild(player);
        } catch (Exception ex) {
            return true;
        }
    }

    public boolean canBuildField(Player player, Block block, FieldSettings fs) {
        if (!hasRedProtect) {
            return true;
        }

        Location loc = block.getLocation();

        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = fs.getRadius();

        if (canBuild(player, new Location(w, x + radius, y + radius, z + radius))) {
            if (canBuild(player, new Location(w, x + radius, y + radius, z - radius))) {
                if (canBuild(player, new Location(w, x + radius, y - radius, z + radius))) {
                    if (canBuild(player, new Location(w, x + radius, y - radius, z - radius))) {
                        if (canBuild(player, new Location(w, x - radius, y + radius, z + radius))) {
                            if (canBuild(player, new Location(w, x - radius, y + radius, z - radius))) {
                                if (canBuild(player, new Location(w, x - radius, y - radius, z + radius))) {
                                    if (canBuild(player, new Location(w, x - radius, y - radius, z - radius))) {
                                        if (canBuild(player, new Location(w, x, y, z))) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
