package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author phaed
 */
public class LightningManager {
    private PreciousStones plugin;

    /**
     *
     */
    public LightningManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param player
     * @param field
     */
    public void enterLightning(final Player player, final Field field) {
        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.lightning")) {
            return;
        }

        if (FieldFlag.LIGHTNING.applies(field, player)) {
            final int delay = field.getSettings().getLightningDelaySeconds();
            final Material leftBehind = field.getSettings().getLightningReplaceBlock();

            plugin.getCommunicationManager().showLightning(player);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Block block = plugin.getForceFieldManager().getBlock(field);

                player.getWorld().strikeLightning(player.getLocation());

                plugin.getForceFieldManager().releaseNoDrop(field);
                block.setType(leftBehind);

            }, delay * 20L);
        }

    }
}
