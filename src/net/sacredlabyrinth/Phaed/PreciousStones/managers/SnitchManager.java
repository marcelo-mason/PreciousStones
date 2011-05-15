package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
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
     * @param se
     */
    public void deleteSnitchEntry(SnitchEntry se)
    {
        try
        {
            plugin.getDatabase().delete(SnitchEntry.class, se.getId());
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error deleting snitchEntry: {0}", ex.getMessage());
        }
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
		if (!field.isAllowed(player.getName()))
		{
		    DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a z");
		    field.addIntruder(player.getName(), ChatColor.BLUE + "Entry", dateFormat.format(new Date()));
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
		if (!field.isAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Break", toBlockDetails(block));
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
		if (!field.isAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.DARK_RED + "Block Place", toBlockDetails(block));
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
		if (!field.isAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.GREEN + "Used", toBlockDetails(block));
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
		if (!field.isAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.GREEN + "Shopped", sign.getLines().length == 0 ? "empty" : sign.getLine(0));
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
		if (!field.isAllowed(player.getName()))
		{
		    field.addIntruder(player.getName(), ChatColor.RED + "Ignite", toBlockDetails(block));
		}
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
	return Helper.friendlyBlockType(block.getType().toString()) + " [" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
    }
}
