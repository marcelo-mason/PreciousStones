package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.block.Block;
import org.bukkit.Location;

/**
 *
 * @author cc_madelg
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
    public ChunkVec getChunkVec()
    {
	return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }
}
