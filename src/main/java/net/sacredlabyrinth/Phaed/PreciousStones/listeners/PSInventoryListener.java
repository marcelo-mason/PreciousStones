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

    protected boolean isMetaField(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.hasItemMeta() && plugin.getSettingsManager().isFieldType(new BlockTypeEntry(item), item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryType inventoryType = event.getInventory().getType();
        InventoryType.SlotType slotType = event.getSlotType();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (inventoryType == InventoryType.ANVIL) {
            // Have to check "current" here as well to avoid shift+clicks
            if (isMetaField(cursor) || isMetaField(current)) {
                event.setCancelled(true);
            }
        } else if (slotType == InventoryType.SlotType.CRAFTING && (inventoryType == InventoryType.CRAFTING || inventoryType == InventoryType.WORKBENCH)) {
            if (isMetaField(cursor)) {
                event.setCancelled(true);
            }
        }
    }
}