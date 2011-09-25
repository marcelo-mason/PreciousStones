package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
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
            return false;
        }
        return wg.canBuild(player, block.getLocation());
    }
}
