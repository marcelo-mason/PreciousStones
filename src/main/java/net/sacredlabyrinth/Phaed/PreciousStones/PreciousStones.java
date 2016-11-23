package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.api.Api;
import net.sacredlabyrinth.Phaed.PreciousStones.api.IApi;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.listeners.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.*;
import net.sacredlabyrinth.Phaed.PreciousStones.uuid.UUIDMigration;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PreciousStones for Bukkit
 *
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin {
    private static PreciousStones instance;
    private ArrayList<String> messages = new ArrayList<String>();
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
    private CommunicationManager communicationManager;
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
    private RedProtectManager redProtectManager;
    private CombatTagManager combatTagManager;
    private ConfiscationManager confiscationManager;
    private TranslocationManager translocationManager;
    private TeleportationManager teleportationManager;
    private PotionManager potionManager;
    private PSPlayerListener playerListener;
    private PSBlockListener blockListener;
    private PSEntityListener entityListener;
    private PSWorldListener worldListener;
    private PSVehicleListener vehicleListener;
    private PSServerListener serverListener;
    private PSInventoryListener inventoryListener;
    private McMMOListener mcmmoListener;
    private LWCListener lwcListener;
    private static IApi api;

    /**
     * @return the instance
     */
    public static PreciousStones getInstance() {
        return instance;
    }

    public static IApi API() {
        return api;
    }

    /**
     * @return the logger
     */
    public static Logger getLog() {
        return logger;
    }

    /**
     * @return the logger
     */
    public static void debug(Object msg, Object... arg) {
        if (getInstance().getSettingsManager() != null && getInstance().getSettingsManager().isDebug()) {
            logger.info(String.format(msg.toString(), arg));
        }
    }

    /**
     * Parameterized logger
     *
     * @param level
     * @param msg   the message
     * @param arg   the arguments
     */
    public static void log(Level level, Object msg, Object... arg) {
        logger.log(level, "[PreciousStones] " + ChatHelper.format(msg.toString(), arg));
    }

    /**
     * Parametrized info logger
     *
     * @param msg
     * @param arg
     */
    public static void log(Object msg, Object... arg) {
        log(Level.INFO, msg, arg);
    }

    /**
     * Runs on plugin enable
     */
    public void onEnable() {
        if (!UUIDMigration.canReturnUUID()) {
            log("This version of PreciousStones only works with Bukkit 1.7.5+");
            return;
        }

        instance = this;
        settingsManager = new SettingsManager();
        languageManager = new LanguageManager();

        displayStatusInfo();

        simpleClansManager = new SimpleClansManager();
        commandManager = new CommandManager();
        limitManager = new LimitManager();
        forceFieldManager = new ForceFieldManager();
        cuboidManager = new CuboidManager();
        unbreakableManager = new UnbreakableManager();
        unprotectableManager = new UnprotectableManager();
        communicationManager = new CommunicationManager();
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
        redProtectManager = new RedProtectManager();
        combatTagManager = new CombatTagManager();
        confiscationManager = new ConfiscationManager();
        potionManager = new PotionManager();
        translocationManager = new TranslocationManager();
        teleportationManager = new TeleportationManager();

        playerListener = new PSPlayerListener();
        blockListener = new PSBlockListener();
        entityListener = new PSEntityListener();
        vehicleListener = new PSVehicleListener();
        worldListener = new PSWorldListener();
        serverListener = new PSServerListener();
        inventoryListener = new PSInventoryListener();

        if (permissionsManager.hasMcMMO()) {
            mcmmoListener = new McMMOListener();
        }

        if (permissionsManager.hasLWC()) {
            lwcListener = new LWCListener();
        }

        api = new Api();

        registerEvents();
        registerCommands();
        pullMessages();
    }

    private void displayStatusInfo() {
        log("psLoaded", getDescription().getVersion());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(entityListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(serverListener, this);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(vehicleListener, this);
        getServer().getPluginManager().registerEvents(worldListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);

        if (permissionsManager.hasMcMMO()) {
            getServer().getPluginManager().registerEvents(mcmmoListener, this);
        }
    }

    private void registerCommands() {
        getCommand("ps").setExecutor(getCommandManager());
    }

    /**
     * Runs on plugin disable
     */

    public void onDisable() {
        PreciousStones.log("Shutting Down: Cancelling all tasks...");
        getServer().getScheduler().cancelTasks(this);

        PreciousStones.log("Shutting Down: Saving all pending data...");
        getForceFieldManager().offerAllDirtyFields();
        getPlayerManager().offerOnlinePlayerEntries();
        getStorageManager().processQueue();

        PreciousStones.log("Shutting Down: Clearing chunks from memory...");
        getForceFieldManager().clearChunkLists();
        getUnbreakableManager().clearChunkLists();

        PreciousStones.log("Shutting Down: Closing db connection...");
        getStorageManager().closeConnection();
    }

    public void pullMessages() {
        if (getSettingsManager().isDisableMessages()) {
            return;
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://minecraftcubed.net/pluginmessage/").openStream(), StandardCharsets.UTF_8));

            String message;
            while ((message = in.readLine()) != null) {
                messages.add(message);
                getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + message);
            }
            in.close();

        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * @return the settingsManager
     */
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    /**
     * @return the commandManager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * @return the forceFieldManager
     */
    public ForceFieldManager getForceFieldManager() {
        return forceFieldManager;
    }

    /**
     * @return the unbreakableManager
     */
    public UnbreakableManager getUnbreakableManager() {
        return unbreakableManager;
    }

    /**
     * @return the unprotectableManager
     */
    public UnprotectableManager getUnprotectableManager() {
        return unprotectableManager;
    }

    /**
     * @return the griefUndoManager
     */
    public GriefUndoManager getGriefUndoManager() {
        return griefUndoManager;
    }

    /**
     * @return the storageManager
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * @return the communicationManager
     */
    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }

    /**
     * @return the entryManager
     */
    public EntryManager getEntryManager() {
        return entryManager;
    }

    /**
     * @return the playerManager
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * @return the snitchManager
     */
    public SnitchManager getSnitchManager() {
        return snitchManager;
    }

    /**
     * @return the mineManager
     */
    public MineManager getMineManager() {
        return mineManager;
    }

    /**
     * @return the lightningManager
     */
    public LightningManager getLightningManager() {
        return lightningManager;
    }

    /**
     * @return the velocityManager
     */
    public VelocityManager getVelocityManager() {
        return velocityManager;
    }

    /**
     * @return the permissionsManager
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    /**
     * @return the simpleClansManager
     */
    public SimpleClansManager getSimpleClansManager() {
        return simpleClansManager;
    }

    /**
     * @return the visualizationManager
     */
    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    /**
     * @return the foresterManager
     */
    public ForesterManager getForesterManager() {
        return foresterManager;
    }

    /**
     * @return the limitManager
     */
    public LimitManager getLimitManager() {
        return limitManager;
    }

    public CuboidManager getCuboidManager() {
        return cuboidManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public CombatTagManager getCombatTagManager() {
        return combatTagManager;
    }

    public PSPlayerListener getPlayerListener() {
        return playerListener;
    }

    public PSBlockListener getBlockListener() {
        return blockListener;
    }

    public PSEntityListener getEntityListener() {
        return entityListener;
    }

    public PSWorldListener getWorldListener() {
        return worldListener;
    }

    public PSVehicleListener getVehicleListener() {
        return vehicleListener;
    }

    public PSServerListener getServerListener() {
        return serverListener;
    }

    public ConfiscationManager getConfiscationManager() {
        return confiscationManager;
    }

    public PotionManager getPotionManager() {
        return potionManager;
    }

    public TranslocationManager getTranslocationManager() {
        return translocationManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public TeleportationManager getTeleportationManager() {
        return teleportationManager;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public RedProtectManager getRedProtectManager() {
        return redProtectManager;
    }
}
