package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

/**
 * @author phaed
 */
public class VelocityManager {
    private PreciousStones plugin;
    private HashMap<String, Integer> fallDamageImmune = new HashMap<String, Integer>();

    /**
     *
     */
    public VelocityManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param player
     * @param field
     */
    public void launchPlayer(final Player player, final Field field) {
        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.launch")) {
            if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking())) {
                if (FieldFlag.LAUNCH.applies(field, player)) {
                    final float height = field.getVelocity() > 0 ? field.getVelocity() : field.getSettings().getLaunchHeight();
                    double speed = 8;

                    Vector loc = player.getLocation().toVector();
                    Vector target = new Vector(field.getX(), field.getY(), field.getZ());

                    final Vector velocity = target.clone().subtract(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    velocity.multiply(speed / velocity.length());
                    velocity.setY(height > 0 ? height : (((player.getLocation().getPitch() * -1) + 90) / 35));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.getPermissionsManager().allowFly(player);
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
     * @param player
     * @param field
     */
    public void shootPlayer(final Player player, Field field) {
        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.bounce")) {
            if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking())) {
                if (FieldFlag.CANNON.applies(field, player)) {
                    final float bounceHeight = field.getVelocity() > 0 ? field.getVelocity() : field.getSettings().getCannonHeight();
                    final float height = bounceHeight > 0 ? bounceHeight : (((player.getLocation().getPitch() * -1) + 90) / 35);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.getPermissionsManager().allowFly(player);
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
    public void startFallImmunity(final Player player) {
        if (fallDamageImmune.containsKey(player.getName())) {
            int current = fallDamageImmune.get(player.getName());

            Bukkit.getScheduler().cancelTask(current);
        }

        fallDamageImmune.put(player.getName(), startImmuneRemovalDelay(player));
    }

    /**
     * @param player
     * @return
     */
    public boolean isFallDamageImmune(final Player player) {
        return fallDamageImmune.containsKey(player.getName());
    }

    /**
     * @param player
     * @return
     */
    public int startImmuneRemovalDelay(final Player player) {
        final String name = player.getName();

        return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                fallDamageImmune.remove(name);
                plugin.getPermissionsManager().resetFly(player);
            }
        }, 15 * 20L);
    }
}
