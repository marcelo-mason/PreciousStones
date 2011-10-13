package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class CuboidEntry
{
    private Field field;
    private Set<BlockData> selected = new HashSet<BlockData>();
    private int minx = 999999999;
    private int miny = 999999999;
    private int minz = 999999999;
    private int maxx = -999999999;
    private int maxy = -999999999;
    private int maxz = -999999999;

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
        selected.add(new BlockData(block));
        calculate();
    }

    public void removeSelected(Block block)
    {
        selected.remove(new BlockData(block));
        calculate();
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
        minx = 999999999;
        miny = 999999999;
        minz = 999999999;
        maxx = -999999999;
        maxy = -999999999;
        maxz = -999999999;

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
        int maxWidth = (field.getSettings().getRadius() * 2) + 1;
        int maxHeight = field.getSettings().getHeight() > 0 ? field.getSettings().getHeight() : maxWidth;

        return (maxHeight * maxWidth * maxWidth) * (field.getChildren().size() + 1);
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
