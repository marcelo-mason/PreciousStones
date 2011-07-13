package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.AbstractVec;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public class GriefBlock extends AbstractVec
{
    private int typeId;
    private byte data;
    private String signText;

    public GriefBlock(int x, int y, int z, String world, int typeId, byte data)
    {
        super(x, y, z, world);
        this.typeId = typeId;
        this.data = data;
    }

    public GriefBlock(Location loc, int typeId, byte data)
    {
        super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
        this.typeId = typeId;
        this.data = data;
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
