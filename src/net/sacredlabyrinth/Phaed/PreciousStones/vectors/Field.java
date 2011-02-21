package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class Field extends AbstractVec
{
    private int radius;
    private int height;
    private int typeId;
    private String owner;
    private String name;
    private ArrayList<String> allowed = new ArrayList<String>();
    private ChunkVec chunkvec;
    
    public Field(int x, int y, int z, int radius, int height, ChunkVec chunkvec, String world, int typeId, String owner, ArrayList<String> allowed, String name)
    {
	super(x, y, z, world);
	
	this.radius = radius;
	this.height = height;
	this.owner = owner;
	this.name = name;
	this.allowed = allowed;
	this.typeId = typeId;
	this.chunkvec = chunkvec;
    }
    
    public Field(Block block, int radius, int height, String owner, ArrayList<String> allowed, String name)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	
	this.radius = radius;
	this.height = height;
	this.owner = owner;
	this.name = name;
	this.allowed = allowed;
	this.typeId = block.getTypeId();
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public Field(Block block, int radius, int height)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	
	this.radius = radius;
	this.height = height;
	this.typeId = block.getTypeId();
	this.chunkvec = new ChunkVec(block.getChunk());
    }
    
    public Field(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
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
    
    public int getRadius()
    {
	return this.radius;
    }
    
    public int getHeight()
    {
	return this.height;
    }
    
    public String getOwner()
    {
	return this.owner;
    }
    
    public void setOwner(String owner)
    {
	this.owner = owner;
    }
    
    public void setName(String name)
    {
	this.name = name;
    }
    
    public String getName()
    {
	if (this.name.length() == 0)
	{
	    return this.owner + "'s domain";
	}
	
	return this.name;
    }
    
    public String getStoredName()
    {
	return this.name;
    }
    
    public ArrayList<String> getAllowed()
    {
	return allowed;
    }
    
    public ArrayList<String> getAllAllowed()
    {
	ArrayList<String> all = new ArrayList<String>();
	all.add(owner);
	all.addAll(allowed);
	return all;
    }
    
    public String getAllowedList()
    {
	String out = "";

	if (allowed.size() > 0)
	{
	    for (int i = 0; i < allowed.size(); i++)
	    {
		out += ", " + allowed.get(i);
	    }
	}
	else
	{
	    return null;
	}
	
	return out.substring(2);
    }
    
    public boolean isOwner(String playerName)
    {
	return playerName.equals(owner);
    }
    
    public boolean isAllAllowed(String playerName)
    {
	return playerName.equals(owner) || allowed.contains(playerName);
    }
    
    public boolean addAllowed(String playerName)
    {
	if (allowed.contains(playerName))
	    return false;
	
	allowed.add(playerName);
	return true;
    }
    
    public boolean removeAllowed(String playerName)
    {
	if (allowed.contains(playerName))
	    return false;
	
	allowed.remove(playerName);
	return true;
    }
    
    public ArrayList<Vector> getCorners()
    {
	ArrayList<Vector> corners = new ArrayList<Vector>();
	
	int minx = x - radius;
	int maxx = x + radius;
	int minz = z - radius;
	int maxz = z + radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	
	corners.add(new Vector(minx, miny, minz));
	corners.add(new Vector(minx, miny, maxz));
	corners.add(new Vector(minx, maxy, minz));
	corners.add(new Vector(minx, maxy, maxz));
	corners.add(new Vector(maxx, miny, minz));
	corners.add(new Vector(maxx, miny, maxz));
	corners.add(new Vector(maxx, maxy, minz));
	corners.add(new Vector(maxx, maxy, maxz));
	
	return corners;
    }
    
    public boolean intersects(Field field)
    {
	ArrayList<Vector> corners = field.getCorners();
	
	for (Vector vec : corners)
	{
	    if (this.envelops(vec))
	    {
		return true;
	    }
	}
	
	corners = this.getCorners();
	
	for (Vector vec : corners)
	{
	    if (field.envelops(vec))
	    {
		return true;
	    }
	}
	
	return false;
    }
    
    public boolean envelops(Vector vec)
    {
	int px = vec.getBlockX();
	int py = vec.getBlockY();
	int pz = vec.getBlockZ();
	
	int minx = x - radius;
	int maxx = x + radius;
	int minz = z - radius;
	int maxz = z + radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean envelops(Field field)
    {
	int px = field.getX();
	int py = field.getY();
	int pz = field.getZ();
	
	int minx = x - radius;
	int maxx = x + radius;
	int minz = z - radius;
	int maxz = z + radius;
	int miny = y - (int) Math.floor(((double) height) / 2);
	int maxy = y + (int) Math.ceil(((double) height) / 2);
	
	if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
	    return true;
	
	return false;
    }
    
    public boolean envelops(Block block)
    {
	return envelops(new Field(block));
    }
    
    @Override
    public String toString()
    {
	return super.toString() + " [owner:" + owner + "]";
    }
}
