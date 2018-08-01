package net.sacredlabyrinth.Phaed.PreciousStones.visualization;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phaed
 */
public class Visualization {
    private List<BlockEntry> blocks = new ArrayList<>();
    private List<BlockEntry> outlineBlocks = new ArrayList<>();
    private List<Field> fields = new ArrayList<>();

    /**
     * @param block
     */
    public void addBlock(Block block) {
        blocks.add(new BlockEntry(block));
    }

    public void addBlock(Location loc, Material material) {
        BlockEntry bd = new BlockEntry(loc, material);

        if (!blocks.contains(bd)) {
            blocks.add(bd);
        }
    }

    /**
     * @param field
     */
    public void addField(Field field) {
        fields.add(field);
    }

    /**
     * Remove the latest added block
     *
     * @return
     */
    public void undoBlock() {
        if (blocks.size() > 1) {
            blocks.remove(blocks.size() - 1);
        }
    }

    /**
     * @return the locations
     */
    public List<BlockEntry> getBlocks() {
        return blocks;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields() {
        List<Field> f = new ArrayList<>();
        f.addAll(fields);
        return f;
    }

    public void setBlocks(List<BlockEntry> bds) {
        this.blocks = bds;
    }

    public List<BlockEntry> getOutlineBlocks() {
        return outlineBlocks;
    }

    public void setOutlineBlocks(List<BlockEntry> outlineBlocks) {
        this.outlineBlocks = outlineBlocks;
    }
}
