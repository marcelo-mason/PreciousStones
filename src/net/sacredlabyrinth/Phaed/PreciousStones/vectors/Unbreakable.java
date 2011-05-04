package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author cc_madelg
 */
public class Unbreakable extends AbstractVec
{
    private String owner;
    private int typeId;
    private ChunkVec chunkvec;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param chunkvec
     * @param world
     * @param typeId
     * @param owner
     */
    public Unbreakable(int x, int y, int z, ChunkVec chunkvec, String world, int typeId, String owner)
    {
	super(x, y, z, world);

	this.owner = owner;
	this.typeId = typeId;
	this.chunkvec = chunkvec;
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
	this.chunkvec = new ChunkVec(block.getChunk());
    }

    /**
     *
     * @param block
     */
    public Unbreakable(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

	this.typeId = block.getTypeId();
	this.chunkvec = new ChunkVec(block.getChunk());
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
	return Material.getMaterial(this.typeId).toString();
    }

    /**
     *
     * @return
     */
    public ChunkVec getChunkVec()
    {
	return this.chunkvec;
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
	return playerName.equals(owner);
    }

    @Override
    public String toString()
    {
	return super.toString() + " [owner:" + owner + "]";
    }
}
