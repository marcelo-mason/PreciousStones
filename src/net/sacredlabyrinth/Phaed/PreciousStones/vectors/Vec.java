package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.block.Block;
import org.bukkit.Location;

/**
 *
 * @author phaed
 */
public class Vec extends AbstractVec
{
    /**
     *
     * @param block
     */
    public Vec(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    /**
     *
     * @param field
     */
    public Vec(AbstractVec av)
    {
        super(av.getX(), av.getY(), av.getZ(), av.getWorld());
    }

    /**
     *
     * @param loc
     */
    public Vec(Location loc)
    {
        super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

    /**
     *
     * @return
     */
    public ChunkVec toChunkVec()
    {
        return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }
}
