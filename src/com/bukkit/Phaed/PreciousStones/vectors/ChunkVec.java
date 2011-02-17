package com.bukkit.Phaed.PreciousStones.vectors;

import org.bukkit.Chunk;

public class ChunkVec extends AbstractVec
{
    public ChunkVec(Chunk chunk)
    {
	super(chunk.getX(), 0, chunk.getZ(), chunk.getWorld().getName());
    }
    
    public ChunkVec(int x, int z, String world)
    {
	super(x, 0, z, world);
    }
}
