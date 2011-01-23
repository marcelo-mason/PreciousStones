package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.block.Block;

/**
 * Holds all unbreakable stones
 * 
 * @author Phaed
 */
public class UnbreakableManager implements java.io.Serializable
{
    static final long serialVersionUID = -1L;
    
    protected final HashMap<Vector, HashMap<Vector, String>> chunkLists = new HashMap<Vector, HashMap<Vector, String>>();
    
    private Queue<Vector> deletionQueue;
    private transient PreciousStones plugin;
    
    public UnbreakableManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public void initiate(PreciousStones plugin)
    {
	this.plugin = plugin;
	this.deletionQueue =  new LinkedList<Vector>();
    }
    
    /**
     * Process pending deletions
     */
    public void flush()
    {
	while(deletionQueue.size() > 0)
	{
	    Vector vec = deletionQueue.poll();
	    
	    for (HashMap<Vector, String> c : chunkLists.values())
		c.remove(vec);	    
	}
   }
    
    /**
     * Total number of unbreakable stones
     */
    public int count()
    {
	int size = 0;
	
	for (HashMap<Vector, String> c : chunkLists.values())
	    size += c.size();

	return size;
    }
    
    /**
     * Looks for the block in our stone collection
     */
    public boolean isPStone(Block block)
    {
	HashMap<Vector, String> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	    return c.containsKey(new Vector(block));
	
	return false;
    }
    
    /**
     * Check if a block is one of the unbreakable types
     */
    public boolean isType(Block block)
    {
	for (Integer t : plugin.psettings.unbreakableBlocks)
	{
	    if (block.getTypeId() == t)
		return true;
	}
	
	return false;
    }
    
    /**
     * Determine whether a player is the owner of a block
     */
    public boolean isOwner(Block block, String name)
    {
	HashMap<Vector, String> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	    return name.equals(c.get(new Vector(block)));
	
	return false;
    }
    
    /**
     * Return the owner of a stone
     */
    public String getOwner(Block block)
    {
	HashMap<Vector, String> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	    return c.get(new Vector(block));
	
	return "";
    }
    
    /**
     * Add stone to the collection
     */
    public void addStone(Block block, String owner)
    {
	Vector cvec = new Vector(block.getChunk());
	HashMap<Vector, String> c = chunkLists.get(cvec);
	
	if (c != null)
	{
	    c.put(new Vector(block), owner);
	}
	else
	{
	    HashMap<Vector, String> newc = new HashMap<Vector, String>();
	    newc.put(new Vector(block), owner);
	    
	    chunkLists.put(cvec, newc);
	}
    }
    
    /**
     * Remove stones from the collection
     */
    public void releaseStone(Block block)
    {
	deletionQueue.add(new Vector(block));
    }    
}
