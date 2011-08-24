package net.sacredlabyrinth.Phaed.PreciousStones;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 *
 * @author phaed
 */
public class BlockData
{
    private final int typeId;
    private final byte data;
    private final Location location;

    /**
     *
     * @param block
     */
    public BlockData(Block block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getData();
        this.location = block.getLocation();
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return typeId;
    }

    /**
     * @return the data
     */
    public byte getData()
    {
        return data;
    }

    /**
     * @return the location
     */
    public Location getLocation()
    {
        return location;
    }
}
