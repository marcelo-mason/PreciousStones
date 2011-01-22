package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

import com.bukkit.Phaed.PreciousStones.PSettings.PStone;

/**
 * PreciousStones entity listener
 * 
 * @author Phaed
 */
public class PSEntityListener extends EntityListener
{
    private final PreciousStones plugin;
    
    public PSEntityListener(PreciousStones plugin)
    {
	 this.plugin = plugin;
    }
    
    public void onEntityExplode(EntityExplodeEvent event)
    {
	for (Block block : event.blockList())
	{
	    // if one of the blocks is an unbreakable one, undo explosion 
	    
	    if (plugin.um.isType(block))
		event.setCancelled(true);

	    // if one of the blocks is a protected one, undo explosion 
	    
	    if (plugin.pm.isPStoneType(block))
		event.setCancelled(true);

	    // if inside a protected area and it prevents explosions, undo explosion
	    
	    Block source = plugin.pm.getProtectedAreaSource(block, null);
	    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
	    
	    if (psettings != null && psettings.preventExplosions)
		event.setCancelled(true);
	}
    }
}
