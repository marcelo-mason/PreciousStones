package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
    
    @Override
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
	    
	    HashMap<Vector, Block> sources = plugin.pm.getSourcePStone(block, null);
	    
	    for (Block source : sources.values())
	    {
		PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
		
		if (psettings != null && psettings.preventExplosions)
		{
		    event.setCancelled(true);
		    break;
		}
	    }
	}
    }
    
    @Override
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
	// prevent pvp
	
	if (event.getEntity() instanceof Player && event.getDamager() instanceof Player)
	{
	    Player player = (Player) event.getEntity();
	    Location loc = player.getLocation();
	    Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	    HashMap<Vector, Block> sources = plugin.pm.getSourcePStone(block, null);
	    
	    for (Block source : sources.values())
	    {
		PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
		
		if (psettings != null && psettings.preventPvP && !PreciousStones.Permissions.Security.permission(player, "preciousstones.bypass.pvp"))
		{
		    event.setCancelled(true);
		    
		    if (plugin.psettings.warnPvP)
			((Player) event.getDamager()).sendMessage(ChatColor.AQUA + "PvP disabled in this area");
		    
		    break;
		}
	    }
	}
    }
}
