package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public class TagManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public TagManager(PreciousStones plugin)
    {
        this.plugin = plugin;
        checkTaggedWorlds();
    }

    private void checkTaggedWorlds()
    {
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds)
        {
            if (!isTaggedWorld(world.getName()))
            {
                tagWorld(world.getName());
            }
        }
    }

    /**
     * Tags the chunk, used when a pstone is placed on the chunk
     * @param cv
     */
    public void tagChunk(ChunkVec cv)
    {
        plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setType(Material.OBSIDIAN);
        plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 1, cv.getZ() << 4).setType(Material.BEDROCK);
    }

    /**
     * Un-tags the chunk, used when there are no more pstones
     * @param cv
     */
    public void untagChunk(ChunkVec cv)
    {
        if (!plugin.tm.containsPStones(cv))
        {
            plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setType(Material.BEDROCK);
        }
    }

    /**
     * Whether the chunk contains any pstones
     * @param cv
     * @return Whether the chunk contains any pstones
     */
    public boolean containsPStones(ChunkVec cv)
    {
        return plugin.ffm.hasField(cv) || plugin.um.hasUnbreakable(cv);
    }

    /**
     * Check if the area around the chunk contains a tagged chunk
     * @param cv
     * @return whether the area around the chunk contains a tagged chunk
     */
    public boolean isTaggedArea(ChunkVec cv)
    {
        World world = plugin.getServer().getWorld(cv.getWorld());

        int type = world.getBlockTypeIdAt(cv.getX() << 4, 0, cv.getZ() << 4);

        if (type == 49)
        {
            return true;
        }

        int xlow = cv.getX() - plugin.settings.chunksInLargestForceFieldArea;
        int xhigh = cv.getX() + plugin.settings.chunksInLargestForceFieldArea;
        int zlow = cv.getZ() - plugin.settings.chunksInLargestForceFieldArea;
        int zhigh = cv.getZ() + plugin.settings.chunksInLargestForceFieldArea;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                if (x == cv.getX() && z == cv.getZ())
                {
                    continue;
                }

                type = world.getBlockTypeIdAt(x << 4, 0, z << 4);

                if (type == 49)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the world's pstones have been initially  tagged
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
    public void tagWorld(String worldName)
    {
        Set<ChunkVec> chunks = new HashSet<ChunkVec>();

        HashMap<ChunkVec, LinkedList<Field>> c = plugin.ffm.retrieveFields(worldName);

        if (c != null)
        {
            chunks.addAll(c.keySet());
        }

        HashMap<ChunkVec, LinkedList<Unbreakable>> u = plugin.um.retrieveUnbreakables(worldName);

        if (u != null)
        {
            chunks.addAll(u.keySet());
        }

        PreciousStones.log(Level.INFO, "Tagging {0} chunks", chunks.size());

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

            tagChunk(cv);
        }

        // tag world

        world.getBlockAt(8, 0, 8).setType(Material.OBSIDIAN);
        world.getBlockAt(8, 1, 8).setType(Material.BEDROCK);
    }
}
