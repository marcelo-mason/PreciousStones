package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.PSettings.PStone;

/**
 * Holds all protected stones
 * 
 * @author Phaed
 */
public class ProtectionManager implements java.io.Serializable
{
    static final long serialVersionUID = -1L;
    
    protected final HashMap<Vector, HashMap<Vector, ArrayList<String>>> chunkLists = new HashMap<Vector, HashMap<Vector, ArrayList<String>>>();
    
    private Queue<Vector> deletionQueue;
    private transient PreciousStones plugin;
    
    public ProtectionManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public void initiate(PreciousStones plugin)
    {
	this.plugin = plugin;
	this.deletionQueue = new LinkedList<Vector>();
    }
    
    /**
     * Returns the settings for a specific pstone block type
     */
    public PStone getPStoneSettings(Block block)
    {
	return plugin.psettings.getPStoneSettings(block);
    }
    
    /**
     * Check if a block is one of the proteciton types
     */
    public boolean isPStoneType(Block block)
    {
	return plugin.psettings.isPStoneType(block);
    }
    
    /**
     * If its unbreakable or not
     */
    public boolean isBreakable(Block block)
    {
	PStone psettings = plugin.psettings.getPStoneSettings(block);
	return psettings == null ? false : psettings.breakable;
    }
    
    /**
     * Process pending deletions
     */
    public void flush()
    {
	while (deletionQueue.size() > 0)
	{
	    Vector vec = deletionQueue.poll();
	    
	    for (HashMap<Vector, ArrayList<String>> c : chunkLists.values())
		c.remove(vec);
	}
    }
    
    /**
     * Total number of protection stones
     */
    public int count()
    {
	int size = 0;
	
	for (HashMap<Vector, ArrayList<String>> c : chunkLists.values())
	    size += c.size();
	
	return size;
    }
    
    /**
     * Looks for the block in our stone collection
     */
    public boolean isPStone(Block block)
    {
	HashMap<Vector, ArrayList<String>> c = chunkLists.get(new Vector(block.getChunk()));
	
	if (c != null)
	    return c.containsKey(new Vector(block));
	
	return false;
    }
    
    /**
     * Whether the block is in a build proteced area owned by someone else
     */
    public boolean isPlaceProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && !c.get(vec).contains(playerName))
		{
		    // the player is in a protected area that he is not allowed
		    // lets see if the area lets strangers build
		    
		    Block source = block.getWorld().getBlockAt(vec.x, vec.y, vec.z);
		    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
		    
		    return psettings != null && psettings.preventPlace;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a break protected area belonging to somebody else (not playerName)
     */
    public boolean isDestroyProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && (playerName == null || !c.get(vec).contains(playerName)))
		{
		    // if in protected area get the settings for the source block and see if it
		    // prevents destroy
		    
		    Block source = block.getWorld().getBlockAt(vec.x, vec.y, vec.z);
		    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
		    
		    return psettings != null && psettings.preventDestroy;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a fire protected area belonging to somebody else (not playerName)
     */
    public boolean isFireProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && (playerName == null || !c.get(vec).contains(playerName)))
		{
		    // if in protected area get the settings for the source block and see if it
		    // prevents fire
		    
		    Block source = block.getWorld().getBlockAt(vec.x, vec.y, vec.z);
		    PStone psettings = source != null ? plugin.pm.getPStoneSettings(source) : null;
		    
		    return psettings != null && psettings.preventFire;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a entry protected area belonging to somebody else (not playerName) Expands the protected area by one to more acurately predict block entry
     */
    public boolean isEntryProtected(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNearPlusOne(block) && (playerName == null || !c.get(vec).contains(playerName)))
		    return true;
	    }
	}
	
	return false;
    }
    
    /**
     * Returns the block that is originating the protective field the block is in
     */
    public HashMap<Vector, Block> getSourcePStone(Block block, String playerName)
    {
	if (block == null)
	    return null;
	
	HashMap<Vector, Block> blocks = new HashMap<Vector, Block>();
	
	// look to see if the block is in a protected zone we are not allowed in
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block) && (playerName == null || !c.get(vec).contains(playerName)))
		    blocks.put(vec, block.getWorld().getBlockAt(vec.getX(), vec.getY(), vec.getZ()));
	    }
	}
	
	return blocks;
    }
    
    /**
     * Returns all pstones of the type
     */
    public HashMap<Vector, Block> getPStonesOfType(int typeid, World world)
    {
	HashMap<Vector, Block> blocks = new HashMap<Vector, Block>();
	
	// look to see if the block is in a protected zone we are not allowed in
	
	for (HashMap<Vector, ArrayList<String>> c : chunkLists.values())
	{
	    if (c != null)
	    {
		for (Vector vec : c.keySet())
		{
		    Block block = world.getBlockAt(vec.getX(), vec.getY(), vec.getZ());
		    
		    if (block.getTypeId() == typeid)
			blocks.put(vec, block);
		}
	    }
	}
	return blocks;
    }
    
    /**
     * Whether the player is standing in a protected area of his
     */
    public boolean isInVector(Block block, String playerName)
    {
	if (block == null)
	    return false;
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
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
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
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
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
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
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    for (Vector vec : c.keySet())
	    {
		if (vec.isNear(block))
		    return c.get(vec);
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
	
	HashMap<Vector, ArrayList<String>> c = getPStonesInArea(block);
	
	if (c != null)
	{
	    if (isPStoneType(block))
	    {
		PStone psettings = getPStoneSettings(block);
		
		if (psettings != null)
		{
		    Vector newStonevec = new Vector(block, psettings.radius, psettings.getHeight());
		    
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
	    Vector vec = new Vector(block);
	    
	    if (c.get(vec) != null && c.get(vec).size() > 0)
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
	    Vector vec = new Vector(block);
	    
	    if (c.get(vec) != null && c.get(vec).size() > 0)
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
	    PStone psettings = getPStoneSettings(block);
	    
	    Vector cvec = new Vector(block.getChunk());
	    HashMap<Vector, ArrayList<String>> c = chunkLists.get(cvec);
	    
	    if (c != null)
	    {
		ArrayList<String> allowed = new ArrayList<String>();
		allowed.add(owner);
		
		c.put(new Vector(block, psettings.radius, psettings.getHeight()), allowed);
	    }
	    else
	    {
		ArrayList<String> allowed = new ArrayList<String>();
		allowed.add(owner);
		
		HashMap<Vector, ArrayList<String>> newc = new HashMap<Vector, ArrayList<String>>();
		newc.put(new Vector(block, psettings.radius, psettings.getHeight()), allowed);
		
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
	deletionQueue.add(new Vector(block));
    }
    
    /**
     * Remove stones from the collection
     */
    public void releaseStone(Vector vec)
    {
	deletionQueue.add(vec);
    }
    
    /**
     * Returns the stones in the chunk and adjacent chunks
     */
    private HashMap<Vector, ArrayList<String>> getPStonesInArea(Block block)
    {
	HashMap<Vector, ArrayList<String>> out = new HashMap<Vector, ArrayList<String>>();
	Chunk chunk = block.getChunk();
	
	int xlow = chunk.getX() - plugin.psettings.chunksInLargestProtectionArea;
	int xhigh = chunk.getX() + plugin.psettings.chunksInLargestProtectionArea;
	int zlow = chunk.getZ() - plugin.psettings.chunksInLargestProtectionArea;
	int zhigh = chunk.getZ() + plugin.psettings.chunksInLargestProtectionArea;
	
	for (int x = xlow; x <= xhigh; x++)
	{
	    for (int z = zlow; z <= zhigh; z++)
	    {
		Chunk chnk = block.getWorld().getChunkAt(x, z);
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
