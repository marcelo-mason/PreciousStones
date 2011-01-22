package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import java.util.ArrayList;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Holds all protected stones
 * 
 * @author Phaed
 */
public class ProtectionManager implements java.io.Serializable
{
    static final long serialVersionUID = -1L;
    
    protected final HashMap<Vector, HashMap<Vector, ArrayList<String>>> chunkLists = new HashMap<Vector, HashMap<Vector, ArrayList<String>>>();
    
    private transient PreciousStones plugin;
    
    public ProtectionManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void initiate(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    /**
     * Total number of protection stones
     */
    public int count()
    {
	int size = 0;
	
	for (HashMap<Vector, ArrayList<String>> c : chunkLists.values())
	{
	    size += c.size();
	}
	
	return size;
    }
    
    /**
     * Looks for the block in our stone collection
     */
    public boolean isStone(Block block)
    {
	HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	{
	    return c.containsKey(new Vector(block));
	}
	
	return false;
    }
    
    /**
     * Check if a block is one of the proteciton types
     */
    public boolean isType(Material type)
    {
	for (Integer t : plugin.protectionBlocks)
	{
	    if (type.getId() == t)
		return true;
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a build proteced area owned by someone else
     */
    public boolean isBuildProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// if its unprotectable then return not protected
	
	for (int ub : plugin.unprotectableBlocks)
	{
	    if (block.getTypeId() == ub)
		return false;
	}
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && !c.get(vec).contains(playerName))
		{
		    // the player is in a protected area that he is not allowed
		    // lets see if the area lets strangers build
		    
		    if (plugin.protectionBlocks.contains(block.getTypeId()))
		    {
			int index = plugin.protectionBlocks.indexOf(block.getTypeId());
			boolean canBuild = plugin.protectionCanBuild.get(index);
			
			return !canBuild;
		    }
		    
		    return true;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a break protected area belonging to somebody else (not playerName)
     */
    public boolean isProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// if its unprotectable then return not protected
	
	for (int ub : plugin.unprotectableBlocks)
	{
	    if (block.getTypeId() == ub)
		return false;
	}
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && (playerName == null || !c.get(vec).contains(playerName)))
		    return true;
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the player is standing in a protected area of his
     */
    public boolean isOwnVector(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && c.get(vec).size() > 0 && c.get(vec).get(0).equals(playerName))
		    return true;
	    }
	}
	
	return false;
    }
    
    /**
     * Add allowed player to protected area
     */
    public boolean addAllowed(Block block, String allowedName)
    {
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block))
		{
		    if (!c.get(vec).contains(allowedName))
		    {
			c.get(vec).add(allowedName);
			return true;
		    }
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Remove allowed player from protected area
     */
    public boolean removeAllowed(Block block, String allowedName)
    {
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block))
		{
		    if (c.get(vec).size() > 1)
		    {
			c.get(vec).remove(allowedName);
			return true;
		    }
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Get allowed list of area
     */
    public ArrayList<String> getAllowedList(Block block)
    {
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block))
		{
		    return c.get(vec);
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the protective block will cover someone elses unbreakable or protection blocks
     */
    public boolean isInConflict(Block block, String owner)
    {
	if (block == null)
	    return false;
	
	HashMap<Vector, ArrayList<String>> c = getStonesInArea(block);
	
	if (c != null)
	{
	    if (plugin.pm.isType(block.getType()))
	    {
		int pindex = plugin.protectionBlocks.indexOf(block.getTypeId());
		int radius = plugin.protectionRadius.get(pindex);
		int extra = plugin.protectionExtraHeight.get(pindex);
		
		Vector newStonevec = new Vector(block, radius, extra);
		
		for (Vector pstone : c.keySet())
		{
		    // skip owned stones
		    
		    if (c.get(pstone).contains(owner))
			continue;
		    
		    // check to see if any of our stones live whithin the newly placed
		    // protective block's area
		    
		    if (newStonevec.isNear(pstone))
			return true;
		    
		    // check to see if were placing this stone whithin the area
		    // of an already placed stone
		    
		    if (pstone.isNear(newStonevec))
			return true;
		}
	    }
	    else
	    {
		Vector newStonevec = new Vector(block);
		
		for (Vector pstone : c.keySet())
		{
		    // skip owned stones
		    
		    if (c.get(pstone).contains(owner))
			continue;
		    
		    // check to see if were placing this stone whithin the area
		    // of an already placed stone
		    
		    if (pstone.isNear(newStonevec))
			return true;
		}
		
	    }
	}
	return false;
    }
    
    /**
     * Determine whether a player is the owner of a block
     */
    public boolean isOwner(Block block, String name)
    {
	HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	{
	    if (c.get(new Vector(block)).size() > 0)
		return name.equals(c.get(new Vector(block)).get(0));
	}
	
	return false;
    }
    
    /**
     * Return the owner of a stone
     */
    public String getOwner(Block block)
    {
	HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	{
	    if (c.get(new Vector(block)).size() > 0)
		return c.get(new Vector(block)).get(0);
	}
	
	return "";
    }
    
    /**
     * Add stone to the collection
     */
    public void addStone(Block block, String owner)
    {
	try
	{
	    int index = plugin.protectionBlocks.indexOf(block.getTypeId());
	    int radius = plugin.protectionRadius.get(index);
	    int extra = plugin.protectionExtraHeight.get(index);
	    
	    Vector cvec = new Vector(block.getChunk());
	    HashMap<Vector, ArrayList<String>> c = chunkLists.get(cvec);
	    
	    if (c != null)
	    {
		ArrayList<String> allowed = new ArrayList<String>();
		allowed.add(owner);
		
		c.put(new Vector(block, radius, extra), allowed);
	    }
	    else
	    {
		ArrayList<String> allowed = new ArrayList<String>();
		allowed.add(owner);
		
		HashMap<Vector, ArrayList<String>> newc = new HashMap<Vector, ArrayList<String>>();
		newc.put(new Vector(block, radius, extra), allowed);
		
		chunkLists.put(cvec, newc);
	    }
	}
	catch (Exception e)
	{
	    PreciousStones.log.info("PreciousStones Error: configuration file inconsistency");
	}
    }
    
    /**
     * Remove stones from the collection
     */
    public void releaseStone(Block block)
    {
	HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	{
	    c.remove(new Vector(block));
	}
    }
    
    /**
     * Returns the stones in the chunk and adjacent chunks
     */
    private HashMap<Vector, ArrayList<String>> getStonesInArea(Block block)
    {
	HashMap<Vector, ArrayList<String>> out = new HashMap<Vector, ArrayList<String>>();
	Chunk chunk = block.getChunk();
	
	int xlow = chunk.getX() - plugin.chunksInLargestProtectionArea;
	int xhigh = chunk.getX() + plugin.chunksInLargestProtectionArea;
	int zlow = chunk.getZ() - plugin.chunksInLargestProtectionArea;
	int zhigh = chunk.getZ() + plugin.chunksInLargestProtectionArea;
	
	for (int x = xlow; x <= xhigh; x++)
	{
	    for (int z = zlow; z <= zhigh; z++)
	    {
		Chunk chnk = plugin.getServer().getWorlds()[0].getChunkAt(x, z);
		if (chunk != null)
		{
		    HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(chnk));
		    if (c != null)
			out.putAll(c);
		}
	    }
	}
	
	return out;
    }
}
