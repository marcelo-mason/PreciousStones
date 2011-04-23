package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.LinkedList;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
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
	    if (plugin.settings.isUnbreakableType(block))
		event.setCancelled(true);
	    
	    if (plugin.settings.isFieldType(block) || plugin.settings.isCloakableType(block))
		event.setCancelled(true);
	    
	    LinkedList<Field> fields = plugin.ffm.getSourceFields(block);
	    
	    for (Field field : fields)
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.preventExplosions)
		{
		    if (fieldsettings.guarddogMode && plugin.ffm.allowedAreOnline(field))
		    {
			plugin.cm.notifyGuardDog(null, field, "creeper explosion");
			continue;
		    }
		    
		    event.setCancelled(true);
		    break;
		}
	    }
	}
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
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
	
	if (event instanceof EntityDamageByEntityEvent)
	{
	    EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
	    
	    if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
	    {
		Player attacker = (Player) sub.getDamager();
		Player victim = (Player) sub.getEntity();
		
		LinkedList<Field> fields = plugin.ffm.getSourceFields(victim);
		
		for (Field field : fields)
		{
		    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		    
		    if (fieldsettings.preventPvP)
		    {
			if (fieldsettings.guarddogMode && plugin.ffm.allowedAreOnline(field))
			{
			    plugin.cm.notifyGuardDog(attacker, field, "pvp");
			    continue;
			}
			
			if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
			{
			    plugin.cm.warnBypassPvP(attacker, victim, field);
			}
			else
			{
			    sub.setCancelled(true);
			    plugin.cm.warnPvP(attacker, victim, field);
			}
			break;
		    }
		}
	    }
	}
	
	if (event instanceof EntityDamageByProjectileEvent)
	{
	    EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent) event;
	    
	    if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
	    {
		Player attacker = (Player) sub.getDamager();
		Player victim = (Player) sub.getEntity();
		
		LinkedList<Field> fields = plugin.ffm.getSourceFields(victim);
		
		for (Field field : fields)
		{
		    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		    
		    if (fieldsettings.preventPvP)
		    {
			if (fieldsettings.guarddogMode && plugin.ffm.allowedAreOnline(field))
			{
			    plugin.cm.notifyGuardDog(attacker, field, "pvp");
			    continue;
			}
			
			if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
			{
			    plugin.cm.warnBypassPvP(attacker, victim, field);
			}
			else
			{
			    sub.setCancelled(true);
			    plugin.cm.warnPvP(attacker, victim, field);
			}
			break;
		    }
		}
	    }
	}
	
	if (event.getCause().equals(DamageCause.ENTITY_ATTACK))
	{
	    if (event.getEntity() instanceof Player)
	    {
		Player player = (Player) event.getEntity();
		
		LinkedList<Field> fields = plugin.ffm.getSourceFields(player);
		
		for (Field field : fields)
		{
		    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		    
		    if (fieldsettings.preventPvP)
		    {
			if (fieldsettings.guarddogMode && plugin.ffm.allowedAreOnline(field))
			{
			    continue;
			}
			
			event.setCancelled(true);
			break;
		    }
		}
	    }
	}
    }
}
