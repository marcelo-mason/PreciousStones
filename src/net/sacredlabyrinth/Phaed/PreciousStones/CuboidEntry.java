package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.LinkedList;
import java.util.List;

public class CuboidEntry
{
    private Field field;
    private List<BlockData> selected = new LinkedList<BlockData>();
    private int minx;
    private int miny;
    private int minz;
    private int maxx;
    private int maxy;
    private int maxz;

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
            selected.add(new BlockData(block));
            calculate();
        }
    }

    /**
     * Removes the selected block from the lsit
     *
     * @param block
     */
    public void removeSelected(Block block)
    {
        selected.remove(new BlockData(block));
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
    public BlockData getLastSelected()
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
        return selected.contains(new BlockData(block));
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

        for (BlockData bd : selected)
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
        int testVolume = (maxy - miny) * (maxx - minx) * (maxz - minz);

        return testVolume > getMaxVolume();
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
