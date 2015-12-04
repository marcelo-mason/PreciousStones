package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author phaed
 */
public class ItemStackEntry {
    private final int typeId;
    private final byte data;
    private final short durability;
    private final int amount;
    private Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();

    /**
     * @param item
     */
    public ItemStackEntry(ItemStack item) {
        this.typeId = item.getTypeId();
        this.data = item.getData().getData();
        this.durability = item.getDurability();
        this.enchantments = item.getEnchantments();
        this.amount = item.getAmount();
    }

    /**
     * @param o
     */
    public ItemStackEntry(JSONObject o) {
        this.typeId = Integer.parseInt(o.get("id").toString());
        this.data = Byte.parseByte(o.get("d").toString());
        this.durability = Short.parseShort(o.get("dmg").toString());
        this.amount = Integer.parseInt(o.get("a").toString());

        JSONObject ench = (JSONObject) o.get("e");

        if (ench != null) {
            for (Object enchId : ench.keySet()) {
                Integer id = Integer.parseInt(enchId.toString());
                Integer level = Integer.parseInt(ench.get(enchId).toString());
                this.enchantments.put(Enchantment.getById(id), level);
            }
        }
    }

    public JSONObject serialize() {
        JSONObject ench = new JSONObject();

        for (Enchantment e : enchantments.keySet()) {
            Integer integer = enchantments.get(e);
            ench.put(e.getId(), integer);
        }

        JSONObject out = new JSONObject();

        out.put("id", getTypeId());
        out.put("d", getData());
        out.put("dmg", getDurability());
        out.put("a", getAmount());
        out.put("e", ench);

        return out;
    }

    /**
     * @return the typeId
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * @return the data
     */
    public byte getData() {
        return data;
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

        int id1 = this.getTypeId();
        int id2 = other.getTypeId();
        byte data1 = this.getData();
        byte data2 = other.getData();

        if (id1 == id2 && data1 == data2) {
            return true;
        }

        // adjust for changing blocks

        if (id1 == 8 && id2 == 9 || id1 == 9 && id2 == 8 || id1 == 11 && id2 == 10 || id1 == 10 && id2 == 11 || id1 == 73 && id2 == 74 || id1 == 74 && id2 == 73 || id1 == 61 && id2 == 62 || id1 == 62 && id2 == 61) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getTypeId();
        hash = 47 * hash + this.getData();
        return hash;
    }

    /**
     * Returns a real itemstack
     *
     * @return
     */
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(getTypeId(), getAmount(), getDurability(), getData());

        for (Enchantment ench : enchantments.keySet()) {
            is.addUnsafeEnchantment(ench, Math.min(enchantments.get(ench), ench.getMaxLevel()));
        }

        return is;
    }

    @Override
    public String toString() {
        if (getData() == 0) {
            return getTypeId() + "";
        }

        if (getDurability() == 0) {
            return getTypeId() + ":" + getData();
        }

        return getTypeId() + ":" + getData() + ":" + getDurability();
    }
}

