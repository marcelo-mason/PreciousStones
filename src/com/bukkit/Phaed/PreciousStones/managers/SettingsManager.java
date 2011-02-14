package com.bukkit.Phaed.PreciousStones.managers;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.Helper;

public class SettingsManager
{
    public List<Integer> unbreakableBlocks;
    public List<Integer> bypassBlocks;
    public List<Integer> noPlaceBlocks;
    public boolean logFire;
    public boolean logEntry;
    public boolean logPlace;
    public boolean logDestroy;
    public boolean logDestroyArea;
    public boolean logPvp;
    public boolean logBypassPvp;
    public boolean logBypassDelete;
    public boolean logBypassPlace;
    public boolean logBypassDestroy;
    public boolean logConflictPlace;
    public boolean notifyPlace;
    public boolean notifyDestroy;
    public boolean notifyBypassPvp;
    public boolean notifyBypassPlace;
    public boolean notifyBypassDestroy;
    public boolean notifyGuardDog;
    public boolean warnInstantHeal;
    public boolean warnSlowHeal;
    public boolean warnSlowDamage;
    public boolean warnFastDamage;
    public boolean warnPlace;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnEntry;
    public boolean warnPvp;
    public boolean warnFire;
    public boolean publicBlockDetails;
    public boolean sneakingBypassesDamage;
    
    public int chunksInLargestForceFieldArea;
    public List<Integer> ffBlocks = new ArrayList<Integer>();
    
    private final HashMap<Integer, FieldSettings> fieldsettings = new HashMap<Integer, FieldSettings>();
    
    public SettingsManager()
    {
	
    }
    
    /**
     * Check if a block is one of the noplace types
     */
    public boolean isNoPlaceType(Block placedblock)
    {
	for (Integer t : unbreakableBlocks)
	{
	    if (placedblock.getTypeId() == t)
		return true;
	}
	
	return false;
    }
        
    /**
     * If the block is touching a no place block
     */
    public boolean touchingNoPlaceBlock(Block block)
    {
	if (block == null)
	    return false;
	
	for (int x = -1; x <= 1; x++)
	{
	    for (int z = -1; z <= 1; z++)
	    {
		for (int y = -1; y <= 1; y++)
		{
		    if (x == 0 && y == 0 && z == 0)
			continue;
		    
		    Material mat = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getType();
		    
		    if (mat.equals(Material.CHEST) || mat.equals(Material.FURNACE))
			return true;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Check if a block is one of the unbreakable types
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
	for (Integer t : unbreakableBlocks)
	{
	    if (unbreakableblock.getTypeId() == t)
		return true;
	}
	
	return false;
    }
    
    /**
     * Check if a block is one of the unbreakable types
     */
    public boolean isUnbreakableType(Material material)
    {
	for (Integer t : unbreakableBlocks)
	{
	    if (material.getId() == t)
		return true;
	}
	
	return false;
    }
    
    /**
     * Check if a block is one of the forcefeld types
     */
    public boolean isFieldType(Block block)
    {
	if (block == null)
	    return false;
	
	return ffBlocks.contains(block.getTypeId());
    }
    
    /**
     * Check if a block is one of the forcefeld types
     */
    public boolean isFieldType(Material material)
    {
	return ffBlocks.contains(material.getId());
    }
    
    @SuppressWarnings("unchecked")
    public void addForceFieldStones(ArrayList<LinkedHashMap> maps)
    {
	if (maps == null)
	    return;
	
	double largestForceField = 0;
	
	for (LinkedHashMap map : maps)
	{
	    FieldSettings pstone = new FieldSettings(map);
	    
	    if (pstone.blockDefined)
	    {
		// add stone to our collection
		fieldsettings.put(pstone.blockId, pstone);
		
		// add the values to our reference lists
		ffBlocks.add(pstone.blockId);
		
		// see if the radius is the largest
		if (pstone.radius > largestForceField)
		    largestForceField = pstone.radius;
	    }
	}
	
	chunksInLargestForceFieldArea = (int) Math.max(Math.ceil(((largestForceField * 2.0) + 1.0) / 16.0), 1);
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
    public FieldSettings getFieldSettings(Block block)
    {
	if (block == null)
	    return null;
	
	return fieldsettings.get(block.getTypeId());
    }
    
    public class FieldSettings
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
	public boolean guarddogMode = false;
	
	public int getHeight()
	{
	    if (this.height == 0)
		return (this.radius * 2) + 1;
	    else
		return this.height;
	}
	
	@SuppressWarnings("unchecked")
	public FieldSettings(LinkedHashMap map)
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
		else if (Helper.isString(map.get("custom-height")))
		{
		    if (((String) map.get("custom-height")).equals("full"))
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
	    
	    if (map.containsKey("guard-dog-mode") && Helper.isBoolean(map.get("guard-dog-mode")))
		guarddogMode = (Boolean) map.get("guard-dog-mode");
	}
    }
}
