package com.bukkit.Phaed.PreciousStones;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Queue;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

/**
 * DEPRECATED - here for deserialization purposes
 * 
 * @author Phaed
 */
public class ProtectionManager implements java.io.Serializable
{
    static final long serialVersionUID = -1L;
    
    protected final HashMap<Vector, HashMap<Vector, ArrayList<String>>> chunkLists = new HashMap<Vector, HashMap<Vector, ArrayList<String>>>();
    
    @SuppressWarnings("unused")
    private Queue<Vector> deletionQueue;
    @SuppressWarnings("unused")
    private transient PreciousStones plugin;
    
    public ProtectionManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public HashMap<Vector, HashMap<Vector, ArrayList<String>>> getChunkLists()
    {
	return chunkLists;
    }
    
    public void initiate(PreciousStones plugin)
    {
    }
    
    public FieldSettings getFieldSettings(Block block)
    {
	return null;
    }
    
    public boolean isBreakable(Block block)
    {
	return false;
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
    
    public boolean isPlaceProtected(Block block, String playerName)
    {
	return false;
    }
    
    public boolean isDestroyProtected(Block block, String playerName)
    {
	return false;
    }
    
    public boolean isFireProtected(Block block, String playerName)
    {
	return false;
    }
    
    public boolean isEntryProtected(Block block, String playerName)
    {
	return false;
    }
    
    public HashMap<Vector, Block> getSourcePStone(Block block, String playerName)
    {
	return null;
    }
    
    public HashMap<Vector, Block> getPStonesOfType(int typeid, World world)
    {
	return null;
    }
    
    public boolean isInVector(Block block, String playerName)
    {
	return false;
    }
    
    public boolean addAllowed(Block block, String allowedName)
    {
	return false;
    }
    
    public boolean removeAllowed(Block block, String allowedName)
    {
	return false;
    }
    
    public ArrayList<String> getAllowedList(Block block)
    {
	return null;
    }
    
    public boolean isInConflict(Block block, String owner)
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
    
    public void releaseStone(Vector vec)
    {
    }
    
    @SuppressWarnings("unused")
    private HashMap<Vector, ArrayList<String>> getPStonesInArea(Block block)
    {
	return null;
    }
}
