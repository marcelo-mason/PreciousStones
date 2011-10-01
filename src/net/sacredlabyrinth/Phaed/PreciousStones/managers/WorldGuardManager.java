package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardManager
{
    private PreciousStones plugin;
    private WorldGuardPlugin wg;

    /**
     *
     */
    public WorldGuardManager()
    {
        plugin = PreciousStones.getInstance();
        getWorldGuard();
    }

    private void getWorldGuard()
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

    public boolean canBuild(Player player, Block block)
    {
        if (wg == null)
        {
            return true;
        }

        Location loc = block.getLocation();

        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return wg.canBuild(player, new Location(w, x, y, z));
    }

    public boolean canBuildField(Player player, Block block, FieldSettings fs)
    {
        if (wg == null)
        {
            return true;
        }

        Location loc = block.getLocation();

        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = fs.getRadius();

        if (wg.canBuild(player, new Location(w, x + radius, y + radius, z + radius)))
        {
            if (wg.canBuild(player, new Location(w, x + radius, y + radius, z - radius)))
            {
                if (wg.canBuild(player, new Location(w, x + radius, y - radius, z + radius)))
                {
                    if (wg.canBuild(player, new Location(w, x + radius, y - radius, z - radius)))
                    {
                        if (wg.canBuild(player, new Location(w, x - radius, y + radius, z + radius)))
                        {
                            if (wg.canBuild(player, new Location(w, x - radius, y + radius, z - radius)))
                            {
                                if (wg.canBuild(player, new Location(w, x - radius, y - radius, z + radius)))
                                {
                                    if (wg.canBuild(player, new Location(w, x - radius, y - radius, z - radius)))
                                    {
                                        if (wg.canBuild(player, new Location(w, x, y, z)))
                                        {
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
