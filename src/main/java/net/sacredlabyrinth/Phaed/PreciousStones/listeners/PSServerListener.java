package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * @author phaed, spacemoose (daniel@errortown.com)
 */
public class PSServerListener implements Listener {

    /**
     *
     */
    public PSServerListener() {
        PreciousStones plugin = PreciousStones.getInstance();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {

    }
}