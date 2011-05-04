package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

/**
 * Handles unprotectable blocks
 *
 * @author Phaed
 */
public class UnprotectableManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public UnprotectableManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }

    /**
     * If the block is touching an unprotectable block
     * @param block
     * @return
     */
    public boolean touchingUnprotectableBlock(Block block)
    {
	return getTouchingUnprotectableBlock(block) != null;
    }

    /**
     * If the block is touching an unprotectable block
     * @param block
     * @return
     */
    public Block getTouchingUnprotectableBlock(Block block)
    {
	for (int x = -1; x <= 1; x++)
	{
	    for (int z = -1; z <= 1; z++)
	    {
		for (int y = -1; y <= 1; y++)
		{
		    if (x == 0 && y == 0 && z == 0)
			continue;

		    Block touchingblock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);

		    if (plugin.settings.isUnprotectableType(touchingblock))
			return touchingblock;
		}
	    }
	}

	return null;
    }

    /**
     * If an unprotectable block exists inside the field
     * @param fieldblock
     * @return
     */
    public Block existsUnprotectableBlock(Block fieldblock)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldblock.getTypeId());

	int minx = fieldblock.getX() - fieldsettings.radius;
	int maxx = fieldblock.getX() + fieldsettings.radius;
	int minz = fieldblock.getZ() - fieldsettings.radius;
	int maxz = fieldblock.getZ() + fieldsettings.radius;

	int miny = fieldblock.getY() - (int) Math.floor(((double) fieldsettings.getHeight()) / 2);
	int maxy = fieldblock.getY() + (int) Math.ceil(((double) fieldsettings.getHeight()) / 2);

	int halfminy = fieldblock.getY() - ((int) Math.floor(((double) fieldsettings.getHeight()) / 2) / 2);
	int halfmaxy = fieldblock.getY() + ((int) Math.ceil(((double) fieldsettings.getHeight()) / 2) / 2);

	// check the y plane the block is placed on first

	for (int x = minx; x <= maxx; x++)
	{
	    for (int z = minz; z <= maxz; z++)
	    {
		if (x == fieldblock.getX() && z == fieldblock.getZ())
		    continue;

		Block found = fieldblock.getWorld().getBlockAt(x, fieldblock.getY(), z);

		if (plugin.settings.isUnprotectableType(found))
		    return found;
	    }
	}

	// check the middle half of the y axis second

	for (int x = minx; x <= maxx; x++)
	{
	    for (int z = minz; z <= maxz; z++)
	    {
		for (int y = halfminy; y <= halfmaxy; y++)
		{
		    if (y == fieldblock.getY())
			continue;

		    Block found = fieldblock.getWorld().getBlockAt(x, y, z);

		    if (plugin.settings.isUnprotectableType(found))
			return found;
		}
	    }
	}

	// check the top and bottom half of the y axis last

	for (int x = minx; x <= maxx; x++)
	{
	    for (int z = minz; z <= maxz; z++)
	    {
		for (int y = halfmaxy + 1; y <= maxy; y++)
		{
		    Block found = fieldblock.getWorld().getBlockAt(x, y, z);

		    if (plugin.settings.isUnprotectableType(found))
			return found;
		}
		for (int y = halfminy - 1; y <= miny; y++)
		{
		    Block found = fieldblock.getWorld().getBlockAt(x, y, z);

		    if (plugin.settings.isUnprotectableType(found))
			return found;
		}
	    }
	}

	return null;
    }
}
