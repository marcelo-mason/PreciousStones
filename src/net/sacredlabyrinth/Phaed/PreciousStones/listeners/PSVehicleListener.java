package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 *
 * @author phaed
 */
public class PSVehicleListener extends VehicleListener
{
    private final PreciousStones plugin;

    /**
     *
     */
    public PSVehicleListener()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     *
     * @param event
     */
    @Override
    public void onVehicleMove(VehicleMoveEvent event)
    {
        Vehicle v = event.getVehicle();
        Entity entity = v.getPassenger();

        if (plugin.getSettingsManager().isBlacklistedWorld(v.getLocation().getWorld()))
        {
            return;
        }

        if(!(entity instanceof Player))
        {
            return;
        }

        plugin.getPlayerListener().onPlayerMove(new PlayerMoveEvent((Player) entity, event.getFrom(), event.getTo()));
    }
}
