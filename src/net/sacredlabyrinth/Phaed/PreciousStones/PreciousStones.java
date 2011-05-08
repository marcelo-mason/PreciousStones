 package net.sacredlabyrinth.Phaed.PreciousStones;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;

/**
 * PreciousStones for Bukkit
 *
 * @author Phaed
 */
public class PreciousStones extends JavaPlugin
{
    public static final Logger logger = Logger.getLogger("Minecraft");

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

    /**
     * Parameterized logger
     * @param level
     * @param msg the message
     * @param arg the arguments
     */
    public static void log(Level level, String msg, Object... arg)
    {
        logger.log(level, new StringBuilder().append("[PreciousStones] ").append(MessageFormat.format(msg, arg)).toString());
    }

    /**
     *  Runs on plugin enable
     */
    @Override
    public void onEnable()
    {
        setupDatabase();
        displayStatusInfo();

        settings = new SettingsManager(this);
        com = new CommandManager(this);
        ffm = new ForceFieldManager(this);
        um = new UnbreakableManager(this);
        upm = new UnprotectableManager(this);
        cm = new CommunicatonManager(this);
        em = new EntryManager(this);
        plm = new PlayerManager(this);
        snm = new SnitchManager(this);
        mm = new MineManager(this);
        lm = new LightningManager(this);
        vm = new VelocityManager(this);
        clm = new CloakManager(this);
        pm = new PermissionsManager(this);

        sm = new StorageManager(this);

        playerListener = new PSPlayerListener(this);
        blockListener = new PSBlockListener(this);
        entityListener = new PSEntityListener(this);
        vehicleListener = new PSVehicleListener(this);
        worldListener = new PSWorldListener(this);

        registerEvents();
        registerCommands();
    }

    private void displayStatusInfo()
    {
        log(Level.INFO, "version {0} loaded", this.getDescription().getVersion());
    }

    private void registerEvents()
    {
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Highest, this);
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
        getServer().getPluginManager().registerEvent(Event.Type.WORLD_LOAD, worldListener, Priority.Highest, this);
    }

    private void registerCommands()
    {
        getCommand("ps").setExecutor(com);
    }

    private void setupDatabase()
    {
        try
        {
            getDatabase().find(Field.class).findRowCount();
        }
        catch (Exception ex)
        {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<Class<?>> getDatabaseClasses()
    {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Field.class);
        list.add(Unbreakable.class);
        list.add(CloakEntry.class);
        list.add(SnitchEntry.class);
        list.add(PSItemStack.class);
        list.add(AllowedEntry.class);
        return list;
    }

    /**
     *  Runs on plugin disable
     */
    @Override
    public void onDisable()
    {
        try
        {
            um.saveAll();
            ffm.saveAll();
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error Saving: ", ex.getMessage());
        }

        PreciousStones.log(Level.INFO, "data saved.");

        getDatabase().endTransaction();
    }
}
