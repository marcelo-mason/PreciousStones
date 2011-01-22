package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.Chunk;

public class Vector implements java.io.Serializable
{
    static final long serialVersionUID = -4L;
    
    protected final int x, y, z;
    protected final int minx;
    protected final int miny;
    protected final int minz;
    protected final int maxx;
    protected final int maxy;
    protected final int maxz;
    protected int radius;
    
    public Vector(int x, int y, int z, int radius, int extraHeight)
    {
	this.x = x;
	this.y = y;
	this.z = z;
	this.minx = x - radius;
	this.miny = y - radius;
	this.minz = z - radius;
	this.maxx = x + radius;
	this.maxy = y + radius + extraHeight;
	this.maxz = z + radius;
    }
    
    public Vector(Block block, int radius, int extraHeight)
    {
	this.x = block.getX();
	this.y = block.getY();
	this.z = block.getZ();
	this.minx = x - radius;
	this.miny = y - radius;
	this.minz = z - radius;
	this.maxx = x + radius;
	this.maxy = y + radius + extraHeight;
	this.maxz = z + radius;
    }
    
    public Vector(int x, int y, int z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
	this.minx = x;
	this.miny = y;
	this.minz = z;
	this.maxx = x;
	this.maxy = y;
	this.maxz = z;

    }
    
    public Vector(Block block)
    {
	this.x = block.getX();
	this.y = block.getY();
	this.z = block.getZ();
	this.minx = x;
	this.miny = y;
	this.minz = z;
	this.maxx = x;
	this.maxy = y;
	this.maxz = z;
    }
    
    public Vector(Chunk chunk)
    {
	this.x = chunk.getX();
	this.y = 0;
	this.z = chunk.getZ();
	this.minx = x;
	this.miny = y;
	this.minz = z;
	this.maxx = x;
	this.maxy = y;
	this.maxz = z;
    }
    
    public int getX()
    {
	return this.x;
    }
    
    public int getY()
    {
	return this.y;
    }
    
    public int getZ()
    {
	return this.z;
    }
    
    public boolean isNear(Block block)
    {	
	int px = block.getX();
	int py = block.getY();
	int pz = block.getZ();
	
	if ( px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	{
	    return true;
	}
	
	return false;
    }
    
    public boolean isNear(Vector vec)
    {	
	int px = vec.getX();
	int py = vec.getY();
	int pz = vec.getZ();
	
	if ( px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	{
	    return true;
	}
	
	return false;
    }
    
    @Override
    public int hashCode()
    {
	return ((new Integer(x)).hashCode() >> 13) ^ ((new Integer(y)).hashCode() >> 7) ^ (new Integer(z)).hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof Vector))
	{
	    return false;
	}
	Vector other = (Vector) obj;
	return other.x == this.x && other.y == this.y && other.z == this.z;
    }
    
    @Override
    public String toString()
    {
	return "[" + x + ", " + y + ", " + z + "]";
    }
}
