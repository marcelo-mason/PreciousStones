package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public abstract class AbstractVec
{
    /**
     *  The world name the vector belongs to
     */
    private int x;
    private int y;
    private int z;
    private String world;

    /**
     *
     */
    public AbstractVec()
    {
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param world
     */
    public AbstractVec(int x, int y, int z, String world)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public int getX()
    {
        return this.x;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     *
     * @return
     */
    public int getY()
    {
        return this.y;
    }

    /**
     * @param z the z to set
     */
    public void setZ(int z)
    {
        this.z = z;
    }

    /**
     *
     * @return
     */
    public int getZ()
    {
        return this.z;
    }

    /**
     * @param world the world to set
     */
    public void setWorld(String world)
    {
        this.world = world;
    }

    /**
     *
     * @return
     */
    public String getWorld()
    {
        return this.world;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AbstractVec))
        {
            return false;
        }

        AbstractVec other = (AbstractVec) obj;
        return other.getX() == this.getX() && other.getY() == this.getY() && other.getZ() == this.getZ() && other.getWorld().equals(this.getWorld());
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + this.x;
        hash = 47 * hash + this.y;
        hash = 47 * hash + this.z;
        hash = 47 * hash + (this.world != null ? this.world.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "[" + getX() + " " + getY() + " " + getZ() + " " + getWorld() + "]";
    }

    /**
     * @return the chunkvec
     */
    public ChunkVec toChunkVec()
    {
        return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }

    /**
     * @return the vec
     */
    public Vec toVec()
    {
        return new Vec(this);
    }

    /**
     *
     * @param world
     * @return
     */
    public Location toLocation(World world)
    {
        return new Location(world, getX(), getY(), getZ());
    }
}
