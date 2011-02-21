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
    
    public PSWorldListener(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    @Override
    public void onWorldSaved(WorldEvent event)
    {
	plugin.sm.save();
    }
}
