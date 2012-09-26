package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.listeners.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

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
    private LanguageManager languageManager;
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
    private WorldGuardManager worldGuardManager;
    private CombatTagManager combatTagManager;
    private ConfiscationManager confiscationManager;
    private TranslocationManager translocationManager;
    private PotionManager potionManager;
    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;
    private PSVehicleListener vehicleListener;
    private PSServerListener serverListener;

    //TODO use a better pattern when scanning grief

    /*
     * Fake main to allow us to run from netbeans
     */
    public static void main(String[] args)
    {


    }

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
    public static Logger getLog()
    {
        return logger;
    }

    /**
     * @return the logger
     */
    public static void debug(String msg, Object... arg)
    {
        if (getInstance().getSettingsManager().isDebugging())
        {
            logger.info(Helper.format(msg, arg));
        }
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
        logger.log(level, new StringBuilder().append("[PreciousStones] ").append(Helper.format(Helper.capitalize(msg), arg)).toString());
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

        languageManager = new LanguageManager();
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
        worldGuardManager = new WorldGuardManager();
        combatTagManager = new CombatTagManager();
        confiscationManager = new ConfiscationManager();
        potionManager = new PotionManager();
        translocationManager = new TranslocationManager();

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
        log("Version {1.version} loaded", getDescription().getVersion());
    }

    private void registerEvents()
    {
        getServer().getPluginManager().registerEvents(entityListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(serverListener, this);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(vehicleListener, this);
        getServer().getPluginManager().registerEvents(worldListener, this);
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
        getVisualizationManager().revertAll();
        getForceFieldManager().finalize();
        getStorageManager().processQueue();
        getServer().getScheduler().cancelTasks(this);
        getStorageManager().closeConnection();
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

    public CombatTagManager getCombatTagManager()
    {
        return combatTagManager;
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

    public ConfiscationManager getConfiscationManager()
    {
        return confiscationManager;
    }

    public PotionManager getPotionManager()
    {
        return potionManager;
    }

    public TranslocationManager getTranslocationManager()
    {
        return translocationManager;
    }

    public LanguageManager getLanguageManager()
    {
        return languageManager;
    }
}
