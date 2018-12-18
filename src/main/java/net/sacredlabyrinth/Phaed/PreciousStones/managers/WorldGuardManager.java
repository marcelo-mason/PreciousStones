package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

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

    public boolean isRegion(Block block) {
        try {
            if (wg == null) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(adapt(block.getWorld()));
            BlockVector3 location = asVector(block.getLocation());
            ApplicableRegionSet regions = manager.getApplicableRegions(location);
            return regions.size() > 0;
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

            if (player == null) {
                return false;
            }

            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testBuild(adapt(loc), wg.wrapPlayer(player));
        } catch (Exception ex) {
            return true;
        }
    }

    public static com.sk89q.worldedit.world.World adapt(World world) {
        return BukkitAdapter.adapt(world);
    }

    public static BlockVector3 asVector(org.bukkit.Location location) {
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    public static com.sk89q.worldedit.util.Location adapt(org.bukkit.Location location) {
        return BukkitAdapter.adapt(location);
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
