package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.*;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.bukkit.Phaed.PreciousStones.listeners.PSBlockListener;
import com.bukkit.Phaed.PreciousStones.listeners.PSEntityListener;
import com.bukkit.Phaed.PreciousStones.listeners.PSPlayerListener;
import com.bukkit.Phaed.PreciousStones.listeners.PSWorldListener;
import com.bukkit.Phaed.PreciousStones.managers.SettingsManager;
import com.bukkit.Phaed.PreciousStones.managers.ForceFieldManager;
import com.bukkit.Phaed.PreciousStones.managers.UnbreakableManager;
import com.bukkit.Phaed.PreciousStones.managers.UnprotectableManager;
import com.bukkit.Phaed.PreciousStones.managers.StorageManager;
import com.bukkit.Phaed.PreciousStones.managers.CommunicatonManager;
import com.bukkit.Phaed.PreciousStones.managers.EntryManager;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;


/**
 * PreciousStones for Bukkit
 * 
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    public static PermissionHandler Permissions = null;
    
    public SettingsManager settings;
    public ForceFieldManager ffm = new ForceFieldManager(this);
    public UnbreakableManager um = new UnbreakableManager(this);
    public UnprotectableManager upm = new UnprotectableManager(this);
    public StorageManager sm = new StorageManager(this);
    public CommunicatonManager cm = new CommunicatonManager(this);
    public EntryManager em = new EntryManager(this);
    
    public static final Logger log = Logger.getLogger("Minecraft");
    
    private final PSPlayerListener playerListener = new PSPlayerListener(this);
    private final PSBlockListener blockListener = new PSBlockListener(this);
    private final PSEntityListener entityListener = new PSEntityListener(this);
    private final PSWorldListener worldListener = new PSWorldListener(this);
    
    private PluginDescriptionFile desc;
    
    public PreciousStones(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
    {
	super(pluginLoader, instance, desc, folder, plugin, cLoader);
	
	this.desc = desc;
	
	if (!folder.exists())
	{
	    if (folder.mkdir())
		log.info("[" + desc.getName() + "] directory: " + folder.getPath() + " created");
	    else
		log.info("[" + desc.getName() + "] could not create directory: " + folder.getPath());
	}
    }
    
    @SuppressWarnings("static-access")
    public void setupPermissions()
    {
	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
	
	if (this.Permissions == null)
	{
	    if (test != null)
	    {
		this.Permissions = ((Permissions) test).getHandler();
	    }
	    else
	    {
		log.info("[" + desc.getName() + "] Permission system not enabled. Disabling plugin.");
		this.getServer().getPluginManager().disablePlugin(this);
	    }
	}
    }
    
    public PluginDescriptionFile getDesc()
    {
	return desc;
    }
    
    @Override
    public void onEnable()
    {
	log.info("[" + desc.getName() + "] version [" + desc.getVersion() + "] loaded");
	
	// read settings
	
	loadConfiguration();
	
	// load force-field and unbreakable blocks from file
	
	sm.load();
	
	// start scheduler
	
	em.startScheduler();
	
	// initiate permissions plugin
	
	setupPermissions();
	
	// Register our events
	
	getServer().getPluginManager().registerEvent(Event.Type.WORLD_SAVED, worldListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Lowest, this);
    }

    @Override
    public void onDisable()
    {

    }
    
    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void loadConfiguration()
    {
	Configuration config = getConfiguration();
	config.load();
	
	settings = new SettingsManager();
	settings.addForceFieldStones((ArrayList) config.getProperty("force-field-blocks"));		
	settings.unbreakableBlocks = config.getIntList("unbreakable-blocks", new ArrayList<Integer>());
	settings.bypassBlocks = config.getIntList("bypass-blocks", new ArrayList<Integer>());
	settings.unprotectableBlocks = config.getIntList("unprotectable-blocks", new ArrayList<Integer>());
	settings.logFire = config.getBoolean("log.fire", false);
	settings.logEntry = config.getBoolean("log.entry", false);
	settings.logPlace = config.getBoolean("log.place", false);
	settings.logPvp = config.getBoolean("log.pvp", false);
	settings.logDestroy = config.getBoolean("log.destroy", false);
	settings.logDestroyArea = config.getBoolean("log.destroy-area", false);
	settings.logUnprotectable = config.getBoolean("log.unprotectable", false);
	settings.logBypassPvp = config.getBoolean("log.bypass-pvp", false);
	settings.logBypassDelete = config.getBoolean("log.bypass-delete", false);
	settings.logBypassPlace = config.getBoolean("log.bypass-place", false);
	settings.logBypassDestroy = config.getBoolean("log.bypass-destroy", false);
	settings.logConflictPlace = config.getBoolean("log.conflict-place", false);
	settings.notifyPlace = config.getBoolean("notify.place", false);
	settings.notifyDestroy = config.getBoolean("notify.destroy", false);
	settings.notifyBypassUnprotectable = config.getBoolean("notify.bypass-unprotectable", false);
	settings.notifyBypassPvp = config.getBoolean("notify.bypass-pvp", false);
	settings.notifyBypassPlace = config.getBoolean("notify.bypass-place", false);
	settings.notifyBypassDestroy = config.getBoolean("notify.bypass-destroy", false);
	settings.notifyGuardDog = config.getBoolean("notify.guard-dog", false);
	settings.warnInstantHeal = config.getBoolean("warn.instant-heal", false);
	settings.warnSlowHeal = config.getBoolean("warn.slow-heal", false);
	settings.warnSlowDamage = config.getBoolean("warn.slow-damage", false);
	settings.warnFastDamage = config.getBoolean("warn.fast-damage", false);
	settings.warnFire = config.getBoolean("warn.fire", false);
	settings.warnEntry = config.getBoolean("warn.entry", false);
	settings.warnPlace = config.getBoolean("warn.place", false);
	settings.warnPvp = config.getBoolean("warn.pvp", false);
	settings.warnDestroy = config.getBoolean("warn.destroy", false);
	settings.warnDestroyArea = config.getBoolean("warn.destroy-area", false);
	settings.warnUnprotectable = config.getBoolean("warn.unprotectable", false);
	settings.publicBlockDetails = config.getBoolean("settings.public-block-details", false);
	settings.sneakingBypassesDamage = config.getBoolean("settings.sneaking-bypasses-damage", false);
    }   
}
