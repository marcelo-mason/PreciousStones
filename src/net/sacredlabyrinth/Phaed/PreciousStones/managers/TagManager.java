package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.List;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author phaed
 */
public class TagManager
{
    private PreciousStones plugin;

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

    public void tagChunk(ChunkVec cv)
    {
        plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setType(Material.OBSIDIAN);
        plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 1, cv.getZ() << 4).setType(Material.BEDROCK);
    }

    public void untagChunk(ChunkVec cv)
    {
        plugin.getServer().getWorld(cv.getWorld()).getBlockAt(cv.getX() << 4, 0, cv.getZ() << 4).setType(Material.BEDROCK);
    }

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

    public void tagWorld(String worldName)
    {
        List<ChunkVec> chunks = plugin.ffm.retrieveChunks(worldName);
        chunks.addAll(plugin.um.retrieveChunks(worldName));

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
