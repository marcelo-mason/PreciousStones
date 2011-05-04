package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author cc_madelg
 */
@MappedSuperclass
public abstract class AbstractVec implements Serializable
{

    /**
     *  The world name the vector belongs to
     */
    private int x;
    private int y;
    private int z;

    private String world;

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
    public int hashCode()
    {
	return ((new Integer(getX())).hashCode() >> 13) ^ ((new Integer(getY())).hashCode() >> 7) ^ ((new Integer(getZ())).hashCode()) ^ ((getWorld()).hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof AbstractVec))
	    return false;

	AbstractVec other = (AbstractVec) obj;
	return other.getX() == this.getX() && other.getY() == this.getY() && other.getZ() == this.getZ() && other.getWorld().equals(this.getWorld());
    }

    @Override
    public String toString()
    {
	return "[" + getX() + " " + getY() + " " + getZ() + "]";
    }
}
