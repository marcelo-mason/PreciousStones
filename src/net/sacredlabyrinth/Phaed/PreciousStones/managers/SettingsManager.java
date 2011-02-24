package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

public class SettingsManager
{
    public List<Integer> unbreakableBlocks;
    public List<Integer> bypassBlocks;
    public List<Integer> unprotectableBlocks;
    public boolean logFire;
    public boolean logEntry;
    public boolean logPlace;
    public boolean logDestroy;
    public boolean logDestroyArea;
    public boolean logUnprotectable;
    public boolean logPvp;
    public boolean logBypassPvp;
    public boolean logBypassDelete;
    public boolean logBypassPlace;
    public boolean logBypassDestroy;
    public boolean logBypassUnprotectable;
    public boolean logConflictPlace;
    public boolean notifyPlace;
    public boolean notifyDestroy;
    public boolean notifyBypassPvp;
    public boolean notifyBypassPlace;
    public boolean notifyBypassDestroy;
    public boolean notifyBypassUnprotectable;
    public boolean notifyGuardDog;
    public boolean warnInstantHeal;
    public boolean warnSlowHeal;
    public boolean warnSlowDamage;
    public boolean warnFastDamage;
    public boolean warnPlace;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnUnprotectable;
    public boolean warnEntry;
    public boolean warnPvp;
    public boolean warnFire;
    public boolean publicBlockDetails;
    public boolean sneakingBypassesDamage;
    public boolean dropOnDelete;
    public boolean disableAlertsForAdmins;
    public boolean disableBypassAlertsForAdmins;
    
    public int chunksInLargestForceFieldArea;
    public List<Integer> ffBlocks = new ArrayList<Integer>();
    
    private final HashMap<Integer, FieldSettings> fieldsettings = new HashMap<Integer, FieldSettings>();
    private transient PreciousStones plugin;
    
    public SettingsManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void loadConfiguration()
    {
	Configuration config = plugin.getConfiguration();
	config.load();
	
	addForceFieldStones((ArrayList) config.getProperty("force-field-blocks"));
	unbreakableBlocks = config.getIntList("unbreakable-blocks", new ArrayList<Integer>());
	bypassBlocks = config.getIntList("bypass-blocks", new ArrayList<Integer>());
	unprotectableBlocks = config.getIntList("unprotectable-blocks", new ArrayList<Integer>());
	logFire = config.getBoolean("log.fire", false);
	logEntry = config.getBoolean("log.entry", false);
	logPlace = config.getBoolean("log.place", false);
	logPvp = config.getBoolean("log.pvp", false);
	logDestroy = config.getBoolean("log.destroy", false);
	logDestroyArea = config.getBoolean("log.destroy-area", false);
	logUnprotectable = config.getBoolean("log.unprotectable", false);
	logBypassPvp = config.getBoolean("log.bypass-pvp", false);
	logBypassDelete = config.getBoolean("log.bypass-delete", false);
	logBypassPlace = config.getBoolean("log.bypass-place", false);
	logBypassDestroy = config.getBoolean("log.bypass-destroy", false);
	logConflictPlace = config.getBoolean("log.conflict-place", false);
	notifyPlace = config.getBoolean("notify.place", false);
	notifyDestroy = config.getBoolean("notify.destroy", false);
	notifyBypassUnprotectable = config.getBoolean("notify.bypass-unprotectable", false);
	notifyBypassPvp = config.getBoolean("notify.bypass-pvp", false);
	notifyBypassPlace = config.getBoolean("notify.bypass-place", false);
	notifyBypassDestroy = config.getBoolean("notify.bypass-destroy", false);
	notifyGuardDog = config.getBoolean("notify.guard-dog", false);
	warnInstantHeal = config.getBoolean("warn.instant-heal", false);
	warnSlowHeal = config.getBoolean("warn.slow-heal", false);
	warnSlowDamage = config.getBoolean("warn.slow-damage", false);
	warnFastDamage = config.getBoolean("warn.fast-damage", false);
	warnFire = config.getBoolean("warn.fire", false);
	warnEntry = config.getBoolean("warn.entry", false);
	warnPlace = config.getBoolean("warn.place", false);
	warnPvp = config.getBoolean("warn.pvp", false);
	warnDestroy = config.getBoolean("warn.destroy", false);
	warnDestroyArea = config.getBoolean("warn.destroy-area", false);
	warnUnprotectable = config.getBoolean("warn.unprotectable", false);
	publicBlockDetails = config.getBoolean("settings.public-block-details", false);
	sneakingBypassesDamage = config.getBoolean("settings.sneaking-bypasses-damage", false);
	dropOnDelete = config.getBoolean("settings.drop-on-delete", false);
	disableAlertsForAdmins = config.getBoolean("disable-alerts-for-admins", false);
	disableBypassAlertsForAdmins = config.getBoolean("disable-bypass-alerts-for-admins", false);
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
     * Check if a block is one of the unprotectable types
     */
    public boolean isUnprotectableType(Block placedblock)
    {
	for (Integer t : unprotectableBlocks)
	{
	    if (placedblock.getTypeId() == t)
		return true;
	}
	
	return false;
    }
    
    /**
     * Check if a block is one of the unbreakable types
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
	return unbreakableBlocks.contains(unbreakableblock.getTypeId());
    }
    
    /**
     * Check if a type is one of the unbreakable types
     */
    public boolean isUnbreakableType(int typeId)
    {
	return unbreakableBlocks.contains(typeId);
    }
    
    /**
     * Check if a type is one of the unbreakable types
     */
    public boolean isUnbreakableType(String type)
    {
	return unbreakableBlocks.contains(Material.getMaterial(type).getId());
    }
    
    /**
     * Check if a block is one of the forcefeld types
     */
    public boolean isFieldType(Block block)
    {
	return ffBlocks.contains(block.getTypeId());
    }
    
    /**
     * Check if a type is one of the forcefeld types
     */
    public boolean isFieldType(String type)
    {
	return ffBlocks.contains(Material.getMaterial(type).getId());
    }
    
    /**
     * Check if a type is one of the forcefeld types
     */
    public boolean isFieldType(int typeId)
    {
	return ffBlocks.contains(typeId);
    }
    
    /**
     * Whetehr the block is a bypass type
     */
    public boolean isBypassBlock(Block block)
    {
	return bypassBlocks.contains(block.getTypeId());
    }
    
    /**
     * Returns the settings for a specific field type
     */
    public FieldSettings getFieldSettings(Field field)
    {
	return fieldsettings.get(field.getTypeId());
    }
    
    /**
     * Returns the settings for a specific block type
     */
    public FieldSettings getFieldSettings(int typeId)
    {
	return fieldsettings.get(typeId);
    }
    
    public class FieldSettings
    {
	public boolean blockDefined = false;
	
	public int blockId;
	public int radius = 0;
	public int height = 0;
	public String title;
	public boolean nameable = false;
	public boolean preventFire = false;
	public boolean preventPlace = false;
	public boolean preventDestroy = false;
	public boolean preventExplosions = false;
	public boolean preventPvP = false;
	public boolean preventEntry = false;
	public boolean preventUnprotectable = false;
	public boolean guarddogMode = false;
	public boolean instantHeal = false;
	public boolean slowHeal = false;
	public boolean slowDamage = false;
	public boolean fastDamage = false;
	public boolean breakable = false;
	public boolean welcomeMessage = false;
	public boolean farewellMessage = false;
	
	public String getTitle()
	{
	    if (title == null)
	    {
		return "";
	    }
	    
	    return title;
	}
	
	public String getTitleCap()
	{
	    if (title == null)
	    {
		return "";
	    }
	    
	    return Helper.capitalize(title);
	}
	
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
	    title = (String) map.get("title");
	    
	    if (map.containsKey("radius") && Helper.isInteger(map.get("radius")))
		radius = (Integer) map.get("radius");
	    
	    if (map.containsKey("custom-height"))
	    {
		if (Helper.isInteger(map.get("custom-height")))
		{
		    height = (Integer) map.get("custom-height");
		    
		}
		if (height == 0)
		    height = radius;
	    }
	    
	    if (map.containsKey("nameable") && Helper.isBoolean(map.get("nameable")))
		nameable = (Boolean) map.get("nameable");
	    
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
	    
	    if (map.containsKey("prevent-unprotectable") && Helper.isBoolean(map.get("prevent-unprotectable")))
		preventUnprotectable = (Boolean) map.get("prevent-unprotectable");
	    
	    if (map.containsKey("guard-dog-mode") && Helper.isBoolean(map.get("guard-dog-mode")))
		guarddogMode = (Boolean) map.get("guard-dog-mode");
	    
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
	    
	    if (map.containsKey("welcome-message") && Helper.isBoolean(map.get("welcome-message")))
		welcomeMessage = (Boolean) map.get("welcome-message");
	    
	    if (map.containsKey("farewell-message") && Helper.isBoolean(map.get("farewell-message")))
		farewellMessage = (Boolean) map.get("farewell-message");
	}
    }
}
