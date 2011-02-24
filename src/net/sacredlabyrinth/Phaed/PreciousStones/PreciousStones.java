package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/*
import java.io.*;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
*/

import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSBlockListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSEntityListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSPlayerListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSWorldListener;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnbreakableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnprotectableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.StorageManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.CommunicatonManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.EntryManager;

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
    public ForceFieldManager ffm;
    public UnbreakableManager um;
    public UnprotectableManager upm;
    public StorageManager sm;
    public CommunicatonManager cm;
    public EntryManager em;
    
    public static final Logger log = Logger.getLogger("Minecraft");
    
    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;
    
    /*
    public PreciousStones(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
    {
	super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }
    */
    
    @Override
    public void onEnable()
    {
	settings = new SettingsManager(this);
	ffm = new ForceFieldManager(this);
	um = new UnbreakableManager(this);
	upm = new UnprotectableManager(this);
	sm = new StorageManager(this);
	cm = new CommunicatonManager(this);
	em = new EntryManager(this);
	
	playerListener = new PSPlayerListener(this);
	blockListener = new PSBlockListener(this);
	entityListener = new PSEntityListener(this);
	worldListener = new PSWorldListener(this);
	
	log.info("[" + this.getDescription().getName() + "] version [" + this.getDescription().getVersion() + "] loaded");
	
	// read settings
	
	settings.loadConfiguration();
	
	// load force-field and unbreakable blocks from file
	
	sm.load();
	
	// start scheduler
	
	em.startScheduler();
	
	// initiate permissions plugin
	
	setupPermissions();
	
	// Register our events
	
	getServer().getPluginManager().registerEvent(Event.Type.WORLD_SAVED, worldListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
    }
    
    @Override
    public void onDisable()
    {
	
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
		log.info("[" + this.getDescription().getName() + "] Permission system not enabled. Disabling plugin.");
		this.getServer().getPluginManager().disablePlugin(this);
	    }
	}
    }
}
