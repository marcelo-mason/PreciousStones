package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;

public class Unbreakable
{
    private final Vector vec;
    private final long world;
    private String owner;
    
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
    
    public void setOwner(String owner)
    {
	this.owner = owner;
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
    public int hashCode()
    {
	return ((new Integer(vec.x)).hashCode() >> 13) ^ ((new Integer(vec.y)).hashCode() >> 7) ^ ((new Integer(vec.z)).hashCode()) ^ ((new Long(world)).hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof Vector))
	{
	    return false;
	}
	Vector other = (Vector) obj;
	return other.x == this.vec.x && other.y == this.vec.y && other.z == this.vec.z;
    }
    
    @Override
    public String toString()
    {
	return "owner:" + owner + " world:" + world + vec.toString();
    }
}
