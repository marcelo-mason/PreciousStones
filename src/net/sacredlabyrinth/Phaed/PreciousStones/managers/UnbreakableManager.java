package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.World;

/**
 * Handles unbreakable blocks
 *
 * @author Phaed
 */
public final class UnbreakableManager
{
    private final HashMap<String, HashMap<ChunkVec, HashMap<Vec, Unbreakable>>> chunkLists = new HashMap<String, HashMap<ChunkVec, HashMap<Vec, Unbreakable>>>();
    private Queue<Unbreakable> deletionQueue = new LinkedList<Unbreakable>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin all unbreakables from the database
     */
    public UnbreakableManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Check if a chunk contains an unbreakable
     * @param cv the chunk vec
     * @return whether the chunk contains unbreakables
     */
    public boolean hasUnbreakable(ChunkVec cv)
    {
        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(cv.getWorld());

        if (w != null)
        {
            if (w.containsKey(cv))
            {
                HashMap<Vec, Unbreakable> c = w.get(cv);

                if (!c.isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieve all unbreakables in a chunk
     * @param cv the chunk vec
     * @return all unbreakables from database that match the chunkvec
     */
    public Collection<Unbreakable> retrieveUnbreakables(ChunkVec cv)
    {
        if (chunkLists.get(cv.getWorld()) == null)
        {
            return null;
        }

        if (chunkLists.get(cv.getWorld()).get(cv) == null)
        {
            return null;
        }

        return chunkLists.get(cv.getWorld()).get(cv).values();
    }

    /**
     * Retrieve all chunks in collection
     * @param world the world you want the unbreakables from
     * @return all the chunks that match the world
     */
    public HashMap<ChunkVec, HashMap<Vec, Unbreakable>> retrieveUnbreakables(String world)
    {
        return chunkLists.get(world);
    }

    /**
     * Gets the unbreakable from source block
     * @param block
     * @return the unbreakable
     */
    public Unbreakable getUnbreakable(Block block)
    {
        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(block.getLocation().getWorld().getName());

        if (w != null)
        {
            HashMap<Vec, Unbreakable> c = w.get(new ChunkVec(block));

            if (c != null)
            {
                Unbreakable ub = c.get(new Vec(block));

                if (ub != null)
                {
                    if (ub.getTypeId() != block.getTypeId())
                    {
                        deleteUnbreakable(ub);
                        return null;
                    }
                }

                return ub;
            }
        }
        return null;
    }

    /**
     * Looks for the block in our unbreakable collection
     * @param unbreakableblock
     * @return confirmation
     */
    public boolean isUnbreakable(Block unbreakableblock)
    {
        return getUnbreakable(unbreakableblock) != null;
    }

    /**
     * Total number of unbreakable stones
     * @return the count
     */
    public int getCount()
    {
        int size = 0;

        for (HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Unbreakable> c : w.values())
                {
                    size += c.size();
                }
            }
        }
        return size;
    }

    /**
     * Clean up orphan unbreakables
     * @param worldName the world name
     * @return
     */
    public int cleanOrphans(World world)
    {
        int cleanedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = retrieveUnbreakables(world.getName());

        if (w != null)
        {
            for (HashMap<Vec, Unbreakable> ubs : w.values())
            {
                for (Unbreakable unbreakable : ubs.values())
                {
                    // ensure chunk is loaded prior to polling

                    ChunkVec cv = unbreakable.toChunkVec();

                    if (!cv.equals(currentChunk))
                    {
                        if (!currentChunkLoaded)
                        {
                            if (currentChunk != null)
                            {
                                world.unloadChunk(currentChunk.getX(), currentChunk.getZ());
                            }
                        }

                        currentChunkLoaded = world.isChunkLoaded(cv.getX(), cv.getZ());

                        if (!currentChunkLoaded)
                        {
                            world.loadChunk(cv.getX(), cv.getZ());
                        }

                        currentChunk = cv;
                    }

                    int type = world.getBlockTypeIdAt(unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());

                    if (!plugin.settings.isUnbreakableType(type))
                    {
                        cleanedCount++;
                        queueRelease(unbreakable);
                    }
                }
            }
        }
        flush();

        if (cleanedCount != 0)
        {
            PreciousStones.log(Level.INFO, "({0}) orphan-unbreakables: {1}", world.getName(), cleanedCount);
        }
        return cleanedCount;
    }

    /**
     * Determine whether a player is the owner of the stone
     * @param unbreakableblock
     * @param playerName
     * @return confirmation
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
     * @return the owner's name
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
     * @param vec
     * @param chunkradius
     * @return the unbreakables
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Vec vec, int chunkradius)
    {
        LinkedList<Unbreakable> out = new LinkedList<Unbreakable>();

        int xlow = (vec.getX() >> 4) - chunkradius;
        int xhigh = (vec.getX() >> 4) + chunkradius;
        int zlow = (vec.getZ() >> 4) - chunkradius;
        int zhigh = (vec.getZ() >> 4) + chunkradius;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                Collection<Unbreakable> ubs = retrieveUnbreakables(new ChunkVec(x, z, vec.getWorld()));

                if (ubs != null)
                {
                    out.addAll(ubs);
                }
            }
        }

        return out;
    }

    /**
     * Returns the unbreakable blocks in the chunk and adjacent chunks
     * @param player
     * @param chunkradius
     * @return the unbreakables
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
        return getUnbreakablesInArea(new Vec(player.getLocation()), chunkradius);
    }

    /**
     * If the block is touching a pstone block
     * @param block
     * @return the block
     */
    public Block touchingUnbrakableBlock(Block block)
    {
        if (block == null)
        {
            return null;
        }

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
     * Whether the piston could displace a pstone
     * @param piston
     * @param placer
     * @return
     */
    public Unbreakable getPistonConflict(Block piston, Player placer)
    {
        for (int z = -15; z <= 15; z++)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    Block block = piston.getRelative(x, y, z);
                    Unbreakable ub = getUnbreakable(block);

                    if (ub != null)
                    {
                        return ub;
                    }
                }
            }
        }

        for (int z = -1; z <= 1; z++)
        {
            for (int x = -15; x <= 15; x++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    Block block = piston.getRelative(x, y, z);
                    Unbreakable ub = getUnbreakable(block);

                    if (ub != null)
                    {
                        return ub;
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

        Unbreakable unbreakable = new Unbreakable(unbreakableblock, owner.getName());

        // add unbreakable to memory

        addToCollection(unbreakable);

        // add unbreakable to database

        plugin.sm.insertUnbreakable(unbreakable);

        // tag the chunk

        plugin.tm.tagChunk(unbreakable.toChunkVec());
        return true;
    }

    /**
     * Add the unbreakable to the collection held in memory
     * @param ub the unbreakable
     */
    public void addToCollection(Unbreakable ub)
    {
        String world = ub.getWorld();
        ChunkVec chunkvec = ub.toChunkVec();

        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(world);

        if (w != null)
        {
            HashMap<Vec, Unbreakable> c = w.get(chunkvec);

            if (c != null)
            {
                c.put(ub.toVec(), ub);
            }
            else
            {
                HashMap<Vec, Unbreakable> newc = new HashMap<Vec, Unbreakable>();
                newc.put(ub.toVec(), ub);
                w.put(chunkvec, newc);
            }
        }
        else
        {
            HashMap<ChunkVec, HashMap<Vec, Unbreakable>> _w = new HashMap<ChunkVec, HashMap<Vec, Unbreakable>>();
            HashMap<Vec, Unbreakable> _c = new HashMap<Vec, Unbreakable>();

            _c.put(ub.toVec(), ub);
            _w.put(chunkvec, _c);
            chunkLists.put(world, _w);
        }
    }

    /**
     * Deletes all unbreakables belonging to a player
     * @param playerName the players
     * @return the count of deleted unbreakables
     */
    public int deleteBelonging(String playerName)
    {
        int deletedUbs = 0;

        for (HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w : chunkLists.values())
        {
            for (HashMap<Vec, Unbreakable> ubs : w.values())
            {
                for (Unbreakable ub : ubs.values())
                {
                    if (ub.getOwner().equalsIgnoreCase(playerName))
                    {
                        queueRelease(ub);
                        deletedUbs++;
                    }
                }
            }
        }

        flush();

        return deletedUbs;
    }

    /**
     * Remove stones from the collection
     * @param unbreakableblock
     */
    public void release(Block unbreakableblock)
    {
        Unbreakable ub = getUnbreakable(unbreakableblock);
        deleteUnbreakable(ub);
    }

    /**
     * Adds to deletion queue
     * @param unbreakableblock
     */
    public void queueRelease(Block unbreakableblock)
    {
        deletionQueue.add(getUnbreakable(unbreakableblock));
    }

    /**
     * Adds to deletion queue
     * @param unbreakable
     */
    public void queueRelease(Unbreakable unbreakable)
    {
        deletionQueue.add(unbreakable);
    }

    /**
     * Delete unbreakables in deletion queue
     */
    public void flush()
    {
        while (deletionQueue.size() > 0)
        {
            Unbreakable pending = deletionQueue.poll();
            deleteUnbreakable(pending);
        }
    }

    /**
     * Delete an unbreakable from memory and from the database
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(ub.getWorld());

        if (w != null)
        {
            HashMap<Vec, Unbreakable> c = w.get(ub.toChunkVec());

            if (c != null)
            {
                c.remove(ub);
            }
        }

        // delete unbreakable form database

        plugin.sm.deleteUnbreakable(ub);

        // untag the chunk

        plugin.tm.untagChunk(ub.toChunkVec());
    }
}
