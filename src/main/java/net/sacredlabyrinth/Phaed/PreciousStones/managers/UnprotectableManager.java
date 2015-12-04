package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Handles unprotectable blocks
 *
 * @author Phaed
 */
public class UnprotectableManager {
    private PreciousStones plugin;

    /**
     *
     */
    public UnprotectableManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Whether the block is touching an unprotectable block
     *
     * @param block the block that has been placed
     * @return true if an unprotectable is found touching it
     */
    public boolean touchingUnprotectableBlock(Block block) {
        return getTouchingUnprotectableBlock(block) != null;
    }

    /**
     * If the block is touching an unprotectable block return it
     *
     * @param block the block that has been placed
     * @return the offending unprotectable block
     */
    public Block getTouchingUnprotectableBlock(Block block) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    Block touching = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);

                    if (plugin.getSettingsManager().isUnprotectableType(touching)) {
                        return block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
                    }
                }
            }
        }

        return null;
    }

    /**
     * If an unprotectable block exists inside the field return it
     *
     * @param fieldblock the block that contains the field
     * @return the offending block
     */
    public Block existsUnprotectableBlock(Block fieldblock) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldblock);

        if (fs == null) {
            return null;
        }

        int minx = fieldblock.getX() - fs.getRadius();
        int maxx = fieldblock.getX() + fs.getRadius();
        int minz = fieldblock.getZ() - fs.getRadius();
        int maxz = fieldblock.getZ() + fs.getRadius();

        int miny;
        int maxy;

        if (fs.getCustomHeight() > 0) {
            miny = fieldblock.getY() - (Math.max(fs.getCustomHeight() - 1, 0) / 2);
            maxy = fieldblock.getY() + (Math.max(fs.getCustomHeight() - 1, 0) / 2);
        } else {
            miny = fieldblock.getY() - (Math.max(fs.getRadius() - 1, 0) / 2);
            maxy = fieldblock.getY() + (Math.max(fs.getRadius() - 1, 0) / 2);
        }

        Field field = plugin.getForceFieldManager().getField(fieldblock);

        if (field != null) {
            minx = field.getMinx();
            maxx = field.getMaxx();
            minz = field.getMinz();
            maxz = field.getMaxz();
            miny = field.getMiny();
            maxy = field.getMaxy();
        }

        World world = fieldblock.getWorld();

        for (int x = minx; x <= maxx; x++) {
            for (int z = minz; z <= maxz; z++) {
                for (int y = miny; y <= maxy; y++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    int type = world.getBlockTypeIdAt(x, y, z);

                    if (plugin.getSettingsManager().isUnprotectableType(new BlockTypeEntry(type))) {
                        return fieldblock.getWorld().getBlockAt(x, y, z);
                    }
                }
            }
        }

        return null;
    }

    /**
     * If an unprotectable block exists inside the field return it
     *
     * @param field the block that contains the field
     * @return the offending block
     */
    public Block existsUnprotectableBlock(Field field) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field.getSettings().getTypeEntry());

        if (fs == null) {
            return null;
        }

        int minx = field.getMinx();
        int maxx = field.getMaxx();
        int minz = field.getMinz();
        int maxz = field.getMaxz();
        int miny = field.getMiny();
        int maxy = field.getMaxy();

        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            for (int x = minx; x <= maxx; x++) {
                for (int z = minz; z <= maxz; z++) {
                    for (int y = miny; y <= maxy; y++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        int type = world.getBlockTypeIdAt(x, y, z);

                        if (plugin.getSettingsManager().isUnprotectableType(new BlockTypeEntry(type))) {
                            return field.getBlock().getWorld().getBlockAt(x, y, z);
                        }
                    }
                }
            }
        }
        return null;
    }
}
