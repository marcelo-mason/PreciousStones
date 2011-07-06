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

            List<Field> currentfields = plugin.ffm.getSourceFields(player);

            // check if were on a prevent entry field the player is no allowed in

            for (Field field : currentfields)
            {
                if (field.isAllowed(player.getName()))
                {
                    continue;
                }

                if (plugin.stm.isTeamMate(player.getName(), field.getOwner()))
                {
                    continue;
                }

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
                        v.setVelocity(new Vector(0, 0, 0));
                        v.teleport(event.getFrom());
                        plugin.cm.warnEntry(player, field);
                        break;
                    }
                }
            }

            // loop through all fields the player just moved into

            if (currentfields != null)
            {
                for (Field currentfield : currentfields)
                {
                    if (!plugin.em.isInsideField(player, currentfield))
                    {
                        if (!plugin.em.containsSameNameOwnedField(player, currentfield))
                        {
                            FieldSettings fieldsettings = plugin.settings.getFieldSettings(currentfield);

                            if (fieldsettings == null)
                            {
                                plugin.ffm.queueRelease(currentfield);
                                return;
                            }

                            if (fieldsettings.welcomeMessage)
                            {
                                if (currentfield.getName().length() > 0)
                                {
                                    plugin.cm.showWelcomeMessage(player, currentfield.getName());
                                }
                            }
                        }

                        plugin.em.enterField(player, currentfield);
                    }
                }
            }

            // remove all stored entry fields that the player is no longer currently in

            LinkedList<Field> entryfields = plugin.em.getPlayerEntryFields(player);

            if (entryfields != null)
            {
                if (currentfields != null)
                {
                    entryfields.removeAll(currentfields);
                }

                for (Field entryfield : entryfields)
                {
                    plugin.em.leaveField(player, entryfield);

                    if (!plugin.em.containsSameNameOwnedField(player, entryfield))
                    {
                        FieldSettings fieldsettings = plugin.settings.getFieldSettings(entryfield);

                        if (fieldsettings == null)
                        {
                            plugin.ffm.queueRelease(entryfield);
                            return;
                        }

                        if (fieldsettings.farewellMessage)
                        {
                            if (entryfield.getName().length() > 0)
                            {
                                plugin.cm.showFarewellMessage(player, entryfield.getName());
                            }
                        }
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
