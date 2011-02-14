package com.bukkit.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Chunk;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.PSVec;
import com.bukkit.Phaed.PreciousStones.Unbreakable;

/**
 * Holds all unbreakable stones
 * 
 * @author Phaed
 */
public class UnbreakableManager
{
    protected final HashMap<Vector, HashSet<Unbreakable>> chunkLists = new HashMap<Vector, HashSet<Unbreakable>>();
    
    @SuppressWarnings("unused")
    private Queue<Unbreakable> deletionQueue;
    private transient PreciousStones plugin;
    
    public UnbreakableManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public void initiate(PreciousStones plugin)
    {
	this.plugin = plugin;
	this.deletionQueue = new LinkedList<Unbreakable>();
    }
    
    /**
     * Exposes the chunklist
     */
    public HashMap<Vector, HashSet<Unbreakable>> getChunkLists()
    {
	return chunkLists;
    }
    
    /**
     * Process pending deletions
     */
    public void flush()
    {
	while (deletionQueue.size() > 0)
	{
	    Unbreakable delstone = deletionQueue.poll();
	    
	    for (HashSet<Unbreakable> c : chunkLists.values())
	    {
		for (Unbreakable unbreakable : c)
		{
		    if (unbreakable.getWorldId() == delstone.getWorldId())
			c.remove(unbreakable.getVector());
		}
	    }
	}
    }
    
    /**
     * Total number of unbreakable stones
     */
    public int count()
    {
	int size = 0;
	
	for (ArrayList<Unbreakable> c : chunkLists.values())
	    size += c.size();
	
	return size;
    }
    
    /**
     * Gets the unbreakable from source block
     */
    public Unbreakable getUnbreakable(Block unbreakableblock)
    {
	ArrayList<Unbreakable> c = chunkLists.get(new Vector(unbreakableblock.getChunk()));
	
	if (c != null)
	{
	    for (Unbreakable unbreakable : c)
	    {
		Vector blockvec = new Vector(unbreakableblock.getLocation());
		
		if (blockvec.equals(unbreakable.getVector()))
		{
		    if (unbreakable.getWorldId() == unbreakableblock.getWorld().getId())
			return unbreakable;
		}
	    }
	}
	return null;
    }
    
    /**
     * Looks for the block in our stone collection
     */
    public boolean isUnbreakable(Block unbreakableblock)
    {
	return getUnbreakable(unbreakableblock) != null;
    }
  
    /**
     * Determine whether a player is the owner of the stone
     */
    public boolean isOwner(Block unbreakableblock, String playerName)
    {
	Unbreakable unbreakable = getUnbreakable(unbreakableblock);
	
	if (unbreakable != null)
	    return unbreakable.isOwner(playerName);
	
	return false;
    }
    
    /**
     * Return the owner of a stone
     */
    public String getOwner(Block unbreakableblock)
    {
	Unbreakable unbreakable = getUnbreakable(unbreakableblock);
	
	if (unbreakable != null)
	    return unbreakable.getOwner();
	
	return "";
    }
    
    /**
     * Returns the unbreakable blocks in the chunk and adjacent chunks
     */
    public ArrayList<Unbreakable> getUnbreakablesInArea(Block blockInArea, int chunkradius)
    {
	ArrayList<Unbreakable> out = new ArrayList<Unbreakable>();
	Chunk chunk = blockInArea.getChunk();
	
	int xlow = chunk.getX() - chunkradius;
	int xhigh = chunk.getX() + chunkradius;
	int zlow = chunk.getZ() - chunkradius;
	int zhigh = chunk.getZ() + chunkradius;
	
	for (int x = xlow; x <= xhigh; x++)
	{
	    for (int z = zlow; z <= zhigh; z++)
	    {
		Chunk chnk = blockInArea.getWorld().getChunkAt(x, z);
		if (chunk != null)
		{
		    ArrayList<Unbreakable> c = chunkLists.get(new Vector(chnk));
		    
		    if (c != null)
		    {
			for (Unbreakable unbreakable : c)
			{
			    if (unbreakable.getWorldId() == blockInArea.getWorld().getId())
				out.add(unbreakable);
			}
		    }
		}
	    }
	}
	
	return out;
    }
    
    /**
     * Returns the unbreakable blocks in the chunk and adjacent chunks
     */
    public ArrayList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
	Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getUnbreakablesInArea(blockInArea, chunkradius);
    }
    
    /**
     * Add stone to the collection
     */
    public void add(Block unbreakableblock, String owner)
    {
	Vector cvec = new Vector(unbreakableblock.getChunk());
	ArrayList<Unbreakable> c = chunkLists.get(cvec);
	
	if (c != null)
	{
	    c.add(new Unbreakable(unbreakableblock, owner));
	}
	else
	{
	    ArrayList<Unbreakable> newc = new ArrayList<Unbreakable>();
	    newc.add(new Unbreakable(unbreakableblock, owner));
	    
	    chunkLists.put(cvec, newc);
	}
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Block unbreakableblock)
    {
	for (ArrayList<Unbreakable> c : chunkLists.values())
	{
	    Vector vec = new Vector(unbreakableblock.getLocation());
	    
	    for (Unbreakable unbreakable : c)
	    {
		if (unbreakable.getWorldId() == unbreakableblock.getWorld().getId())
		    c.remove(vec);
	    }
	}
    }
    
    /**
     * Adds to deletion queue
     */
    public void queueRelease(Block unbreakableblock)
    {
	deletionQueue.add(new Unbreakable(new Vector(unbreakableblock.getLocation()), null, unbreakableblock.getWorld().getId()));
    }
    
    /**
     * If the block is touching a pstone block
     */
    public boolean touchingUnbrakableBlock(Block block)
    {
	if (block == null)
	    return false;
	
	for (int x = -1; x <= 1; x++)
	{
	    for (int z = -1; z <= 1; z++)
	    {
		for (int y = -1; y <= 1; y++)
		{
		    if (x == 0 && y == 0 && z == 0)
			continue;
		    
		    Block surroundingblock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
		    
		    if (plugin.settings.isUnbreakableType(surroundingblock))
			return true;
		}
	    }
	}
	
	return false;
    }    
}
