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
	
	LinkedList<Field> currentfields = plugin.ffm.getSourceFields(player);
	
	// loop through all fields the player just moved into
	
	if (currentfields != null)
	{
	    for (Field currentfield : currentfields)
	    {
		if (!plugin.em.isInsideField(player, currentfield))
		{
		    if (!plugin.em.containsSameNameAllowedField(player, currentfield))
		    {
			FieldSettings fieldsettings = plugin.settings.getFieldSettings(currentfield);
			
			if (fieldsettings.welcomeMessage)
			{
			    plugin.cm.showWelcomeMessage(player, currentfield.getName());
			}
		    }
		    
		    plugin.em.enterField(player, currentfield);
		}
	    }
	}
	
	// remove all stored entry fields that the player is no longer currently in
	
	LinkedList<Field> entryfields = plugin.em.getPlayerEntryFields(player);
	
	if (entryfields != null)
	{
	    if (currentfields != null)
	    {
		entryfields.removeAll(currentfields);
	    }
	    
	    for (Field entryfield : entryfields)
	    {
		plugin.em.leaveField(player, entryfield);
		
		if (!plugin.em.containsSameNameAllowedField(player, entryfield))
		{
		    FieldSettings fieldsettings = plugin.settings.getFieldSettings(entryfield);
		    
		    if (fieldsettings.farewellMessage)
		    {
			plugin.cm.showFarewellMessage(player, entryfield.getName());
		    }
		}
	    }
	}
	
	// check if were on a prevent entry field not owned by player
	
	LinkedList<Field> fields = plugin.ffm.getSourceFields(player, player.getName());
	
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
