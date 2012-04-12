package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class BlockTypeEntry
{
    private final int typeId;
    private final byte data;
    /**
     * @param block
     */
    public BlockTypeEntry(Block block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getData();
    }

    /**
     * @param block
     */
    public BlockTypeEntry(int typeId, byte data)
    {
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
     * @return the data
     */
    public byte getData()
    {
        return data;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BlockTypeEntry))
        {
            return false;
        }

        BlockTypeEntry other = (BlockTypeEntry) obj;

        int id1 = this.getTypeId();
        int id2 = other.getTypeId();
        byte data1 = this.getData();
        byte data2 = other.getData();

        if (getData() == 0 || other.getData() == 0)
        {
            if (id1 == id2)
            {
                return true;
            }
        }
        else
        {
            if (id1 == id2 && data1 == data2)
            {
                return true;
            }
        }

        // adjust for changing blocks

        if (id1 == 73 && id2 == 74 || id1 == 74 && id2 == 73 || id1 == 61 && id2 == 62 || id1 == 62 && id2 == 61)
        {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + this.getTypeId();
        hash = 47 * hash + this.getData();
        return hash;
    }

    @Override
    public String toString()
    {
        if (getData() == 0)
        {
            return getTypeId() + "";
        }

        return getTypeId() + ":" + getData();
    }
}

