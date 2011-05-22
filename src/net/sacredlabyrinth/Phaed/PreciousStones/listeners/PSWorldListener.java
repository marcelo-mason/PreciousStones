package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import org.bukkit.event.world.*;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.World;

/**
 * PreciousStones world listener
 *
 * @author Phaed
 */
public class PSWorldListener extends WorldListener
{
    private final PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public PSWorldListener(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param event
     */
    @Override
    public void onWorldLoad(WorldLoadEvent event)
    {
        World world = event.getWorld();

        plugin.ffm.loadWorld(world.getName());
        plugin.um.loadWorld(world.getName());
        plugin.tm.tagWorld(world.getName());
    }
}
