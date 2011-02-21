package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

/**
 * Handles unbreakable blocks
 * 
 * @author Phaed
 */
public class UnbreakableManager
{
    protected final HashMap<ChunkVec, LinkedList<Unbreakable>> chunkLists = new HashMap<ChunkVec, LinkedList<Unbreakable>>();
    
    private Queue<Unbreakable> deletionQueue = new LinkedList<Unbreakable>();
    private PreciousStones plugin;
    private boolean dirty = false;
    
    public UnbreakableManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    /**
     * Whether we need to save
     */
    public boolean isDirty()
    {
	return dirty;
    }
    
    /**
     * force dirty
     */
    public void setDirty()
    {
	dirty = true;
    }
    
    /**
     * reset dirty
     */
    public void resetDirty()
    {
	dirty = false;
    }
    
    /**
     * Exposes the chunklist
     */
    public HashMap<ChunkVec, LinkedList<Unbreakable>> getChunkLists()
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
	    Unbreakable pending = deletionQueue.poll();
	    
	    LinkedList<Unbreakable> chunkunbreakables = chunkLists.get(pending.getChunkVec());
	    
	    if (chunkunbreakables != null)
	    {
		chunkunbreakables.remove(pending);
	    }
	    setDirty();
	}
    }
    
    /**
     * Total number of unbreakable stones
     */
    public int count()
    {
	int size = 0;
	
	for (LinkedList<Unbreakable> c : chunkLists.values())
	{
	    size += c.size();
	}
	return size;
    }
    
    /**
     * Gets the unbreakable from source block
     */
    public Unbreakable getUnbreakable(Block unbreakableblock)
    {
	LinkedList<Unbreakable> c = chunkLists.get(new ChunkVec(unbreakableblock.getChunk()));
	
	if (c != null)
	{
	    int index = c.indexOf(new Vec(unbreakableblock));
	    
	    if (index > -1)
	    {
		return (c.get(index));
	    }
	}
	return null;
    }
    
    /**
     * Looks for the block in our unbreakable collection
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
	{
	    return unbreakable.isOwner(playerName);
	}
	return false;
    }
    
    /**
     * Return the owner of a stone
     */
    public String getOwner(Block unbreakableblock)
    {
	Unbreakable unbreakable = getUnbreakable(unbreakableblock);
	
	if (unbreakable != null)
	{
	    return unbreakable.getOwner();
	}
	return "";
    }
    
    /**
     * Returns the unbreakable blocks in the chunk and adjacent chunks
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Block blockInArea, int chunkradius)
    {
	LinkedList<Unbreakable> out = new LinkedList<Unbreakable>();
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
		    LinkedList<Unbreakable> c = chunkLists.get(new ChunkVec(chnk));
		    
		    if (c != null)
		    {
			out.addAll(c);
		    }
		}
	    }
	}
	
	return out;
    }
    
    /**
     * Returns the unbreakable blocks in the chunk and adjacent chunks
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
	Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getUnbreakablesInArea(blockInArea, chunkradius);
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
		    {
			continue;
		    }
		    
		    Block surroundingblock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
		    
		    if (plugin.settings.isUnbreakableType(surroundingblock))
		    {
			return true;
		    }
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Add stone to the collection
     */
    public void add(Block unbreakableblock, String owner)
    {
	ChunkVec chunkvec = new ChunkVec(unbreakableblock.getChunk());
	Unbreakable unbreakable = new Unbreakable(unbreakableblock, owner);
	
	LinkedList<Unbreakable> c = chunkLists.get(chunkvec);
	
	if (c != null)
	{
	    if (!c.contains(unbreakable))
	    {
		c.add(unbreakable);
	    }
	}
	else
	{
	    LinkedList<Unbreakable> newc = new LinkedList<Unbreakable>();
	    newc.add(unbreakable);
	    chunkLists.put(chunkvec, newc);
	}
	setDirty();
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Block unbreakableblock)
    {
	LinkedList<Unbreakable> c = chunkLists.get(new ChunkVec(unbreakableblock.getChunk()));
	
	c.remove(new Vec(unbreakableblock));
	setDirty();
    }
    
    /**
     * Adds to deletion queue
     */
    public void queueRelease(Block unbreakableblock)
    {
	deletionQueue.add(new Unbreakable(unbreakableblock));
    }
}
