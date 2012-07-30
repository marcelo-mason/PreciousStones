package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class Vec extends AbstractVec
{
    /**
     * @param x
     * @param y
     * @param world
     * @param z
     */
    public Vec(int x, int y, int z, String world)
    {
        super(x, y, z, world);
    }

    /**
     * @param block
     */
    public Vec(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    /**
     * @param av
     */
    public Vec(AbstractVec av)
    {
        super(av.getX(), av.getY(), av.getZ(), av.getWorld());
    }

    /**
     * @param loc
     */
    public Vec(Location loc)
    {
        super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

    /**
     * @param vec
     * @return
     */
    public double distance(Vec vec)
    {
        return Math.sqrt(Math.pow(vec.getX() - getX(), 2.0D) + Math.pow(vec.getY() - getY(), 2.0D) + Math.pow(vec.getZ() - getZ(), 2.0D));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Vec add(int x, int y, int z)
    {
        return new Vec(this.getX() + x, this.getY() + y, this.getZ() + z, this.getWorld());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Vec subtract(int x, int y, int z)
    {
        return new Vec(this.getX() - x, this.getY() - y, this.getZ() - z, this.getWorld());
    }

    public Vec(String packed)
    {
        super(Integer.parseInt(packed.split("[|]")[0]), Integer.parseInt(packed.split("[|]")[1]), Integer.parseInt(packed.split("[|]")[2]), packed.split("[|]")[3]);
    }

    public String serialize()
    {
        return getX() + "|" + getY() + "|" + getZ() + "|" + getBlock();
    }
}
