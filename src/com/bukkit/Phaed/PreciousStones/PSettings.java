package com.bukkit.Phaed.PreciousStones;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.block.Block;

public class PSettings
{
    public boolean publicAllowedList;
    public boolean logPlace;
    public boolean logDestroy;
    public boolean logBypassDestroy;
    public boolean notifyPlace;
    public boolean notifyDestroy;
    public boolean notifyBypassDestroy;
    public boolean warnPlace;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnEntry;
    public boolean warnFire;
    public List<Integer> unbreakableBlocks;
    public List<String> bypassPlayers;
    public List<Integer> bypassBlocks;
    public int chunksInLargestProtectionArea;
    public List<Integer> pBlocks = new ArrayList<Integer>();

    private final HashMap<Integer, PStone> pstones = new HashMap<Integer, PStone>();
    
    public PSettings()
    {
	
    }
    
    @SuppressWarnings("unchecked")
    public void addProtectionStones(ArrayList<LinkedHashMap> maps)
    {
	double largestProtection = 0;

	for (LinkedHashMap map : maps)
	{
	    PStone pstone = new PStone(map);
	    
	    if(pstone.blockDefined)
	    {
		// add stone to our collection		
		pstones.put(pstone.blockId, pstone);
		
		// add the values to our reference lists		
		pBlocks.add(pstone.blockId);
		
		// see if the radius is the largest		
		if (pstone.radius > largestProtection)
		    largestProtection = pstone.radius;
	    }
	}
	
	chunksInLargestProtectionArea = (int) Math.max(Math.ceil(((largestProtection * 2.0) + 1.0) / 16.0), 1);
    }
    
    /**
     * Returns the settings for a specific pstone block type
     */
    public PStone getPStoneSettings(Block block)
    {
	if(block == null)
	    return null;
	
	return pstones.get(block.getTypeId());
    }    
    
    /**
     * Check if a block is one of the proteciton types
     */
    public boolean isPStoneType(Block block)
    {
	if(block == null)
	    return false;
	
	return pBlocks.contains(block.getTypeId());
    }
    
    public class PStone
    {
	public boolean blockDefined = false;

	public int blockId;
	public int radius = 3;
	public int extraHeight = 3;
	public boolean preventFire = false;
	public boolean preventPlace = false;
	public boolean preventDestroy = false;
	public boolean preventExplosions = false;
	public boolean preventEntry = false;

	@SuppressWarnings("unchecked")
	public PStone(LinkedHashMap map)
	{
	    // if no block specified then skip it its garbage
	    if (!map.containsKey("block") || !Helper.isInteger(map.get("block")))
		return;
	    
	    blockDefined = true;

	    blockId = (Integer) map.get("block");
	    
	    if (map.containsKey("radius") && Helper.isInteger(map.get("radius")))
		radius = (Integer) map.get("radius");

	    if (map.containsKey("extra-height") && Helper.isInteger(map.get("extra-height")))
		extraHeight = (Integer) map.get("extra-height");

	    if (map.containsKey("prevent-fire") && Helper.isBoolean(map.get("prevent-fire")))
		preventFire = (Boolean) map.get("prevent-fire");    

	    if (map.containsKey("prevent-place") && Helper.isBoolean(map.get("prevent-place")))
		preventPlace = (Boolean) map.get("prevent-place");    
	    
	    if (map.containsKey("prevent-destroy") && Helper.isBoolean(map.get("prevent-destroy")))
		preventDestroy = (Boolean) map.get("prevent-destroy");    

	    if (map.containsKey("prevent-explosions") && Helper.isBoolean(map.get("prevent-explosions")))
		preventExplosions = (Boolean) map.get("prevent-explosions");    

	    if (map.containsKey("prevent-entry") && Helper.isBoolean(map.get("prevent-entry")))
		preventEntry = (Boolean) map.get("prevent-entry");    
	}
    }
}
