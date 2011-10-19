package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.World;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.List;

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
     * @param event
     */
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        World world = event.getWorld();

        if (plugin.getSettingsManager().isBlacklistedWorld(world))
        {
            return;
        }

        List<Field> fields = plugin.getForceFieldManager().getSourceFields(new ChunkVec(event.getChunk()), FieldFlag.KEEP_CHUNKS_LOADED);

        if (!fields.isEmpty())
        {
            event.setCancelled(true);
        }
    }

    /**
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

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }
}
