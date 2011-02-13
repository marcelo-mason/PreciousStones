package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import java.util.Queue;

import org.bukkit.block.Block;

/**
 * DEPRECATED - here for deserialization purposes
 * 
 * @author Phaed
 */
public class UnbreakableManager implements java.io.Serializable
{
    static final long serialVersionUID = -1L;
    
    protected final HashMap<Vector, HashMap<Vector, String>> chunkLists = new HashMap<Vector, HashMap<Vector, String>>();
    
    @SuppressWarnings("unused")
    private Queue<Vector> deletionQueue;
    @SuppressWarnings("unused")
    private transient PreciousStones plugin;
    
    public UnbreakableManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public HashMap<Vector, HashMap<Vector, String>> getChunkLists()
    {
	return chunkLists;
    }
    
    public void initiate(PreciousStones plugin)
    {
    }

    public void flush()
    {
    }

    public int count()
    {
	return 0;
    }

    public boolean isPStone(Block block)
    {
	return false;
    }

    public boolean isType(Block block)
    {
	return false;
    }

    public boolean isOwner(Block block, String name)
    {
	return false;
    }

    public String getOwner(Block block)
    {
	return "";
    }

    public void addStone(Block block, String owner)
    {
    }

    public void releaseStone(Block block)
    {
    }
}
