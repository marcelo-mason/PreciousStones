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

/**
 *
 * @author phaed
 */
public final class SettingsManager
{
    public List<Integer> unbreakableBlocks;
    public List<Integer> bypassBlocks;
    public List<Integer> unprotectableBlocks;
    public List<Integer> cloakableBlocks;
    public List<Integer> cloakBlocks;
    public int cloakMinRadius;
    public int cloakDefaultRadius;
    public int cloakMaxRadius;
    public List<Integer> toolItems;
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
    public boolean warnCloaking;
    public boolean warnInstantHeal;
    public boolean warnSlowHeal;
    public boolean warnSlowDamage;
    public boolean warnFastDamage;
    public boolean warnGiveAir;
    public boolean warnPlace;
    public boolean warnDestroy;
    public boolean warnDestroyArea;
    public boolean warnUnprotectable;
    public boolean warnEntry;
    public boolean warnPvp;
    public boolean warnFire;
    public boolean warnLaunch;
    public boolean warnCannon;
    public boolean warnMine;
    public boolean publicBlockDetails;
    public boolean sneakingBypassesDamage;
    public boolean allowedCanBreakPstones;
    public boolean dropOnDelete;
    public boolean disableAlertsForAdmins;
    public boolean disableBypassAlertsForAdmins;
    public boolean offByDefault;
    public int chunksInLargestForceFieldArea;
    public List<Integer> ffBlocks = new ArrayList<Integer>();
    public int[] throughFields = new int[] { 0, 6, 8, 9, 10, 11, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 69, 68, 70, 72, 75, 76, 77, 83, 92, 93, 94 };
    public int linesPerPage;
    public long saveFrequency;

    private final HashMap<Integer, FieldSettings> fieldsettings = new HashMap<Integer, FieldSettings>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public SettingsManager(PreciousStones plugin)
    {
	this.plugin = plugin;

	loadConfiguration();
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
	toolItems = config.getIntList("tool-items", new ArrayList<Integer>());
	cloakableBlocks = config.getIntList("cloak.cloakable-blocks", new ArrayList<Integer>());
	cloakBlocks = config.getIntList("cloak.cloak-blocks", new ArrayList<Integer>());
	cloakMinRadius = config.getInt("cloak.viewing-radius.min", 5);
	cloakMaxRadius = config.getInt("cloak.viewing-radius.max", 20);
	cloakDefaultRadius = config.getInt("cloak.viewing-radius.default", 10);
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
	warnCloaking = config.getBoolean("warn.cloaking", false);
	warnInstantHeal = config.getBoolean("warn.instant-heal", false);
	warnSlowHeal = config.getBoolean("warn.slow-heal", false);
	warnSlowDamage = config.getBoolean("warn.slow-damage", false);
	warnFastDamage = config.getBoolean("warn.fast-damage", false);
	warnGiveAir = config.getBoolean("warn.give-air", false);
	warnFire = config.getBoolean("warn.fire", false);
	warnEntry = config.getBoolean("warn.entry", false);
	warnPlace = config.getBoolean("warn.place", false);
	warnPvp = config.getBoolean("warn.pvp", false);
	warnDestroy = config.getBoolean("warn.destroy", false);
	warnDestroyArea = config.getBoolean("warn.destroy-area", false);
	warnUnprotectable = config.getBoolean("warn.unprotectable", false);
	warnLaunch = config.getBoolean("warn.launch", false);
	warnCannon = config.getBoolean("warn.cannon", false);
	warnMine = config.getBoolean("warn.mine", false);
	publicBlockDetails = config.getBoolean("settings.public-block-details", false);
	sneakingBypassesDamage = config.getBoolean("settings.sneaking-bypasses-damage", false);
	allowedCanBreakPstones = config.getBoolean("settings.allowed-can-break-pstones", false);
	dropOnDelete = config.getBoolean("settings.drop-on-delete", false);
	disableAlertsForAdmins = config.getBoolean("settings.disable-alerts-for-admins", false);
	disableBypassAlertsForAdmins = config.getBoolean("settings.disable-bypass-alerts-for-admins", false);
	offByDefault = config.getBoolean("settings.off-by-default", false);
	linesPerPage = config.getInt("settings.lines-per-page", 12);
        saveFrequency = config.getInt("saving-frequency-minutes", 30);
    }

    /**
     *
     * @param maps
     */
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

	largestForceField = Math.max(largestForceField, cloakMaxRadius);

	chunksInLargestForceFieldArea = (int) Math.max(Math.ceil(((largestForceField * 2.0) + 1.0) / 16.0), 1);
    }

    /**
     * Whether any nameable pstones have been defined
     * @return
     */
    public boolean haveNameable()
    {
	for (FieldSettings setting : fieldsettings.values())
	{
	    if (setting.nameable)
	    {
		return true;
	    }
	}

	return false;
    }

    /**
     * Check if a block is one of the unprotectable types
     * @param placedblock
     * @return
     */
    public boolean isUnprotectableType(Block placedblock)
    {
	for (Integer t : unprotectableBlocks)
	{
	    if (placedblock.getTypeId() == 0)
	    {
		continue;
	    }

	    if (placedblock.getTypeId() == t)
	    {
		return true;
	    }
	}

	return false;
    }

    /**
     * Check if a block is one of the tool item types
     * @param block
     * @return
     */
    public boolean isToolItemType(Block block)
    {
	return toolItems.contains(block.getTypeId());
    }

    /**
     * Check if a block is one of the tool item types
     * @param typeId
     * @return
     */
    public boolean isToolItemType(int typeId)
    {
	return toolItems.contains(typeId);
    }

    /**
     * Check if a block is one of the tool item types
     * @param type
     * @return
     */
    public boolean isToolItemType(String type)
    {
	return toolItems.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the cloakable types
     * @param block
     * @return
     */
    public boolean isCloakableType(Block block)
    {
	return cloakableBlocks.contains(block.getTypeId());
    }

    /**
     * Check if a block is one of the cloakable types
     * @param typeId
     * @return
     */
    public boolean isCloakableType(int typeId)
    {
	return cloakableBlocks.contains(typeId);
    }

    /**
     * Check if a block is one of the cloakable types
     * @param type
     * @return
     */
    public boolean isCloakableType(String type)
    {
	return cloakableBlocks.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the cloak types
     * @param block
     * @return
     */
    public boolean isCloakType(Block block)
    {
	return cloakBlocks.contains(block.getTypeId());
    }

    /**
     * Check if a block is one of the cloak types
     * @param typeId
     * @return
     */
    public boolean isCloakType(int typeId)
    {
	return cloakBlocks.contains(typeId);
    }

    /**
     * Check if a block is one of the snitch types
     * @param block
     * @return
     */
    public boolean isSnitchType(Block block)
    {
	for (FieldSettings setting : fieldsettings.values())
	{
	    if (setting.snitch && setting.blockId == block.getTypeId())
	    {
		return true;
	    }
	}

	return false;
    }

    /**
     * Check if a block is one of the unbreakable types
     * @param unbreakableblock
     * @return
     */
    public boolean isUnbreakableType(Block unbreakableblock)
    {
	return unbreakableBlocks.contains(unbreakableblock.getTypeId());
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param typeId
     * @return
     */
    public boolean isUnbreakableType(int typeId)
    {
	return unbreakableBlocks.contains(typeId);
    }

    /**
     * Check if a type is one of the unbreakable types
     * @param type
     * @return
     */
    public boolean isUnbreakableType(String type)
    {
	return unbreakableBlocks.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if a block is one of the forcefeld types
     * @param block
     * @return
     */
    public boolean isFieldType(Block block)
    {
	return ffBlocks.contains(block.getTypeId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param type
     * @return
     */
    public boolean isFieldType(String type)
    {
	return ffBlocks.contains(Material.getMaterial(type).getId());
    }

    /**
     * Check if the material is one of the forcefeld types
     * @param material
     * @return
     */
    public boolean isFieldType(Material material)
    {
	return ffBlocks.contains(material.getId());
    }

    /**
     * Check if a type is one of the forcefeld types
     * @param typeId
     * @return
     */
    public boolean isFieldType(int typeId)
    {
	return ffBlocks.contains(typeId);
    }

    /**
     * Whetehr the block is a bypass type
     * @param block
     * @return
     */
    public boolean isBypassBlock(Block block)
    {
	return bypassBlocks.contains(block.getTypeId());
    }

    /**
     * Returns the settings for a specific field type
     * @param field
     * @return
     */
    public FieldSettings getFieldSettings(Field field)
    {
	return getFieldSettings(field.getTypeId());
    }

    /**
     * Returns the settings for a specific block type
     * @param typeId
     * @return
     */
    public FieldSettings getFieldSettings(int typeId)
    {
	if (isCloakableType(typeId))
	{
	    FieldSettings fs = new FieldSettings(typeId);
	    return fs;
	}

	return fieldsettings.get(typeId);
    }

    /**
     * Returns all the field settings
     * @return
     */
    public HashMap<Integer, FieldSettings> getFieldSettings()
    {
	return fieldsettings;
    }

    /**
     *
     */
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
        public boolean preventMobDamage = false;
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
        public boolean giveAir = false;
        public boolean snitch = false;
        public boolean noConflict = false;
        public boolean launch = false;
        public int launchHeight = 0;
        public boolean cannon = false;
        public int cannonHeight = 0;
        public boolean mine = false;
        public int mineDelaySeconds = 0;
        public int mineReplaceBlock = 0;
        public boolean lightning = false;
        public int lightningDelaySeconds = 0;
        public int lightningReplaceBlock = 0;
        public boolean cloak = false;

        /**
         *
         * @return
         */
        public String getTitle()
	{
	    if (title == null)
	    {
		return "";
	    }

	    return title;
	}

        /**
         *
         * @return
         */
        public String getTitleCap()
	{
	    if (title == null)
	    {
		return "";
	    }

	    return Helper.capitalize(title);
	}

        /**
         *
         * @return
         */
        public int getHeight()
	{
	    if (this.height == 0)
		return (this.radius * 2) + 1;
	    else
		return this.height;
	}

        /**
         *
         * @param blockId
         */
        public FieldSettings(int blockId)
	{
	    this.title = "Cloak";
	    this.blockId = blockId;
	    this.radius = cloakDefaultRadius;
	    this.cloak = true;
	    this.noConflict = true;
	    this.breakable = true;
	}

        /**
         *
         * @param map
         */
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

	    if (map.containsKey("prevent-mob-damage") && Helper.isBoolean(map.get("prevent-mob-damage")))
		preventMobDamage = (Boolean) map.get("prevent-mob-damage");

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

	    if (map.containsKey("give-air") && Helper.isBoolean(map.get("give-air")))
		giveAir = (Boolean) map.get("give-air");

	    if (map.containsKey("snitch") && Helper.isBoolean(map.get("snitch")))
		snitch = (Boolean) map.get("snitch");

	    if (map.containsKey("no-conflict") && Helper.isBoolean(map.get("no-conflict")))
		noConflict = (Boolean) map.get("no-conflict");

	    if (map.containsKey("launch") && Helper.isBoolean(map.get("launch")))
		launch = (Boolean) map.get("launch");

	    if (map.containsKey("launch-height") && Helper.isInteger(map.get("launch-height")))
		launchHeight = (Integer) map.get("launch-height");

	    if (map.containsKey("cannon") && Helper.isBoolean(map.get("cannon")))
		cannon = (Boolean) map.get("cannon");

	    if (map.containsKey("cannon-height") && Helper.isInteger(map.get("cannon-height")))
		cannonHeight = (Integer) map.get("cannon-height");

	    if (map.containsKey("mine") && Helper.isBoolean(map.get("mine")))
		mine = (Boolean) map.get("mine");

	    if (map.containsKey("mine-replace-block") && Helper.isInteger(map.get("mine-replace-block")))
		mineReplaceBlock = (Integer) map.get("mine-replace-block");

	    if (map.containsKey("mine-delay-seconds") && Helper.isInteger(map.get("mine-delay-seconds")))
		mineDelaySeconds = (Integer) map.get("mine-delay-seconds");

	    if (map.containsKey("lightning") && Helper.isBoolean(map.get("lightning")))
	    lightning = (Boolean) map.get("lightning");

	    if (map.containsKey("lightning-replace-block") && Helper.isInteger(map.get("lightning-replace-block")))
	    lightningReplaceBlock = (Integer) map.get("lightning-replace-block");

	    if (map.containsKey("lightning-delay-seconds") && Helper.isInteger(map.get("lightning-delay-seconds")))
	    lightningDelaySeconds = (Integer) map.get("lightning-delay-seconds");
	}

	@Override
	public String toString()
	{
	    String header = "Title: " + title + " Type: " + Material.getMaterial(blockId) + " Radius: " + radius + " Height: " + getHeight();
	    String properties = getProtertiesString();

	    return header + " " + properties;
	}

        /**
         *
         * @return
         */
        public String getProtertiesString()
	{
	    String properties = "";

	    if (welcomeMessage || farewellMessage)
		properties += ", message";

	    if (preventFire)
		properties += ", -fire";

	    if (preventEntry)
		properties += ", -entry";

	    if (preventPlace)
		properties += ", -place";

	    if (preventDestroy)
		properties += ", -destroy";

	    if (preventExplosions)
		properties += ", -explosions";

	    if (preventPvP)
		properties += ", -pvp";

	    if (guarddogMode)
		properties += ", guard-dog";

	    if (instantHeal)
		properties += ", heal";

	    if (slowHeal)
		properties += ", slow-heal";

	    if (slowDamage)
		properties += ", slow-damage";

	    if (fastDamage)
		properties += ", fast-damage";

	    if (giveAir)
		properties += ", air";

	    if (snitch)
		properties += ", snitch";

	    if (launch)
		properties += ", launch";

	    if (cannon)
		properties += ", cannon";

	    /*
            if (noConflict)
		properties += ", no-conflict";
            */

	    if (mine)
		properties += ", mine";

	    if (cloak)
		properties += ", cloak";

	    if (properties.length() > 0)
		return properties.substring(2);

	    return "";
	}
    }
}
