package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
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
        if (!plugin.tm.isTaggedArea(new ChunkVec(event.getTo().getBlock().getChunk())))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onVehicleMove");

        Vehicle v = event.getVehicle();
        Entity entity = v.getPassenger();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;

            // undo a player's visualization if it exists

            if (plugin.settings.visualizeEndOnMove)
            {
                if (!plugin.pm.hasPermission(player, "preciousstones.admin.visualize"))
                {
                    plugin.viz.revertVisualization(player);
                }
            }

            // remove player form any entry field he is not currently in

            LinkedList<Field> entryfields = plugin.em.getPlayerEntryFields(player);

            if (entryfields != null)
            {
                for (Field entryfield : entryfields)
                {
                    if (!entryfield.envelops(player.getLocation()))
                    {
                        plugin.em.leaveField(player, entryfield);

                        if (!plugin.em.containsSameNameOwnedField(player, entryfield))
                        {
                            plugin.em.leaveOverlappedArea(player, entryfield);
                        }
                    }
                }
            }

            // check all fields hes standing on and teleport him if hes in a prevent-entry field

            List<Field> currentfields = plugin.ffm.getSourceFields(player.getLocation());

            for (Field field : currentfields)
            {
                FieldSettings fs = plugin.settings.getFieldSettings(field);

                if (fs == null)
                {
                    plugin.ffm.queueRelease(field);
                    continue;
                }

                if (fs.preventEntry)
                {
                    if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
                    {
                        if (!plugin.ffm.isAllowed(field, player.getName()))
                        {
                            Location loc = plugin.plm.getOutsideFieldLocation(field, player);
                            Location outside = plugin.plm.getOutsideLocation(player);

                            if (outside != null)
                            {
                                loc = outside;
                            }

                            v.setVelocity(new Vector(0, 0, 0));
                            v.teleport(loc);
                            player.teleport(loc);
                            plugin.cm.warnEntry(player, field);
                            return;
                        }
                    }
                }
            }

            // he is not inside a prevent entry field so we update his location

            plugin.plm.updateOutsideLocation(player);

            // enter all fields hes is not currently entered into yet

            for (Field currentfield : currentfields)
            {
                if (!plugin.em.enteredField(player, currentfield))
                {
                    if (!plugin.em.containsSameNameOwnedField(player, currentfield))
                    {
                        plugin.em.enterOverlappedArea(player, currentfield);
                    }

                    plugin.em.enterField(player, currentfield);
                }
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }
}
