package com.bukkit.Phaed.PreciousStones;

import org.bukkit.block.Block;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class Vector implements java.io.Serializable
{
    static final long serialVersionUID = -4L;
    
    protected final int x, y, z;
    protected int minx;
    protected int miny;
    protected int minz;
    protected int maxx;
    protected int maxy;
    protected int maxz;
    protected int height;
    protected int extraHeight; // DEPRECATED
    protected int radius;
    
    public Vector(int x, int y, int z, int radius, int height)
    {
	this.x = x;
	this.y = y;
	this.z = z;
	this.minx = x - radius;
	this.miny = y - (int) Math.floor(((double) height) / 2);
	this.minz = z - radius;
	this.maxx = x + radius;
	this.maxy = y + (int) Math.ceil(((double) height) / 2);
	this.maxz = z + radius;
	this.height = height;
    }
    
    public Vector(Block block, int radius, int height)
    {
	
	this.x = block.getX();
	this.y = block.getY();
	this.z = block.getZ();
	this.minx = x - radius;
	this.miny = y - (int) Math.floor(((double) height) / 2);
	this.minz = z - radius;
	this.maxx = x + radius;
	this.maxy = y + (int) Math.ceil(((double) height) / 2);
	this.maxz = z + radius;
	this.height = height;
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
    
    public Vector(Location loc)
    {
	this.x = loc.getBlockX();
	this.y = loc.getBlockY();
	this.z = loc.getBlockZ();
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
    
    public void setRadius(int newRadius)
    {
	this.radius = newRadius;
	this.minx = x - radius;
	this.miny = y - (int) Math.floor(((double) height) / 2);
	this.minz = z - radius;
	this.maxx = x + radius;
	this.maxy = y + (int) Math.ceil(((double) height) / 2);
	this.maxz = z + radius;
    }
    
    public boolean isNear(Block block)
    {
	int px = block.getX();
	int py = block.getY();
	int pz = block.getZ();
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean isNearPlusOne(Block block)
    {
	int px = block.getX();
	int py = block.getY();
	int pz = block.getZ();
	
	if (px >= minx - 1 && px <= maxx + 1 && py >= miny - 1 && py <= maxy + 1 && pz >= minz - 1 && pz <= maxz + 1)
	    return true;
	
	return false;
    }
    
    public boolean isNear(Player player)
    {
	int px = player.getLocation().getBlockX();
	int py = player.getLocation().getBlockY();
	int pz = player.getLocation().getBlockZ();
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean isNear(Vector vec)
    {
	int px = vec.getX();
	int py = vec.getY();
	int pz = vec.getZ();
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
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
