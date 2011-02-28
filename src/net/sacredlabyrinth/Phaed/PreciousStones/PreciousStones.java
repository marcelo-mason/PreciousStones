package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSBlockListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSEntityListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSPlayerListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSWorldListener;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnbreakableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnprotectableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.StorageManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.CommunicatonManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.EntryManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PlayerManager;

/**
 * PreciousStones for Bukkit
 * 
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    public SettingsManager settings;
    public PermissionsManager pm;
    public ForceFieldManager ffm;
    public UnbreakableManager um;
    public UnprotectableManager upm;
    public StorageManager sm;
    public CommunicatonManager cm;
    public EntryManager em;
    public PlayerManager plm;
        
    public static final Logger log = Logger.getLogger("Minecraft");
    
    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;

    @Override
    public void onEnable()
    {
	settings = new SettingsManager(this);
	pm = new PermissionsManager(this);
	ffm = new ForceFieldManager(this);
	um = new UnbreakableManager(this);
	upm = new UnprotectableManager(this);
	sm = new StorageManager(this);
	cm = new CommunicatonManager(this);
	em = new EntryManager(this);
	plm = new PlayerManager(this);
	
	playerListener = new PSPlayerListener(this);
	blockListener = new PSBlockListener(this);
	entityListener = new PSEntityListener(this);
	worldListener = new PSWorldListener(this);
	
	log.info("[" + this.getDescription().getName() + "] version [" + this.getDescription().getVersion() + "] loaded");
	
	// Register our events
	
	getServer().getPluginManager().registerEvent(Event.Type.WORLD_SAVED, worldListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
    }
    
    @Override
    public void onDisable()
    {
	sm.save();
    }
}
