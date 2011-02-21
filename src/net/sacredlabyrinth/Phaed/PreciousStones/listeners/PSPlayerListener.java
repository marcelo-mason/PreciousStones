package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
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
	    if (plugin.um.isOwner(block, player.getName()) || plugin.settings.publicBlockDetails || PreciousStones.Permissions.has(player, "preciousstones.admin.details"))
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
	    if (plugin.ffm.isAllowed(block, player.getName()) || plugin.settings.publicBlockDetails || PreciousStones.Permissions.has(player, "preciousstones.admin.details"))
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
	    
	    if (!PreciousStones.Permissions.has(player, "preciousstones.bypass.entry"))
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
    
    @Override
    public void onPlayerCommand(PlayerChatEvent event)
    {
	String[] split = event.getMessage().split(" ");
	Player player = event.getPlayer();
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	
	if (split[0].equalsIgnoreCase("/ps"))
	{
	    event.setCancelled(true);
	    
	    if (split.length > 1)
	    {
		if (split[1].equals("allow") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.allow"))
		{
		    if (split.length == 3)
		    {
			Field field = plugin.ffm.inAllowedVector(block, player.getName());
			
			if (field != null)
			{
			    String playerName = split[2];
			    
			    int count = plugin.ffm.addAllowed(player, field, playerName);
			    
			    if (count > 0)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " was added to the allowed list of " + ChatBlock.numberToWord(count) + " force-fields");
			    }
			    else
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " is already on the list");
			    }
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
			
			return;
		    }
		}
		else if (split[1].equals("remove") && PreciousStones.Permissions.has(player, "preciousstones.whitelist.remove"))
		{
		    if (split.length == 3)
		    {
			Field field = plugin.ffm.inAllowedVector(block, player.getName());
			
			if (field != null)
			{
			    String playerName = split[2];
			    
			    int count = plugin.ffm.removeAllowed(player, field, playerName);
			    
			    if (count > 0)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + playerName + " was removed from the allowed list of " + ChatBlock.numberToWord(count) + " force-fields");
			    }
			    else
			    {
				ChatBlock.sendMessage(player, ChatColor.RED + playerName + " not found or is the last player on the list");
			    }
			}
			else
			{
			    plugin.cm.showNotFound(player);
			}
			
			return;
		    }
		}
		else if (split[1].equals("setname") && PreciousStones.Permissions.has(player, "preciousstones.benefit.setname"))
		{
		    if (split.length >= 2)
		    {
			String playerName = "";
			
			for (int i = 2; i < split.length; i++)
			{
			    playerName += split[i] + " ";
			}
			playerName = playerName.trim();
			
			if (playerName.length() > 0)
			{
			    Field field = plugin.ffm.inAllowedVector(block, player.getName());
			    
			    if (field != null)
			    {
				int count = plugin.ffm.setNameFields(player, field, playerName);
				
				if (count > 0)
				{
				    ChatBlock.sendMessage(player, ChatColor.AQUA + "" + ChatBlock.numberToWord(count) + " fields renamed to " + playerName);
				}
				else
				{
				    plugin.cm.showNotFound(player);
				}
				return;
			    }
			    else
			    {
				plugin.cm.showNotFound(player);
			    }
			    return;
			}
		    }
		}
		else if (split[1].equals("delete") && PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
		{
		    if (split.length == 3)
		    {
			if (Helper.isInteger(split[2]))
			{
			    LinkedList<Field> fields = plugin.ffm.getFieldsOfType(Integer.parseInt(split[2]), player.getWorld());
			    
			    for (Field field : fields)
			    {
				plugin.ffm.release(field);
			    }
			    
			    ChatBlock.sendMessage(player, ChatColor.AQUA + "" + fields.size() + " protective fields removed from pstones of type " + split[2]);
			    
			    if (plugin.settings.logBypassDelete)
			    {
				PreciousStones.log.info("PreciousStones: " + fields.size() + " protective field removed from pstones of type " + split[2] + " by " + player.getName());
			    }
			    
			    return;
			}
		    }
		}
		else if (split[1].equals("setowner") && PreciousStones.Permissions.has(player, "preciousstones.admin.setowner"))
		{
		    if (split.length == 3)
		    {
			String owner = split[2];
			
			List<String> ignore = Arrays.asList(new String[] { "0", "6", "8", "9", "37", "38", "39", "40", "50", "51", "55", "59", "63", "68", "69", "70", "72", "75", "76", "83", "85" });
			
			TargetBlock tb = new TargetBlock(player, 100, 0.2, (ArrayList<String>) ignore);
			
			if (tb != null)
			{
			    Block targetblock = tb.getTargetBlock();
			    
			    if (targetblock != null)
			    {
				if (plugin.settings.isUnbreakableType(targetblock))
				{
				    Unbreakable unbreakable = plugin.um.getUnbreakable(targetblock);
				    
				    if (unbreakable != null)
				    {
					unbreakable.setOwner(owner);
					ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner set to " + owner);
					return;
				    }
				}
				
				if (plugin.settings.isFieldType(targetblock))
				{
				    Field field = plugin.ffm.getField(targetblock);
				    
				    if (field != null)
				    {
					field.setOwner(owner);
					ChatBlock.sendMessage(player, ChatColor.AQUA + "Owner set to " + owner);
					return;
				    }
				}
			    }
			}
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "You are not pointing at a force-field or unbreakable block");
			return;
		    }
		}
		else if (split[1].equals("list") && PreciousStones.Permissions.has(player, "preciousstones.admin.list"))
		{
		    if (split.length == 3)
		    {
			if (Helper.isInteger(split[2]))
			{
			    LinkedList<Unbreakable> unbreakables = plugin.um.getUnbreakablesInArea(player, Integer.parseInt(split[2]));
			    LinkedList<Field> fields = plugin.ffm.getFieldsInArea(player, Integer.parseInt(split[2]));
			    
			    for (Unbreakable u : unbreakables)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + u.toString());
			    }
			    
			    for (Field f : fields)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + f.toString());
			    }
			    
			    if (unbreakables.size() == 0 && fields.size() == 0)
			    {
				ChatBlock.sendMessage(player, ChatColor.AQUA + "No force-field or unbreakable blocks found");
			    }
			    return;
			}
		    }
		}
		else if (split[1].equals("delete") && PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
		{
		    if (split.length == 2)
		    {
			LinkedList<Field> fields = plugin.ffm.getSourceFields(block);
			
			for (Field field : fields)
			{
			    plugin.ffm.release(field);
			    
			    ChatBlock.sendMessage(player, ChatColor.AQUA + "Protective field removed from: " + field.toString());
			    
			    if (plugin.settings.logBypassDelete)
			    {
				PreciousStones.log.info("PreciousStones: Protective field removed from " + field.toString() + " by " + player.getName());
			    }
			}
			
			if (fields.size() == 0)
			{
			    plugin.cm.showNotFound(player);
			}
			return;
		    }
		}
		else if (split[1].equals("info") && PreciousStones.Permissions.has(player, "preciousstones.admin.info"))
		{
		    if (split.length == 2)
		    {
			LinkedList<Field> fields = plugin.ffm.getSourceFields(block);
			
			for (Field field : fields)
			{
			    plugin.cm.showFieldDetails(field, player);
			}
			
			if (fields.size() == 0)
			{
			    plugin.cm.showNotFound(player);
			}
			return;
		    }
		}
		else if (split[1].equals("reload") && PreciousStones.Permissions.has(player, "preciousstones.admin.reload"))
		{
		    if (split.length == 2)
		    {
			plugin.loadConfiguration();
			
			ChatBlock.sendMessage(player, ChatColor.AQUA + "Configuration and pstone files reloaded");
			return;
		    }
		}
	    }
	    
	    ChatBlock.sendBlank(player);
	    ChatBlock.sendMessage(player, ChatColor.YELLOW + plugin.getDesc().getName() + " " + plugin.getDesc().getVersion());
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps allow [player] " + ChatColor.DARK_GRAY + "- Add player to the allowed lists");
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps remove [player] " + ChatColor.DARK_GRAY + "- Remove player from the allowed lists");
	    ChatBlock.sendMessage(player, ChatColor.AQUA + "/ps setname [name] " + ChatColor.DARK_GRAY + "- Set the name of force-fields");
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.delete"))
	    {
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps delete " + ChatColor.DARK_GRAY + "- Delete the field(s) you're standing on");
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps delete [blockid] " + ChatColor.DARK_GRAY + "- Delete the field(s) from this type");
	    }
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.info"))
	    {
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps info " + ChatColor.DARK_GRAY + "- Get info for the field youre standing on");
	    }
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.list"))
	    {
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps list [chunks-in-radius]" + ChatColor.DARK_GRAY + "- Lists all pstones in area");
	    }
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.setowner"))
	    {
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps setowner [player] " + ChatColor.DARK_GRAY + "- Of the block you're pointing at");
	    }
	    
	    if (PreciousStones.Permissions.has(player, "preciousstones.admin.reload"))
	    {
		ChatBlock.sendMessage(player, ChatColor.DARK_RED + "/ps reload" + ChatColor.DARK_GRAY + "- Reload configuraton and save files");
	    }
	}
    }
}
