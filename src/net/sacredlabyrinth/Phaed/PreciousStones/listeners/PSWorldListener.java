package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
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
        DebugTimer dt = new DebugTimer("onWorldLoad");

        World world = event.getWorld();

        if (plugin.settings.isBlacklistedWorld(world))
        {
            return;
        }

        plugin.sm.loadWorldFields(world.getName());
        plugin.sm.loadWorldUnbreakables(world.getName());

        // remove in version 6
        plugin.tm.untagWorld(world.getName());

        if (plugin.settings.debug)
        {
           dt.logProcessTime();
        }
    }
}
