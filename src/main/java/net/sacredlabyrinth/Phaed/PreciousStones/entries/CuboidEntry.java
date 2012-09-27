package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CuboidEntry
{
    private Field field;
    private List<BlockEntry> selected = new ArrayList<BlockEntry>();
    private int minx;
    private int miny;
    private int minz;
    private int maxx;
    private int maxy;
    private int maxz;
    private Location expanded;

    public CuboidEntry(Field field)
    {
        this.field = field;
        minx = field.getX();
        miny = field.getY();
        minz = field.getZ();
        maxx = field.getX();
        maxy = field.getY();
        maxz = field.getZ();
    }

    /**
     * Add a single selected block
     *
     * @param block
     */
    public void addSelected(Block block)
    {
        if (!selected.contains(block))
        {
            selected.add(new BlockEntry(block));
            calculate();
        }
    }

    /**
     * Removes the selected block from the list
     *
     * @param block
     */
    public void removeSelected(Block block)
    {
        selected.remove(new BlockEntry(block));
        calculate();
    }

    /**
     * Reverts the last selected block
     */
    public void revertLastSelected()
    {
        if (selected.size() > 1)
        {
            selected.remove(selected.size() - 1);
        }
        calculate();
    }

    /**
     * Gets the latest reverted block
     */
    public BlockEntry getLastSelected()
    {
        if (selected.size() > 1)
        {
            return selected.get(selected.size() - 1);
        }

        return null;
    }

    /**
     * Check if a block has already been selected
     *
     * @param block
     * @return
     */
    public boolean isSelected(Block block)
    {
        return selected.contains(new BlockEntry(block));
    }

    /**
     * Return a count of selected blocks
     *
     * @return
     */
    public int selectedCount()
    {
        return selected.size();
    }

    private void calculate()
    {
        minx = field.getX();
        miny = field.getY();
        minz = field.getZ();
        maxx = field.getX();
        maxy = field.getY();
        maxz = field.getZ();

        for (BlockEntry bd : selected)
        {
            Location loc = bd.getLocation();

            if (loc.getBlockX() < minx)
            {
                minx = loc.getBlockX();
            }

            if (loc.getBlockY() < miny)
            {
                miny = loc.getBlockY();
            }

            if (loc.getBlockZ() < minz)
            {
                minz = loc.getBlockZ();
            }

            if (loc.getBlockX() > maxx)
            {
                maxx = loc.getBlockX();
            }

            if (loc.getBlockY() > maxy)
            {
                maxy = loc.getBlockY();
            }

            if (loc.getBlockZ() > maxz)
            {
                maxz = loc.getBlockZ();
            }
        }
    }

    /**
     * Get one block outside of the players facing direction
     *
     * @return
     */
    public Block getExpandedBlock(Player player)
    {
        calculate();

        Location loc = player.getLocation();

        if (!envelopsPlusOne(loc))
        {
            ChatBlock.send(player, "mustBeInCuboidToExpand");
            return null;
        }

        List<Block> lineOfSight = player.getLineOfSight(null, Math.max(Math.max(Math.max(maxx - miny, maxz - minz), maxy - miny), 256));

        for (Block block : lineOfSight)
        {
            if (!envelopsPlusOne(block.getLocation()))
            {
                if (expanded != null)
                {
                    player.sendBlockChange(expanded, 0, (byte) 0);
                }

                expanded = block.getLocation();

                return block;
            }
        }

        return null;
    }

    private boolean envelopsPlusOne(Location loc)
    {
        if (loc.getX() < maxx + 1 && loc.getX() > minx - 1 && loc.getY() > miny - 1 && loc.getY() < maxy + 1 && loc.getZ() > minz - 1 && loc.getZ() < maxz + 1)
        {
            return true;
        }

        return false;
    }

    /**
     * Write the final cuboid dimensions to the field
     */
    public void finalizeField()
    {
        field.setCuboidDimensions(minx, miny, minz, maxx, maxy, maxz);
    }

    /**
     * Test whether the new selected location meets the max valume size
     *
     * @param newLoc
     * @return
     */
    public boolean testOverflow(Location newLoc)
    {
        int minxt = minx;
        int minyt = miny;
        int minzt = minz;
        int maxxt = maxx;
        int maxyt = maxy;
        int maxzt = maxz;

        if (newLoc.getBlockX() < minxt)
        {
            minxt = newLoc.getBlockX();
        }

        if (newLoc.getBlockY() < minyt)
        {
            minyt = newLoc.getBlockY();
        }

        if (newLoc.getBlockZ() < minzt)
        {
            minzt = newLoc.getBlockZ();
        }

        if (newLoc.getBlockX() > maxxt)
        {
            maxxt = newLoc.getBlockX();
        }

        if (newLoc.getBlockY() > maxyt)
        {
            maxyt = newLoc.getBlockY();
        }

        if (newLoc.getBlockZ() > maxzt)
        {
            maxzt = newLoc.getBlockZ();
        }

        int testVolume = (maxyt - minyt) * (maxxt - minxt) * (maxzt - minzt);

        return testVolume <= getMaxVolume();
    }

    /**
     * Get the current volume of the definition
     *
     * @return
     */
    public int getVolume()
    {
        return (maxy - miny) * (maxx - minx) * (maxz - minz);
    }

    /**
     * The the maximum allowed volume for the cuboid
     *
     * @return
     */
    public int getMaxVolume()
    {
        int volume = field.getVolume();

        for (Field child : field.getChildren())
        {
            volume += child.getVolume();
        }

        return volume;
    }

    /**
     * The the available volume that is left
     *
     * @return
     */
    public int getAvailableVolume()
    {
        return getMaxVolume() - getVolume();
    }

    /**
     * Get the parent field
     *
     * @return
     */
    public Field getField()
    {
        return field;
    }

    /**
     * Whether the current definition has exceeded the volume
     *
     * @return
     */
    public boolean isExceeded()
    {
        return getVolume() > getMaxVolume();
    }

    public int getMinx()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return minx;
    }

    public int getMiny()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return miny;
    }

    public int getMinz()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return minz;
    }

    public int getMaxx()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return maxx;
    }

    public int getMaxy()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return maxy;
    }

    public int getMaxz()
    {
        if (selected.isEmpty())
        {
            return 0;
        }

        return maxz;
    }
}
