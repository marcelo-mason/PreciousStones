package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
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
        DebugTimer dt = new DebugTimer("onVehicleMove");

        Vehicle v = event.getVehicle();
        Entity entity = v.getPassenger();

        if (plugin.getSettingsManager().isBlacklistedWorld(v.getLocation().getWorld()))
        {
            return;
        }

        if (entity instanceof Player)
        {
            Player player = (Player) entity;

            // undo a player's visualization if it exists

            if (plugin.getSettingsManager().isVisualizeEndOnMove())
            {
                if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.admin.visualize"))
                {
                    plugin.getVisualizationManager().revertVisualization(player);
                }
            }

            // remove player form any entry field he is not currently in

            List<Field> entryfields = plugin.getEntryManager().getPlayerEntryFields(player);

            if (entryfields != null)
            {
                for (Field entryfield : entryfields)
                {
                    if (!entryfield.envelops(player.getLocation()))
                    {
                        plugin.getEntryManager().leaveField(player, entryfield);

                        if (!plugin.getEntryManager().containsSameNameOwnedField(player, entryfield))
                        {
                            plugin.getEntryManager().leaveOverlappedArea(player, entryfield);
                        }
                    }
                }
            }

            // check all fields hes standing on and teleport him if hes in a prevent-entry field

            List<Field> currentfields = plugin.getForceFieldManager().getSourceFields(player.getLocation());

            for (Field field : currentfields)
            {
                FieldSettings fs = field.getSettings();

                if (fs.hasFlag(FieldFlag.PREVENT_ENTRY))
                {
                    if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.entry"))
                    {
                        if (!plugin.getForceFieldManager().isAllowed(field, player.getName()))
                        {
                            Location loc = plugin.getPlayerManager().getOutsideFieldLocation(field, player);
                            Location outside = plugin.getPlayerManager().getOutsideLocation(player);

                            if (outside != null)
                            {
                                loc = outside;
                            }

                            v.setVelocity(new Vector(0, 0, 0));
                            v.teleport(loc);
                            player.teleport(loc);
                            plugin.getCommunicationManager().warnEntry(player, field);
                            return;
                        }
                    }
                }
            }

            // he is not inside a prevent entry field so we update his location

            plugin.getPlayerManager().updateOutsideLocation(player);

            // enter all fields hes is not currently entered into yet

            for (Field currentfield : currentfields)
            {
                if (!plugin.getEntryManager().enteredField(player, currentfield))
                {
                    if (!plugin.getEntryManager().containsSameNameOwnedField(player, currentfield))
                    {
                        plugin.getEntryManager().enterOverlappedArea(player, currentfield);
                    }

                    plugin.getEntryManager().enterField(player, currentfield);
                }
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }
}
