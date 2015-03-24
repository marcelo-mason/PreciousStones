package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
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

    public CuboidEntry(Field field, boolean existing)
    {
        this.field = field;

        if (existing)
        {
            minx = field.getMinx();
            miny = field.getMiny();
            minz = field.getMinz();
            maxx = field.getMaxx();
            maxy = field.getMaxy();
            maxz = field.getMaxz();
        }
        else
        {
            minx = field.getX();
            miny = field.getY();
            minz = field.getZ();
            maxx = field.getX();
            maxy = field.getY();
            maxz = field.getZ();
        }
    }

    public CuboidEntry(Field field, int minx, int maxx, int miny, int maxy, int minz, int maxz, List<BlockEntry> selected, Location expanded)
    {
        this.field = field;
        this.maxx = maxx;
        this.minx = minx;
        this.maxy = maxy;
        this.miny = miny;
        this.maxz = maxz;
        this.minz = minz;
        this.selected = new ArrayList<BlockEntry>(selected);
        this.expanded = expanded;
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
     * Expand cuboid
     *
     * @param num
     * @param dir
     */
    public void expand(int num, String dir)
    {
        if (dir.toLowerCase().startsWith("u"))
        {
            this.maxy = this.maxy + num;
        }
        else if (dir.toLowerCase().startsWith("d"))
        {
            this.miny = this.miny - num;
        }
        else if (dir.toLowerCase().startsWith("n"))
        {
            this.minz = this.minz - num;
        }
        else if (dir.toLowerCase().startsWith("s"))
        {
            this.maxz = this.maxz + num;
        }
        else if (dir.toLowerCase().startsWith("e"))
        {
            this.maxx = this.maxx + num;
        }
        else if (dir.toLowerCase().startsWith("w"))
        {
            this.minx = this.minx - num;
        }
        else if (dir.toLowerCase().startsWith("a"))
        {
            this.minx = this.minx - num;
            this.miny = this.miny - num;
            this.minz = this.minz - num;
            this.maxz = this.maxz + num;
            this.maxx = this.maxx + num;
            this.minx = this.minx - num;
        }
    }

    /**
     * Expand cuboid
     *
     * @param u
     * @param d
     * @param n
     * @param s
     * @param e
     * @param w
     */
    public void expand(int u, int d, int n, int s, int e, int w)
    {
        this.maxy = this.maxy + u;
        this.miny = this.miny - d;
        this.minz = this.minz - n;
        this.maxz = this.maxz + s;
        this.maxx = this.maxx + e;
        this.minx = this.minx - w;
    }

    /**
     * Contract cuboid
     *
     * @param num
     * @param dir
     */
    public void contract(int num, String dir)
    {
        if (dir.toLowerCase().startsWith("u"))
        {
            this.maxy = this.maxy - num;
        }
        else if (dir.toLowerCase().startsWith("d"))
        {
            this.miny = this.miny + num;
        }
        else if (dir.toLowerCase().startsWith("n"))
        {
            this.minz = this.minz + num;
        }
        else if (dir.toLowerCase().startsWith("s"))
        {
            this.maxz = this.maxz - num;
        }
        else if (dir.toLowerCase().startsWith("e"))
        {
            this.maxx = this.maxx - num;
        }
        else if (dir.toLowerCase().startsWith("w"))
        {
            this.minx = this.minx + num;
        }
        else if (dir.toLowerCase().startsWith("a"))
        {
            this.maxy = this.maxy - num;
            this.miny = this.miny + num;
            this.minz = this.minz + num;
            this.maxz = this.maxz - num;
            this.maxx = this.maxx - num;
            this.minx = this.minx + num;
        }

        fixOverContract();
    }

    /**
     * Contract cuboid
     *
     * @param u
     * @param d
     * @param n
     * @param s
     * @param e
     * @param w
     */
    public void contract(int u, int d, int n, int s, int e, int w)
    {
        this.maxy = this.maxy - u;
        this.miny = this.miny + d;
        this.minz = this.minz + n;
        this.maxz = this.maxz - s;
        this.maxx = this.maxx - e;
        this.minx = this.minx + w;

        fixOverContract();
    }

    public void fixOverContract()
    {
        if (maxy < field.getY())
        {
            maxy = field.getY();
        }

        if (miny > field.getY())
        {
            miny = field.getY();
        }

        if (minz > field.getZ())
        {
            minz = field.getZ();
        }

        if (maxz < field.getZ())
        {
            maxz = field.getZ();
        }

        if (maxx < field.getX())
        {
            maxx = field.getX();
        }

        if (minx > field.getX())
        {
            minx = field.getX();
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

        List<Block> lineOfSight = player.getLineOfSight(PreciousStones.getInstance().getSettingsManager().getThroughFieldsByteSet(), Math.max(Math.max(Math.max(Helper.getWidthFromCoords(maxx, miny), Helper.getWidthFromCoords(maxz, minz)), Helper.getWidthFromCoords(maxy, miny)), 256));

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

        int testVolume = Helper.getWidthFromCoords(maxyt, minyt) * Helper.getWidthFromCoords(maxxt, minxt) * Helper.getWidthFromCoords(maxzt, minzt);

        return testVolume <= getMaxVolume();
    }

    public int getOverflow()
    {
        return getVolume() - getMaxVolume();
    }

    /**
     * Get the current volume of the definition
     *
     * @return
     */
    public int getVolume()
    {
        return Helper.getWidthFromCoords(maxy, miny) * Helper.getWidthFromCoords(maxx, minx) * Helper.getWidthFromCoords(maxz, minz);
    }

    /**
     * The the maximum allowed volume for the cuboid
     *
     * @return
     */
    public int getMaxVolume()
    {
        int volume = field.getMaxVolume();

        for (Field child : field.getChildren())
        {
            volume += child.getMaxVolume();
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
        PreciousStones.debug("x: %s %s", minx, maxx);
        PreciousStones.debug("y: %s %s", miny, maxy);
        PreciousStones.debug("z: %s %s", minz, maxz);
        PreciousStones.debug("Lengths: %s %s %s", Helper.getWidthFromCoords(maxx, minx), Helper.getWidthFromCoords(maxy, miny), Helper.getWidthFromCoords(maxz, minz));
        PreciousStones.debug("Max volume: %s", getMaxVolume());
        PreciousStones.debug("Volume: %s", getVolume());

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

    public Field getMockField()
    {
        return new Field(field.getX(), field.getY(), field.getZ(), minx, miny, minz, maxx, maxy, maxz, 0, field.getWorld(), field.getTypeEntry(), field.getOwner(), field.getName(), 0);
    }

    public CuboidEntry Clone()
    {
        return new CuboidEntry(field, minx, maxx, miny, maxy, minz, maxz, selected, expanded);
    }
}
