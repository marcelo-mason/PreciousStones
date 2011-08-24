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
     */
    public PSWorldListener()
    {
        plugin = PreciousStones.getInstance();
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

        if (plugin.getSettingsManager().isBlacklistedWorld(world))
        {
            return;
        }

        plugin.getStorageManager().loadWorldFields(world.getName());
        plugin.getStorageManager().loadWorldUnbreakables(world.getName());

        // remove in version 6
        plugin.getTagManager().untagWorld(world.getName());

        if (plugin.getSettingsManager().isDebug())
        {
           dt.logProcessTime();
        }
    }
}
