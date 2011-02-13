package com.bukkit.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.Unbreakable;

/**
 * Holds all unbreakable stones
 * 
 * @author Phaed
 */
public class UnbreakableManager
{
    protected final HashMap<Vector, ArrayList<Unbreakable>> chunkLists = new HashMap<Vector, ArrayList<Unbreakable>>();
    
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
    public HashMap<Vector, ArrayList<Unbreakable>> getChunkLists()
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
	    
	    for (ArrayList<Unbreakable> c : chunkLists.values())
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
     * Check if a block is one of the unbreakable types
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
	for (Integer t : plugin.settings.unbreakableBlocks)
	{
	    if (unbreakableblock.getTypeId() == t)
		return true;
	}
	
	return false;
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
}
