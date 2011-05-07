package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author phaed
 */
public class PSVehicleListener extends VehicleListener
{
    private final PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public PSVehicleListener(PreciousStones plugin)
    {
        this.plugin = plugin;
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

        if (entity instanceof Player)
        {
            Player player = (Player) entity;

            if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
            {
                Field field = plugin.ffm.isEntryProtected(player.getLocation(), player);

                if (field != null)
                {
                    v.setVelocity(new Vector(0,0,0));
                    v.teleport(event.getFrom());
                }
            }
        }
    }
}
