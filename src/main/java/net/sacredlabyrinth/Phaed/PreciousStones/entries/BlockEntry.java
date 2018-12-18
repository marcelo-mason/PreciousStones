package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class BlockEntry {
    private final Material type;
    private final Location location;

    /**
     * @param block
     */
    public BlockEntry(Block block) {
        this.type = block.getType();
        this.location = block.getLocation();
    }

    /**
     * @param loc
     * @param type
     */
    public BlockEntry(Location loc, Material type) {
        this.type = type;
        this.location = loc;
    }

    public Material getType() {
        return type;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the block
     */
    public Block getBlock() {
        return location.getWorld().getBlockAt(location);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockEntry)) {
            return false;
        }

        BlockEntry other = (BlockEntry) obj;
        return other.getType() == this.getType() && Helper.isSameBlock(this.getLocation(), other.getLocation());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getType().ordinal();
        hash = 47 * hash + this.getLocation().getBlockX() + this.getLocation().getBlockY() + this.getLocation().getBlockZ();
        return hash;
    }

    @Override
    public String toString() {
        return "[" + getType().name() + " " + Helper.toLocationString(location) + "]";
    }
}
