package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardManager {
    private PreciousStones plugin;
    private WorldGuardPlugin wg;

    /**
     *
     */
    public WorldGuardManager() {
        plugin = PreciousStones.getInstance();
        getWorldGuard();
    }

    private void getWorldGuard() {
        if (wg == null) {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

            if (test != null) {
                this.wg = (WorldGuardPlugin) test;
            }
        }
    }

    public boolean isWGRegion(Block block) {
        try {
            if (wg == null) {
                return false;
            }

            RegionManager manager = wg.getRegionManager(block.getWorld());

            ApplicableRegionSet regions = manager.getApplicableRegions(block.getLocation());

            return regions != null && regions.size() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean canBuild(Player player, Location loc) {
        try {
            if (wg == null) {
                return true;
            }

            // if null passed then pick up some random player

            if (player == null) {
                player = plugin.getServer().getWorlds().get(0).getPlayers().get(0);
            }

            return wg.canBuild(player, loc);
        } catch (Exception ex) {
            return true;
        }
    }

    public boolean canBuildField(Player player, Block block, FieldSettings fs) {
        if (wg == null) {
            return true;
        }

        Location loc = block.getLocation();

        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = fs.getRadius();

        if (wg.canBuild(player, new Location(w, x + radius, y + radius, z + radius))) {
            if (wg.canBuild(player, new Location(w, x + radius, y + radius, z - radius))) {
                if (wg.canBuild(player, new Location(w, x + radius, y - radius, z + radius))) {
                    if (wg.canBuild(player, new Location(w, x + radius, y - radius, z - radius))) {
                        if (wg.canBuild(player, new Location(w, x - radius, y + radius, z + radius))) {
                            if (wg.canBuild(player, new Location(w, x - radius, y + radius, z - radius))) {
                                if (wg.canBuild(player, new Location(w, x - radius, y - radius, z + radius))) {
                                    if (wg.canBuild(player, new Location(w, x - radius, y - radius, z - radius))) {
                                        if (wg.canBuild(player, new Location(w, x, y, z))) {
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
