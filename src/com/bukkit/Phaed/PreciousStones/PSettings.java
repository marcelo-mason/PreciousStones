package com.bukkit.Phaed.PreciousStones;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.bukkit.block.Block;

public class PSettings
{
    public boolean publicBlockDetails;
    public boolean logPlace;
    public boolean logDestroy;
    public boolean logBypassDelete;
    public boolean logBypassDestroy;
    public boolean notifyPlace;
    public boolean notifyDestroy;
    public boolean notifyBypassDestroy;
    public boolean warnInstantHeal;
    public boolean warnSlowHeal;
    public boolean warnSlowDamage;
    public boolean warnFastDamage;
    public boolean warnPlace;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnEntry;
    public boolean warnPvP;
    public boolean warnFire;
    public List<Integer> unbreakableBlocks;
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
	if(maps == null)
	    return;
	
	double largestProtection = 0;
	
	for (LinkedHashMap map : maps)
	{
	    PStone pstone = new PStone(map);
	    
	    if (pstone.blockDefined)
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
     * Whetehr the block is a bypass type
     */
    public boolean isBypassBlock(Block block)
    {
	return bypassBlocks.contains(block.getTypeId());
    }
    
    /**
     * Returns the settings for a specific pstone block type
     */
    public PStone getPStoneSettings(Block block)
    {
	if (block == null)
	    return null;
	
	return pstones.get(block.getTypeId());
    }
    
    /**
     * Check if a block is one of the proteciton types
     */
    public boolean isPStoneType(Block block)
    {
	if (block == null)
	    return false;
	
	return pBlocks.contains(block.getTypeId());
    }
    
    public class PStone
    {
	public boolean blockDefined = false;
	
	public int blockId;
	public int radius = 0;
	public int height = 0;
	public boolean preventFire = false;
	public boolean preventPlace = false;
	public boolean preventDestroy = false;
	public boolean preventExplosions = false;
	public boolean preventPvP = false;
	public boolean preventEntry = false;
	public boolean instantHeal = false;
	public boolean slowHeal = false;
	public boolean slowDamage = false;
	public boolean fastDamage = false;
	public boolean breakable = false;
	
	public int getHeight()
	{
	    if(this.height == 0)
		return (this.radius * 2) + 1;
	    else
		return this.height;
	}
	
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
	    
	    if (map.containsKey("custom-height"))
	    {
		if (Helper.isInteger(map.get("custom-height")))
		{
		    height = (Integer) map.get("custom-height");
		    
		}
		else if(Helper.isString(map.get("custom-height")))
		{
		    if(((String)map.get("custom-height")).equals("full"))
			height = 500;
		}
		
		if (height == 0)
		    height = radius;
	    }
	    
	    if (map.containsKey("prevent-fire") && Helper.isBoolean(map.get("prevent-fire")))
		preventFire = (Boolean) map.get("prevent-fire");
	    
	    if (map.containsKey("prevent-place") && Helper.isBoolean(map.get("prevent-place")))
		preventPlace = (Boolean) map.get("prevent-place");
	    
	    if (map.containsKey("prevent-destroy") && Helper.isBoolean(map.get("prevent-destroy")))
		preventDestroy = (Boolean) map.get("prevent-destroy");
	    
	    if (map.containsKey("prevent-explosions") && Helper.isBoolean(map.get("prevent-explosions")))
		preventExplosions = (Boolean) map.get("prevent-explosions");
	    
	    if (map.containsKey("prevent-pvp") && Helper.isBoolean(map.get("prevent-pvp")))
		preventPvP = (Boolean) map.get("prevent-pvp");
	    
	    if (map.containsKey("prevent-entry") && Helper.isBoolean(map.get("prevent-entry")))
		preventEntry = (Boolean) map.get("prevent-entry");
	    
	    if (map.containsKey("instant-heal") && Helper.isBoolean(map.get("instant-heal")))
		instantHeal = (Boolean) map.get("instant-heal");
	    
	    if (map.containsKey("slow-heal") && Helper.isBoolean(map.get("slow-heal")))
		slowHeal = (Boolean) map.get("slow-heal");
	    
	    if (map.containsKey("slow-damage") && Helper.isBoolean(map.get("slow-damage")))
		slowDamage = (Boolean) map.get("slow-damage");

	    if (map.containsKey("fast-damage") && Helper.isBoolean(map.get("fast-damage")))
		fastDamage = (Boolean) map.get("fast-damage");

	    if (map.containsKey("breakable") && Helper.isBoolean(map.get("breakable")))
		breakable = (Boolean) map.get("breakable");
}
    }
}
