package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * @author phaed
 */
public class BlockTypeEntry
{
    private ItemInfo info;

    /**
     * @param string
     */
    public BlockTypeEntry(String string)
    {
        info = Items.itemByString(string);
    }

    /**
     * @param block
     */
    public BlockTypeEntry(Block block)
    {
        info = Items.itemByType(block.getType());
    }

    /**
     * @param block
     */
    public BlockTypeEntry(BlockState block)
    {
        info = Items.itemByType(block.getType());
    }

    /**
     * @param typeId
     * @param data
     */
    public BlockTypeEntry(int typeId, short data)
    {
        info = Items.itemByType(Material.getMaterial(typeId), data);
    }

    /**
     * @param typeId
     */
    public BlockTypeEntry(int typeId)
    {
        info = Items.itemByType(Material.getMaterial(typeId), (short) 0);
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return info.getId();
    }

    /**
     * @return the data
     */
    public short getSubTypeId()
    {
        return info.getSubTypeId();
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
        short data1 = this.getSubTypeId();
        short data2 = other.getSubTypeId();

        if (getSubTypeId() == 0 || other.getSubTypeId() == 0)
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
        return info.hashCode();
    }

    @Override
    public String toString()
    {
        if (getSubTypeId() == 0)
        {
            return getTypeId() + "";
        }

        return getTypeId() + ":" + getSubTypeId();
    }

    public ItemInfo getInfo()
    {
        return info;
    }

    public boolean isValid()
    {
        return info != null;
    }

    public String getFriendly()
    {
        return Helper.friendlyBlockType(getTypeId());
    }
}

