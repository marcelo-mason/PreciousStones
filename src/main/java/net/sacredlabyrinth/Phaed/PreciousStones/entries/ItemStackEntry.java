package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author phaed
 */
public class ItemStackEntry {
    private final Material type;
    private final short durability;
    private final int amount;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();

    /**
     * @param item
     */
    public ItemStackEntry(ItemStack item) {
        this.type = item.getType();
        this.durability = item.getDurability();
        this.enchantments = item.getEnchantments();
        this.amount = item.getAmount();
    }

    /**
     * @param o
     */
    @SuppressWarnings("deprecation")
    public ItemStackEntry(JSONObject o) {
        this.type = Material.valueOf(o.get("type").toString());
        this.durability = Short.parseShort(o.get("dmg").toString());
        this.amount = Integer.parseInt(o.get("a").toString());

        JSONObject ench = (JSONObject) o.get("e");

        if (ench != null) {
            for (Object enchId : ench.keySet()) {
                Integer level = Integer.parseInt(ench.get(enchId).toString());
                this.enchantments.put(Enchantment.getByName(enchId.toString()), level);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public JSONObject serialize() {
        JSONObject ench = new JSONObject();

        for (Enchantment e : enchantments.keySet()) {
            Integer integer = enchantments.get(e);
            ench.put(e.getName(), integer);
        }

        JSONObject out = new JSONObject();

        out.put("type", getType().name());
        out.put("dmg", getDurability());
        out.put("a", getAmount());
        out.put("e", ench);

        return out;
    }

    /**
     * @return the type
     */
    public Material getType() {
        return type;
    }

    public short getDurability() {
        return durability;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStackEntry)) {
            return false;
        }

        ItemStackEntry other = (ItemStackEntry) obj;

        Material id1 = this.getType();
        Material id2 = other.getType();

        return (id1 == id2);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getType().ordinal();
        return hash;
    }

    /**
     * Returns a real itemstack
     *
     * @return
     */
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(getType(), getAmount(), getDurability());

        for (Entry<Enchantment, Integer> ench : enchantments.entrySet()) {
            is.addUnsafeEnchantment(ench.getKey(), Math.min(ench.getValue(), ench.getKey().getMaxLevel()));
        }

        return is;
    }

    @Override
    public String toString() {
        if (getDurability() == 0) {
            return getType().name();
        }

        return getType().name() + ":" + getDurability();
    }
}

