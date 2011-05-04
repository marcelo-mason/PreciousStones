package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

/**
 *
 * @author cc_madelg
 */
public abstract class AbstractVec
{
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    protected final int x, y, z;
    /**
     *
     */
    protected final String world;

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
     *
     * @return
     */
    public int getX()
    {
	return this.x;
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
     *
     * @return
     */
    public int getZ()
    {
	return this.z;
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
	return ((new Integer(x)).hashCode() >> 13) ^ ((new Integer(y)).hashCode() >> 7) ^ ((new Integer(z)).hashCode()) ^ ((world).hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof AbstractVec))
	    return false;

	AbstractVec other = (AbstractVec) obj;
	return other.x == this.x && other.y == this.y && other.z == this.z && other.world.equals(this.world);
    }

    @Override
    public String toString()
    {
	return "[" + x + " " + y + " " + z + "]";
    }
}
