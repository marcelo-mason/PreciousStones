package com.bukkit.Phaed.PreciousStones.vectors;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Unbreakable extends AbstractVec
{
    private String owner;
    private int typeId;
    private ChunkVec chunkvec;
    
    public Unbreakable(int x, int y, int z, ChunkVec chunkvec, String world, int typeId, String owner)
    {
	super(x, y, z, world);

	this.owner = owner;
	this.typeId = typeId;
	this.chunkvec = chunkvec;
    }
    
    public Unbreakable(Block block, String owner)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	
	this.owner = owner;
	this.typeId = block.getTypeId();
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public Unbreakable(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

	this.typeId = block.getTypeId();
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public int getTypeId()
    {
	return this.typeId;
    }
        
    public String getType()
    {
	return Material.getMaterial(this.typeId).toString();
    }

    public ChunkVec getChunkVec()
    {
	return this.chunkvec;
    }
    
    public String getOwner()
    {
	return owner;
    }
    
    public void setOwner(String owner)
    {
	this.owner = owner;
    }
    
    public boolean isOwner(String playerName)
    {
	return playerName.equals(owner);
    }    
    
    @Override
    public String toString()
    {
	return super.toString() + " [owner:" + owner + "]";
    }
}
