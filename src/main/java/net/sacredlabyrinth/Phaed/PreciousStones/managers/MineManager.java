package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author phaed
 */
public class MineManager {
    private PreciousStones plugin;

    /**
     *
     */
    public MineManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param player
     * @param field
     */
    public void enterMine(final Player player, final Field field) {
        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.mine")) {
            return;
        }

        if (FieldFlag.MINE.applies(field, player)) {
            final int delay = field.getSettings().getMineDelaySeconds();
            final Block block = plugin.getForceFieldManager().getBlock(field);

            if (!plugin.getPermissionsManager().canBuild(player, block.getLocation())) {
                return;
            }

            plugin.getCommunicationManager().showMine(player);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.getForceFieldManager().releaseNoDrop(field);

                    block.getWorld().createExplosion(block.getLocation(), field.getSettings().getMineStrength(), field.getSettings().isMineHasFire());
                }
            }, delay * 20L);
        }
    }
}
