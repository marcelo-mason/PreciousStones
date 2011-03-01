package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;


/**
 * PreciousStones player listener
 * 
 * @author Phaed
 */
public class PSPlayerListener extends PlayerListener
{
    private final PreciousStones plugin;
    
    public PSPlayerListener(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    @Override
    public void onPlayerItem(PlayerItemEvent event)
    {
	Player player = event.getPlayer();
	Block block = event.getBlockClicked();
	
	if (block == null || player == null)
	{
	    return;
	}
	
	if (plugin.settings.isBypassBlock(block))
	{
	    return;
	}
	
	if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
	{
	    if (plugin.um.isOwner(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
	    {
		plugin.cm.showUnbreakableDetails(plugin.um.getUnbreakable(block), player);
	    }
	    else
	    {
		plugin.cm.showUnbreakableOwner(player, block);
	    }
	}
	else if (plugin.settings.isFieldType(block) && plugin.ffm.isField(block))
	{
	    if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
	    {
		plugin.cm.showFieldDetails(plugin.ffm.getField(block), player);
	    }
	    else
	    {
		plugin.cm.showFieldOwner(player, block);
	    }
	}
	else
	{
	    Field field = plugin.ffm.isDestroyProtected(block, null);
	    
	    if (field != null)
	    {
		if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails)
		{
		    LinkedList<Field> fields = plugin.ffm.getSourceFields(block);
		    
		    plugin.cm.showProtectedLocation(fields, player);
		}
		else
		{
		    plugin.cm.showProtected(player);
		}
	    }
	}
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
	Player player = event.getPlayer();
	
	// handle entries and exits from fields
	
	boolean insideField = false;
	
	LinkedList<Field> fields = plugin.ffm.getSourceFields(player);
	
	for (Field field : fields)
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    if (plugin.em.isInsideField(player))
	    {
		Field previousField = plugin.em.getEnvelopingField(player);
		
		if (previousField.getOwner().equals(field.getOwner()))
		{
		    insideField = true;
		}
		else
		{
		    plugin.em.leave(player);
		}
		
		continue;
	    }
	    
	    plugin.em.enter(player, field);
	    
	    if (fieldsettings.welcomeMessage)
	    {
		plugin.cm.showWelcomeMessage(player, field.getName());
	    }
	    
	    insideField = true;
	    break;
	}
	
	if (!insideField && plugin.em.isInsideField(player))
	{
	    Field field = plugin.em.getEnvelopingField(player);
	    
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    if (fieldsettings.farewellMessage)
	    {
		plugin.cm.showFarewellMessage(player, field.getName());
	    }
	    
	    plugin.em.leave(player);
	}
	
	// check if were on a prevent entry field, only those not owned by player
	
	fields = plugin.ffm.getSourceFields(player, player.getName());
	
	for (Field field : fields)
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
	    {
		if (fieldsettings.preventEntry)
		{
		    player.teleportTo(event.getFrom());
		    event.setCancelled(true);
		    plugin.cm.warnEntry(player, field);
		    break;
		}
	    }
	}
    }
}
