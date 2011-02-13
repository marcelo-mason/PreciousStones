package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;

public class Unbreakable
{
    private Vector vec;
    private String owner;
    private long world;
    
    public Unbreakable(Block block, String owner)
    {
	this.vec = new Vector(block.getLocation());
	this.owner = owner;
	this.world = block.getWorld().getId();
    }
    
    public Unbreakable(Vector vec, String owner, long world)
    {
	this.vec = vec;
	this.owner = owner;
	this.world = world;
    }
    
    public Vector getVector()
    {
	return vec;
    }
    
    public String getOwner()
    {
	return owner;
    }
    
    public long getWorldId()
    {
	return world;
    }
    
    public boolean isOwner(String playerName)
    {
	return playerName.equals(owner);
    }
    
    @Override
    public String toString()
    {
	return "owner:" + owner + " world:" + world + vec.toString();
    }
}
