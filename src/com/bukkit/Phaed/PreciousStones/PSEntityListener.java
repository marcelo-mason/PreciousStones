package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;


/**
 * PreciousStones entity listener
 * 
 * @author Phaed
 */
public class PSEntityListener  extends EntityListener
{
    //private final PreciousStones plugin;
    
    public PSEntityListener(PreciousStones plugin)
    {
	//this.plugin = plugin;
    }
    
    public void onEntityExplode(EntityExplodeEvent event)
    {
	for(Block block : event.blockList())
	{
	    if(block.getType() == Material.GOLD_BLOCK || block.getType() == Material.DIAMOND_BLOCK)
		event.setCancelled(true);
	}
    }
}
