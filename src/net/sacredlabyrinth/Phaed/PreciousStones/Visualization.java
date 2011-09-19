package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author phaed
 */
public class Visualization
{
    private Queue<BlockData> bds = new LinkedList<BlockData>();
    private List<Field> fields = new LinkedList<Field>();

    /**
     * @param loc
     */
    public void addBlock(Block block)
    {
        bds.add(new BlockData(block));
    }

    public void addBlock(Location loc, int material, byte data)
    {
        BlockData bd = new BlockData(loc, material, data);

        if (!bds.contains(bd))
        {
            bds.add(bd);
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
    public Queue<BlockData> getBlocks()
    {
        return bds;
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
}
