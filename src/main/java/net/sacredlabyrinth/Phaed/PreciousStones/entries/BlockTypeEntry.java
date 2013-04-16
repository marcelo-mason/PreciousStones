package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.getspout.spoutapi.block.SpoutBlock;
import org.getspout.spoutapi.material.CustomBlock;

/**
 * @author phaed
 */
public class BlockTypeEntry
{
    private final int typeId;
    private byte data;
    private boolean isSpout;

    /**
     * @param block
     */
    public BlockTypeEntry(Block block)
    {
        if (PreciousStones.hasSpout())
        {
            SpoutBlock sblock = (SpoutBlock) block;
            CustomBlock customBlock = sblock.getCustomBlock();

            if (customBlock != null)
            {
                this.typeId = customBlock.getCustomId();
                this.data = sblock.getCustomBlockData();
                this.isSpout = true;
                return;
            }
        }

        this.typeId = block.getTypeId();
        this.data = block.getData();
        this.isSpout = false;
    }

    public BlockTypeEntry(String packed)
    {
        String[] unpacked = packed.split("[:]");
        this.typeId = Integer.parseInt(unpacked[0]);

        if (unpacked.length > 1)
        {
            this.data = Byte.parseByte(unpacked[1]);

            if (unpacked.length > 2)
            {
                this.isSpout = Boolean.parseBoolean(unpacked[2]);
            }
        }
        else
        {
            this.data = 0;
            this.isSpout = false;
        }
    }

    /**
     * @param block
     */
    public BlockTypeEntry(BlockState block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getRawData();
        this.isSpout = false;
    }

    /**
     * @param typeId
     * @param data
     */
    public BlockTypeEntry(int typeId, byte data)
    {
        this.typeId = typeId;
        this.data = data;
        this.isSpout = false;
    }

    /**
     * @param typeId
     * @param data
     */
    public BlockTypeEntry(int typeId, byte data, boolean isSpout)
    {
        this.typeId = typeId;
        this.data = data;
        this.isSpout = isSpout;
    }

    /**
     * @param typeId
     */
    public BlockTypeEntry(int typeId)
    {
        this.typeId = typeId;
        this.data = 0;
        this.isSpout = false;
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

        if (id1 == 8 && id2 == 9 || id1 == 9 && id2 == 8 || id1 == 11 && id2 == 10 || id1 == 10 && id2 == 11 || id1 == 73 && id2 == 74 || id1 == 74 && id2 == 73 || id1 == 61 && id2 == 62 || id1 == 62 && id2 == 61)
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
        hash = 47 * hash + (this.isSpout() ? 1 : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        if (isSpout)
        {
            return getTypeId() + ":" + getData() + ":" + isSpout();
        }
        else
        {
            if (getData() == 0)
            {
                return getTypeId() + "";
            }

            return getTypeId() + ":" + getData();
        }
    }

    public void setData(byte data)
    {
        this.data = data;
    }

    public boolean isValid()
    {
        if (isSpout())
        {
            return true;
        }

        return getTypeId() >= 0;
    }

    public String getFriendly()
    {
        if (isSpout())
        {
            return getTypeId() + "";
        }
        else
        {
            return Helper.friendlyBlockType(getTypeId());
        }
    }

    public boolean isSpout()
    {
        return isSpout;
    }
}

