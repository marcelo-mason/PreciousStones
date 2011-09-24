package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.LinkedList;
import java.util.List;

/**
 * @author phaed
 */
public class Visualization
{
    private List<BlockData> blocks = new LinkedList<BlockData>();
    private List<BlockData> outlineBlocks = new LinkedList<BlockData>();
    private List<Field> fields = new LinkedList<Field>();

    /**
     * @param loc
     */
    public void addBlock(Block block)
    {
        blocks.add(new BlockData(block));
    }

    public void addBlock(Location loc, int material, byte data)
    {
        BlockData bd = new BlockData(loc, material, data);

        if (!blocks.contains(bd))
        {
            blocks.add(bd);
        }
    }

    /**
     * @param field
     */
    public void addField(Field field)
    {
        fields.add(field);
    }

    /**
     * @return the locations
     */
    public List<BlockData> getBlocks()
    {
        return blocks;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields()
    {
        List<Field> f = new LinkedList<Field>();
        f.addAll(fields);
        return f;
    }

    public void setBlocks(List<BlockData> bds)
    {
        this.blocks = bds;
    }

    public List<BlockData> getOutlineBlocks()
    {
        return outlineBlocks;
    }

    public void setOutlineBlocks(List<BlockData> outlineBlocks)
    {
        this.outlineBlocks = outlineBlocks;
    }
}
