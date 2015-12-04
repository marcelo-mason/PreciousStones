package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class ChunkVec extends AbstractVec {
    /**
     * @param chunk
     */
    public ChunkVec(Chunk chunk) {
        super(chunk.getX(), 0, chunk.getZ(), chunk.getWorld().getName());
    }

    /**
     * @param block
     */
    public ChunkVec(Block block) {
        super(block.getChunk().getX(), 0, block.getChunk().getZ(), block.getWorld().getName());
    }

    /**
     * @param location
     */
    public ChunkVec(Location location) {
        super(location.getChunk().getX(), 0, location.getChunk().getZ(), location.getWorld().getName());
    }

    /**
     * @param x
     * @param z
     * @param world
     */
    public ChunkVec(int x, int z, String world) {
        super(x, 0, z, world);
    }
}
