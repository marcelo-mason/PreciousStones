package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

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
	    // prevent explosion if breaking unbreakable
	    
	    if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
	    {
		event.setCancelled(true);
		break;
	    }
	    
	    // prevent explosion if breaking field
	    
	    if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
	    {
		event.setCancelled(true);
		break;
	    }
	    
	    // prevent explosion if explosion protected
	    
	    Field field = plugin.ffm.isExplosionProtected(block);
	    
	    if (field != null)
	    {
		event.setCancelled(true);
		break;
	    }
	}
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
	// prevent fall damage after cannon throws
	
	if (event.getCause().equals(DamageCause.FALL))
	{
	    if (event.getEntity() instanceof Player)
	    {
		Player player = (Player) event.getEntity();
		
		if (plugin.vm.isFallDamageImmune(player))
		{
		    event.setCancelled(true);
		    plugin.cm.showThump(player);
		}
	    }
	}
	
	// pvp protect against player
	
	if (event instanceof EntityDamageByEntityEvent)
	{
	    EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
	    
	    if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
	    {
		Player attacker = (Player) sub.getDamager();
		Player victim = (Player) sub.getEntity();
		
		Field field = plugin.ffm.isPvPProtected(victim);
		
		if (field != null)
		{
		    if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
		    {
			plugin.cm.warnBypassPvP(attacker, victim, field);
		    }
		    else
		    {
			sub.setCancelled(true);
			plugin.cm.warnPvP(attacker, victim, field);
		    }
		}
		else
		{
		    field = plugin.ffm.isPvPProtected(attacker);
		    
		    if (field != null)
		    {
			if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
			{
			    plugin.cm.warnBypassPvP(attacker, victim, field);
			}
			else
			{
			    sub.setCancelled(true);
			    plugin.cm.warnPvP(attacker, victim, field);
			}
		    }
		}
	    }
	}
	
	// pvp protect against projectile
	
	if (event instanceof EntityDamageByProjectileEvent)
	{
	    EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent) event;
	    
	    if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
	    {
		Player attacker = (Player) sub.getDamager();
		Player victim = (Player) sub.getEntity();
		
		Field field = plugin.ffm.isPvPProtected(victim);
		
		if (field != null)
		{
		    if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
		    {
			plugin.cm.warnBypassPvP(attacker, victim, field);
		    }
		    else
		    {
			sub.setCancelled(true);
			plugin.cm.warnPvP(attacker, victim, field);
		    }
		}
		else
		{
		    field = plugin.ffm.isPvPProtected(attacker);
		    
		    if (field != null)
		    {
			if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
			{
			    plugin.cm.warnBypassPvP(attacker, victim, field);
			}
			else
			{
			    sub.setCancelled(true);
			    plugin.cm.warnPvP(attacker, victim, field);
			}
		    }
		}
	    }
	}
	
	// pvp protect against any other entity attack
	
	if (event.getCause().equals(DamageCause.ENTITY_ATTACK))
	{
	    if (event.getEntity() instanceof Player)
	    {
		Player player = (Player) event.getEntity();
		
		Field field = plugin.ffm.isPvPProtected(player);
		
		if (field != null)
		{
		    event.setCancelled(true);
		}
	    }
	}
    }
}
