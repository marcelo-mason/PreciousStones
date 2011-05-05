package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.List;
import java.util.logging.Level;
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

        int fields = plugin.ffm.cleanOrphans(world.getName());
        int ubs = plugin.um.cleanOrphans(world.getName());

        if (fields > 0)
        {
            PreciousStones.log(Level.INFO, "[{0}] ghost fields in {1} cleaned: {2}", plugin.getDescription().getName(), world.getName(), fields);
        }
        
        if (ubs > 0)
        {
            PreciousStones.log(Level.INFO, "[{0}] ghost unbreakables in {1} cleaned: {2}", plugin.getDescription().getName(), world.getName(), ubs);
        }
    }
}
