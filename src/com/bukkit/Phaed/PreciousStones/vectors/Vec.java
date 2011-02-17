package com.bukkit.Phaed.PreciousStones.vectors;

import org.bukkit.block.Block;

public class Vec extends AbstractVec
{
    private ChunkVec chunkvec;

    public Vec(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public ChunkVec getChunkVec()
    {
	return this.chunkvec;
    }
}
