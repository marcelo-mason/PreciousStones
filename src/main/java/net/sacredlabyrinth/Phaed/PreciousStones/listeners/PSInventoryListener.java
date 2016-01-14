package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class PSInventoryListener implements Listener {
    private final PreciousStones plugin;

    public PSInventoryListener() {
        plugin = PreciousStones.getInstance();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && cursor.getType() != Material.AIR &&
            plugin.getSettingsManager().isFieldType(new BlockTypeEntry(cursor), cursor)) {
            event.setCancelled(true);
        } else if (current != null && current.getType() != Material.AIR &&
                plugin.getSettingsManager().isFieldType(new BlockTypeEntry(current), current)) {
            event.setCancelled(true);
        }
    }
}