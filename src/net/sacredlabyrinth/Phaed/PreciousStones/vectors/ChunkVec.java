package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Chunk;

/**
 *
 * @author phaed
 */
public class ChunkVec extends AbstractVec
{
    /**
     *
     * @param chunk
     */
    public ChunkVec(Chunk chunk)
    {
	super(chunk.getX(), 0, chunk.getZ(), chunk.getWorld().getName());
    }

    /**
     *
     * @param x
     * @param z
     * @param world
     */
    public ChunkVec(int x, int z, String world)
    {
	super(x, 0, z, world);
    }
}
