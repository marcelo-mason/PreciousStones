package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
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
        if (new Vec(event.getFrom()).equals(new Vec(event.getTo())))
        {
            return;
        }

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

            if (!plugin.pm.hasPermission(player, "preciousstones.admin.visualize") && plugin.settings.visualizeEndOnMove)
            {
                plugin.viz.revertVisualization(player);
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
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                if (fieldsettings == null)
                {
                    plugin.ffm.queueRelease(field);
                    return;
                }

                if (!plugin.pm.hasPermission(player, "preciousstones.bypass.entry"))
                {
                    if (fieldsettings.preventEntry)
                    {
                        if (!plugin.ffm.isAllowed(field, player.getName()))
                        {
                            v.setVelocity(new Vector(0, 0, 0));
                            v.teleport(event.getFrom());
                            player.teleport(event.getFrom());
                            plugin.cm.warnEntry(player, field);
                            return;
                        }
                    }
                }
            }

            // enter all fields hes is not currently entered into yet

            if (currentfields != null)
            {
                for (Field currentfield : currentfields)
                {
                    if (!plugin.em.isInsideField(player, currentfield))
                    {
                        if (!plugin.em.containsSameNameOwnedField(player, currentfield))
                        {
                            plugin.em.enterOverlappedArea(player, currentfield);
                        }

                        plugin.em.enterField(player, currentfield);
                    }
                }
            }
        }

        if (plugin.settings.debug)
        {
           dt.logProcessTime();
        }
    }
}
