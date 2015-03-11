package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.MaterialName;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author phaed
 */
public class BlockTypeEntry
{
    private final int typeId;
    private byte data = 0;

    /**
     * @param block
     */
    public BlockTypeEntry(Block block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getData();
    }

    /**
     * @param material
     */
    public BlockTypeEntry(Material material)
    {
        this.typeId = material.getId();
    }

    public BlockTypeEntry(String string)
    {
        // int
        Pattern pattern = Pattern.compile("(?i)^(\\d+)$");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find())
        {
            this.typeId = Integer.parseInt(matcher.group(1));
            return;
        }

        // int:int
        matcher.reset();
        pattern = Pattern.compile("(?i)^(\\d+):(\\d+)$");
        matcher = pattern.matcher(string);
        if (matcher.find())
        {
            this.typeId = Integer.parseInt(matcher.group(1));
            this.data = Byte.parseByte(matcher.group(2));
            return;
        }

        // name
        matcher.reset();
        pattern = Pattern.compile("(?i)^(.*)$");
        matcher = pattern.matcher(string);
        if (matcher.find())
        {
            String name = matcher.group(1);
            Material material = MaterialName.getBlockMaterial(name);

            if (material == null)
            {
                material = MaterialName.getItemMaterial(name);
            }

            if (material != null)
            {
                this.typeId = material.getId();
            }
            else
            {
                this.typeId = -1;
            }
        }
        else
        {
            this.typeId = -1;
        }
    }

    /**
     * @param block
     */
    public BlockTypeEntry(BlockState block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getRawData();
    }

    /**
     * @param typeId
     * @param data
     */
    public BlockTypeEntry(int typeId, byte data)
    {
        this.typeId = typeId;
        this.data = data;
    }

    /**
     * @param typeId
     */
    public BlockTypeEntry(int typeId)
    {
        this.typeId = typeId;
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

    public void setData(byte data)
    {
        this.data = data;
    }

    public boolean isValid()
    {
        return getTypeId() >= 0;
    }

    public String getFriendly()
    {
        return Helper.friendlyBlockType(getTypeId());
    }
}

