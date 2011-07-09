package net.sacredlabyrinth.Phaed.PreciousStones;

/**
 *
 * @author phaed
 */
public class GriefBlock
{
    private int x;
    private int y;
    private int z;
    private String world;
    private int typeId;
    private byte data;
    private String signText;

    public GriefBlock(int x, int y, int z, String world, int typeId, byte data)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.typeId = typeId;
        this.data = data;
    }

    /**
     * @return the x
     */
    public int getX()
    {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY()
    {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * @return the z
     */
    public int getZ()
    {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(int z)
    {
        this.z = z;
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return typeId;
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    /**
     * @return the data
     */
    public byte getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte data)
    {
        this.data = data;
    }

    /**
     * @return the world
     */
    public String getWorld()
    {
        return world;
    }

    /**
     * @param world the world to set
     */
    public void setWorld(String world)
    {
        this.world = world;
    }

    /**
     * @return the signText
     */
    public String getSignText()
    {
        return signText;
    }

    /**
     * @param signText the signText to set
     */
    public void setSignText(String signText)
    {
        this.signText = signText;
    }

}
