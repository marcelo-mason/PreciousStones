package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class FencingModule {
    private Field field;
    private List<BlockEntry> fenceBlocks = new ArrayList<BlockEntry>();

    public FencingModule(Field field) {
        this.field = field;
    }

    public int getFencePrice() {
        return fenceBlocks.size() * field.getSettings().getFenceItemPrice();
    }

    /**
     * Generate fence around the field
     */
    public void generateFence(int item) {
        PreciousStones plugin = PreciousStones.getInstance();

        World world = Bukkit.getServer().getWorld(field.getWorld());

        if (world == null) {
            return;
        }

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        int mid = field.getY();

        if (field.hasFlag(FieldFlag.CUBOID)) {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        int limity = Math.min(plugin.getSettingsManager().getFenceMaxDepth(), miny);

        // traveling the z length

        for (int z = minz; z <= maxz; z++) {
            int sideOneMidId = world.getBlockTypeIdAt(minx, mid, z);

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    int sideOne = world.getBlockTypeIdAt(minx, y, z);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        int sideOne = world.getBlockTypeIdAt(minx, y, z);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                            continue;
                        }

                        Block block = world.getBlockAt(minx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }

            int sideTwoMidId = world.getBlockTypeIdAt(maxx, mid, z);

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                            continue;
                        }

                        Block block = world.getBlockAt(maxx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }
        }

        // traveling the x length

        for (int x = minx; x <= maxx; x++) {
            int sideOneMidId = world.getBlockTypeIdAt(x, mid, minz);

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    int sideOne = world.getBlockTypeIdAt(x, y, minz);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        int sideOne = world.getBlockTypeIdAt(x, y, minz);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, minz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }

            int sideTwoMidId = world.getBlockTypeIdAt(x, mid, maxz);

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, maxz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }
        }
    }

    /**
     * Remove fence from around the field
     */
    public void clearFence() {

    }

}
