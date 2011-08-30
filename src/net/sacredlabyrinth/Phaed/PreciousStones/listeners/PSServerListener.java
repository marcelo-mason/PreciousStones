package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.server.ServerListener;

import net.sacredlabyrinth.register.payment.Methods;
import java.util.logging.Level;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 *
 * @author phaed
 */
public class PSServerListener extends ServerListener
{
    private final PreciousStones plugin;
    private Methods Methods;

    /**
     *
     */
    public PSServerListener()
    {
        plugin = PreciousStones.getInstance();
        Methods = new Methods();
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPluginDisable(PluginDisableEvent event)
    {
        if (Methods != null && Methods.hasMethod())
        {
            Boolean check = Methods.checkDisabled(event.getPlugin());

            if (check)
            {
                plugin.setMethod(null);
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event)
    {
        if (Methods != null && !Methods.hasMethod())
        {
            if (Methods.setMethod(event.getPlugin()))
            {
                plugin.setMethod(Methods.getMethod());
                PreciousStones.log("Payment method: {0} v{1}", plugin.getMethod().getName(), plugin.getMethod().getVersion());
            }
        }
    }
}
