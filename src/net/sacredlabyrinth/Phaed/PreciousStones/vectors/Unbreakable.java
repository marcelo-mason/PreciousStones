package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author phaed
 */

public class Unbreakable extends AbstractVec
{
    private String owner;
    private int typeId;
    private boolean dirty;
    private long id;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param world
     * @param typeId
     * @param owner
     */
    public Unbreakable(long id, int x, int y, int z, String world, int typeId, String owner)
    {
	super(x, y, z, world);

        this.id = id;
	this.owner = owner;
	this.typeId = typeId;
        this.dirty = true;
    }

    /**
     *
     * @param block
     * @param owner
     */
    public Unbreakable(Block block, String owner)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

	this.owner = owner;
	this.typeId = block.getTypeId();
        this.dirty = true;
    }

    /**
     *
     * @param block
     */
    public Unbreakable(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

	this.typeId = block.getTypeId();
        this.dirty = true;
    }

    /**
     *
     * @return
     */
    public int getTypeId()
    {
	return this.typeId;
    }

    /**
     *
     * @return
     */
    public String getType()
    {
	return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     *
     * @return
     */
    public ChunkVec toChunkVec()
    {
	return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }

    /**
     *
     * @return
     */
    public String getOwner()
    {
	return owner;
    }

    /**
     *
     * @param owner
     */
    public void setOwner(String owner)
    {
	this.owner = owner;
    }

    /**
     *
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName)
    {
	return playerName.equals(getOwner());
    }

    @Override
    public String toString()
    {
	return super.toString() + " [owner:" + getOwner() + "]";
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    /**
     * @return the dirty
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * @param dirty the dirty to set
     */
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    /**
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id)
    {
        this.id = id;
    }
}
