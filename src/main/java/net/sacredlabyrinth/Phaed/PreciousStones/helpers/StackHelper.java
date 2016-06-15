package net.sacredlabyrinth.Phaed.PreciousStones.helpers;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StackHelper {
    
    private StackHelper() {
        
    }
    
    public static void unHoldItem(Player player, int slot) {
        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItem(slot);
        int empty = -1;

        if (item != null) {
            for (int i = 9; i <= 35; i++) {
                ItemStack test = inv.getItem(i);

                if (test == null) {
                    empty = i;
                    break;
                }
            }

            if (empty == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                inv.setItem(empty, item);
            }

            inv.setItem(slot, new ItemStack(Material.AIR));
            player.updateInventory();
        }
    }

    public static void remove(Player player, BlockTypeEntry item, int amount) {
        for (ItemStack stack : makeStacks(item, amount)) {
            player.getInventory().removeItem(stack);
        }
        player.updateInventory();
    }

    public static void give(Player player, BlockTypeEntry item, int amount) {
        for (ItemStack stack : makeStacks(item, amount)) {
            HashMap<Integer, ItemStack> rem = player.getInventory().addItem(stack);

            if (rem != null && !rem.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.CHEST));

                for (ItemStack is : rem.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), is);
                }
            }
        }

        player.updateInventory();
    }

    public static void give(Player player, ItemStack stack) {
        HashMap<Integer, ItemStack> rem = player.getInventory().addItem(stack);

        if (rem != null && !rem.isEmpty()) {
            for (ItemStack is : rem.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), is);
            }
        }

        player.updateInventory();
    }

    public static List<ItemStack> makeStacks(BlockTypeEntry item, int amount) {
        List<ItemStack> out = new ArrayList<ItemStack>();

        Material material = Material.getMaterial(item.getTypeId());

        int blocks = amount / 64;

        for (int i = 0; i < blocks; i++) {
            ItemStack is = new ItemStack(material, 64);
            is.setDurability(item.getData());
            out.add(is);
        }

        int remainder = amount % 64;

        if (remainder > 0) {
            ItemStack is = new ItemStack(material, remainder);
            is.setDurability(item.getData());
            out.add(is);
        }

        return out;
    }

    public static boolean hasItems(Player player, BlockTypeEntry item, int amount) {
        for (ItemStack i : player.getInventory()) {
            if (i == null) {
                continue;
            }

            if (i.getTypeId() == item.getTypeId()) {
                if (item.getData() == 0 || i.getData().getData() == item.getData()) {
                    amount -= i.getAmount();
                }
            }
        }

        if (amount <= 0) {
            return true;
        }

        return false;
    }

    /**
     * Sets meta data to an item stack
     *
     * @param is
     * @param settings
     */
    public static void setItemMeta(ItemStack is, FieldSettings settings){
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(settings.getMetaName());
        meta.setLore(settings.getMetaLore());
        is.setItemMeta(meta);
    }
}
