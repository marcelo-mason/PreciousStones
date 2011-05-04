package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author cc_madelg
 */
public class SnitchManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public SnitchManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }

    /**
     *
     * @param player
     * @param field
     */
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
		    plugin.getDatabase().save(field);
		}
	    }
	}

    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchBlockBreak(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    List<Field> snitchFields = plugin.ffm.getSnitchFields(block);

	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Break", toBlockDetails(block));
		   plugin.getDatabase().save(field);
		}
	    }
	}
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchBlockPlace(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    List<Field> snitchFields = plugin.ffm.getSnitchFields(block);

	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Place", toBlockDetails(block));
		    plugin.getDatabase().save(field);
		}
	    }
	}
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchUsed(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    List<Field> snitchFields = plugin.ffm.getSnitchFields(block);

	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.GREEN + "Used", toBlockDetails(block));
		    plugin.getDatabase().save(field);
		}
	    }
	}
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchShop(Player player, Block block)
    {
	Sign sign = (Sign) block.getState();

	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    List<Field> snitchFields = plugin.ffm.getSnitchFields(block);

	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.GREEN + "Shopped", sign.getLines().length == 0 ? "empty" : sign.getLine(0));
		    plugin.getDatabase().save(field);
		}
	    }
	}
    }

    /**
     *
     * @param player
     * @param block
     */
    public void recordSnitchIgnite(Player player, Block block)
    {
	if (!plugin.pm.hasPermission(player, "preciousstones.bypass.snitch"))
	{
	    List<Field> snitchFields = plugin.ffm.getSnitchFields(block);

	    for (Field field : snitchFields)
	    {
		if (!field.isAllAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.RED + "Ignite", toBlockDetails(block));
		    plugin.getDatabase().save(field);
		}
	    }
	}
    }

    /**
     *
     * @param player
     * @param field
     */
    public void showIntruderList(Player player, Field field)
    {
	if (field.getOwner().equals(player.getName()) || plugin.pm.hasPermission(player, "preciousstones.admin.details"))
	{
	    if (field != null)
	    {
		List<SnitchEntry> snitches = field.getSnitchList();

		if (snitches.size() > 0)
		{
		    plugin.com.getCacheBlock().clear();

		    ChatBlock.sendBlank(player);
		    ChatBlock.saySingle(player, ChatColor.YELLOW + "Intruder log " + ChatColor.DARK_GRAY + "----------------------------------------------------------------------------------------");
		    ChatBlock.sendBlank(player);

		    plugin.com.getCacheBlock().addRow(new String[] { "  " + ChatColor.GRAY + "Name", "Reason", "Details" });

		    for (SnitchEntry se : snitches)
		    {
			plugin.com.getCacheBlock().addRow(new String[] { "  " + ChatColor.GOLD + se.getName(), se.getReasonDisplay(), ChatColor.WHITE + se.getDetails().replace("{", "[").replace("}", "]") });
		    }

		    boolean more = plugin.com.getCacheBlock().sendBlock(player, plugin.settings.linesPerPage);

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
     * @param block
     * @return
     */
    public static String toBlockDetails(Block block)
    {
	return Helper.friendlyBlockType(block.getType().toString()) + " {" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "}";
    }
}
