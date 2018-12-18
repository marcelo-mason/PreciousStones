package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class FencingModule {
    private Field field;
    private List<BlockEntry> fenceBlocks = new ArrayList<>();

    public FencingModule(Field field) {
        this.field = field;
    }

    public int getFencePrice() {
        return fenceBlocks.size() * field.getSettings().getFenceItemPrice();
    }

    /**
     * Generate fence around the field
     */
    public void generateFence(Material item) {
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
            Material sideOneMidId = world.getBlockAt(minx, mid, z).getType();

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    Material sideOne = world.getBlockAt(minx, y, z).getType();

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        Material sideOne = world.getBlockAt(minx, y, z).getType();

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                            continue;
                        }

                        Block block = world.getBlockAt(minx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setType(item);
                    }
                }
            }

            Material sideTwoMidId = world.getBlockAt(maxx, mid, z).getType();

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    Material sideTwo = world.getBlockAt(maxx, y, z).getType();

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        Material sideTwo = world.getBlockAt(maxx, y, z).getType();

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                            continue;
                        }

                        Block block = world.getBlockAt(maxx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setType(item);
                    }
                }
            }
        }

        // traveling the x length

        for (int x = minx; x <= maxx; x++) {
            Material sideOneMidId = world.getBlockAt(x, mid, minz).getType();

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    Material sideOne = world.getBlockAt(x, y, minz).getType();

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        Material sideOne = world.getBlockAt(x, y, minz).getType();

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne)) {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, minz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setType(item);
                    }
                }
            }

            Material sideTwoMidId = world.getBlockAt(x, mid, maxz).getType();

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId)) {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--) {
                    Material sideTwo = world.getBlockAt(x, y, maxz).getType();

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor) {
                    for (int y = mid; y >= limity; y--) {
                        Material sideTwo = world.getBlockAt(x, y, maxz).getType();

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo)) {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, maxz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setType(item);
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
