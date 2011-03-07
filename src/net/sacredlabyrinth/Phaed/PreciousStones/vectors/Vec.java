package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.block.Block;
import org.bukkit.Location;

public class Vec extends AbstractVec
{
    private ChunkVec chunkvec;

    public Vec(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public Vec(Location loc)
    {
	super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }
    
    public ChunkVec getChunkVec()
    {
	return this.chunkvec;
    }
}
