package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public final class StorageManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public StorageManager(PreciousStones plugin)
    {
        this.plugin = plugin;
        loadWorldData();
        startScheduler();
    }

    /**
     *
     */
    public void loadWorldData()
    {
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds)
        {
            plugin.ffm.loadWorld(world.getName());
            plugin.um.loadWorld(world.getName());
        }
    }

    /**
     *
     */
    public void startScheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.um.saveAll();
                        plugin.ffm.saveAll();

                        PreciousStones.log(Level.INFO, "data saved.");
                    }
                }, 0, 20L * 60 * plugin.settings.saveFrequency);
            }
        }, 20L * 60 * plugin.settings.saveFrequency);
    }
}
