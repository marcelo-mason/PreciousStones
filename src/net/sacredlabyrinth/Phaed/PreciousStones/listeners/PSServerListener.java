package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/**
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
     * @param event
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event)
    {
        if (!Methods.hasMethod())
        {
            if (Methods.setMethod(plugin.getServer().getPluginManager()))
            {
                plugin.setMethod(Methods.getMethod());
                PreciousStones.log("Payment method: {0} v{1}", plugin.getMethod().getName(), plugin.getMethod().getVersion());
            }
        }
    }
}
