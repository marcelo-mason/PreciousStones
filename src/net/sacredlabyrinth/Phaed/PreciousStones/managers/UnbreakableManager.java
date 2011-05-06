package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

import org.bukkit.Chunk;
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
public class UnbreakableManager
{
    private final HashMap<ChunkVec, LinkedList<Unbreakable>> chunkLists = new HashMap<ChunkVec, LinkedList<Unbreakable>>();
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
     * Loads all unbreakables for a specific world into memory
     * @param world
     */
    public void loadWorld(String world)
    {
        int orphans = cleanOrphans(world);

        if (orphans > 0)
        {
            PreciousStones.log(Level.INFO, "[{0}] {1} orphan unbreakables: {2}", plugin.getDescription().getName(), world, orphans);
        }

        int ubs = importFromDatabase(world);

        PreciousStones.log(Level.INFO, "[{0}] {1} unbreakables: {2}", plugin.getDescription().getName(), world, ubs);
    }

    /**
     *
     * @param world
     * @return imports all unbreakables from the database
     */
    public int importFromDatabase(String world)
    {
        List<Unbreakable> ubs = plugin.getDatabase().find(Unbreakable.class).where().ieq("world", world).orderBy("x").orderBy("z").findList();

        for (Unbreakable ub : ubs)
        {
            LinkedList<Unbreakable> c = chunkLists.get(ub.toChunkVec());

            if (c != null)
            {
                if(!c.contains(ub))
                {
                    c.add(ub);
                }
            }
            else
            {
                LinkedList<Unbreakable> newc = new LinkedList<Unbreakable>();
                newc.add(ub);
                chunkLists.put(ub.toChunkVec(), newc);
            }
        }

        return ubs.size();
    }

    /**
     * Saves all unbreakables on DB
     */
    public void saveAll()
    {
        for (ChunkVec cv : chunkLists.keySet())
        {
            LinkedList<Unbreakable> list = chunkLists.get(cv);

            for(Unbreakable ub : list)
            {
                saveUnbreakable(ub);
            }
        }
    }

    /**
     * Save an unbreakable back into the database
     * @param ub
     */
    public void saveUnbreakable(Unbreakable ub)
    {
        try
        {
            plugin.getDatabase().save(ub);
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error saving unbreakable: {0}", ex.getMessage());
        }
    }

    /**
     * Delete an unbreakable from memory and from the database
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        LinkedList<Unbreakable> c = chunkLists.get(ub.toChunkVec());

        if (c != null)
        {
            c.remove(ub);
        }

        try
        {
            plugin.getDatabase().delete(ub);
        }
        catch (Exception ex)
        {
        }
    }

    /**
     *
     * @param cv
     * @return all unbreakables from database that match the chunkvec
     */
    public List<Unbreakable> retrieveUnbreakables(ChunkVec cv)
    {
        return chunkLists.get(cv);
    }

    /**
     *
     * @param world
     * @return all unbreakables from the database that match the world
     */
    public List<Unbreakable> retrieveUnbreakables(String world)
    {
        List<Unbreakable> out = new LinkedList<Unbreakable>();

        for (ChunkVec cv : chunkLists.keySet())
        {
            if (cv.getWorld().equalsIgnoreCase(world))
            {
                out.addAll(chunkLists.get(cv));
            }
        }

        return out;
    }

    /**
     * Gets the unbreakable from source block
     * @param block
     * @return
     */
    public Unbreakable getUnbreakable(Block block)
    {
        LinkedList<Unbreakable> c = chunkLists.get(new ChunkVec(block));

        if (c != null)
        {
            int index = c.indexOf(new Vec(block));

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
     * Total number of unbreakable stones
     * @return
     */
    public int getCount()
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
     * @param worldName
     * @return
     */
    public int cleanOrphans(String worldName)
    {
        int cleanedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        World world = plugin.getServer().getWorld(worldName);

        if (world == null)
        {
            return 0;
        }

        List<Unbreakable> ubs = retrieveUnbreakables(world.getName());

        for (Unbreakable unbreakable : ubs)
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

        flush();

        return cleanedCount;
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
     * @param vec
     * @param chunkradius
     * @return
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
                List<Unbreakable> ubs = retrieveUnbreakables(new ChunkVec(x, z, vec.getWorld()));

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
     * @return
     */
    public LinkedList<Unbreakable> getUnbreakablesInArea(Player player, int chunkradius)
    {
        return getUnbreakablesInArea(new Vec(player.getLocation()), chunkradius);
    }

    /**
     * If the block is touching a pstone block
     * @param block
     * @return
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
        saveUnbreakable(unbreakable);
        return true;
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

            plugin.getDatabase().delete(pending);
        }
    }
}
