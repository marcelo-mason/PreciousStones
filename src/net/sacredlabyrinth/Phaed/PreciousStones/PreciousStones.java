package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.register.payment.Method;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PreciousStones for Bukkit
 *
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    private static PreciousStones instance;
    private static Logger logger = Logger.getLogger("Minecraft");
    private Method Method;
    private SettingsManager settingsManager;
    private SimpleClansManager simpleClansManager;
    private CommandManager commandManager;
    private LimitManager limitManager;
    private ForceFieldManager forceFieldManager;
    private CuboidManager cuboidManager;
    private UnbreakableManager unbreakableManager;
    private UnprotectableManager unprotectableManager;
    private GriefUndoManager griefUndoManager;
    private StorageManager storageManager;
    private CommunicatonManager communicationManager;
    private EntryManager entryManager;
    private PlayerManager playerManager;
    private SnitchManager snitchManager;
    private MineManager mineManager;
    private LightningManager lightningManager;
    private VelocityManager velocityManager;
    private PermissionsManager permissionsManager;
    private VisualizationManager visualizationManager;
    private ForesterManager foresterManager;
    private LegacyManager legacyManager;
    private WorldGuardManager worldGuardManager;
    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;
    private PSVehicleListener vehicleListener;
    private PSServerListener serverListener;

    /**
     * @return the instance
     */
    public static PreciousStones getInstance()
    {
        return instance;
    }

    /**
     * @return the logger
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Parameterized logger
     *
     * @param level
     * @param msg   the message
     * @param arg   the arguments
     */
    public static void log(Level level, String msg, Object... arg)
    {
        logger.log(level, new StringBuilder().append("[PreciousStones] ").append(MessageFormat.format(msg, arg)).toString());
    }

    /**
     * Parameterized info logger
     *
     * @param msg
     * @param arg
     */
    public static void log(String msg, Object... arg)
    {
        log(Level.INFO, msg, arg);
    }

    /**
     * Runs on plugin enable
     */
    public void onEnable()
    {
        displayStatusInfo();

        instance = this;

        settingsManager = new SettingsManager();
        simpleClansManager = new SimpleClansManager();
        commandManager = new CommandManager();
        limitManager = new LimitManager();
        forceFieldManager = new ForceFieldManager();
        cuboidManager = new CuboidManager();
        unbreakableManager = new UnbreakableManager();
        unprotectableManager = new UnprotectableManager();
        communicationManager = new CommunicatonManager();
        entryManager = new EntryManager();
        playerManager = new PlayerManager();
        snitchManager = new SnitchManager();
        mineManager = new MineManager();
        lightningManager = new LightningManager();
        velocityManager = new VelocityManager();
        permissionsManager = new PermissionsManager();
        visualizationManager = new VisualizationManager();
        foresterManager = new ForesterManager();
        griefUndoManager = new GriefUndoManager();
        storageManager = new StorageManager();
        legacyManager = new LegacyManager();
        worldGuardManager = new WorldGuardManager();

        playerListener = new PSPlayerListener();
        blockListener = new PSBlockListener();
        entityListener = new PSEntityListener();
        vehicleListener = new PSVehicleListener();
        worldListener = new PSWorldListener();
        serverListener = new PSServerListener();

        registerEvents();
        registerCommands();
    }

    private void displayStatusInfo()
    {
        log("Version {0} loaded", getDescription().getVersion());
    }

    private void registerEvents()
    {
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENDERMAN_PICKUP, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENDERMAN_PLACE, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.ITEM_SPAWN, entityListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PAINTING_BREAK, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PAINTING_PLACE, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_BUCKET_FILL, playerListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PISTON_EXTEND, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PISTON_RETRACT, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_UPDATE, vehicleListener, Priority.High, this);
        getServer().getPluginManager().registerEvent(Event.Type.WORLD_LOAD, worldListener, Priority.High, this);
    }

    private void registerCommands()
    {
        getCommand("ps").setExecutor(getCommandManager());
    }

    /**
     * Runs on plugin disable
     */
    public void onDisable()
    {
        getStorageManager().processQueue();
        getServer().getScheduler().cancelTasks(this);
        getStorageManager().closeConnection();
    }

    /**
     * @param Method the Method to set
     */
    public void setMethod(Method Method)
    {
        this.Method = Method;
    }

    /**
     * @return the Method
     */
    public Method getMethod()
    {
        return Method;
    }

    /**
     * @return the settingsManager
     */
    public SettingsManager getSettingsManager()
    {
        return settingsManager;
    }

    /**
     * @return the commandManager
     */
    public CommandManager getCommandManager()
    {
        return commandManager;
    }

    /**
     * @return the forceFieldManager
     */
    public ForceFieldManager getForceFieldManager()
    {
        return forceFieldManager;
    }

    /**
     * @return the unbreakableManager
     */
    public UnbreakableManager getUnbreakableManager()
    {
        return unbreakableManager;
    }

    /**
     * @return the unprotectableManager
     */
    public UnprotectableManager getUnprotectableManager()
    {
        return unprotectableManager;
    }

    /**
     * @return the griefUndoManager
     */
    public GriefUndoManager getGriefUndoManager()
    {
        return griefUndoManager;
    }

    /**
     * @return the storageManager
     */
    public StorageManager getStorageManager()
    {
        return storageManager;
    }

    /**
     * @return the communicationManager
     */
    public CommunicatonManager getCommunicationManager()
    {
        return communicationManager;
    }

    /**
     * @return the entryManager
     */
    public EntryManager getEntryManager()
    {
        return entryManager;
    }

    /**
     * @return the playerManager
     */
    public PlayerManager getPlayerManager()
    {
        return playerManager;
    }

    /**
     * @return the snitchManager
     */
    public SnitchManager getSnitchManager()
    {
        return snitchManager;
    }

    /**
     * @return the mineManager
     */
    public MineManager getMineManager()
    {
        return mineManager;
    }

    /**
     * @return the lightningManager
     */
    public LightningManager getLightningManager()
    {
        return lightningManager;
    }

    /**
     * @return the velocityManager
     */
    public VelocityManager getVelocityManager()
    {
        return velocityManager;
    }

    /**
     * @return the permissionsManager
     */
    public PermissionsManager getPermissionsManager()
    {
        return permissionsManager;
    }

    /**
     * @return the simpleClansManager
     */
    public SimpleClansManager getSimpleClansManager()
    {
        return simpleClansManager;
    }

    /**
     * @return the visualizationManager
     */
    public VisualizationManager getVisualizationManager()
    {
        return visualizationManager;
    }

    /**
     * @return the foresterManager
     */
    public ForesterManager getForesterManager()
    {
        return foresterManager;
    }

    /**
     * @return the legacyManager
     */
    public LegacyManager getLegacyManager()
    {
        return legacyManager;
    }

    /**
     * @return the limitManager
     */
    public LimitManager getLimitManager()
    {
        return limitManager;
    }

    public CuboidManager getCuboidManager()
    {
        return cuboidManager;
    }

    public WorldGuardManager getWorldGuardManager()
    {
        return worldGuardManager;
    }

    public PSPlayerListener getPlayerListener()
    {
        return playerListener;
    }

    public PSBlockListener getBlockListener()
    {
        return blockListener;
    }

    public PSEntityListener getEntityListener()
    {
        return entityListener;
    }

    public PSWorldListener getWorldListener()
    {
        return worldListener;
    }

    public PSVehicleListener getVehicleListener()
    {
        return vehicleListener;
    }

    public PSServerListener getServerListener()
    {
        return serverListener;
    }
}
