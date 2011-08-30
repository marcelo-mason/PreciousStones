package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author phaed
 */
public final class TagManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public TagManager()
    {
        plugin = PreciousStones.getInstance();
        untagWorlds();
    }

    /**
     *
     */
    public void untagWorlds()
    {
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds)
        {
            untagWorld(world.getName());
        }
    }

    /**
     * Un-tags the chunk, used when there are no more pstones
     * @param cv
     */
    public void untagChunk(ChunkVec cv)
    {
        World world = plugin.getServer().getWorld(cv.getWorld());

        if (inAir(world.getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4)))
        {
            world.getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setTypeId(0);
        }
        else
        {
            world.getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setTypeId(7);
        }

        if (inAir(world.getBlockAt(cv.getX() << 4, 1, cv.getZ() << 4)))
        {
            world.getBlockAt(cv.getX() << 4, 1, cv.getZ() << 4).setTypeId(0);
        }
        else
        {
            world.getBlockAt(cv.getX() << 4, 1, cv.getZ() << 4).setTypeId(7);
        }
    }

    private boolean inAir(Block block)
    {
        if (block.getRelative(BlockFace.EAST).getTypeId() == 0 || block.getRelative(BlockFace.WEST).getTypeId() == 0 || block.getRelative(BlockFace.NORTH).getTypeId() == 0 || block.getRelative(BlockFace.SOUTH).getTypeId() == 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Check whether the world's pstones have been initially tagged
     * @param worldName
     * @return
     */
    public boolean isTaggedWorld(String worldName)
    {
        World world = plugin.getServer().getWorld(worldName);

        int type = world.getBlockTypeIdAt(8, 0, 8);

        if (type == 49)
        {
            return true;
        }
        return false;
    }

    /**
     * Tags all of the worlds pstone chunks
     * @param worldName
     */
    public void untagWorld(String worldName)
    {
        if (!isTaggedWorld(worldName))
        {
            return;
        }

        Set<ChunkVec> chunks = new HashSet<ChunkVec>();

        HashMap<ChunkVec, HashMap<Vec, Field>> c = plugin.getForceFieldManager().getFields(worldName);

        if (c != null)
        {
            chunks.addAll(c.keySet());
        }

        HashMap<ChunkVec, HashMap<Vec, Unbreakable>> u = plugin.getUnbreakableManager().retrieveUnbreakables(worldName);

        if (u != null)
        {
            chunks.addAll(u.keySet());
        }

        if (chunks.size() > 0)
        {
            PreciousStones.log("untagging {0} chunks", chunks.size());
        }

        World world = plugin.getServer().getWorld(worldName);

        ChunkVec currentChunk = null;
        boolean currentChunkLoaded = false;

        for (ChunkVec cv : chunks)
        {
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

            untagChunk(cv);
        }

        // untag world

        if (inAir(world.getBlockAt(8, 0, 8)))
        {
            world.getBlockAt(8, 0, 8).setTypeId(0);
        }
        else
        {
            world.getBlockAt(8, 0, 8).setTypeId(7);
        }

        if (inAir(world.getBlockAt(8, 1, 8)))
        {
            world.getBlockAt(8, 1, 8).setTypeId(0);
        }
        else
        {
            world.getBlockAt(8, 1, 8).setTypeId(7);
        }
    }
}
