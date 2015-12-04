package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LWCListener implements Listener {
    private PreciousStones plugin;

    /**
     *
     */
    public LWCListener() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Cancels McMMO block breaking on fields
     *
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onProtectionRegister(LWCProtectionRegisterEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player == null || block == null) {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PROTECT_LWC);

        if (field != null) {
            if (FieldFlag.PROTECT_LWC.applies(field, player)) {
                event.setCancelled(true);
                ChatHelper.send(player, "notAllowedToCreateLWC");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProtectionRemove(LWCProtectionDestroyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getProtection().getBlock();

        if (player == null || block == null) {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PROTECT_LWC);

        if (field != null) {
            if (FieldFlag.PROTECT_LWC.applies(field, player)) {
                event.setCancelled(true);
                ChatHelper.send(player, "notAllowedToDestroyLWC");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getProtection().getBlock();

        if (player == null || block == null) {
            return;
        }

        plugin.getSnitchManager().recordSnitchLWC(player, block, event.getActions());
    }
}
