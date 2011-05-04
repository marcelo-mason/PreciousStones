package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSBlockListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSEntityListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSPlayerListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSWorldListener;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.PSVehicleListener;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.CommandManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnbreakableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.UnprotectableManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.StorageManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.CommunicatonManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.EntryManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PlayerManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SnitchManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.MineManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.LightningManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.VelocityManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.CloakManager;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * PreciousStones for Bukkit
 *
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    public static final Logger log = Logger.getLogger("Minecraft");

    public SettingsManager settings;
    public CommandManager com;
    public ForceFieldManager ffm;
    public UnbreakableManager um;
    public UnprotectableManager upm;
    public StorageManager sm;
    public CommunicatonManager cm;
    public EntryManager em;
    public PlayerManager plm;
    public SnitchManager snm;
    public MineManager mm;
    public LightningManager lm;
    public VelocityManager vm;
    public CloakManager clm;
    public PermissionsManager pm;

    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;
    private PSVehicleListener vehicleListener;

    @Override
    public void onEnable()
    {
	log.log(Level.INFO, "[{0}] version [{1}] loaded", new Object[]{this.getDescription().getName(), this.getDescription().getVersion()});

	settings = new SettingsManager(this);
	com = new CommandManager(this);
	ffm = new ForceFieldManager(this);
	um = new UnbreakableManager(this);
	upm = new UnprotectableManager(this);
	sm = new StorageManager(this);
	cm = new CommunicatonManager(this);
	em = new EntryManager(this);
	plm = new PlayerManager(this);
	snm = new SnitchManager(this);
	mm = new MineManager(this);
	lm = new LightningManager(this);
	vm = new VelocityManager(this);
	clm = new CloakManager(this);
	pm = new PermissionsManager(this);

	playerListener = new PSPlayerListener(this);
	blockListener = new PSBlockListener(this);
	entityListener = new PSEntityListener(this);
	worldListener = new PSWorldListener(this);
	vehicleListener = new PSVehicleListener(this);

	registerEvents();

	com.registerHelpCommands();
    }

    private void registerEvents()
    {
	getServer().getPluginManager().registerEvent(Event.Type.WORLD_SAVE, worldListener, Priority.Lowest, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Monitor, this);
	getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Highest, this);
	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_UPDATE, vehicleListener, Priority.Highest, this);
    }

    @Override
    public void onDisable()
    {
	if (sm != null)
	{
	    sm.save();
	}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
    {
	try
	{
	    String[] split = args;
	    String commandName = command.getName().toLowerCase();
	    if (sender instanceof Player)
	    {
		if (commandName.equals("ps"))
		{
		    return com.processCommand((Player) sender, split);
		}
	    }
	    return false;
	}
	catch (Throwable ex)
	{
	    return true;
	}
    }
}
