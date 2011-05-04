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
    private final HashMap<ChunkVec, LinkedList<Unbreakable>> chunkLists = new HashMap<ChunkVec, LinkedList<Unbreakable>>();

    private Queue<Unbreakable> deletionQueue = new LinkedList<Unbreakable>();
    private PreciousStones plugin;
    private boolean dirty = false;

    /**
     *
     * @param plugin
     */
    public UnbreakableManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }

    /**
     * Retrieve a copy of the chunk list
     * @return
     */
    public HashMap<ChunkVec, LinkedList<Unbreakable>> getChunks()
    {
	HashMap<ChunkVec, LinkedList<Unbreakable>> out = new HashMap<ChunkVec, LinkedList<Unbreakable>>();
	out.putAll(chunkLists);
	return out;
    }

    /**
     * Import chunks to the chunklist
     * @param chunks
     */
    public void importChunks(HashMap<ChunkVec, LinkedList<Unbreakable>> chunks)
    {
	chunkLists.putAll(chunks);
    }

    /**
     * Whether we need to save
     * @return
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
     * @return
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
     * Clean up orphan fields
     * @return
     */
    public int cleanOrphans()
    {
	int cleanedCount = 0;

	for (LinkedList<Unbreakable> c : chunkLists.values())
	{
	    for (Unbreakable unbreakable : c)
	    {
		if (plugin.getServer().getWorld(unbreakable.getWorld()) == null)
		{
		    cleanedCount++;
		    queueRelease(unbreakable);
		}

		Block block = plugin.getServer().getWorld(unbreakable.getWorld()).getBlockAt(unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());

		if (!plugin.settings.isUnbreakableType(block))
		{
		    cleanedCount++;
		    queueRelease(unbreakable);
		}
	    }
	}
	flush();

	return cleanedCount;
    }


    /**
     * Gets the unbreakable from source block
     * @param unbreakableblock
     * @return
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
     * @param unbreakableblock
     * @return
     */
    public boolean isUnbreakable(Block unbreakableblock)
    {
	return getUnbreakable(unbreakableblock) != null;
    }

    /**
     * Determine whether a player is the owner of the stone
     * @param unbreakableblock
     * @param playerName
     * @return
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
     * @param unbreakableblock
     * @return
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
     * @param blockInArea
     * @param chunkradius
     * @return
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
     * @param player
     * @param chunkradius
     * @return
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
	Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getUnbreakablesInArea(blockInArea, chunkradius);
    }

    /**
     * If the block is touching a pstone block
     * @param block
     * @return
     */
    public Block touchingUnbrakableBlock(Block block)
    {
	if (block == null)
	    return null;

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
			if (plugin.um.isUnbreakable(surroundingblock))
			{
			    return surroundingblock;
			}
		    }
		}
	    }
	}

	return null;
    }

    /**
     * Add stone to the collection
     * @param unbreakableblock
     * @param owner
     * @return
     */
    public boolean add(Block unbreakableblock, Player owner)
    {
	if (plugin.plm.isDisabled(owner))
	{
	    return false;
	}

	ChunkVec chunkvec = new ChunkVec(unbreakableblock.getChunk());
	Unbreakable unbreakable = new Unbreakable(unbreakableblock, owner.getName());

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
	return true;
    }

    /**
     * Remove stones from the collection
     * @param unbreakableblock
     */
    public void release(Block unbreakableblock)
    {
	LinkedList<Unbreakable> c = chunkLists.get(new ChunkVec(unbreakableblock.getChunk()));

	c.remove(new Vec(unbreakableblock));
	setDirty();
    }

    /**
     * Adds to deletion queue
     * @param unbreakableblock
     */
    public void queueRelease(Block unbreakableblock)
    {
	deletionQueue.add(new Unbreakable(unbreakableblock));
    }

    /**
     * Adds to deletion queue
     * @param unbreakable
     */
    public void queueRelease(Unbreakable unbreakable)
    {
	deletionQueue.add(unbreakable);
    }
}
