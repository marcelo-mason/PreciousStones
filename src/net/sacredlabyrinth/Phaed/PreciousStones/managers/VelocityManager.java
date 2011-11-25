package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

/**
 * @author phaed
 */
public class VelocityManager
{
    private PreciousStones plugin;
    private HashMap<String, Integer> fallDamageImmune = new HashMap<String, Integer>();

    /**
     *
     */
    public VelocityManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param entity
     * @param field
     */
    public void launchPlayer(final Player player, final Field field)
    {
        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.launch"))
        {
            if (plugin.getForceFieldManager().isAllowed(field, player.getName()))
            {

                

                if (field.hasFlag(FieldFlag.LAUNCH))
                {
                    final float launchheight = field.getVelocity() > 0 ? field.getVelocity() : field.getSettings().getLaunchHeight();
                    double speed = 8;

                    Vector loc = player.getLocation().toVector();
                    Vector target = new Vector(field.getX(), field.getY(), field.getZ());

                    final Vector velocity = target.clone().subtract(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    velocity.multiply(speed / velocity.length());
                    velocity.setY(launchheight > 0 ? launchheight : (((player.getLocation().getPitch() * -1) + 90) / 35));
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        public void run()
                        {
                            if (player.getVehicle() != null)
                            {
                                player.getVehicle().setVelocity(velocity);
                            }

                            player.setVelocity(velocity);

                            plugin.getCommunicationManager().showLaunch(player);
                            startFallImmunity(player);
                            player.getWorld().createExplosion(player.getLocation(), -1);
                        }
                    }, 0L);
                }
            }
        }
    }

    /**
     * @param entity
     * @param field
     */
    public void shootPlayer(final Player player, Field field)
    {
        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.bounce"))
        {
            if (plugin.getForceFieldManager().isAllowed(field, player.getName()))
            {


                if (field.hasFlag(FieldFlag.CANNON))
                {
                    final float bounceHeight = field.getVelocity() > 0 ? field.getVelocity() : field.getSettings().getCannonHeight();
                    final float height = bounceHeight > 0 ? bounceHeight : (((player.getLocation().getPitch() * -1) + 90) / 35);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        public void run()
                        {
                            if (player.getVehicle() != null)
                            {
                                player.setVelocity(new Vector(0, height, 0));
                            }

                            player.setVelocity(new Vector(0, height, 0));

                            plugin.getCommunicationManager().showCannon(player);
                            startFallImmunity(player);
                            player.getWorld().createExplosion(player.getLocation(), -1);
                        }
                    }, 0L);
                }
            }
        }
    }

    /**
     * @param player
     */
    public void startFallImmunity(final Player player)
    {
        if (fallDamageImmune.containsKey(player.getName()))
        {
            int current = fallDamageImmune.get(player.getName());

            plugin.getServer().getScheduler().cancelTask(current);
        }

        fallDamageImmune.put(player.getName(), startImmuneRemovalDelay(player));
    }

    /**
     * @param player
     * @return
     */
    public boolean isFallDamageImmune(final Player player)
    {
        return fallDamageImmune.containsKey(player.getName());
    }

    /**
     * @param player
     * @return
     */
    public int startImmuneRemovalDelay(final Player player)
    {
        final String name = player.getName();

        return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                fallDamageImmune.remove(name);
            }
        }, 15 * 20L);
    }
}
