package net.sacredlabyrinth.Phaed.PreciousStones.managers;

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
    private Queue<Unbreakable> deletionQueue = new LinkedList<Unbreakable>();
    private PreciousStones plugin;
    private boolean dirty = false;

    /**
     *
     * @param plugin all unbreakables from the database
     */
    public UnbreakableManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param cv
     * @return all unbreakables form the database that match the chunkvec
     */
    public List<Unbreakable> retrieveUnbreakables(ChunkVec cv)
    {
        return plugin.getDatabase().find(Unbreakable.class).where().eq("chunkX", cv.getX()).eq("chunkZ", cv.getZ()).ieq("world", cv.getWorld()).findList();
    }

    /**
     *
     * @param world
     * @return all unbreakables from the database that match the world
     */
    public List<Unbreakable> retrieveUnbreakables(String world)
    {
        return plugin.getDatabase().find(Unbreakable.class).where().ieq("world", world).findList();
    }

    /**
     *
     * @return
     */
    public List<Unbreakable> retrieveUnbreakables()
    {
        return plugin.getDatabase().find(Unbreakable.class).orderBy("chunkX").orderBy("chunkZ").findList();
    }

    /**
     *
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
     *
     * @param ub
     */
    public void deleteUnbreakable(Unbreakable ub)
    {
        try
        {
            plugin.getDatabase().delete(ub);
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error saving unbreakable: {0}", ex.getMessage());
        }
    }

    /**
     * Process pending deletions
     */
    public void flush()
    {
        while (deletionQueue.size() > 0)
        {
            Unbreakable pending = deletionQueue.poll();

            plugin.getDatabase().delete(pending);
        }
    }

    /**
     * Total number of unbreakable stones
     * @return
     */
    public int getCount()
    {
        return plugin.getDatabase().find(Unbreakable.class).findRowCount();
    }

    /**
     * Gets the unbreakable from source block
     * @param block
     * @return
     */
    public Unbreakable getUnbreakable(Block block)
    {
        return plugin.getDatabase().find(Unbreakable.class).where().eq("x", block.getX()).eq("y", block.getY()).eq("z", block.getZ()).ieq("world", block.getWorld().getName()).findUnique();
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

        List<Unbreakable> ubs = plugin.um.retrieveUnbreakables(world.getName());

        for (Unbreakable unbreakable : ubs)
        {
            // ensure chunk is loaded prior to polling

            ChunkVec cv = unbreakable.getChunkVec();

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
                    List<Unbreakable> ubs = retrieveUnbreakables(new ChunkVec(chnk));

                    if (ubs != null)
                    {
                        out.addAll(ubs);
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
}
