package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.util.logging.Logger;

/**
 * PreciousStones for Bukkit
 * 
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    public PSettings psettings;
    
    public ProtectionManager pm = new ProtectionManager(this);
    public UnbreakableManager um = new UnbreakableManager(this);
    
    protected static final Logger log = Logger.getLogger("Minecraft");
    
    private final PSPlayerListener playerListener = new PSPlayerListener(this);
    private final PSBlockListener blockListener = new PSBlockListener(this);
    private final PSEntityListener entityListener = new PSEntityListener(this);
    
    private PluginDescriptionFile desc;
    private File folder;
    
    public PreciousStones(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
    {
	super(pluginLoader, instance, desc, folder, plugin, cLoader);
	
	this.desc = desc;
	this.folder = folder;
	
	if (!folder.exists())
	{
	    if (folder.mkdir())
		log.info("[" + desc.getName() + "] directory: " + folder.getPath() + " created");
	    else
		log.info("[" + desc.getName() + "] could not create directory: " + folder.getPath());
	}
    }
    
    public void onEnable()
    {
	log.info("[" + desc.getName() + "] version [" + desc.getVersion() + "] loaded");
	
	// read settings
	
	loadConfiguration();
	
	// load saved stones
	
	loadStones();
	
	// Register our events
	
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Highest, this);
    }
    
    public void onDisable()
    {
	
    }
    
    /**
     * Load stones from disk
     */
    public void loadStones()
    {
	File unbreakableFile = new File(folder.getPath() + File.separator + "unbreakable.bin");
	
	if (unbreakableFile.exists())
	{
	    try
	    {
		FileInputStream fi = new FileInputStream(unbreakableFile);
		ObjectInputStream oi = new ObjectInputStream(fi);
		
		um = (UnbreakableManager) oi.readObject();
		um.initiate(this);
		
		oi.close();
		fi.close();
		
		log.info("[" + desc.getName() + "] loaded " + um.count() + " unbreakable stones");
	    }
	    catch (Exception e)
	    {
		log.info("[" + desc.getName() + "] loading failed with error. unbreakable.bin");
		
		if (e.getMessage() != null)
		    log.info("[" + desc.getName() + "] error: " + e.getMessage());
	    }
	}
	
	File protectionFile = new File(folder.getPath() + File.separator + "protection.bin");
	
	if (protectionFile.exists())
	{
	    try
	    {
		FileInputStream fi = new FileInputStream(protectionFile);
		ObjectInputStream oi = new ObjectInputStream(fi);
		
		pm = (ProtectionManager) oi.readObject();
		pm.initiate(this);
		
		oi.close();
		fi.close();
		
		log.info("[" + desc.getName() + "] loaded " + pm.count() + " protection stones");
	    }
	    catch (Exception e)
	    {
		log.info("[" + desc.getName() + "] loading failed with error. protection.bin");
		
		if (e.getMessage() != null)
		    log.info("[" + desc.getName() + "] error: " + e.getMessage());
	    }
	}
    }
    
    /**
     * Load the configuration
     */
    @SuppressWarnings("unchecked")
    public void loadConfiguration()
    {
	Configuration config = getConfiguration();
	config.load();
	
	List<Integer> ublocks = new ArrayList<Integer>();
	ublocks.add(41); // gold
	
	List<Boolean> pcb = new ArrayList<Boolean>();
	pcb.add(false); // disable building
	
	List<Integer> bypassb = new ArrayList<Integer>();
	List<String> bypass = new ArrayList<String>();
	
	psettings = new PSettings();
	psettings.addProtectionStones((ArrayList) config.getProperty("protection"));
	
	psettings.unbreakableBlocks = config.getIntList("unbreakable-blocks", ublocks);
	psettings.logPlace = config.getBoolean("log.place", false);
	psettings.logDestroy = config.getBoolean("log.destroy", false);
	psettings.logBypassDelete = config.getBoolean("log.bypass-delete", true);
	psettings.logBypassDestroy = config.getBoolean("log.bypass-destroy", true);
	psettings.notifyPlace = config.getBoolean("notify.place", true);
	psettings.notifyDestroy = config.getBoolean("notify.destroy", true);
	psettings.notifyBypassDestroy = config.getBoolean("notify.bypass-destroy", true);
	psettings.warnInstantHeal = config.getBoolean("warn.instant-heal", true);
	psettings.warnSlowHeal = config.getBoolean("warn.slow-heal", true);
	psettings.warnSlowDamage = config.getBoolean("warn.slow-damage", true);
	psettings.warnFire = config.getBoolean("warn.fire", true);
	psettings.warnEntry = config.getBoolean("warn.entry", true);
	psettings.warnPlace = config.getBoolean("warn.place", true);
	psettings.warnPvP = config.getBoolean("warn.pvp", true);
	psettings.warnDestroy = config.getBoolean("warn.destroy", true);
	psettings.warnDestroyArea = config.getBoolean("warn.destroy-area", true);
	psettings.bypassPlayers = config.getStringList("bypass-players", bypass);
	psettings.bypassBlocks = config.getIntList("bypass-blocks", bypassb);
	psettings.publicBlockDetails = config.getBoolean("public-block-details", false);
    }
    
    /**
     * Write stones to disk
     */
    public void writeProtection()
    {
	PluginDescriptionFile desc = this.getDescription();
	ObjectOutputStream out;
	
	pm.flush();
	
	File unbreakableFile = new File(folder.getPath() + File.separator + "protection.bin");
	
	try
	{
	    out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(unbreakableFile)));
	    out.writeObject(pm);
	    out.close();
	}
	catch (Exception e)
	{
	    log.info("[" + desc.getName() + "] save file write failed: protection.bin");
	    
	    if (e.getMessage() != null)
		log.info("[" + desc.getName() + "] error: " + e.getMessage());
	}
	finally
	{
	    out = null;
	}
    }
    
    /**
     * Write diamond stones to disk
     */
    public void writeUnbreakable()
    {
	PluginDescriptionFile desc = this.getDescription();
	ObjectOutputStream out;
	
	um.flush();
	
	File protectionFile = new File(folder.getPath() + File.separator + "unbreakable.bin");
	
	try
	{
	    out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(protectionFile)));
	    out.writeObject(um);
	    out.close();
	}
	catch (Exception e)
	{
	    log.info("[" + desc.getName() + "] save file write failed: unbreakable.bin");
	    
	    if (e.getMessage() != null)
		log.info("[" + desc.getName() + "] error: " + e.getMessage());
	}
	finally
	{
	    out = null;
	}
    }
}
