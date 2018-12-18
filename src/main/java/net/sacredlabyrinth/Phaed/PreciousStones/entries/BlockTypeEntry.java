package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import net.sacredlabyrinth.Phaed.PreciousStones.MaterialName;

/**
 * @author phaed
 */
public class BlockTypeEntry {
    private final Material material;

    /**
     * @param block
     */
    public BlockTypeEntry(Block block) {
        this.material = block.getType();
    }

    public BlockTypeEntry(ItemStack item) {
        this.material = item.getType();
    }

    /**
     * @param material
     */
    public BlockTypeEntry(Material material) {
        this.material = material;
    }

    public BlockTypeEntry(String string) {

        // string
        this.material = MaterialName.getBlockMaterial(string);
    }

    /**
     * @param block
     */
    public BlockTypeEntry(BlockState block) {
        this.material = block.getType();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockTypeEntry)) {
            return false;
        }

        BlockTypeEntry other = (BlockTypeEntry) obj;

        Material id1 = this.getMaterial();
        Material id2 = other.getMaterial();

        return id1 == id2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getMaterial().ordinal();
        return hash;
    }

    @Override
    public String toString() {
        return MaterialName.getIDName(getMaterial());
    }

    public boolean isValid() {
        return getMaterial() != null;
    }

    public Material getMaterial() {
        return material;
    }
}

