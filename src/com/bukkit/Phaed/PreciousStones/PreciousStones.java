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
    // configuration
    public boolean publicAllowedList;
    public boolean logPlacement;
    public boolean logRemoval;
    public boolean logBypassRemoval;
    public boolean notifyPlacement;
    public boolean notifyRemoval;
    public boolean notifyBypassRemoval;
    public boolean warnPlaceOnProtected;
    public boolean warnBreakOnProtected;
    public boolean warnBreakAnothersStone;
    
    public List<Integer> unbreakableBlocks;
    public List<Integer> protectionBlocks;
    public List<Integer> protectionRadius;
    public List<Integer> protectionExtraHeight;
    public List<Boolean> protectionCanBuild;
    
    public List<String> bypassList;
    public List<Integer> unprotectableBlocks;
    
    public int chunksInLargestProtectionArea;
    
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
	// read settings
	
	loadConfiguration();
	
	// Register our events
	
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Monitor, this);
	
	PluginDescriptionFile desc = this.getDescription();
	log.info("[" + desc.getName() + "] version [" + desc.getVersion() + "] loaded");
	
	// load saved stones
	
	loadStones();
    }
    
    public void onDisable()
    {
	
    }
    
    /**
     * Load the configuration
     */
    public void loadConfiguration()
    {
	Configuration config = getConfiguration();
	config.load();
	
	List<Integer> ublocks = new ArrayList<Integer>();
	ublocks.add(41); // gold
	
	List<Integer> pblocks = new ArrayList<Integer>();
	pblocks.add(57); // diamond
	
	List<Integer> pradius = new ArrayList<Integer>();
	pradius.add(3); // diamond radius 3
	
	List<Integer> pextra = new ArrayList<Integer>();
	pextra.add(3); // diamond extra height of 3
	
	List<Boolean> pcb = new ArrayList<Boolean>();
	pcb.add(false); // disable building
	
	List<Integer> unprotectable = new ArrayList<Integer>();
	List<String> bypass = new ArrayList<String>();
	
	unbreakableBlocks = config.getIntList("unbreakable.blocks", ublocks);
	protectionBlocks = config.getIntList("protection.blocks", pblocks);
	protectionRadius = config.getIntList("protection.radius", pradius);
	protectionExtraHeight = config.getIntList("protected.extra-height", pextra);
	protectionCanBuild = config.getBooleanList("protection.can-build", pcb);
	logPlacement = config.getBoolean("log.placement", false);
	logRemoval = config.getBoolean("log.removal", false);
	logBypassRemoval = config.getBoolean("log.bypass-removal", true);
	notifyPlacement = config.getBoolean("notify.placement", true);
	notifyRemoval = config.getBoolean("notify.removal", true);
	notifyBypassRemoval = config.getBoolean("notify.bypass-removal", true);
	warnPlaceOnProtected = config.getBoolean("warn.place-on-protected-area", true);
	warnBreakOnProtected = config.getBoolean("warn.break-on-protected-area", true);
	warnBreakAnothersStone = config.getBoolean("warn.on-break-anothers-stone", true);
	bypassList = config.getStringList("bypass-list", bypass);
	unprotectableBlocks = config.getIntList("unprotectable-blocks", unprotectable);
	publicAllowedList = config.getBoolean("public-allowed-list", false);
	
	// calculate the number of chunks that encompass the
	// largest protection area offered
	
	int largestProtection = 0;
	
	for (int num : protectionRadius)
	    if (num > largestProtection)
		largestProtection = num;
	
	chunksInLargestProtectionArea = (int) Math.ceil(largestProtection / 16);
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
     * Write stones to disk
     */
    public void writeProtection()
    {
	PluginDescriptionFile desc = this.getDescription();
	ObjectOutputStream out;
	
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
