package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CuboidEntry
{
    private Field field;
    private Set<BlockData> selected = new HashSet<BlockData>();
    private int minx = 1024;
    private int miny = 1024;
    private int minz = 1024;
    private int maxx = -1024;
    private int maxy = -1024;
    private int maxz = -1024;
    private int movedItem = -1;

    public CuboidEntry(Field field)
    {
        this.field = field;
    }

    public Set<BlockData> getSelected()
    {
        return Collections.unmodifiableSet(selected);
    }

    public void addSelected(Block block)
    {
        selected.add(new BlockData(block));
    }

    public boolean testOverflow(Location newLoc)
    {
        calculate();

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

        int testHeight = maxyt - minyt;
        int testWidth = Math.max(maxxt - minxt, maxzt - minzt);

        if (testHeight > getMaxHeight())
        {
            return false;
        }

        if (testWidth > getMaxWidth())
        {
            return false;
        }

        return true;
    }

    public void calculate()
    {
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

    public Field getField()
    {
        return field;
    }

    public int getMinx()
    {
        return minx;
    }

    public int getMiny()
    {
        return miny;
    }

    public int getMinz()
    {
        return minz;
    }

    public int getMaxx()
    {
        return maxx;
    }

    public int getMaxy()
    {
        return maxy;
    }

    public int getMaxz()
    {
        return maxz;
    }

    public int getMovedItem()
    {
        return movedItem;
    }

    public void setMovedItem(int movedItem)
    {
        this.movedItem = movedItem;
    }

    public int getMaxHeight()
    {
        int maxHeight = (field.getSettings().getRadius() * 2) + 1;

        if (field.getSettings().getHeight() > 0)
        {
            maxHeight = field.getSettings().getHeight();
        }

        int multiplier = field.getChildren().size() + 1;

        maxHeight *= multiplier;
        return Math.min(maxHeight, 512);
    }

    public int getMaxWidth()
    {
        int maxWidth = (field.getSettings().getRadius() * 2) + 1;
        int multiplier = field.getChildren().size() + 1;

        maxWidth *= multiplier;
        return maxWidth;
    }

    public boolean isExceeded()
    {
        if (maxx - minx > getMaxWidth())
        {
            return true;
        }

        if (maxz - minz > getMaxWidth())
        {
            return true;
        }

        if (maxy - miny > getMaxHeight())
        {
            return true;
        }

        return false;
    }
}
