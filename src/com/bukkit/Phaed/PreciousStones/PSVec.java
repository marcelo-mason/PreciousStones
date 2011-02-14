package com.bukkit.Phaed.PreciousStones;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class PSVec
{
    protected final int x, y, z;
    protected int height;
    protected int radius;
    
    public PSVec(int x, int y, int z, int radius, int height)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }
    
    public PSVec(Location loc, int radius, int height)
    {
	this.x = loc.getBlockX();
	this.y = loc.getBlockY();
	this.z = loc.getBlockZ();
    }
    
    public PSVec(int x, int y, int z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }
    
    public PSVec(Location loc)
    {
	this.x = loc.getBlockX();
	this.y = loc.getBlockY();
	this.z = loc.getBlockZ();
    }

    public int getRadius()
    {
	return this.radius;
    }
    
    public int getHeight()
    {
	return this.height;
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
	
	int minx = x - radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int minz = z - radius;
	int maxx = x + radius;
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	int maxz = z + radius;
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean isNear(PSVec vec)
    {
	int px = vec.getX();
	int py = vec.getY();
	int pz = vec.getZ();
	
	int minx = x - radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int minz = z - radius;
	int maxx = x + radius;
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	int maxz = z + radius;

	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean isNearPlusOne(Block block)
    {
	int px = block.getX();
	int py = block.getY();
	int pz = block.getZ();

	int minx = x - radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int minz = z - radius;
	int maxx = x + radius;
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	int maxz = z + radius;

	if (px >= minx - 1 && px <= maxx + 1 && py >= miny - 1 && py <= maxy + 1 && pz >= minz - 1 && pz <= maxz + 1)
	    return true;
	
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
	if (!(obj instanceof PSVec))
	    return false;

	PSVec other = (PSVec) obj;
	return other.x == this.x && other.y == this.y && other.z == this.z;
    }
    
    @Override
    public String toString()
    {
	return "[" + x + ", " + y + ", " + z + "]";
    }
}