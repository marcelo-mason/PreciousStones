package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class SnitchManager
{
    private PreciousStones plugin;
    private ChatBlock cacheblock = new ChatBlock();
    
    public SnitchManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public ChatBlock getCacheBlock()
    {
	return cacheblock;
    }
    
    public void recordSnitchEntry(Player player, Field field)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
	    
	    if (fieldsettings.snitch)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a z");
		    field.addIntruder(player.getName(), ChatColor.BLUE + "Entry", dateFormat.format(new Date()));
		    plugin.ffm.setDirty();
		}
	    }
	}
	
    }
    
    public void recordSnitchBlockBreak(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    LinkedList<Field> snitchFields = plugin.ffm.getSnitchFields(block);
	    
	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Break", toBlockDetails(block));
		    plugin.ffm.setDirty();
		}
	    }
	}
    }
    
    public void recordSnitchBlockPlace(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    LinkedList<Field> snitchFields = plugin.ffm.getSnitchFields(block);
	    
	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Place", toBlockDetails(block));
		    plugin.ffm.setDirty();
		}
	    }
	}
    }
    
    public void recordSnitchUsed(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    LinkedList<Field> snitchFields = plugin.ffm.getSnitchFields(block);
	    
	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.GREEN + "Used", Helper.friendlyBlockType(block.getType().toString()));
		    plugin.ffm.setDirty();
		}
	    }
	}
    }
    
    public void showIntruderList(Player player, Field field)
    {
	if (field.getOwner().equals(player.getName()) || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
	{	    
	    if (field != null)
	    {
		ArrayList<SnitchEntry> snitches = field.getSnitchList();
		
		if (snitches.size() > 0)
		{
		    cacheblock = new ChatBlock();
		    
		    ChatBlock.sendBlank(player);
		    ChatBlock.saySingle(player, ChatColor.YELLOW + "Intruder log " + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");
		    ChatBlock.sendBlank(player);
		    
		    cacheblock.addRow(new String[] { "  " + ChatColor.GRAY + "Name", "Reason", "Details" });
		    
		    for (SnitchEntry se : snitches)
		    {
			cacheblock.addRow(new String[] { "  " + ChatColor.GOLD + se.getName(), se.getReason(), ChatColor.WHITE + se.getDetails().replace("{", "[").replace("}", "]") });
		    }
		    
		    boolean more = cacheblock.sendBlock(player, 12);
		    
		    if (more)
		    {
			ChatBlock.sendBlank(player);
			ChatBlock.sendMessage(player, ChatColor.GOLD + "Type " + ChatColor.WHITE + "/ps more " + ChatColor.GOLD + "to view next page.");
		    }
		    
		    ChatBlock.sendBlank(player);
		}
		else
		{
		    ChatBlock.sendMessage(player, ChatColor.RED + "There have been no intruders around here");
		}
		
		return;
	    }
	    else
	    {
		plugin.cm.showNotFound(player);
	    }
	}
    }
    
    /**
     * Returns formatted coordinates
     */
    public static String toBlockDetails(Block block)
    {
	return block.getType() + " {" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "}";
    }
}
