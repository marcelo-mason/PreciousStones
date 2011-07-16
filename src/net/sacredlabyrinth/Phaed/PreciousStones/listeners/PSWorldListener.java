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

        plugin.sm.loadWorldFields(world.getName());
        plugin.sm.loadWorldUnbreakables(world.getName());

        if (plugin.settings.debug)
        {
           dt.logProcessTime();
        }
    }
}
