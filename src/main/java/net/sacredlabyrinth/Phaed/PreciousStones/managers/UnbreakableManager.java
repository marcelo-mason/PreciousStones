package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

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
     */
    public UnbreakableManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Clear out the unbreakables in memory
     */
    public void clearChunkLists()
    {
        chunkLists.clear();
    }

    /**
     * Add stone to the collection
     *
     * @param unbreakableblock
     * @param owner
     * @return
     */
    public boolean add(Block unbreakableblock, Player owner)
    {
        if (plugin.getPlayerManager().getPlayerEntry(owner.getName()).isDisabled())
        {
            return false;
        }

        // deny if world is blacklisted

        if (plugin.getSettingsManager().isBlacklistedWorld(unbreakableblock.getWorld()))
        {
            return false;
        }

        Unbreakable unbreakable = new Unbreakable(unbreakableblock, owner.getName());

        // add unbreakable to memory

        addToCollection(unbreakable);

        // add unbreakable to database

        plugin.getStorageManager().offerUnbreakable(unbreakable, true);
        return true;
    }

    /**
     * Add the unbreakable to the collection held in memory
     *
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
     * Check whether a chunk has unbreakables
     *
     * @param cv
     * @return
     */
    public boolean hasUnbreakables(ChunkVec cv)
    {
        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(cv.getWorld());

        if (w != null)
        {
            HashMap<Vec, Unbreakable> c = w.get(cv);

            if (c != null)
            {
                return !c.isEmpty();
            }

            return false;
        }

        return false;
    }

    /**
     * Retrieve all unbreakables in a chunk
     *
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
     *
     * @param world the world you want the unbreakables from
     * @return all the chunks that match the world
     */
    public HashMap<ChunkVec, HashMap<Vec, Unbreakable>> retrieveUnbreakables(String world)
    {
        return chunkLists.get(world);
    }

    /**
     * Gets the unbreakable from source block
     *
     * @param location
     * @return the unbreakable
     */
    public Unbreakable getUnbreakable(Location location)
    {
        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w = chunkLists.get(location.getWorld().getName());

        if (w != null)
        {
            HashMap<Vec, Unbreakable> c = w.get(new ChunkVec(location));

            if (c != null)
            {
                return c.get(new Vec(location));
            }
        }
        return null;
    }

    /**
     * Gets the unbreakable from source block
     *
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
     *
     * @param unbreakableblock
     * @return confirmation
     */
    public boolean isUnbreakable(Block unbreakableblock)
    {
        return getUnbreakable(unbreakableblock) != null;
    }

    /**
     * Total number of unbreakable stones
     *
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
     *
     * @param world
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
                if (ubs != null)
                {
                    for (Unbreakable unbreakable : ubs.values())
                    {
                        // ensure chunk is loaded prior to polling
/*
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
*/
                        int type = world.getBlockTypeIdAt(unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());

                        if (type != unbreakable.getTypeId())
                        {
                            cleanedCount++;
                            queueRelease(unbreakable);
                        }
                    }
                }
            }
        }
        flush();

        if (cleanedCount != 0)
        {
            PreciousStones.log("countsOrphanedUnbreakables", world.getName(), cleanedCount);
        }
        return cleanedCount;
    }

    /**
     * Revert orphan unbreakables
     *
     * @param world
     * @return
     */
    public int revertOrphans(World world)
    {
        int revertedCount = 0;
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

                    Block block = world.getBlockAt(unbreakable.getX(), unbreakable.getY(), unbreakable.getZ());

                    if (!plugin.getSettingsManager().isUnbreakableType(block))
                    {
                        revertedCount++;
                        block.setTypeId(unbreakable.getTypeId());
                        block.setData((byte)unbreakable.getData());
                    }
                }
            }
        }

        return revertedCount;
    }

    /**
     * Determine whether a player is the owner of the stone
     *
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
     *
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
     *
     * @param vec
     * @param chunkradius
     * @return the unbreakables
     */
    public ArrayList<Unbreakable> getUnbreakablesInArea(Vec vec, int chunkradius)
    {
        ArrayList<Unbreakable> out = new ArrayList<Unbreakable>();

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
     *
     * @param player
     * @param chunkradius
     * @return the unbreakables
     */
    public ArrayList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
        return getUnbreakablesInArea(new Vec(player.getLocation()), chunkradius);
    }

    /**
     * If the block is touching a pstone block
     *
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

                    if (plugin.getSettingsManager().isUnbreakableType(surroundingblock))
                    {
                        if (plugin.getUnbreakableManager().isUnbreakable(surroundingblock))
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
     * Deletes all unbreakables belonging to a player
     *
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
     * Deletes all unbreakables of a certain type
     *
     * @param type
     * @return the count of deleted unbreakables
     */
    public int deleteUnbreakablesOfType(BlockTypeEntry type)
    {
        int deletedCount = 0;

        for (HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Unbreakable> c : w.values())
                {
                    if (c != null)
                    {
                        for (Unbreakable ub : c.values())
                        {
                            if (ub.getTypeEntry().equals(type))
                            {
                                queueRelease(ub);
                                deletedCount++;
                            }
                        }
                    }
                }
            }
        }

        if (deletedCount > 0)
        {
            flush();
        }

        return deletedCount;
    }

    /**
     * Remove stones from the collection
     *
     * @param unbreakableblock
     */
    public void release(Block unbreakableblock)
    {
        Unbreakable ub = getUnbreakable(unbreakableblock);
        deleteUnbreakable(ub);
    }

    /**
     * Adds to deletion queue
     *
     * @param unbreakableblock
     */
    public void queueRelease(Block unbreakableblock)
    {
        deletionQueue.add(getUnbreakable(unbreakableblock));
    }

    /**
     * Adds to deletion queue
     *
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
     *
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

        plugin.getStorageManager().offerUnbreakable(ub, false);
    }


    /**
     * Changes username of all unbreakables to a new one
     *
     * @param oldName
     * @param newName
     */
    public void migrateUsername(String oldName, String newName)
    {
        for (HashMap<ChunkVec, HashMap<Vec, Unbreakable>> w : chunkLists.values())
        {
            for (HashMap<Vec, Unbreakable> ubs : w.values())
            {
                for (Unbreakable ub : ubs.values())
                {
                    if (ub.getOwner().equalsIgnoreCase(oldName))
                    {
                        ub.setOwner(newName);
                        plugin.getStorageManager().offerUnbreakable(ub, false);
                    }
                }
            }
        }
    }
}
