package net.sacredlabyrinth.Phaed.PreciousStones.blocks;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.AbstractVec;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class Unbreakable extends AbstractVec {
    private String owner;
    private BlockTypeEntry type;
    private boolean dirty;

    /**
     * @param x
     * @param y
     * @param z
     * @param world
     * @param owner
     */
    public Unbreakable(int x, int y, int z, String world, BlockTypeEntry type, String owner) {
        super(x, y, z, world);

        this.owner = owner;
        this.type = type;
        this.dirty = true;
    }

    /**
     * @param block
     * @param owner
     */
    public Unbreakable(Block block, String owner) {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.owner = owner;
        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
        this.dirty = true;
    }

    /**
     * @param block
     */
    public Unbreakable(Block block) {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
        this.dirty = true;
    }

    /**
     * @return
     */
    public int getTypeId() {
        return this.type.getTypeId();
    }

    /**
     * @return the block data
     */
    public byte getData() {
        return type.getData();
    }

    /**
     * @return the type entry
     */
    public BlockTypeEntry getTypeEntry() {
        return type;
    }

    /**
     * @return
     */
    public String getType() {
        return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     * @return
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName) {
        return playerName.equals(getOwner());
    }

    @Override
    public String toString() {
        return super.toString() + " [owner:" + getOwner() + "]";
    }

    /**
     * @return the dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @param dirty the dirty to set
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getDetails() {
        return "[" + getType() + "|" + getX() + " " + getY() + " " + getZ() + "]";
    }
}
