package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
 */
public class VelocityManager
{
    private PreciousStones plugin;
    private HashMap<String, Integer> fallDamageImmune = new HashMap<String, Integer>();

    /**
     *
     * @param plugin
     */
    public VelocityManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param player
     * @param field
     */
    public void launchPlayer(final Player player, final Field field)
    {
        if (plugin.pm.hasPermission(player, "preciousstones.benefit.launch"))
        {
            if (field.isAllowed(player.getName()))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                final float launchheight = field.getVelocity() > 0 ? field.getVelocity() : fieldsettings.launchHeight;
                double speed = 8;

                Vector loc = player.getLocation().toVector();
                Vector target = new Vector(field.getX(), field.getY(), field.getZ());

                final Vector velocity = target.clone().subtract(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                velocity.multiply(speed / velocity.length());
                velocity.setY(launchheight > 0 ? launchheight : (((player.getLocation().getPitch() * -1) + 90) / 35));

                if (fieldsettings.launch)
                {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            player.setVelocity(velocity);
                            plugin.cm.showLaunch(player);
                            startFallImmunity(player);
                        }
                    }, 0L);
                }
            }
        }
    }

    /**
     *
     * @param player
     * @param field
     */
    public void shootPlayer(final Player player, Field field)
    {
        if (plugin.pm.hasPermission(player, "preciousstones.benefit.bounce"))
        {
            if (field.isAllowed(player.getName()))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                final float bounceHeight = field.getVelocity() > 0 ? field.getVelocity() : fieldsettings.cannonHeight;
                final float height = bounceHeight > 0 ? bounceHeight : (((player.getLocation().getPitch() * -1) + 90) / 35);

                if (fieldsettings.cannon)
                {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            player.setVelocity(new Vector(0, height, 0));
                            plugin.cm.showCannon(player);
                            startFallImmunity(player);
                        }
                    }, 0L);
                }
            }
        }
    }

    /**
     *
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
     *
     * @param player
     * @return
     */
    public boolean isFallDamageImmune(final Player player)
    {
        return fallDamageImmune.containsKey(player.getName());
    }

    /**
     *
     * @param player
     * @return
     */
    public int startImmuneRemovalDelay(final Player player)
    {
        final String name = player.getName();

        return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                fallDamageImmune.remove(name);
            }
        }, 15 * 20L);
    }
}
