package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import org.bukkit.event.world.*;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

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
    public void onWorldSave(WorldSaveEvent event)
    {
	plugin.sm.save();
    }
}
